package jj.stella.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class MainController {
	
	/** 로그인 메인페이지 */
	@GetMapping(value={"/", "/login"})
	public ModelAndView main(HttpServletRequest req) throws Exception {
		
		ModelAndView page = new ModelAndView();
		page.setViewName("index");
		
		return page;
		
	};

	/** 로그인 팝업창 */
	@GetMapping("/home")
	public void popupLoginSuccess(HttpServletResponse response) throws Exception {
		String script = """
			<script>
				window.opener.postMessage('oauth-success', window.location.origin);
				window.close();
			</script>
		""";
		response.setContentType("text/html;charset=UTF-8");
		response.getWriter().write(script);
	}

}

