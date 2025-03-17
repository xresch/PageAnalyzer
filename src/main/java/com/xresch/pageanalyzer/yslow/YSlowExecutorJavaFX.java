package com.xresch.pageanalyzer.yslow;

import java.io.StringWriter;
import java.util.logging.Level;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import com.sun.javafx.webkit.WebConsoleListener;
import com.xresch.cfw._main.CFWProperties;
import com.xresch.cfw.utils.files.CFWFiles;
import com.xresch.pageanalyzer._main.Main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class YSlowExecutorJavaFX extends Application {
	
	private WebEngine engine;
	private static YSlowExecutorJavaFX INSTANCE;
	
	//private static Logger logger = Logger.getLogger(YSlowExecutor.class.getName());
    
	/***********************************************************************
	 * 
	 ***********************************************************************/
	public static YSlowExecutorJavaFX instance() {
		if(INSTANCE == null) {
			INSTANCE = new YSlowExecutorJavaFX();
			YSlowExecutorThread thread = new YSlowExecutorThread();
			thread.start();
		}
		
		return INSTANCE;
	}
	
	/***********************************************************************
	 * 
	 ***********************************************************************/
	@Override
	public void init() throws Exception {
		super.init();
	}
	
	/***********************************************************************
	 * 
	 ***********************************************************************/
	public void start(Stage stage){
		
		String yslowJS = CFWFiles.getFileContent(null, "./resources/js/custom_yslow.js");
		
		int contextCount = CFWProperties.configAsInt("pa_analysis_threads", 10);
		
		Main.javafxLogWorkaround(Level.INFO, "Create "+contextCount+" execution context for analysis", "YSlowExecutor.start()");
		for(int i = 0; i < contextCount; i++) {
			
			//--------------------------
			// Create Web View
			WebView view = new WebView();
			stage.setScene(new Scene(view, 900, 600));
			
			engine = view.getEngine();
			
			WebConsoleListener.setDefaultListener((webView, message, lineNumber, sourceId) -> {
			    String log = "[JS at line "+ lineNumber + "]"+ message;
				Main.javafxLogWorkaround(Level.INFO, log, "YSlowJavascriptConsoleOutput");
			});
	
			engine.setJavaScriptEnabled(true);
			engine.loadContent("<html><head></head><body><script language=\"javascript\">"+yslowJS+"</script>Hello World</body></html>");
			
			//--------------------------
			// Create Context
			ExecutionContext context = new ExecutionContext();
			context.setView(view);
			ExecutionContextPool.addExecutor(context);
		}
		
//		engine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
//		    if (newState == State.SUCCEEDED) {
//		        //Document doc = engine.getDocument();
//		        //System.out.println(debugGetStringFromDocument(doc));
//		        
//		        JSObject window = (JSObject)engine.executeScript("window");
//				String result = (String) window.call("analyzeHARString", harString);
//				YSlow.instance().setResult(result);
//		    }
//		});
				
	}
	
	/***********************************************************************
	 * 
	 ***********************************************************************/
	public static void analyzeHARString(ExecutionContext context, String harString){
		try{
			JSObject window = (JSObject)context.getView().getEngine().executeScript("window");
			String result = (String) window.call("analyzeHARString", harString);
			context.setResult(result);
		}catch(Exception e){
			Main.javafxLogWorkaround(Level.INFO, e.getMessage(), e, "YSlowExecutor.analyzeHARString()");
			context.setResult("{\"error\": \""+e.getMessage().replace("\"", "'")+" - Check if your HAR file is a valid JSON file. \"}");
		}
				
	}

	/***********************************************************************
	 * 
	 ***********************************************************************/
	public String debugGetStringFromDocument(Document doc)
	{
	    try
	    {
	       DOMSource domSource = new DOMSource(doc);
	       StringWriter writer = new StringWriter();
	       StreamResult result = new StreamResult(writer);
	       TransformerFactory tf = TransformerFactory.newInstance();
	       Transformer transformer = tf.newTransformer();
	       transformer.transform(domSource, result);
	       return writer.toString();
	    }
	    catch(TransformerException ex)
	    {
	       ex.printStackTrace();
	       return null;
	    }
	} 
}
