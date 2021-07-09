package es.tid.pce.computingEngine.algorithms.wson;

import java.net.Inet4Address;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jgrapht.GraphPath;

import es.tid.pce.computingEngine.ComputingRequest;
import es.tid.pce.computingEngine.ComputingResponse;
import es.tid.pce.computingEngine.algorithms.AlgorithmReservation;
import es.tid.pce.computingEngine.algorithms.ComputingAlgorithm;
import es.tid.pce.computingEngine.algorithms.PCEPUtils;
import es.tid.pce.computingEngine.algorithms.wson.wa.FirstFit;
import es.tid.pce.pcep.constructs.EndPoint;
import es.tid.pce.pcep.constructs.EndPointAndRestrictions;
import es.tid.pce.pcep.constructs.P2MPEndpoints;
import es.tid.pce.pcep.constructs.P2PEndpoints;
import es.tid.pce.pcep.constructs.Path;
import es.tid.pce.pcep.constructs.Request;
import es.tid.pce.pcep.constructs.Response;
import es.tid.pce.pcep.objects.EndPoints;
import es.tid.pce.pcep.objects.EndPointsIPv4;
import es.tid.pce.pcep.objects.ExplicitRouteObject;
import es.tid.pce.pcep.objects.GeneralizedEndPoints;
import es.tid.pce.pcep.objects.NoPath;
import es.tid.pce.pcep.objects.ObjectParameters;
import es.tid.pce.pcep.objects.RequestParameters;
import es.tid.pce.pcep.objects.tlvs.NoPathTLV;
import es.tid.pce.server.wson.ReservationManager;
import es.tid.rsvp.RSVPProtocolViolationException;
import es.tid.rsvp.constructs.gmpls.DWDMWavelengthLabel;
import es.tid.rsvp.objects.subobjects.GeneralizedLabelEROSubobject;
import es.tid.rsvp.objects.subobjects.IPv4prefixEROSubobject;
import es.tid.rsvp.objects.subobjects.UnnumberIfIDEROSubobject;
import es.tid.tedb.DomainTEDB;
import es.tid.tedb.IntraDomainEdge;
import es.tid.tedb.TEDB;
import es.tid.tedb.WSONInformation;

public class KSPprecompFF_Algorithm implements ComputingAlgorithm {
	private WSONInformation WSONInfo;

	private Logger log=LoggerFactory.getLogger("PCEServer");
	
	private DomainTEDB ted;
	
	private GenericLambdaReservation  reserv;
	
	private ReservationManager reservationManager;
	
	private KSPprecompFF_AlgorithmPreComputation preComp;
	
	private ComputingRequest pathReq;
	
	public KSPprecompFF_Algorithm(ComputingRequest pathReq,TEDB ted, ReservationManager reservationManager ){
		//this.num_lambdas=((DomainTEDB)ted).getNumLambdas();
		this.pathReq=pathReq;
		this.reservationManager=reservationManager;
		this.ted=(DomainTEDB)ted;
	}
	
