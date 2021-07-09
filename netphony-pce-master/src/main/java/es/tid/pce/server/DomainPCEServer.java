package es.tid.pce.server;

/** 
 * PCE Domain Server.
 * 
 * It is the main class of a PCE that is responsible of a domain.
 * 
 * By default listens on port 4189
 * 
 * @author Oscar, Eduardo
 */

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Timer;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.tid.pce.computingEngine.ReportDispatcher;
import es.tid.pce.computingEngine.RequestDispatcher;
import es.tid.pce.computingEngine.algorithms.ComputingAlgorithmManager;
import es.tid.pce.computingEngine.algorithms.ComputingAlgorithmManagerSSON;
import es.tid.pce.computingEngine.algorithms.ComputingAlgorithmPreComputation;
import es.tid.pce.computingEngine.algorithms.ComputingAlgorithmPreComputationSSON;
import es.tid.pce.computingEngine.algorithms.multiLayer.OperationsCounter;
import es.tid.pce.pcepsession.PCEPSessionsInformation;
import es.tid.pce.server.communicationpce.BackupSessionManagerTask;
import es.tid.pce.server.communicationpce.CollaborationPCESessionManager;
import es.tid.pce.server.lspdb.ReportDB_Handler;
import es.tid.pce.server.lspdb.SingleDomainLSPDB;
import es.tid.pce.server.management.PCEManagementSever;
import es.tid.pce.server.wson.ReservationManager;
import es.tid.tedb.DomainTEDB;
import es.tid.tedb.MultiLayerTEDB;
import es.tid.tedb.SimpleITTEDB;
import es.tid.tedb.SimpleTEDB;


public class DomainPCEServer implements Runnable{

	/**
	 * Logger
	 */
	public static final Logger log = LoggerFactory.getLogger("PCEServer");
	public static Logger log5;
	private static OperationsCounter OPcounter;
	
	private static ReportDB_Handler rptdb;
	private static boolean listening;

	/**
	 * Parameters of the PCE
	 */
	PCEServerParameters params;

