package com.pengtoolbox.pageanalyzer.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pengtoolbox.cfw.datahandling.CFWField;
import com.pengtoolbox.cfw.datahandling.CFWField.FormFieldType;
import com.pengtoolbox.cfw.datahandling.CFWObject;
import com.pengtoolbox.cfw.db.CFWDB;
import com.pengtoolbox.cfw.features.api.APIDefinition;
import com.pengtoolbox.cfw.features.api.APIDefinitionFetch;
import com.pengtoolbox.cfw.features.api.APIRequestHandler;
import com.pengtoolbox.cfw.features.usermgmt.User;
import com.pengtoolbox.cfw.features.usermgmt.User.UserFields;
import com.pengtoolbox.cfw.response.PlaintextResponse;
import com.pengtoolbox.cfw.validation.LengthValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class Result extends CFWObject {
	
	public static final String TABLE_NAME = "PA_RESULT";
	
	public enum ResultFields{
		PK_ID,
		FK_ID_USER,
		NAME,
		USERNAME,
		PAGE_URL,
		JSON_RESULT,
		JSON_HAR_FILE,
		TIME_CREATED,
	}
	
	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, ResultFields.PK_ID.toString())
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the result.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(-999);
	
	private CFWField<Integer> foreignKeyUser = CFWField.newInteger(FormFieldType.HIDDEN, ResultFields.FK_ID_USER)
			.setForeignKeyCascade(this, User.class, UserFields.PK_ID)
			.setDescription("The id of the user who created the result.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(-999);
	
	private CFWField<String> name = CFWField.newString(FormFieldType.TEXT, ResultFields.NAME.toString())
			.setColumnDefinition("VARCHAR(255)")
			.setDescription("The name of the result.")
			.addValidator(new LengthValidator(-1, 255));
	
	private CFWField<String> username = CFWField.newString(FormFieldType.NONE, ResultFields.USERNAME.toString())
			.setColumnDefinition("VARCHAR(255)")
			.setDescription("The name of the user.")
			.addValidator(new LengthValidator(-1, 255));
	
	private CFWField<String> pageURL = CFWField.newString(FormFieldType.NONE, ResultFields.PAGE_URL.toString())
			.setDescription("The url that was analyzed.")
			.disableSecurity();
	
	private CFWField<String> result = CFWField.newString(FormFieldType.NONE, ResultFields.JSON_RESULT.toString())
			.setColumnDefinition("CLOB")
			.setDescription("The the page analyzer results.")
			.disableSecurity();
	
	private CFWField<String> harfile = CFWField.newString(FormFieldType.NONE, ResultFields.JSON_HAR_FILE.toString())
			.setColumnDefinition("CLOB")
			.setDescription("The raw har file.")
			.disableSecurity();
	
	private CFWField<Timestamp> timeCreated = CFWField.newTimestamp(FormFieldType.NONE, ResultFields.TIME_CREATED.toString())
			.setDescription("The results of the yslow analysis.")
			.setValue(new Timestamp(new Date().getTime()));
	
	public Result() {
		initializeFields();
	}
	
	public Result(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);	
	}
	
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, foreignKeyUser, name, username, pageURL, result, harfile, timeCreated);
	}
	
	
	/**************************************************************************************
	 * Migrate existing table structure to new CFWObject structure.
	 **************************************************************************************/
	public void migrateTable() {
		//---------------------------
		// Rename Columns
		
		String renameResultID = "ALTER TABLE IF EXISTS results ALTER COLUMN result_id RENAME TO "+ResultFields.PK_ID;
		CFWDB.preparedExecute(renameResultID);
		
		String renameUserID = "ALTER TABLE IF EXISTS results ALTER COLUMN user_id RENAME TO "+ResultFields.USERNAME;
		CFWDB.preparedExecute(renameUserID);
		
		String renamePageURL = "ALTER TABLE IF EXISTS results ALTER COLUMN page_url RENAME TO "+ResultFields.PAGE_URL;
		CFWDB.preparedExecute(renamePageURL);
		
		String renameJSONResult = "ALTER TABLE IF EXISTS results ALTER COLUMN json_result RENAME TO "+ResultFields.JSON_RESULT;
		CFWDB.preparedExecute(renameJSONResult);
		
		String renameHarFile = "ALTER TABLE IF EXISTS results ALTER COLUMN har_file RENAME TO "+ResultFields.JSON_HAR_FILE;
		CFWDB.preparedExecute(renameHarFile);
		
		String renameTime = "ALTER TABLE IF EXISTS results ALTER COLUMN time RENAME TO "+ResultFields.TIME_CREATED;
		CFWDB.preparedExecute(renameTime);
		
		//---------------------------
		// Rename Table
		String renameTable = "ALTER TABLE IF EXISTS results RENAME TO "+new Result().getTableName();
		CFWDB.preparedExecute(renameTable);
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
		
		
		String[] inputFields = 
				new String[] {
						ResultFields.PK_ID.toString(), 
						ResultFields.NAME.toString(),
						ResultFields.FK_ID_USER.toString(),
						ResultFields.PAGE_URL.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						ResultFields.PK_ID.toString(),
						ResultFields.FK_ID_USER.toString(),
						ResultFields.NAME.toString(),
						ResultFields.USERNAME.toString(),
						ResultFields.PAGE_URL.toString(),
						ResultFields.TIME_CREATED.toString(),
				};

		//----------------------------------
		// fetchData
		APIDefinitionFetch fetchDataAPI = 
				new APIDefinitionFetch(
						this.getClass(),
						this.getClass().getSimpleName(),
						"fetchData",
						inputFields,
						outputFields
				);
		
		apis.add(fetchDataAPI);
		
		//----------------------------------
		// getHar
		APIDefinition getHar = 
				new APIDefinition(
						this.getClass(),
						this.getClass().getSimpleName(),
						"getHar",
						new String[] {ResultFields.PK_ID.toString()},
						new String[] {ResultFields.JSON_HAR_FILE.toString()}
				);
		
		getHar.setDescription("Returns the HAR as json for the specified result ID.");
		
		getHar.setRequestHandler(new APIRequestHandler() {
			
			@Override
			public void handleRequest(HttpServletRequest request, HttpServletResponse response, APIDefinition definition) {
				// TODO Auto-generated method stub
				PlaintextResponse plaintext = new PlaintextResponse();
				
				String id = request.getParameter("PK_ID");
				if(id != null && !id.isEmpty()) {
					plaintext.getContent().append(PADBResults.getHARFileByID(Integer.parseInt(id)) );
				}
				
			}
		});
			
		apis.add(getHar);
		
		//----------------------------------
		// getResult
		APIDefinition getResult = 
				new APIDefinition(
						this.getClass(),
						this.getClass().getSimpleName(),
						"getResult",
						new String[] {ResultFields.PK_ID.toString()},
						new String[] {ResultFields.JSON_RESULT.toString()}
				);
		
		getResult.setDescription("Returns the results as json for the specified result ID.");
		
		getResult.setRequestHandler(new APIRequestHandler() {
			
			@Override
			public void handleRequest(HttpServletRequest request, HttpServletResponse response, APIDefinition definition) {
				// TODO Auto-generated method stub
				PlaintextResponse plaintext = new PlaintextResponse();
				
				String id = request.getParameter("PK_ID");
				plaintext.getContent().append(PADBResults.getResultByID(Integer.parseInt(id)) );
				
			}
		});
			
		apis.add(getResult);
		return apis;
	}

	public int id() {
		return id.getValue();
	}
	
	public Result id(int id) {
		this.id.setValue(id);
		return this;
	}
	
	public int foreignKeyUser() {
		return foreignKeyUser.getValue();
	}
	
	public Result foreignKeyUser(int foreignKeyUser) {
		this.foreignKeyUser.setValue(foreignKeyUser);
		return this;
	}
	
	public String name() {
		return name.getValue();
	}
	
	public Result name(String name) {
		this.name.setValue(name);
		return this;
	}
	
	public String username() {
		return username.getValue();
	}
	
	public Result username(String username) {
		this.username.setValue(username);
		return this;
	}
	
	public String pageURL() {
		return pageURL.getValue();
	}
	
	public Result pageURL(String pageURL) {
		this.pageURL.setValue(pageURL);
		return this;
	}
	
	public String result() {
		return result.getValue();
	}
	
	public Result result(String result) {
		this.result.setValue(result);
		return this;
	}
	
	public String harfile() {
		return harfile.getValue();
	}
	
	public Result harfile(String harfile) {
		this.harfile.setValue(harfile);
		return this;
	}
	
	public Timestamp timeCreated() {
		return timeCreated.getValue();
	}
	
	public Result timeCreated(Timestamp timeCreated) {
		this.timeCreated.setValue(timeCreated);
		return this;
	}
	

}
