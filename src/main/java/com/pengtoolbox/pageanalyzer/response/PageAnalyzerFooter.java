package com.pengtoolbox.pageanalyzer.response;

import com.pengtoolbox.cfw.response.bootstrap.BTFooter;
import com.pengtoolbox.cfw.response.bootstrap.BTLink;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class PageAnalyzerFooter extends BTFooter {

	public PageAnalyzerFooter() {
		
		this.addChild(
				new BTLink("Support Info", "#")
				.addAttribute("data-toggle", "modal")
				.addAttribute("data-target", "#supportInfo")
			)
			.addChild(new BTLink("Custom", "./custom"))
			;
		
		
	}

}
