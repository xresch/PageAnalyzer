package com.pengtoolbox.pageanalyzer.phantomjs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
class StreamCatcher extends Thread
{
    private InputStream is;
    private String type;
    private StringBuffer catchedData = new StringBuffer();
    
    StreamCatcher(InputStream is, String type)
    {
        this.is = is;
        this.type = type;
    }
    
    public void run()
    {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        
        try
        {
        	
	        while(!br.ready()) {
	    		Thread.sleep(200);
	    	}

            String line=null;
            while ((line = br.readLine()) != null) {
                //DEBUG System.out.println(type + ">" + line); 
            	catchedData.append(line+"\n");
            }      
        } catch (IOException ioe){
            ioe.printStackTrace();  
        } catch (InterruptedException e) {
			e.printStackTrace();
		} finally{
        	try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
    
    public String getCatchedData(){
    	return catchedData.toString();
    }
}