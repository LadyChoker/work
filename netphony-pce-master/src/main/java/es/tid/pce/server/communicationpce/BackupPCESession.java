package es.tid.pce.server.communicationpce;

import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.tid.pce.pcep.PCEPProtocolViolationException;
import es.tid.pce.pcep.messages.PCEPClose;
import es.tid.pce.pcep.messages.PCEPMessage;
import es.tid.pce.pcep.messages.PCEPMessageTypes;
import es.tid.pce.pcep.messages.PCEPNotification;
import es.tid.pce.pcepsession.DeadTimerThread;
import es.tid.pce.pcepsession.GenericPCEPSession;
import es.tid.pce.pcepsession.KeepAliveThread;
import es.tid.pce.pcepsession.PCEPSessionsInformation;
import es.tid.pce.pcepsession.PCEPValues;
import es.tid.pce.server.NotificationDispatcher;
import es.tid.pce.server.NotificationProcessorThread;
import es.tid.tedb.TEDB;

/**
 * STRONGEST: Collaborative PCEs
 * Session del PCE de Backup. 
 * @author Marta Cuaresma 
 *
 */
public class BackupPCESession extends GenericPCEPSession{
	
	
	/**
	 * Address of the peer PCE
	 */
	private String primaryPCE_IPaddress;
	
	private String localIPaddress;
	/**
	 * Port of the peer PCE
	 */
	private int primaryPCE_port;
	
	private int localPort;
	/**
	 * Flag to indicate that the session is up
	 */
	private boolean running = true;
	
	private boolean no_delay=false;
	
	private NotificationDispatcher notificationDispatcher;

	NotificationProcessorThread npt;
	CollaborationPCESessionManager collaborationPCESessionManager;

	public BackupPCESession(String ip, int port, boolean no_delay,TEDB ted,CollaborationPCESessionManager collaborationPCESessionManager, NotificationDispatcher notificationDispatcher,Timer timer, PCEPSessionsInformation pcepSessionInformation/*, String localIP, int localPort*/) {
		super(pcepSessionInformation);
		this.setFSMstate(PCEPValues.PCEP_STATE_IDLE);
		log=LoggerFactory.getLogger("PCEServer");
		this.primaryPCE_IPaddress=ip;
		this.primaryPCE_port=port;
		//crm= new ClientRequestManager();
		this.keepAliveLocal=30;
		this.deadTimerLocal=120;
		this.timer=timer;
		this.no_delay=no_delay;
		this.collaborationPCESessionManager=collaborationPCESessionManager;	
		this.notificationDispatcher=notificationDispatcher;
		/*this.localIPaddress=localIP;
		this.localPort=localPort;*/
	}

	
public void run (){
	running=true;
	//Connect to the primary PCE
	log.info("Opening new PCEP Session with host "+ primaryPCE_IPaddress + " on port " + primaryPCE_port);
	
	try {
		this.socket = new Socket(primaryPCE_IPaddress, primaryPCE_port/*, (Inet4Address)InetAddress.getByName(localIPaddress), localPort*/);
		if (no_delay){
			this.socket.setTcpNoDelay(true);
			log.info("No delay activated");
		}
		log.info("Socket opened");
	} catch (IOException e) {
		log.error("Couldn't get I/O for connection to " + primaryPCE_IPaddress + " in port "+ primaryPCE_port);
		killSession();
		return;			
	} 

	//Inicialize session
	initializePCEPSession(false, 15, 200,false,false,null,null,0);

	collaborationPCESessionManager.getOpenedSessionsManager().registerNewSession(out, RollSessionType.COLLABORATIVE_PCE);
	this.deadTimerT=new DeadTimerThread(this, this.deadTimerLocal);
	startDeadTimer();	
	this.keepAliveT=new KeepAliveThread(out, this.keepAliveLocal);
	startKeepAlive();

	
	while(running) {
		try {
			   this.msg = readMsg(in);//Read a new message
			}catch (IOException e){
				cancelDeadTimer();
				cancelKeepAlive();
				timer.cancel();
				try {
					in.close();
					out.close();
				} catch (IOException e1) {
				}
					log.error("Finishing PCEP Session abruptly");
				return;
			}
			if (this.msg != null) {//If null, it is not a valid PCEP message								
				boolean pceMsg = true;//By now, we assume a valid PCEP message has arrived
				//Depending on the type a different action is performed
				switch(PCEPMessage.getMessageType(this.msg)) {
				
				case PCEPMessageTypes.MESSAGE_OPEN:
					log.debug("OPEN message received");
					//After the session has been started, ignore subsequent OPEN messages
					log.warn("OPEN message ignored");
					break;
					
				case PCEPMessageTypes.MESSAGE_KEEPALIVE:
					log.debug("KEEPALIVE message received");
					//The Keepalive message allows to reset the deadtimer
					break;
					
				case PCEPMessageTypes.MESSAGE_CLOSE:
					log.debug("CLOSE message received");
					try {
						PCEPClose m_close=new PCEPClose(this.msg);		
						log.warn("Closing due to reason "+m_close.getReason());
						this.killSession();
					} catch (PCEPProtocolViolationException e1) {
						log.warn("Problem decoding message, closing session"+e1.getMessage());
						this.killSession();
						return;
					}					
					return;
					
					
				case PCEPMessageTypes.MESSAGE_ERROR:
					log.debug("ERROR message received");
					break;
					
				case PCEPMessageTypes.MESSAGE_NOTIFY:				
					log.debug("Received NOTIFY message");
					PCEPNotification m_not;
					try {
						m_not=new PCEPNotification(this.msg);		
						notificationDispatcher.dispatchNotification(m_not);
					} catch (PCEPProtocolViolationException e1) {
						log.warn("Problem decoding notify message, ignoring message"+e1.getMessage());
						e1.printStackTrace();
					}			
					break;		
					
				default:
					log.warn("ERROR: unexpected message");
					pceMsg = false;
				}
				if (pceMsg) {
					log.debug("Reseting Dead Timer as PCEP Message has arrived");
					resetDeadTimer();
				}
				
			}
	}
}

	public CollaborationPCESessionManager getCollaborationPCESessionManager() {
	return collaborationPCESessionManager;
}


public void setCollaborationPCESessionManager(CollaborationPCESessionManager collaborationPCESessionManager) {
	this.collaborationPCESessionManager = collaborationPCESessionManager;
}


	@Override
	protected void endSession() {
		// TODO Auto-generated method stub
		
	}

}
