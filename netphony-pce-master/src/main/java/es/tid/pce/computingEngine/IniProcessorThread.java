package es.tid.pce.computingEngine;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.tid.pce.computingEngine.algorithms.ComputingAlgorithm;
import es.tid.pce.computingEngine.algorithms.ComputingAlgorithmManager;
import es.tid.pce.computingEngine.algorithms.ComputingAlgorithmManagerSSON;
import es.tid.pce.computingEngine.algorithms.DefaultSVECPathComputing;
import es.tid.pce.computingEngine.algorithms.DefaultSinglePathComputing;
import es.tid.pce.computingEngine.algorithms.multiLayer.OperationsCounter;
import es.tid.pce.parentPCE.ChildPCERequestManager;
import es.tid.pce.parentPCE.MD_LSP;
import es.tid.pce.parentPCE.ParentPCESession;
import es.tid.pce.parentPCE.MDLSPDB.MultiDomainLSPDB;
import es.tid.pce.parentPCE.MDLSPDB.SaveLSPinRedis;
import es.tid.pce.pcep.PCEPProtocolViolationException;
import es.tid.pce.pcep.constructs.EndPoint;
import es.tid.pce.pcep.constructs.ErrorConstruct;
import es.tid.pce.pcep.constructs.MetricPCE;
import es.tid.pce.pcep.constructs.Notify;
import es.tid.pce.pcep.constructs.P2PEndpoints;
import es.tid.pce.pcep.constructs.PCEPIntiatedLSP;
import es.tid.pce.pcep.constructs.Path;
import es.tid.pce.pcep.constructs.Request;
import es.tid.pce.pcep.constructs.Response;
import es.tid.pce.pcep.constructs.StateReport;
import es.tid.pce.pcep.messages.PCEPError;
import es.tid.pce.pcep.messages.PCEPInitiate;
import es.tid.pce.pcep.messages.PCEPMessageTypes;
import es.tid.pce.pcep.messages.PCEPNotification;
import es.tid.pce.pcep.messages.PCEPReport;
import es.tid.pce.pcep.messages.PCEPRequest;
import es.tid.pce.pcep.messages.PCEPResponse;
import es.tid.pce.pcep.objects.Bandwidth;
import es.tid.pce.pcep.objects.EndPoints;
import es.tid.pce.pcep.objects.EndPointsIPv4;
import es.tid.pce.pcep.objects.EndPointsUnnumberedIntf;
import es.tid.pce.pcep.objects.ExplicitRouteObject;
import es.tid.pce.pcep.objects.GeneralizedEndPoints;
import es.tid.pce.pcep.objects.LSP;
import es.tid.pce.pcep.objects.NoPath;
import es.tid.pce.pcep.objects.Notification;
import es.tid.pce.pcep.objects.ObjectParameters;
import es.tid.pce.pcep.objects.ObjectiveFunction;
import es.tid.pce.pcep.objects.PCEPErrorObject;
import es.tid.pce.pcep.objects.PceIdIPv4;
import es.tid.pce.pcep.objects.ProcTime;
import es.tid.pce.pcep.objects.RequestParameters;
import es.tid.pce.pcep.objects.SRP;
import es.tid.pce.pcep.objects.tlvs.EndPointDataPathTLV;
import es.tid.pce.pcep.objects.tlvs.EndPointIPv4TLV;
import es.tid.pce.pcep.objects.tlvs.NoPathTLV;
import es.tid.pce.pcep.objects.tlvs.PathReservationTLV;
import es.tid.pce.pcep.objects.tlvs.SymbolicPathNameTLV;
import es.tid.pce.pcep.objects.tlvs.UnnumberedEndpointTLV;
import es.tid.pce.pcep.objects.tlvs.subtlvs.SymbolicPathNameSubTLV;
import es.tid.pce.server.ParentPCERequestManager;
import es.tid.pce.server.communicationpce.CollaborationPCESessionManager;
import es.tid.pce.server.wson.ReservationManager;
import es.tid.rsvp.objects.subobjects.EROSubobject;
import es.tid.rsvp.objects.subobjects.IPv4prefixEROSubobject;
import es.tid.rsvp.objects.subobjects.UnnumberIfIDEROSubobject;
import es.tid.tedb.DomainTEDB;
import es.tid.tedb.ReachabilityManager;
import es.tid.tedb.TEDB;
//import tid.pce.pcep.objects.tlvs.NotificationTLV;
//import tid.pce.pcep.objects.tlvs.subtlvs.NotificationSubTLV;
import es.tid.util.Analysis;
import es.tid.util.UtilsFunctions;

