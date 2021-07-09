package es.tid.pce.pcep.constructs;

import java.util.LinkedList;

import es.tid.pce.pcep.PCEPProtocolViolationException;
import es.tid.pce.pcep.objects.*;


/**
 * LSP Instantiation Request
 * {@code 
 <lsp-instantiation-request> ::= <END-POINTS>
                                   <LSPA>
                                   [<ERO>]
                                   [<BANDWIDTH>]
                                   [<metric-list>]}
 * 
 * @author ogondio
 *
 */
public class LSPInstantationRequest extends PCEPConstruct{

	private EndPoints endPoints;//COMPULSORY!!!
	private LSPA lSPA;//COMPULSORY!!
	private ExplicitRouteObject eRO;
	private Bandwidth bandwidth;
	private LinkedList<Metric> metricList;
		
	/**
	 * Default constructor. 
	 * Use this method to create a new Request from scratch
	 */
	public LSPInstantationRequest(){
		this.metricList=new LinkedList<Metric>();
	}
	
	/**
	 * 
	 * Use this method to create a new Request from a sequence of bytes	 
	 * @param bytes bytes
	 * @param offset offset
	 * @throws PCEPProtocolViolationException Exception when the bytes do not lead to a valid PCEP Request Construct 
	 */
	public LSPInstantationRequest(byte[] bytes, int offset) throws PCEPProtocolViolationException{
		this.metricList=new LinkedList<Metric>();
		decode(bytes,offset);
	}

	public void encode() throws PCEPProtocolViolationException{
		int len=0;
		
		if (endPoints!=null){
			endPoints.encode();
			len=len+endPoints.getLength();
		}
		else {
			log.warn("EndPoints not found! They are compulsory");
			throw new PCEPProtocolViolationException();
		}
		if (lSPA!=null){
			lSPA.encode();
			len=len+lSPA.getLength();
		}
		else{
			log.warn("lSPA not found! It is compulsory");
			throw new PCEPProtocolViolationException();
		}

		if (eRO!=null)
		{	
			eRO.encode();
			len += eRO.getLength();

		}
		if (bandwidth!=null){
			bandwidth.encode();
			len=len+bandwidth.getLength();
		}
		if (metricList!=null){
			for (int i=0;i<metricList.size();++i){
				(metricList.get(i)).encode();
				len=len+(metricList.get(i)).getLength();
			}
		}
		
		this.setLength(len);
		bytes=new byte[len];
		int offset=0;

		System.arraycopy(endPoints.getBytes(), 0, bytes, offset, endPoints.getLength());
		offset=offset+endPoints.getLength();

		System.arraycopy(lSPA.getBytes(), 0, bytes, offset, lSPA.getLength());
		offset=offset+lSPA.getLength();
		if (eRO!=null)
		{
			System.arraycopy(eRO.getBytes(), 0, this.getBytes(), offset, eRO.getLength());
			offset=offset + eRO.getLength();
		}
		if (bandwidth!=null){
			System.arraycopy(bandwidth.getBytes(), 0, bytes, offset, bandwidth.getLength());
			offset=offset+bandwidth.getLength();
		}
		if (metricList!=null){
			for (int i=0;i<metricList.size();++i){
				System.arraycopy(metricList.get(i).getBytes(), 0, bytes, offset, metricList.get(i).getLength());
				offset=offset+metricList.get(i).getLength();
			}
		}
	}

