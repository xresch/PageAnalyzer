package com.pengtoolbox.pageanalyzer.servlets;


import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw._main.CFWProperties;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.response.HTMLResponse;
import com.pengtoolbox.cfw.utils.CFWFiles;
import com.pengtoolbox.pageanalyzer.db.PAPermissions;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
@WebServlet("/docu")
@MultipartConfig(maxFileSize=1024*1024*100, maxRequestSize=1024*1024*100)
public class DocuServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = LogManager.getLogManager().getLogger(DocuServlet.class.getName());
    
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
   protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
   {
		CFWLog log = new CFWLog(logger).method("doGet");
		log.info(request.getRequestURL().toString());
			
		HTMLResponse html = new HTMLResponse("Documentation");
		StringBuffer content = html.getContent();
		
		if(CFW.Context.Request.hasPermission(PAPermissions.VIEW_DOCU)) {
			content.append(CFWFiles.getFileContent(request, "./resources/html/docu.html"));
			
			String supportDetails = CFWProperties.configAsString("pa_support_details", "");
			if(supportDetails != null) {
				content.append("<h1>Support Contact</h1>");
		
				content.append("<ul>");
				String[] supportDetailsArray = supportDetails.split(";");
				for(String detail : supportDetailsArray) {
					content.append("<li>"+detail+"</li>");
				}
				content.append("</ul>");
			}
			
			html.getJavascript().append("<script>CFW.ui.toc(\"#tocContent\", \"#toc\");</script>");
			
		}
       
       response.setContentType("text/html");
       response.setStatus(HttpServletResponse.SC_OK);
   }

}
