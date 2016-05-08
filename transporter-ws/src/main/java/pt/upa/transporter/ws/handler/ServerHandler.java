package pt.upa.transporter.ws.handler;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.Certificate;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateFactory;
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
	public static final String REQUEST_PROPERTY_CERT = "certificate"; 
	
	public static final String RESPONSE_PROPERTY_CERT = "certificate";
	public static final String REQUEST_PROPERTY_DIGSIG = "digitalSig";
	public static final String RESPONSE_PROPERTY_DIGSIG = "digitalSig";

	public static final String REQUEST_HEADER_DIGSIG = "digitalSig";
	public static final String REQUEST_NS_DIGSIG = "urn:digSig";
	public static final String RESPONSE_HEADER_DIGSIG = "digitalSigResponse";
	public static final String RESPONSE_NS_DIGSIG = REQUEST_NS_DIGSIG;
	
	public static final String REQUEST_HEADER_CERT = "certificate";
	public static final String REQUEST_NS_CERT = "urn:cert";
	public static final String RESPONSE_HEADER_CERT = "certificateResponse";
	public static final String RESPONSE_NS_CERT = REQUEST_NS_CERT;

	public static final String CLASS_NAME = ServerHandler.class.getSimpleName();
	public static final String TOKEN = "server-handler";
	
	final static String CERTIFICATE_FILE = "example.cer";

	final static String KEYSTORE_FILE = "keystore.jks";
	final static String KEYSTORE_PASSWORD = "1nsecure";

	final static String KEY_ALIAS = "example";
	final static String KEY_PASSWORD = "ins3cur3";
	
	public static Certificate cert =null;
	public static PublicKey CApublicKey=null;
	String digSigValue=null;

	//TODO change absolute paths
	String CA_cert_path = "/home/daniel/Desktop/projetos/SistemasD/T_60-project/keys_2016_05_07__17_34_07/ca/";
	
	
	// returns true to continue processing
	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		if (outbound) {
			
			// outbound message

			// *** #8 ***
			// get token from response context
			String propertyValue = (String) smc.get(REQUEST_PROPERTY_DIGSIG);
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

				// add header element (name, namespace prefix, namespace)
				Name name = se.createName(RESPONSE_HEADER_DIGSIG, "e", RESPONSE_NS_DIGSIG);
				SOAPHeaderElement element = sh.addHeaderElement(name);

				// *** #9 ***
				// add header element value
				String newValue = propertyValue + "," + TOKEN;
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

				// get first header element
				Name name = se.createName(REQUEST_HEADER_CERT, "cert", REQUEST_NS_CERT);
				Iterator it = sh.getChildElements(name);
				// check header element
				if (it.hasNext()) {
					
				
				SOAPElement certElement = (SOAPElement) it.next();

				// get header element value
				String certValue = certElement.getValue();

				System.out.printf("%s %s got '%s'%n", CLASS_NAME,"debug digSig", certValue);
				} else {
					//TODO should only get certificate once
					System.out.printf("Header element %s not found.%n", REQUEST_HEADER_CERT);
				}
				
				
				
				
				// gets text to compare to digital signature
				String bodyValue=null;
				SOAPBody soapBody= se.getBody();
				Iterator it2 = soapBody.getChildElements();
	            while(it2.hasNext()) {
	                SOAPElement tempElement = (SOAPElement) it2.next();
	                bodyValue += "%nnewline%n" + tempElement.getValue();
	            }
	            System.out.printf("%n body element%n %s%n", bodyValue);
				
				
				
				
				
				// DIGITAL SIGNATURE
				
				name = se.createName(REQUEST_HEADER_DIGSIG, "digS", REQUEST_NS_DIGSIG);
				it = sh.getChildElements(name);
				// check header element
				
				// check for certified
				if (it.hasNext()) {
	
				SOAPElement digSigElement = (SOAPElement) it.next();

				// get header element value
				digSigValue = digSigElement.getValue();
				System.out.printf("%s %s got '%s'%n", CLASS_NAME,"debug cert", digSigValue);
				} else {
					//TODO should only get certificate once server should take care of this
					System.out.printf("Header element %s not found.%n", REQUEST_HEADER_DIGSIG);
				}
				
				try {
					cert=readCertificateFile(CA_cert_path);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				// public key
				CApublicKey = getPublicKeyFromCertificate(cert);
				boolean digSigVerify = false;
				byte[] cipheredSig = digSigValue.getBytes(); //temporary value
				byte[] result = bodyValue.getBytes();
				try {
					digSigVerify = verifyDigitalSignature(cipheredSig, result, CApublicKey);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (!digSigVerify) {
					//TODO check what else to do here
					System.out.printf("wrong digital signature%n");
					return true;
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

}