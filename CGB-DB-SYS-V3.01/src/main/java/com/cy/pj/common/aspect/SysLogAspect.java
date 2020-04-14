package com.cy.pj.common.aspect;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.cy.pj.common.annotation.RequiredLog;
import com.cy.pj.sys.entity.SysLog;
import com.cy.pj.sys.service.SysLogService;

import lombok.extern.slf4j.Slf4j;

//@Order(Integer.MIN_VALUE)
@Aspect
@Component
@Slf4j
public class SysLogAspect {

	/*@Pointcut注解用于描述方法  描述的方法不需要写具体内容  底层运行是
	 * 会获取方法上的注解  并拿到注解中定义的表达式
	 * 
	 * */
	@Pointcut("bean(sysUserServiceImpl)")
	public void logPointCut() {}
	
	@Around("logPointCut()")
	public Object around(ProceedingJoinPoint jp)
	throws Throwable{
		try {
			long start = System.currentTimeMillis();
			log.info("start:" + start);
			Object result = jp.proceed();
			long end = System.currentTimeMillis();
			log.info("after:" + end);
			saveLog(jp,end-start );
			return result;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	@Autowired
	private SysLogService sysLogService;                
	
	
	private void saveLog(ProceedingJoinPoint jp, long time) throws Exception, SecurityException {
		Class<?> targetClass = jp.getTarget().getClass();
		MethodSignature signature = (MethodSignature)jp.getSignature();
		String method = targetClass.getName() + "." + signature.getName();
		String params = Arrays.toString(jp.getArgs());
		
		
		Method targetMethod = targetClass.getMethod(signature.getName(), signature.getParameterTypes());
		RequiredLog annotation = targetMethod.getAnnotation(RequiredLog.class);
		String operation = "operation";
		if(annotation!=null) {
			operation = annotation.value();
		}
		SysLog log = new SysLog();
		log.setIp("192.168.1.1");
		log.setUsername("cgb1910");
		log.setOperation(operation); 
		log.setMethod(method);
		log.setParams(params);
		log.setTime(time);
		log.setCreatedTime(new Date());
		sysLogService.saveObject(log);
	}
}
