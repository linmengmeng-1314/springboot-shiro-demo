
shiro 知识整理：

# 认证流程：

## 获取主体对象
	Subject currentUser = SecurityUtils.getSubject();

## 构建 UsernamePasswordToken 对象
```
UsernamePasswordToken token = new UsernamePasswordToken("username", "password");
```
&nbsp;可自定义实现，自定义实现时，需要将token实体传入realm中
	
## 调用login方法，开始认证

```
currentUser.login(token);
```
	
该方法会抛出异常：
- UnknownAccountException	表示 username 不存在
- IncorrectCredentialsException	表示 password 有误
- LockedAccountException	表示 username 被锁定
	
&ensp;重点是 DelegatingSubject 类，该类默认实现了 Subject 接口，作为 shiro 默认的认证类，自定义时，继承该类，重写认证方法。
	
&emsp;Realm 本质上是一个特定的安全 DAO：它封装与数据源连接的细节，得到Shiro 所需的相关的数据。在配置 Shiro 的时候，你必须指定至少一个Realm 来实现认证（authentication）和/或授权（authorization）。SecurityManager 可以配置多个复杂的 Realm，但是至少有一个是需要的。

&nbsp;自定义 Realm 一般继承 AuthorizingRealm 就可以了，其继承了AuthenticatingRealm（即身份验证），而且也间接继承了CachingRealm（带有缓存实现），最顶端的父类还是Realm。  需传入SecurityManager中才会生效。

&nbsp;如果定义了多个 realm 则 shiro 会根据 securityManager 中设置的顺序 来以此进行 realm 的验证。  每个realm验证成功后返回一个 SimpleAuthenticationInfo(username, password, getName()) 对象，getName方法可在realm中自定义实现，如返回realm的名称，验证失败则抛出对应的异常。
这时可以使用subject.getPrincipals(); 得到一个身份集合，其包含了Realm验证成功的身份信息。可以对返回的身份集合进行判断，全部满足，部分满足，至少一个满足等(源码：org.apache.shiro.authc.pam.ModularRealmAuthenticator.doMultiRealmAuthentication(Collection<Realm>, AuthenticationToken))。principalCollection.asList().size()返回身份集合的大小。

当需要自定义实现多个realm场景的机制时，可以自定义 AuthenticationStrategy 实现。其api包含：//在所有Realm验证之前调用，在每个Realm之前调用，在每个Realm之后调用 ，在所有Realm之后调用 

可以看到源码doMultiRealmAuthentication方法中AuthenticationInfo 只有一个实例。因为每个AuthenticationStrategy实例都是无状态的，所以每次都通过接口将相应的认证信息传入下一次流程；通过如上接口可以进行如合并/返回第一个验证成功的认证信息。

自定义 AuthenticationStrategy 实现时一般继承org.apache.shiro.authc.pam.AbstractAuthenticationStrategy 即可，shiro提供了三种实现：AllSuccessfulStrategy，AtLeastOneSuccessfulStrategy，FirstSuccessfulStrategy

shiro的ini.ini 文件为shiro 的一种配置方式

如果实现自定义的密码加密方式，可以在 realm 中告诉默认的iniRealm

在 ini文件中使用：
```
[main]
sha256Matcher = org.apache.shiro.authc.credential.Sha256CredentialsMatcher

iniRealm.credentialsMatcher = $sha256Matcher

[users]
# user1 = sha256-hashed-hex-encoded password, role1, role2, ...
user1 = 2bb80d537b1da3e38bd30361aa855686bde0eacd7162fef6a25fe97bf527a25b, role1, role2, ...
```
使用javaBean时，类似实例化自定义的realm后，使用set属性，给realm的credentialsMatcher 赋值
	
# 登录验证过程

## 第一步：收集用户身份和证明
```
//最常用的情况是 username/password 对:
UsernamePasswordToken token = new UsernamePasswordToken(username, password);

//”Remember Me” 功能是内建的
token.setRememberMe(true);
```
在实例化 UsernamePasswordToken  后，建议使用 setRememberMe(true); 这确保 Shiro 在用户今后返回系统时能记住他们的身份

## 第二步：提交身份和证明
```
Subject currentUser = SecurityUtils.getSubject();

currentUser.login(token);
```	 
	 
	
## 第三步：处理成功或失败

当login函数没有返回信息时表明验证通过了。程序可以继续运行，此时执行 SecurityUtils.getSubject() 将返回验证后的 Subject 实例，subject.isAuthenticated()) 将返回true。

