package net.rails.ciphertext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import net.rails.ciphertext.exception.CiphertextException;
import net.rails.support.Support;

/**
 * Cipher class includes Base64/DES/3DES/RSA.
 * @author Jack
 *
 */
@SuppressWarnings("static-access")
public class Ciphertext {
	
	/**
	 * Includes Base64Worker.encode(bytes) and Base64Worker.decode(text).
	 */
	public final static class Base64Worker {
		
		public static String encode(byte[] binaryData){
			return new String(Support.base64().encode(binaryData));
		}
		
		public static byte[] decode(String base64Text){
			return Support.base64().decode(base64Text);
		}
	}

	/**
	 * DES Worker Class.
	 * key and iv length only is 8 bit. 
	 * @author Jack
	 *
	 */
	public final static class DESWorker {
		private DESKeySpec keySpec;
		private AlgorithmParameterSpec iv;
		private SecretKeyFactory keyFactory;
		private Key key;
		private Cipher cipher;

		public DESWorker(String keyText, String ivText)
				throws CiphertextException {
			super();
			try{
				keySpec = new DESKeySpec(keyText.getBytes());
				iv = new IvParameterSpec(ivText.getBytes());
				keyFactory = SecretKeyFactory.getInstance("DES");
				key = keyFactory.generateSecret(keySpec);
				cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			}catch(Exception e){
				throw new CiphertextException(e.getMessage(),e.getCause());
			}
		}

		public String encrypt(byte[] data) throws CiphertextException {
			try{
				cipher.init(Cipher.ENCRYPT_MODE, key, iv);
				return Base64Worker.encode(cipher.doFinal(data));
			}catch(Exception e){
				throw new CiphertextException(e.getMessage(),e.getCause());
			}
		}

		public byte[] decrypt(String cipherText) throws CiphertextException {
			try{
				cipher.init(Cipher.DECRYPT_MODE, key, iv);
				return cipher.doFinal(Base64Worker.decode(cipherText));
			}catch(Exception e){
				throw new CiphertextException(e.getMessage(),e.getCause());
			}
		}
		
		public static String encrypt(String keyText, String ivText, byte[] data)
				throws CiphertextException {
			return new DESWorker(keyText, ivText).encrypt(data);
		}

		public static byte[] decrypt(String keyText, String ivText,
				String cipherText) throws CiphertextException {
			return new DESWorker(keyText, ivText).decrypt(cipherText);
		}
	}
	
	public final static class ThreeDESWorker {
		public static final String Algorithm = "DESede";
		public static final String TRANS_FORMATION = "DESede/CBC/PKCS5Padding";
		private SecretKey key;
		private Cipher cipher;
		private IvParameterSpec iv;
		
		static {
			Security.addProvider(new com.sun.crypto.provider.SunJCE());
		}

		/**
		 * <p>Constructor Parameter(keyText,ivText) length must be 24.</p>
		 * <b> Defaults:</b>
		 * <p> key = new SecretKeySpec(keyText.getBytes(), "DESede"); </p>
		 * <p> Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding"); </p>
		 */
		public ThreeDESWorker(String keyText,String ivText) throws CiphertextException {
			super();
			try {
				key = new SecretKeySpec(keyText.getBytes(), Algorithm);
				cipher = Cipher.getInstance(TRANS_FORMATION);	
				iv = new IvParameterSpec(ivText.getBytes());
			} catch (Exception e) {
				throw new CiphertextException(e.getMessage(), e.getCause());
			}
		}

		public String encrypt(byte[] data) throws CiphertextException {
			try {
				cipher.init(Cipher.ENCRYPT_MODE, key,iv);
				return new String(Base64Worker.encode(cipher.doFinal(data)));
			} catch (Exception e) {
				throw new CiphertextException(e.getMessage(), e.getCause());
			}
		}

		public byte[] decrypt(String cipherText) throws CiphertextException {
			try {
				cipher.init(Cipher.DECRYPT_MODE, key,iv);
				return cipher.doFinal(Base64Worker.decode(cipherText));
			} catch (Exception e) {
				throw new CiphertextException(e.getMessage(), e.getCause());
			}
		}

		public static String encrypt(String keyText,String ivText,byte[] data)
				throws CiphertextException {
			return new ThreeDESWorker(keyText,ivText).encrypt(data);
		}

		public static byte[] decrypt(String keyText,String ivText,String cipherText)
				throws CiphertextException {
			return new ThreeDESWorker(keyText,ivText).decrypt(cipherText);
		}
	}
	
	/**
	 * AES Cipher
	 * @author Jack
	 *
	 */
	public final static class AESWorker {

		private KeyGenerator generator;
		private SecretKey secretkey;
		private SecretKeySpec spec;
		private Cipher cipher;
		public static String RANDOM_ALOGRITHM = "SHA1PRNG";

