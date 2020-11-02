package com.framework.spring.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.framework.spring.annotation.XINAutowired;
import com.framework.spring.annotation.XINController;
import com.framework.spring.annotation.XINRequestMapping;
import com.framework.spring.annotation.XINService;

public class XINDispatcherServlet extends HttpServlet{
	private static final long serialVersionUID = 1L;
	
	private Properties properties = new Properties();
	
	//享元模式， 缓存
    private List<String> classNames = new ArrayList<String>();
	
    //Ioc容器,key默认是类名首字母小写，value就是对应的实例对象
    private Map<String,Object> ioc = new HashMap<String,Object>();
    
    //映射方法map
    private Map<String,Method> handlerMap  = new HashMap<String,Method>();
    
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doPost(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		//6 等待用户请求
        doDispatcher(req,resp);
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		//1、加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        //2、扫描相关的类
        doScanner(properties.getProperty("scanPackage"));

        //3、初始化Ioc容器，将扫描到的相关的类实例化，保存到Ioc容器中
        doInstance();

        //4、DI 依赖注入
        doAutowired();
        //================SPRING END====================
        
        //================MVC部分====================
        //5、初始化HandlerMapping
        doInitHandlerMapping();
        System.out.println("Spring init success !!!");
	}

	private void doDispatcher(HttpServletRequest req,HttpServletResponse resp) {
		String url = req.getRequestURI();
		String contextPath = req.getContextPath();
		String uri = url.replace(contextPath, "").replaceAll("/+", "/");
		if(!handlerMap.containsKey(uri)) {
			try {
				resp.getWriter().write("not found 404!");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return ;
		}
		
		Map<String, String[]> parameterMap = req.getParameterMap();
		Method method = handlerMap.get(uri);
		String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName());
		try {
			method.invoke(ioc.get(beanName), new Object[] {req,resp,parameterMap.get("name")[0]});
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	//把映射对应方法放到handlermapping容器
	private void doInitHandlerMapping() {
		if(ioc.isEmpty()) return;
		for(String key : ioc.keySet()) {
			Class<? extends Object> clazz = ioc.get(key).getClass();
			if(!clazz.isAnnotationPresent(XINController.class)) continue;
			String baseUrl = "" ; //controller的映射
			if(clazz.isAnnotationPresent(XINRequestMapping.class)) {
				XINRequestMapping requestMappings = clazz.getAnnotation(XINRequestMapping.class);
				baseUrl = requestMappings.value() ;
			}
			for(Method method : clazz.getMethods()) {
				if(method.isAnnotationPresent(XINRequestMapping.class)) {
					XINRequestMapping requestMappings = method.getAnnotation(XINRequestMapping.class);
					String url = requestMappings.value();
					//拼接完整映射url,将多个//替换成/
					url = (baseUrl + "/" + url).replace("/+", "/");
					this.handlerMap.put(url, method);
				}
			}
			
		}
	}
	
	//DI 依赖注入
	private void doAutowired() {
		if(ioc.isEmpty()) return;
		//遍历所有实例，依次取出所有字段
		for(String key : ioc.keySet()) {
			for(Field field : ioc.get(key).getClass().getDeclaredFields()) {
				if(!field.isAnnotationPresent(XINAutowired.class)) continue;
				//如果@service里面没有值，取类名
				String beanName = field.getAnnotation(XINAutowired.class).value().trim();
				if("".equals(beanName)) {
					beanName = field.getType().getName();
				}
				//反射暴力访问
				field.setAccessible(true);
				try {
					// set(实例，赋值)
					field.set(ioc.get(key), ioc.get(beanName));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	//3 ioc创建实例
	private void doInstance() {
		if(classNames.isEmpty()) return;
        try {
            for (String className : classNames) {
                Class<?> clazz = Class.forName(className);
                if(clazz.isAnnotationPresent(XINController.class)) {
                	//Spring中beanid默认是类名首字母小写
                    String beanId = toLowerFirstCase(clazz.getSimpleName());
                    Object instance = clazz.newInstance();
                    ioc.put(beanId,instance);
                } else if(clazz.isAnnotationPresent(XINService.class)) {
                	//1、获取@service("beanName") 中 beanName
                    String beanName = clazz.getAnnotation(XINService.class).value();

                    //2、没有 beanName ,用默认的类名首字母小写
                    if("".equals(beanName.trim())) {  //trim() 去空格
                        beanName = toLowerFirstCase(clazz.getSimpleName());
                    }
                    Object instance = clazz.newInstance();
                    ioc.put(beanName,instance);

                    //3 取出接口所有实现类
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> i : interfaces) {
                    	if(ioc.containsKey(i.getName())) throw new Exception("this interface:"+i.getName()+"is exsist!");
						ioc.put(i.getName(), instance) ;
					}
                } else {
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	//2 扫描配置文件下的包 将包名放入list中
	private void doScanner(String property) {
		URL url = this.getClass().getClassLoader().getResource("/" + property.replaceAll("\\.", "/"));
        File files = new File(url.getFile());
        //当成是一个ClassPath文件夹
        for (File file : files.listFiles()) {
            if(file.isDirectory()){
                doScanner(property + "." + file.getName());
            } else {
                if(!file.getName().endsWith(".class")) {continue;}
                String className = property + "." + file.getName().replace(".class", "");
                //使用Class.forName(className)反射
                classNames.add(className);
            }
        }
    }
	
	//1 初始化加载Dispacherservlet的配置文件
	private void doLoadConfig(String initParameter) {
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(initParameter);
		try {
			properties.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			if(inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	//首字母转小写  大小写字母ascii值差32
	private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
