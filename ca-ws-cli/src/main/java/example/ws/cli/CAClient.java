package example.ws.cli;
import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.List;
import java.util.Map;
import javax.xml.ws.BindingProvider;

import example.ws.*;

public class CAClient {
	private CA port;
	
	public CAClient(String url) {
		CAImplService service = new CAImplService();
		CA port = service.getCAImplPort();
		BindingProvider bindingProvider = (BindingProvider) port;
		Map<String, Object> requestContext = bindingProvider.getRequestContext();
		requestContext.put(ENDPOINT_ADDRESS_PROPERTY, url);
	}
	
	public byte[] getCertificate(String name) {
		return port.getCertificate(name);
	}
	
}