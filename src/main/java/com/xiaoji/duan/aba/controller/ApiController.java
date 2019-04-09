package com.xiaoji.duan.aba.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.util.StringUtils;

import com.xiaoji.duan.aba.entity.ABA_OAuthCache;
import com.xiaoji.duan.aba.entity.ABA_OnlineUser;
import com.xiaoji.duan.aba.entity.ABA_RegistedUser;
import com.xiaoji.duan.aba.service.ABAService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/user")
public class ApiController {

//	private static final String OPEN_WEIXIN_APP_ID = "wx079e1b50f0e7d2ea";
//	private static final String OPEN_WEIXIN_SECRET = "c547ee92fce2f67991f50c3f0192f35b";
	
	private static List<String> ACCESS_HEADERS = new ArrayList<String>();
	
	private static Map<String, Object> OAUTH_MAPPING = new HashMap<String, Object>();
	
	static {
		ApiController.ACCESS_HEADERS.add("ai");
		ApiController.ACCESS_HEADERS.add("pi");
		ApiController.ACCESS_HEADERS.add("pv");
		ApiController.ACCESS_HEADERS.add("dt");
		ApiController.ACCESS_HEADERS.add("di");
		ApiController.ACCESS_HEADERS.add("x-real-ip");
		ApiController.ACCESS_HEADERS.add("Content-Type");
		
		Map<String, String> weixin = new HashMap<String, String>();
		weixin.put("OPEN_APP_ID", "wx079e1b50f0e7d2ea");
		weixin.put("OPEN_SECRET", "c547ee92fce2f67991f50c3f0192f35b");
		weixin.put("LOGIN_URL", "https://open.weixin.qq.com/connect/qrconnect");
		weixin.put("ACCESSTOKEN", "https://api.weixin.qq.com/sns/oauth2/access_token");
		weixin.put("REFRESHTOKEN", "https://api.weixin.qq.com/sns/oauth2/refresh_token");
		weixin.put("USERINFO", "https://api.weixin.qq.com/sns/userinfo");
		ApiController.OAUTH_MAPPING.put("WEIXIN", weixin);

		Map<String, String> duanxin = new HashMap<String, String>();
		duanxin.put("OPEN_APP_ID", "wx079e1b50f0e7d2ea");
		duanxin.put("OPEN_SECRET", "c547ee92fce2f67991f50c3f0192f35b");
		duanxin.put("LOGIN_URL", "https://www.guobaa.com/tpx/qrconnect");
		duanxin.put("ACCESSTOKEN", "http://sa-tpx:8080/tpx/log/access_token");
		duanxin.put("REFRESHTOKEN", "http://sa-tpx:8080/tpx/log/refresh_token");
		duanxin.put("USERINFO", "http://sa-tpx:8080/tpx/log/userinfo");
		ApiController.OAUTH_MAPPING.put("DUANXIN", duanxin);

		Map<String, String> origin = new HashMap<String, String>();
		origin.put("OPEN_APP_ID", "d3d3Lmd1b2JhYS5jb20");
		origin.put("OPEN_SECRET", "c2VjcmV0QHd3dy5ndW9iYWEuY29t");
		origin.put("LOGIN_URL", "https://www.guobaa.com/auo/login");
		origin.put("ACCESSTOKEN", "http://sa-auo:8080/auo/api/access_token");
		origin.put("REFRESHTOKEN", "http://sa-auo:8080/auo/api/refresh_token");
		origin.put("USERINFO", "http://sa-auo:8080/auo/api/userinfo");
		ApiController.OAUTH_MAPPING.put("ORIGIN", origin);

		Map<String, String> mwxing = new HashMap<String, String>();
		mwxing.put("OPEN_APP_ID", "d3d3Lmd1b2JhYS5jb20");
		mwxing.put("OPEN_SECRET", "c2VjcmV0QHd3dy5ndW9iYWEuY29t");
		mwxing.put("LOGIN_URL", "https://www.guobaa.com/aup/login");
		mwxing.put("ACCESSTOKEN", "http://sa-aup:8080/aup/api/access_token");
		mwxing.put("REFRESHTOKEN", "http://sa-aup:8080/aup/api/refresh_token");
		mwxing.put("USERINFO", "http://sa-aup:8080/aup/api/userinfo");
		ApiController.OAUTH_MAPPING.put("MWXING", mwxing);
	}
	
