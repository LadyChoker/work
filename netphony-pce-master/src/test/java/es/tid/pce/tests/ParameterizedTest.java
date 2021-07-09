package es.tid.pce.tests;

import static org.junit.Assert.assertTrue;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

import es.tid.pce.client.QuickClientObj;
import es.tid.pce.pcep.constructs.Request;
import es.tid.pce.pcep.messages.PCEPMessage;
import es.tid.pce.pcep.messages.PCEPRequest;
import es.tid.pce.pcep.messages.PCEPResponse;
import es.tid.pce.pcep.objects.Bandwidth;
import es.tid.pce.pcep.objects.ExplicitRouteObject;
import es.tid.pce.server.DomainPCEServer;
import es.tid.rsvp.objects.subobjects.EROSubobject;
import es.tid.rsvp.objects.subobjects.IPv4prefixEROSubobject;
import es.tid.rsvp.objects.subobjects.UnnumberIfIDEROSubobject;

@RunWith(org.junit.runners.Parameterized.class)
public class ParameterizedTest {
	
	private String fileConf;
	private String[] msgSend;
	private String[] checkRes;

	//创建大小为5的线程池
	ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(5) ;

	public static final Logger log =LoggerFactory.getLogger("ParameterizedTest");

	//@Parameters
		@Parameters()
	    public static Collection configs() {
	    	Object[][] objects={
//	    			{"src/test/resources/PCEServerConfiguration_SSON_Line.xml", "localhost 4189 192.168.1.2 192.168.1.5 -g -of 1002 -rgbw 2", "-nopath"},
	    			{"src/test/resources/PCEServerConfiguration_SSON_Line.xml", "localhost 4189 192.168.1.1 192.168.1.2 -g -of 1002 -rgbw 2", "-ero 192.168.1.1:1,192.168.1.2/32"},
//	    			{"src/test/resources/PCEServerConfiguration_SSON_Line.xml", "localhost 4189 192.168.1.1 192.168.1.3 -g -rgbw 2", "-ero 192.168.1.1:1,192.168.1.2:2,192.168.1.3/32"},
//    				{"src/test/resources/PCEServerConfiguration_SSON_Line.xml", "localhost 4189 192.168.1.3 192.168.1.1 -rgbw 2", "-ero 192.168.1.3:2,192.168.1.2:1,192.168.1.1/32"},
//network1 run success
//	    			{"src/test/resources/PCEServerConfiguration_SSON_Triangle.xml", "localhost 4189 172.16.101.102 172.16.102.101 -rgbw 2", "-ero 192.168.1.3:2,192.168.1.2:1,192.168.1.1/32"},
//network2 my topo
//					{"src/test/resources/MultipleDomainPCEConfiguration_SSON.xml", "localhost 4189 10.0.1.2 10.0.2.5 -g -rgbw 2", ""},
//					{"src/test/resources/MultipleDomainPCEConfiguration_SSON.xml", "localhost 4189 10.0.1.1 10.0.3.1 -g -rgbw 200", ""},
//	    			{"target/PCEServerConfiguration_BGPLS.xml", "localhost 4189 192.168.1.1 192.168.1.3 -g -rgbw 2", "-nopath"},
//test
// 	    			{"src/test/resources/PCEServerConfiguration_SSON_Triangle.xml", "localhost 4189 192.168.1.2 192.168.1.3 -of 1002", "-ero 192.168.1.3:2,192.168.1.2/32"},
//	    			{"src/test/resources/PCEServerConfiguration_SSON_Triangle.xml", "localhost 4189 192.168.1.4 192.168.1.2 -rgbw 2", "-nopath"},
	    	};
			return Arrays.asList(objects);
	    }
		
	public ParameterizedTest(String fileConf, String msgSend, String checkRes) {
		this.fileConf = fileConf;
		this.msgSend = msgSend.split(" ");
		this.checkRes = checkRes.split(" ");

		/*System.out.println("fileConf = " + fileConf);
		System.out.println("msgSend = " + msgSend);
		System.out.println("checkRes = " + checkRes);*/

	}

