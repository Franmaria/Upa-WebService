package pt.upa.broker.ws.it;

import org.junit.*;

import pt.upa.broker.ws.InvalidPriceFault_Exception;
import pt.upa.broker.ws.TransportView;
import pt.upa.broker.ws.UnavailableTransportFault_Exception;
import pt.upa.broker.ws.UnavailableTransportPriceFault_Exception;
import pt.upa.broker.ws.UnknownLocationFault_Exception;
import pt.upa.broker.ws.UnknownTransportFault_Exception;
import pt.upa.broker.ws.cli.BrokerClient;


import static org.junit.Assert.*;

import java.util.List;


/**
 *  Integration Test example
 *  
 *  Invoked by Maven in the "verify" life-cycle phase
 *  Should invoke "live" remote servers 
 */
public class BrokerIT {

    // static members
	private static String uddiURL = "http://localhost:9090";
	private static String request1, request2, request3,request4,request5,request6;
	private static BrokerClient bp;
	private static TransportView t1,t2,t3,t4,t5;
    // one-time initialization and clean-up


	
    @BeforeClass
    public static void oneTimeSetUp() {
    	request1="";
    	request2="";
    	request3="";
    	request4="";
    	request5="";
    	request6="";
    	
  
		bp = new BrokerClient(uddiURL);
		

    	bp.clearTransports();
    	
    }
    
    @AfterClass
    public static void oneTimeTearDown() {
    	bp.clearTransports();
    }

    @Before
    public void setUp() {
    	try {
			request1 = bp.requestTransport("Lisboa", "Porto", 40); // centro norte menor
			request2 = bp.requestTransport("Lisboa", "Coimbra", 10); // centro centro menor
			request4 = bp.requestTransport("Portalegre", "Beja", 39); //sul sul menor
					
		} catch (InvalidPriceFault_Exception e) {
			fail("caught invalidprice" + " " +request1 +" " +request2+" " +request4+"");
			e.printStackTrace();
		} catch (UnavailableTransportFault_Exception e) {
			fail("caught unavailable transport fault" + " " +request1 +" " +request2+" " +request4+"");
			e.printStackTrace();
		} catch (UnavailableTransportPriceFault_Exception e) {
			fail("caught unavailable transport price fault" + " " +request1 +" " +request2+" " +request4+" ");
			e.printStackTrace();
		} catch (UnknownLocationFault_Exception e) {
			fail("caught unknown location fault" + " " +request1 +" " +request2+" "+request4);
			e.printStackTrace();
		}
    	
    }

    @After
    public void tearDown() {
    	t1=null;
    	t2=null;
    	t3=null;
    	t4=null;
    	
    	bp.clearTransports();
    }

    

    // tests
    @Test
    public void testPing() {
    	String a = bp.ping("thisBP");
    	assertEquals("erro ping BP", "thisBP", a);
    }
    
    @Test
    public void test1() {
    	/* criar um broker client com duas transportadores
    	 * 1. fazer operacoes basicas em ambas as transportadoras
    	 * */
    	// setup
    	
    	
    	// request
    	try {
			t1 = bp.viewTransport(request1);
	    	t2 = bp.viewTransport(request2);
	    	t4 = bp.viewTransport(request4);
	    	
		} catch (UnknownTransportFault_Exception e) {
			fail("caught UnknownTransportFault_Exception in viewTransport requests 1 and 2");
			e.printStackTrace();
		}
    	assertEquals("wrong Transporter 1", "UpaTransporter2",t1.getTransporterCompany());
    	assertEquals("wrong price1", 39,t1.getPrice().intValue());
    	
    	// impossivel saber transportadora
    	assertEquals("wrong price2", 9,t2.getPrice().intValue());
    	
    	assertEquals("wrong Transporter 4", "UpaTransporter1",t4.getTransporterCompany());
    	assertEquals("wrong price2", 38,t4.getPrice().intValue());
    }
    
