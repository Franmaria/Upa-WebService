package pt.upa.transporter.ws;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;
import static org.junit.Assert.*;

import java.util.Map;

import javax.xml.ws.BindingProvider;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;

public class TransporterPortTest {

		
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// mock UDDI
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
	public  
	void testRequestJobFullCheck()
        throws Exception {
		JobView j1;
		JobView j2;
		JobView j3;
		JobView j4;
		JobView j5;
		/*
		 * para este teste ser valido e' necessario que o valor
		 * dado por um request job, caso n exista nenhum job com o valor
		 * pedido,
		 * incremente e decremente apenas um em vez de ser random,
		 * verificar se e' necessario ser random
		 * */
		// TESTS
		
		// impar - impar desce oferta| par par sobe oferta
		
		TransporterPort transporter1 = new TransporterPort(1); // T impar
		TransporterPort transporter2 = new TransporterPort(2); // T par
		assertNotNull(transporter1);
		assertNotNull(transporter2);
		j1 = transporter1.requestJob("Lisboa", "Setúbal", 49); // price check
		assertEquals("check origin","Lisboa",j1.getJobOrigin());
		assertEquals("check dest","Setúbal",j1.getJobDestination());
		assertEquals("check ID","1/0",j1.getJobIdentifier());
		assertEquals("impar T impar price",48,j1.getJobPrice());  
		j2 = transporter1.requestJob("Lisboa", "Setúbal", 50); // [4 in list] 
		assertEquals("impar T par price",51,j2.getJobPrice());  // checks price
		j3 = transporter2.requestJob("Lisboa", "Lisboa", 49); // [5 in list] 
		assertEquals("par T impar price",50,j3.getJobPrice()); // checks price
		j4 = transporter2.requestJob("Lisboa", "Porto", 50); // [6 in list] 
		assertEquals("par T par price",49,j4.getJobPrice()); // checks price
		j5 = transporter2.requestJob("Lisboa","Porto",5); // test < 10
		assertEquals("impar T price < 10",4,j5.getJobPrice()); // checks price
		j5 = transporter1.requestJob("Lisboa","Setúbal",5); // test < 10
		assertEquals("impar T price < 10",4,j5.getJobPrice()); // checks price
	}
	
	
	@Test
	public  
	void testRequestJobBadPriceFault()
        throws Exception {
		JobView j1;
		
		// TESTS
		TransporterPort transporter = new TransporterPort(1);
		// transporter impar
		assertNotNull(transporter);
		
		
		try {
			// transporter impar
			j1 = transporter.requestJob("Lisboa", "Setúbal", 100); // impar T par price
			// tests price must be 100
			assertEquals("impar T impar price", 101, j1.getJobPrice());
		} catch(BadPriceFault_Exception e) {
			fail("shouldnt have caugh badprice fault in usual check");
		}	
		
		try {
			j1 = transporter.requestJob("Lisboa", "Setúbal", 0); // impar T par price
			// tests price must be 100
			assertEquals("impar T par price(0)", 0, j1.getJobPrice());
		} catch(BadPriceFault_Exception e) {
			fail("shouldnt have caugh badprice fault in 0 input check");
		}
		
		try {
			j1 = transporter.requestJob("Lisboa", "Setúbal", -1);
			// this causes exception
			fail("should have caught badpricefault");
		} catch(BadPriceFault_Exception e) {
			// if it gets exception in first decideJob fails
			assertEquals("Preco incorreto", e.getMessage());	
		}
		// verifications -> verifies expectations
	}
	
	@Test
	public  
	void testRequestJobOriginDestUnknownToTransporter()
        throws Exception {
		JobView j1;
		/*
		 * Este teste verifica se regioes que existem, nao devolvem excecao
		 * e que se existirem mas nao pertencam ao ambito de uma dada 
		 * transportadora retornem null
		 * 
		 * */

		
		// TESTS
		TransporterPort transporter1 = new TransporterPort(1);
		// impar -centro e sul
		TransporterPort transporter2 = new TransporterPort(2);
		// par - centro e norte
		assertNotNull(transporter1);
		assertNotNull(transporter2);
		
		
		try {
			// checks transporter impar
			j1 = transporter1.requestJob("Évora", "Setúbal", 100);
			// tests price must be 100
			assertNotNull("T impar null error",j1); 
			// deve retornar != null regioes pertencem centro e sul
			 
		} catch(BadLocationFault_Exception e) {
			fail("badLocationFault in T impar not null");
		}
		
		try {
			// checks transporter impar
			j1 = transporter1.requestJob("Lisboa", "Porto", 100);
			// tests price must be 100
			assertNull("T impar null error",j1);
			// deve retornar != null regioes pertencem centro e norte
			 
		} catch(BadLocationFault_Exception e) {
			fail("badLocationFault in T impar null");
		}
		
		try {
			// checks transporter par
			j1 = transporter2.requestJob("Porto", "Lisboa", 100);
			assertNotNull("T par not null error",j1);
			// this causes exception
		} catch(BadLocationFault_Exception e) {
			fail("badLocationFault in T par not null");
		}
		
		try {
			// checks transporter par
			j1 = transporter2.requestJob("Setúbal", "Lisboa", 100);
			assertNull("T par null error",j1);
			// this causes exception
		} catch(BadLocationFault_Exception e) {
			fail("badLocationFault in T par null");
		}
		
	}
	
