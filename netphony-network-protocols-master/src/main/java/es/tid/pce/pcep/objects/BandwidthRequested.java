package es.tid.pce.pcep.objects;

/**
 * <p>Represents a PCEP BANDWIDTH object, as defined in RFC 5440.</p>
 * 
 * <p>From RFC 5440 Section 7.7. BANDWIDTH Object:</p>
 * 
 * <p>The BANDWIDTH object is used to specify the requested bandwidth for a
 * TE LSP.  The notion of bandwidth is similar to the one used for RSVP
 * signaling in [RFC2205], [RFC3209], and [RFC3473].</p>
 *
 * <p>If the requested bandwidth is equal to 0, the BANDWIDTH object is
 * optional.  Conversely, if the requested bandwidth is not equal to 0,
 * the PCReq message MUST contain a BANDWIDTH object.</p>
 *
 * <p>In the case of the reoptimization of a TE LSP, the bandwidth of the
 * existing TE LSP MUST also be included in addition to the requested
 * bandwidth if and only if the two values differ.  Consequently, two
 * Object-Type values are defined that refer to the requested bandwidth
 * and the bandwidth of the TE LSP for which a reoptimization is being
 * performed.</p>
 *
 * <p>The BANDWIDTH object may be carried within PCReq and PCRep messages.
 * BANDWIDTH Object-Class is 5.</p>
 *
 * <p>Two Object-Type values are defined for the BANDWIDTH object:</p>
 *<pre>
 *  o  Requested bandwidth: BANDWIDTH Object-Type is 1.
 *
 *  o  Bandwidth of an existing TE LSP for which a reoptimization is
 *     requested.  BANDWIDTH Object-Type is 2.
 *
 * The format of the BANDWIDTH object body is as follows:
 *
 *  0                   1                   2                   3
 *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                        Bandwidth                              |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * 
 *                 Figure 14: BANDWIDTH Object Body Format
 * </pre>                
 * <p>Bandwidth (32 bits):  The requested bandwidth is encoded in 32 bits
      in IEEE floating point format (see [IEEE.754.1985]), expressed in
      bytes per second.  Refer to Section 3.1.2 of [RFC3471] for a table
      of commonly used values.</p>

   <p>The BANDWIDTH object body has a fixed length of 4 bytes.</p>

 * 
 * @author Carlos Garcia Argos (cgarcia@novanotio.es)  (Feb. 17 2010)
 * @author Oscar Gonzalez de Dios (ogondio@tid.es) (Nov. 29 2010)
 * @version 0.1
 */
public class BandwidthRequested extends Bandwidth{

	/**
	 * Bandwidth (32 bits):  The requested bandwidth is encoded in 32 bits
      in IEEE floating point format (see [IEEE.754.1985]), expressed in
      bytes per second.  Refer to Section 3.1.2 of [RFC3471] for a table
      of commonly used values.
	 */
	public float bw  = 0;

	//Constructors
	
	/**
	 * Constructs a Bandwidth object
	 */
	public BandwidthRequested (){
		super();
		this.setOT(ObjectParameters.PCEP_OBJECT_TYPE_BANDWIDTH_REQUEST);
	}	

	/**
	 * Constructs a PCEP object from a sequence of bytes 
	 * @param bytes Sequence of bytes where the object is present
	 * @param offset Position at which the object starts
	 * @throws MalformedPCEPObjectException Exception when the object is malformed
	 */
	public BandwidthRequested (byte []bytes, int offset) throws MalformedPCEPObjectException{
		super(bytes, offset);
		decode();
	}

	//Encode and Decode
	
	/**
	 * Encodes the PCEP BANDWIDTH object
	 */
	public void encode(){
		ObjectLength=8;/* 4 bytes de la cabecera + 4 del cuerpo */
		object_bytes=new byte[ObjectLength];
		encode_header();
		int bwi=Float.floatToIntBits(bw);
		this.object_bytes[4]=(byte)(bwi >>> 24);
		this.object_bytes[5]=(byte)(bwi >> 16 & 0xff);
		this.object_bytes[6]=(byte)(bwi >> 8 & 0xff);
		this.object_bytes[7]=(byte)(bwi & 0xff);	
	}

	
	/**
	 * Decodes the BANDWDITH object
	 */
	public void decode() throws MalformedPCEPObjectException{
		if (ObjectLength!=8){
			throw new MalformedPCEPObjectException();
		}
	
		int bwi = 0;		
		for (int k = 0; k < 4; k++) {
			bwi = (bwi << 8) | (object_bytes[k+4] & 0xff);
		}
		bw=Float.intBitsToFloat(bwi);
	}
	
	//Getters and Setters
	
	/**
	 *  Gets the requested bandwidth
	 * @return Bandwidth
	 */
	public float getBw() {
		return bw;
	}

	/**
	 * Sets the requested bandwidth
	 * @param bw Requested Bandwidth
	 */
	public void setBw(float bw) {
		this.bw = bw;
	}

	
	public String toString(){
		String ret="<BW= "+bw+">";
		return ret;
	}

	public Bandwidth duplicate(){
		BandwidthRequested bww= new BandwidthRequested();
		bww.bw=bw;
		return bww;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Float.floatToIntBits(bw);
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
		BandwidthRequested other = (BandwidthRequested) obj;
		if (Float.floatToIntBits(bw) != Float.floatToIntBits(other.bw))
			return false;
		return true;
	}
	
	

}
