package pt.upa.transporter.ws.handler;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Iterator;
import java.util.Set;
import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

import static javax.xml.bind.DatatypeConverter.printHexBinary;

import javax.xml.ws.handler.MessageContext;

import pt.upa.transporter.ws.TransporterMain;

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
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;



public class ServerHandler implements SOAPHandler<SOAPMessageContext> {

	//TODO ver o que fazer com os urn's
	
	// CA manipula esta propriedade para o handler ter o certificado apenas uma vez
	
	// field with requested organization "parameter from non-handler code"
	public static String PROPERTY_ORGANIZATION=""; // should hold name of the current organization, for private own certificate path

	public static final String REQUEST_HEADER_NONCE = "nonce";
	public static final String REQUEST_NS_NONCE = "nonce";
	public static final String RESPONSE_HEADER_NONCE = "nonce";
	public static final String RESPONSE_NS_NONCE = REQUEST_NS_NONCE;
	
	public static final String REQUEST_HEADER_DIGSIG = "digitalSignature";
	public static final String REQUEST_NS_DIGSIG = "urn:digSig";
	public static final String RESPONSE_HEADER_DIGSIG = "digitalSignature";
	public static final String RESPONSE_NS_DIGSIG = REQUEST_NS_DIGSIG;
	
	public static String FOREIGN_ORG_PROPERTY = "";
	public static final String HEADER_ORG = "org";
	public static final String REQUEST_NS_ORG = "urn:org";
	public static final String RESPONSE_NS_ORG = REQUEST_NS_ORG;
	
	public static final String CLASS_NAME = ServerHandler.class.getSimpleName();

	public static Certificate cert=null;
	public static PublicKey publicKey=null;
	public static PrivateKey privateKey=null;
	String digSigValue=null;
	
	// returns true to continue processing the next handler, false otherwise
	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		System.out.printf("%s current class %n", CLASS_NAME);
		System.out.printf("organizacao: %s %n", PROPERTY_ORGANIZATION);

		if (outbound) {
			// outbound message
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
				String bodyValue="";
				SOAPBody soapBody= se.getBody();
				Iterator<?> it2 = soapBody.getChildElements();
				bodyValue=getFullBody(it2,bodyValue);
	            System.out.printf("%n outgoing message body element%n %s%n", bodyValue);
	            
	            Name name3=se.createName(HEADER_ORG,"org",RESPONSE_NS_ORG);
	            SOAPHeaderElement element3 = sh.addHeaderElement(name3);
	            element3.addTextNode(PROPERTY_ORGANIZATION);
	            

	            //nonces insertion into soap
	            Name name2 = se.createName(RESPONSE_HEADER_NONCE, "nonce",RESPONSE_NS_NONCE);
	            SOAPHeaderElement element2 = sh.addHeaderElement(name2);
	            SecureRandom random = null;
				try {
					random = SecureRandom.getInstance("SHA1PRNG");
				} catch (NoSuchAlgorithmException e1) {
		    		System.out.println("wrong algorithm");
				}
	    		//System.out.println("random's information: "+random.getProvider().getInfo());

	    		System.out.println("Generating random byte array ...");

	    		final byte array[] = new byte[16];
	    		random.nextBytes(array);
	            element2.addTextNode(printBase64Binary(array));
	            System.out.printf("%n element2Value: %s%n",element2.getValue());
	            
	            
	            //DIGSIG
				Name name = se.createName(RESPONSE_HEADER_DIGSIG, "digS", RESPONSE_NS_DIGSIG);
				SOAPHeaderElement element = sh.addHeaderElement(name);


				// add header element value
				String newValue=null;
				try {
					String KEYSTORE_FILE = "../keys_2016_05_06__20_39_05/" + PROPERTY_ORGANIZATION + "/"+PROPERTY_ORGANIZATION+".jks";
					String KEYSTORE_PASSWORD = "ins3cur3";
					String KEY_ALIAS = PROPERTY_ORGANIZATION;
					String KEY_PASSWORD = "1nsecure";
					
					privateKey= getPrivateKeyFromKeystore(KEYSTORE_FILE, KEYSTORE_PASSWORD.toCharArray(), KEY_ALIAS, KEY_PASSWORD.toCharArray());
					newValue = printBase64Binary(makeDigitalSignature(parseBase64Binary(bodyValue), privateKey)); 
				} catch (Exception e) {
					System.out.printf("erro ao ler keystore ou ler certificado");
					e.printStackTrace();
					throw new RuntimeException("erro ao ler certificado ou utilizar private key de keystore ao criar digSig ");
				}
				element.addTextNode(newValue);

				System.out.printf(" digSig:%n '%s' %n", element.getValue());
				System.out.println();
				
			} catch (SOAPException e) {
				System.out.printf("Failed to add SOAP header because of %s%n", e);
				throw new RuntimeException("excecao SOAP falhou a adicionar header");
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
					throw new RuntimeException("soap header n foi encontrado");
				}