	public ComputingResponse call(){ 
		
		//Time stamp of the start of the algorithm;
		long tiempoini =System.nanoTime();
		
		log.debug("Starting KSPprecomp Algorithm");
		
		//Create the response message
		//It will contain either the path or noPath
		ComputingResponse m_resp=new ComputingResponse();
		m_resp.setEncodingType(pathReq.getEcodingType());
		//The request that needs to be solved
		Request req=pathReq.getRequestList().get(0);
		//Request Id, needed for the response
		long reqId=req.getRequestParameters().getRequestID();
		log.info("Request id: "+reqId+", getting endpoints");
		//Start creating the response
		Response response=new Response();
		RequestParameters rp = new RequestParameters();
		rp.setBidirect(req.getRequestParameters().isBidirect());
		rp.setRequestID(reqId);
		response.setRequestParameters(rp);
		m_resp.addResponse(response);
		
		EndPoints  EP= req.getEndPoints();	
		Inet4Address source_router_id_addr = null;
		Inet4Address dest_router_id_addr = null;

		if (EP.getOT()==ObjectParameters.PCEP_OBJECT_TYPE_ENDPOINTS_IPV4){
			EndPointsIPv4  ep=(EndPointsIPv4) req.getEndPoints();
			source_router_id_addr=ep.getSourceIP();
			dest_router_id_addr=ep.getDestIP();
		}else if (EP.getOT()==ObjectParameters.PCEP_OBJECT_TYPE_ENDPOINTS_IPV6){

		}else if (EP.getOT()==ObjectParameters.PCEP_OBJECT_TYPE_GENERALIZED_ENDPOINTS){
			GeneralizedEndPoints  gep=(GeneralizedEndPoints) req.getEndPoints();
			if(gep.getGeneralizedEndPointsType()==ObjectParameters.PCEP_GENERALIZED_END_POINTS_TYPE_P2P){
				P2PEndpoints p2pep= gep.getP2PEndpoints();
				EndPoint sourceep=p2pep.getSourceEndPoint();
				EndPoint destep=p2pep.getDestinationEndPoint();
				source_router_id_addr=sourceep.getEndPointIPv4TLV().IPv4address;
				dest_router_id_addr=destep.getEndPointIPv4TLV().IPv4address;
			}
			if(gep.getGeneralizedEndPointsType()==ObjectParameters.PCEP_GENERALIZED_END_POINTS_TYPE_P2MP_NEW_LEAVES){
				P2MPEndpoints p2mpep= gep.getP2MPEndpoints();
				EndPointAndRestrictions epandrest=p2mpep.getEndPointAndRestrictions();
				EndPoint sourceep=epandrest.getEndPoint();
				source_router_id_addr=sourceep.getEndPointIPv4TLV().IPv4address;
				int cont=0;
				while (cont<=p2mpep.getEndPointAndRestrictionsList().size()){ //esto est� mal
					epandrest=p2mpep.getEndPointAndRestrictionsList().get(cont);
					EndPoint destep=epandrest.getEndPoint();
					source_router_id_addr=sourceep.getEndPointIPv4TLV().IPv4address;
					dest_router_id_addr=destep.getEndPointIPv4TLV().IPv4address;

				}
			}
		}
				
		//Now, check if the source and destination are in the TED.
		log.debug("Source: "+source_router_id_addr+"; Destination:"+dest_router_id_addr);
		if (!(((ted.containsVertex(source_router_id_addr))&&(ted.containsVertex(dest_router_id_addr))))){
			log.warn("Source or destination are NOT in the TED");	
			NoPath noPath= new NoPath();
			noPath.setNatureOfIssue(ObjectParameters.NOPATH_NOPATH_SAT_CONSTRAINTS);
			NoPathTLV noPathTLV=new NoPathTLV();
			if (!((ted.containsVertex(source_router_id_addr)))){
				log.debug("Unknown source");	
				noPathTLV.setUnknownSource(true);	
			}
			if (!((ted.containsVertex(dest_router_id_addr)))){
				log.debug("Unknown destination");
				noPathTLV.setUnknownDestination(true);	
			}

			noPath.setNoPathTLV(noPathTLV);				
			response.setNoPath(noPath);
			return m_resp;
		}
		
		boolean nopath=true;//Initially, we still have no path
		int lambda_chosen=0;//We begin with lambda index 0
		GraphPath<Object,IntraDomainEdge> gp_chosen=null;
		boolean noLambda = true;
		
		log.debug("Starting the selection of the path");
		
		if (!(source_router_id_addr.equals(dest_router_id_addr)))
			nopath=false;
		
		Hashtable<Object,List<GraphPath<Object,IntraDomainEdge>>> routeInterList= new Hashtable<Object,List<GraphPath<Object,IntraDomainEdge>>>();
		
		routeInterList = preComp.routeTable.get(source_router_id_addr);
		
		List<GraphPath<Object,IntraDomainEdge>> edge_K_list;
		edge_K_list = routeInterList.get(dest_router_id_addr);
		
		GraphPath<Object, IntraDomainEdge> EdgePath;
				
		int j =0;
		if (nopath==false){
			for (j =0; j<edge_K_list.size(); j++)
			{
				gp_chosen = edge_K_list.get(j);
	
				Path path=new Path();
				ExplicitRouteObject ero= new ExplicitRouteObject();
				List<IntraDomainEdge> edge_list=gp_chosen.getEdgeList();
				
				//FirstFit returns the lambda for the path
				lambda_chosen = FirstFit.getLambda(edge_list);
				
				//FF returns "-1" in case there is no lambda free for path
				if (lambda_chosen == -1)
				{
					noLambda = true;
					continue;
				}
								
				noLambda = false;
							
				//in case there is a lambda free for path
				
				int i;
				for (i=0;i<edge_list.size();i++){
					UnnumberIfIDEROSubobject eroso= new UnnumberIfIDEROSubobject();
					eroso.setRouterID((Inet4Address)edge_list.get(i).getSource());
					eroso.setInterfaceID(edge_list.get(i).getSrc_if_id());
					eroso.setLoosehop(false);
					ero.addEROSubobject(eroso);
					
					GeneralizedLabelEROSubobject genLabel= new GeneralizedLabelEROSubobject();
					ero.addEROSubobject(genLabel);
					//ITU-T Format
					DWDMWavelengthLabel WDMlabel=new DWDMWavelengthLabel();
					WDMlabel.setGrid(preComp.getWSONInfo().getGrid());
					WDMlabel.setChannelSpacing(preComp.getWSONInfo().getCs());
					WDMlabel.setN(lambda_chosen+preComp.getWSONInfo().getnMin());
					WDMlabel.setIdentifier(0);
					try {
						WDMlabel.encode();
					} catch (RSVPProtocolViolationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					genLabel.setLabel(WDMlabel.getBytes());		
					
				}
				IPv4prefixEROSubobject eroso= new IPv4prefixEROSubobject();
				eroso.setIpv4address((Inet4Address)edge_list.get(edge_list.size()-1).getTarget());
				eroso.setPrefix(32);
				ero.addEROSubobject(eroso);
				path.setEro(ero);
				PCEPUtils.completeMetric(path, req, edge_list);
				response.addPath(path);
	
				//FIXME: RESERVATION NEEDS TO BE IMPROVED!!!
				LinkedList<Object> sourceVertexList=new LinkedList<Object>();
				LinkedList<Object> targetVertexList=new LinkedList<Object>();
				for (i=0;i<edge_list.size();i++){
					sourceVertexList.add(edge_list.get(i).getSource());
					targetVertexList.add(edge_list.get(i).getTarget());
				}
				
				
				if (req.getReservation()!=null){
					
					reserv= new GenericLambdaReservation();
					reserv.setResp(m_resp);
					reserv.setLambda_chosen(lambda_chosen);
					reserv.setReservation(req.getReservation());
					reserv.setSourceVertexList(sourceVertexList);
					reserv.setTargetVertexList(targetVertexList);
					
					if (rp.isBidirect() == true){
						reserv.setBidirectional(true);
					}
					
					else{
						reserv.setBidirectional(false);
					}
					
					reserv.setReservationManager(reservationManager);
				}
			}
		}
		long tiempofin =System.nanoTime();
		long tiempotot=tiempofin-tiempoini;
		log.info("Ha tardado "+tiempotot+" nanosegundos");
		
		if (noLambda == true){
			log.debug("No path found");
			NoPath noPath= new NoPath();
			noPath.setNatureOfIssue(ObjectParameters.NOPATH_NOPATH_SAT_CONSTRAINTS);
			NoPathTLV noPathTLV=new NoPathTLV();
			noPath.setNoPathTLV(noPathTLV);				
			response.setNoPath(noPath);
			return m_resp;
		}
		return m_resp;
	}
	
	public void setPreComp(KSPprecompFF_AlgorithmPreComputation preComp) {
		this.preComp = preComp;
	}
	
	public AlgorithmReservation getReserv() {
		return reserv;
	}

	
}