	/**
	 * Decode a Request rule;
	 * @param bytes bytes
	 * @param offset offset
	 * @throws PCEPProtocolViolationException Exception when the bytes do not lead to a valid PCEP Request Construct 
	 */
	private void decode(byte[] bytes, int offset) throws PCEPProtocolViolationException{
		int len=0;		
		int max_offset=bytes.length;
		if (offset>=max_offset){
			log.warn("Empty Request construct!!!");
			throw new PCEPProtocolViolationException();
		}
		// END-POINTS
		int oc=PCEPObject.getObjectClass(bytes, offset);
		if (oc==ObjectParameters.PCEP_OBJECT_CLASS_ENDPOINTS){
			int ot=PCEPObject.getObjectType(bytes, offset);
			if (ot==ObjectParameters.PCEP_OBJECT_TYPE_ENDPOINTS_IPV4){
				try {
					endPoints=new EndPointsIPv4(bytes,offset);
				} catch (MalformedPCEPObjectException e) {
					log.warn("Malformed ENDPOINTS IPV4 Object found");
					throw new PCEPProtocolViolationException();
				}
			}
			else if (ot==ObjectParameters.PCEP_OBJECT_TYPE_ENDPOINTS_IPV6){
				try {
					endPoints=new EndPointsIPv6(bytes,offset);
				} catch (MalformedPCEPObjectException e) {
					log.warn("Malformed ENDPOINTSIPV6 Object found");
					throw new PCEPProtocolViolationException();
				}
			}
			else if (ot==ObjectParameters.PCEP_OBJECT_TYPE_GENERALIZED_ENDPOINTS){
				try {
					int endPointType=GeneralizedEndPoints.getGeneralizedEndPointsType(bytes,offset);
					if (endPointType==1) {
						endPoints=new P2PGeneralizedEndPoints(bytes,offset);	
					}
					
				} catch (MalformedPCEPObjectException e) {
					log.warn("Malformed GENERALIZED END POINTS Object found");
					throw new PCEPProtocolViolationException();
				}
			}
			else {
				log.warn("ENDPOINTS TYPE NOT SUPPORTED");
				throw new PCEPProtocolViolationException();
			}
			offset=offset+endPoints.getLength();
			len=len+endPoints.getLength();
			if (offset>=max_offset){
				this.setLength(len);
				return;
			}
		}
		else {
			log.warn("LSPInstantationRequest must start with ENDPOINTS");
			throw new PCEPProtocolViolationException();
		}
		// LSPA
		oc=PCEPObject.getObjectClass(bytes, offset);		
		if (oc==ObjectParameters.PCEP_OBJECT_CLASS_LSPA){
			try {
				lSPA=new LSPA(bytes,offset);
			} catch (MalformedPCEPObjectException e) {
				log.warn("Malformed LSPA Object found");
				throw new PCEPProtocolViolationException();
			}
			offset=offset+lSPA.getLength();
			len=len+lSPA.getLength();
			if (offset>=max_offset){
				this.setLength(len);
				return;
			}
		}else{
			log.warn("LSPInstantationRequest follow with an LSPA after the ENDPOINTS");
		}
		// ERO
		oc=PCEPObject.getObjectClass(bytes, offset);
		if (oc==ObjectParameters.PCEP_OBJECT_CLASS_ERO){
			try {
				eRO=new ExplicitRouteObject(bytes, offset);
			}catch (MalformedPCEPObjectException e){
				log.warn("Malformed ERO Object found");
				throw new PCEPProtocolViolationException();
			}
			offset=offset+eRO.getLength();
			len=len+eRO.getLength();
			if (offset>=max_offset){
				this.setLength(len);
				return;
			}
		}
		
		// Bandwidth
		oc=PCEPObject.getObjectClass(bytes, offset);
		if (oc==ObjectParameters.PCEP_OBJECT_CLASS_BANDWIDTH){
			try {
				bandwidth=new BandwidthRequested(bytes,offset);
			} catch (MalformedPCEPObjectException e) {
				log.warn("Malformed BANDWIDTH Object found");
				throw new PCEPProtocolViolationException();
			}
			offset=offset+bandwidth.getLength();
			len=len+bandwidth.getLength();
			if (offset>=max_offset){
				this.setLength(len);
				return;
			}
		}
		// Metric List
		oc=PCEPObject.getObjectClass(bytes, offset);
		while (oc==ObjectParameters.PCEP_OBJECT_CLASS_METRIC){
			Metric metric;
			try {
				metric = new Metric(bytes,offset);
			} catch (MalformedPCEPObjectException e) {
				log.warn("Malformed METRIC Object found");
				throw new PCEPProtocolViolationException();
			}
			metricList.add(metric);
			offset=offset+metric.getLength();
			len=len+metric.getLength();
			if (offset>=max_offset){
				this.setLength(len);
				return;
			}
			oc=PCEPObject.getObjectClass(bytes, offset);
		}
		this.setLength(len);
	}
	
	public EndPoints getEndPoints() {
		return endPoints;
	}

	public void setEndPoints(EndPoints endPoints) {
		this.endPoints = endPoints;
	}

	public LSPA getLSPA() {
		return lSPA;
	}

	public void setLSPA(LSPA lSPA) {
		this.lSPA = lSPA;
	}
	


	public ExplicitRouteObject getERO() {
		return eRO;
	}

	public void setERO(ExplicitRouteObject eRO) {
		this.eRO = eRO;
	}

	public Bandwidth getBandwidth() {
		return bandwidth;
	}

	public void setBandwidth(Bandwidth bandwidth) {
		this.bandwidth = bandwidth;
	}

	public LinkedList<Metric> getMetricList() {
		return metricList;
	}

	public void setMetricList(LinkedList<Metric> metricList) {
		this.metricList = metricList;
	}

	public String toString(){
		StringBuffer sb=new StringBuffer();
		
		if (endPoints!=null){
			sb.append(endPoints);
		}
		if (lSPA!=null){
			sb.append(lSPA);
		}
		if (eRO!=null){
			sb.append(eRO.toString());
		}
		if (bandwidth!=null){
			sb.append(bandwidth.toString());
		}
		if (metricList!=null){
			for (int i=0;i<metricList.size();++i){
				sb.append(metricList.get(i).toString());
			}
		}
		return sb.toString();
	}
	
	public LSPInstantationRequest duplicate(){
		LSPInstantationRequest req=new LSPInstantationRequest();
		req.setEndPoints(this.endPoints);
		req.setLSPA(this.lSPA);
		req.setERO(this.eRO);
		req.setBandwidth(this.bandwidth);
		req.setMetricList(this.metricList);
		return req;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((bandwidth == null) ? 0 : bandwidth.hashCode());
		result = prime * result + ((eRO == null) ? 0 : eRO.hashCode());
		result = prime * result
				+ ((endPoints == null) ? 0 : endPoints.hashCode());
		result = prime * result + ((lSPA == null) ? 0 : lSPA.hashCode());
		result = prime * result
				+ ((metricList == null) ? 0 : metricList.hashCode());
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
		LSPInstantationRequest other = (LSPInstantationRequest) obj;
		if (bandwidth == null) {
			if (other.bandwidth != null)
				return false;
		} else if (!bandwidth.equals(other.bandwidth))
			return false;
		if (eRO == null) {
			if (other.eRO != null)
				return false;
		} else if (!eRO.equals(other.eRO))
			return false;
		if (endPoints == null) {
			if (other.endPoints != null)
				return false;
		} else if (!endPoints.equals(other.endPoints))
			return false;
		if (lSPA == null) {
			if (other.lSPA != null)
				return false;
		} else if (!lSPA.equals(other.lSPA))
			return false;
		if (metricList == null) {
			if (other.metricList != null)
				return false;
		} else if (!metricList.equals(other.metricList))
			return false;

		return true;
	}
	
	
}
