package com.xresch.pageanalyzer.yslow;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class YSlowExecutorThread extends Thread{
	
	@Override
	public void run() {
		YSlowExecutorJavaFX.launch(YSlowExecutorJavaFX.class);
	}
}
