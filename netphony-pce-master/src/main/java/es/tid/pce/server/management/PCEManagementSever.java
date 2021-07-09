package es.tid.pce.server.management;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.tid.pce.computingEngine.RequestDispatcher;
import es.tid.pce.server.PCEServerParameters;
import es.tid.pce.server.communicationpce.CollaborationPCESessionManager;
import es.tid.pce.server.wson.ReservationManager;
import es.tid.tedb.DomainTEDB;


public class PCEManagementSever extends Thread {
	
	private Logger log;
		
	private RequestDispatcher requestDispatcher;
	
	private DomainTEDB tedb;
	 
	private PCEServerParameters params;
	
	private ReservationManager reservationManager;
	private CollaborationPCESessionManager collaborationPCESessionManager;
	private ServerSocket serverSocket;
	private boolean listening=true;
	public PCEManagementSever(RequestDispatcher requestDispatcher, DomainTEDB tedb, PCEServerParameters params, ReservationManager reservationManager){
		log =LoggerFactory.getLogger("PCEServer");
		this.requestDispatcher=requestDispatcher;
		this.tedb=tedb;
		this.params = params;
		this.reservationManager=reservationManager;
		
	}
	
	public PCEManagementSever(RequestDispatcher requestDispatcher, DomainTEDB tedb, PCEServerParameters params, ReservationManager reservationManager,CollaborationPCESessionManager collaborationPCESessionManager){
		log =LoggerFactory.getLogger("PCEServer");
		this.requestDispatcher=requestDispatcher;
		this.tedb=tedb;
		this.params = params;
		this.reservationManager=reservationManager;
		this.collaborationPCESessionManager= collaborationPCESessionManager;
	}
	
	public void run(){

		try {
	      	  log.info("Listening on port "+params.getPCEManagementPort());	
	          serverSocket = new ServerSocket(params.getPCEManagementPort(), 0,(Inet4Address) InetAddress.getByName(params.getLocalPceAddress()));
		  }
		catch (Exception e){
			 log.error("Could not listen management on port "+params.getPCEManagementPort());
			e.printStackTrace();
			return;
		}

		try {
	       	while (listening) {
	       		new PCEManagementSession(serverSocket.accept(),requestDispatcher, tedb,reservationManager,collaborationPCESessionManager, params).start();
	       	}	    
	       	serverSocket.close();
		}catch (SocketException e) {
			if (listening==false){
				log.info("Socket closed due to controlled close");
			}else {
				log.error("Problem with the socket, exiting");
				e.printStackTrace();
			}
		}  catch (Exception e) {
	       	e.printStackTrace();
		}				
	}
	
	public void stopServer(){
		listening=false;
		if (serverSocket!=null){
			try {
				serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
	}
}
