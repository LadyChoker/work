package es.tid.rsvp.objects;

import java.net.Inet4Address;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.tid.rsvp.RSVPProtocolViolationException;



/*
 *

RFC 3209  RSVP-TE

 4.6.1.1. LSP_TUNNEL_IPv4 Session Object

   Class = SESSION, LSP_TUNNEL_IPv4 C-Type = 7

    0                   1                   2                   3
    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |                   IPv4 tunnel end point address               |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |  MUST be zero                 |      Tunnel ID                |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |                       Extended Tunnel ID                      |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

      IPv4 tunnel end point address

         IPv4 address of the egress node for the tunnel.

      Tunnel ID

         A 16-bit identifier used in the SESSION that remains constant
         over the life of the tunnel.

      Extended Tunnel ID

         A 32-bit identifier used in the SESSION that remains constant
         over the life of the tunnel.  Normally set to all zeros.
         Ingress nodes that wish to narrow the scope of a SESSION to the
         ingress-egress pair may place their IPv4 address here as a
         globally unique identifier.


 */





public class SessionLSPTunnelIPv4 extends Session{

	protected Inet4Address egressNodeAddress;
	protected long tunnelId;
	protected Inet4Address extendedTunnelId;
	
	/**
	 * Log
	 */

  private static final Logger log = LoggerFactory.getLogger("ROADM");
	
  public SessionLSPTunnelIPv4() {
	  super();
	  classNum = 1;
		cType = 7;
  }
  
	/**
	 * Constructor to be used when a new Session LSP Tunnel IPv4 Object wanted to be
	 * attached to a new message
	 * @param egressNodeAddress egressNodeAddress
	 * @param extendedTunnelId extendedTunnelId
	 * @param tunnelId tunnelId
	 */	
	public SessionLSPTunnelIPv4(Inet4Address egressNodeAddress, long tunnelId, Inet4Address extendedTunnelId){
		super();
		classNum = 1;
		cType = 7;
		

		this.egressNodeAddress = egressNodeAddress;
		this.tunnelId = tunnelId;
		this.extendedTunnelId = extendedTunnelId;
		
		log.debug("Session LSP Tunnel IPv4 Object Created");
		
	}
	
	/**
	 * Constructor to be used when a new Session LSP Tunnel IPv4 Object wanted to be decoded from a received
	 * message.
	 * @param bytes bytes
	 * @param offset offset
	 * @throws RSVPProtocolViolationException RSVP Protocol Violation Exception
	 */
	
	public SessionLSPTunnelIPv4(byte[] bytes, int offset) throws RSVPProtocolViolationException{
		
		super(bytes, offset);
		decode();
		
		log.debug("Session LSP Tunnel IPv4 Object Created");
	}

	/*
    0                   1                   2                   3
    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |                   IPv4 tunnel end point address (4 bytes)     |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |  MUST be zero                 |      Tunnel ID                |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |                       Extended Tunnel ID                      |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 */
	
	public void encode(){
		length = RSVPObjectParameters.RSVP_OBJECT_COMMON_HEADER_SIZE + 12;
		bytes = new byte[length];
		encodeHeader();
		
		byte[] addr = egressNodeAddress.getAddress();
		
		int currentIndex =  RSVPObjectParameters.RSVP_OBJECT_COMMON_HEADER_SIZE;
		
		System.arraycopy(addr,0, getBytes(), currentIndex, addr.length);
		currentIndex = currentIndex + addr.length;
		
		bytes[currentIndex] = (byte) 0;
		bytes[currentIndex+1] = (byte) 0;
		bytes[currentIndex+2] = (byte)((tunnelId>>8) & 0xFF);
		bytes[currentIndex+3] = (byte)(tunnelId & 0xFF);
		
		addr = extendedTunnelId.getAddress();
		
		currentIndex = currentIndex + 4;
		
		System.arraycopy(addr, 0, getBytes(), currentIndex, addr.length);

	}


	public void decode() throws RSVPProtocolViolationException{
		try{
		int offset = 4;
		
		byte[] receivedAddress = new byte[4];
		System.arraycopy(bytes,offset,receivedAddress,0,4);
	
		
			this.egressNodeAddress = (Inet4Address) Inet4Address.getByAddress(receivedAddress);
		
		offset = offset + 4;					
		this.tunnelId = (long)((bytes[offset+2] & 0xFF00) | bytes[offset+3]);
		
		offset = offset + 4;
		byte[] identTun = new byte[4];
		
		System.arraycopy(bytes,offset,identTun,0,4);
		
		
			this.extendedTunnelId = (Inet4Address) Inet4Address.getByAddress(identTun);
		}catch(Exception e){
			throw new RSVPProtocolViolationException();
		}
	}

	// Getters & Setters
	
	public Inet4Address getEgressNodeAddress() {
		return egressNodeAddress;
	}

	public void setEgressNodeAddress(Inet4Address egressNodeAddress) {
		this.egressNodeAddress = egressNodeAddress;
	}

	public long getTunnelId() {
		return tunnelId;
	}

	public void setTunnelId(long tunnelId) {
		this.tunnelId = tunnelId;
	}

	public Inet4Address getExtendedTunnelId() {
		return extendedTunnelId;
	}

	public void setExtendedTunnelId(Inet4Address extendedTunnelId) {
		this.extendedTunnelId = extendedTunnelId;
	}

}
