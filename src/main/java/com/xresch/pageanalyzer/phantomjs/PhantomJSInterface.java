package com.xresch.pageanalyzer.phantomjs;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class PhantomJSInterface
{
	private static Logger logger = CFWLog.getLogger(PhantomJSInterface.class.getName());
	private static PhantomJSInterface INSTANCE = null;
	
	private PhantomJSInterface() {
		
	}
	public static PhantomJSInterface instance() {
		if(INSTANCE == null) {
			INSTANCE = new PhantomJSInterface();
		}
		
		return INSTANCE;
	}
	
    public String getHARStringForWebsite(HttpServletRequest request, String url)
    {
    	CFWLog log = new CFWLog(logger);
    	
    	log.start();
    	String returnValue = "";
    	
        if (url == null)
        {
        	log.severe("Please provide a valid URL.");
        	returnValue = "{\"Error\" : \"Please provide a valid URL.\"} ";
            
        }else{
        	
        	if(!url.startsWith("http")){
        		log.warn("URL does not start with required 'http*'. We prepend 'http://' for you and try it again.");
        		url = "http://"+url;
        	}
	        try
	        {            
	            String osName = System.getProperty("os.name" );
	            if( ! osName.startsWith("Win") )
	            {
	            	returnValue = "{\"Error\" : \"This feature is only available when running the Service on Windows.\"} ";
	            }
	            else {
	            	String command = "./resources/phantomjs/phantomjs2.1.1.exe ./resources/phantomjs/netsniff.js "+url;
	            	Runtime rt = Runtime.getRuntime();
	
	                Process proc = rt.exec(command);
	                
	                // any error message?
	                StreamCatcher errorCatcher = new StreamCatcher(proc.getErrorStream(), "ERROR");            
	                
	                // any output?
	                StreamCatcher outputCatcher = new StreamCatcher(proc.getInputStream(), "OUTPUT");
	                
	                // kick them off
	                errorCatcher.start();
	                outputCatcher.start();
	                
	                // any error???
	                int exitValue = proc.waitFor();
	                
	                if(exitValue != 0){
	                	log.severe("PhantomJS process returned with an error. "+errorCatcher.getCatchedData());
	                	returnValue = "{\"Error\" : \"PhantomJS process returned with an error state '"+
	                					exitValue+"' and the error output: '"+
	                					errorCatcher.getCatchedData()+"'.\"} ";
	                }else{
	                	returnValue = outputCatcher.getCatchedData();
	                }
	            }
	            
	                 
	        } catch (Throwable t) {
	        	log.severe("Internal Error: Issue with threading.", t);
	        } 
        }
        
        log.end();
        return returnValue;
    }
}
