package com.xresch.pageanalyzer.servlets;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw.response.HTMLResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CustomContentServlet extends HttpServlet
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    protected void doGet( HttpServletRequest request,
                          HttpServletResponse response ) throws ServletException,
                                                        IOException
    {
		String folder = request.getParameter("f");
		String page = request.getParameter("p");
		
		HTMLResponse html = new HTMLResponse(folder);
		StringBuilder content = html.getContent();
		
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        
        File[] directories = new File("./resources/custom/").listFiles(File::isDirectory);
        
        StringBuffer dirLinks = new StringBuffer();
        for(File dir : directories){
        	dirLinks.append("<a class=\"btn btn-default\" href=\"./custom?f="+dir.getName()+"\">"+dir.getName()+"</a>");
        }
        content.append("<div>"+dirLinks+"</div>");
        if(folder != null){
        	
        	if(page == null) page = "index";
	        List<String> fileContent = Files.readAllLines(Paths.get("./resources/custom/"+folder+"/"+page+".html"), Charset.forName("UTF-8"));
	        
	        for(String line : fileContent){
	        	content.append(line);
	        	content.append("\n");
	    	}
        }
    }
}