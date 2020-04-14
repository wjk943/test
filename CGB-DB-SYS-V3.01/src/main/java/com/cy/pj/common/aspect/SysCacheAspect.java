package com.cy.pj.common.aspect;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SysCacheAspect {

	private Map<String,Object> cacheMap = new ConcurrentHashMap<>();
	
	@Pointcut("@annotation(com.cy.pj.common.annotation.RequiredCache)")
	public void doCachePointCut() {}
	
	@Around("doCachePointCut()")
	public Object around(ProceedingJoinPoint jp) throws Throwable{
		System.out.println("get data from cache");
		Object obj=cacheMap.get("dept.findObjects");
		if(obj!=null) return obj;
		Object result = jp.proceed();
		System.out.println("put data from cache");
		cacheMap.put("dept.findObjects", result);
		return result;
	}
	
	@AfterReturning("@annotation(com.cy.pj.common.annotation.ClearCache)")
	public void clear() {
		System.out.println("======clear========");
		cacheMap.clear();
	}
}