	/**
	 * Socket where the PCE is listening;
	 */
	ServerSocket serverSocket;
	/**
	 * Management server of PCE
	 */
	PCEManagementSever pms;

	
	public void configure (String configFile){
		if (configFile!=null){
			
			params=new PCEServerParameters(configFile);
			log.info("params=new PCEServerParameters(configFile)");
		}else {
			params=new PCEServerParameters();
		}
		log.info("初始化PCE服务器: initialize()");
		params.initialize();
		
		//Initialize loggers
//		FileHandler fh;
//		FileHandler fh2;
//		FileHandler fh3;
//		FileHandler fh4;
//		FileHandler fh5;
		//FileHandler fh6;
		try {
			
//			fh=new FileHandler(params.getPCEServerLogFile());
//			fh2=new FileHandler(params.getPCEPParserLogFile());
//			fh3=new FileHandler(params.getOSPFParserLogFile());
//			fh4=new FileHandler(params.getTEDBParserLogFile());
//			fh5=new FileHandler("OpMultiLayer.log", false);
//			fh5.setFormatter(new SimpleFormatter());
			//fh.setFormatter(new SimpleFormatter());
			//fh2.setFormatter(new SimpleFormatter());	
			
//			Logger log2=LoggerFactory.getLogger("PCEPParser");
//			Logger log3=LoggerFactory.getLogger("OSPFParser");
//			Logger log4=LoggerFactory.getLogger("TEDBParser");
			//Logger log6=LoggerFactory.getLogger("BGPParser");
			log5=LoggerFactory.getLogger("OpMultiLayer");
//			log.addHandler(fh);
//			log2.addHandler(fh2);
//			log3.addHandler(fh3);
//			log4.addHandler(fh4);
			//logTimePCE.addHandler(fh5);
//			fh4.setFormatter(new SimpleFormatter());
//			log5.addHandler(fh5);
//			log5.setLevel(Level.ALL);
//			if (params.isSetTraces() == false){		    	
//				log.setLevel(Level.SEVERE);
//				log2.setLevel(Level.SEVERE);	
//				log3.setLevel(Level.SEVERE);
//				log4.setLevel(Level.SEVERE);
//				//log6.setLevel(Level.SEVERE);
//				//log5.setLevel(Level.SEVERE);
//			}
//
//			else{
//				log.setLevel(Level.ALL);
//				log2.setLevel(Level.ALL);
//				log3.setLevel(Level.ALL);
//				log4.setLevel(Level.ALL);
//				//log6.setLevel(Level.ALL);
//			}

		} catch (Exception e1) {
			e1.printStackTrace();
			System.exit(1);
		}

		log.info("载入PCE服务器: " + configFile);

		//log.info("Inizializing TID PCE Server!!");
		log.info("初始化 PCE 服务器!!");

	}

//	public void run(){
//		log.info("运行PCE服务器");
//		//Elements of the PCE Server
//
//
//		// Information about all the sessions of the PCE
//		//PCE session list
//		//载入PCE sessions信息来自配置文件
//		PCEPSessionsInformation pcepSessionsInformation = new PCEPSessionsInformation();
//		// PCE有无状态
//		pcepSessionsInformation.setStateful(params.isStateful());
//		//D、S、T flag
//		pcepSessionsInformation.setStatefulDFlag(params.isStatefulDFlag());
//		pcepSessionsInformation.setStatefulSFlag(params.isStatefulSFlag());
//		pcepSessionsInformation.setStatefulTFlag(params.isStatefulTFlag());
//
//
//		pcepSessionsInformation.setActive(params.isActive());
//		pcepSessionsInformation.setSRCapable(params.isSRCapable());
//		pcepSessionsInformation.setMSD(params.getMSD());
//
//		SingleDomainInitiateDispatcher iniDispatcher=null;
//
//		SingleDomainLSPDB singleDomainLSPDB=null;
//		 IniPCCManager iniManager=null;
//
//		if (params.isSRCapable())
//			log.info("PCEServer: PCE is SR capable with MSD="+pcepSessionsInformation.getMSD());
//
//		//The Traffic Engineering Database
//		DomainTEDB ted;
//		if(params.ITcapable==true){
//			//IT CAPABLE PCE
//			log.info("IT capable Domain PCE");
//			ted=new SimpleITTEDB();
//		}
//		//普通通用服务器
//		else{
//			//GENERIC PCE
//			log.info("GENERIC PCE");
//			if (params.isMultilayer()){
//				ted=new MultiLayerTEDB();
//				log.info("is multilayer");
//			}else{
//				ted=new SimpleTEDB();
//				//ted.printTopology();
//			}
//		}
//		//有状态服务器
//		if (params.isStateful())
//		{
//			//Stateful PCE with T=false D=false S=false
//			log.info("Stateful PCE with T="+params.isStatefulTFlag()+" D="+params.isStatefulDFlag()+" S="+params.isStatefulSFlag());
//			//单域LSP数据库
//			singleDomainLSPDB=new SingleDomainLSPDB();
//			//dbType默认是_，从服务器配置文件中读取
//			if(params.getDbType().equals("_"))
//			{
//				singleDomainLSPDB.setExportDb(false);
//			}
//			//PCC初始配置
//			iniManager= new IniPCCManager();
//			//单域初始配置调度器
//			iniDispatcher = new SingleDomainInitiateDispatcher(singleDomainLSPDB,iniManager);
//		}
//
//
//
//		TopologyManager topologyManager = new TopologyManager(params, ted, log);
//		topologyManager.initTopology();
//
//
//		//************************ 都没用 ************************/
//		OPcounter = new OperationsCounter();
//
//		ChildPCESessionManager pcm=null;
//
//		RequestDispatcher PCCRequestDispatcherChild = null;
//		if (params.getParentPCEAddress()!=null){
//			//ChildPCERequestDispatcherParentPCE childPCERequestDispatcherParentPCE = new ChildPCERequestDispatcherParentPCE();
//			//For the session between the Domain (Child) PCE and the parent PCE
//			log.info("Initializing Manager of the ChildPCE - Parent PCE Session");
//			PCCRequestDispatcherChild=new  RequestDispatcher(1,ted,null,params.isAnalyzeRequestTime());
//			pcm=new ChildPCESessionManager(PCCRequestDispatcherChild, params,ted,ted.getReachabilityEntry().getDomainId(),pcepSessionsInformation,iniDispatcher);
//		}else {
//			log.info("There is no parent PCE");
//
//		}
//		/************************ 都没用 ************************/
//
//		//请求调度器
//		//The Request Dispatcher, needed to dispatch the requests coming from the PCCs
//		log.info("Initializing Request Dispatcher");
//		RequestDispatcher PCCRequestDispatcher;
//
//		/************************ 都没用 ************************/
//		ReservationManager reservationManager=null;
//		if (params.isReservation()){
//			log.info("Launching Reservation Manager");
//			reservationManager= new ReservationManager(ted);//FIXME: he hecho el casting
//		}
//		CollaborationPCESessionManager	collaborationPCESessionManager=null;
//		if ((params.getParentPCEAddress()!=null)){
//			if (params.isCollaborativePCEs()){//STRONGEST: Collaborative PCEs
//				collaborationPCESessionManager = new CollaborationPCESessionManager();
//				PCCRequestDispatcher=new  RequestDispatcher(params.getPCCRequestsProcessors(),ted,pcm.getChildPCERequestManager(),params.isAnalyzeRequestTime(),collaborationPCESessionManager);
//			}
//			else
//				PCCRequestDispatcher=new  RequestDispatcher(params.getPCCRequestsProcessors(),ted,pcm.getChildPCERequestManager(),params.isAnalyzeRequestTime());
//		}
//
//		else {
//			if (params.isMultilayer()== true){
//				PCCRequestDispatcher=new  RequestDispatcher(params.getPCCRequestsProcessors(),ted,null,params.isAnalyzeRequestTime(),params.isUseMaxReqTime(), reservationManager, OPcounter, params.isMultilayer());
//			}
//
//			else if (params.isCollaborativePCEs()){//STRONGEST: Collabotarive PCEs
//				collaborationPCESessionManager = new CollaborationPCESessionManager();
//				PCCRequestDispatcher=new  RequestDispatcher(params.getPCCRequestsProcessors(),ted,null,params.isAnalyzeRequestTime(),params.isUseMaxReqTime(), reservationManager,collaborationPCESessionManager);
//			}
//			/************************ 都没用 ************************/
//			//PCC请求数，配置 = 1，可以修改，请求中没有带计算路径请求
//			//PCC请求调度器，启动多个PCC请求线程
//			else
//				PCCRequestDispatcher=new  RequestDispatcher(params.getPCCRequestsProcessors(),ted,null,params.isAnalyzeRequestTime(),params.isUseMaxReqTime(), reservationManager);
//		}
//
//		if(params.ITcapable==false){
////			if (params.isMultilayer() == false)
////			{
////				((SimpleTEDB)ted).setRequestDispatcher(PCCRequestDispatcher);
////			}
//
//		}
//
//		//The Reservation Manager,
//		NotificationDispatcher nd=new NotificationDispatcher(reservationManager);
//
//
//		if(params.algorithmRuleList.size()==0){
//
//			log.warn("No hay algoritmos registrados!");
//			//System.exit(1);
//
//		}
//		//FIXME: cambiar esto de orden
//
//		/************************ 都没用 ************************/
//		Timer timer=new Timer();
//		if (params.getParentPCEAddress()!=null){
//			log.info("Inizializing Session with Parent PCE");
//			timer.schedule(pcm, 0, 1000000);
//		}
//		/************************ 都没用 ************************/
//
//		//PCE管理服务器，等待session
//		pms=new PCEManagementSever(PCCRequestDispatcher,ted,params, reservationManager,collaborationPCESessionManager);
//		pms.start();
//
//		SendTopologyTask stg=null;
//		ITSendTopologyTask ITstg=null;
//
//		if (params.getParentPCEAddress()!=null){
//			if(params.ITcapable==true){
//				ITstg=new ITSendTopologyTask((SimpleITTEDB)ted,pcm);
//
//				Timer timer2=new Timer();
//				if (params.getParentPCEAddress()!=null){
//					log.info("Changing topology");
//					timer2.schedule(ITstg, 0, 100000);
//				}
//			}else if (!(params.isActingAsBGP4Peer())){
//				stg=new SendTopologyTask((DomainTEDB)ted,pcm);
//
//				Timer timer2=new Timer();
//				if (params.getParentPCEAddress()!=null){
//					log.info("Changing topology");
//					timer2.schedule(stg, 0, 100000);
//				}
//			}
//		}
//
//		SendReachabilityTask srt= new SendReachabilityTask(ted,pcm);
//		Timer timer3=new Timer();
//		if (params.getParentPCEAddress()!=null){
//			timer3.schedule(srt, 0, params.getTimeSendReachabilityTime());
//		}
//		//STRONGEST: Collaborative PCEs
//		if (params.isCollaborativePCEs()){
//			if (!(params.isPrimary())){	//If Backup PCE
//				BackupSessionManagerTask backupSessionTask =null;
//				backupSessionTask= new BackupSessionManagerTask(params,ted,collaborationPCESessionManager,nd,pcepSessionsInformation);
//				//backupSessionTask.manageBackupPCESession();
//				Timer timerBackupSession=new Timer();
//				log.info("Inizializing Session with Primary PCE");
//				timerBackupSession.schedule(backupSessionTask, 0, 100000);
//			}
//		}
//		else
//			log.info("There are no collaborative PCEs");
//
//		listening = true;
//		try {
//			log.info("Listening on port: "+params.getPCEServerPort());
//
//			// Local PCE address for multiple network interfaces in a single computer
//
//			log.info("Listening on address: "+params.getLocalPceAddress());
//
//
//			//服务器开启监听
//			serverSocket = new ServerSocket(params.getPCEServerPort(),0,(Inet4Address) InetAddress.getByName(params.getLocalPceAddress()));
//
//
//		} catch (IOException e) {
//			System.err.println("Could not listen on port: "+params.getPCEServerPort());
//			System.exit(-1);
//		}
//
//		//algorithmRuleList
//		try {
//			//FIXME: lo he cambiado aqu�
//			//Registration of Algorithms
//			for (int i=0;i<params.algorithmRuleList.size();++i){
//				try {
//					Class<?> aClass = Class.forName("es.tid.pce.computingEngine.algorithms."+params.algorithmRuleList.get(i).algoName+"Manager");
//					log.info("Registering algorithm "+ params.algorithmRuleList.get(i).algoName+" for of = "+params.algorithmRuleList.get(i).ar.of+" and svec = "+params.algorithmRuleList.get(i).ar.svec);
//					if (params.algorithmRuleList.get(i).isParentPCEAlgorithm==false){
//						if(params.algorithmRuleList.get(i).isSSSONAlgorithm==false){
//							ComputingAlgorithmManager cam= ( ComputingAlgorithmManager)aClass.newInstance();
//							PCCRequestDispatcher.registerAlgorithm(params.algorithmRuleList.get(i).ar, cam);
//							if ((params.getParentPCEAddress()!=null)){
//								PCCRequestDispatcherChild.registerAlgorithm(params.algorithmRuleList.get(i).ar, cam);
//							}
//							try{
//
//								Class<?> aClass2 = Class.forName("es.tid.pce.computingEngine.algorithms."+params.algorithmRuleList.get(i).algoName+"PreComputation");
//								ComputingAlgorithmPreComputation cam2= ( ComputingAlgorithmPreComputation)aClass2.newInstance();
//
//								cam2.setTEDB(ted);
//
//								cam2.initialize();
//								cam.setPreComputation(cam2);
//								((DomainTEDB) ted).register(cam2);
//								cam.setReservationManager(reservationManager);
//							}
//							catch (Exception e2){
//								e2.printStackTrace();
//								log.warn("No precomputation in "+"es.tid.pce.computingEngine.algorithms."+params.algorithmRuleList.get(i).algoName+"PreComputation");
//							}
//						}
//						else{
//							ComputingAlgorithmManagerSSON cam_sson= (ComputingAlgorithmManagerSSON)aClass.newInstance();
//							PCCRequestDispatcher.registerAlgorithmSSON(params.algorithmRuleList.get(i).ar, cam_sson);
//							if ((params.getParentPCEAddress()!=null)){
//								PCCRequestDispatcherChild.registerAlgorithmSSON(params.algorithmRuleList.get(i).ar, cam_sson);
//
//							}
//							try{
//								Class<?> aClass2 = Class.forName("es.tid.pce.computingEngine.algorithms."+params.algorithmRuleList.get(i).algoName+"PreComputation");
//								ComputingAlgorithmPreComputationSSON cam3= ( ComputingAlgorithmPreComputationSSON)aClass2.newInstance();
//								cam3.setTEDB(ted);
//								cam3.initialize();
//								cam_sson.setPreComputation(cam3);
//								((DomainTEDB) ted).registerSSON(cam3);
//								cam_sson.setReservationManager(reservationManager);
//							}
//							catch (Exception e2){
//								e2.printStackTrace();
//								log.warn("No precomputation in "+"es.tid.pce.computingEngine.algorithms."+params.algorithmRuleList.get(i).algoName+"PreComputation");
//							}
//						}
//					}
//
//					//No registro los algoritmos que sean de parentPCE
//				} catch (ClassNotFoundException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (InstantiationException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (IllegalAccessException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			if ((params.getLambdaEnd()!=Integer.MAX_VALUE)&&(params.ITcapable==false)&&((params.isMultilayer())==false))
//				((SimpleTEDB)ted).notifyAlgorithms( params.getLambdaIni(),params.getLambdaEnd());
//
//
//
//
//			// This parameter tells the dispatcher that sync will be avoided.
//			// In better future times sync should be implemented
//
//			ReportDispatcher PCCReportDispatcher = null;
//			//
//			if (pcepSessionsInformation.isStateful())
//			{
//			log.info("redis: "+params.getDbType() + " "+params.getDbName());
//				if (params.getDbType().equals("redis") && params.getDbName().length() > 0)
//				{
//					log.info("redis: redis db with id: "+ params.getDbName());
//					rptdb = new ReportDB_Handler(params.getDbName(),"localhost");
//					rptdb.fillFromDB();
//				}
//				else
//				{
//					rptdb = new ReportDB_Handler();
//				}
//				params.setLspDB(rptdb);
//				log.info("Creando dispatchers para el LSP DB");
//				PCCReportDispatcher = new ReportDispatcher( rptdb, 2);
//			}
//
//
//			//while (listening) {
//			while (listening) {
//				System.out.println("listening");
//				//new PCESession(serverSocket.accept(),params, PCCRequestsQueue,ted,pcm.getChildPCERequestManager()).start();
//				//null,ted,pcm.getChildPCERequestManager()).start(
//				if (params.isCollaborativePCEs())
//					new DomainPCESession(serverSocket.accept(),params,PCCRequestDispatcher,ted,nd,reservationManager,collaborationPCESessionManager,pcepSessionsInformation,PCCReportDispatcher).start();
//				else {
//					new DomainPCESession(serverSocket.accept(),params,PCCRequestDispatcher,ted,nd,reservationManager,pcepSessionsInformation,PCCReportDispatcher,iniDispatcher).start();
//				}
//				System.out.println("connected!!!");
//			}
//			serverSocket.close();
//
//		} catch (SocketException e) {
//			if (listening==false){
//				log.info("Socket closed due to controlled close");
//			}else {
//				log.error("Problem with the socket, exiting");
//				e.printStackTrace();
//			}
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//		System.out.println("End Running Server!!!!!!!!!!!!!!!");
//	}