    @Test
    public void test2() {
    	
    	
    	try {
        	t1 = bp.viewTransport(request1); // T2 Lisboa - Porto
			t2 = bp.viewTransport(request2); // T1 
		} catch (UnknownTransportFault_Exception e1) {
			fail("unknown Transport fault");
			e1.printStackTrace();
		} // T2 Lisboa - Beja
    	
    	try {
    		Thread.sleep(6500);
		} catch (InterruptedException ie) { // da excecao porque e' criado um novo timer? verificar
			System.out.println(ie);
		}
    	 
    	
    	try {
			t3 = bp.viewTransport(request1);
	    	t4 = bp.viewTransport(request2);
	    	if(t1.getState().value().equals(t3.getState().value())) {
	    		fail("wrong state Transporter 1");
	    	}
	    	if(t2.getState().value().equals(t4.getState().value())) {
	    		fail("wrong state Transporter 2");
	    	}

		} catch (UnknownTransportFault_Exception e) {
			fail("unknown transport fault");
			e.printStackTrace();
		}
    	
    	
    	
    }
    
    @Test
    public void test3() {
    	/*
    	 * teste serve para verificar excecoes do viewTransport
    	 * 
    	 * */
    	// 1/0 Lisboa Beja
    	try { // destino indispunivel
        	t1 = bp.viewTransport(request1); // testing view Transport exception
		} catch (UnknownTransportFault_Exception e1) {
			fail("caught wrong exception");
		}
    	
    	try { // destino indispunivel
        	t1 = bp.viewTransport("id"); // testing view Transport exception
        	fail("should have caught exception");
		} catch (UnknownTransportFault_Exception e1) {
			assertEquals("wrong message", "O contrato de transporte não existe", e1.getMessage());
		}

    }
    @Test
    public void test4() {
    	// verifica excecoes do broker requestTransport de norte para sul transporte indispunivel
    	
		try {
			bp.requestTransport("Lisboa", "Porto", 10);
		} catch (InvalidPriceFault_Exception | UnavailableTransportFault_Exception
				| UnavailableTransportPriceFault_Exception | UnknownLocationFault_Exception e) {
			e.printStackTrace();
			fail("caught exception");
			
		} 
	
		try {
			bp.requestTransport("Porto", "Beja", 10);
			fail("uncaught exception");
		} catch (InvalidPriceFault_Exception | UnavailableTransportFault_Exception
				| UnavailableTransportPriceFault_Exception | UnknownLocationFault_Exception e) {
			assertEquals("wrong exception", "Nao existe um transporter disponivel", e.getMessage());
		}
    }
    
    @Test
    public void test5() {
    	// verifica excecoes do broker Port request preco <0
    	
		try {
			bp.requestTransport("Lisboa", "Porto", 10);
		} catch (InvalidPriceFault_Exception | UnavailableTransportFault_Exception
				| UnavailableTransportPriceFault_Exception | UnknownLocationFault_Exception e) {
			e.printStackTrace();
			fail("caught exception");
			
		} 
	
		try {
			bp.requestTransport("Lisboa", "Porto", -1);
			fail("should catch exception ");
		} catch (InvalidPriceFault_Exception | UnavailableTransportFault_Exception
				| UnavailableTransportPriceFault_Exception | UnknownLocationFault_Exception e) {
			assertEquals("wrong exception", "Preço incorreto", e.getMessage());
		}
    }
    @Test
    public void test6() {
    	// verifica excecoes requestTransport local n existe
    	
		try {
			bp.requestTransport("Lisboa", "Porto", 10);
		} catch (InvalidPriceFault_Exception | UnavailableTransportFault_Exception
				| UnavailableTransportPriceFault_Exception | UnknownLocationFault_Exception e) {
			e.printStackTrace();
			fail("caught exception");
			
		} 
	
		try {
			bp.requestTransport("china", "Porto", 10);
			fail("should catch exception ");
		} catch (InvalidPriceFault_Exception | UnavailableTransportFault_Exception
				| UnavailableTransportPriceFault_Exception | UnknownLocationFault_Exception e) {
			assertEquals("wrong exception", "Localização errada", e.getMessage());
		}
    }
    
