package jj.stella.repository.service;

import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import jj.stella.entity.vo.OAuth2UserVo;

@Service
public class OAuth2Service extends DefaultOAuth2UserService {
	
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		
		OAuth2User user = super.loadUser(userRequest);
		
		// 추가 로직: 사용자 DB 확인 및 등록
		return processOAuth2User(userRequest, user);
		
	}
	
	private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User user) {
		
		String registration = userRequest.getClientRegistration().getRegistrationId();
		System.out.println("어떤 요청인지 확인 ===> " + registration);
		
		
		// 사용자 정보를 기반으로 데이터베이스에 사용자를 저장하거나 업데이트
		// 여기에 사용자 검증 또는 생성 로직 추가
		Map<String, Object> attributes = user.getAttributes();
		System.out.println("OAuth2User Attributes:");
		for(Map.Entry<String, Object> entry : attributes.entrySet()) {
			System.out.println(entry.getKey() + ": " + entry.getValue());
		}
		
		String userName = extractUserName(registration, attributes);
		System.out.println("userName =====> " + userName);
		
//		return user;
		return new OAuth2UserVo(
			attributes,
			Collections.singletonList(
					new SimpleGrantedAuthority("ROLE_USER")
			),
			userName
		);
		
	}
	
	
	private String extractUserName(String registrationId, Map<String, Object> attributes) {
		switch (registrationId.toLowerCase()) {
			case "google":
				return (String) attributes.getOrDefault("name", "Unknown");  // Google typically provides a 'name' attribute
			case "github":
				return (String) attributes.getOrDefault("name", "Unknown"); // GitHub uses 'login' as the username
//				return (String) attributes.getOrDefault("login", "Unknown"); // GitHub uses 'login' as the username
			case "naver":
				Map<String, Object> response = (Map<String, Object>) attributes.get("response");
				return (String) response.getOrDefault("name", "Unknown");   // Naver wraps user info in a 'response' object
			case "kakao":
				Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
				Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
				return (String) profile.getOrDefault("nickname", "Unknown"); // Kakao uses 'profile' inside 'kakao_account'
			default:
				throw new OAuth2AuthenticationException("Unsupported OAuth2 provider");
		}
	}
//	private OAuth2User processGoogleUser(Map<String, Object> attributes) {
//		// Google의 API로부터 받은 attribute를 사용
//		System.out.println("Logged in with Google: " + attributes.get("name"));
//		// 여기에 사용자 검증 또는 생성 로직 추가
//		return new DefaultOAuth2User(
//		Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
//		attributes, "name");
//	}
//
//	private OAuth2User processGithubUser(Map<String, Object> attributes) {
//		// GitHub의 API로부터 받은 attribute를 사용
//		System.out.println("Logged in with GitHub: " + attributes.get("name"));
//		// 여기에 사용자 검증 또는 생성 로직 추가
//		return new DefaultOAuth2User(
//		Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
//		attributes, "name");
//	}
//
//	private OAuth2User processNaverUser(Map<String, Object> attributes) {
//		// Naver는 사용자 정보가 response 객체 안에 들어있음
//		Map<String, Object> response = (Map<String, Object>) attributes.get("response");
//		System.out.println("Logged in with Naver: " + response.get("name"));
//		// 여기에 사용자 검증 또는 생성 로직 추가
//		return new DefaultOAuth2User(
//		Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
//		response, "name");
//	}
//
//	private OAuth2User processKakaoUser(Map<String, Object> attributes) {
//		// Kakao는 사용자 정보가 kakao_account 객체 안에 들어있음
//		Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
//		Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
//		System.out.println("Logged in with Kakao: " + profile.get("nickname"));
//		// 여기에 사용자 검증 또는 생성 로직 추가
//		return new DefaultOAuth2User(
//		Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
//		kakaoAccount, "nickname");
//	}
	
}