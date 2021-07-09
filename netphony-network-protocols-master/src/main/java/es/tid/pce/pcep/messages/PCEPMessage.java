package es.tid.pce.pcep.messages;

/**
 * PCEP Message as defined in RFC 5440
 * <p> Derive all PCEP Messages from this base class <p>
 * @author Oscar Gonzalez de Dios
**/

import java.util.Arrays;

import es.tid.pce.pcep.PCEPElement;
import es.tid.pce.pcep.PCEPProtocolViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Base class for PCEP Messages.
 * 
 * Common Header PCEP
 * {@code
0                   1                   2                   3
0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
| Ver |  Flags  |  Message-Type |       Message-Length          |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+}
*/
public abstract class PCEPMessage implements PCEPElement {

	protected byte messageBytes[];//The bytes of the message 

	private int messageType; //MessageType
	private int Ver;//PCEP Version (by default=0x01
	private int Flags;//By default to 0x00
	private int messageLength;

	protected final Logger log = LoggerFactory.getLogger("PCEPParser");



	public void setMessageLength(int messageLength) {
		this.messageLength = messageLength;
	}

	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}

	/** Default constructor
	 * 
	 */
	public PCEPMessage(){
		Ver=0x01;
		Flags=0x00;
		messageLength=0;
	}

	/**
	 * Creates a PCEP message from a byte array. 
	 * Decodes the message header 
	 * @param bytes bytes
	 * @throws PCEPProtocolViolationException Exception when the message is malformed 
	 */
	public PCEPMessage(byte []bytes) throws PCEPProtocolViolationException{
		messageLength=(bytes[2] & 0xFF)* 256 + (bytes[3]& 0xFF);
		if (bytes.length!=this.getLength()){
			log.warn("Bytes and length in header do not match");
			throw new PCEPProtocolViolationException();
		}
		this.messageBytes=new byte[messageLength];
		System.arraycopy(bytes, 0, messageBytes, 0, messageLength);
		messageType=messageBytes[1]&0xFF;
		Ver= (messageBytes[0] & 0xE0)>>>5;
	}


	/**
	 * Get the message Bytes
	 * @return bytes of the message
	 */
	public byte[] getBytes() {
		return messageBytes;
	}

/**
	Message-Type (8 bits):  The following message types are currently
      defined:

         Value    Meaning
           1        Open
           2        Keepalive
           3        Path Computation Request
           4        Path Computation Reply
           5        Notification
           6        Error
           7        Close
           8 		PCMonReq
           9		PCMonRep
          TBD  		PCUpdate
          TBD		PCReport
          @return message Type
*/
	public int getMessageType() {
		return messageType;	
	}
	public static int getMessageType(byte[] bytes){
		int mt;
		mt= bytes[1];
		return mt;
	}

	public int getVer() {
		return Ver;	
	}

	public int getLength() {
		return messageLength;
	}


	//public abstract void decode(byte[] bytes) throws PCEPProtocolViolationException;

	protected void encodeHeader() { 
		messageBytes[0]= (byte)(((Ver<<5) &0xE0) | (Flags & 0x1F));
		messageBytes[1]=(byte)messageType;
		messageBytes[2]=(byte)((messageLength>>8) & 0xFF);
		messageBytes[3]=(byte)(messageLength & 0xFF);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Flags;
		result = prime * result + Ver;
		result = prime * result + Arrays.hashCode(messageBytes);
		result = prime * result + messageLength;
		result = prime * result + messageType;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PCEPMessage other = (PCEPMessage) obj;
		if (Flags != other.Flags)
			return false;
		if (Ver != other.Ver)
			return false;
		if (!Arrays.equals(messageBytes, other.messageBytes))
			return false;
		if (messageLength != other.messageLength)
			return false;
		if (messageType != other.messageType)
			return false;
		return true;
	}	

//	protected void decodeHeader(){
//		messageType=messageBytes[1];
//		Ver= (messageBytes[0] & 0x224)/32;
//		//Flags=
//		messageLength=(messageBytes[2] & 0xFF)* 256 + (messageBytes[3]& 0xFF);	
//	}

	
	
}