	@Test
	public void myTest (){
		log.info("1、创建PCE服务器对象");
		DomainPCEServer pceserver = new DomainPCEServer();
		log.info("2、读取PCE服务器配置文件并初始化参数");
		//this.confFile=confFile;
		//读取配置文件并初始化参数
		pceserver.configure(this.fileConf);
		log.info("3、0延迟执行一个PCE服务器");
		//0延迟执行一个command/**/
		pceserver.run();
		//executor.execute(pceserver);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

/*
		//Run PCC QuickClient
		System.out.println("Run PCC QuickClient!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		try{

			if(msgSend.length < 4){
				assertTrue("Invalid test arguments number (>4)", false);
			}
			//localhost 4189 192.168.1.2 192.168.1.5 -g -of 1002 -rgbw 2
			CommandLine optReq = QuickClientObj.getLineOptions(this.msgSend);



			//Generalized end points
			System.out.println("optReq.getOptionValue(g) = " + optReq.getOptionValue("g"));
			//Explicit Route Object
			System.out.println("optReq.getOptionValue(ero) = " + optReq.getOptionValue("ero"));
			//set of value = 1002
			System.out.println("optReq.getOptionValue(of) = " + optReq.getOptionValue("of"));
			//set rgbw value = 2
			System.out.println("optReq.getOptionValue(rgbw) = " + optReq.getOptionValue("rgbw"));
			//local interface
			System.out.println("optReq.getOptionValue(li) = " + optReq.getOptionValue("li"));





			Logger log =LoggerFactory.getLogger("PCCClient");
			// log, localhost, 4189,
			System.out.println("msgSend[0] = " + msgSend[0]);
			System.out.println("msgSend[1] = " + msgSend[1]);
			QuickClientObj qcObj = new QuickClientObj(log, msgSend[0], Integer.valueOf(msgSend[1]).intValue());
			qcObj.start();
			//src 192.168.1.2 dest 192.168.1.3
			Request req = qcObj.createReqMessage(msgSend[2],msgSend[3], optReq);
			PCEPRequest p_r = new PCEPRequest();
			p_r.addRequest(req);
			LinkedList<PCEPMessage> messageList=new LinkedList<PCEPMessage>();
			PCEPResponse res = qcObj.sendReqMessage(p_r, messageList);
			this.parseAndCheckRes(res);

		}catch(ParseException ex){
			assertTrue("Invalid test arguments", false);
		}

		System.out.println("Finalizando PCE Server");

		pceserver.stopServer();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Number of active threads: "+executor.getActiveCount());
*/


	}
	
	@Test
    public void test (){
		DomainPCEServer pceserver = new DomainPCEServer();
		//this.confFile=confFile;
		//读取配置文件并初始化参数
		pceserver.configure(this.fileConf);
		//0延迟执行一个command
		//pceserver.run()
		executor.execute(pceserver);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//Run PCC QuickClient
		try{
			
			if(msgSend.length < 4){
				assertTrue("Invalid test arguments number (>4)", false);
			}
			//localhost 4189 192.168.1.2 192.168.1.5 -g -of 1002 -rgbw 2
			//发送计算请求信息
    		CommandLine optReq = QuickClientObj.getLineOptions(this.msgSend);

/*

			//Generalized end points
			System.out.println("optReq.getOptionValue(g) = " + optReq.getOptionValue("g"));
			//Explicit Route Object
			System.out.println("optReq.getOptionValue(ero) = " + optReq.getOptionValue("ero"));
			//set of value = 1002
			System.out.println("optReq.getOptionValue(of) = " + optReq.getOptionValue("of"));
			//set rgbw value = 2
			System.out.println("optReq.getOptionValue(rgbw) = " + optReq.getOptionValue("rgbw"));
			//local interface
			System.out.println("optReq.getOptionValue(li) = " + optReq.getOptionValue("li"));
*/



    		Logger log =LoggerFactory.getLogger("PCCClient");
    		// log, localhost, 4189, 快速客户端连接到服务器
			QuickClientObj qcObj = new QuickClientObj(log, msgSend[0], Integer.valueOf(msgSend[1]).intValue());
			qcObj.start();
			//src 192.168.1.2 dest 192.168.1.5

			//建立请求对象
			Request req = qcObj.createReqMessage(msgSend[2],msgSend[3], optReq);

			PCEPRequest p_r = new PCEPRequest();
			p_r.addRequest(req);
			LinkedList<PCEPMessage> messageList=new LinkedList<PCEPMessage>();
			PCEPResponse res = qcObj.sendReqMessage(p_r, messageList);
//			this.parseAndCheckRes(res);
//			ExplicitRouteObject eroRes = res.getResponseList().getFirst().getPath(0).geteRO();
//			log.info("ERO en respuesta: "+eroRes);
//			System.out.println("ERO en respuesta: "+eroRes);

    	}catch(ParseException ex){
    		assertTrue("Invalid test arguments", false);
    	}
		
		System.out.println("Finalizando PCE Server");
		
		pceserver.stopServer();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Number of active threads: "+executor.getActiveCount());
	
		
	}
	
	
	



