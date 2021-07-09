package es.tid.bgp.bgp4.update.tlv;

import java.util.ArrayList;
import org.slf4j.Logger;

import es.tid.bgp.bgp4.update.tlv.node_link_prefix_descriptor_subTLVs.AreaIDNodeDescriptorSubTLV;
import es.tid.bgp.bgp4.update.tlv.node_link_prefix_descriptor_subTLVs.AutonomousSystemNodeDescriptorSubTLV;
import es.tid.bgp.bgp4.update.tlv.node_link_prefix_descriptor_subTLVs.BGP4SubTLV;
import es.tid.bgp.bgp4.update.tlv.node_link_prefix_descriptor_subTLVs.BGPLSIdentifierNodeDescriptorSubTLV;
import es.tid.bgp.bgp4.update.tlv.node_link_prefix_descriptor_subTLVs.IGPRouterIDNodeDescriptorSubTLV;
import es.tid.bgp.bgp4.update.tlv.node_link_prefix_descriptor_subTLVs.NodeDescriptorsSubTLV;
import es.tid.bgp.bgp4.update.tlv.node_link_prefix_descriptor_subTLVs.NodeDescriptorsSubTLVTypes;
import org.slf4j.LoggerFactory;

/**
 *    The Remote Node Descriptors TLV (Type 257) contains Node Descriptors
   for the node anchoring the remote end of the link.  The length of
   this TLV is variable.  The value contains one or more Node Descriptor
   Sub-TLVs defined in Section 3.2.1.3.

      0                   1                   2                   3
      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |              Type             |             Length            |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |                                                               |
     |               Node Descriptor Sub-TLVs (variable)             |
     |                                                               |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

               Figure 10: Remote Node Descriptors TLV format
               
 * @author mcs
 *
 */
public class RemoteNodeDescriptorsTLV extends BGP4TLVFormat {

public static final int Remote_Node_Descriptors_TLV = 257;

private AutonomousSystemNodeDescriptorSubTLV autonomousSystemSubTLV; //512
private BGPLSIdentifierNodeDescriptorSubTLV BGPLSIDSubTLV; //513
private AreaIDNodeDescriptorSubTLV AreaID; //514
private IGPRouterIDNodeDescriptorSubTLV IGPRouterID; //515

	
	public RemoteNodeDescriptorsTLV(){
		super();
		this.setTLVType(RemoteNodeDescriptorsTLV.Remote_Node_Descriptors_TLV);
		//nodeDescriptorsSubTLVList=new ArrayList<NodeDescriptorsSubTLV>();
	}

  private static Logger log = LoggerFactory.getLogger("BGP4Parser");

	public RemoteNodeDescriptorsTLV(byte []bytes, int offset) {		
		super(bytes, offset);
		decode();
	}
	
