package es.tid.pce.pcep.messages;

import es.tid.pce.pcep.PCEPProtocolViolationException;
import es.tid.pce.pcep.objects.Close;
import es.tid.pce.pcep.objects.MalformedPCEPObjectException;
import es.tid.pce.pcep.objects.ObjectParameters;
import es.tid.pce.pcep.objects.PCEPObject;

/**
 * PCEP Close Message (RFC 5440). 
 * <p> From RFC 5440 Section 6.8 </p>
 * 6.8. Close Message
 *{@code
 * The Close message is a PCEP message that is either sent by a PCC to a
 * PCE or by a PCE to a PCC in order to close an established PCEP
 * session.  The Message-Type field of the PCEP common header for the
 * Close message is set to 7.
 * The format of a Close message is as follows:
 *
 *  <Close Message>::= <Common Header>
 *                    <CLOSE>
 * The Close message MUST contain exactly one CLOSE object (see
 * Section 6.8).  If more than one CLOSE object is present, the first
 * MUST be processed and subsequent objects ignored.
 *
 * Upon the receipt of a valid Close message, the receiving PCEP peer
 * MUST cancel all pending requests, it MUST close the TCP connection
 * and MUST NOT send any further PCEP messages on the PCEP session.
 * }
 * @author Oscar Gonzalez de Dios
 * @version 0.1
**/

public class PCEPClose extends PCEPMessage {
	
	/**
	 * PCEP Close object
	 */
	private Close close;

	/**
	 * Construct new PCEP CLOSE Message
	 */
	public PCEPClose () {
		super();
		this.setMessageType(PCEPMessageTypes.MESSAGE_CLOSE);
		close=new Close();
	}
	
	/**
	 * Contructs and decodes new PCEP CLOSE Message from a byte array
	 * @param bytes Bytes of the message
	 * @throws PCEPProtocolViolationException Exception when the message is malformed 
	 */
	public PCEPClose(byte[] bytes) throws PCEPProtocolViolationException {
		super(bytes);
		this.decode();
	}

	/**
	 * Set the reason for closing the PCEP session
	 * @param reason reason of the close
	 */
	public void setReason(int reason){
		close.setReason(reason);
	}
	
	/**
	 * Get the reason for closing the PCEP session
	 * @return reason 
	 */
	public int getReason(){
		return close.getReason();
	}
	
	/**
	 * Encode the PCEP Message
	 */
	public void encode() throws PCEPProtocolViolationException {
		close.encode();
		this.setMessageLength(4+close.getLength());
		this.messageBytes=new byte[this.getLength()];
		encodeHeader();
		System.arraycopy(close.getBytes(), 0, messageBytes, 4, close.getLength());				
	}
	
	/**
	 * Decode a PCEP Message from a byte array. 
	 * The byte array is copied in messageBytes
	 * @throws PCEPProtocolViolationException Exception when the message is malformed 
	 */
	private void decode()  throws PCEPProtocolViolationException {
		//Decoding PCEP Close Message"
		int offset=4;//We start after the object header
		// TODO Auto-generated method stub
		if (PCEPObject.getObjectClass(this.messageBytes, offset)==ObjectParameters.PCEP_OBJECT_CLASS_CLOSE){
			try {
				close=new Close(this.messageBytes,offset);
			} catch (MalformedPCEPObjectException e) {
				log.warn("Malformed PCEP Close object");
				throw new PCEPProtocolViolationException();
			}			
		}
		else {
			throw new PCEPProtocolViolationException();
		}
	}
	
	

	public Close getClose() {
		return close;
	}

	public void setClose(Close close) {
		this.close = close;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((close == null) ? 0 : close.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj)) 
			return false;
		if (getClass() != obj.getClass())
			return false;
		PCEPClose other = (PCEPClose) obj;
		if (close == null) {
			if (other.close != null)
				return false;
		} else if (!close.equals(other.close)) 
			return false;
		return true;
	}
	
	
	
}