	@Autowired
	private ABAService abaService;
	
	@RequestMapping("login")
	public Map<String, Object> login(@RequestBody(required=false) Map<String, Object> params) {
		Map<String, Object> rslt = new HashMap<String, Object>();
		
		rslt.put("code", "");
		rslt.put("message", "");
		
		Map<String, Object> data = new HashMap<String, Object>();
		String requestUri = ((ArrayList<String>) params.get("redirect_url")).get(0);
		String verifyType = ((ArrayList<String>) params.get("verifyType")).get(0);
		if (verifyType == null || "".equals(verifyType))
			verifyType = "WEIXIN";
		System.out.println(requestUri + " verify with " + verifyType);

		String state = "";
		
		if (params != null) {
			try {
				requestUri = URLEncoder.encode(requestUri.replace("localhost:8221", "www.guobaa.com"), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			requestUri = (String) params.get("request_uri");
		}

		try {
			state = URLEncoder.encode(this.abaService.initOAuth(verifyType), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		StringBuffer url = new StringBuffer((String) ((Map) OAUTH_MAPPING.get(verifyType)).get("LOGIN_URL"));
		url.append("?appid=");
		url.append((String) ((Map) OAUTH_MAPPING.get(verifyType)).get("OPEN_APP_ID"));
		url.append("&redirect_uri=");
		url.append(requestUri);
		url.append("&response_type=code");
		url.append("&scope=snsapi_login");
		url.append("&state=");
		url.append(state);
		url.append("#wechat_redirect");

		data.put("login_url", url);

		rslt.put("data", data);
		
		return rslt;
	}
	
	@RequestMapping("{verifytype}/access")
	public Map<String, Object> access(HttpServletRequest req,
			@PathVariable("verifytype") String verifyType,
			@RequestParam(required=false) String appid,
			@RequestParam(required=false) String secret,
			@RequestParam(required=false) String code,
			@RequestParam(required=false, name="grant_type") String grantType) {
		Map<String, Object> rslt = new HashMap<String, Object>();
		System.out.println(verifyType + " access_token");
		rslt.put("code", "");
		rslt.put("message", "");
		
		Object data = new HashMap<String, Object>();
		
		if (!StringUtils.isEmpty(appid) &&
				!StringUtils.isEmpty(secret) && 
				!StringUtils.isEmpty(code) &&
				!StringUtils.isEmpty(grantType)) {
			System.out.println("appid " + appid);
			System.out.println("secret " + secret);
			System.out.println("code " + code);
			System.out.println("grantType " + grantType);

			// 获取ACCESS_TOKEN
			StringBuffer url = new StringBuffer((String) ((Map) OAUTH_MAPPING.get(verifyType.toUpperCase())).get("ACCESSTOKEN"));
			url.append("?appid=");
			url.append(appid);
			url.append("&secret=");
			url.append(secret);
			url.append("&code=");
			url.append(code);
			url.append("&grant_type=authorization_code");
			
			Map<String, String> headers = new HashMap<String, String>();
			Enumeration<String> headernames = req.getHeaderNames();
			
			while (headernames.hasMoreElements()) {
				String name = headernames.nextElement();
				if (ACCESS_HEADERS.contains(name)) {
					headers.put(name, req.getHeader(name));
					System.out.println("*header " + name + " = " + req.getHeader(name));
				} else {
					System.out.println("header " + name + " = " + req.getHeader(name));
				}
			}
			
			Map<String, Object> accessTokenRet = abaService.https(headers, url.toString());

			if (accessTokenRet != null && accessTokenRet.containsKey("access_token")) {
				accessTokenRet.put("code", code);
				System.out.println(accessTokenRet);
				((Map) data).putAll(accessTokenRet);

				// 获取ACCESS_TOKEN成功
				String accessToken = (String) accessTokenRet.get("access_token");
				String openId = (String) accessTokenRet.get("openid");

				String state = this.abaService.initOAuth(verifyType.toUpperCase());
				this.abaService.callbackOAuth(code, state);
				
				abaService.saveOnlineUser(accessTokenRet);
				
				String scope = (String) accessTokenRet.get("scope");
				if (scope.contains("snsapi_login") || scope.contains("snsapi_userinfo")) {
					// 授权可以获取用户信息
					StringBuffer userinfo = new StringBuffer((String) ((Map) OAUTH_MAPPING.get(verifyType.toUpperCase())).get("USERINFO"));
					userinfo.append("?access_token=");
					userinfo.append(accessToken);
					userinfo.append("&openid=");
					userinfo.append(openId);
					
					Map<String, Object> userInfoRet = abaService.https(headers, userinfo.toString());
					
					abaService.saveRegistedUser(userInfoRet);
					
					//((Map) data).putAll(userInfoRet);
				}
			} else {
				// 获取ACCESS_TOKEN失败
				rslt.put("code", "ABA_000002");
				rslt.put("message", "获取ACCESS_TOKEN失败!");
			}
		} else {
			rslt.put("code", "ABA_000005");
			rslt.put("message", "请求参数为空!");
		}
		
		rslt.put("data", data);
		
		if ("".equals(rslt.get("code")))
			return (Map) data;
		else
			return rslt;
		
	}
	
	@RequestMapping("{state}/login")
	public Map<String, Object> stateLogin(@PathVariable("state") String state, @RequestBody(required=false) Map<String, Object> params) {
		Map<String, Object> rslt = new HashMap<String, Object>();
		
		rslt.put("code", "");
		rslt.put("message", "");
		
		Object data = new HashMap<String, Object>();
		
		if (params == null) {
			rslt.put("code", "ABA_000005");
			rslt.put("message", "请求参数为空!");
		} else {
			if (params.containsKey("code")) {
				// 登录授权成功
				String code = ((ArrayList<String>) params.get("code")).get(0);
				
				boolean needToken = this.abaService.callbackOAuth(code, state);
				String verifyType = this.abaService.getVerifyType(state);
				
				System.out.println(code + " - " + state + " verifyType " + verifyType);
				
				if (needToken) {
					// 获取ACCESS_TOKEN
					StringBuffer url = new StringBuffer((String) ((Map) OAUTH_MAPPING.get(verifyType)).get("ACCESSTOKEN"));
					url.append("?appid=");
					url.append((String) ((Map) OAUTH_MAPPING.get(verifyType)).get("OPEN_APP_ID"));
					url.append("&secret=");
					url.append((String) ((Map) OAUTH_MAPPING.get(verifyType)).get("OPEN_SECRET"));
					url.append("&code=");
					url.append(code);
					url.append("&grant_type=authorization_code");
					
					Map<String, Object> accessTokenRet = abaService.https(url.toString());
					accessTokenRet.put("code", code);
					System.out.println(accessTokenRet);
					((Map) data).putAll(accessTokenRet);
		
					if (accessTokenRet.containsKey("access_token")) {
						// 获取ACCESS_TOKEN成功
						String accessToken = (String) accessTokenRet.get("access_token");
						String openId = (String) accessTokenRet.get("openid");
						
						abaService.saveOnlineUser(accessTokenRet);
						
						String scope = (String) accessTokenRet.get("scope");
						if (scope.contains("snsapi_login") || scope.contains("snsapi_userinfo")) {
							// 授权可以获取用户信息
							StringBuffer userinfo = new StringBuffer((String) ((Map) OAUTH_MAPPING.get(verifyType)).get("USERINFO"));
							userinfo.append("?access_token=");
							userinfo.append(accessToken);
							userinfo.append("&openid=");
							userinfo.append(openId);
							
							Map<String, Object> userInfoRet = abaService.https(userinfo.toString());
							
							abaService.saveRegistedUser(userInfoRet);
							
							((Map) data).putAll(userInfoRet);
						}
					} else {
						// 获取ACCESS_TOKEN失败
						rslt.put("code", "ABA_000002");
						rslt.put("message", "获取ACCESS_TOKEN失败!");
					}
				} else {
					ABA_OnlineUser ou = this.abaService.getOnlineUser(code);
					if (ou != null)
						data = this.abaService.getRegistedUserByOpenId(ou.getOpenId());
					else
						System.out.println(verifyType + " verified with code " + code + " has not exist online user.");
				}
			} else {
				// 登录授权失败
				rslt.put("code", "ABA_000001");
				rslt.put("message", "登录授权失败!");
			}
		}
		
		rslt.put("data", data);
		
		return rslt;
	}

	@RequestMapping("islogin")
	public Map<String, Object> isLogin(@RequestBody(required=false) Map<String, Object> params) {
		Map<String, Object> rslt = new HashMap<String, Object>();
		
		rslt.put("code", "");
		rslt.put("message", "");
		
		Map<String, Object> data = new HashMap<String, Object>();
		
		if (params == null) {
			rslt.put("code", "ABA_000005");
			rslt.put("message", "请求参数为空!");
		} else {
			if (params.containsKey("access_token")) {
				// 登录状态判断,刷新ACCESS_TOKEN
				String accessToken = (String) ((ArrayList) params.get("access_token")).get(0);
				
				// 刷新ACCESS_TOKEN
				ABA_OnlineUser ou = this.abaService.getOnlineUserByAccessToken(accessToken);
				if (ou == null) {
					// 不存在已登录客户，或者登录已失效
					rslt.put("code", "ABA_000004");
					rslt.put("message", "不存在已登录客户，或者登录已失效!");
				} else {
					System.out.println("accessToken " + accessToken);
					System.out.println("User " + ou.getOpenId() + " with code " + ou.getCode() + " will expired in " + ou.getExpiresIn());
					
					ABA_OAuthCache oac = this.abaService.getOAuthCache(ou.getCode());
					String verifyType = this.abaService.getVerifyType(oac.getState());
					if (verifyType == null || "".equals(verifyType)) {
						verifyType = "WEIXIN";
					}
					
					StringBuffer url = new StringBuffer((String) ((Map) OAUTH_MAPPING.get(verifyType)).get("REFRESHTOKEN"));
					url.append("?appid=");
					url.append((String) ((Map) OAUTH_MAPPING.get(verifyType)).get("OPEN_APP_ID"));
					url.append("&grant_type=refresh_token");
					url.append("&refresh_token=");
					url.append(ou.getRefreshToken());
	
					Map<String, Object> refreshTokenRet = this.abaService.https(url.toString());
	
					refreshTokenRet.put("unionid", ou.getUnionId());
					
					data.putAll(refreshTokenRet);
					
					if (refreshTokenRet.containsKey("access_token")) {
						// 获取ACCESS_TOKEN成功
						this.abaService.saveOnlineUser(refreshTokenRet);
						ABA_RegistedUser ru = this.abaService.getRegistedUserByOpenId((String) refreshTokenRet.get("openid"));

						Map<String, Object> ruMap = this.abaService.entityToMap(ru, ABA_RegistedUser.class, null);
						data.putAll(ruMap);
					} else {
						// 获取ACCESS_TOKEN失败
						rslt.put("code", "ABA_000002");
						rslt.put("message", "获取ACCESS_TOKEN失败!");
					}
				}
			} else {
				// 登录状态请求失败
				rslt.put("code", "ABA_000003");
				rslt.put("message", "登录状态请求失败!");
			}
		}
		
		rslt.put("data", data);

		return rslt;
	}
	
	@RequestMapping("{openid}/info")
	public Map<String, Object> info(@PathVariable String openid) {
		Map<String, Object> rslt = new HashMap<String, Object>();
		
		rslt.put("code", "");
		rslt.put("message", "");
		
		Map<String, Object> data = new HashMap<String, Object>();
		
		ABA_RegistedUser ru = this.abaService.getRegistedUserByOpenId(openid);
		
		if (ru == null) {
			rslt.put("code", "ABA_000006");
			rslt.put("message", "用户(" + openid + ")不存在！");
		} else {
			data.put("unionid", ru.getUnionId());
			data.put("openid", ru.getOpenId());
			data.put("name", ru.getNickName());
			data.put("avatar", ru.getAvatar());
			data.put("sex", ru.getSex());
		}
		
		rslt.put("data", data);
		
		return rslt;
	}
}
