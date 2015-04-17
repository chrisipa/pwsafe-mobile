package de.papke.pwsafe.http;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;

/**
 * HTTP client class which accepts all kinds of SSL certificates
 * (self-signed, expired, ...). 
 * 
 * @author Christoph Papke (info@christoph-papke.de)
 *
 */
@SuppressWarnings("deprecation")
public class TrustAllSSLCertHttpClient extends DefaultHttpClient {
	
	public TrustAllSSLCertHttpClient(HttpParams httpParams) {
		super(httpParams);
	}

	@Override
	protected ClientConnectionManager createClientConnectionManager() {
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", new TrustAllSSLCertSocketFactory(), 443));
    	return new ThreadSafeClientConnManager(getParams(), schemeRegistry);
	}
}