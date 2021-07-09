package es.tid.bgp.bgp4.update.fields.pathAttributes;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import es.tid.bgp.bgp4.update.fields.PathAttribute;

/**
 * RFC 4760  Multiprotocol Reachable NLRI - MP_REACH_NLRI (Type Code 14).

   This is an optional non-transitive attribute that can be used for the
   following purposes:

   (a) to advertise a feasible route to a peer

   (b) to permit a router to advertise the Network Layer address of the
       router that should be used as the next hop to the destinations
       listed in the Network Layer Reachability Information field of the
       MP_NLRI attribute.

   The attribute is encoded as shown below:

        +---------------------------------------------------------+
        | Address Family Identifier (2 octets)                    |
        +---------------------------------------------------------+
        | Subsequent Address Family Identifier (1 octet)          |
        +---------------------------------------------------------+
        | Length of Next Hop Network Address (1 octet)            |
        +---------------------------------------------------------+
        | Network Address of Next Hop (variable)                  |
        +---------------------------------------------------------+
        | Reserved (1 octet)                                      |
        +---------------------------------------------------------+
        | Network Layer Reachability Information (variable)       |
        +---------------------------------------------------------+

   The use and meaning of these fields are as follows:

      Address Family Identifier (AFI):

         This field in combination with the Subsequent Address Family
         Identifier field identifies the set of Network Layer protocols
         to which the address carried in the Next Hop field must belong,
         the way in which the address of the next hop is encoded, and
         the semantics of the Network Layer Reachability Information
         that follows.  If the Next Hop is allowed to be from more than
         one Network Layer protocol, the encoding of the Next Hop MUST
         provide a way to determine its Network Layer protocol.

         Presently defined values for the Address Family Identifier
         field are specified in the IANA's Address Family Numbers
         registry [IANA-AF].

      Subsequent Address Family Identifier (SAFI):

         This field in combination with the Address Family Identifier
         field identifies the set of Network Layer protocols to which
         the address carried in the Next Hop must belong, the way in
         which the address of the next hop is encoded, and the semantics
         of the Network Layer Reachability Information that follows.  If
         the Next Hop is allowed to be from more than one Network Layer
         protocol, the encoding of the Next Hop MUST provide a way to
         determine its Network Layer protocol.

      Length of Next Hop Network Address:

         A 1-octet field whose value expresses the length of the
         "Network Address of Next Hop" field, measured in octets.

      Network Address of Next Hop:

         A variable-length field that contains the Network Address of
         the next router on the path to the destination system.  The
         Network Layer protocol associated with the Network Address of
         the Next Hop is identified by a combination of {@code<AFI, SAFI>}
         carried in the attribute.

      Reserved:

         A 1 octet field that MUST be set to 0, and SHOULD be ignored
         upon receipt.

      Network Layer Reachability Information (NLRI):

         A variable length field that lists NLRI for the feasible routes
         that are being advertised in this attribute.  The semantics of
         NLRI is identified by a combination of {@code<AFI, SAFI>} carried in
         the attribute.

         When the Subsequent Address Family Identifier field is set to
         one of the values defined in this document, each NLRI is
         encoded as specified in the "NLRI encoding" section of this
         document.

   The next hop information carried in the MP_REACH_NLRI path attribute
   defines the Network Layer address of the router that SHOULD be used
   as the next hop to the destinations listed in the MP_NLRI attribute
   in the UPDATE message.

   The rules for the next hop information are the same as the rules for
   the information carried in the NEXT_HOP BGP attribute (see Section
   5.1.3 of [BGP-4]).

   An UPDATE message that carries the MP_REACH_NLRI MUST also carry the
   ORIGIN and the AS_PATH attributes (both in EBGP and in IBGP
   exchanges).  Moreover, in IBGP exchanges such a message MUST also
   carry the LOCAL_PREF attribute.

   An UPDATE message that carries no NLRI, other than the one encoded in
   the MP_REACH_NLRI attribute, SHOULD NOT carry the NEXT_HOP attribute.
   If such a message contains the NEXT_HOP attribute, the BGP speaker
   that receives the message SHOULD ignore this attribute.

   An UPDATE message SHOULD NOT include the same address prefix (of the
   same {@code<AFI, SAFI>}) in more than one of the following fields: WITHDRAWN
   ROUTES field, Network Reachability Information fields, MP_REACH_NLRI
   field, and MP_UNREACH_NLRI field.  The processing of an UPDATE
   message in this form is undefined.
 * @author pac
 *
 */
public abstract class MP_Reach_Attribute extends PathAttribute{

	private int addressFamilyIdentifier;
    
    private int subsequentAddressFamilyIdentifier;
    
    private InetAddress nextHop;
    
    private int nextHopLength;
	
	/*
	
    | Length of Next Hop Network Address (1 octet)            |
    +---------------------------------------------------------+
    | Network Address of Next Hop (variable)                  |
    +---------------------------------------------------------+
    | Reserved (1 octet)                                   
    */
	
