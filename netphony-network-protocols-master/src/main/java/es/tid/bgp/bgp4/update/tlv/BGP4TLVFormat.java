package es.tid.bgp.bgp4.update.tlv;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Information in the new link state NLRIs and attributes is encoded in
   Type/Length/Value triplets.  The TLV format is shown in Figure 4.
<pre>
      0                   1                   2                   3
      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |              Type             |             Length            |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |                                                               |
     |                         Value (variable)                      |
     |                                                               |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
</pre>
                           Figure 4: TLV format

   The Length field defines the length of the value portion in octets
   (thus a TLV with no value portion would have a length of zero).  The
   TLV is not padded to four-octet alignment; Unrecognized types are
   ignored.


 * @author pac
 *
 */
public abstract class BGP4TLVFormat {

	
	protected int TLVType;
	
	protected int TLVValueLength;
	protected int TotalTLVLength;
	
	protected byte[] tlv_bytes;
	
	protected static final Logger log = LoggerFactory.getLogger("BGP4Parser");
	public BGP4TLVFormat(){
	}
	
	public BGP4TLVFormat(byte []bytes, int offset) {
		this.TLVType=((  ((int)bytes[offset]&0xFF)   <<8)& 0xFF00) |  ((int)bytes[offset+1] & 0xFF);
		this.TLVValueLength=((((int)bytes[offset+2]&0xFF)<<8)& 0xFF00) |  ((int)bytes[offset+3] & 0xFF);
		this.TotalTLVLength=TLVValueLength+4;
		this.tlv_bytes=new byte[TotalTLVLength];
		System.arraycopy(bytes, offset, tlv_bytes, 0, TotalTLVLength);

	}
	
	protected void encodeHeader(){
		this.tlv_bytes[0]=(byte)(TLVType>>>8 & 0xFF);
		this.tlv_bytes[1]=(byte)(TLVType & 0xFF);
		this.tlv_bytes[2]=(byte)(TLVValueLength>>>8 & 0xFF);
		this.tlv_bytes[3]=(byte)(TLVValueLength & 0xFF);
	}
	
	public int getTLVValueLength(){
		return TLVValueLength;
	}
	public int getTotalTLVLength(){
		return TotalTLVLength;
	}
	
	public static int getTotalTLVLength(byte []bytes, int offset) {
		int len=((((int)bytes[offset+2]&0xFF)<<8)& 0xFF00) |  ((int)bytes[offset+3] & 0xFF)+4;
		return len;
	}
	
	public static int getTLVLength(byte []bytes, int offset) {
		int len=((((int)bytes[offset+2]&0xFF)<<8)& 0xFF00) |  ((int)bytes[offset+3] & 0xFF);
		return len;
	}
	
	
	public static int getType(byte []bytes, int offset) {
		
		int typ=((  ((int)bytes[offset]&0xFF)   <<8)& 0xFF00) |  ((int)bytes[offset+1] & 0xFF);
		return typ;
	}
	
	
	
	public int getTLVType() {
		return TLVType;
	}


	protected void setTLVType(int tLVType) {
		TLVType = tLVType;
	}


	public byte[] getTlv_bytes() {
		return tlv_bytes;
	}


	protected void setTlv_bytes(byte[] tlv_bytes) {
		this.tlv_bytes = tlv_bytes;
	}


	/**
	 * Sets the lenght of the VALUE of the TLV. The total length is computed!!!
	 * @param TLVValueLength TLV Value Length
	 */
	protected void setTLVValueLength(int TLVValueLength) {
		this.TLVValueLength = TLVValueLength;
		this.TotalTLVLength=TLVValueLength+4;		
	}
	


	public abstract void encode();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + TLVType;
		result = prime * result + TLVValueLength;
		result = prime * result + TotalTLVLength;
		result = prime * result + Arrays.hashCode(tlv_bytes);
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
		BGP4TLVFormat other = (BGP4TLVFormat) obj;
		if (TLVType != other.TLVType)
			return false;
		if (TLVValueLength != other.TLVValueLength)
			return false;
		if (TotalTLVLength != other.TotalTLVLength)
			return false;
		if (!Arrays.equals(tlv_bytes, other.tlv_bytes))
			return false;
		return true;
	}
	
	

}
