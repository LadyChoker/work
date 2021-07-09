package es.tid.pce.client;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.tid.pce.pcepsession.PCEPSessionsInformation;

public class TestClient {

	private static UserInterface ui;
	private static PCCPCEPSession PCEsession;
	public static final Logger Log =LoggerFactory.getLogger("PCCClient");
	
	public static void main(String[] args) {
		//FileHandler fh;
		//FileHandler fh2;
		try {
			//fh=new FileHandler("PCCClient.log");
			//fh2=new FileHandler("PCEPClientParser.log");
			//fh.setFormatter(new SimpleFormatter());
			//Log.addHandler(fh);
			//Log.setLevel(Level.ALL);
			Logger log2=LoggerFactory.getLogger("PCEPParser");
			//log2.addHandler(fh2);
			//log2.setLevel(Level.ALL);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(1);
		}
		
		if (args.length < 2) {
			Log.info("Usage: ClientTester <host> <port>");
			return;
		}
		
			
		String ip;
		int port;

		
		ip = args[0];
		port = Integer.valueOf(args[1]).intValue();
		PCEPSessionsInformation pcepSessionManager=new PCEPSessionsInformation();
		PCEsession = new PCCPCEPSession(ip, port,false,pcepSessionManager);
		PCEsession.start();
		ui = new UserInterface(PCEsession);
		ui.start();
		
	
	}	
	
}
