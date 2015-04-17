package de.papke.pwsafe.twofactor;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Spring component class for using the google authenticator logic.
 * 
 * @author Christoph Papke (info@christoph-papke.de)
 */
@Component
public class GoogleAuthenticatorService {
	
	private Map<String, String> secretMap;
	
	@Value("${google.authenticator.enabled}")
	private boolean enabled;
	
	@Value("${google.authenticator.window.size}")
	private int windowSize;
	
	/**
	 * Check if code is correct.
	 * You can have different secrets dependent on the user.
	 * 
	 * @param username
	 * @param code
	 * @return
	 * @throws Exception
	 */
	public boolean check(String username, String code) throws Exception {
		
		//TODO: Use google auth library from: https://github.com/wstrange/GoogleAuth
		String secret = secretMap.get(username);
		return GoogleAuthenticator.checkCode(secret, windowSize, Long.parseLong(code));
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}	
	
	public int getWindowSize() {
		return windowSize;
	}

	public Map<String, String> getSecretMap() {
		return secretMap;
	}

	public void setSecretMap(Map<String, String> secretMap) {
		this.secretMap = secretMap;
	}
}
