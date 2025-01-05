package jj.stella.entity.vo;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import lombok.Getter;
import lombok.Setter;

//@Getter
//@Setter
public class OAuth2UserVo implements OAuth2User {
	
	private Map<String, Object> attributes;
	private Collection<? extends GrantedAuthority> authorities;
	private String nameAttributeKey;
	
	public OAuth2UserVo(Map<String, Object> attributes, Collection<? extends GrantedAuthority> authorities, String nameAttributeKey) {
		
		System.out.println(attributes);
		System.out.println(authorities);
		System.out.println(nameAttributeKey);
		
		
		this.attributes = attributes;
		this.authorities = authorities;
		this.nameAttributeKey = nameAttributeKey;
	}
	
	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getName() {
//		return attributes.getOrDefault(nameAttributeKey, "").toString();
		return nameAttributeKey;
	}

}