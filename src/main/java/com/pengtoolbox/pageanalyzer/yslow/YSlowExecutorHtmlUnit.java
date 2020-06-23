package com.pengtoolbox.pageanalyzer.yslow;

import javafx.stage.Stage;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class YSlowExecutorHtmlUnit {
	
	private static YSlowExecutorHtmlUnit INSTANCE;
	
	//private static Logger logger = Logger.getLogger(YSlowExecutor.class.getName());
    
	/***********************************************************************
	 * 
	 ***********************************************************************/
	public static YSlowExecutorHtmlUnit instance() {
		if(INSTANCE == null) {
			INSTANCE = new YSlowExecutorHtmlUnit();
		}
		
		return INSTANCE;
	}

	
	/***********************************************************************
	 * 
	 ***********************************************************************/
	public void start(Stage stage){

				
	}
	
	/***********************************************************************
	 * 
	 ***********************************************************************/
	public static void analyzeHARString(ExecutionContext context, String harString){
		
//		URL url;
//		WebClient client = null;
//		try {
//			url = new URL("http://localhost");
//
//			String yslowJS = CFWFiles.getFileContent(null, "./resources/js/custom_yslow.js");
//			//System.out.println(("<html><head></head><body><script>var HAR_STRING_INPUT = "+harString+";</script>Hello World</body></html>").substring(0, 500));
//			//StringWebResponse response = new StringWebResponse("<html><head></head><body><script>"+yslowJS+"</script>Hello World</body></html>", url);
//			
//			StringWebResponse response = new StringWebResponse("<html><head></head><body><script>"+yslowJS+"</script>Hello World</body></html>", url);
//		
//			client = new WebClient();
//			client.setJavaScriptErrorListener(new JavaScriptErrorListener() {
//
//				@Override
//				public void loadScriptError(HtmlPage arg0, URL arg1, Exception e) {
//					Main.javafxLogWorkaround(Level.INFO, e.getMessage(), e, "YSlowExecutor.analyzeHARString()");
//				}
//
//				@Override
//				public void malformedScriptURL(HtmlPage arg0, String arg1, MalformedURLException e) {
//					Main.javafxLogWorkaround(Level.INFO, e.getMessage(), e, "YSlowExecutor.analyzeHARString()");
//				}
//
//				public void scriptException(HtmlPage arg0, ScriptException e) {
//					Main.javafxLogWorkaround(Level.INFO, e.getMessage(), e, "YSlowExecutor.analyzeHARString()");
//				}
//				public void timeoutError(HtmlPage arg0, long arg1, long arg2) {
//					Main.javafxLogWorkaround(Level.INFO, "timeoutError", null, "YSlowExecutor.analyzeHARString()");
//				}
//				public void warn(String arg0, String arg1, int arg2, String arg3, int arg4) {
//					Main.javafxLogWorkaround(Level.INFO, arg0, null, "YSlowExecutor.analyzeHARString()");
//				}
//				
//			});
//			
//			HtmlPage page = HTMLParser.parseXHtml(response, client.getCurrentWindow());
//			
//			ScriptResult result = page.executeJavaScript("analyzeHARObject("+harString+");");
//			Object returnValue = result.getJavaScriptResult();
//			System.out.println("RETURN VALUE");
//			System.out.println(returnValue);
//			context.setResult((String)returnValue);
//			
//		}catch(Exception e){
//			Main.javafxLogWorkaround(Level.INFO, e.getMessage(), e, "YSlowExecutor.analyzeHARString()");
//			context.setResult("{\"error\": \""+e.getMessage().replace("\"", "'")+" - Check if your HAR file is a valid JSON file. \"}");
//		}finally {
//			if(client != null) {
//				client.close();
//			}
//		}		
	}

}
