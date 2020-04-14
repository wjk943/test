package com.cy.pj.sys.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cy.pj.common.util.Assert;
import com.cy.pj.common.vo.Node;
import com.cy.pj.sys.dao.SysMenuDao;
import com.cy.pj.sys.dao.SysRoleMenuDao;
import com.cy.pj.sys.entity.SysMenu;
import com.cy.pj.sys.service.SysMenuService;

@Service
public class SysMenuServiceImpl implements SysMenuService {

	@Autowired
	private SysMenuDao sysMenuDao;
	@Autowired
	private SysRoleMenuDao sysRoleMenuDao;
	
	@Override
	public int updateObject(SysMenu entity) {
		//1.参数校验
		Assert.isArgumentValid(entity==null, "保存对象不能为空");
		Assert.isEmpty(entity.getName(), "菜单名不能为空");
		//....
		//2.保存菜单信息
		int rows=sysMenuDao.updateObject(entity);//有可能网络中断,数据库连接没有了.
		//3.返回结果
		return rows;
	}
	@Override
	public int saveObject(SysMenu entity) {
		//1.参数校验
		Assert.isArgumentValid(entity==null, "保存对象不能为空");
		Assert.isEmpty(entity.getName(), "菜单名不能为空");
		//....
		//2.保存菜单信息
		int rows=sysMenuDao.insertObject(entity);//有可能网络中断,数据库连接没有了.
		//3.返回结果
		return rows;
	}
	
	@Override
	public List<Node> findZtreeMenuNodes() {
		return sysMenuDao.findZtreeMenuNodes();
	}
	@Override
	public int deleteObject(Integer id) {
		//1.参数校验
		Assert.isArgumentValid(id==null||id<1, "id值无效");
		//2.统计当前菜单对应的子菜单个数并校验
		int childCount=sysMenuDao.getChildCount(id);
		Assert.isServiceValid(childCount>0, "请先删除子元素");
		//3.删除菜单对应的关系数据
		sysRoleMenuDao.deleteObjectsByMenuId(id);
		//4.删除菜单自身信息并校验
		int rows=sysMenuDao.deleteObject(id);
		Assert.isServiceValid(rows==0, "记录可能已经不存在");
		return rows;
	}
	
	//后续会追加扩展业务
	@Override
	public List<SysMenu> findObjects() {
		return sysMenuDao.findObjects();
	}

}