	/*
	 *  Network Layer Reachability Information (variable)  
	 * 
	 */
    
    public MP_Reach_Attribute(){
    	this.setTypeCode(PathAttributesTypeCode.PATH_ATTRIBUTE_TYPECODE_MP_REACH_NLRI);
    	this.optionalBit = true;
		this.transitiveBit = false;
		//By default nextHop Length=4;
		this.nextHopLength=4;
		//By default nextHop=0.0.0.0
		try {
			this.nextHop=Inet4Address.getByName("0.0.0.0");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public MP_Reach_Attribute(byte [] bytes, int offset){
    	super(bytes,offset);
    	int offset2=this.mandatoryLength;
		this.addressFamilyIdentifier=((this.bytes[offset2]&0xFF)<<8) | (this.bytes[offset2+1]&0xFF);
		this.subsequentAddressFamilyIdentifier = (this.bytes[offset2+2]&0xFF);
		this.nextHopLength= (this.bytes[offset2+3]&0xFF);
		byte[] bytos = new byte[nextHopLength];
		
		System.arraycopy(bytes, offset2+4, bytos, 0, this.nextHopLength);
		if (this.nextHopLength==4){
			try {
				this.nextHop=Inet4Address.getByAddress(bytos);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//FIXME: ADD IPv6
		
    	
    }
	
    protected void encodeMP_Reach_Header(){
    	int offset = this.getMandatoryLength();
    	//AFI
    	this.bytes[offset]=(byte)((this.addressFamilyIdentifier>>8)&0xFF);
    	this.bytes[offset+1]=(byte)(this.addressFamilyIdentifier&0xFF);
    	//SAFI
    	this.bytes[offset+2]=(byte)(this.subsequentAddressFamilyIdentifier&0xFF);
    	this.bytes[offset+3]=(byte)this.getNextHopLength();
    	offset=offset+4;
    	//FIXME: Complete IPv6
    	if (this.nextHopLength==4){
    		System.arraycopy(this.nextHop.getAddress(), 0, this.bytes, offset, 4);
    	}
    	offset=offset+this.getNextHopLength();
    	this.bytes[offset]=0;
    }
	


	public int getLengthofNextHopNetworkAddress() {
		return nextHopLength;
	}

	public int getAddressFamilyIdentifier() {
		return addressFamilyIdentifier;
	}

	protected void setAddressFamilyIdentifier(int addressFamilyIdentifier) {
		this.addressFamilyIdentifier = addressFamilyIdentifier;
	}

	public int getSubsequentAddressFamilyIdentifier() {
		return subsequentAddressFamilyIdentifier;
	}

	protected void setSubsequentAddressFamilyIdentifier(
			int subsequentAddressFamilyIdentifier) {
		this.subsequentAddressFamilyIdentifier = subsequentAddressFamilyIdentifier;
	}
	
	public static int getAFI(byte [] bytes, int offset){
		int ml= PathAttribute.getMandatoryLength(bytes, offset);
		int offset2= offset+ml;
		int addressFamilyIdentifier=((bytes[offset2]&0xFF)<<8) | (bytes[offset2+1]&0xFF);
		return addressFamilyIdentifier;
	}
	
	public static int getSAFI(byte [] bytes, int offset){
		int ml= PathAttribute.getMandatoryLength(bytes, offset);
		int offset2= offset+ml;
		int subsequentAddressFamilyIdentifier = (bytes[offset2+2]&0xFF);
		return subsequentAddressFamilyIdentifier;

	}
	
	
	
	public InetAddress getNextHop() {
		return nextHop;
	}

	public void setNextHop(InetAddress nextHop) {
		this.nextHop = nextHop;
		if (nextHop instanceof Inet4Address){
    		nextHopLength=4;
    	}else if (nextHop instanceof Inet6Address){
    		nextHopLength=8;
    	}else {
    		nextHopLength=4;
    	}
	}
	
	

	public int getNextHopLength() {
		return nextHopLength;
	}

	public void setNextHopLength(int nextHopLength) {
		this.nextHopLength = nextHopLength;
	}

	public String toString(){
		return "mp";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + addressFamilyIdentifier;
		result = prime * result + ((nextHop == null) ? 0 : nextHop.hashCode());
		result = prime * result + nextHopLength;
		result = prime * result + subsequentAddressFamilyIdentifier;
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
		MP_Reach_Attribute other = (MP_Reach_Attribute) obj;
		if (addressFamilyIdentifier != other.addressFamilyIdentifier)
			return false;
		if (nextHop == null) {
			if (other.nextHop != null)
				return false;
		} else if (!nextHop.equals(other.nextHop))
			return false;
		if (nextHopLength != other.nextHopLength)
			return false;
		if (subsequentAddressFamilyIdentifier != other.subsequentAddressFamilyIdentifier)
			return false;
		return true;
	}
	
	

}
