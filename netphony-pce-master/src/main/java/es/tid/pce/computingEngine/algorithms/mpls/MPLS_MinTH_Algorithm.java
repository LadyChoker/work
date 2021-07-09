package es.tid.pce.computingEngine.algorithms.mpls;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jgrapht.GraphPath;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import es.tid.pce.computingEngine.ComputingRequest;
import es.tid.pce.computingEngine.ComputingResponse;
import es.tid.pce.computingEngine.algorithms.AlgorithmReservation;
import es.tid.pce.computingEngine.algorithms.ComputingAlgorithm;
import es.tid.pce.computingEngine.algorithms.PCEPUtils;
import es.tid.pce.computingEngine.algorithms.multiLayer.BFS_from_dst;
import es.tid.pce.computingEngine.algorithms.multiLayer.BFS_from_src;
import es.tid.pce.computingEngine.algorithms.multiLayer.Operacion2;
import es.tid.pce.computingEngine.algorithms.multiLayer.Operacion3;
import es.tid.pce.computingEngine.algorithms.multiLayer.OperationsCounter;
import es.tid.pce.computingEngine.algorithms.wson.GenericLambdaReservation;
import es.tid.pce.pcep.constructs.EndPoint;
import es.tid.pce.pcep.constructs.EndPointAndRestrictions;
import es.tid.pce.pcep.constructs.P2MPEndpoints;
import es.tid.pce.pcep.constructs.P2PEndpoints;
import es.tid.pce.pcep.constructs.Path;
import es.tid.pce.pcep.constructs.Request;
import es.tid.pce.pcep.constructs.Response;
import es.tid.pce.pcep.objects.BandwidthRequested;
import es.tid.pce.pcep.objects.EndPoints;
import es.tid.pce.pcep.objects.EndPointsIPv4;
import es.tid.pce.pcep.objects.ExplicitRouteObject;
import es.tid.pce.pcep.objects.GeneralizedEndPoints;
import es.tid.pce.pcep.objects.NoPath;
import es.tid.pce.pcep.objects.ObjectParameters;
import es.tid.pce.pcep.objects.RequestParameters;
import es.tid.pce.pcep.objects.tlvs.NoPathTLV;
import es.tid.pce.server.wson.ReservationManager;
import es.tid.tedb.DomainTEDB;
import es.tid.tedb.IntraDomainEdge;
import es.tid.tedb.TEDB;

public class MPLS_MinTH_Algorithm implements ComputingAlgorithm {
	
	/**
	 * The Logger.
	 */
	private Logger log=LoggerFactory.getLogger("PCEServer");
	private Logger log_OP=LoggerFactory.getLogger("OpMultiLayer");
	
	/**
	 * The Path Computing Request to calculate.
	 */
	private ComputingRequest pathReq;

	/**
	 * Access to the Pre computation part of the algorithm.
	 */
	private MPLS_MinTH_AlgorithmPreComputation preComp;
	
	static Operacion3 op3;
	static BFS_from_src Op4a;
	static BFS_from_dst Op4b;	
	
	/*private int num_op1;
	private int num_op2;
	private int num_op3;
	private int num_op4;*/
	/**
	 * Access to the Reservation Manager to make reservations of Wavelengths/labels.
	 */
	private ReservationManager reservationManager;
	
	//	/********************************
	//	 * Number of wavelenghts (labels).
	//	 ********************************/

	//	private int num_lambdas;
	
	/**
	 * The traffic engineering database
	 */
	private DomainTEDB ted;
	
	private GenericLambdaReservation  reserv;
	
	private OperationsCounter OP_Counter;
	
	private Lock graphLock;
	
	private ArrayList<SimpleDirectedWeightedGraph<Inet4Address, IntraDomainEdge>> networkGraphs_precomp;
	
	/*
	 * Constructor
	 * @param pathReq
	 * @param ted
	 * @param reservationManager
	 */
	public MPLS_MinTH_Algorithm(ComputingRequest pathReq,TEDB ted, ReservationManager reservationManager, OperationsCounter OPcounter){
		//this.num_lambdas=((DomainTEDB)ted).getNumLambdas();
		this.pathReq=pathReq;
		this.reservationManager=reservationManager;
		this.ted=(DomainTEDB)ted;
		this.OP_Counter=OPcounter;
	}

