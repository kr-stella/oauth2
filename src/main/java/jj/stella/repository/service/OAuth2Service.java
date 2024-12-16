package jj.stella.repository.service;

import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class OAuth2Service extends DefaultOAuth2UserService {
	
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User user = super.loadUser(userRequest);
		System.out.println("loadUser");
		// 추가 로직: 사용자 DB 확인 및 등록
		return processOAuth2User(userRequest, user);
	}
	
	private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User user) {
		
		// 사용자 정보를 기반으로 데이터베이스에 사용자를 저장하거나 업데이트
		// 여기에 사용자 검증 또는 생성 로직 추가
		Map<String, Object> attributes = user.getAttributes();
		System.out.println("OAuth2User Attributes:");
		for(Map.Entry<String, Object> entry : attributes.entrySet()) {
			System.out.println(entry.getKey() + ": " + entry.getValue());
		}
		
		return user;
	}
	
}