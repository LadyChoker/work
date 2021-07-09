package es.tid.pce.pcepsession;

import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.tid.pce.pcep.constructs.ErrorConstruct;
import es.tid.pce.pcep.messages.PCEPError;
import es.tid.pce.pcep.objects.ObjectParameters;
import es.tid.pce.pcep.objects.PCEPErrorObject;

/**
 * If no Open message is received before the expiration of the OpenWait
   timer, the PCEP peer sends a PCErr message with Error-Type=1 and
   Error-value=2, the system releases the PCEP resources for the PCEP
   peer, closes the TCP connection, and moves to the Idle state.

 * @author Oscar Gonzalez de Dios
 *
 */
public class OpenWaitTimerTask extends TimerTask {

//	private DataOutputStream out=null; //Use this to send messages to peer
	private PCEPSession parentPCESession;
	private Logger log;
	
	public OpenWaitTimerTask(PCEPSession parentPCESession){
		this.parentPCESession=parentPCESession;
		log=LoggerFactory.getLogger("PCEServer");
	}
		
	
	public void run() {
		log.warn("OPEN WAIT Timer OVER");
		PCEPError perror=new PCEPError();
		PCEPErrorObject perrorObject=new PCEPErrorObject();
		perrorObject.setErrorType(ObjectParameters.ERROR_ESTABLISHMENT);
		perrorObject.setErrorValue(ObjectParameters.ERROR_ESTABLISHMENT_NO_OPEN_MESSAGE);
		ErrorConstruct error_c=new ErrorConstruct();
		error_c.getErrorObjList().add(perrorObject);
		perror.setError(error_c);
		log.info("Sending Error");
		parentPCESession.sendPCEPMessage(perror);
		this.parentPCESession.killSession();
		return;
	}

}