可以将 login 放入到 try/catch 块中并捕获所有你想捕获的异常并对它们做出处理。例如：
```
	try {
		currentUser.login(token);
	} catch ( UnknownAccountException uae ) { ...
	} catch ( IncorrectCredentialsException ice ) { ...
	} catch ( LockedAccountException lae ) { ...
	} catch ( ExcessiveAttemptsException eae ) { ...
	} ... 捕获你自己的异常 ...
	} catch ( AuthenticationException ae ) {
		//未预计的错误?
	}

	//没问题，继续
```
  但是如果 login 失败了呢？例如，用户提供了一个错误的密码或者因访问系统次数过多而被锁定将会怎样呢？
	
- UnknownAccountException	表示 username 不存在
- IncorrectCredentialsException	表示 password 有误
- LockedAccountException	表示 username 被锁定
- DisabledAccountException（禁用的帐号）
- ExcessiveAttemptsException（登录失败次数过多）
- ExpiredCredentialsException（过期的凭证）
  对于页面的错误消息展示，最好使用如“用户名/密码错误”而不是“用户名错误”/“密码错误”，防止一些恶意用户非法扫描帐号库；
	
可以将 login 放入到 try/catch 块中并捕获所有你想捕获的异常并对它们做出处理。或者使用全局异常捕获该异常

Remembered vs. Authenticated 的区别

Subject（remembered Subject）和一个正常通过认证的 Subject（authenticated Subject）在 Shiro 是完全不同的。

	记住的（Remembered）：一个被记住的 Subject 不不会是匿名的，拥有一个已知的身份（也就是说subject.getPrincipals())返回非空）。它的身份被先前的认证过程所记住，并存于先前session中，一个被认为记住的对象在执行subject.isRemembered())返回true。
	已验证（Authenticated）：一个被验证的 Subject 是成功验证后（如登录成功）并存于当前 session 中，一个被认为验证过的对象调用subject.isAuthenticated()) 将返回true。

已记住（Remembered）和已验证（Authenticated）是互斥的--一个标识值为真另一个就为假，反过来也一样。

总结下来就是 Remembered 并不能确定“你就是你”，而 Authenticated 可以确定“你就是你”，当涉及到要求比较严谨的步骤时，建议使用 subject.isAuthenticated() 判断是否为 true ，否则提示重新认证。
	
# 退出登录

与验证相对的是释放所有已知的身份信息，当 Subject 与程序不再交互了，你可以调用 subject.logout() 丢掉所有身份信息。

currentUser.logout(); //清除验证信息，使 session 失效
当你调用 logout，任何现存的 session 将变为不可用并且所有的身份信息将消失（如：在 web 程序中，RememberMe 的 Cookie 信息同样被删除）。
	

#权限验证注解
	Shiro提供了一个 Java 5 的注解集，当subject没有该权限时，会抛出 AuthorizationException
		RequiresAuthentication 注解，要求 Subject 在当前的 session中已经被验证。
		
		@RequiresAuthentication
		public void updateAccount(Account userAccount) {
			//这个方法只会被调用在
			//Subject 保证被认证的情况下
			...
		}
		这基本上与下面的基于对象的逻辑效果相同：
			public void updateAccount(Account userAccount) {
				if (!SecurityUtils.getSubject().isAuthenticated()) {
					throw new AuthorizationException(...);
				}

				//这里 Subject 保证被认证的情况下
				...
			}
		
		RequiresGuest 注解，表示要求当前Subject是一个“guest(访客)”，也就是，在访问或调用被注解的类/实例/方法时，他们没有被认证或者在被前一个Session 记住。

		RequiresPermissions 注解，表示要求当前Subject在执行被注解的方法时具备一个或多个对应的权限。
		
		RequiresRoles 注解，表示要求当前Subject在执行被注解的方法时具备所有的角色，否则将抛出 AuthorizationException 异常。
		
		RequiresUser 注解，表示要求在访问或调用被注解的类/实例/方法时，当前 Subject 是一个程序用户，“程序用户”是一个已知身份的 Subject，或者在当前 Session 中被验证过或者在以前的 Session 中被记住过。

# 优化

	1. shiro支持在认证主体成功后，将用户的权限存起来，在调用相关接口时，不用每次都要查询用户的权限。直接使用 currentUser.hasRole("administrator") 
		弊端：用户认证有效期内，会一直持有该权限。撤销权限不能及时生效。
