package es.tid.pce.computingEngine;

import java.io.DataOutputStream;
import java.net.Inet4Address;
import java.util.LinkedList;

import es.tid.pce.pcep.constructs.PCEPIntiatedLSP;
import es.tid.pce.pcep.constructs.Request;
import es.tid.pce.pcep.constructs.SVECConstruct;
import es.tid.pce.pcep.messages.PCEPMessageTypes;
import es.tid.pce.pcep.objects.Monitoring;
import es.tid.pce.pcep.objects.PccReqId;
import es.tid.pce.pcep.objects.tlvs.SymbolicPathNameTLV;

/**
 * 
 * 
 *
 */


public class ComputingRequest 
{
	
	private SVECConstruct svec;
	
	private LinkedList<SVECConstruct> SvecList;
	
	private LinkedList<Request> requestList;
	
	private Monitoring monitoring;
	
	private PccReqId pccReqId;
	
	private Inet4Address remotePCEId;
	
	private long timeStampNs;
	
	private long maxTimeInPCE=120000;//Por defecto, dos minutos...
	
	private int encodinType = PCEPMessageTypes.MESSAGE_PCREP;
	
	
	private  PCEPIntiatedLSP iniLSP;
	
	
	
	public PCEPIntiatedLSP getIniLSP() {
		return iniLSP;
	}

	public void setIniLSP(PCEPIntiatedLSP iniLSP) {
		this.iniLSP = iniLSP;
	}

	/**
	 * DataOutputStream to send the response to the peer PCC
	 */

	private DataOutputStream out=null; 
	
	public SVECConstruct getSvec() 
	{
		return svec;
	}

	public void setSvec(SVECConstruct svec) 
	{
		this.svec = svec;
	}

	public LinkedList<SVECConstruct> getSvecList() 
	{
		return SvecList;
	}

	public void setSvecList(LinkedList<SVECConstruct> svecList) 
	{
		SvecList = svecList;
	}

	public LinkedList<Request> getRequestList() 
	{
		return requestList;
	}

	public void setRequestList(LinkedList<Request> requestList)
	{
		this.requestList = requestList;
	}

	public DataOutputStream getOut()
	{
		return out;
	}

	public void setOut(DataOutputStream out)
	{
		this.out = out;
	}

	public Monitoring getMonitoring()
	{
		return monitoring;
	}

	public void setMonitoring(Monitoring monitoring)
	{
		this.monitoring = monitoring;
	}

	public PccReqId getPccReqId()
	{
		return pccReqId;
	}

	public void setPccReqId(PccReqId pccReqId)
	{
		this.pccReqId = pccReqId;
	}

	public long getTimeStampNs()
	{
		return timeStampNs;
	}

	public void setTimeStampNs(long timeStampNs) 
	{
		this.timeStampNs = timeStampNs;
	}

	public long getMaxTimeInPCE()
	{
		return maxTimeInPCE;
	}

	public void setMaxTimeInPCE(long maxTimeInPCE)
	{
		this.maxTimeInPCE = maxTimeInPCE;
	}

	public Inet4Address getRemotePCEId() {
		return remotePCEId;
	}



	public void setRemotePCEId(Inet4Address remotePCEId) {
		this.remotePCEId = remotePCEId;
	}

	public int getEcodingType() 
	{
		return encodinType;
	}

	public void getEcodingType(int isInitiate) 
	{
		this.encodinType = isInitiate;
	}
}
