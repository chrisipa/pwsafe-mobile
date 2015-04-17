package de.papke.pwsafe.maven;

import java.util.ResourceBundle;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

/**
 * Maven properties class for accessing project properties.
 * 
 * @author Christoph Papke (info@christoph-papke.de)
 */
@Component
public class MavenProperties {
	
	private ResourceBundle resourceBundle;

	@PostConstruct
	public void initialize() {
		try {
			this.resourceBundle = ResourceBundle.getBundle("maven");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public Object get(String key) {
		return resourceBundle.getObject(key);
	}
}
