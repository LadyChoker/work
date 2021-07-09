package es.tid.pce.server;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import es.tid.bgp.bgp4Peer.peer.BGPPeer;
import es.tid.ospf.ospfv2.OSPFv2LinkStateUpdatePacket;
import es.tid.tedb.DomainTEDB;
import es.tid.tedb.IntraDomainEdge;
import es.tid.tedb.SimpleTEDB;
import es.tid.tedb.controllers.TEDUpdaterController;
import es.tid.tedb.ospfv2.OSPFSessionServer;
import es.tid.tedb.ospfv2.OSPFTCPSessionServer;
import es.tid.util.UtilsFunctions;


/**
 * Topology server, receives a TED, initializes it and maintains it
 * Valors
 */

public class TopologyManager 
{
	PCEServerParameters params;
	DomainTEDB ted;
	Logger log;
	
	public TopologyManager(PCEServerParameters params,DomainTEDB ted, Logger log)
	{
		this.params = params;
		this.ted = ted;
		this.log = log;
	}
	
	public void initTopology()
	{
		if (params.isWLAN())
		{
			initFromWLANController();
			params.initFromFile = false;
		}
		if (params.initFromFile)
		{
			initFromFile();
		}
		if (params.isOSPFSession())
		{
			log.info("Initializing from OSPF");
			initFromOSPF();
		}
		
		if (params.isActingAsBGP4Peer()) {//BGP
			log.info("Acting as BGP Peer!");
			BGPPeer bgpPeer = new BGPPeer();		
			bgpPeer.configure(params.getBGP4File());			
			//bgpPeer.configure("PCEServerConfiguration.xml");				
			bgpPeer.setReadDomainTEDB(ted);
			bgpPeer.createUpdateDispatcher();
			bgpPeer.startClient();		
			bgpPeer.startServer();
			bgpPeer.startManagementServer();
			bgpPeer.startSendTopology();
		}
	}
	
	private void initFromFile() 
	{
		//Initialize Traffic Engineering Database
		if(params.ITcapable==true){
			log.info("Initializing IT capable TEDB");
			ted.initializeFromFile(params.getITNetworkDescriptionFile());
		}else{
			
			if (params.isMultilayer()){
				log.info("Initializing Multilayer TEDB");
				ted.initializeFromFile(params.getNetworkDescriptionFile());
			}else {
				//获取单层单层多域拓扑。。。
				if (params.isMultidomain()){
					log.info("多个域合成一个");
				}else {
					//获取单层单层单域拓扑。。。
					log.info("Initializing Single Layer TEDB");	
				}
				//STRONGEST: Independent PCE's with lambdas' sub-set assignment
				if (params.getLambdaEnd()!=Integer.MAX_VALUE){
					log.info("Entro en Max Value");
					((SimpleTEDB)ted).initializeFromFile(params.getNetworkDescriptionFile(),params.getLayer() ,params.isMultidomain(),params.getLambdaIni(),params.getLambdaEnd(),params.isSSOn(),false);
				}else if (params.isActingAsBGP4Peer()&&(params.isOSPFSession())){
					((SimpleTEDB)ted).initializeFromFile(params.getNetworkDescriptionFile(),params.getLayer() ,params.isMultidomain(),params.getLambdaIni(),params.getLambdaEnd(),true,false);
					log.info("Entro en OSPF y BGP");
				}
				else{
					//获取拓扑的文件
					//log.info("Entro en el else:::"+params.isSSOn()+",params.isWLAN()"+params.isWLAN());
					log.info("params.getNetworkDescriptionFile()::"+params.getNetworkDescriptionFile());
					//params.setNetworkDescriptionFile("/usr/local/nodeConfig/topologia2.xml");
					log.info("多域:"+params.isMultidomain()+"");
					log.info("layer:"+params.getLayer());
					((SimpleTEDB)ted).initializeFromFile(params.getNetworkDescriptionFile(),params.getLayer() ,params.isMultidomain(),0,Integer.MAX_VALUE ,params.isSSOn(),false, params.isWLAN());
					/*log.info("printTopology()");
					log.info(ted.printTopology());
					log.info("printTopology()");*/
				}
			}
		}
	}

