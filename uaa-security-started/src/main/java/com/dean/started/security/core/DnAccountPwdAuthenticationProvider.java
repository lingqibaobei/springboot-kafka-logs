//package com.dean.started.security.core;
//
//import com.rangers.manage.common.utils.RSAUtils;
//import com.rangers.manage.customer.context.EncodeHolder;
//import com.rangers.manage.customer.handler.CustomerServiceAuthHandler;
//import org.springframework.beans.factory.InitializingBean;
//import org.springframework.security.authentication.AuthenticationProvider;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.util.Assert;
//import org.springframework.util.StringUtils;
///**
// *
// * @author Dean
// * @date 2021-04-06
// */
//public class DnAccountPwdAuthenticationProvider implements AuthenticationProvider, InitializingBean {
//
//	private final S userServices;
//
//
//
//	@Override
//	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
//		String account = (String) authentication.getPrincipal();
//		String rawPassword = (String) authentication.getCredentials();
//		if (!StringUtils.hasText(account)) {
//			throw new BadCredentialsException("账户不能为空");
//		}
//		if (!StringUtils.hasText(rawPassword)) {
//			throw new BadCredentialsException("密码不能为空");
//		}
//		UserDetails userDetails = userServices.loadUserByUsername(account);
//		if (userDetails == null)
//			throw new BadCredentialsException("账户不存在");
//		String password = userDetails.getPassword();
//		if (!StringUtils.hasText(password)) {
//			throw new BadCredentialsException("账户未启用账户密码模式");
//		}
//		// rsa 解密
//		try {
//			byte[] decrypt = RSAUtils.decrypt(RSAUtils.privateKey, RSAUtils.base64Decode(rawPassword));
//			rawPassword = new String(decrypt, RSAUtils.UTF8);
//		} catch (Exception e) {
//			throw new BadCredentialsException("解密秘钥失败");
//		}
//		if (!EncodeHolder.get().matches(rawPassword, password)) {
//			throw new BadCredentialsException("账户密码不匹配");
//		}
//		return createSuccessAuthentication(userDetails, authentication);
//	}
//
//
//	private Authentication createSuccessAuthentication(UserDetails userDetails, Authentication authentication) {
//		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetails.getUsername(),
//				userDetails.getPassword(), userDetails.getAuthorities());
//		token.setDetails(userDetails);
//		return token;
//	}
//
//	@Override
//	public boolean supports(Class<?> authentication) {
//		return AccountPwdRequestToken.class.isAssignableFrom(authentication);
//	}
//
//	@Override
//	public void afterPropertiesSet() throws Exception {
//		Assert.notNull(userServices, "UserServices 不能为空");
//	}
//
//}
