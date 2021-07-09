package es.tid.pce.pcep.objects;

import java.util.LinkedList;
import java.util.logging.Logger;

import es.tid.rsvp.objects.subobjects.*;

/** Explicit Route Object
 * The ERO is used to encode the path of a TE LSP through the network.
 * The ERO is carried within a PCRep message to provide the computed TE
 * LSP if the path computation was successful.
 *
 * The contents of this object are identical in encoding to the contents
 * of the Resource Reservation Protocol Traffic Engineering Extensions
 * (RSVP-TE) Explicit Route Object (ERO) defined in [RFC3209],
 * [RFC3473], and [RFC3477].  That is, the object is constructed from a
 * series of sub-objects.  Any RSVP-TE ERO sub-object already defined or
 * that could be defined in the future for use in the RSVP-TE ERO is
 * acceptable in this object.
 * PCEP ERO sub-object types correspond to RSVP-TE ERO sub-object types.
 *
 * Since the explicit path is available for immediate signaling by the
 * MPLS or GMPLS control plane, the meanings of all of the sub-objects
 * and fields in this object are identical to those defined for the ERO.
 *
 * ERO Object-Class is 7.
 *
 * ERO Object-Type is 1.
 *
 * RFC 3209:
 *   0                   1                   2                   3
 *    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *  |                                                               |
 *  //                        (Subobjects)                          //
 *  |                                                               |
 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * @author Oscar Gonzalez de Dios (ogondio@tid.es)
 */
public class ExplicitRouteObject extends PCEPObject{
	
	public LinkedList<EROSubobject> EROSubobjectList;
	
	//Constructors

	public ExplicitRouteObject() {
		super();
		this.setObjectClass(ObjectParameters.PCEP_OBJECT_CLASS_ERO);
		this.setOT(ObjectParameters.PCEP_OBJECT_TYPE_ERO);
		EROSubobjectList=new LinkedList<EROSubobject>();
	}
	
	/**
	 * Constructs a new ERO (Explicit Route Object) from a sequence of bytes
	 * @param bytes Sequence of bytes where the object is present
	 * @param offset Position at which the object starts
	 * @throws MalformedPCEPObjectException Thrown if the decoded object is not well formed
	 */
	public ExplicitRouteObject (byte []bytes, int offset)throws MalformedPCEPObjectException{
		super(bytes, offset);
		EROSubobjectList=new LinkedList<EROSubobject>();
		decode();
	}
	
	//Encode and Decode
	
	/**
	 * Encode Explicit Route Object
	 */
	public void encode() {
		int len=4;//The four bytes of the header
		for (int k=0; k<EROSubobjectList.size();k=k+1){
			EROSubobjectList.get(k).encode();			
			len=len+EROSubobjectList.get(k).getErosolength();
		}
		ObjectLength=len;
		this.object_bytes=new byte[ObjectLength];
		encode_header();
		int pos=4;
		for (int k=0 ; k<EROSubobjectList.size(); k=k+1) {					
			System.arraycopy(EROSubobjectList.get(k).getSubobject_bytes(),0, this.object_bytes, pos, EROSubobjectList.get(k).getErosolength());
			pos=pos+EROSubobjectList.get(k).getErosolength();
		}				
	}

