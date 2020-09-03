package com.xresch.pageanalyzer.response;

import com.xresch.cfw.response.bootstrap.BTFooter;
import com.xresch.cfw.response.bootstrap.BTLink;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
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
