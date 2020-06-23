package com.xresch.pageanalyzer.servlets;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWContextRequest;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage;
import com.xresch.cfw.utils.CFWFiles;
import com.xresch.pageanalyzer.db.PADBResults;
import com.xresch.pageanalyzer.db.PAPermissions;
import com.xresch.pageanalyzer.yslow.YSlow;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
//@MultipartConfig(maxFileSize=1024*1024*100, maxRequestSize=1024*1024*100)
public class HARUploadServlet extends HttpServlet
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = CFWLog.getLogger(HARUploadServlet.class.getName());

	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		
		CFWLog log = new CFWLog(logger).method("doGet");
		log.info(request.getRequestURL().toString());
			
		HTMLResponse html = new HTMLResponse("Analyze");
		
		if(CFW.Context.Request.hasPermission(PAPermissions.ANALYZE_HAR)) {
			StringBuffer content = html.getContent();
			content.append(CFWFiles.getFileContent(request, "./resources/html/harupload.html"));
			
	        response.setContentType("text/html");
	        response.setStatus(HttpServletResponse.SC_OK);
		}
        
    }
	
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		CFWLog log = new CFWLog(logger).method("doPost");
		log.info(request.getRequestURL().toString());
			
		HTMLResponse html = new HTMLResponse("Analyze HAR");
		StringBuffer content = html.getContent();
		StringBuffer javascript = html.getJavascript();
		
		if(CFW.Context.Request.hasPermission(PAPermissions.ANALYZE_HAR)) {
			content.append(CFWFiles.getFileContent(request, "./resources/html/harupload.html"));
			
			content.append("<h1>Results</h1>");
			content.append("<p>Use the links in the menu to change the view. </p>");
			
			//--------------------------------------
			// Get Save Results Checkbox
			Part resultNamePart = request.getPart("resultName");
			String resultName = "";
			
			if(resultNamePart != null) {
				resultName = CFWFiles.readContentsFromInputStream(resultNamePart.getInputStream()).trim();
			}
			
			Part saveResults = request.getPart("saveResults");
			String saveResultsString = "off";
			
			if(saveResults != null) {
				saveResultsString =	CFWFiles.readContentsFromInputStream(saveResults.getInputStream());
			}
	
	
			//--------------------------------------
			// Get HAR File
			Part harFile = request.getPart("harFile");
	
			if (harFile == null) {
				CFWContextRequest.addAlertMessage(AlertMessage.MessageType.ERROR, "HAR File could not be loaded.");
			}else {
	
				log.start().method("doPost()-StreamHarFile");
					String harContents = CFWFiles.readContentsFromInputStream(harFile.getInputStream());
				log.end();
							
				String results = YSlow.instance().analyzeHarString(harContents);
				
				//--------------------------------------
				// Save Results to DB
				if(saveResultsString.trim().toLowerCase().equals("on")) {
					PADBResults.saveResults(request, resultName, results, harContents);
				}
				
				//--------------------------------------
				// Prepare Response
				content.append("<div id=\"results\"></div>");
				
				
				javascript.append("<script defer>");
				javascript.append("		YSLOW_RESULT = "+results+";\n");
				javascript.append("		initialize();");
				javascript.append("		prepareYSlowResults(YSLOW_RESULT);");
				javascript.append("		RULES = CFW.array.sortArrayByValueOfObject(RULES, \"score\");");
				javascript.append("		$(\".result-view-tabs\").css(\"display\", \"block\");");
				javascript.append("		draw({data: 'yslowresult', info: 'overview', view: ''})");
				javascript.append("</script>");
					
			}
		}
	}
}