package com.framework.spring.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.swing.DefaultBoundedRangeModel;

@Target({ElementType.TYPE,ElementType.METHOD})   //注解使用范围  ElementType:范围枚举集合,type:类或者接口上
@Documented    //注解应该被 javadoc工具记录
@Retention(RetentionPolicy.RUNTIME)  //生命周期
public @interface XINRequestMapping {
	String value() default "" ;
}
