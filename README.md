
shiro 知识整理：

# 认证流程：

## 获取主体对象
	Subject currentUser = SecurityUtils.getSubject();

## 构建 UsernamePasswordToken 对象
	UsernamePasswordToken token = new UsernamePasswordToken("username", "password");
	可自定义实现，自定义实现时，需要将token实体传入realm中
	
## 调用login方法，开始认证
	currentUser.login(token);
	
	该方法会抛出异常：
		UnknownAccountException	表示 username 不存在
		IncorrectCredentialsException	表示 password 有误
		LockedAccountException	表示 username 被锁定
	
	重点是 DelegatingSubject 类，该类默认实现了 Subject 接口，作为 shiro 默认的认证类，自定义时，继承该类，重写认证方法。
