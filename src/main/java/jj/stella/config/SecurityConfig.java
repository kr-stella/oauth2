package jj.stella.config;

import java.security.Key;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.Filter;
import jj.stella.filter.Redirect;
import jj.stella.filter.auth.AuthDetailsSource;
import jj.stella.filter.auth.AuthFailure;
import jj.stella.filter.auth.AuthLogout;
import jj.stella.filter.auth.AuthProvider;
import jj.stella.filter.csrf.Csrf;
import jj.stella.filter.csrf.CsrfHandler;
import jj.stella.filter.csrf.CsrfRepository;
import jj.stella.filter.jwt.JwtIssue;
import jj.stella.filter.oauth.OAuth2DetailsSource;
import jj.stella.properties.AuthProperties;
import jj.stella.properties.ServerProperties;
import jj.stella.repository.dao.AuthDao;
import jj.stella.repository.service.OAuth2Service;
import jj.stella.util.auth.AuthUtil;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	private final AuthProperties authProperties;
	private final ServerProperties serverProperties;
	public SecurityConfig(AuthProperties authProperties, ServerProperties serverProperties) {
		this.authProperties = authProperties;
		this.serverProperties = serverProperties;
	};
	
	private static final String[] WHITE_LIST = {
		"/resources/**", "/static/**", "/favicon.ico", "/", "/login", "/logout", "/oauth2/**"
	};

	/**
	 * JwtAuthenticationFilter와 같은 Filter에 직접 추가하는 경우
	 * 생명주기 밖에서 생성되기 때문에 null로 초기화 됨.
	 */
	@Autowired
	private AuthDao authDao;
	@Autowired
	private AuthUtil authUtil;
	
	/** 사용자 인증 */
	@Autowired
	private AuthProvider authProvider;

	/**
	 * JWT 발급을 위한 Redis 설정
	 * - jti에 사용자 기기 식별번호를 넣고 TTL 설정
	 * - 계정 비활성화 할 때 redis도 사용자 기기 식별번호로 파기하면 됨.
	 * - 검증할 때 토큰과 Redis의 jti를 비교
	 */
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	/** 정적 파일 무시 */
//	@Bean
//	public WebSecurityCustomizer webSecurityCustomizer() {
//		return (web) -> web.ignoring()
//				.requestMatchers(PathRequest.toStaticResources().atCommonLocations());
//	};
	
	@Bean
	protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		
		String JWT_HEADER = authProperties.getJwt().getHeader();
		String JWT_KEY = authProperties.getJwt().getKey();
		String JWT_NAME = authProperties.getJwt().getName();
		String JWT_ISSUER = authProperties.getJwt().getIssuer();
		String JWT_AUDIENCE = authProperties.getJwt().getAudience();
		String JWT_REFRESH_ISSUER = authProperties.getJwt().getRefresh().getIssuer();
		String JWT_REFRESH_AUDIENCE = authProperties.getJwt().getRefresh().getAudience();
		String JWT_DOMAIN = authProperties.getJwt().getDomain();
		String JWT_PATH = authProperties.getJwt().getPath();
		String JWT_EXPIRED = authProperties.getJwt().getExpired();
		String JWT_ENCRYPT_SIGN = authProperties.getJwt().getEncrypt().getSign();
		String JWT_ENCRYPT_TOKEN = authProperties.getJwt().getEncrypt().getToken();
		String JWT_ENCRYPT_REFRESH_SIGN = authProperties.getJwt().getEncrypt().getRefresh().getSign();
		String JWT_ENCRYPT_REFRESH_TOKEN = authProperties.getJwt().getEncrypt().getRefresh().getToken();
		
		String CSRF_NAME = authProperties.getCsrf().getName();
		String CSRF_PARAMETER = authProperties.getCsrf().getParameter();
		String CSRF_HEADER = authProperties.getCsrf().getHeader();
		
		String AUTH_SERVER = serverProperties.getAuth();
		String JTI_SERVER = serverProperties.getJti();
		String HOME_SERVER = serverProperties.getHome();
		String ORIGIN_IP_API = serverProperties.getApi().getOriginIp();
		
		return http
			.authorizeHttpRequests(auth ->
					auth
							.requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
							.requestMatchers(getRequestMatchers(WHITE_LIST)).permitAll()
							.anyRequest().authenticated()
			)
			.formLogin(form ->
					form
							.loginPage("/login")
							.loginProcessingUrl("/loginproc")
							.authenticationDetailsSource(new AuthDetailsSource(ORIGIN_IP_API))
							.failureHandler(new AuthFailure(authDao, authUtil))
			)
			.logout(logout ->
					logout
							.logoutUrl("/logout")
							.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
							.logoutSuccessHandler(new AuthLogout(
									JWT_HEADER, JWT_KEY, JWT_NAME, JWT_DOMAIN, JWT_PATH,
									JTI_SERVER, authDao, redisTemplate
							))
							.deleteCookies("JSESSIONID")
							.invalidateHttpSession(true)
			)
			.oauth2Login(oauth2 ->
					oauth2
							.loginPage("/login")
							// 뒤의 true는 alwaysUse 파라미터에 들어가는 값임.
							// 로그인 성공 후 해당 URL로 리다이렉트.
							// 사용자가 이전에 어떤 페이지를 요청했든 상관없이.
							// 만약 alwaysUse 파라미터 없이 defaultSuccessUrl("login-success")로 설정했다면
							// 사용자가 로그인 전에 특정 페이지를 요청했었다면, 로그인 성공 후 그 페이지로 돌아감
							.defaultSuccessUrl("/login-success", true)
							.failureUrl("/login?error=true")
							.redirectionEndpoint(redirect ->
									redirect.baseUri("/oauth2/callback/*")
							)
							.userInfoEndpoint(endpoint ->
									endpoint.userService(new OAuth2Service())
							)
			)
			.headers(headers ->
					headers.frameOptions(frame -> frame.sameOrigin())
			)
			.cors(cors -> corsConfigurationSource())
			.csrf(csrf ->
					csrf
							.csrfTokenRepository(new CsrfRepository(CSRF_NAME, CSRF_PARAMETER, CSRF_HEADER))
							.csrfTokenRequestHandler(new CsrfHandler(CSRF_PARAMETER))
			)
