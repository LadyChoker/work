package es.tid.pce.pcep.objects.subobjects;

import java.util.Arrays;

public abstract class XROSubobject {

	protected int type;
	protected int erosolength;//ERO Subobject Length
	protected boolean loosehop=false;
	protected byte [] subobject_bytes;
	
	protected int attribute;
	
	public XROSubobject(){
		
	}
	
	public XROSubobject(byte[] bytes, int offset) {
		erosolength=(int)bytes[offset+1];
		this.subobject_bytes=new byte[erosolength];
		System.arraycopy(bytes, offset, subobject_bytes, 0, erosolength);
		decodeSoHeader();
	}
	public abstract void encode();

	public abstract void decode();
	
	public void encodeSoHeader(){
		if (loosehop){
			subobject_bytes[0]=(byte)(0x80 | (type & 0x7F));
		}
		else {
			subobject_bytes[0]=(byte)(type & 0x7F);
		}
			subobject_bytes[1]=(byte)erosolength;		
	}
	
	public void decodeSoHeader() {
		int lhopbit= (int)((subobject_bytes[0]>>7)&0x01);
		if (lhopbit==1){
			loosehop=true;
		}
		else {
			loosehop=false;
		}
		type=subobject_bytes[0]&0x7F;
		erosolength=(int)subobject_bytes[1];
	}	
		
	
	public static int getLength(byte []bytes, int offset) {
		int len=(int)bytes[offset+1];
		return len;
	}
	
	public static int getType(byte []bytes, int offset) {
		int typ=bytes[offset]& 0x7F;
		return typ;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getErosolength() {
		return erosolength;
	}

	public void setErosolength(int erosolength) {
		this.erosolength = erosolength;
	}

	public boolean isLoosehop() {
		return loosehop;
	}

	public void setLoosehop(boolean loosehop) {
		this.loosehop = loosehop;
	}

	public byte[] getSubobject_bytes() {
		return subobject_bytes;
	}

	public void setSubobject_bytes(byte[] subobject_bytes) {
		this.subobject_bytes = subobject_bytes;
	}

	public int getAttribute() {
		return attribute;
	}

	public void setAttribute(int attribute) {
		this.attribute = attribute;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + attribute;
		result = prime * result + erosolength;
		result = prime * result + (loosehop ? 1231 : 1237);
		result = prime * result + Arrays.hashCode(subobject_bytes);
		result = prime * result + type;
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
		XROSubobject other = (XROSubobject) obj;
		if (attribute != other.attribute)
			return false;
		if (erosolength != other.erosolength)
			return false;
		if (loosehop != other.loosehop)
			return false;
		if (!Arrays.equals(subobject_bytes, other.subobject_bytes))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
	
	
}
