package es.tid.rsvp.objects.subobjects;

import java.util.LinkedList;

import es.tid.rsvp.objects.subobjects.subtlvs.SubTLV;
import es.tid.rsvp.objects.subobjects.subtlvs.SubTLVTypes;
import es.tid.rsvp.objects.subobjects.subtlvs.SubTransponderTLV;
import es.tid.rsvp.objects.subobjects.subtlvs.SubTransponderTLVFEC;
import es.tid.rsvp.objects.subobjects.subtlvs.SubTransponderTLVFS;
import es.tid.rsvp.objects.subobjects.subtlvs.SubTransponderTLVID;
import es.tid.rsvp.objects.subobjects.subtlvs.SubTransponderTLVModFormat;
import es.tid.rsvp.objects.subobjects.subtlvs.SubTransponderTLVTC;

public class ETCEROSubobject extends EROSubobject {

	private LinkedList<SubTransponderTLV> subTransponderList;
	
	public ETCEROSubobject(){
		super();
		this.setType(SubObjectValues.ERO_SUBOBJECT_ETC);
		subTransponderList=new LinkedList<SubTransponderTLV>();
	}
	
	public ETCEROSubobject (byte [] bytes, int offset){
		super(bytes, offset);
		subTransponderList=new LinkedList<SubTransponderTLV>();
		decode();
	}
	
	
	@Override
	public void encode() {
		// TODO Auto-generated method stub
		int len=4;//The four bytes of the header
		
		if (subTransponderList != null){
		
			for (int k=0; k<subTransponderList.size();k=k+1){
			subTransponderList.get(k).encode();			
			len=len+subTransponderList.get(k).getTotalTLVLength();
			}
			
		}
		
		erosolength=len;
		this.subobject_bytes=new byte[erosolength];
		encodeSoHeader();
		int pos=4;
		
		if (subTransponderList != null){
		
			for (int k=0 ; k<subTransponderList.size(); k=k+1) {					
			System.arraycopy(subTransponderList.get(k).getTlv_bytes(),0, this.subobject_bytes, pos, subTransponderList.get(k).getTotalTLVLength());
			pos=pos+subTransponderList.get(k).getTotalTLVLength();
			}	
			
		}
	}

	@Override
	public void decode() {
		//Decoding SubTransponderTLV
				boolean fin=false;
				int offset=4;//Position of the next subobject

				while (!fin) {
				
					int subtlvType=SubTLV.getType(subobject_bytes, offset);
					int subtlvLength=SubTLV.getTotalTLVLength(subobject_bytes, offset);
					
					switch(subtlvType) {
					
						case SubTLVTypes.ERO_SUBTLV_SUBTRANSPONDER:
							System.out.println("ERO_SUBTLV_SUBTRANSPONDER FOUND");
							SubTransponderTLV a = new SubTransponderTLV(this.subobject_bytes, offset);
							subTransponderList.add(a);
						break;		
									
						default:
							System.out.println( "xx SubTransponderTLV Unknown ");
							break;
					}
					
					offset=offset+subtlvLength;
					
					if (offset>=this.erosolength){
						fin=true;
					}
					else{
						System.out.println("sigo leyendo SubTransponderTLV ");
					}
				}
				
	}

	public LinkedList<SubTransponderTLV> getSubTransponderList() {
		return subTransponderList;
	}

	public void setSubTransponderList(
			LinkedList<SubTransponderTLV> subTransponderList) {
		this.subTransponderList = subTransponderList;
	}
	
	
	public String toString(){
		StringBuffer sb=new StringBuffer(subTransponderList.size()*100);
		sb.append("<ETC: ");
		for (int i=0;i<subTransponderList.size();++i){
			sb.append(subTransponderList.get(i).toString());
			sb.append(" ");
		}
		sb.append(">");
		return sb.toString();
	}
	

}
