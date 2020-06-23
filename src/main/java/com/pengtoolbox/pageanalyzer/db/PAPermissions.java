package com.pengtoolbox.pageanalyzer.db;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.features.usermgmt.Permission;
import com.pengtoolbox.cfw.features.usermgmt.Role;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class PAPermissions {

	public static String MANAGE_RESULTS 		= "Manage Results";
	public static String ANALYZE_HAR 			= "Analyze HAR";
	public static String DOWNLOAD_HAR 			= "Download HAR";
	public static String DELETE_RESULT			= "Delete Result";
	public static String ANALYZE_URL 			= "Analyze URL";
	public static String VIEW_HISTORY 			= "View History";
	public static String VIEW_DOCU				= "View Documentation";
	
	public static void initializePermissions() {
		
		Role adminRole = CFW.DB.Roles.selectFirstByName(CFW.DB.Roles.CFW_ROLE_ADMIN);
		Role userRole = CFW.DB.Roles.selectFirstByName(CFW.DB.Roles.CFW_ROLE_USER);
		
		//-----------------------------------
		// Manage Results
		if(!CFW.DB.Permissions.checkExistsByName(MANAGE_RESULTS)) {
			Permission manageResults = 
					new Permission(MANAGE_RESULTS, "user")
						.description("Manage all Page Analyzer results results in the DB.");
			
			CFW.DB.Permissions.create(manageResults);
			
			manageResults = CFW.DB.Permissions.selectByName(MANAGE_RESULTS);
			CFW.DB.RolePermissionMap.addPermissionToRole(manageResults, adminRole, true);
		}
		
		//-----------------------------------
		// Analyze HAR
		if(!CFW.DB.Permissions.checkExistsByName(ANALYZE_HAR)) {
			Permission analyzeHAR = 
					new Permission(ANALYZE_HAR, "user")
						.description("Upload and analyze HAR files.")
;
			
			CFW.DB.Permissions.create(analyzeHAR);
			analyzeHAR = CFW.DB.Permissions.selectByName(ANALYZE_HAR);
			CFW.DB.RolePermissionMap.addPermissionToRole(analyzeHAR, adminRole, true);
			CFW.DB.RolePermissionMap.addPermissionToRole(analyzeHAR, userRole, true);
		}
		//-----------------------------------
		// Download HAR
		if(!CFW.DB.Permissions.checkExistsByName(DOWNLOAD_HAR)) {
			Permission downloadHAR = 
					new Permission(DOWNLOAD_HAR, "user")
						.description("Download HAR files from the result history and analyze Gantt Charts.");
			
			CFW.DB.Permissions.create(downloadHAR);
			downloadHAR = CFW.DB.Permissions.selectByName(DOWNLOAD_HAR);
			CFW.DB.RolePermissionMap.addPermissionToRole(downloadHAR, adminRole, true);
			CFW.DB.RolePermissionMap.addPermissionToRole(downloadHAR, userRole, true);
		}
		
		//-----------------------------------
		// Analyze URL
		if(!CFW.DB.Permissions.checkExistsByName(ANALYZE_URL)) {
			Permission analyzeURL = 
					new Permission(ANALYZE_URL, "user")
						.description("Analyze a web application by using a URL.");
			
			CFW.DB.Permissions.create(analyzeURL);
			analyzeURL = CFW.DB.Permissions.selectByName(ANALYZE_URL);
			CFW.DB.RolePermissionMap.addPermissionToRole(analyzeURL, adminRole, true);
			CFW.DB.RolePermissionMap.addPermissionToRole(analyzeURL, userRole, true);
		}
		
		//-----------------------------------
		// View History
		if(!CFW.DB.Permissions.checkExistsByName(VIEW_HISTORY)) {
			Permission viewHistory = 
					new Permission(VIEW_HISTORY, "user")
						.description("View the history of the saved results.");
			
			CFW.DB.Permissions.create(viewHistory);
			viewHistory = CFW.DB.Permissions.selectByName(VIEW_HISTORY);
			CFW.DB.RolePermissionMap.addPermissionToRole(viewHistory, adminRole, true);
			CFW.DB.RolePermissionMap.addPermissionToRole(viewHistory, userRole, true);
		}
		
		//-----------------------------------
		// Delete Result
		if(!CFW.DB.Permissions.checkExistsByName(DELETE_RESULT)) {
			Permission deleteResult = 
					new Permission(DELETE_RESULT, "user")
						.description("Delete results from the result history.");
			
			CFW.DB.Permissions.create(deleteResult);
			deleteResult = CFW.DB.Permissions.selectByName(DELETE_RESULT);
			CFW.DB.RolePermissionMap.addPermissionToRole(deleteResult, adminRole, true);
			CFW.DB.RolePermissionMap.addPermissionToRole(deleteResult, userRole, true);
		}
		//-----------------------------------
		// View Documentation
		if(!CFW.DB.Permissions.checkExistsByName(VIEW_DOCU)) {
			Permission viewDocu = 
					new Permission(VIEW_DOCU, "user")
						.description("View the documentation page of the page analyzer.");
			
			CFW.DB.Permissions.create(viewDocu);
			viewDocu = CFW.DB.Permissions.selectByName(VIEW_DOCU);
			CFW.DB.RolePermissionMap.addPermissionToRole(viewDocu, adminRole, true);
			CFW.DB.RolePermissionMap.addPermissionToRole(viewDocu, userRole, true);
		}
	}
	
	
}
