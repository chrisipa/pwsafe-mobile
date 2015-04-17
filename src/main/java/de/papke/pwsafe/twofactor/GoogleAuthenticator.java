package de.papke.pwsafe.twofactor;

import java.awt.Desktop;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base32;

/**
 * Java Server side class for Google Authenticator's TOTP generator 
 * Thanks to Enrico's blog for the sample code.
 * 
 * @see http://thegreyblog.blogspot.com/2011/12/google-authenticator-using-it-in-your.html 
 * @see http://code.google.com/p/google-authenticator 
 * @see http://tools.ietf.org/id/draft-mraihi-totp-timebased-06.txt
 * 
 * @author Christoph Papke (info@christoph-papke.de)
 */
public class GoogleAuthenticator {
		
	// taken from Google pam docs - we probably don't need to mess with these
	private static final int SECRET_SIZE = 10;
	private static final int NUMBER_OF_SCRATCH_CODES = 5;
	private static final int SCRATCH_CODE_SIZE = 8;
	private static final String ENCRYPTION_ALGORITHM = "HmacSHA1";
	private static final String QR_CODE_URL = "https://www.google.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl=otpauth://totp/%s@%s%%3Fsecret%%3D%s";
	
	public static void main(String[] args) throws Exception {
		
		Scanner console = new Scanner(System.in);
		
		System.out.print("Enter username: ");
		String username = console.nextLine().trim();
		System.out.print("Enter host: ");
		String host = console.nextLine().trim();
		
		String secret = generateSecretKey();
		URI url = new URI(getQRBarcodeURL(username, host, secret));
		Desktop.getDesktop().browse(url);
		System.out.println("Secret: " + secret);
	}
	
	/**
	 * Generate a random secret key. This must be saved by the server and associated with the 
	 * users account to verify the code displayed by Google Authenticator. 
	 * The user must register this secret on their device. 
	 * 
	 * @return secret key
	 */
	public static String generateSecretKey() {
		
		// allocating the buffer
		byte[] buffer = new byte[SECRET_SIZE + NUMBER_OF_SCRATCH_CODES* SCRATCH_CODE_SIZE];

		// filling the buffer with random numbers.
		// notice: you want to reuse the same random generator
		// while generating larger random number sequences.
		new Random().nextBytes(buffer);

		// getting the key and converting it to Base32
		Base32 codec = new Base32();
		byte[] secretKey = Arrays.copyOf(buffer, SECRET_SIZE);
		byte[] bEncodedKey = codec.encode(secretKey);
		String encodedKey = new String(bEncodedKey);
		
		return encodedKey;
	}

	/**
	 * Return a URL that generates and displays a QR barcode. The user scans this bar code with the
	 * Google Authenticator application on their smartphone to register the auth code. They can also manually enter the
	 * secret if desired.
	 * 
	 * @param user   user id (e.g. fflinstone)
	 * @param host   host or system that the code is for (e.g. myapp.com)
	 * @param secret the secret that was previously generated for this user
	 * @return the URL for the QR code to scan
	 */
	public static String getQRBarcodeURL(String user, String host, String secret) {
		return String.format(QR_CODE_URL, user, host, secret);
	}

	/**
	 * Check the code entered by the user to see if it is valid.
	 * 
	 * @param secret  The users secret. 
	 * @param code  The code displayed on the users device
	 * @return
	 */
	public static boolean checkCode(String secret, int windowSize, long code) {
		
		Base32 codec = new Base32();
		byte[] decodedKey = codec.decode(secret);
			
		// convert unix msec time into a 30 second "window" 
		// this is per the TOTP spec (see the RFC for details)
		long t = (System.currentTimeMillis() / 1000L) / 30L;

		// window is used to check codes generated in the near past.
		// you can use this value to tune how far you're willing to go.
		for (int i = -windowSize; i <= windowSize; ++i) {
			
			long hash;
			
			try {
				hash = verifyCode(decodedKey, t + i);
			}
			catch (Exception e) {
				// yes, this is bad form - but
				// the exceptions thrown would be rare and a static configuration problem
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}
			
			if (hash == code) {
				return true;
			}
		}
		
		// the validation code is invalid.
		return false;
	}
	
	/**
	 * Verify the if the hash fits to the given timestamp.
	 * 
	 * @param key
	 * @param t
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 */
	private static int verifyCode(byte[] key, long t) throws NoSuchAlgorithmException, InvalidKeyException {
		
		byte[] data = new byte[8];
		long value = t;
		
		for (int i = 8; i-- > 0; value >>>= 8) {
			data[i] = (byte) value;
		}

		SecretKeySpec signKey = new SecretKeySpec(key, ENCRYPTION_ALGORITHM);
		Mac mac = Mac.getInstance(ENCRYPTION_ALGORITHM);
		mac.init(signKey);
		byte[] hash = mac.doFinal(data);
		int offset = hash[20 - 1] & 0xF;

		// we're using a long because Java hasn't got unsigned int.
		long truncatedHash = 0;
		for (int i = 0; i < 4; ++i) {
			
			truncatedHash <<= 8;
			
			// we are dealing with signed bytes:
			// we just keep the first byte.
			truncatedHash |= (hash[offset + i] & 0xFF);
		}

		truncatedHash &= 0x7FFFFFFF;
		truncatedHash %= 1000000;

		return (int) truncatedHash;
	}
}