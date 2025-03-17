package com.xresch.pageanalyzer.response;

import com.xresch.cfw.response.bootstrap.CFWHTMLItemFooter;
import com.xresch.cfw.response.bootstrap.CFWHTMLItemLink;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class PageAnalyzerFooter extends CFWHTMLItemFooter {

	public PageAnalyzerFooter() {
		
		this.addChild(
				new CFWHTMLItemLink("Support Info", "#")
					.onclick("cfw_ui_showSupportInfoModal()")
			)
			.addChild(new CFWHTMLItemLink("Custom", "./custom"))
			;
		
		
	}

}
