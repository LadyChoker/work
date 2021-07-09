package es.tid.pce.computingEngine;

import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.tid.pce.pcep.messages.PCEPReport;
import es.tid.pce.server.PCEServerParameters;
import es.tid.pce.server.lspdb.ReportDB_Handler;

/**
 * The ReportDispatcher receives PCEPReport messages and decides what to do with:
 * Add information to the database, generate an error,...
 * @author jaume, the farruk
 *
 */

public class ReportDispatcher 
{
	/**
	 * Number of processing threads
	 */
	 private final int nThreads;
	 
	 /**
	  * Array of RequestProcessor Threads
	  */
	 private final ReportProcessorThread[] threads;
	 
	 
	 
	 /**
	  * Queue to add path computing requests.
	  * This queue is read by the request processor threads. 
	  */
	 private LinkedBlockingQueue<PCEPReport> reportMessageQueue;

	
	/**
	  * The logger
	  */
	private Logger log;
	
			
	public ReportDispatcher(ReportDB_Handler lspDB, int nThreads)
	{
		log=LoggerFactory.getLogger("PCEServer");
		
		
		this.nThreads = nThreads;
	    reportMessageQueue = new LinkedBlockingQueue<PCEPReport>();
	    threads = new ReportProcessorThread[nThreads];
        for (int i=0; i<this.nThreads; i++) {
        	log.info("Starting Request Processor Thread");	        	
            threads[i] = new ReportProcessorThread(reportMessageQueue, lspDB);
            threads[i].setPriority(Thread.MAX_PRIORITY);
            threads[i].start();
            
        }
	}
	
	public void dispatchReport(PCEPReport pcepReport)
	{	    
		reportMessageQueue.add(pcepReport);
    }
	

}
