package com.framework.spring.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})   //field:可用于域上
@Documented    //注解应该被 javadoc工具记录
@Retention(RetentionPolicy.RUNTIME)  //生命周期
public @interface XINAutowired {
	String value() default ""; //默认值空
}