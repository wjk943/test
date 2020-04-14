package com.cy.pj.sys.service.impl;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import com.cy.pj.common.annotation.RequiredLog;
import com.cy.pj.common.config.PaginationProperties;
import com.cy.pj.common.util.Assert;
import com.cy.pj.common.vo.PageObject;
import com.cy.pj.sys.dao.SysUserDao;
import com.cy.pj.sys.dao.SysUserRoleDao;
import com.cy.pj.sys.entity.SysUser;
import com.cy.pj.sys.service.SysUserService;
import com.cy.pj.sys.vo.SysUserDeptVo;

@Service
public class SysUserServiceImpl implements SysUserService {
	@Autowired
	private SysUserDao sysUserDao;
	
	@Autowired
	private SysUserRoleDao sysUserRoleDao;
	@Autowired
	private PaginationProperties paginationProperties;
	
	@Override
	public Map<String, Object> findObjectById(Integer userId) {
		//1.参数校验
		Assert.isArgumentValid(userId==null||userId<1,"id值不正确" );
		//2.查询用户以及用户对应的部门信息
		SysUserDeptVo user=sysUserDao.findObjectById(userId);
		Assert.isNull(user, "记录可能已经不存在");
		//3.查询用户对应的角色id
		List<Integer> roleIds=sysUserRoleDao.findRoleIdsByUserId(userId);
		//4.对查询结果进行封装
		Map<String,Object> map=new HashMap<>();
		map.put("user", user);
		map.put("roleIds", roleIds);
		return map;
	}

	@Override
	public int saveObject(SysUser entity, Integer[] roleIds) {
		//1.参数校验
		Assert.isNull(entity, "保存对象不能为空");
		Assert.isEmpty(entity.getUsername(), "用户名不能为空");
		Assert.isEmpty(entity.getPassword(), "密码不能为空");
		//..
		Assert.isArgumentValid(roleIds==null||roleIds.length==0, "必须为用户分配权限");
		//2.对密码进行加密
		String password=entity.getPassword();
		String salt=UUID.randomUUID().toString();
		//String hashPassword=DigestUtils.md5DigestAsHex((password+salt).getBytes());//spring
		SimpleHash sh=new SimpleHash(
				"MD5",//algorithmName 加密算法
				password,//source 需要加密的密码
				salt, //salt 加密盐
				1);//hashIterations 加密次数
		entity.setPassword(sh.toHex());//将密码加密结果转换为16进制并存储到entity对象
		entity.setSalt(salt);
		//3.保存用户自身信息
		int rows=sysUserDao.insertObject(entity);
		//4.保存用户与角色的关系数据
		sysUserRoleDao.insertObjects(entity.getId(), roleIds);
		//5.返回业务结果
		return rows;
	}
	
	@Override
	public int updateObject(SysUser entity, Integer[] roleIds) {
		//1.参数校验
		Assert.isNull(entity, "保存对象不能为空");
		Assert.isEmpty(entity.getUsername(), "用户名不能为空");
		//..
		Assert.isArgumentValid(roleIds==null||roleIds.length==0, "必须为用户分配权限");
		//3.更新用户自身信息
		int rows=sysUserDao.updateObject(entity);
		//4.更新用户与角色的关系数据
		sysUserRoleDao.deleteObjectsByUserId(entity.getId());
		sysUserRoleDao.insertObjects(entity.getId(), roleIds);
		//5.返回业务结果
		return rows;
	}
	
	
	@RequiresPermissions("sys:user:update")
	@Override
	public int validById(Integer id, Integer valid) {
		//1.参数校验
		Assert.isArgumentValid(id==null||id<1, "id值不正确");
		Assert.isArgumentValid(valid!=1&&valid!=0,"状态值不正确");
		//2.执行更新并校验
		int rows=sysUserDao.validById(id, valid, "admin");//admin后续是登录用户
		Assert.isServiceValid(rows==0, "记录可能已经不存在");
		//3.返回结果
		return rows;
	}
	
	@Transactional(readOnly = true)
	@RequiredLog("分页")
	@Override
	public PageObject<SysUserDeptVo> findPageObjects(String username, Integer pageCurrent) {
		//1.参数校验
		Assert.isArgumentValid(pageCurrent==null||pageCurrent<1, "当前页码值不正确");
		//2.查询总记录数并校验
		int rowCount=sysUserDao.getRowCount(username);
		Assert.isServiceValid(rowCount==0, "没有对应的记录");
		//3.查询当前页用户记录信息
		Integer pageSize=paginationProperties.getPageSize();
		Integer startIndex=paginationProperties.getStartIndex(pageCurrent);
		List<SysUserDeptVo> records=
		sysUserDao.findPageObjects(username, startIndex, pageSize);
		//4.封装结果并返回
		return new PageObject<>(rowCount, records, pageSize, pageCurrent);
	}

}