	private void initFromOSPF()
	{	
		LinkedBlockingQueue<OSPFv2LinkStateUpdatePacket> ospfv2PacketQueue = new LinkedBlockingQueue<OSPFv2LinkStateUpdatePacket>();		
		TopologyUpdaterThread tut = null;
		
		if (params.isMultilayer()==true)
		{
			tut = new TopologyUpdaterThread(ospfv2PacketQueue, ted,params.getLambdaIni(),params.getLambdaEnd(), params.isCompletedAuxGraph(), params.isMultilayer());		
		}
		else
		{
			tut = new TopologyUpdaterThread(ospfv2PacketQueue, ted,params.getLambdaIni(),params.getLambdaEnd());
		}
		tut.start();
		
		/*
		 * Code added to handle the redis topology update
		 * 
		 */
		
		boolean redisDatabase = true;
				
		LinkedBlockingQueue<OSPFv2LinkStateUpdatePacket> redisOspfv2PacketQueue = new LinkedBlockingQueue<OSPFv2LinkStateUpdatePacket>();
		if(redisDatabase)
		{
			RedisTEDUpdaterThread rtut = new RedisTEDUpdaterThread(redisOspfv2PacketQueue);
			rtut.start();			
		}
		
		log.info("params.isOSPFTCPSession():::"+params.isOSPFTCPSession());
		log.info("params.isOSPFSession():::"+params.isOSPFSession());
		if (params.isOSPFTCPSession())
		{
			OSPFTCPSessionServer OSPFsessionserver= new OSPFTCPSessionServer(ospfv2PacketQueue,redisOspfv2PacketQueue);
			OSPFsessionserver.setOSPFTCPPort(params.getOSPFTCPPort());
			OSPFsessionserver.start();	
		}
		else if (params.isOSPFSession())
		{
			System.out.println("Es OSPF Session");
			OSPFSessionServer OSPFsessionserver = null;
			try {
				
				/*
				 * Redis database
				 */
				
				if(redisDatabase){
				
					OSPFsessionserver = new OSPFSessionServer(ospfv2PacketQueue, redisOspfv2PacketQueue, ((Inet4Address) InetAddress.getByName(params.getOSPFListenerIP())));
					OSPFsessionserver.setMulticast(params.isOSPFSession());
					OSPFsessionserver.start();
				
					
				}
				else{
				
					OSPFsessionserver = new OSPFSessionServer(ospfv2PacketQueue, ((Inet4Address) InetAddress.getByName(params.getOSPFListenerIP())));
					OSPFsessionserver.setMulticast(params.isOSPFSession());
					OSPFsessionserver.start();
				}
			} 
			catch (UnknownHostException e) 
			{
				log.info(UtilsFunctions.exceptionToString(e));
			}
		}
	}
	
	private void initFromWLANController()
	{
		log.info("Initializing TED from WLAN Controller. New");
		
		ArrayList<String> ips = new ArrayList<String>();
		ArrayList<String> ports = new ArrayList<String>();
		ArrayList<String> types = new ArrayList<String>();
		
		TopologyManager.parseControllerFile(params.getControllerListFile(), ips, ports, types);
		
		SimpleDirectedWeightedGraph<Object, IntraDomainEdge> networkGraph = new SimpleDirectedWeightedGraph<Object, IntraDomainEdge>(IntraDomainEdge.class);
		((SimpleTEDB)ted).setNetworkGraph(networkGraph);
		
		TopologyManager.updateTopology( ips, ports, types, ted, params.getInterDomainFile(), log);
	}
	
	public static void updateTopology(ArrayList<String> ips, ArrayList<String> ports, ArrayList<String> types,DomainTEDB ted, String interDomainFile, Logger log)
	{
		for (int i = 0; i < ips.size(); i++)
		{
		
			try 
			{
				Class<?> act = Class.forName("tid.pce.tedb.controllers.TEDUpdater" + types.get(i));
				
				Class[] cArg = new Class[6];
				//(ArrayList<String> ips, ArrayList<String>ports , String topologyPathLinks, 
				//String topologyPathNodes,DomainTEDB ted, Logger log)
				cArg[0] = String.class;
				cArg[1] = String.class;
				cArg[2] = String.class;
				cArg[3] = String.class;
				cArg[4] = DomainTEDB.class;
				cArg[5] = Logger.class;
				
				Object[] args = new Object[6];
				args[0] = ips.get(i);
				args[1] = ports.get(i);
				args[2] = "";
				//args[3] = "/wm/core/controller/switches/json"; /*Floodlight*/
				args[3] = "/v1.0/topology/switches"; /*Ryu*/
				args[4] = ted;
				args[5] = log;
				
				TEDUpdaterController topUpdater = (TEDUpdaterController)act.getDeclaredConstructor(cArg).newInstance(args);
				topUpdater.setInterDomainFile(interDomainFile);
				topUpdater.run();
			}
			catch (Exception e1)
			{
				log.info(UtilsFunctions.exceptionToString(e1));			
				return;
			}
			
			log.info(i+ ":i + TED" + ted.printTopology());
			//TEDUpdaterFloodlight thread = new TEDUpdaterFloodlight(ips.get(i), ports.get(i), params.getTopologyPath(),"/wm/core/controller/switches/json", ted, log);
			/*
			TopologyUpdaterFloodlight thread = new TopologyUpdaterFloodlight(ips, ports, params.getTopologyPath(),"/wm/core/controller/switches/json", ted, log);
			thread.setInterDomainFile(params.getInterDomainFile());
			thread.run();
			*/	
		}
		TEDUpdaterController.parseRemainingLinksFromXML(ted, interDomainFile);
	}
	
	public static void parseControllerFile(String controllerFile, ArrayList<String> ips, ArrayList<String> ports, ArrayList<String> types)
	{		
		try 
		{
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			File confFile = new File(controllerFile);		
			Document doc = builder.parse(confFile);
			
			NodeList list_nodes_Edges = doc.getElementsByTagName("controller");
			
			for (int i = 0; i < list_nodes_Edges.getLength(); i++) 
			{
				Element nodes_servers = (Element) list_nodes_Edges.item(i);
				String ip = UtilsFunctions.getCharacterDataFromElement((Element) nodes_servers.getElementsByTagName("ip").item(0));
				String port = UtilsFunctions.getCharacterDataFromElement((Element) nodes_servers.getElementsByTagName("port").item(0));
				String type = UtilsFunctions.getCharacterDataFromElement((Element) nodes_servers.getElementsByTagName("type").item(0));
				
				ips.add(ip);
				ports.add(port);
				types.add(type);
				
				System.out.print("Adding controller with IP: " + ip + " and port: " + port + "and type: " + type);
			}
		} 
		catch (Exception e) 
		{
			System.out.print(UtilsFunctions.exceptionToString(e));
		}	
	}
}

