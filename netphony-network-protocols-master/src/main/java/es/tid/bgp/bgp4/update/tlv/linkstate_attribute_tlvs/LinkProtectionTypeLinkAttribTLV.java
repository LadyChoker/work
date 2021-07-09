package es.tid.bgp.bgp4.update.tlv.linkstate_attribute_tlvs;

import es.tid.bgp.bgp4.update.tlv.BGP4TLVFormat;

/**
 * Link Protection Type	TLV (Type 1093) [RFC5307, Section 1.2]
 * 
 * @author pac
 *
 */

public class LinkProtectionTypeLinkAttribTLV extends BGP4TLVFormat {
	
	/*
	 *The first octet is a bit vector describing the protection capabilities
	   of the link (see Section 2.2, "Link Protection Type", of
	   [GMPLS-ROUTING]).  They are:

	      0x01  Extra Traffic

	      0x02  Unprotected

	      0x04  Shared

	      0x08  Dedicated 1:1

	      0x10  Dedicated 1+1

	      0x20  Enhanced

	      0x40  Reserved

	      0x80  Reserved

	   The second octet SHOULD be set to zero by the sender, and SHOULD be
	   ignored by the receiver.
	   
	 */
	
	private int protection_type;
	
	public static final int EXTRA_TRAFFIC = 0x01;
	public static final int UNPROTECTED = 0x02;
	public static final int SHARED = 0x04;
	public static final int DEDICATED_1_1 = 0x08;
	public static final int DEDICATED_1_PLUS_1 = 0x10;
	public static final int ENHANCED = 0x20;
	public static final int RESERVED1 = 0x40;
	public static final int RESERVED2 = 0x80;
	
	

	public LinkProtectionTypeLinkAttribTLV(){
		super();
		this.setTLVType(LinkStateAttributeTLVTypes.LINK_ATTRIBUTE_TLV_TYPE_LINK_PROTECTION_TYPE);
	}
	
	public LinkProtectionTypeLinkAttribTLV(byte[] bytes, int offset){
		super(bytes,offset);
		decode();
	}	
		
	@Override
	public void encode() {
		// TODO Auto-generated method stub

	}
	
	protected void decode(){
		int offset = 4;
		this.setProtection_type(this.tlv_bytes[offset]&0xFF);		
	}

	public int getProtection_type() {
		return protection_type;
	}

	public void setProtection_type(int protection_type) {
		this.protection_type = protection_type;
	}
	
	public String toString(){
		return "PROTECTION TYPE [type=" + this.getProtection_type() + "]";
	}

}
