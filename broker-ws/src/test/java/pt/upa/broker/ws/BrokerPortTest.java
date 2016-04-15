package pt.upa.broker.ws;

import static org.junit.Assert.*;

import java.util.Map;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.JobStateView;
import pt.upa.transporter.ws.JobView;
import pt.upa.transporter.ws.TransporterPortType; 
import pt.upa.transporter.ws.TransporterService;

public class BrokerPortTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public <P extends TransporterPortType & BindingProvider>
	void testMockTransporterServer(
        @Mocked final TransporterService service,
        @Mocked final P port,
        @Mocked final Map<String,Object> requestCont, // P extends the above types
        @Mocked final UDDINaming uddi)
        throws Exception {
		/* this test should always work
		 * starting Template Test
		 * */
		// declarations
		 new Expectations() {{
	            new UDDINaming(anyString);
	      }};
	      //TESTS
	      BrokerPort bp = new BrokerPort("placeHolderUrl");
	      String ping = bp.ping("string");
	      assertEquals("error in equals","string", ping); // checks ping result
	      new Verifications() {{
              // Verifies that zero or one invocations occurred, with the specified argument value:
	      }};
	      // asserts if needed
	}

	@Test
	public <P extends TransporterPortType & BindingProvider>
	void testRequestTransportInvalidPriceFault_Exception(
		@Mocked final TransporterService service,
        @Mocked final P port,
        @Mocked final Map<String,Object> requestCont,
        @Mocked final UDDINaming uddi) // P extends the above types
        throws Exception {
		// declarations
		
		final JobView j1 = new JobView();
		j1.setJobPrice(1);
		j1.setJobOrigin("Lisboa");
		j1.setJobDestination("Porto");
		j1.setJobIdentifier("2/0");
		j1.setJobState(null);		
		j1.setCompanyName("T1");
		 new Expectations() {{
	            new TransporterService(); minTimes=0;
	            service.getTransporterPort(); result = port;minTimes=0;
	            port.getRequestContext(); result = requestCont;minTimes=0;
	            requestCont.put(anyString, anyString); minTimes=0;
	            uddi.list(anyString); result= "T1"; minTimes=0;// check if needs result
	            port.requestJob("Lisboa", "Porto", anyInt); minTimes=0; 
	            result = j1;
	            port.decideJob(anyString, anyBoolean); minTimes=0;
	      }};
	      BrokerPort bp = new BrokerPort("placeholderURL");
	      assertNotNull("null broker port",bp);
	      try {
	    	  bp.requestTransport("Lisboa", "Porto", 100);
	    	  assertNotNull(bp);
	      } catch (InvalidPriceFault_Exception e) {
	    	  fail("caught price fault exception with right price input");
	      }
	      try {
	    	  bp.requestTransport("Lisboa", "Porto", -1);
	    	  fail("price input <0 no exception thrown fail");
	      } catch (InvalidPriceFault_Exception e) {
	    	  assertEquals("Preço incorreto", e.getMessage());
	      } 
	      new Verifications() {{
	    	  // Verifies that zero or one invocations occurred, with the specified argument value:
	      }};
	}
	
	@Test
	public <P extends TransporterPortType & BindingProvider>
	void testRequestUnknownLocationFault_Exception(
        @Mocked final TransporterService service,
        @Mocked final P port,
        @Mocked final UDDINaming uddi,
        @Mocked final Map<String,Object> requestCont) // P extends the above types
        throws Exception {
		// declarations
		final JobView j1 = new JobView();
		j1.setJobPrice(2);
		j1.setJobOrigin("Lisboa");
		j1.setJobDestination("Porto");
		j1.setJobIdentifier("2/0");
		j1.setJobState(null);		
		j1.setCompanyName("T1");
		/* 
		 * test to check if transport doesnt exist
		 * gives wrong input of non-existing origin/destiny
		 * */
		// done
		new Expectations() {{
            new TransporterService(); minTimes=0;
            service.getTransporterPort(); result = port;minTimes=0;
            port.getRequestContext(); result = requestCont;minTimes=0;
            requestCont.put(anyString, anyString); minTimes=0;
            uddi.list(anyString); result="T1";minTimes=0;
            port.requestJob("Lisboa", "Porto", anyInt); minTimes=0; 
            result = j1;
            port.requestJob("China", "france", anyInt); minTimes=0;
            port.decideJob(anyString, anyBoolean); minTimes=0;
		}};
		
	      //TESTS
	      BrokerPort bp = new BrokerPort("placeholderURL");
	      assertNotNull("null broker port",bp);
	      try {
	    	  bp.requestTransport("Lisboa", "Porto", 100);
	      } catch (UnknownLocationFault_Exception e) {
	    	  fail("caught wrong except available transport");
	      }
	      
	      try {
	    	  bp.requestTransport("China", "france", 100);
	    	  fail("unknown location didnt throw exception");
	      } catch (UnknownLocationFault_Exception e) {
	    	  assertEquals("Localização errada", e.getMessage());
	      } 
	      
	      new Verifications() {{
	    	  // Verifies that zero or one invocations occurred, with the specified argument value:
	      }};
	     
	}
	@Test
	public  <P extends TransporterPortType & BindingProvider>
	void testUDDIServerErrorSimulation(
	        @Mocked final UDDINaming uddi)
        throws Exception {
		/*
		 * tests uddi exceptions
		 * */
		 new Expectations() {{
	            new TransporterService();
	            uddi.list(anyString); result=  new JAXRException(); minTimes=0;// check if needs result
	            uddi.lookup(anyString); result=  new JAXRException() ; minTimes=0;
	      }};
	      
	      BrokerPort bp = new BrokerPort("placeholderURL");
	      assertNotNull("null broker port",bp);
	      
	      try {
	      	bp.requestTransport("Lisboa", "Porto", 10);
	      	fail("Nao apanhou excepcao");
	      }catch(UnavailableTransportFault_Exception e) {
	    	  assertEquals("Nao existe um transporter disponivel", e.getMessage());
	      }
	      
	}
	
	@Test
	public <P extends TransporterPortType & BindingProvider>
	void testRequestnsportPriceFault_Exception(
        @Mocked final TransporterService service,
        @Mocked final P port,
        @Mocked final UDDINaming uddi,
        @Mocked final Map<String,Object> requestCont) // P extends the above types
        throws Exception {
		// declarations
		final JobView j1 = new JobView();
		j1.setJobPrice(2);
		j1.setCompanyName("T1");
		j1.setJobDestination("Porto");
		j1.setJobIdentifier("2/0");
		j1.setJobOrigin("Lisboa");
		j1.setJobState(null);
		
		 new Expectations() {{
	            new TransporterService();
	            service.getTransporterPort(); result = port;minTimes=0;
	            port.getRequestContext(); result = requestCont;minTimes=0;
	            requestCont.put(anyString, anyString); minTimes=0;
	            uddi.list(anyString); result="T1"; minTimes=0;
	            port.requestJob("Lisboa", "Porto", anyInt); minTimes=0; 
	            result = j1;
	            port.decideJob(anyString, anyBoolean); minTimes=0;
	      }};
	      //TESTS
	      BrokerPort bp = new BrokerPort("placeholderURL");
	      assertNotNull("null broker port",bp);

	      try {
	    	  bp.requestTransport("Lisboa", "Porto", 100);
	      } catch (UnavailableTransportPriceFault_Exception e) {
	    	  fail("fail exception unavailableTransportPriceFault was thrown");
	      }
	      try {
	    	  bp.requestTransport("Lisboa", "Porto", 1);
	    	  fail("fail exception unavailableTransportPriceFault not thrown");
	      } catch (UnavailableTransportPriceFault_Exception e) {
	    	  assertEquals("Não existe uma oferta com um preço menor", e.getMessage());
	      }
	      
	      new Verifications() {{
	    	  // Verifies that zero or one invocations occurred, with the specified argument value:
				uddi.list(anyString); minTimes=1;
			    port.decideJob(anyString, anyBoolean); minTimes=1;
			    port.requestJob(anyString, anyString, anyInt); minTimes=2;
	      }};
	     
	}
	@Test
	public <P extends TransporterPortType & BindingProvider>
	void testRequestUnavailableTransportFault_Exception(
        @Mocked final TransporterService service,
        @Mocked final P port,
        @Mocked final UDDINaming uddi,
        @Mocked final Map<String,Object> requestCont) // P extends the above types
        throws Exception {
		final JobView j1 = new JobView();
		j1.setJobPrice(2);
		j1.setCompanyName("T1");
		j1.setJobDestination("Porto");
		j1.setJobIdentifier("1/0");
		j1.setJobOrigin("Lisboa");
		j1.setJobState(null);
		
		/* 
		 * this exception is caught when there is no transporter with an available 
		 * transport
		 */
		
		 new Expectations() {{
			 new TransporterService(); minTimes=0;
            service.getTransporterPort(); result = port;  minTimes=0;
            port.getRequestContext(); result = requestCont;  minTimes=0;
            requestCont.put(anyString, anyString); minTimes=0;
            uddi.list(anyString); minTimes=0; 
            result="T1"; 
            port.requestJob("Lisboa", "Porto", anyInt); minTimes=0; 
            result=j1;
            port.requestJob("Lisboa", "Braga", anyInt); minTimes=0;
            result=null;//simula que Lisboa Braga nao existe 
            port.decideJob(anyString, anyBoolean); minTimes=0;
	      }};
	      //TESTS
	     
	      BrokerPort bp = new BrokerPort("placeholderURL");
	      assertNotNull("null in broker port", bp);
	      try {
	    	  bp.requestTransport("Lisboa", "Porto", 100);
	      } catch (UnavailableTransportFault_Exception e) {
	    	  fail("caught wrong exception transp fault");
	      }
	      
	      try {
	    	  bp.requestTransport("Lisboa", "Braga", 100);
	    	  fail("request transport unexisting");
	      } catch (UnavailableTransportFault_Exception e) {
	    	  assertEquals("Nao existe um transporter disponivel", e.getMessage());
	      }
	      
	      new Verifications() {{
	    	  // Verifies that zero or one invocations occurred, with the specified argument value:
				uddi.list(anyString); minTimes=1;
			    port.decideJob(anyString, anyBoolean); minTimes=1;
			    port.requestJob(anyString, anyString, anyInt); minTimes=1;
	      }};
	      
	}
	@Test
	public <P extends TransporterPortType & BindingProvider>
	void testRequestTransportFullCheck(
	        @Mocked final TransporterService service,
	        @Mocked final P port,
	        @Mocked final UDDINaming uddi,
	        @Mocked final Map<String,Object> requestCont) 
        throws Exception {
		// declarations
		JobView j1 = new JobView();
		j1.setJobPrice(2);
		j1.setCompanyName("T1");
		j1.setJobDestination("Porto");
		j1.setJobIdentifier("1/0");
		j1.setJobOrigin("Lisboa");
		j1.setJobState(null);
		JobView j2 = new JobView();
		j2.setJobPrice(3);
		j2.setCompanyName("T1");
		j2.setJobDestination("Porto");
		j2.setJobIdentifier("1/1");
		j2.setJobOrigin("Lisboa");
		j2.setJobState(null);
		
		 new Expectations() {{
			 new TransporterService(); minTimes=0;
	            service.getTransporterPort(); result = port;  minTimes=0;
	            port.getRequestContext(); result = requestCont;  minTimes=0;
	            requestCont.put(anyString, anyString); minTimes=0;
	            uddi.list(anyString); minTimes=0; 
	            result="T1"; 
	            port.requestJob("Lisboa", "Porto", anyInt); minTimes=0; 
	            result=j1;
	            result=j1;
	            result=j1;
	            port.decideJob(anyString, anyBoolean); minTimes=0;
	      }};
	      //TESTS
	      BrokerPort bp = new BrokerPort("placeholderURL");
	      assertNotNull("bp Null",bp);
	      String jV1 = bp.requestTransport("Lisboa", "Porto", 100);
	      String jV2 = bp.requestTransport("Lisboa", "Porto", 3);
	      String jV3 = bp.requestTransport("Lisboa", "Porto", 2);
	      assertEquals("wrong jV1","1/0",jV1);
	      assertEquals("wrong jV2","1/0",jV2);
	      assertEquals("wrong jV3","1/0",jV3);
		      
	      
	      new Verifications() {{
	    	  // Verifies that zero or one invocations occurred, with the specified argument value:
	      }};
	      
	}

	
	@Test 
	public <P extends TransporterPortType & BindingProvider>
	void testViewFullCheck(
	        @Mocked final TransporterService service,
	        @Mocked final P port,
	        @Mocked final UDDINaming uddi,
	        @Mocked final Map<String,Object> requestCont) // P extends the above types
	        throws Exception {
			/*
			 * simple full check on viewTransport
			 * with exception handling
			 * */
			JobView j1 = new JobView(); // adds to mock return jobStatus
			j1.setJobPrice(2);
			j1.setCompanyName("T1");
			j1.setJobDestination("Porto");
			j1.setJobIdentifier("1/0");
			j1.setJobOrigin("Lisboa");
			j1.setJobState(JobStateView.ACCEPTED);
			TransportView T1 = new TransportView(); // adds to list to search
			T1.setPrice(2);
			T1.setTransporterCompany("T1");
			T1.setDestination("Porto");
			T1.setId("1/0");
			T1.setOrigin("Lisboa");
			T1.setState(TransportStateView.BOOKED);
			TransportView T2 = new TransportView(); // adds to list to search
			T2.setPrice(2);
			T2.setTransporterCompany("T1");
			T2.setDestination("Porto");
			T2.setId("1/1");
			T2.setOrigin("Lisboa");
			T2.setState(TransportStateView.FAILED);
			
			new Expectations() {{
				new TransporterService(); minTimes=0;
	            service.getTransporterPort(); result = port;  minTimes=0;
	            port.getRequestContext(); result = requestCont;  minTimes=0;
	            requestCont.put(anyString, anyString); minTimes=0;
	            uddi.lookup(anyString); minTimes=0; 
	            result="placeholder";
	            port.jobStatus(anyString); 
	            result=j1;
		      }};
		      //TESTS
		      BrokerPort bp = new BrokerPort("placeholderURL");
		      assertNotNull("null bp", bp);
		      TransportView t1;
		      bp.listTransports().add(T1);
		      bp.listTransports().add(T2);

		      try {
		    	  t1 = bp.viewTransport("1/0");
		    	  assertNotNull("null state", t1.getState());
		    	  assertEquals("wrong state",TransportStateView.BOOKED.value(),t1.getState().value());
		    	  assertSame("T1 t1 not same ref ", T1,t1);
		      } catch (UnknownTransportFault_Exception e) {
		    	  fail("shouldn't have caught exception");
		      }
		      
		      try { // T2 state FAILED
		    	  t1 = bp.viewTransport("1/1"); // failed case
		    	  assertNotNull("null state", t1.getState());
		    	  assertEquals("wrong state",TransportStateView.FAILED.value(),t1.getState().value());
		    	  assertSame("T2 t1 not same ref ", T2,t1);
		      } catch (UnknownTransportFault_Exception e) {
		    	 fail("shouldn't have caught exception");
		      }
		      
		      
		      try {
		    	  bp.viewTransport(""); // no id sends exception
		    	  fail("exception not caught");
		      } catch (UnknownTransportFault_Exception e) {
		    	  assertEquals("wrong exception caught","O contrato de transporte não existe", e.getMessage());
		      }
		      
		      new Verifications() {{	            
		      }};
	}
	


}