				Name name4 = se.createName(HEADER_ORG, "org", RESPONSE_NS_ORG);
				Iterator<?> it4 = sh.getChildElements(name4);
				
				String foreignOrg =null;
				SOAPElement orgElement=null;
				if (it4.hasNext()) {
					orgElement = (SOAPElement) it4.next();
					foreignOrg = orgElement.getValue();
				System.out.printf("%s %s%n","debug foreignOrg:", foreignOrg);
				} else {
					System.out.printf("Header element %s not found%n", HEADER_ORG);
					throw new RuntimeException("foreign org header n foi encontrado");
				}
				FOREIGN_ORG_PROPERTY=foreignOrg;
				
				Name name3 = se.createName(REQUEST_HEADER_NONCE, "nonce", REQUEST_NS_NONCE);
				Iterator<?> it3 = sh.getChildElements(name3);
				
				// check for certified
				String nonceValue =null;
				SOAPElement nonceElement=null;
				if (it3.hasNext()) {
				nonceElement = (SOAPElement) it3.next();
				// get header element value
				nonceValue = nonceElement.getValue();
				System.out.printf("%s %s%n","debug nonce", nonceValue);
				} else {
					System.out.printf("Header element %s not found invalid signature.%n", REQUEST_HEADER_NONCE);
					throw new RuntimeException("nonce header n foi encontrado");
				}
				if(TransporterMain.nonceList.contains(parseBase64Binary(nonceValue))) {
					System.out.printf("nonce already present, replayed message%n", REQUEST_HEADER_NONCE);
					throw new RuntimeException("mensagem repetida, verificado em nonce list");
				}
				TransporterMain.nonceList.add(parseBase64Binary(nonceValue));

				// gets text to compare to digital signature
				String bodyValue="";
				SOAPBody soapBody= se.getBody();
				Iterator<?> it2 = soapBody.getChildElements();
				bodyValue=getFullBody(it2,bodyValue);
	            System.out.printf("%n body element%n %s%n", bodyValue);
	            
				// DIGITAL SIGNATURE
				Name name = se.createName(REQUEST_HEADER_DIGSIG, "digS", REQUEST_NS_DIGSIG);
				Iterator<?> it = sh.getChildElements(name);
				
				// check for certified
				if (it.hasNext()) {
				SOAPElement digSigElement = (SOAPElement) it.next();

				// get header element value
				digSigValue = digSigElement.getValue();
				System.out.printf("%s %s should print header digSig'%s'%n", CLASS_NAME,"debug digSig", digSigValue);
				} else {
					System.out.printf("Header element %s not found invalid signature.%n", REQUEST_HEADER_DIGSIG);
					throw new RuntimeException("digSig header n encontrado");
				}
				cert=TransporterMain.certMap.get(FOREIGN_ORG_PROPERTY); // static map
				if (cert == null) {
					System.out.printf("requested organization not found %n");
					throw new RuntimeException("certificado da organizacao n encontrado no mapa de certificados do transporter main");
				}
				// public key
				publicKey = getPublicKeyFromCertificate(cert);
				boolean digSigVerify = false;
				byte[] cipheredSig = parseBase64Binary(digSigValue); //temporary value
				System.out.printf("%n ciphered signature: %s%n%n",printHexBinary(cipheredSig));

				byte[] result = parseBase64Binary(bodyValue);
				//System.out.printf("text bytes: %s%n",printHexBinary(result));

				try {
					// compares ciphered digested message with soap body message
					digSigVerify = verifyDigitalSignature(cipheredSig, result, publicKey);
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException("erro ao verificar assinatura");
				}
				if (!digSigVerify) {
					System.out.printf("%n %n wrong digital signature after verification %n %n");
					throw new RuntimeException("confirmada assinatura errada depois de verificar");
				}
				System.out.println("digSig verified!%n"); //TODO fix same prob in client handler incoming

			} catch (SOAPException e) {
				System.out.printf("Failed to get SOAP header because of %s%n", e);
				throw new RuntimeException("sem soap headers");
			}

		}
		return true;
	}

	@Override
	public boolean handleFault(SOAPMessageContext context) {
		// n devera ser utilizado
		System.out.printf("%s current class %n", CLASS_NAME);
		throw new RuntimeException("from: handle fault: erro no server handler");
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
		System.out.printf("text bytes insides verifyDigSig method: %s%n",printHexBinary(bytes));

		
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
		public String getFullBody(Iterator<?> it,String result) {
		
		while(it.hasNext()) {
            SOAPElement tempElement = (SOAPElement) it.next();
            if(tempElement.getValue()==null) {
            	result+=getFullBody(tempElement.getChildElements(),result);
            } else {
            	result += tempElement.getValue();
            }
        }
		return result;
	}

}