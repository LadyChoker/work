/*
 * PCEP KeepAlive management Thread
 * 
 * Carlos Garcia Argos (cgarcia@novanotio.es)
 * Feb. 11 2010
 */

package es.tid.pce.pcepsession;

import java.io.DataOutputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.tid.pce.pcep.PCEPProtocolViolationException;
import es.tid.pce.pcep.messages.PCEPKeepalive;

public class KeepAliveThread extends Thread {

	private int keepAlive = 0;
	private boolean running;
	private Logger log;
	private DataOutputStream out=null; //Use this to send messages to peer	
	
	 /* 
	 * @param p
	 * @param k
	 */
	public KeepAliveThread(DataOutputStream out, int k) {
		this.keepAlive = k;
		this.out = out;
		log=LoggerFactory.getLogger("PCEServer");
	}
	
	/**
	 * Starts the Keepalive process
	 */
	public void run() {
		running=true;
		while (running) {
			try {
				if (keepAlive > 0) {
					sleep(keepAlive * 1000);
					sendKeepAlive();
				}
				else {
					log.warn("Ending KEEPALIVE mechanism");
					return;
				}
			} catch (InterruptedException e) {
				if (running==false){
					log.warn("Ending KeepAliveThread");
					return;
				}
				else {
					//Keepalive Timer is reseted
					log.debug("Reseting Keepalive timer");
				}
			} 
		}
	}
	
	/**
	 * Sets the running variable to false. After this, an interrupt will cause 
	 * the KeepaliveThread to end.
	 */
	public void stopRunning(){
		running=false;
	}
	/**
	 * Sends KeepAlive Message. It does not wait for any response.
	 */
	private void sendKeepAlive() {
		PCEPKeepalive p_ka= new PCEPKeepalive();
		try {
			p_ka.encode();
		} catch (PCEPProtocolViolationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			log.debug("Sending Keepalive message");
			out.write(p_ka.getBytes());
			out.flush();
		} catch (IOException e) {
			log.warn("Sending KEEPALIVE: " + e.getMessage());
		}
	}
	
}
