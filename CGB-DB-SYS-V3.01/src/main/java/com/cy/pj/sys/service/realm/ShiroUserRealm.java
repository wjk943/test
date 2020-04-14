package com.cy.pj.sys.service.realm;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.cy.pj.sys.dao.SysMenuDao;
import com.cy.pj.sys.dao.SysRoleMenuDao;
import com.cy.pj.sys.dao.SysUserDao;
import com.cy.pj.sys.dao.SysUserRoleDao;
import com.cy.pj.sys.entity.SysUser;


@Service
public class ShiroUserRealm extends AuthorizingRealm{
	
	@Autowired
	private SysUserDao sysUserDao;
	
	@Autowired
	private SysUserRoleDao sysUserRoleDao;
	
	@Autowired
	private SysRoleMenuDao sysRoleMenuDao;
	
	@Autowired
	private SysMenuDao sysMenuDao;
	
	@Override
	public void setCredentialsMatcher(CredentialsMatcher credentialsMatcher) {
		HashedCredentialsMatcher cMatcher = new HashedCredentialsMatcher("MD5");
		cMatcher.setHashIterations(1);
		super.setCredentialsMatcher(cMatcher);
	}
	
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		UsernamePasswordToken uToken = (UsernamePasswordToken)token;
		String username = uToken.getUsername();
		SysUser name = sysUserDao.findUserByUserName(username);
		if(name == null)
			throw new UnknownAccountException();
		if(name.getValid() == 0)
			throw new LockedAccountException();
		ByteSource credentialsSalt = ByteSource.Util.bytes(name.getSalt());
		SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(name, 
																	name.getPassword(), 
																	credentialsSalt,
																	getName());
		return info;
	}
	
	
	

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		//1.获取登录用户信息，例如用户id
				SysUser user=(SysUser)principals.getPrimaryPrincipal();
				Integer userId=user.getId();
				//2.基于用户id获取用户拥有的角色(sys_user_roles)
				List<Integer> roleIds=
				sysUserRoleDao.findRoleIdsByUserId(userId);
				if(roleIds==null||roleIds.size()==0)
				throw new AuthorizationException();
				//3.基于角色id获取菜单id(sys_role_menus)
				Integer[] array={};
				List<Integer> menuIds=
				sysRoleMenuDao.findMenuIdsByRoleIds(
						roleIds.toArray(array));
			    if(menuIds==null||menuIds.size()==0)
			    throw new AuthorizationException();
				//4.基于菜单id获取权限标识(sys_menus)
			    List<String> permissions=
			    sysMenuDao.findPermissions(
			    	menuIds.toArray(array));
				//5.对权限标识信息进行封装并返回
			    Set<String> set=new HashSet<>();
			    for(String per:permissions){
			    	if(!StringUtils.isEmpty(per)){
			    		set.add(per);
			    	}
			    }
			    SimpleAuthorizationInfo info=
			    new SimpleAuthorizationInfo();
			    info.setStringPermissions(set);
				return info;//返回给授权管理器
	}


}