	/**
	 * Exectutes the path computation and returns the PCEP Response
	 */
	public ComputingResponse call(){ 
		//Time stamp of the start of the algorithm;
		log.debug("Starting Multilayer Min Ligth Paths Algorithm");
		graphLock = preComp.getGraphLock();
	
					
		
									/////////////////////////
									// PETICION INDIVIDUAL //
									/////////////////////////

		ComputingResponse m_resp = new ComputingResponse();
		m_resp.setEncodingType(pathReq.getEcodingType());
		
		long tiempoini =System.nanoTime();
			
		//Create the response message
		//It will contain either the path or noPath
		ComputingResponse m_resp_individual = new ComputingResponse();
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
			
		BandwidthRequested bandwidth = new BandwidthRequested();
		bandwidth.setBw(((BandwidthRequested)req.getBandwidth()).getBw());
		response.setBandwidth(bandwidth);
					
		//esto hay que cambiarlo para poder leer del GENERALIZED END POINTS
		//if (getObjectType(req.getEndPoints()))
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
		//aqu� acaba lo que he a�adido
		//Now, check if the source and destination are in the TED.
		log.error("Source: "+source_router_id_addr+"; Destination:"+dest_router_id_addr);
		if (!((((ted).containsVertex(source_router_id_addr))&&((ted).containsVertex(dest_router_id_addr))))){
			log.error("Source or destination are NOT in the TED");	
			NoPath noPath= new NoPath();
			noPath.setNatureOfIssue(ObjectParameters.NOPATH_NOPATH_SAT_CONSTRAINTS);
			NoPathTLV noPathTLV=new NoPathTLV();
			if (!((ted.containsVertex(source_router_id_addr)))){
				log.error("Unknown source");	
				noPathTLV.setUnknownSource(true);	
			}
			if (!((ted.containsVertex(dest_router_id_addr)))){
				log.error("Unknown destination");
				noPathTLV.setUnknownDestination(true);	
			}
					noPath.setNoPathTLV(noPathTLV);				
			response.setNoPath(noPath);
			return m_resp;
		}
				
		//grafo con los ligth paths
		SimpleDirectedWeightedGraph<Object, IntraDomainEdge> GraphIP = preComp.getnetworkGraphIP();
		
		//grafo con las conexiones interlayer
		SimpleDirectedWeightedGraph<Object, IntraDomainEdge> InterlayerGraph =preComp.getInterLayerGraph();
		
		//Grago Óptico
		ArrayList<SimpleDirectedWeightedGraph<Object, IntraDomainEdge>> networkGraphs = preComp.getNetworkGraphs();
		
		boolean nopath=true;//Initially, we still have no path
		
		//SimpleDirectedWeightedGraph<Inet4Address,IntraDomainEdge> OpticalGraph = preComp.getOpticalnetworkGraph();
		GraphPath<Object,IntraDomainEdge> gp_chosen=null;
		float bwt_req = ((BandwidthRequested)req.getBandwidth()).getBw(); // ancho de banda de la request
		
		// OPERATION 2
		GraphPath<Object,IntraDomainEdge> gp = Operacion2.get_op2(GraphIP, 
				source_router_id_addr, dest_router_id_addr, bwt_req, graphLock);
						
		if (gp != null){
			log.warn("Multiple LigthPath found at Operation 2--");	
			nopath = false;
			gp_chosen = gp;
		}
		
		if (nopath==true){
			log.debug("No path found"); // NO PATH FOUND
			NoPath noPath= new NoPath();
			noPath.setNatureOfIssue(ObjectParameters.NOPATH_NOPATH_SAT_CONSTRAINTS);
			NoPathTLV noPathTLV=new NoPathTLV();
			noPath.setNoPathTLV(noPathTLV);				
			response.setNoPath(noPath);
			return m_resp;
		}
		else if (nopath==false){  // PATH FOUND
			Path path=new Path();
			ExplicitRouteObject ero= new ExplicitRouteObject();
			List<IntraDomainEdge> edge_list =gp_chosen.getEdgeList();			
						
			//tengo un listado con todos los edges del camino a lo largo de todas las capas
			//FIXME: llamar a la función EncodeEroMPLS
			path.setEro(ero);
			PCEPUtils.completeMetric(path, req, edge_list);
			response.addPath(path);
			
			//FIXME: RESERVATION NEEDS TO BE IMPROVED!!!
			EncodeEroMPLS.createEroMpls(ero, edge_list);
			
			LinkedList<Object> sourceVertexList=new LinkedList<Object>();
			LinkedList<Object> targetVertexList=new LinkedList<Object>();
			for (int i=0;i<edge_list.size();i++){
				sourceVertexList.add(edge_list.get(i).getSource());
				targetVertexList.add(edge_list.get(i).getTarget());
			}
				
			if (req.getReservation()!=null){
				reserv= new GenericLambdaReservation();
				reserv.setResp(m_resp);
				reserv.setReservation(req.getReservation());
				reserv.setSourceVertexList(sourceVertexList);
				reserv.setTargetVertexList(targetVertexList);
				reserv.setBidirectional(rp.isBidirect());
				reserv.setReservationManager(reservationManager);
			}
			else
				log.info("Reservation is FALSE");
		}
		
		long tiempofin =System.nanoTime();
		long tiempotot=tiempofin-tiempoini;
		log.info("Ha tardado "+tiempotot+" nanosegundos");
			
		return m_resp;
	}
	
	
	public void setPreComp(MPLS_MinTH_AlgorithmPreComputation preComp) {
		this.preComp = preComp;
	}
	
	public AlgorithmReservation getReserv() {
		return reserv;
	}
	public void notifyWavelengthReservation(LinkedList<Inet4Address> sourceVertexList,
	        LinkedList<Inet4Address> targetVertexList, int wavelength) {
		//System.out.println("LLEGO AQUI"); 
		graphLock.lock();
		try{
			//System.out.println("DENTRO DE LA RESERVA DEL ALGORITMO CON LAMBDA --> "+wavelength);
		    SimpleDirectedWeightedGraph<Inet4Address, IntraDomainEdge> networkGraph=networkGraphs_precomp.get(wavelength);
		    for (int i=0;i<sourceVertexList.size();++i){                                
		    	networkGraph.removeEdge(sourceVertexList.get(i), targetVertexList.get(i));
		        //System.out.println("EDGE OUT");
		    }
		}finally{
		  	graphLock.unlock();
		}
	}
}
