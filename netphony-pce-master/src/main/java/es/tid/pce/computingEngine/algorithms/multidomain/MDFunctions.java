package es.tid.pce.computingEngine.algorithms.multidomain;

import java.net.Inet4Address;
import java.util.Iterator;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jgrapht.graph.DirectedWeightedMultigraph;

import es.tid.pce.pcep.objects.ExcludeRouteObject;
import es.tid.pce.pcep.objects.subobjects.UnnumberIfIDXROSubobject;
import es.tid.pce.pcep.objects.subobjects.XROSubObjectValues;
import es.tid.pce.pcep.objects.subobjects.XROSubobject;
import es.tid.tedb.InterDomainEdge;
import es.tid.tedb.ReachabilityManager;

public class MDFunctions {
	
		 	
	public static void processXRO(ExcludeRouteObject xro,ReachabilityManager reachabilityManager, DirectedWeightedMultigraph<Object,InterDomainEdge> networkGraph){
		Logger log=LoggerFactory.getLogger("PCEServer");
		try{
		if (xro!=null){
			log.info("XRO: Thre are "+xro.getXROSubobjectList().size()+" exclusions");
			for (int i=0;i<xro.getXROSubobjectList().size();++i){
				XROSubobject eroso=xro.getXROSubobjectList().get(i);
				if (eroso.getType()==XROSubObjectValues.XRO_SUBOBJECT_UNNUMBERED_IF_ID){					
					UnnumberIfIDXROSubobject eros=(UnnumberIfIDXROSubobject)eroso;					
					log.info("XRO: UNNUMBERED_IF_ID TO EXCLUDE: "+eros.getRouterID()+":"+eros.getInterfaceID());
					Inet4Address source_domain_id=reachabilityManager.getDomain(eros.getRouterID());
					log.info("XRO: IT BELONGS TO DOMAIN "+source_domain_id);
					boolean hasVertex=networkGraph.containsVertex(source_domain_id);
					log.info("XRO: EL GRAPH TIENE VERTEX "+hasVertex);
					if (hasVertex){
						Set<InterDomainEdge> setEdges=networkGraph.edgesOf(source_domain_id);
						Iterator<InterDomainEdge> iter=setEdges.iterator();
						while (iter.hasNext()){
							InterDomainEdge edge=iter.next();
							log.info("XRO: LOOKING AT EDGE "+edge.getSrc_router_id());
							if (edge.getSrc_router_id().equals(eros.getRouterID())){
								if (edge.getSrc_if_id()==eros.getInterfaceID()){
								networkGraph.removeEdge(edge);				
								log.info("XRO: REMOVE EDGE "+eros.getRouterID()+":"+edge.getSrc_if_id());
								}
							}
						}
						
					}
				}
			}
		}
		}catch (Exception e){
			e.printStackTrace();
		}
		
	}
}
