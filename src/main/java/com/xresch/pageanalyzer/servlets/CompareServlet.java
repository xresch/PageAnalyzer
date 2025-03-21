package com.xresch.pageanalyzer.servlets;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFWContextRequest;
import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.HTMLResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
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
			
		HTMLResponse html = new HTMLResponse("Compare Results");
		StringBuilder content = html.getContent();

		content.append("<div id=\"results\"></div>");
		
		//Comma separated IDs
		String resultIDs = request.getParameter("resultids");
		
		//---------------------------------
		// Create array with json results
		
		if(!resultIDs.matches("(\\d,?)+")) {
			CFWContextRequest.addAlertMessage(MessageType.ERROR, "Result IDs '"+resultIDs+"' is not a string of comma separated numbers.");
		}

		StringBuilder javascript = html.getJavascript();
		
		javascript.append("<script defer>");
			javascript.append("initialize();");
			javascript.append("draw({data: 'compareyslow', info: 'compare', view: 'yslow'})");
		javascript.append("</script>");

    }
	

}