    @Test
    public void test7() {
    	// verifica excecoes do broker Port
    	
		try {
			bp.requestTransport("Lisboa", "Porto", 10);
		} catch (InvalidPriceFault_Exception | UnavailableTransportFault_Exception
				| UnavailableTransportPriceFault_Exception | UnknownLocationFault_Exception e) {
			e.printStackTrace();
			fail("caught exception");
			
		} 
	
		try {
			bp.requestTransport("Lisboa", "Porto", 10000);
		} catch (InvalidPriceFault_Exception | UnavailableTransportFault_Exception
				| UnavailableTransportPriceFault_Exception | UnknownLocationFault_Exception e) {
			assertEquals("excepcao diferente" ,"Nao existe um transporter disponivel",e.getMessage());
		}
    }
    @Test
    public void test8() {
    	// verifica excecoes do broker Port request UnavailableTransportPriceFault_Exception 
    	
		try {
			bp.requestTransport("Lisboa", "Porto", 10);
		} catch (InvalidPriceFault_Exception | UnavailableTransportFault_Exception
				| UnavailableTransportPriceFault_Exception | UnknownLocationFault_Exception e) {
			e.printStackTrace();
			fail("caught exception");
		}
	
		try {
			bp.requestTransport("Lisboa", "Porto", 11);
		} catch (InvalidPriceFault_Exception | UnavailableTransportFault_Exception
				| UnavailableTransportPriceFault_Exception | UnknownLocationFault_Exception e) {
			assertEquals("wrong error caught","Não existe uma oferta com um preço menor", e.getMessage());
		}
    }
    @Test
    public void testReplicacao() {
    	// verifica se a replicacao esta a funcionar bem 
    	bp.clearTransports(); //no setup pedem-se duas viagens, ao fazer clear tanto a replica como o servidor principal tem de ter 0
    	
    	String job1 = null;
    	String job2 = null; 
    	try {
    		job1 = bp.requestTransport("Lisboa","Coimbra" , 20);
			job2 = bp.requestTransport("Lisboa", "Porto", 20);
    	} catch (Exception e) {
			fail("caught exception");
		}
    	
    	try { // espera que os trabalhos mudem de estado nos trasporters 
			 Thread.sleep(6550);
		}catch (InterruptedException e) {
			System.out.printf("Caught exception: %s%n", e);
			e.printStackTrace();
		}
    	
    	TransportView state = null;
    	
    	try {
			state = bp.viewTransport(job1); //o estado do job1 vai mudar 
		} catch (UnknownTransportFault_Exception e) {
			fail("caught exception");
		}
    	
    	
    	bp.ping("kill"); // esta operacao mata o servidor principal
    	
    	List<TransportView> list = null; 
    	try{	
    		
    		list = bp.listTransports();
    	
    	}catch(Exception e){
    		fail("caught exception"); // se apanhar excepcao a troca para a replica nao foi bem feita
    	}
    	int size = list.size();
  
    	if (size != 2){
			fail("update dos transporters ou do clean nao foi feito");
		}
    	
    	if(!(list.get(0).getId().equals(job1) && list.get(1).getId().equals(job2))){
    		fail("jobs diferentes dos do main");
    	}
		
    	if(!state.getState().equals(list.get(0).getState())) {
    		fail("o estado nao mudou na replica");// teste para ver se quando o estado muda no principal na replica tambem muda 
    	} 
    	
    	setUp();
    	test1();
    	tearDown();
    	
    	setUp();
    	test2();
    	tearDown();
    	
    	setUp();
    	test3();
    	tearDown();
    	
    	setUp();
    	test4();
    	tearDown();
    	
    	setUp();
    	test5();
    	tearDown();
    	
    	setUp();
    	test6();
    	tearDown();
    	
    	setUp();
    	test7();
    	tearDown();
    	
    	setUp();
    	test8();
    	tearDown();
    }
}