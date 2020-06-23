package com.pengtoolbox.pageanalyzer.yslow;

import javafx.scene.web.WebView;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class ExecutionContext {
	
	private boolean isResultUpdated = false;
	private String result = null;
	private WebView view;
	
	public boolean isResultUpdated() {
		return isResultUpdated;
	}
	
	public void setResultUpdated(boolean isResultUpdated) {
		this.isResultUpdated = isResultUpdated;
	}
	
	public String getResult() {
		return result;
	}
	
	public void setResult(String result) {
		this.result = result;
		this.isResultUpdated = true;
	}
	
	public WebView getView() {
		return view;
	}
	
	public void reset() {
		result = null;
		isResultUpdated = false;
	}
	
	public void setView(WebView view) {
		this.view = view;
	}

	
}
