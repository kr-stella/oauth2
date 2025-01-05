package jj.stella.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class AuthController {
	
	private static final String LOGIN_SUCCESS = "<script>window.opener.postMessage(\"login-success\", \"*\"); window.close();</script>";
	
	/** 로그인 팝업창 */
	@GetMapping("/login-success")
	public ResponseEntity<?> loginSuccess(HttpServletRequest request) {
		return ResponseEntity.ok(LOGIN_SUCCESS);
	}
	
}