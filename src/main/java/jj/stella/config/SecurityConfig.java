package jj.stella.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsUtils;

import jj.stella.repository.service.OAuth2Service;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	private static final String[] WHITE_LIST = {
		"/resources/**", "/favicon.ico", "/", "/login", "/logout", "/oauth2/**"
	};
	
	@Bean
	protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
			.authorizeHttpRequests(auth ->
				auth
					.requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
					.requestMatchers(getRequestMatchers(WHITE_LIST)).permitAll()
					.anyRequest().authenticated()
			).formLogin(form ->
				form
					.loginPage("/login")
			).logout(logout ->
				logout
					.logoutUrl("/logout")
					.logoutSuccessUrl("/login")
					.deleteCookies("JSESSIONID")
					.invalidateHttpSession(true)
			)
			.oauth2Login(oauth2 ->
				oauth2
					.defaultSuccessUrl("/home", true)
					.failureUrl("/login?error=true")
					.redirectionEndpoint(redirect ->
						redirect
							.baseUri("/oauth2/callback/*")
					)
					.userInfoEndpoint(endpoint ->
						endpoint
							.userService(new OAuth2Service())
					)
			).build();
	};
	
	/** 비밀번호 암호화 ( 단방향 복호화 불가능 ) */
	@Bean
	public PasswordEncoder encoder() {
		
		PasswordEncoder encoder = new BCryptPasswordEncoder();
		return encoder;
		
	};
	
	/** session control */
	@Bean
	public SessionRegistry sessionRegistry() {
		return new SessionRegistryImpl();
	};

	private RequestMatcher[] getRequestMatchers(String... str) {
		return Arrays.stream(str)
			.map(AntPathRequestMatcher::new)
			.toArray(RequestMatcher[]::new);
	};
	
}