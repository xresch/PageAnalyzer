package com.xresch.pageanalyzer.yslow;

import java.util.Stack;
import java.util.logging.Logger;

import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ExecutionContextPool {
	
	private static Stack<ExecutionContext> freeContextPool = new Stack<ExecutionContext>();
	private static Stack<ExecutionContext> lockedContextPool = new Stack<ExecutionContext>();

	private static Logger logger = Logger.getLogger(ExecutionContextPool.class.getName());
	
	/***********************************************************************
	 * 
	 ***********************************************************************/
	public static void initializeExecutors(int count) {
		
		for(int i = 0; i < count; i++) {

		}
		
		//wait for first executor to initialize, max 50 seconds
		for(int i = 0; freeContextPool.isEmpty() && i < 100; i++){
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/***********************************************************************
	 * Retrieve and lock a free context.
	 ***********************************************************************/
	public static ExecutionContext lockContext() {
		
		new CFWLog(logger)
			.finer("Before Lock: lockedExecutorPool["+lockedContextPool.size()+"], freeExecutorPool["+freeContextPool.size()+"]");
		
		ExecutionContext context = null;
		while(context == null) {
			
			synchronized(freeContextPool) {
				if(!freeContextPool.isEmpty()) {
					context =  freeContextPool.pop();
					context.reset();
					synchronized(lockedContextPool) {
						lockedContextPool.push(context);
					}
				}
			}
			
			if(context == null) {
				try {
					Thread.sleep(200);
					new CFWLog(logger).fine("Thread waiting for free executor.");
				} catch (InterruptedException e) {
					new CFWLog(logger).warn("Thread interrupted while taking a nap.", e);
					e.printStackTrace();
				}
			}
		}
		
		new CFWLog(logger)
			.finer("After Lock: lockedExecutorPool["+lockedContextPool.size()+"], freeExecutorPool["+freeContextPool.size()+"]");
		
		return context;
	}
	
	/***********************************************************************
	 * Release a context by unlocking it.
	 ***********************************************************************/
	public static void releaseContext(ExecutionContext context) {
		context.reset();
		synchronized(lockedContextPool) {
			lockedContextPool.remove(context);
		}
		synchronized(freeContextPool) {
			freeContextPool.push(context);
		}

		new CFWLog(logger)
		.info("After Release: lockedExecutorPool["+lockedContextPool.size()+"], freeExecutorPool["+freeContextPool.size()+"]");
	}

	/***********************************************************************
	 * 
	 ***********************************************************************/
	public static void addExecutor(ExecutionContext executor) {
		synchronized(freeContextPool) {
			freeContextPool.add(executor);
		}
	}
}
