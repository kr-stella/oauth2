package jj.stella.filter.oauth;

import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import jakarta.servlet.http.HttpServletRequest;
import jj.stella.filter.auth.AuthDetails;

public class OAuth2DetailsSource implements AuthenticationDetailsSource<HttpServletRequest, WebAuthenticationDetails> {
	
	private String ORIGIN_IP_API;
	public OAuth2DetailsSource(String ORIGIN_IP_API) {
		this.ORIGIN_IP_API = ORIGIN_IP_API;
	}
	
	@Override
	public WebAuthenticationDetails buildDetails(HttpServletRequest request) {
		
		AuthDetails details = new AuthDetails(request, ORIGIN_IP_API);
		request.getSession().setAttribute("St2lla-Authenticaion-Details", details);
		
		return details;
		
	}
	
}