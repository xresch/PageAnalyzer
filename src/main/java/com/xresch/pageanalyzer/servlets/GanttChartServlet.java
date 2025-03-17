package com.xresch.pageanalyzer.servlets;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFWContextRequest;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.bootstrap.CFWHTMLItemAlertMessage;
import com.xresch.pageanalyzer.db.PADBResults;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class GanttChartServlet extends HttpServlet
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = CFWLog.getLogger(GanttChartServlet.class.getName());

	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {

		HTMLResponse html = new HTMLResponse("Gantt Chart");
		StringBuilder content = html.getContent();

		//content.append("<h1>Gantt Chart</h1>");
		//content.append("<p>Use the links in the menu to change the view. </p>");
		
		String resultID = request.getParameter("resultid");
		
		String jsonResults = null;
		if(resultID.matches("\\d+")) {
			jsonResults = PADBResults.getHARFileByID(Integer.parseInt(resultID));
		}else {
			CFWContextRequest.addAlertMessage(CFWHTMLItemAlertMessage.MessageType.ERROR, "Result ID '"+resultID+"' is not a number.");
		}
	
		
		if (jsonResults == null) {
			CFWContextRequest.addAlertMessage(CFWHTMLItemAlertMessage.MessageType.ERROR, "Results could not be loaded.");
		}else {
									
			content.append("<div id=\"results\"></div>");
			
			StringBuilder javascript = html.getJavascript();
			
			javascript.append("<script defer>");
				javascript.append("initialize();");
				javascript.append("draw({data: 'har', info: 'ganttchart', view: ''})");
			javascript.append("</script>");
							
		}
        
    }
	

}