		public AESWorker(String keyText) throws CiphertextException {
			super();
			try {
				generator = KeyGenerator.getInstance("AES");
				SecureRandom random = SecureRandom.getInstance(RANDOM_ALOGRITHM);
				random.setSeed(keyText.getBytes());
				generator.init(128, random);
				secretkey = generator.generateKey();
				spec = new SecretKeySpec(secretkey.getEncoded(), "AES");
				cipher = Cipher.getInstance("AES");
			} catch (Exception e) {
				throw new CiphertextException(e.getMessage(), e.getCause());
			}
		}

		public String encrypt(byte[] date) throws CiphertextException {
			try {
				cipher.init(Cipher.ENCRYPT_MODE, spec);
				return Base64Worker.encode(cipher.doFinal(date));
			} catch (Exception e) {
				throw new CiphertextException(e.getMessage(), e.getCause());
			}
		}

		public byte[] decrypt(String cipherText) throws CiphertextException {
			try {
				cipher.init(Cipher.DECRYPT_MODE, spec);
				return cipher.doFinal(Base64Worker.decode(cipherText));
			} catch (Exception e) {
				throw new CiphertextException(e.getMessage(), e.getCause());
			}
		}
		
		public static String encrypt(String keyText, byte[] data)
				throws CiphertextException {
			return new AESWorker(keyText).encrypt(data);
		}

		public static byte[] decrypt(String keyText, String cipherText)
				throws CiphertextException {
			return new AESWorker(keyText).decrypt(cipherText);
		}
	}
	
	/**
	 * RSA Cipher
	 * @author Jack
	 *
	 */
	public final static class RSAWorker {

		public static String ALOGRITHM = "RSA";
		public static String RANDOM_ALOGRITHM = "SHA1PRNG";

		public final static class RSATool {

			private KeyPairGenerator generator;
			private KeyPair keyPair;

			public RSATool(byte[] seed) throws CiphertextException {
				super();
				try {
					generator = KeyPairGenerator.getInstance(ALOGRITHM);
					SecureRandom random = SecureRandom
							.getInstance(RANDOM_ALOGRITHM);
					random.setSeed(seed);
					generator.initialize(1024, random);
					keyPair = generator.generateKeyPair();
				} catch (NoSuchAlgorithmException e) {
					throw new CiphertextException(e.getMessage(),e.getCause());
				}
			}

			public PublicKey getPublicKey() {
				return keyPair.getPublic();
			}

			public PrivateKey getPrivateKey() {
				return keyPair.getPrivate();
			}

			public String getPublicKeyText() {
				return new String(Base64Worker.encode(getPublicKey().getEncoded()));
			}

			public String getPrivateKeyText() {
				return Base64Worker.encode(getPrivateKey().getEncoded());
			}
		}

		public static PublicKey readPublicKey(String keyText)
				throws CiphertextException {
			try {
				KeySpec keySpec = new X509EncodedKeySpec(
				Base64Worker.decode(keyText));
				KeyFactory keyFactory = KeyFactory.getInstance(ALOGRITHM);
				return keyFactory.generatePublic(keySpec);
			} catch (Exception e) {
				throw new CiphertextException(e.getMessage(),e.getCause());
			}
		}

		public static PrivateKey readPrivateKey(String keyText)
				throws CiphertextException {
			try {
				KeySpec keySpec = new PKCS8EncodedKeySpec(
				Base64Worker.decode(keyText));
				KeyFactory keyFactory = KeyFactory.getInstance(ALOGRITHM);
				return keyFactory.generatePrivate(keySpec);
			} catch (Exception e) {
				throw new CiphertextException(e.getMessage(),e.getCause());
			}
		}

		public static byte[] encrypt(Key key, byte[] data) throws CiphertextException {
			try {
				Cipher cipher = Cipher.getInstance(ALOGRITHM);
				cipher.init(Cipher.ENCRYPT_MODE, key);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				CipherInputStream cis = new CipherInputStream(
						new ByteArrayInputStream(data), cipher);
				byte[] buffer = new byte[1024];
				int r;
				try {
					while ((r = cis.read(buffer)) > 0) {
						out.write(buffer, 0, r);
					}
				} finally {
					cis.close();
					out.close();
				}
				return out.toByteArray();
			} catch (Exception e) {
				throw new CiphertextException(e.getMessage(),e.getCause());
			}
		}

		public static byte[] decrypt(Key key, byte[] cipherText)
				throws CiphertextException {
			try {
				Cipher cipher = Cipher.getInstance(ALOGRITHM);
				cipher.init(Cipher.DECRYPT_MODE, key);
				ByteArrayInputStream is = new ByteArrayInputStream(cipherText);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				CipherOutputStream cos = new CipherOutputStream(out, cipher);
				byte[] buffer = new byte[1024];
				int r;
				try {
					while ((r = is.read(buffer)) >= 0) {
						cos.write(buffer, 0, r);
					}
				} finally {
					is.close();
					cos.close();
					out.close();
				}
				return out.toByteArray();
			} catch (Exception e) {
				throw new CiphertextException(e.getMessage(),e.getCause());
			}
		}
	}
}
