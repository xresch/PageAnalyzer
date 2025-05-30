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
public class ResultViewServlet extends HttpServlet
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = CFWLog.getLogger(ResultViewServlet.class.getName());

	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {

		HTMLResponse html = new HTMLResponse("View Result");
		StringBuilder content = html.getContent();

		content.append("<h1>Results</h1>");
		content.append("<p>Use the links in the menu to change the view. </p>");
		
		String resultID = request.getParameter("resultid");
		
		if(!resultID.matches("\\d+")) {
			CFWContextRequest.addAlertMessage(MessageType.ERROR, "Result ID '"+resultID+"' is not a number.");
		}
			
		content.append("<div id=\"results\"></div>");
		
		StringBuilder javascript = html.getJavascript();
		javascript.append("<script defer>");
			javascript.append("initialize();");
			javascript.append("draw({data: 'yslowresult', info: 'overview', view: ''})");
		javascript.append("</script>");

    }
	

}