//			.addFilterBefore(oAuth2AuthenticationDetailsFilter(), OAuth2LoginAuthenticationFilter.class)
			.addFilterBefore(new Redirect(
					JWT_HEADER, JWT_KEY, JWT_NAME, JWT_ISSUER, JWT_AUDIENCE, JWT_EXPIRED,
					JWT_DOMAIN, JWT_PATH, JTI_SERVER,
					encryptSignKey(JWT_ENCRYPT_SIGN), encryptTokenKey(JWT_ENCRYPT_TOKEN),
					AUTH_SERVER, HOME_SERVER, authDao, authUtil, redisTemplate
			), UsernamePasswordAuthenticationFilter.class)
			.addFilterAfter(new Csrf(), CsrfFilter.class)
			.addFilterAfter(new JwtIssue(
							JWT_NAME, JWT_ISSUER, JWT_AUDIENCE,
							JWT_REFRESH_ISSUER, JWT_REFRESH_AUDIENCE, JWT_DOMAIN, JWT_PATH, JWT_EXPIRED,
							encryptSignKey(JWT_ENCRYPT_SIGN), encryptTokenKey(JWT_ENCRYPT_TOKEN),
							encryptSignKey(JWT_ENCRYPT_REFRESH_SIGN), encryptTokenKey(JWT_ENCRYPT_REFRESH_TOKEN),
					HOME_SERVER, authDao, authUtil, redisTemplate
			), UsernamePasswordAuthenticationFilter.class)
			.sessionManagement(session ->
					session
							.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
							.sessionFixation().changeSessionId()
							.maximumSessions(7)
							.maxSessionsPreventsLogin(false)
							.expiredUrl("/")
			)
			.build();

	};