/**
 * IniProcessorThread is devoted to LSP Inititation from the PCE
 * @author ogondio
 *
 */
public class IniProcessorThread extends Thread{
	
	private static int lspIdSeq=1;
	
	//private Hashtable <Integer,MD_LSP> multiDomain_LSP_list;
	
	private MultiDomainLSPDB multiDomainLSPDB;

	 /** The queue to read the initation requests
	 */
	private LinkedBlockingQueue<InitiationRequest> lspInitiationRequestQueue;
	 
	private ChildPCERequestManager childPCERequestManager;
	private ReachabilityManager reachabilityManager;

	public LinkedBlockingQueue<InitiationRequest> getLspInitiationRequestQueue() {
		return lspInitiationRequestQueue;
	}

	public void setLspInitiationRequestQueue(
			LinkedBlockingQueue<InitiationRequest> lspInitiationRequestQueue) {
		this.lspInitiationRequestQueue = lspInitiationRequestQueue;
	}

	/**
	 * Indicates whether the thread is running
	 */
	private boolean running;

	SaveLSPinRedis savelsp;
	

	/**
	 * Logger
	 */
	private Logger log;

	public IniProcessorThread( LinkedBlockingQueue<InitiationRequest> lspInitiationRequestQueue, ReachabilityManager reachabilityManager,ChildPCERequestManager childPCERequestManager,MultiDomainLSPDB multiDomainLSPDB ){
		log=LoggerFactory.getLogger("PCEServer");
		this.lspInitiationRequestQueue=lspInitiationRequestQueue;
		this.reachabilityManager=reachabilityManager;
		this.childPCERequestManager=childPCERequestManager;
		this.multiDomainLSPDB=multiDomainLSPDB;
		//FIXME
		savelsp=new SaveLSPinRedis();
		savelsp.configure(multiDomainLSPDB, "127.0.0.1",6379);
		running=true;
		
	}
	

