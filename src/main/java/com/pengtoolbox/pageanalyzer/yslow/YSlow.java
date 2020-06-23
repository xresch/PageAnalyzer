package com.pengtoolbox.pageanalyzer.yslow;

import java.util.logging.Logger;

import com.pengtoolbox.cfw.logging.CFWLog;

import javafx.application.Platform;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class YSlow {

	private static YSlow INSTANCE = null;
	private static Logger logger = Logger.getLogger(YSlow.class.getName());
	
	/***********************************************************************
	 * 
	 ***********************************************************************/
	private YSlow(){
		
		//ExecutionContextPool.initializeExecutors(10);
	}
	
	/***********************************************************************
	 * 
	 ***********************************************************************/
	public static YSlow instance(){
		
		if(INSTANCE == null){
			INSTANCE = new YSlow();
			
		}
		return INSTANCE;
		
	}

	/***********************************************************************
	 * 
	 ***********************************************************************/
	public String analyzeHarString(String harString){
		
		CFWLog log = new CFWLog(logger);
		
		log.start().method("analyzeHarString");
		
		ExecutionContext context = ExecutionContextPool.lockContext();

		//----------------------------------------------
		// Execute the Java FX Application.
		// It will set the Result on the singelton instance		
		Platform.runLater(new Runnable(){
			@Override
			public void run() {
				YSlowExecutorJavaFX.analyzeHARString(context, harString);
			}
		});
		
		//----------------------------------------------
		//wait for result, max 50 seconds
		for(int i = 0; !context.isResultUpdated() && i < 100; i++){
			try {
				System.out.println("wait");
				Thread.sleep(500);
			} catch (InterruptedException e) {
				log.severe("Thread was interrupted.", e);
			}
		}
		String result = context.getResult();
		ExecutionContextPool.releaseContext(context);
		log.end();
		
		return result;
	}

	
}