//		return http
//			.authorizeHttpRequests(auth ->
//				auth
//					.requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
//					.requestMatchers(getRequestMatchers(WHITE_LIST)).permitAll()
//					.anyRequest().authenticated()
//			)
//			.formLogin(form ->
//				form
//					.loginPage("/login")
//			).logout(logout ->
//				logout
//					.logoutUrl("/logout")
//					.logoutSuccessUrl("/login")
//					.deleteCookies("JSESSIONID")
//					.invalidateHttpSession(true)
//			)
//			.oauth2Login(oauth2 ->
//				oauth2
//					.loginPage("/login")
//					.defaultSuccessUrl("/home", true)
//					.failureUrl("/login?error=true")
//					.redirectionEndpoint(redirect ->
//						redirect
//							.baseUri("/oauth2/callback/*")
//					)
//					.userInfoEndpoint(endpoint ->
//						endpoint
//							.userService(new OAuth2Service())
//					)
//			)
//			.build();

	/** 비밀번호 암호화 ( 단방향 복호화 불가능 ) */
	@Bean
	public PasswordEncoder encoder() {

		PasswordEncoder encoder = new BCryptPasswordEncoder();
		return encoder;

	};

	/** CORS 정책 수립 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {

		CorsConfiguration corsConfig = new CorsConfiguration();
		corsConfig.addAllowedOriginPattern("*://*.dev.st2lla.co.kr");
		corsConfig.addAllowedOriginPattern("*://*.intra.st2lla.co.kr");
		corsConfig.setAllowCredentials(true);
		corsConfig.setMaxAge(3600L);
		corsConfig.setAllowedMethods(Arrays.asList("GET", "POST"));
		corsConfig.setAllowedHeaders(
				Arrays.asList(
						"Content-Type",
						"X-XSRF-TOKEN",
						"Authorization",
						"User-Agent",
						"Content-Length",
						"X-Requested-With",

						"REISSUE-ID",
						"REISSUE-IP",
						"REISSUE-AGENT",
						"REISSUE-DEVICE"
				)
		);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfig);

		return source;

	};

	/** session control */
	@Bean
	public SessionRegistry sessionRegistry() {
		return new SessionRegistryImpl();
	};

	// 서명과 토큰의 암호화를 위한 Key 설정
	private Key encryptSignKey(String key) {

		// Base64 인코딩된 문자열을 디코드하여 바이트 배열로 변환
		byte[] decodedKey = Base64.getDecoder().decode(key);

		// 바이트 배열을 사용하여 SecretKey 객체 생성
		return new SecretKeySpec(decodedKey, 0, decodedKey.length, "HmacSHA256");

	};
	private Key encryptTokenKey(String key) {

		byte[] decodedKey = Base64.getDecoder().decode(key);
		return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

	};


	private RequestMatcher[] getRequestMatchers(String... str) {
		return Arrays.stream(str)
			.map(AntPathRequestMatcher::new)
			.toArray(RequestMatcher[]::new);
	};
	
//	@Bean
//	public OAuth2AuthorizedClientService authorizedClientService(ClientRegistrationRepository clientRegistrationRepository) {
//		return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
//	}

//	private Filter oAuth2AuthenticationDetailsFilter() {
//		
//		OAuth2LoginAuthenticationFilter filter = new OAuth2LoginAuthenticationFilter(new OAuth2Service(), authorizedClientService);
//		filter.setAuthenticationDetailsSource(new OAuth2DetailsSource(ORIGIN_IP_API));
////		filter.setAuthenticationDetailsSource(new OAuth2AuthDetailsSource(ORIGIN_IP_API));
//		
//		return filter;
//		
//	}
	
}