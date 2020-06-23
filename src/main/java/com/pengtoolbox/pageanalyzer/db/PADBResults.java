package com.pengtoolbox.pageanalyzer.db;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.features.api.FeatureAPI;
import com.pengtoolbox.cfw.features.usermgmt.Permission;
import com.pengtoolbox.cfw.features.usermgmt.User;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.response.bootstrap.AlertMessage.MessageType;
import com.pengtoolbox.pageanalyzer.db.Result.ResultFields;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class PADBResults {

	public static Logger logger = CFWLog.getLogger(PADBResults.class.getName());
	
	
	/********************************************************************************************
	 *
	 ********************************************************************************************/
	public static boolean saveResults(HttpServletRequest request, String resultName, String jsonResult, String harString) {
		
		//-------------------------------
		// Get UserID
		User user = CFW.Context.Request.getUser();
		
		//-------------------------------
		// Extract URL
		Pattern pattern = Pattern.compile(".*?\"u\":\"([^\"]+)\".*");
		Matcher matcher = pattern.matcher(jsonResult);

		String pageURL = "N/A";
		if(matcher.matches()) {
			pageURL = matcher.group(1);
			
			if(pageURL == null) {
				pageURL = "N/A";
			}
			
		}

		return new Result()
			.name(resultName)
			.pageURL(pageURL)
			.result(jsonResult)
			.harfile(harString)
			.foreignKeyUser(user.id())
			.username(user.username())
			.insert();
			
	}
	
	/********************************************************************************************
	 * Returns a result as a json array.
	 * If the result is null, the method returns an empty array.
	 * 
	 ********************************************************************************************/
	public static String getResultListForUser(User user) {
					
		return new Result() 
				.selectWithout(
						ResultFields.USERNAME.toString(),
						ResultFields.JSON_RESULT.toString(), 
						ResultFields.JSON_HAR_FILE.toString())
				.where(ResultFields.FK_ID_USER.toString(), user.id())
				.orderbyDesc(ResultFields.TIME_CREATED.toString())
				.getAsJSON();
				
	}
	
	/********************************************************************************************
	 * Returns a result as a json array.
	 * If the result is null, the method returns an empty array.
	 * 
	 ********************************************************************************************/
	public static String getAllResults() {
		
		if(CFW.Context.Request.hasPermission(PAPermissions.MANAGE_RESULTS)) {
			
			return new Result() 
					.selectWithout(ResultFields.JSON_RESULT.toString(), 
								   ResultFields.JSON_HAR_FILE.toString())
					.orderbyDesc(ResultFields.TIME_CREATED.toString())
					.getAsJSON();
			
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Access Denied");
		}
		
		return null;
		
	}
	
	/********************************************************************************************
	 * Returns a result as a json array.
	 * If the result is null, the method returns an empty array.
	 * 
	 ********************************************************************************************/
	@SuppressWarnings("all")
	public static String getResultListForComparison(String resultIDArray) {
				
		//----------------------------------
		// Check input format
		if(resultIDArray == null ^ !resultIDArray.matches("(\\d,?)+")) {
			return null;
		}
		
		//----------------------------------
		// Execute
		int userID = CFW.Context.Request.getUser().id();
				
		if( CFW.Context.Request.getUserPermissions() != null
		 && CFW.Context.Request.getUserPermissions().containsKey(PAPermissions.MANAGE_RESULTS)) {
			return new Result() 
					.selectWithout(ResultFields.JSON_HAR_FILE.toString())
					.whereIn(ResultFields.PK_ID.toString(), resultIDArray.split(","))
					.orderbyDesc(ResultFields.TIME_CREATED.toString())
					.getAsJSON();
		}else {
			return new Result() 
					.selectWithout(ResultFields.JSON_HAR_FILE.toString())
					.whereIn(ResultFields.PK_ID.toString(), resultIDArray.split(","))
					.and(ResultFields.FK_ID_USER.toString(), userID)
					.orderbyDesc(ResultFields.TIME_CREATED.toString())
					.getAsJSON();
		}
	}
	
	/********************************************************************************************
	 * Returns the YSlow results as json string for the given resultID.
	 * 
	 * @param request
	 * @param resultId the ID of the result
	 ********************************************************************************************/
	public static String getResultByID(int resultID) {
		
		//----------------------------------
		// Initialize
		int userID = CFW.Context.Request.getUser().id();
		
		//----------------------------------
		// Execute
		
		if( CFW.Context.Request.getUserPermissions() != null
			&& ( CFW.Context.Request.getUserPermissions().containsKey(PAPermissions.MANAGE_RESULTS)
				|| CFW.Context.Request.getUserPermissions().containsKey(FeatureAPI.PERMISSION_CFW_API))	
			) {
			Result result = (Result)new Result() 
					.select(ResultFields.JSON_RESULT.toString())
					.where(ResultFields.PK_ID.toString(), resultID)
					.orderbyDesc(ResultFields.TIME_CREATED.toString())
					.getFirstObject();
			
			if(result != null) {
				return result.result();
			}else {
				return "";
			}
			
		}else {
			Result result = (Result)new Result() 
					.select(ResultFields.JSON_RESULT.toString())
					.where(ResultFields.PK_ID.toString(), resultID)
					.and(ResultFields.FK_ID_USER.toString(), userID)
					.orderbyDesc(ResultFields.TIME_CREATED.toString())
					.getFirstObject();
			
			if(result != null) {
				return result.result();
			}else {
				return "";
			}
		}
	}
	
	
	/********************************************************************************************
	 * Returns the YSlow results as json string for the given resultID.
	 * 
	 * @param request
	 * @param resultId the ID of the result
	 ********************************************************************************************/
	public static String getHARFileByID(int resultID) {
		
		//----------------------------------
		// Initialize
		int userID = CFW.Context.Request.getUser().id();
		
		//----------------------------------
		// Execute
		if( CFW.Context.Request.getUserPermissions() != null
			&& ( CFW.Context.Request.getUserPermissions().containsKey(PAPermissions.MANAGE_RESULTS)
				|| CFW.Context.Request.getUserPermissions().containsKey(FeatureAPI.PERMISSION_CFW_API))	
			) {
			Result result = (Result)new Result() 
					.select(ResultFields.JSON_HAR_FILE.toString())
					.where(ResultFields.PK_ID.toString(), resultID)
					.orderbyDesc(ResultFields.TIME_CREATED.toString())
					.getFirstObject();
			
			return result.harfile();
			
		}else {
			Result result = (Result)new Result() 
					.select(ResultFields.JSON_HAR_FILE.toString())
					.where(ResultFields.PK_ID.toString(), resultID)
					.and(ResultFields.FK_ID_USER.toString(), userID)
					.orderbyDesc(ResultFields.TIME_CREATED.toString())
					.getFirstObject();
			
			return result.harfile();
		}

	}
	
	/********************************************************************************************
	 *
	 ********************************************************************************************/
	@SuppressWarnings("all")
	public static boolean deleteResults(String resultIDArray) {
		
		boolean result = false;
				
		//----------------------------------
		// Check input format
		if(!resultIDArray.matches("(\\d,?)+")) {
			return false;
		}
		
		return new Result() 
			.delete()
			.whereIn(ResultFields.PK_ID.toString(), resultIDArray.split(","))
			.executeDelete();
		
	}

}