	private void parseAndCheckRes(PCEPResponse res) {
		
		
		Option noPathOpt= new Option("nopath", "NoPath object present");
		Option eroOpt= Option.builder("ero").argName("EROstring").hasArg().desc(  "Explicit Route Object present" ).build( );
		Option bwOpt= Option.builder( "bandwidth" ).argName("BWstring").hasArg().desc(  "Bandwidth Object present" ).build(  );
		Options options = new Options();
		options.addOption(eroOpt);
		options.addOption(bwOpt);
		options.addOption(noPathOpt);
		CommandLineParser parser = new DefaultParser();
		
		try {
			CommandLine line=parser.parse( options, this.checkRes );
			if(line.hasOption("nopath")){
				assertTrue("NOPATH object no present",res.getResponse(0).getNoPath()!=null);
			}else{
				assertTrue("NOPATH object present",res.getResponse(0).getNoPath()==null);
			}
			if(line.hasOption("bandwidth")){
				//TODO check bandwidth object 
			}
			if(line.hasOption("ero")){
				ExplicitRouteObject ero = new ExplicitRouteObject();
				ExplicitRouteObject eroRes = res.getResponseList().getFirst().getPath(0).geteRO();
				System.out.println("ERO en respuesta: "+eroRes);
				
				String argEro = line.getOptionValue("ero");
				String[] argsEro = new String[]{argEro} ;
				if(argEro.contains(",")){
					argsEro=argEro.split(",");
				}
				try {
					for(String s : argsEro){
						if(s.contains(":")){ //UnnumberIfIDEROSubobject
							String[] part=s.split(":");
							Inet4Address ipHost = (Inet4Address)Inet4Address.getByName(part[0]);
							UnnumberIfIDEROSubobject ipIfSub= new UnnumberIfIDEROSubobject();
							ipIfSub.setRouterID(ipHost);
							ipIfSub.setInterfaceID(Long.parseLong(part[1]));
							ipIfSub.encode();
							ero.addEROSubobject(ipIfSub);
							boolean flagEq=false;
							for (EROSubobject eroSubObj : eroRes.getEROSubobjectList()){
								if(flagEq==false && eroSubObj.equals(ipIfSub))
									flagEq=true;
							}
							assertTrue("ERO subobject IP with IF ID wrong (expected: ["+ipIfSub.toString()+"])", flagEq);
						}else if(s.contains("/")){ //IPv4prefixEROSubobject  
							String[] part=s.split("/");
							IPv4prefixEROSubobject ip4Sub = new IPv4prefixEROSubobject();
							Inet4Address ipHost = (Inet4Address)Inet4Address.getByName(part[0]);
							ip4Sub.setIpv4address(ipHost);
							ip4Sub.setPrefix(Integer.parseInt(part[1]));
							ip4Sub.encode();
							ero.addEROSubobject(ip4Sub);
							boolean flagEq=false;
							for (EROSubobject eroSubObj : eroRes.getEROSubobjectList()){
								if(flagEq==false && eroSubObj.equals(ip4Sub))
									flagEq=true;
							}
							assertTrue("ERO subobject IPv4 wrong (expected: ["+ip4Sub.toString()+"])", flagEq);
						}
					}
					//ero.encode();
					//boolean check = eroRes.equals(ero);
					//assertTrue("ERO wrong ["+ero.toString()+"] VS ["+eroRes.toString()+"]", check);
				} catch (UnknownHostException e) {
					e.printStackTrace();
					assertTrue("Unknow error", false);
				}
			
			}
		} catch (ParseException e) {
			assertTrue("Invalid test response parse arguments", false);
		}
	}
}
