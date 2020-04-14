package com.cy.pj.sys.controller;


import org.apache.shiro.SecurityUtils;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cy.pj.common.vo.JsonResult;
import com.cy.pj.sys.entity.SysUser;
import com.cy.pj.sys.service.SysUserService;

@RestController
@RequestMapping("/user/")
public class SysUserController {

	 @Autowired
	 private SysUserService sysUserService;
	 
	 @RequestMapping("doFindObjectById")
	 public JsonResult doFindObjectById(Integer id) {
		 return new JsonResult(sysUserService.findObjectById(id));
	 }
	 
	 @RequestMapping("doUpdateObject")
	 public JsonResult doUpdateObject(SysUser entity,Integer[] roleIds) {
		 sysUserService.updateObject(entity, roleIds);
		 return new JsonResult("update ok");
	 }
	 @RequestMapping("doSaveObject")
	 public JsonResult doSaveObject(SysUser entity,Integer[] roleIds) {
		 sysUserService.saveObject(entity, roleIds);
		 return new JsonResult("save ok");
	 }
	 
	 @RequestMapping("doValidById")
	 public JsonResult doValidById(Integer id,Integer valid) {
		 sysUserService.validById(id, valid);
		 return new JsonResult("update ok");
	 }
	 
	 @RequestMapping("doFindPageObjects")
	 public JsonResult doFindPageObjects(String username,Integer pageCurrent) {
		 return new JsonResult(sysUserService.findPageObjects(username, pageCurrent));
	 }

	 @RequestMapping("doLogin")
	 public JsonResult doLogin(String username, String password) {
		 Subject subject = SecurityUtils.getSubject();
		 UsernamePasswordToken token = new UsernamePasswordToken(username, password);
		 subject.login(token);
		 return new JsonResult("login ok");
	 }
	 
	 
	 @ExceptionHandler(ShiroException.class)
		public JsonResult doHandleShiroException(
				ShiroException e) {
			JsonResult r=new JsonResult();
			r.setState(0);
			if(e instanceof UnknownAccountException) {
				r.setMessage("账户不存在");
			}else if(e instanceof LockedAccountException) {
				r.setMessage("账户已被禁用");
			}else if(e instanceof IncorrectCredentialsException) {
				r.setMessage("密码不正确");
			}else if(e instanceof AuthorizationException) {
				r.setMessage("没有此操作权限");
			}else {
				r.setMessage("系统维护中");
			}
			e.printStackTrace();
			return r;
		}
}