	public void run(){
		log.info("|------------------------------- 运行PCE服务器 -------------------------------|");

		//载入PCE sessions信息来自配置文件
		PCEPSessionsInformation pcepSessionsInformation = new PCEPSessionsInformation();
		// PCE有无状态
		pcepSessionsInformation.setStateful(params.isStateful());
		//D、S、T flag
		pcepSessionsInformation.setStatefulDFlag(params.isStatefulDFlag());
		pcepSessionsInformation.setStatefulSFlag(params.isStatefulSFlag());
		pcepSessionsInformation.setStatefulTFlag(params.isStatefulTFlag());
		//其他pcep信息
		pcepSessionsInformation.setActive(params.isActive());
		pcepSessionsInformation.setSRCapable(params.isSRCapable());
		pcepSessionsInformation.setMSD(params.getMSD());

		//TEDB
		DomainTEDB ted;
		ted=new SimpleTEDB();
		ted=new SimpleTEDB();

		//从配置文件读入拓扑
		TopologyManager topologyManager = new TopologyManager(params, ted, log);
		topologyManager.initTopology();

		//有状态服务器
		//单域初始化调度器
		SingleDomainInitiateDispatcher iniDispatcher=null;
		//单域LSP数据库
		SingleDomainLSPDB singleDomainLSPDB=null;
		//初始PCC管理器
		IniPCCManager iniManager=null;


		//有状态服务器
		if (params.isStateful())
		{
			//有状态PCE标志 with T=false D=false S=false
			log.info("Stateful PCE with T="+params.isStatefulTFlag()+" D="+params.isStatefulDFlag()+" S="+params.isStatefulSFlag());
			//单域LSP数据库
			singleDomainLSPDB=new SingleDomainLSPDB();
			//dbType默认是_，从服务器配置文件中读取
			if(params.getDbType().equals("_"))
			{
				singleDomainLSPDB.setExportDb(false);
			}
			//PCC初始配置
			iniManager= new IniPCCManager();
			//单域初始配置调度器
			iniDispatcher = new SingleDomainInitiateDispatcher(singleDomainLSPDB,iniManager);
		}

		//操作计数器
		OPcounter = new OperationsCounter();
		//子会话管理器
		ChildPCESessionManager pcm=null;
		//子请求调度器
		RequestDispatcher PCCRequestDispatcherChild = null;
		//请求调度器
		log.info("初始化请求调度器");
		RequestDispatcher PCCRequestDispatcher;

		//保留管理器
		ReservationManager reservationManager=null;
		reservationManager= new ReservationManager(ted);
		CollaborationPCESessionManager	collaborationPCESessionManager=null;
		//PCC请求调度器
		PCCRequestDispatcher=new  RequestDispatcher(params.getPCCRequestsProcessors(),ted,null,params.isAnalyzeRequestTime(),params.isUseMaxReqTime(), reservationManager);
		NotificationDispatcher nd = new NotificationDispatcher(reservationManager);
		//PCE管理服务器，等待session
		pms=new PCEManagementSever(PCCRequestDispatcher, ted, params, reservationManager, collaborationPCESessionManager);
		pms.start();

		listening = true;
		try {
			log.info("Listening on port: "+params.getPCEServerPort());
			log.info("Listening on address: "+params.getLocalPceAddress());
			serverSocket = new ServerSocket(params.getPCEServerPort(),0,(Inet4Address) InetAddress.getByName(params.getLocalPceAddress()));
		} catch (IOException e) {
			System.err.println("Could not listen on port: "+params.getPCEServerPort());
			System.exit(-1);
		}

		//路径计算算法
		//cam 路径计算算法管理器
		//cam2 路径计算算法预计算
		//cam3 路径计算算法SSON
		try {
			for (int i=0;i<params.algorithmRuleList.size();++i){
				try {
					Class<?> aClass = Class.forName("es.tid.pce.computingEngine.algorithms."+params.algorithmRuleList.get(i).algoName+"Manager");
					log.info("Registering algorithm "+ params.algorithmRuleList.get(i).algoName+" for of = "+params.algorithmRuleList.get(i).ar.of+" and svec = "+params.algorithmRuleList.get(i).ar.svec);
					if (params.algorithmRuleList.get(i).isParentPCEAlgorithm==false){
						if(params.algorithmRuleList.get(i).isSSSONAlgorithm==false){
							ComputingAlgorithmManager cam = (ComputingAlgorithmManager)aClass.newInstance();
							PCCRequestDispatcher.registerAlgorithm(params.algorithmRuleList.get(i).ar, cam);
							try{
								//预计算
								Class<?> aClass2 = Class.forName("es.tid.pce.computingEngine.algorithms."+params.algorithmRuleList.get(i).algoName+"PreComputation");
								ComputingAlgorithmPreComputation cam2= ( ComputingAlgorithmPreComputation)aClass2.newInstance();
								cam2.setTEDB(ted);
								cam2.initialize();
								//计算设置预计算
								cam.setPreComputation(cam2);
								((DomainTEDB) ted).register(cam2);
								cam.setReservationManager(reservationManager);
							}
							catch (Exception e2){
								e2.printStackTrace();
								log.warn("No precomputation in "+"es.tid.pce.computingEngine.algorithms."+params.algorithmRuleList.get(i).algoName+"PreComputation");
							}
						}
						else{
							ComputingAlgorithmManagerSSON cam_sson= (ComputingAlgorithmManagerSSON)aClass.newInstance();
							PCCRequestDispatcher.registerAlgorithmSSON(params.algorithmRuleList.get(i).ar, cam_sson);
							try{
								Class<?> aClass2 = Class.forName("es.tid.pce.computingEngine.algorithms."+params.algorithmRuleList.get(i).algoName+"PreComputation");
								ComputingAlgorithmPreComputationSSON cam3= ( ComputingAlgorithmPreComputationSSON)aClass2.newInstance();
								cam3.setTEDB(ted);
								cam3.initialize();
								cam_sson.setPreComputation(cam3);
								((DomainTEDB) ted).registerSSON(cam3);
								cam_sson.setReservationManager(reservationManager);
							}
							catch (Exception e2){
								e2.printStackTrace();
								log.warn("No precomputation in "+"es.tid.pce.computingEngine.algorithms."+params.algorithmRuleList.get(i).algoName+"PreComputation");
							}
						}
					}
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//if ((params.getLambdaEnd()!=Integer.MAX_VALUE)&&(params.ITcapable==false)&&((params.isMultilayer())==false))
			//	((SimpleTEDB)ted).notifyAlgorithms( params.getLambdaIni(),params.getLambdaEnd());
			//报告调度器
			ReportDispatcher PCCReportDispatcher = null;
			//
			if (pcepSessionsInformation.isStateful())
			{
				log.info("redis: "+params.getDbType() + " "+params.getDbName());
				if (params.getDbType().equals("redis") && params.getDbName().length() > 0)
				{
					log.info("redis: redis db with id: "+ params.getDbName());
					rptdb = new ReportDB_Handler(params.getDbName(),"localhost");
					rptdb.fillFromDB();
				}
				else
				{
					rptdb = new ReportDB_Handler();
				}
				params.setLspDB(rptdb);
				log.info("Creando dispatchers para el LSP DB");
				PCCReportDispatcher = new ReportDispatcher( rptdb, 2);
			}


			while (listening) {
				System.out.println("listening");
				new DomainPCESession(serverSocket.accept(),params,PCCRequestDispatcher,ted,nd,reservationManager,pcepSessionsInformation,PCCReportDispatcher,iniDispatcher).start();
				System.out.println("connected!!!");
			}
			serverSocket.close();

		} catch (SocketException e) {
			if (listening==false){
				log.info("Socket closed due to controlled close");
			}else {
				log.error("Problem with the socket, exiting");
				e.printStackTrace();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("End Running Server!!!!!!!!!!!!!!!");
	}

	public void stopServer(){
		pms.stopServer();
		listening=false;
		if (serverSocket!=null){
			try {
				serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

}
