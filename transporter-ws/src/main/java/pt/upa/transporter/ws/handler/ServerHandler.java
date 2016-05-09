package pt.upa.transporter.ws.handler;

import java.awt.List;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.Certificate;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import javax.xml.soap.SOAPBody;
import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;


public class ServerHandler implements SOAPHandler<SOAPMessageContext> {

	//TODO ver o que fazer com os urn's
	
	// CA manipula esta propriedade para o handler ter o certificado apenas uma vez
	
	// field with requested organization "parameter from non-handler code"
	public static final String REQUEST_PROPERTY_ORGANIZATION="";
	public static final String RESPONSE_PROPERTY_ORGANIZATION=""; // should hold name of the current organization, for private own certificate path
	public static final String REQUEST_HEADER_ORGANIZATION = "org";
	public static final String REQUEST_NS_ORGANIZATION = "urn:requestedOrg";
	public static final String RESPONSE_HEADER_ORGANIZATION = "org";
	public static final String RESPONSE_NS_ORGANIZATION = REQUEST_NS_ORGANIZATION;

	public static final String REQUEST_HEADER_NONCE = "nonce";
	public static final String REQUEST_NS_NONCE = "nonce";
	public static final String RESPONSE_HEADER_NONCE = "nonce";
	public static final String RESPONSE_NS_NONCE = REQUEST_NS_NONCE;
	
	
	public static final String REQUEST_PROPERTY_DIGSIG = "";
	public static final String RESPONSE_PROPERTY_DIGSIG = "";
	public static final String REQUEST_HEADER_DIGSIG = "digitalSig";
	public static final String REQUEST_NS_DIGSIG = "urn:digSig";
	public static final String RESPONSE_HEADER_DIGSIG = "digitalSigResponse";
	public static final String RESPONSE_NS_DIGSIG = REQUEST_NS_DIGSIG;
	
	ArrayList<byte[]> nonceList= new ArrayList<byte[]>();
	
	public static final String CLASS_NAME = ServerHandler.class.getSimpleName();
	public static final String TOKEN = "server-handler";
	
	final static String CERTIFICATE_FILE = "example.cer";

	final static String KEYSTORE_FILE = "keystore.jks";
	final static String KEYSTORE_PASSWORD = "1nsecure";

	final static String KEY_ALIAS = "example";
	final static String KEY_PASSWORD = "ins3cur3";
	
	public static Certificate cert=null;
	public static PublicKey publicKey=null;
	public static PrivateKey privateKey=null;
	String digSigValue=null;