	/**
	 * The "core" of the Initiation Request
	 */
	public void run(){	
		InitiationRequest iniReq;
		ExplicitRouteObject fullEro=null;
		while (running) {
			log.info("Waiting for a new Initiation Request");
			try {
				iniReq=lspInitiationRequestQueue.take();
				PCEPIntiatedLSP pini=iniReq.getLspIniRequest();
				long sRP_ID_number = pini.getRsp().getSRP_ID_number();
				//Check if delete
				if (pini.getRsp().isrFlag()){
					delete(pini.getLsp().getLspId());
				}
				else {
					//Check if need to take path computation
					boolean needComputation=false;
					if (pini.getEro()!=null){
						needComputation=false;
						log.info("Provision Multidomain LSP  with ERO");
					}else {
						needComputation=true;
						log.info("Provision Multidomain LSP that needs computation");
					}
					if (needComputation){
						fullEro=null;
					}else {
						fullEro=pini.getEro();
					}

					

					LinkedList<PCEPInitiate> iniList= new LinkedList<PCEPInitiate>();
					log.debug("Creating the bandwidth");
					Bandwidth bw=null;

					//Get the Bandwidth
					if (pini.getBandwidth()!=null){
						bw= pini.getBandwidth().duplicate();
					}
					
					log.info("Creating the Domain list");
					//Create the domain list
					Iterator<EROSubobject> eroi= fullEro.getEROSubobjectList().iterator();
					Inet4Address lastDomain=null;
					Hashtable<Inet4Address,ExplicitRouteObject> eroSplit = new Hashtable<Inet4Address,ExplicitRouteObject>();
					ExplicitRouteObject lastEro=null;
					Inet4Address domain=null;
					while (eroi.hasNext()){
						EROSubobject eroso=eroi.next();
						if (eroso instanceof IPv4prefixEROSubobject) {
							domain=this.reachabilityManager.getDomain(((IPv4prefixEROSubobject) eroso).getIpv4address());
						}else if (eroso instanceof UnnumberIfIDEROSubobject) {
							domain=this.reachabilityManager.getDomain(((UnnumberIfIDEROSubobject) eroso).getRouterID());
							log.info("A3: "+domain);
						}
						if (domain!=null){
							if (lastDomain==null){
								ExplicitRouteObject eeero=new ExplicitRouteObject();
								lastEro=eeero;
								eeero.addEROSubobject(eroso);
								eroSplit.put(domain,eeero );
								lastDomain=domain;
								
							}else if (domain.equals(lastDomain)){
								lastEro.addEROSubobject(eroso);
								
							}else {
								log.info("New domaiiin");
								ExplicitRouteObject eeero=new ExplicitRouteObject();
								lastEro=eeero;
								eeero.addEROSubobject(eroso);
								eroSplit.put(domain,eeero );
								lastDomain=domain;
							}
						}else {
							
							lastEro.addEROSubobject(eroso);
						}
						
					}
					
					Enumeration<Inet4Address> domains=eroSplit.keys();
					LinkedList<Object> domainList=new  LinkedList<Object>();
					while(domains.hasMoreElements()){						
						domain = domains.nextElement();
						log.info("Starting with domain "+domain);
						PCEPInitiate ini = new PCEPInitiate();
						PCEPIntiatedLSP inilsp = new PCEPIntiatedLSP();
						ini.getPcepIntiatedLSPList().add(inilsp);
						SRP srp= new SRP();
						srp.setSRP_ID_number(ParentPCESession.getNewReqIDCounter());
						inilsp.setRsp(srp);
						log.info("Getting endpoints of Domain "+domain);
						inilsp.setEndPoint(getEndPoints(eroSplit.get(domain)));
						inilsp.setEro(eroSplit.get(domain));
						inilsp.setBandwidth(bw);
						LSP lsp =new LSP();
						lsp.setLspId(0);
						SymbolicPathNameTLV symbolicPathNameTLV_tlv = new SymbolicPathNameTLV();
						String name ="IDEALIST "+ParentPCESession.getNewReqIDCounter();
						byte [] symbolicPathNameID= name.getBytes();
						symbolicPathNameTLV_tlv.setSymbolicPathNameID(symbolicPathNameID);
		
						lsp.setSymbolicPathNameTLV_tlv(symbolicPathNameTLV_tlv);
						inilsp.setLsp(lsp);
						iniList.add(ini);
						domainList.add(domain);
					}
					
					
					try {
						
						if (childPCERequestManager==null){
							log.error("o ooooooooo");
						}
						LinkedList<ComputingResponse> reps=childPCERequestManager.executeInitiates(iniList, domainList);
	     				log.info("Hay "+reps.size()+" reps");
	     				MD_LSP mdlsp=new MD_LSP();
	     				mdlsp.setFullERO(fullEro);
	     				for (int i=0;i<reps.size();++i){
	     					ComputingResponse cr=reps.get(i);
	     					LinkedList<StateReport> lsr=cr.ReportList;
	     					StateReport sr=lsr.get(0);
	     					int id=sr.getLSP().getLspId();
	     					mdlsp.getDomainLSPIDMap().put((Inet4Address)domainList.get(i), new Integer(id));
	     					mdlsp.getDomainLSRMpa().put((Inet4Address)domainList.get(i), sr);
	     				}
	     				
	     				int lspId = IniProcessorThread.getID();
	     				multiDomainLSPDB.getMultiDomain_LSP_list().put(lspId, mdlsp);
						StateReport srmd = new StateReport();
						SRP srp = new SRP();
						srp.setSRP_ID_number(sRP_ID_number);
						srmd.setSRP(srp);
						LSP lsp = new LSP();
						lsp.setLspId(lspId);
						srmd.setLSP(lsp);
						Path p=new Path();
						p.setEro(fullEro);
						srmd.setPath(p);
												
						PCEPReport rep = new PCEPReport();
						rep.getStateReportList().add(srmd);
						rep.encode();
						
						
						log.info("Mando: "+ rep.toString());					
						iniReq.getOut().write(rep.getBytes());
						iniReq.getOut().flush();
						
					}catch (Exception e){
						e.printStackTrace();
						log.error("PROBLEM SENDING THE INITIATES");
					}
				}
				
				try{
					this.savelsp.run();
				}catch (Exception e){
					log.warn("Save LSP in Redis fail");
					e.printStackTrace();
					break;
				}

			} catch (InterruptedException e) {
				log.warn("There is no ini to make");
				e.printStackTrace();
				break;
			}
		}
	}	