	public void encode(){		
		
		int len = 0;//Header TLV
		
		if (autonomousSystemSubTLV != null){
			autonomousSystemSubTLV.encode();
			len = len + autonomousSystemSubTLV.getTotalSubTLVLength();
		}
		if (BGPLSIDSubTLV != null){
			BGPLSIDSubTLV.encode();
			len = len + BGPLSIDSubTLV.getTotalSubTLVLength();
		}
		if (AreaID != null){
			AreaID.encode();
			len = len + AreaID.getTotalSubTLVLength();
		}
		if (IGPRouterID != null){
			IGPRouterID.encode();
			len = len + IGPRouterID.getTotalSubTLVLength();
		}
		
				
		this.setTLVValueLength(len);		
		this.setTlv_bytes(new byte[this.getTotalTLVLength()]);
		encodeHeader();
		int offset=4;//Header TLV
	
		
		if (autonomousSystemSubTLV != null){
			System.arraycopy(autonomousSystemSubTLV.getSubTLV_bytes(),0,this.tlv_bytes,offset,autonomousSystemSubTLV.getTotalSubTLVLength());			
			offset=offset+autonomousSystemSubTLV.getTotalSubTLVLength();
		}
		if (BGPLSIDSubTLV != null){
			System.arraycopy(BGPLSIDSubTLV.getSubTLV_bytes(),0,this.tlv_bytes,offset,BGPLSIDSubTLV.getTotalSubTLVLength());			
			offset=offset+BGPLSIDSubTLV.getTotalSubTLVLength();
		}
		if (AreaID != null){
			System.arraycopy(AreaID.getSubTLV_bytes(),0,this.tlv_bytes,offset,AreaID.getTotalSubTLVLength());			
			offset=offset+AreaID.getTotalSubTLVLength();
		}		
		if (IGPRouterID != null){
			System.arraycopy(IGPRouterID.getSubTLV_bytes(),0,this.tlv_bytes,offset,IGPRouterID.getTotalSubTLVLength());			
			offset=offset+IGPRouterID.getTotalSubTLVLength();
		}
		
		
	}
	public void decode(){
		//Decoding RemoteNodeDescriptorsTLV
		boolean fin=false;
		int offset=4;
		while (!fin) {
			int subtlvType=BGP4SubTLV.getType(tlv_bytes, offset);
			int subtlvLength=BGP4SubTLV.getTotalSubTLVLength(tlv_bytes, offset);
			switch(subtlvType) {
				case NodeDescriptorsSubTLVTypes.NODE_DESCRIPTORS_SUBTLV_TYPE_AUTONOMOUS_SYSTEM:
					autonomousSystemSubTLV=new AutonomousSystemNodeDescriptorSubTLV(this.tlv_bytes, offset);
					break;		
				
				case NodeDescriptorsSubTLVTypes.NODE_DESCRIPTORS_SUBTLV_TYPE_BGP_LS_IDENTIFIER:
					BGPLSIDSubTLV=new BGPLSIdentifierNodeDescriptorSubTLV(this.tlv_bytes, offset);
					break;
				
				case NodeDescriptorsSubTLVTypes.NODE_DESCRIPTORS_SUBTLV_TYPE_AREA_ID:
					AreaID = new AreaIDNodeDescriptorSubTLV(this.tlv_bytes, offset);
					break;
				
				case NodeDescriptorsSubTLVTypes.NODE_DESCRIPTORS_SUBTLV_TYPE_IGP_ROUTER_ID:
					IGPRouterID = new IGPRouterIDNodeDescriptorSubTLV(this.tlv_bytes, offset);
					break;
					
				default:
					log.warn("Remote Node Descriptor Unknown, subtlvType: " + subtlvType);
					break;
			}
			
			offset=offset+subtlvLength;
			if (offset>=this.TLVValueLength){
				fin=true;
			}
		}
	}

	
	public AutonomousSystemNodeDescriptorSubTLV getAutonomousSystemSubTLV() {
		return autonomousSystemSubTLV;
	}

	public void setAutonomousSystemSubTLV(
			AutonomousSystemNodeDescriptorSubTLV autonomousSystemSubTLV) {
		this.autonomousSystemSubTLV = autonomousSystemSubTLV;
	}

	public BGPLSIdentifierNodeDescriptorSubTLV getBGPLSIDSubTLV() {
		return BGPLSIDSubTLV;
	}

	public void setBGPLSIDSubTLV(BGPLSIdentifierNodeDescriptorSubTLV bGPLSIDSubTLV) {
		BGPLSIDSubTLV = bGPLSIDSubTLV;
	}

	public AreaIDNodeDescriptorSubTLV getAreaID() {
		return AreaID;
	}

	public void setAreaID(AreaIDNodeDescriptorSubTLV areaID) {
		AreaID = areaID;
	}

	public IGPRouterIDNodeDescriptorSubTLV getIGPRouterID() {
		return IGPRouterID;
	}

	public void setIGPRouterID(IGPRouterIDNodeDescriptorSubTLV iGPRouterID) {
		IGPRouterID = iGPRouterID;
	}

	public static int getRemoteNodeDescriptorsTlv() {
		return Remote_Node_Descriptors_TLV;
	}

	@Override
	public String toString() {
		
		StringBuffer sb=new StringBuffer(1000);
		
		if (autonomousSystemSubTLV != null)
			sb.append("\n\t> "+autonomousSystemSubTLV.toString()+"\n");
			
		if (BGPLSIDSubTLV != null)
			sb.append("\n\t> "+BGPLSIDSubTLV.toString()+"\n");
			
		if (AreaID != null)
			sb.append("\n\t> "+AreaID.toString()+"\n");
		
		if (IGPRouterID != null)
			sb.append("\n\t> "+IGPRouterID.toString()+"\n");
		
		return sb.toString();
	}
	
}
