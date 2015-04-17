package de.papke.pwsafe.http;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * Trust manager class which accepts all kinds of SSL certificates
 * (self-signed, expired, ...). 
 * 
 * @author Christoph Papke (info@christoph-papke.de)
 *
 */
public class TrustAllSSLCertTrustManager implements X509TrustManager {

	public TrustAllSSLCertTrustManager(KeyStore keystore) throws NoSuchAlgorithmException, KeyStoreException {
		super();
	}

	public void checkClientTrusted(X509Certificate[] certificates, String authType) throws CertificateException {
	}

	public void checkServerTrusted(X509Certificate[] certificates, String authType) throws CertificateException {
	}

	public X509Certificate[] getAcceptedIssuers() {
		return null;
	}
}