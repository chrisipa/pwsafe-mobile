package de.papke.pwsafe.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import de.papke.pwsafe.Constants;
import de.papke.pwsafe.maven.MavenProperties;
import de.papke.pwsafe.servlet.RestProxyServlet;
import de.papke.pwsafe.twofactor.GoogleAuthenticatorService;

/**
 * Session controller class for creating a user session and 
 * presenting the default view.
 * 
 * @author Christoph Papke (info@christoph-papke.de)
 */
@Controller
public class SessionController {
	
	@Autowired 
	private GoogleAuthenticatorService googleAuthenticatorService;
	
	@Autowired
	private RestProxyServlet restProxyServlet;
	
	@Autowired
	private MavenProperties mavenProperties;
	
	/**
	 * Method for presenting the default view.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView welcomePage(HttpServletRequest request) {

		HttpSession session = request.getSession();
		
		// get allowed flag from session
		Boolean allowed = false;
		Object allowedObject = session.getAttribute(Constants.SESSION_ATTRIBUTE_ALLOWED);
		if (allowedObject != null && allowedObject instanceof Boolean) {
			allowed = (Boolean) allowedObject;
		}
		
		// get username from session
		String username = null;
		Object usernameObject = session.getAttribute(Constants.SESSION_ATTRIBUTE_USERNAME);
		if (usernameObject != null && usernameObject instanceof String) {
			username = (String) usernameObject;
		}
		
		// create model and view
		ModelAndView model = new ModelAndView();
		model.addObject(Constants.APP_NAME, mavenProperties.get(Constants.APP_NAME));
		model.addObject(Constants.APP_VERSION, mavenProperties.get(Constants.APP_VERSION));
		model.addObject(Constants.SESSION_ATTRIBUTE_ALLOWED, allowed);
		model.addObject(Constants.SESSION_ATTRIBUTE_USERNAME, username);
		model.addObject(Constants.GOOGLE_AUTH_ENABLED, googleAuthenticatorService.isEnabled());
		model.setViewName("index");
		
		return model;
	}
	
	/**
	 * Method for user login.
	 * 
	 * @param username
	 * @param password
	 * @param code
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public ModelAndView login(
			@RequestParam(value = "username", required = true) String username,
			@RequestParam(value = "password", required = true) String password,
			@RequestParam(value = "code", required = false) String code,
			HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		boolean allowed = false;
		
		try {
			
			// check if user is allowed to use the application
			allowed = restProxyServlet.login(username, password) && (googleAuthenticatorService.isEnabled() ? googleAuthenticatorService.check(username, code) : true);
			
			// allowed -> set necessary session attributes
			if (allowed) {
				HttpSession session = request.getSession();
				session.setAttribute(Constants.SESSION_ATTRIBUTE_ALLOWED, true);
				session.setAttribute(Constants.SESSION_ATTRIBUTE_USERNAME, username);
				session.setAttribute(Constants.SESSION_ATTRIBUTE_PASSWORD, password);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		// not allowed -> send http status 403
		if (!allowed) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
		}

		return null;
	}
	
	/**
	 * Method for user logout.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public ModelAndView logout(HttpServletRequest request) {
		request.getSession().invalidate();
		return null;
	}		
}