	@Test
	public  
	void testRequestJobBadLocationFault()
        throws Exception {
		
		JobView j1;
		// TESTS
		TransporterPort transporter = new TransporterPort(1);
		// impar transporter south center
		assertNotNull(transporter);

		try {
			j1 = transporter.requestJob("Lisboa", "Setúbal", 100);
			// center 
			// tests price must be 100
		} catch(BadLocationFault_Exception e) {
			fail("BadLocationFault in valid call Lisboa Setubal");
		}
		
		try {
			j1 = transporter.requestJob("Lisboa", "Porto", 100);
			// center 
			assertNull("impar transporter center-north",j1);
			// tests price must be 100
		} catch(BadLocationFault_Exception e) {
			fail("BadLocationFault in valid call Lisboa Porto, should be Null");
		}
		
		
		try {
			j1 = transporter.requestJob("China", "Franca", 100);
			// this causes exception
			fail("fail in invalid origin-dest should return BadJobLocException");
		} catch(BadLocationFault_Exception e) {
			// if it gets exception in first decideJob fails
			assertEquals("Localizacao nao existe", e.getMessage());
			
		}
		// verifications -> verifies expectations
		
		
		
	}
	
	@Test
	public
	void testDecideJobCorrectStateTransition(@Mocked TransporterPort trans, @Mocked JobView Mj)
        throws Exception {
		// this test is used to check the correct transition of states in decideJob
		// should take a lot of time, to be implemented last
	}

	@Test
	public
	void testDecideJobCheckREJECTEDState()
        throws Exception {
		
		JobView j1;
		
		// TESTS
		TransporterPort transporter = new TransporterPort(1);
		assertNotNull(transporter);
		transporter.addJob(transporter.createJob("T1","Lisboa","1/0","Porto",11,JobStateView.ACCEPTED));
		transporter.addJob(transporter.createJob("T1","Lisboa","1/1","Porto",11,JobStateView.ACCEPTED));
		transporter.addJob(transporter.createJob("T1","Lisboa","1/2","Porto",11,JobStateView.ACCEPTED));

		try {
			j1 = transporter.decideJob("1/0", false);
			assertEquals(JobStateView.REJECTED,j1.getJobState());
		} catch(BadJobFault_Exception e) {
			// if it gets exception in first decideJob fails
			//assertEquals("fabricated1", e.getMessage());
			fail();
		}
		
	}
	
	@Test
	public
	void testDecideJobBadJobFaultException()
        throws Exception {
		

		// TESTS
		TransporterPort transporter = new TransporterPort(1);
		assertNotNull(transporter);
		transporter.addJob(transporter.createJob("T1","Lisboa","1/0","Porto",11,JobStateView.REJECTED));
		transporter.addJob(transporter.createJob("T1","Lisboa","1/1","Porto",11,JobStateView.ACCEPTED));
		transporter.addJob(transporter.createJob("T1","Lisboa","1/2","Porto",11,JobStateView.ACCEPTED));

		try {
			transporter.decideJob("1/0", false); 
		} catch(BadJobFault_Exception e) {
			// if it gets exception in first decideJob fails
			//assertEquals("fabricated1", e.getMessage());
			fail();
		}
		
		try {
			transporter.decideJob("china", false);
			// if it doesn't get exception in second decideJob fails
			fail();
		} catch(BadJobFault_Exception e) {
			assertEquals("badJobFault", e.getMessage());
		}
		
		// verifications -> verifies expectations
		
	}
	
	@Test
	public 
	void testJobStatusCheckSameNameInJobs() {
	// checks if is possible for to JobView's to have same .jobIdentification
	// this test adds only to exact same jobs and calls jobStatus
	// faz sentido existir dois jobs exatamente iguais na mesma lista?
	}

	
	// test jobStatus wrong id input should return null
	// job Status input should be (indexNum|TransporterNum)
	@Test
	public 
	void testJobStatusNullJobsList() { 
			
		JobView jv;
		// TESTS
		TransporterPort transporter = new TransporterPort(5);
		jv = transporter.jobStatus("T1");
		
		// verifications -> verifies expectations or assertions
		
		
		assertNull("correct job status input",jv); // checks if NULL
		// nothing in list must be NULL
		
	}
	@Test
	public 
	void testJobStatus() { 
		// this tests JobStatus to check 
		// test data
		JobView j1;
		JobView j2;

		JobStateView js = JobStateView.ACCEPTED;
		JobView jv = new JobView();
		jv.setCompanyName("T1");
		jv.setJobDestination("Lisboa");
		jv.setJobIdentifier("1/1"); // transporterNum/contratNum->starts at 0
		jv.setJobOrigin("Porto");
		jv.setJobPrice(11);
		jv.setJobState(js);
		
		// TESTS
		TransporterPort transporter = new TransporterPort(1);
		
		transporter.addJob(transporter.createJob("T2","Lisboa","1/0","Porto",12,JobStateView.ACCEPTED));
		transporter.addJob(transporter.createJob("T1","Lisboa","1/1","Porto",11,JobStateView.ACCEPTED));
		j1=transporter.jobStatus("1/1"); // change values and recheck
		j2=transporter.jobStatus("id");
		
		// verifications -> verifies expectations
		
		assertNotNull(transporter); // checks if NULL
		assertNotNull(j1); // checks if NULL
		assertEquals(jv.getCompanyName(),j1.getCompanyName());
		assertEquals(jv.getJobDestination(),j1.getJobDestination());
		assertEquals(jv.getJobIdentifier(),j1.getJobIdentifier());
		assertEquals(jv.getJobOrigin(),j1.getJobOrigin());
		assertEquals(jv.getJobPrice(),j1.getJobPrice());
		assertEquals(jv.getJobState(),j1.getJobState());
		assertNull(j2); // j2 must be null because id is wrong
	} 
}