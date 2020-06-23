package com.xresch.pageanalyzer.servlets;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWContextRequest;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage;
import com.xresch.pageanalyzer.db.PADBResults;
import com.xresch.pageanalyzer.db.PAPermissions;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class ResultListServlet extends HttpServlet
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = CFWLog.getLogger(ResultListServlet.class.getName());

	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		CFWLog log = new CFWLog(logger).method("doGet");
		log.info(request.getRequestURL().toString());
			
		HTMLResponse html = new HTMLResponse("History");
		StringBuffer content = html.getContent();

		
		if(CFW.Context.Request.hasPermission(PAPermissions.VIEW_HISTORY)) {
			String jsonResults = PADBResults.getResultListForUser(CFW.Context.Request.getUser());
			
			//TODO: Check User
			
			if (jsonResults == null || jsonResults.isEmpty()) {
				CFWContextRequest.addAlertMessage(AlertMessage.MessageType.ERROR, "Results could not be loaded.");
			}else {
										
				content.append("<div id=\"results\"></div>");
				
				StringBuffer javascript = html.getJavascript();
				javascript.append("<script defer>");
					javascript.append("initialize();");
					javascript.append("draw({data: 'resultlist', info: 'resultlist', view: ''})");
				javascript.append("</script>");
					
			}
		}
    }
}