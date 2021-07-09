package es.tid.pce.tests;

import com.sun.org.apache.xalan.internal.lib.NodeInfo;
import es.tid.pce.client.QuickClientObj;
import es.tid.pce.pcep.constructs.Request;
import es.tid.pce.pcep.messages.PCEPMessage;
import es.tid.pce.pcep.messages.PCEPRequest;
import es.tid.pce.pcep.messages.PCEPResponse;
import es.tid.pce.pcep.objects.ExplicitRouteObject;
import es.tid.pce.server.DomainPCEServer;
import es.tid.pce.server.PCEServerParameters;
import es.tid.pce.server.TopologyManager;
import es.tid.tedb.*;
import org.apache.commons.cli.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class PCETester {
    private PCEServerParameters params;
    private DomainTEDB ted;
    public static final Logger log = LoggerFactory.getLogger("PCETester");
    private String configFile = "PCEConfig.xml";
    private int MOD = 5;
    private PrintWriter out;
    private int NODES_NUM = 25;
    private int[] degree = new int[NODES_NUM];
    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(5) ;
    private String[] msgSend;
    private int[][] graph = {
      //     1             2             3             4             5
        //1.1
        {0,1,1,0,0,    1,1,0,0,0,    0,0,0,0,0,    0,0,0,0,0,    0,0,0,0,0},
        //1.2
        {1,0,1,0,0,    0,0,0,0,0,    0,0,0,0,0,    1,1,0,0,0,    0,0,0,0,0},
        //1.3
        {1,1,0,1,1,    0,0,0,0,0,    0,0,0,0,0,    0,0,0,0,0,    0,0,0,0,0},
        //1.4
        {0,0,1,0,1,    1,1,0,0,0,    1,1,0,0,0,    0,0,0,0,0,    0,0,0,0,0},
        //1.5
        {0,0,1,1,0,    0,0,0,0,0,    1,1,0,0,0,    1,1,0,0,0,    0,0,0,0,0},

        //2.1
        {1,0,0,1,0,    0,1,1,0,0,    0,0,0,0,0,    0,0,0,0,0,    0,0,0,0,0},
        //2.2
        {1,0,0,1,0,    1,0,1,0,0,    1,0,0,1,0,    0,0,0,0,0,    0,0,0,0,0},
        //2.3
        {0,0,0,0,0,    1,1,0,1,1,    0,0,0,0,0,    0,0,0,0,0,    0,0,0,0,0},
        //2.4
        {0,0,0,0,0,    0,0,1,0,1,    0,0,0,0,0,    0,0,0,0,0,    1,0,0,1,0},
        //2.5
        {0,0,0,0,0,    0,0,1,1,0,    1,0,0,1,0,    0,0,0,0,0,    1,0,0,1,0},

        //3.1
        {0,0,0,1,1,    0,1,0,0,1,    0,1,1,0,0,    0,0,0,0,0,    0,0,0,0,0},
        //3.2
        {0,0,0,1,1,    0,0,0,0,0,    1,0,1,0,0,    1,0,0,1,0,    0,0,0,0,0},
        //3.3
        {0,0,0,0,0,    0,0,0,0,0,    1,1,0,1,1,    0,0,0,0,0,    0,0,0,0,0},
        //3.4
        {0,0,0,0,0,    0,1,0,0,1,    0,0,1,0,1,    0,0,0,0,0,    1,1,0,0,0},
        //3.5
        {0,0,0,0,0,    0,0,0,0,0,    0,0,1,1,0,    1,0,0,1,0,    1,1,0,0,0},

        //4.1
        {0,1,0,0,1,    0,0,0,0,0,    0,1,0,0,1,    0,1,1,0,0,    0,0,0,0,0},
        //4.2
        {0,1,0,0,1,    0,0,0,0,0,    0,0,0,0,0,    1,0,1,0,0,    0,0,0,0,0},
        //4.3
        {0,0,0,0,0,    0,0,0,0,0,    0,0,0,0,0,    1,1,0,1,1,    0,0,0,0,0},
        //4.4
        {0,0,0,0,0,    0,0,0,0,0,    0,1,0,0,1,    0,0,1,0,1,    0,1,0,0,1},
        //4.5
        {0,0,0,0,0,    0,0,0,0,0,    0,0,0,0,0,    0,0,1,1,0,    0,1,0,0,1},

        //5.1
        {0,0,0,0,0,    0,0,0,1,1,    0,0,0,1,1,    0,0,0,0,0,    0,1,1,0,0},
        //5.2
        {0,0,0,0,0,    0,0,0,0,0,    0,0,0,1,1,    0,0,0,1,1,    1,0,1,0,0},
        //5.3
        {0,0,0,0,0,    0,0,0,0,0,    0,0,0,0,0,    0,0,0,0,0,    1,1,0,1,1},
        //5.4
        {0,0,0,0,0,    0,0,0,1,1,    0,0,0,0,0,    0,0,0,0,0,    0,0,1,0,1},
        //5.5
        {0,0,0,0,0,    0,0,0,0,0,    0,0,0,0,0,    0,0,0,1,1,    0,0,1,1,0},
    };
    @Test
    public void test (){
        startPCEServer();
    }
    //Thread
    //main
    //DomainPCEServer Thread
    //PCEManagementSever
    //RequestProcessorThread
    @Test
    public void startPCEServer() {
        //PCE服务器
        params = new PCEServerParameters(configFile);
        params.initialize();
        DomainPCEServer pceserver = new DomainPCEServer();
        pceserver.configure(configFile);
        //executor.execute(pceserver);
        Thread t1 = new Thread(pceserver);
        t1.start();
        try {
            Thread.sleep(100000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testPCCClient() {
        String[] msg = {"localhost 4189 10.0.1.1 10.0.5.1 -g -of 1002 -rgbw 2"};
        startPCCClient(msg[0]);
    }

    public void startPCCClient(String msg) {
        //发送计算请求信息
        this.msgSend = msg.split(" ");
//        for(String str : msgSend)
//            System.out.println("str = " + str);
        try {
            CommandLine optReq = QuickClientObj.getLineOptions(this.msgSend);
//            String[] argList = optReq.getArgs();
//            Option[] options = optReq.getOptions();
//            for(String arg : argList)
//                System.out.println("arg = " + arg);
//            for(Option op : options) {
//                System.out.println(op);
//                System.out.println("Description = "+op.getDescription()+", argName = "+op.getArgName()+", value = "+op.getValue());
//            }
            // log, localhost, 4189, 快速客户端连接到服务器
            QuickClientObj qcObj = new QuickClientObj(log, msgSend[0], Integer.valueOf(msgSend[1]).intValue());
            qcObj.start();
            //src 192.168.1.2 dest 192.168.1.5

            //建立请求对象
//            Request req = qcObj.createReqMessage(msgSend[2],msgSend[3], optReq);
//
//            PCEPRequest p_r = new PCEPRequest();
//            p_r.addRequest(req);
//            LinkedList<PCEPMessage> messageList=new LinkedList<PCEPMessage>();
//            PCEPResponse res = qcObj.sendReqMessage(p_r, messageList);
//            ExplicitRouteObject eroRes = res.getResponseList().getFirst().getPath(0).geteRO();
//			log.info("ERO en respuesta: "+eroRes);
//			System.out.println("ERO en respuesta: "+eroRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void loadTEDB() {
        //TEDB
        /*ted = new SimpleTEDB();
        log = LoggerFactory.getLogger("TEDBTester");
        TopologyManager topo = new TopologyManager(params, ted, log);
        topo.initTopology();*/

        //输出TEDB
        //log.info(ted.printTopology());
        //log.info(String.valueOf(ted.getDomainID()));
        //ted.getNodeTable();
        //System.out.println(ted.toString());
    }

    @Test
    public void generateTopo() {
        try {
            out = new PrintWriter(
                    new OutputStreamWriter(
                            new FileOutputStream("topo.xml"),"UTF-8"
                    ),true);

            generateNetwork();
            System.out.println("Done!!!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void generateNetwork() {
        printWithTab(0,"<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        int tab = 0;
        printWithTab(tab, "<network>");
        //edgeCommon
        generateEdgeCommon(tab+1);
        for(int i = 1;i <= 5;i++)
            generateDomain(i, tab+1);
        printWithTab(tab, "</network>");
    }
    public void generateDomain(int idx, int tab) {
        printWithTab(tab, "<!--\tdomain "+idx+"\t-->");
        //domain
        printWithTab(tab, "<domain>");
        int tabPlus = tab+1;
        printWithTab(tabPlus, "<layer type=\"mpls\" ></layer>");
        printWithTab(tabPlus, "<domain_id>0.0.0." + idx + "</domain_id>");
        //ReachabilityEntry
        generateReachabilityEntry(tab+1, idx);
        //Nodes
        int nodeCnt = 5;
        generateNodes(tab+1, idx, nodeCnt);
        //edges
        generateEdges(tab+1, idx);
        //printWithTab(tabPlus, idx+"");
        printWithTab(tab, "</domain>");
    }
    public void generateEdgeCommon(int tab) {
        printWithTab(tab, "<edgeCommon>");
        printWithTab(tab+1, "<AvailableLabels>");
        printWithTab(tab+2, "<LabelSetField type=\"4\">");
        printWithTab(tab+3, "<numLabels>160</numLabels>");
        printWithTab(tab+3, "<baseLabel grid=\"3\" cs=\"5\" n=\"0\"></baseLabel>");
        printWithTab(tab+2, "</LabelSetField>");
        printWithTab(tab+1, "</AvailableLabels>");
        printWithTab(tab, "</edgeCommon>");
    }
    public void generateReachabilityEntry(int tab, int idx) {
        printWithTab(tab, "<reachability_entry>");
        printWithTab(tab+1, "<ipv4_address>10.0."+ idx +".0</ipv4_address>");
        printWithTab(tab+1, "<prefix>24</prefix>");
        printWithTab(tab, "</reachability_entry>");
    }
    public void generateNodes(int tab, int idx,int nodeCnt) {
        for(int i = 1;i <= nodeCnt;i++) {
            printWithTab(tab, "<node>");
            printWithTab(tab+1, "<router_id>10.0." + idx + "." + i + "</router_id>");
            printWithTab(tab, "</node>");
        }
    }
    public void generateEdges(int tab, int idx) {
        int minIndex = (idx-1) * MOD;
        int maxIndex = idx * MOD;
        for(int i = minIndex;i < maxIndex;i++) {
            for(int j = 0;j < NODES_NUM;j++) {
                int s1 = i/MOD+1, s2 = i%MOD+1;
                int e1 = j/MOD+1, e2 = j%MOD+1;
                if(graph[i][j] == 1) {
                    //if(graph[j][i] == 0)
                        //System.out.println(s1 + "." + s2 + " => " + e1 + "." + e2);
                    printWithTab(tab, "<!--\t" + s1 + "." + s2 + " => " + e1 + "." + e2 + "\t-->");
                    printWithTab(tab, "<edge type='interdomain'>");
                    printWithTab(tab+1, "<source>");
                    printWithTab(tab+2, "<router_id>10.0."+s1+"."+s2+"</router_id>");
                    printWithTab(tab+2, "<if_id>"+(++degree[i])+"</if_id>");
                    printWithTab(tab+1, "</source>");

                    printWithTab(tab+1, "<destination>");
                    printWithTab(tab+2, "<router_id>10.0."+e1+"."+e2+"</router_id>");
                    printWithTab(tab+2, "<if_id>"+(++degree[j])+"</if_id>");
                    printWithTab(tab+1, "</destination>");

                    printWithTab(tab+1, "<delay>"+100+"</delay>");
                    printWithTab(tab+1, "<bandwidth>"+100+"</bandwidth>");
                    printWithTab(tab+1, "<maximum_bandwidth>"+100+"</maximum_bandwidth>");

                    printWithTab(tab, "</edge>");
                }

            }
        }


    }
    public void printWithTab(int tab, String str) {
        for(int i = 0;i < tab;i++)
            out.print("\t");
        out.println(str);
    }
}
