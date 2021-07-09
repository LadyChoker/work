package es.tid.ospf.ospfv2.lsa.tlv.subtlv;

/**
 * 
 * Maximum Bandwidth (Type 6) RFC 3630
 *
 * The Maximum Bandwidth sub-TLV specifies the maximum bandwidth that
 * can be used on this link, in this direction (from the system
 * originating the LSA to its neighbor), in IEEE floating point format.
 * This is the true link capacity.  The units are bytes per second.
 *
 * IANA Assignment in https://www.iana.org/assignments/ospf-traffic-eng-tlvs/ospf-traffic-eng-tlvs.xhtml#subtlv6
 *
 * @see <a href="https://www.iana.org/assignments/ospf-traffic-eng-tlvs/ospf-traffic-eng-tlvs.xhtml#subtlv6">IANA assignments of OSPF Traffic Engneering TLVs</a>
 * @see <a href="http://www.ietf.org/rfc/rfc3630"> RFC 3630</a>
 *    
 * @author ogondio
 *
 */
public class MaximumBandwidth extends OSPFSubTLV {

	/*
	 *    The Maximum Bandwidth sub-TLV is TLV type 6, and is four octets in
   length.
	 */
	
	public float maximumBandwidth;
	
	public MaximumBandwidth(){
		this.setTLVType(OSPFSubTLVTypes.MaximumBandwidth);
	}
	
	public MaximumBandwidth(byte[] bytes, int offset)throws MalformedOSPFSubTLVException{
		super(bytes,offset);
		decode();
	}
	
	public void encode() {
		this.setTLVValueLength(4);
		this.tlv_bytes=new byte[this.getTotalTLVLength()];
		encodeHeader();
		int bwi=Float.floatToIntBits(maximumBandwidth);
		this.tlv_bytes[4]=(byte)(bwi >>> 24);
		this.tlv_bytes[5]=(byte)(bwi >> 16 & 0xff);
		this.tlv_bytes[6]=(byte)(bwi >> 8 & 0xff);
		this.tlv_bytes[7]=(byte)(bwi & 0xff);	
	}
	
	protected void decode()throws MalformedOSPFSubTLVException{
		if (this.getTLVValueLength()!=4){
			throw new MalformedOSPFSubTLVException();
		}
		int bwi = 0;		
		for (int k = 0; k < 4; k++) {
			bwi = (bwi << 8) | (this.tlv_bytes[k+4] & 0xff);
		}
		this.maximumBandwidth=Float.intBitsToFloat(bwi);
	}
	
	

	public float getMaximumBandwidth() {
		return maximumBandwidth;
	}

	public void setMaximumBandwidth(float maximumBandwidth) {
		this.maximumBandwidth = maximumBandwidth;
	}

	public String toString(){
		return "MaximumBandwidth: "+Float.toString(maximumBandwidth);
	}
	public String toStringShort(){
		return "MaxBW: "+Float.toString(maximumBandwidth);
	}
}
