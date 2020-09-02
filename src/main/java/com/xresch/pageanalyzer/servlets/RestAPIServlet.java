package com.xresch.pageanalyzer.servlets;

import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.PlaintextResponse;
import com.xresch.cfw.utils.CFWFiles;
import com.xresch.pageanalyzer.yslow.YSlow;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class RestAPIServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = LogManager.getLogManager().getLogger(RestAPIServlet.class.getName());
       
	/*****************************************************************
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 ******************************************************************/
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
			
		HTMLResponse html = new HTMLResponse("Rest API");
		StringBuilder content = html.getContent();
		content.append(CFWFiles.getFileContent(request, "./resources/html/api.html"));
		
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
       
	}
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
					
		PlaintextResponse plain = new PlaintextResponse();
		StringBuilder content = plain.getContent();

		Part harFile = request.getPart("harFile");
		if (harFile == null) {
			content.append("{\"error\": \"HAR File could not be loaded.\"}");
		}else {

			String harContents = CFWFiles.readContentsFromInputStream(harFile.getInputStream());
			
			String results = YSlow.instance().analyzeHarString(harContents);

			content.append(results);
			
		}
	}
}