	//TODO change absolute paths

	
	// returns true to continue processing the next handler, false otherwise
	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		if (outbound) {
			
			// outbound message

			// get token from response context
			// TODO check following lines output, delete when no longer needed, testing purposes only
			String propertyValue = (String) smc.get(RESPONSE_PROPERTY_DIGSIG); 
			System.out.printf("%s received '%s'%n", CLASS_NAME, propertyValue);

			// put token in response SOAP header
			try {
				// get SOAP envelope
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				// add header
				SOAPHeader sh = se.getHeader();
				if (sh == null)
					sh = se.addHeader();
				// gets text to make digital signature
				String bodyValue=null;
				SOAPBody soapBody= se.getBody();
				Iterator it2 = soapBody.getChildElements();
	            while(it2.hasNext()) {
	                SOAPElement tempElement = (SOAPElement) it2.next();
	                bodyValue +=  tempElement.getValue();
	            }
	            System.out.printf("%n outgoing message body element%n %s%n", bodyValue);
	            
	            //nonces insertion into soap
	            
	            Name name2 = se.createName(RESPONSE_HEADER_NONCE, "nonce",RESPONSE_NS_NONCE);
	            SOAPHeaderElement element2 = sh.addHeaderElement(name2);
	            SecureRandom random = null;
				try {
					random = SecureRandom.getInstance("SHA1PRNG");
				} catch (NoSuchAlgorithmException e1) {
		    		System.out.println("wrong algorithm");
				}
	    		System.out.println("random's information: "+random.getProvider().getInfo());

	    		System.out.println("Generating random byte array ...");

	    		final byte array[] = new byte[16];
	    		random.nextBytes(array); // random num in "array"
	            element2.addTextNode(array.toString());
	            
				// add header element (name, namespace prefix, namespace)
				Name name = se.createName(RESPONSE_HEADER_DIGSIG, "digS", RESPONSE_NS_DIGSIG);
				SOAPHeaderElement element = sh.addHeaderElement(name);
				Iterator it = sh.getChildElements(name);
				
				if (it.hasNext()) {
					SOAPElement digSigElement = (SOAPElement) it.next();
					// get header element value
					digSigValue = digSigElement.getValue();
					System.out.printf("%s %s should print header digSig'%s'%n", CLASS_NAME,"debug digSig", digSigValue);
				} else {
					System.out.printf("Header element %s not found in outgoing message%n", RESPONSE_HEADER_DIGSIG);
					return false;
				}

				// add header element value
				String newValue=null;
				try {
					cert = readCertificateFile("../keys_2016_05_06__20_39_05/" + RESPONSE_PROPERTY_ORGANIZATION+ "/"+ "RESPONSE_PROPERTY_ORGANIZATION"+".cer");
					
					//TODO check path and everything else
					String KEYSTORE_FILE = "../keys_2016_05_06__20_39_05/" + RESPONSE_PROPERTY_ORGANIZATION + "/"+RESPONSE_PROPERTY_ORGANIZATION+".jks";
					String KEYSTORE_PASSWORD = "1nsecure";
					String KEY_ALIAS = RESPONSE_PROPERTY_ORGANIZATION;
					String KEY_PASSWORD = "ins3cur3";
					
					privateKey= getPrivateKeyFromKeystore(KEYSTORE_FILE, KEYSTORE_PASSWORD.toCharArray(), KEY_ALIAS, KEY_PASSWORD.toCharArray());
					newValue = makeDigitalSignature(bodyValue.getBytes(), privateKey).toString(); // toString to add SOAP
				} catch (Exception e) {
					System.out.printf("n consegue ler certificado");
					e.printStackTrace();
					return false;
				}
				element.addTextNode(newValue);

				System.out.printf("%s put token '%s' on response message header%n", CLASS_NAME, TOKEN);

			} catch (SOAPException e) {
				System.out.printf("Failed to add SOAP header because of %s%n", e);
			}

			
		} else {
			// inbound message

			// get token from request SOAP header
			try {
				// get SOAP envelope header
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				SOAPHeader sh = se.getHeader();

				// check header
				if (sh == null) {
					System.out.println("Header not found.");
					return true;
				}
				
				Name name3 = se.createName(REQUEST_HEADER_NONCE, "nonce", REQUEST_NS_NONCE);
				Iterator it3 = sh.getChildElements(name3);
				// check header element
				
				// check for certified
				String nonceValue =null;
				SOAPElement nonceElement=null;
				if (it3.hasNext()) {
				nonceElement = (SOAPElement) it3.next();
				// get header element value
				nonceValue = nonceElement.getValue();
				System.out.printf("%s %s should print header digSig'%s'%n", CLASS_NAME,"debug nonce", nonceValue);
				} else {
					System.out.printf("Header element %s not found invalid signature.%n", REQUEST_HEADER_NONCE);
					return true;
				}
				if(nonceList.contains(nonceValue.getBytes())) {
					System.out.printf("nonce already present, replayed message%n", REQUEST_HEADER_NONCE);
					return true;
				}
				nonceList.add(nonceValue.getBytes());

				// gets text to compare to digital signature
				String bodyValue=null;
				SOAPBody soapBody= se.getBody();
				Iterator it2 = soapBody.getChildElements();
	            while(it2.hasNext()) {
	                SOAPElement tempElement = (SOAPElement) it2.next();
	                bodyValue +=  tempElement.getValue();
	            }
	            System.out.printf("%n body element%n %s%n", bodyValue);
				
				
				
				// DIGITAL SIGNATURE
				
				Name name = se.createName(REQUEST_HEADER_DIGSIG, "digS", REQUEST_NS_DIGSIG);
				Iterator it = sh.getChildElements(name);
				// check header element
				
				// check for certified
				if (it.hasNext()) {
				SOAPElement digSigElement = (SOAPElement) it.next();

				// get header element value
				digSigValue = digSigElement.getValue();
				System.out.printf("%s %s should print header digSig'%s'%n", CLASS_NAME,"debug digSig", digSigValue);
				} else {
					System.out.printf("Header element %s not found invalid signature.%n", REQUEST_HEADER_DIGSIG);
					return true;
				}
				
				cert=CertCheckHandlerServer.certMap.get(REQUEST_PROPERTY_ORGANIZATION); // static map
				if (cert == null) {
					System.out.printf("requested organization not found %n");
					return true;
				}
				// public key
				publicKey = getPublicKeyFromCertificate(cert);
				boolean digSigVerify = false;
				byte[] cipheredSig = digSigValue.getBytes(); //temporary value
				byte[] result = bodyValue.getBytes();
				try {
					// compares ciphered digested message with soap body message
					digSigVerify = verifyDigitalSignature(cipheredSig, result, publicKey);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (!digSigVerify) {
					//TODO check what else to do here
					System.out.printf("wrong digital signature after verification %n");
					return false;
				}
		
			} catch (SOAPException e) {
				System.out.printf("Failed to get SOAP header because of %s%n", e);
			}

		}
		return true;
	}