	public EndPoints getEndPoints(ExplicitRouteObject ero){
				
		Iterator<EROSubobject> eroi= ero.getEROSubobjectList().iterator();
		EROSubobject eroso;
		GeneralizedEndPoints gep = new GeneralizedEndPoints();
		P2PEndpoints p2pEndpoints = new P2PEndpoints();
		EndPoint sourceEndPoint=null;
		EndPoint destinationEndPoint=null;
		
		gep.setP2PEndpoints(p2pEndpoints);
		while (eroi.hasNext()){
			eroso=eroi.next();
			if (eroso instanceof IPv4prefixEROSubobject) {
				if(sourceEndPoint==null){
					sourceEndPoint=new EndPoint();
					EndPointIPv4TLV epipv4=new EndPointIPv4TLV();
					epipv4.setIPv4address(((IPv4prefixEROSubobject)eroso).getIpv4address());
					sourceEndPoint.setEndPointIPv4TLV(epipv4);					
				}
				else if (!eroi.hasNext()){
					destinationEndPoint=new EndPoint();
					EndPointIPv4TLV epipv4=new EndPointIPv4TLV();
					epipv4.setIPv4address(((IPv4prefixEROSubobject)eroso).getIpv4address());
					destinationEndPoint.setEndPointIPv4TLV(epipv4);
				}
			}else if (eroso instanceof UnnumberIfIDEROSubobject) {
				if(sourceEndPoint==null){
					sourceEndPoint=new EndPoint();
					UnnumberedEndpointTLV epipv4=new UnnumberedEndpointTLV();
					epipv4.setIPv4address(((UnnumberIfIDEROSubobject)eroso).getRouterID());
					epipv4.setIfID(((UnnumberIfIDEROSubobject)eroso).getInterfaceID());
					sourceEndPoint.setUnnumberedEndpoint(epipv4);		
					destinationEndPoint=new EndPoint();
					UnnumberedEndpointTLV epipv42=new UnnumberedEndpointTLV();
					epipv42.setIPv4address(((UnnumberIfIDEROSubobject)eroso).getRouterID());
					epipv42.setIfID(((UnnumberIfIDEROSubobject)eroso).getInterfaceID());
					destinationEndPoint.setUnnumberedEndpoint(epipv42);
				}
				else {
					destinationEndPoint=new EndPoint();
					UnnumberedEndpointTLV epipv4=new UnnumberedEndpointTLV();
					epipv4.setIPv4address(((UnnumberIfIDEROSubobject)eroso).getRouterID());
					epipv4.setIfID(((UnnumberIfIDEROSubobject)eroso).getInterfaceID());
					destinationEndPoint.setUnnumberedEndpoint(epipv4);
				}
			}
		}
		p2pEndpoints.setSourceEndpoint(sourceEndPoint);
		p2pEndpoints.setDestinationEndPoints(destinationEndPoint);
		return gep;
		
	}
	
	public void delete (int lspID){
		log.info("GOING TO DELTE "+lspID);
		MD_LSP mdlsp= this.multiDomainLSPDB.getMultiDomain_LSP_list().get(lspID);
		if (mdlsp==null) {
			log.error("LSP is NULL!!");
		}
		LinkedList<PCEPInitiate> iniList= new LinkedList<PCEPInitiate>();
		LinkedList<Object> domainList=new  LinkedList<Object>();
		if (mdlsp!=null){
			log.info("LSP OK in DB!!!");
			Enumeration<Inet4Address> domains=mdlsp.getDomainLSPIDMap().keys();
			while(domains.hasMoreElements()){
				Inet4Address domain = domains.nextElement();
				PCEPInitiate ini = new PCEPInitiate();
				PCEPIntiatedLSP inilsp = new PCEPIntiatedLSP();
				ini.getPcepIntiatedLSPList().add(inilsp);
				SRP srp= new SRP();
				srp.setSRP_ID_number(ParentPCESession.getNewReqIDCounter());
				inilsp.setRsp(srp);
				srp.setRFlag(true);
				LSP lsp =new LSP();
				SymbolicPathNameTLV symbolicPathNameTLV_tlv = new SymbolicPathNameTLV();
				StateReport SR =mdlsp.getDomainLSRMpa().get(domain);
				if (SR.getLSP().getSymbolicPathNameTLV_tlv()!=null){
					symbolicPathNameTLV_tlv.setSymbolicPathNameID(SR.getLSP().getSymbolicPathNameTLV_tlv().getSymbolicPathNameID() );
					lsp.setSymbolicPathNameTLV_tlv(symbolicPathNameTLV_tlv);
				}else {
					log.warn("NO SYMBOLIC PATH NAME TLV!!!" );
				}
				//FIXME: CREATE ENDPOINTS
				lsp.setLspId(mdlsp.getDomainLSPIDMap().get(domain));
				inilsp.setLsp(lsp);
				iniList.add(ini);
				domainList.add(domain);
			}
			try {
				log.info("GOING TO send the deletes of "+lspID);
 				childPCERequestManager.executeInitiates(iniList, domainList);
				log.info("Removing MD LSP "+lspID);
				this.multiDomainLSPDB.getMultiDomain_LSP_list().remove(lspID);
				 
				 
			}catch (Exception e){
				log.error("PROBLEM SENDING THE DELETES");
			}
			
		}
	}
	
	
	public static synchronized int getID(){
		IniProcessorThread.lspIdSeq+=1;
		return IniProcessorThread.lspIdSeq;
	}
}