	/**
	 * Decodes Explicit Route Object
	 */
	public void decode() throws MalformedPCEPObjectException{
		boolean fin=false;
		int offset=4;//Position of the next subobject
		if (ObjectLength==4){
			fin=true;
		}
		while (!fin) {
			int subojectclass=EROSubobject.getType(this.getObject_bytes(), offset);
			int subojectlength=EROSubobject.getLength(this.getObject_bytes(), offset);
			switch(subojectclass) {
			/*		case SubObjectValues.ERO_SUBOBJECT_SR_ERO:
					SREROSubobject sreroso = new SREROSubobject(this.getObject_bytes(), offset);
					this.addEROSubobject(sreroso);
					break;*/
				case SubObjectValues.ERO_SUBOBJECT_IPV4PREFIX:
					IPv4prefixEROSubobject sobjt4=new IPv4prefixEROSubobject(this.getObject_bytes(), offset);
					this.addEROSubobject(sobjt4);
					break;
			
				case SubObjectValues.ERO_SUBOBJECT_IPV6PREFIX:
					IPv6prefixEROSubobject sobjt6=new IPv6prefixEROSubobject(this.getObject_bytes(), offset);
					addEROSubobject(sobjt6);
					break;		
				
				case SubObjectValues.ERO_SUBOBJECT_ASNUMBER:
					ASNumberEROSubobject sobjas=new ASNumberEROSubobject (this.getObject_bytes(), offset);
					addEROSubobject(sobjas);
					break;
				
				case SubObjectValues.ERO_SUBOBJECT_UNNUMBERED_IF_ID:
					UnnumberIfIDEROSubobject subun=new UnnumberIfIDEROSubobject(this.getObject_bytes(), offset);
					addEROSubobject(subun);
					break;
					
				case SubObjectValues.ERO_SUBOBJECT_DATAPATH_ID:
					DataPathIDEROSubobject subdp=new DataPathIDEROSubobject(this.getObject_bytes(), offset);
					addEROSubobject(subdp);
					break;
					
				case SubObjectValues.ERO_SUBOBJECT_UNNUMBERED_DATAPATH_ID:
					UnnumberedDataPathIDEROSubobject subudp=new UnnumberedDataPathIDEROSubobject(this.getObject_bytes(), offset);
					addEROSubobject(subudp);
					break;
					
					
				case SubObjectValues.ERO_SUBOBJECT_LAYER_INFO:
					ServerLayerInfo sli =new ServerLayerInfo(this.getObject_bytes(), offset);
					addEROSubobject(sli);
					break;
					
				case SubObjectValues.ERO_SUBOBJECT_SWITCH_ID:
					SwitchIDEROSubobject macS =new SwitchIDEROSubobject(this.getObject_bytes(), offset);
					addEROSubobject(macS);
					break;
					
				case SubObjectValues.ERO_SUBOBJECT_SWITCH_ID_EDGE:
					SwitchIDEROSubobjectEdge macSE =new SwitchIDEROSubobjectEdge(this.getObject_bytes(), offset);
					addEROSubobject(macSE);
					break;
				
				case SubObjectValues.ERO_SUBOBJECT_UNNUMBERED_IF_ID_OPEN_FLOW:
					OpenFlowUnnumberIfIDEROSubobject unifOF =new OpenFlowUnnumberIfIDEROSubobject(this.getObject_bytes(), offset);
					addEROSubobject(unifOF);
					break;
					
				case SubObjectValues.ERO_SUBOBJECT_ID_OPEN_FLOW:
					OpenFlowIDEROSubobject idOF =new OpenFlowIDEROSubobject(this.getObject_bytes(), offset);
					addEROSubobject(idOF);
					break;
					
				case SubObjectValues.ERO_SUBOBJECT_ETC:
					ETCEROSubobject etcSERO =new ETCEROSubobject(this.getObject_bytes(), offset);
					addEROSubobject(etcSERO);
					break;
					
				case SubObjectValues.ERO_SUBOBJECT_LABEL:
					int ctype=LabelEROSubobject.getCType(this.getObject_bytes(), offset);
					switch (ctype){
					
						case SubObjectValues.ERO_SUBOBJECT_LABEL_CTYPE_GENERALIZED_LABEL:
							GeneralizedLabelEROSubobject subgl=new GeneralizedLabelEROSubobject(this.getObject_bytes(), offset);
							addEROSubobject(subgl);
							break;
						case SubObjectValues.ERO_SUBOBJECT_LABEL_CTYPE_WAVEBAND_LABEL:
							WavebandLabelEROSubobject subwl=new WavebandLabelEROSubobject(this.getObject_bytes(), offset);
							addEROSubobject(subwl);
							break;
						case SubObjectValues.ERO_SUBOBJECT_LABEL_CTYPE_OBS_MAINS_LABEL :
							OBSMAINSLabelEROSubobject oles=new OBSMAINSLabelEROSubobject(this.getObject_bytes(), offset);
							addEROSubobject(oles);
							break;	
						default:
							log.warn("ERO LABEL Subobject Ctype Unknown");
							break;							
					}
					break;
				default:
					log.warn("ERO Subobject Unknown subojectclass: "+subojectclass);
					//FIXME What do we do??
					break;
			}
			offset=offset+subojectlength;
			if (offset>=ObjectLength){
				fin=true;
			}
		}
		
	}
	
	//Getters and setters
	
	//FIXME: Ver si es mejor Vector o LInkedList, y A�ADIR MAS METODOS UTILES, estos son escasos.
	
	public void addEROSubobject(EROSubobject eroso){
		EROSubobjectList.add(eroso);
	}
	
	public void addEROSubobjectList(LinkedList<EROSubobject> erosovec){
		EROSubobjectList.addAll(erosovec);
	}

	public LinkedList<EROSubobject> getEROSubobjectList() {
		return EROSubobjectList;
	}

	public void setEROSubobjectList(LinkedList<EROSubobject> eROSubobjectList) {
		EROSubobjectList = eROSubobjectList;
	}
	

	public String toString(){
		StringBuffer sb=new StringBuffer(EROSubobjectList.size()*100);
		sb.append("<ERO: ");
		for (int i=0;i<EROSubobjectList.size();++i){
			sb.append(EROSubobjectList.get(i).toString());
			sb.append(" ");
		}
		sb.append(">");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime
				* result
				+ ((EROSubobjectList == null) ? 0 : EROSubobjectList.hashCode());
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
		ExplicitRouteObject other = (ExplicitRouteObject) obj;
		if (EROSubobjectList == null) {
			if (other.EROSubobjectList != null)
				return false;
		} else if (!EROSubobjectList.equals(other.EROSubobjectList))
			return false;

		return true;
	}
	
	


}
