package com.pengtoolbox.pageanalyzer.servlets;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pengtoolbox.cfw._main.CFWContextRequest;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.response.HTMLResponse;
import com.pengtoolbox.cfw.response.bootstrap.AlertMessage;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class CompareServlet extends HttpServlet
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = CFWLog.getLogger(CompareServlet.class.getName());

	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		CFWLog log = new CFWLog(logger).method("doPost");
		log.info(request.getRequestURL().toString());
			
		HTMLResponse html = new HTMLResponse("Compare Results");
		StringBuffer content = html.getContent();

		content.append("<div id=\"results\"></div>");
		
		//Comma separated IDs
		String resultIDs = request.getParameter("resultids");
		
		//---------------------------------
		// Create array with json results
		
		if(!resultIDs.matches("(\\d,?)+")) {
			CFWContextRequest.addAlertMessage(AlertMessage.MessageType.ERROR, "Result IDs '"+resultIDs+"' is not a string of comma separated numbers.");
		}

		StringBuffer javascript = html.getJavascript();
		
		javascript.append("<script defer>");
			javascript.append("initialize();");
			javascript.append("draw({data: 'compareyslow', info: 'compare', view: 'yslow'})");
		javascript.append("</script>");

    }
	

}