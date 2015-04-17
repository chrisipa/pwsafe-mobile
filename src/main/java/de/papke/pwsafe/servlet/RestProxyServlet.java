package de.papke.pwsafe.servlet;

import java.io.IOException;
import java.net.URI;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestHandler;

import de.papke.pwsafe.Constants;
import de.papke.pwsafe.http.TrustAllSSLCertHttpClient;

/**
 * REST servlet class for proxing all requests 
 * to original webpasswordsafe webapp. 
 * 
 * @author Christoph Papke (info@christoph-papke.de)
 */
@Component("rest")
public class RestProxyServlet extends ProxyServlet implements HttpRequestHandler {

	private static final long serialVersionUID = 1L;
	
	private JSONParser jsonParser;

	@Value("${pwsafe.webservice.url}")	
	private String webServiceUrl;

	/**
	 * Method for initializing the servlet. 
	 * The trust all ssl cert HTTP client is used here.
	 * 
	 * @see TrustAllSSLCertHttpClient
	 * @throws Exception
	 */
	@PostConstruct
	public void initialize() throws Exception {
		
		this.jsonParser = new JSONParser();
	    this.doLog = false;
	    this.doForwardIP = true;
	    this.targetUriObj = new URI(webServiceUrl);
	    this.targetUri = targetUriObj.toString();

	    HttpParams httpParams = new BasicHttpParams();
	    httpParams.setParameter(ClientPNames.HANDLE_REDIRECTS, true);
	    
	    this.proxyClient = new TrustAllSSLCertHttpClient(httpParams);
	}

	/* (non-Javadoc)
	 * @see org.springframework.web.HttpRequestHandler#handleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session = request.getSession();
		Boolean allowed = (Boolean) session.getAttribute(Constants.SESSION_ATTRIBUTE_ALLOWED);
		
		// only process the request if user is allowed
		if (allowed != null && allowed.booleanValue() == true) {
			super.service(request, response);
		}
		else {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
		}
	}

	/* (non-Javadoc)
	 * @see org.mitre.dsmiley.httpproxy.ProxyServlet#copyRequestHeaders(javax.servlet.http.HttpServletRequest, org.apache.http.HttpRequest)
	 */
	@Override
	protected void copyRequestHeaders(HttpServletRequest servletRequest, HttpRequest proxyRequest) {
		
		// copy all request headers
		super.copyRequestHeaders(servletRequest, proxyRequest);
		
		// get session attributes
		HttpSession session = servletRequest.getSession();
		String username = (String) session.getAttribute(Constants.SESSION_ATTRIBUTE_USERNAME);
		String password = (String) session.getAttribute(Constants.SESSION_ATTRIBUTE_PASSWORD);
		
		// set username and password as additional request headers
		if (username != null && password != null) {
			proxyRequest.setHeader(Constants.X_WPS_USERNAME, username);
			proxyRequest.setHeader(Constants.X_WPS_PASSWORD, password);
		}
		
	}
	
	/**
	 * Method for authenticating a user with a password. 
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public boolean login(String username, String password) {

		boolean allowed = false;
		HttpResponse response = null;
		
		// create dummy HTTP GET request to check if user credentials are valid
		HttpGet httpGet = new HttpGet(webServiceUrl + "/passwords");
		httpGet.addHeader(Constants.X_WPS_USERNAME, username);
		httpGet.addHeader(Constants.X_WPS_PASSWORD, password);
		
		try {
			// execute HTTP GET request
			response = proxyClient.execute(httpGet);
			
			// get HTTP response as string
			String responseString = EntityUtils.toString(response.getEntity());
			
			// parse response string to json
			JSONObject jsonObject = (JSONObject) jsonParser.parse(responseString);
			if (jsonObject != null) {
				
				// keyword "success" -> user credentials are valid
				Object successObject = jsonObject.get("success");
				if (successObject != null && successObject instanceof Boolean) {
					allowed = (Boolean) successObject;
				}
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			// close HTTP connection
			if (response != null) {
				EntityUtils.consumeQuietly(response.getEntity());
			}
		}
		
		return allowed;
	}
}
