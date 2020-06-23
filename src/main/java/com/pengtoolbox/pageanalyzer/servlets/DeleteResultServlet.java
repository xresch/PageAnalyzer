package com.pengtoolbox.pageanalyzer.servlets;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.response.JSONResponse;
import com.pengtoolbox.cfw.response.bootstrap.AlertMessage.MessageType;
import com.pengtoolbox.pageanalyzer.db.PADBResults;
import com.pengtoolbox.pageanalyzer.db.PAPermissions;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class DeleteResultServlet extends HttpServlet
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = CFWLog.getLogger(DeleteResultServlet.class.getName());

	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		
		CFWLog log = new CFWLog(logger).method("doGet");
		log.info(request.getRequestURL().toString());
				
		JSONResponse jsonResponse = new JSONResponse();
		StringBuffer content = jsonResponse.getContent();
		
		if(!CFW.Context.Request.hasPermission(PAPermissions.DELETE_RESULT)
		&& !CFW.Context.Request.hasPermission(PAPermissions.MANAGE_RESULTS)) {
			jsonResponse.setSuccess(false);
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "You don't have the required permission to delete results.");
			CFW.HTTP.redirectToReferer(request, response);
			return;
		}
		
		String resultIDs = request.getParameter("resultids");
		
		if(resultIDs.matches("(\\d,?)+")) {
			boolean result = PADBResults.deleteResults(resultIDs);
			content.append("{\"result\": "+result+"}");
		}else {
			content.append("{\"result\": false, \"error\": \"The result could not be deleted: ResultID is not a number.\"}");
			log.severe("The result could not be deleted: ResultID is not a number.");
		}
		
		CFW.HTTP.redirectToReferer(request, response);
    }
	
}