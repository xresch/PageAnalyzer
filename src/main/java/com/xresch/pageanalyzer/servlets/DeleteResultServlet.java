package com.xresch.pageanalyzer.servlets;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.pageanalyzer.db.PADBResults;
import com.xresch.pageanalyzer.db.PAPermissions;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
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
						
		JSONResponse jsonResponse = new JSONResponse();
		StringBuilder content = jsonResponse.getContent();
		
		if(!CFW.Context.Request.hasPermission(PAPermissions.DELETE_RESULT)
		&& !CFW.Context.Request.hasPermission(PAPermissions.MANAGE_RESULTS)) {
			jsonResponse.setSuccess(false);
			CFW.Messages.addErrorMessage("You don't have the required permission to delete results.");
			CFW.HTTP.redirectToReferer(request, response);
			return;
		}
		
		String resultIDs = request.getParameter("resultids");
		
		if(resultIDs.matches("(\\d,?)+")) {
			boolean result = PADBResults.deleteResults(resultIDs);
			content.append("{\"result\": "+result+"}");
		}else {
			content.append("{\"result\": false, \"error\": \"The result could not be deleted: ResultID is not a number.\"}");
			new CFWLog(logger).severe("The result could not be deleted: ResultID is not a number.");
		}
		
		CFW.HTTP.redirectToReferer(request, response);
    }
	
}