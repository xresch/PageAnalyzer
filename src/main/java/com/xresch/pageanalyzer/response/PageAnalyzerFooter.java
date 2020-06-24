package com.xresch.pageanalyzer.response;

import com.xresch.cfw.response.bootstrap.BTFooter;
import com.xresch.cfw.response.bootstrap.BTLink;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class PageAnalyzerFooter extends BTFooter {

	public PageAnalyzerFooter() {
		
		this.addChild(
				new BTLink("Support Info", "#")
					.onclick("cfw_showSupportInfoModal()")
			)
			.addChild(new BTLink("Custom", "./custom"))
			;
		
		
	}

}
