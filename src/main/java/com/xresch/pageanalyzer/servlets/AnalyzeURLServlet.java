package com.xresch.pageanalyzer.servlets;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWContextRequest;
import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.utils.files.CFWFiles;
import com.xresch.pageanalyzer.db.PADBResults;
import com.xresch.pageanalyzer.db.PAPermissions;
import com.xresch.pageanalyzer.phantomjs.PhantomJSInterface;
import com.xresch.pageanalyzer.yslow.YSlow;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class AnalyzeURLServlet extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	private static Logger logger = CFWLog.getLogger(AnalyzeURLServlet.class.getName());

	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
			
		HTMLResponse html = new HTMLResponse("Analyze URL");
		StringBuilder content = html.getContent();
		
		if(CFW.Context.Request.hasPermission(PAPermissions.ANALYZE_URL)) {
			content.append(CFWFiles.getFileContent(request, "./resources/html/analyzeurl.html"));
			
	        response.setContentType("text/html");
	        response.setStatus(HttpServletResponse.SC_OK);
		}
        
    }
	
	/*****************************************************************
	 *
	 *****************************************************************/
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
				
		//--------------------------
		// Create Content
		HTMLResponse html = new HTMLResponse("Analyze URL");
		StringBuilder content = html.getContent();
		
		if(CFW.Context.Request.hasPermission(PAPermissions.ANALYZE_URL)) {
			content.append(CFWFiles.getFileContent(request, "./resources/html/analyzeurl.html"));
			
			content.append("<h1>Results</h1>");
			content.append("<p>Use the links in the menu to change the view. </p>");
			
			//--------------------------------------
			// Get Save Results Checkbox
			String resultName = request.getParameter("resultName");
			String saveResults = request.getParameter("saveResults");
			
			//--------------------------------------
			// Get URL
			String analyzeURL = request.getParameter("analyzeurl");
			
			if(analyzeURL == null){
				CFWContextRequest.addAlertMessage(MessageType.ERROR, "Please specify a URL.");
			}else {
	
				//--------------------------
				// Create HAR for URL and
				// cut out additional strings
				String harContents = PhantomJSInterface.instance().getHARStringForWebsite(request, analyzeURL);
				
				int jsonIndex = harContents.indexOf("{");
				if(jsonIndex > 0) {
					String infoString = harContents.substring(0,jsonIndex-1);
					new CFWLog(logger).warn("PhantomJS returned Information: "+ infoString);
					harContents = harContents.substring(jsonIndex);
				}
				
				//--------------------------
				// Analyze HAR
				String results = YSlow.instance().analyzeHarString(harContents);
				
				//--------------------------------------
				// Save Results to DB
				if(saveResults != null && saveResults.trim().toLowerCase().equals("on")) {
					PADBResults.saveResults(request, resultName, results, harContents);
				}
				
				//--------------------------------------
				// Prepare Response
				content.append("<div id=\"results\"></div>");
				
				StringBuilder javascript = html.getJavascript();
				javascript.append("<script defer>");
				javascript.append("		YSLOW_RESULT = "+results+";\n");
				javascript.append("		HAR_DATA = "+harContents.replaceAll("</script>", "&lt;/script>")+";\n");
				javascript.append("		initialize();");
				javascript.append("		prepareYSlowResults(YSLOW_RESULT);");
				javascript.append("		prepareGanttData(HAR_DATA);");
				javascript.append("		RULES = CFW.array.sortArrayByValueOfObject(RULES, \"score\");");
				javascript.append("		$(\".result-view-tabs\").css(\"display\", \"block\");");
				javascript.append("		draw({data: 'yslowresult', info: 'overview', view: ''})");
				javascript.append("</script>");
					
			}
			
	        response.setContentType("text/html");
	        response.setStatus(HttpServletResponse.SC_OK);
		}
	}
}