	@Override
	public boolean handleFault(SOAPMessageContext context) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void close(MessageContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<QName> getHeaders() {
		// TODO Auto-generated method stub
		return null;
	} 
	
	public static byte[] makeDigitalSignature(byte[] bytes, PrivateKey privateKey) throws Exception {
		
		// get a signature object using the SHA-1 and RSA combo
		// and sign the plain-text with the private key
		Signature sig = Signature.getInstance("SHA1WithRSA");
		sig.initSign(privateKey);
		sig.update(bytes);
		byte[] signature = sig.sign();

		return signature;
}

	/**
	 * auxiliary method to calculate new digest from text and compare it to the
	 * to ciphered digest
	 */
	public static boolean verifyDigitalSignature(byte[] cipherDigest, byte[] bytes, PublicKey publicKey)
			throws Exception {
	
		// verify the signature with the public key
		Signature sig = Signature.getInstance("SHA1WithRSA");
		sig.initVerify(publicKey);
		sig.update(bytes);
		try {
			return sig.verify(cipherDigest);
		} catch (SignatureException se) {
			System.err.println("Caught exception while verifying signature " + se);
			return false;
		}
	}
	
	// gets public key from cert
	public static PublicKey getPublicKeyFromCertificate(Certificate certificate) {
		return certificate.getPublicKey();
	}

	/**
	 * Reads a certificate from a file
	 * 
	 * @return
	 * @throws Exception
	 */
	public static Certificate readCertificateFile(String certificateFilePath) throws Exception {
		FileInputStream fis;

		try {
			fis = new FileInputStream(certificateFilePath);
		} catch (FileNotFoundException e) {
			System.err.println("Certificate file <" + certificateFilePath + "> not fount.");
			return null;
		}
		BufferedInputStream bis = new BufferedInputStream(fis);

		CertificateFactory cf = CertificateFactory.getInstance("X.509");

		if (bis.available() > 0) {
			Certificate cert = (Certificate) cf.generateCertificate(bis);
			return cert;
			// It is possible to print the content of the certificate file:
			// System.out.println(cert.toString());
		}
		bis.close();
		fis.close();
		return null;
	}
	
	/**
	 * Reads a PrivateKey from a key-store
	 * 
	 * @return The PrivateKey
	 * @throws Exception
	 */
	public static PrivateKey getPrivateKeyFromKeystore(String keyStoreFilePath, char[] keyStorePassword,
			String keyAlias, char[] keyPassword) throws Exception {

		KeyStore keystore = readKeystoreFile(keyStoreFilePath, keyStorePassword);
		PrivateKey key = (PrivateKey) keystore.getKey(keyAlias, keyPassword);

		return key;
	}

	/**
	 * Reads a KeyStore from a file
	 * 
	 * @return The read KeyStore
	 * @throws Exception
	 */
	public static KeyStore readKeystoreFile(String keyStoreFilePath, char[] keyStorePassword) throws Exception {
		FileInputStream fis;
		try {
			fis = new FileInputStream(keyStoreFilePath);
		} catch (FileNotFoundException e) {
			System.err.println("Keystore file <" + keyStoreFilePath + "> not fount.");
			return null;
		}
		KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		keystore.load(fis, keyStorePassword);
		return keystore;
	}

}