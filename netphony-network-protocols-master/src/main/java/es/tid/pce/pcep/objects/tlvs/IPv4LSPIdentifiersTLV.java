package es.tid.pce.pcep.objects.tlvs;

import java.net.Inet4Address;
import java.net.UnknownHostException;

import es.tid.pce.pcep.objects.MalformedPCEPObjectException;
import es.tid.pce.pcep.objects.ObjectParameters;
import es.tid.protocol.commons.ByteHandler;

/** IPV4-LSP-IDENTIFIERS TLV draft-ietf-pce-stateful-pce-11.
 * Encoding: 
 * TLV Type: 18 (non standard)
 * Whenever the value of an LSP identifier changes, a PCC MUST send out
   an LSP State Report, where the LSP Object carries the LSP Identifiers
   TLV that contains the new value.  The LSP Identifiers TLV MUST also
   be included in the LSP object during state synchronization.  There
   are two LSP Identifiers TLVs, one for IPv4 and one for IPv6.

   The format of the IPV4-LSP-IDENTIFIERS TLV is shown in the following
   figure:

      0                   1                   2                   3
      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |           Type=[TBD]          |           Length=12           |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |                   IPv4 Tunnel Sender Address                  |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |             LSP ID            |           Tunnel ID           |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |                        Extended Tunnel ID                     |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

                Figure 18: IPV4-LSP-IDENTIFIERS TLV format

   The type of the TLV is [TBD] and it has a fixed length of 12 octets.
   The value contains the following fields:

   IPv4 Tunnel Sender Address:  contains the sender node's IPv4 address,
      as defined in [RFC3209], Section 4.6.2.1 for the LSP_TUNNEL_IPv4
      Sender Template Object.

   LSP ID:  contains the 16-bit 'LSP ID' identifier defined in
      [RFC3209], Section 4.6.2.1 for the LSP_TUNNEL_IPv4 Sender Template
      Object.

   Tunnel ID:  contains the 16-bit 'Tunnel ID' identifier defined in
      [RFC3209], Section 4.6.1.1 for the LSP_TUNNEL_IPv4 Session Object.
      Tunnel ID remains constant over the life time of a tunnel.
      However, when Global Path Protection or Global Default Restoration
      is used, both the primary and secondary LSPs have their own Tunnel
      IDs.  A PCC will report a change in Tunnel ID when traffic
      switches over from primary LSP to secondary LSP (or vice versa).

   Extended Tunnel ID:  contains the 32-bit 'Extended Tunnel ID'
      identifier defined in [RFC3209], Section 4.6.1.1 for the
      LSP_TUNNEL_IPv4 Session Object.
      
      @author jaume
 */

public class IPv4LSPIdentifiersTLV extends PCEPTLV 
{
	private Inet4Address tunnelSenderIPAddress;
	
	private int lspID;
	
	private int tunnelID;
	
	private int extendedTunnelID;

	public IPv4LSPIdentifiersTLV()
	{
		this.TLVType=ObjectParameters.PCEP_TLV_TYPE_IPV4_LSP_IDENTIFIERS;
	}

	public IPv4LSPIdentifiersTLV(byte[] bytes, int offset)throws MalformedPCEPObjectException
	{		
		super(bytes,offset);		
		decode();
	}

	@Override
	public void encode() 
	{		
		int length = 12;
		this.setTLVValueLength(length);
		this.tlv_bytes=new byte[this.getTotalTLVLength()];
		encodeHeader();
		
		int offset = 4;
		System.arraycopy(tunnelSenderIPAddress.getAddress(),0, this.tlv_bytes, offset, 4);
		
		offset += 4;
		//this.tlv_bytes[offset]=0x01;
		this.tlv_bytes[offset]=(byte)(lspID>>>8 & 0xFF);
		this.tlv_bytes[offset+1]=(byte)(lspID & 0xFF);
		this.tlv_bytes[offset+2]=(byte)(tunnelID>>>8 & 0xFF);
		this.tlv_bytes[offset+3]=(byte)(tunnelID & 0xFF);
		
		offset += 4;
		
		ByteHandler.IntToBuffer(0,offset * 8,32,extendedTunnelID,tlv_bytes);
		
	}

	
	public void decode() throws MalformedPCEPObjectException 
	{		
		byte[] ip=new byte[4]; 
		int offset = 4;
		System.arraycopy(this.tlv_bytes,offset, ip, 0, 4);
		
		try 
		{
			tunnelSenderIPAddress=(Inet4Address)Inet4Address.getByAddress(ip);
			log.debug("Sender IP adress, tunnel: "+tunnelSenderIPAddress);
		} 
		catch (UnknownHostException e) 
		{			
			e.printStackTrace();
			throw new MalformedPCEPObjectException();
		}
		
		offset += 4;
		
		lspID = ByteHandler.easyCopy(0, 15, tlv_bytes[offset],tlv_bytes[offset+1]);
		tunnelID = ByteHandler.easyCopy(0, 15, tlv_bytes[offset+2],tlv_bytes[offset+3]);
		
		offset += 4;
		
		extendedTunnelID = ByteHandler.easyCopy(0, 31, tlv_bytes[offset],tlv_bytes[offset+1],
				tlv_bytes[offset+2],tlv_bytes[offset+3]);
		
	}
	
	//GETTERS & SETTERS
	
	public Inet4Address getTunnelSenderIPAddress() 
	{
		return tunnelSenderIPAddress;
	}

	public void setTunnelSenderIPAddress(Inet4Address tunnelSenderIPAddress) 
	{
		this.tunnelSenderIPAddress = tunnelSenderIPAddress;
	}

	public int getLspID() 
	{
		return lspID;
	}

	public void setLspID(int lspID) 
	{
		this.lspID = lspID;
	}

	public int getTunnelID() 
	{
		return tunnelID;
	}

	public void setTunnelID(int tunnelID) 
	{
		this.tunnelID = tunnelID;
	}

	public int getExtendedTunnelID() 
	{
		return extendedTunnelID;
	}

	public void setExtendedTunnelID(int extendedTunnelID) 
	{
		this.extendedTunnelID = extendedTunnelID;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + extendedTunnelID;
		result = prime * result + lspID;
		result = prime * result + tunnelID;
		result = prime
				* result
				+ ((tunnelSenderIPAddress == null) ? 0 : tunnelSenderIPAddress
						.hashCode());
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
		IPv4LSPIdentifiersTLV other = (IPv4LSPIdentifiersTLV) obj;
		if (extendedTunnelID != other.extendedTunnelID)
			return false;
		if (lspID != other.lspID)
			return false;
		if (tunnelID != other.tunnelID)
			return false;
		if (tunnelSenderIPAddress == null) {
			if (other.tunnelSenderIPAddress != null)
				return false;
		} else if (!tunnelSenderIPAddress.equals(other.tunnelSenderIPAddress))
			return false;
		return true;
	}
	
	
	

}
