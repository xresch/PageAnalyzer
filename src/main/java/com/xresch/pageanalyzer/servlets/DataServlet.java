package com.xresch.pageanalyzer.servlets;

import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.PlaintextResponse;
import com.xresch.pageanalyzer.db.PADBResults;
import com.xresch.pageanalyzer.db.PAPermissions;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class DataServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = LogManager.getLogManager().getLogger(DataServlet.class.getName());
       
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//-------------------------------------------
		// Initialize
		//-------------------------------------------		
		String type = request.getParameter("type");
		String resultID = request.getParameter("resultid");
		if(resultID != null) resultID = resultID.replace("#", "");

//		//-------------------------------------------
//		// Resolve User ID
//		//-------------------------------------------
//		String username = "";
//		
//		if(CFWProperties.AUTHENTICATION_ENABLED) {
//			SessionData data = CFW.Context.Request.getSessionData(); 
//			if(data.isLoggedIn()) {
//				username = data.getUser().username();
//			}
//		}else {
//			username = "anonymous";
//		}
		
		//-------------------------------------------
		// Fetch Data
		//-------------------------------------------
		JSONResponse json = new JSONResponse();
		StringBuilder content = json.getContent();

		if (type == null) {
			CFW.Messages.addErrorMessage("Type was not specified");
			//content.append("{\"error\": \"Type was not specified.\"}");
		}else {

			switch(type.toLowerCase()) {
				case "yslowresult": 	content.append(PADBResults.getResultByID(Integer.parseInt(resultID)));
										break;
										
				case "resultlist": 		content.append(PADBResults.getResultListForUser(CFW.Context.Request.getUser()));
										break;
				
				case "allresults": 		content.append(PADBResults.getAllResults());
										break;
										
				case "har": 			if(CFW.Context.Request.hasPermission(PAPermissions.DOWNLOAD_HAR)) {
											content.append(PADBResults.getHARFileByID(Integer.parseInt(resultID)));
										}else {
											CFW.Messages.addErrorMessage("You don't have the required permission to download HAR files.");
										}
										break;
				
				case "hardownload":		if(CFW.Context.Request.hasPermission(PAPermissions.DOWNLOAD_HAR)) {
											PlaintextResponse plain = new PlaintextResponse();
											plain.getContent().append(PADBResults.getHARFileByID(Integer.parseInt(resultID)));
										}else {
											CFW.Messages.addErrorMessage("You don't have the required permission to download HAR files.");
										}
				break;
				case "compareyslow": 	String resultIDs = request.getParameter("resultids");
										content.append(PADBResults.getResultListForComparison(resultIDs));
										break;
										
				default: 				CFW.Messages.addErrorMessage("The type '"+type+"' is not supported.");
										break;
										
			}
						
		}
		
		//response.setContentType("application/json");
	}
}
