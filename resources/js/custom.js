/*************************************************************************
 * 
 * @author Reto Scheiwiller, 2018
 * 
 * Distributed under the MIT license
 *************************************************************************/

/**************************************************************************************
 * GLOBAL VARIABLES
 *************************************************************************************/
var GLOBAL_COUNTER=0;
var SUMMARY;
var RULES;
var STATS_BY_TYPE;
var STATS_PRIMED_CACHE;

var GANTT_SUMMARY_BY_DOMAINS;
var GANTT_SUMMARY_BY_DOMAIN_SEQUENCE;
//object containing the url parameters as name/value pairs {"name": "value", "name2": "value2" ...}
var URL_PARAMETERS;

//used to store the current entry(of the HAR file) used for showing the details modal for the gantt chart
var CURRENT_DETAILS_ENTRY;

//-----------------------------------------
// Data Objects
var YSLOW_RESULT = null;
var RESULT_LIST = null;
var HAR_DATA = null;
var COMPARE_YSLOW = null;

var GRADE_CLASS = {
	A: "success",
	B: "success",
	C: "warning",
	D: "warning",
	E: "danger",
	F: "danger",
	None: "info"
};

/******************************************************************
 * Initialization function executed once when starting the page.
 * 
 * @param 
 * @return 
 ******************************************************************/
function initialize(){
	
	URL_PARAMETERS = CFW.http.getURLParams();
	
}


/******************************************************************
 * Method to fetch data from the server. The result is stored in
 * global variables.
 * If the data was already loaded, no additional request is made.
 * After the data is returned, the draw method is executed with 
 * the args-object passed to this function.
 * 
 * @param args the object containing the arguments
 * @return 
 *
 ******************************************************************/
function fetchData(args){
	
	//---------------------------------------
	// Check loading status and create URL
	//---------------------------------------
	var url = "./data";
	var params = {type: args.data};
	switch (args.data){
		case "yslowresult": 	if(YSLOW_RESULT != null) return;
								params.resultid = URL_PARAMETERS.resultid;
								break;
								
		case "resultlist":		if(RESULT_LIST != null) return;
								break;
		
		case "allresults":		if(RESULT_LIST != null) return;
								break;
								
		case "har":				if(HAR_DATA != null) return;
								params.resultid = URL_PARAMETERS.resultid;
								break;
								
		case "compareyslow":	if(COMPARE_YSLOW != null) return;
								params.resultids = URL_PARAMETERS.resultids;
								break;						
								
	}
	
	//---------------------------------------
	// Fetch and Return Data
	//---------------------------------------
	
	CFW.http.getJSON(url, params, 
			function(data) {
				    
			switch (args.data){
				case "yslowresult": 	YSLOW_RESULT = data.payload;
										prepareYSlowResults(YSLOW_RESULT);
										RULES = CFW.array.sortArrayByValueOfObject(RULES, "score");
										$(".result-view-tabs").css("display", "block");
										draw(args);
										break;
										
				case "resultlist":		RESULT_LIST = data.payload;
										draw(args);
										break;
										
				case "allresults":		RESULT_LIST = data.payload;
										draw(args);
										break;
										
				case "har":				HAR_DATA = data.payload;
										gantt_statistics_prepareGanttData(HAR_DATA);
										draw(args);
										break;
										
				case "compareyslow":	COMPARE_YSLOW = data.payload;
										draw(args);
										break;						
										
			}
		});
}

/**************************************************************************************
 * Get the Grade as A/B/C/D/E/F/None depending on the given YSlow score.
 * 
 * @param score the yslow score as number
 * @return the grade as A/B/C/D/E/F/None
 *************************************************************************************/
function getGrade(score){
	
	if		(score >= 90){return "A" }
	else if (score >= 80){return "B" }
	else if (score >= 70){return "C" }
	else if (score >= 60){return "D" }
	else if (score >= 50){return "E" }
	else if (score >= 0){return "F" }
	else {return "None" }
}




/******************************************************************
 * Prepare the fetched yslow results so they can be easily displayed.
 * This method doesn't return a value, everything is stored in 
 * global variables.
 * 
 * @param data the object containing the YSlow results. 
 * @return nothing
 ******************************************************************/
function prepareYSlowResults(data){
	
//	"w": "size",
//	"o": "overall score",
//	"u": "url",
//	"r": "total number of requests",
//	"s": "space id of the page",
//	"i": "id of the ruleset used",
//	"lt": "page load time",
//	"w_c": "page weight with primed cache",
//	"r_c": "number of requests with primed cache",
	
	//===================================================
	// Load Summary Values
	//===================================================
	SUMMARY = {};

	SUMMARY.url					= decodeURIComponent(data.u);
	SUMMARY.size				= data.w;
	SUMMARY.sizeCached			= data.w_c;
	SUMMARY.totalScore			= data.o;
	SUMMARY.grade				= getGrade(SUMMARY.totalScore);
	SUMMARY.requests			= data.r;
	SUMMARY.requestsCachable 	= data.r_c;
	SUMMARY.requestsFromCache 	= data.fromcache;
	SUMMARY.ruleset				= data.i;
	SUMMARY.loadtime			= data.resp;

	//===================================================
	// 
	//===================================================
	if (SUMMARY.requestsFromCache > 0){

		CFW.ui.addToast('This result contains '+SUMMARY.requestsFromCache+' requests that were loaded from cache. It is recommended to disable caching in the developer tools when taking HAR file snapshots.',
		null,		
		"warning");
	} 
	
	//===================================================
	// Load Rules
	//===================================================
	RULES = [];
	for(key in data.g){

		var rule = {};
		rule.name 			= key;
		rule.score 			= data.g[key].score;
		rule.grade 			= getGrade(rule.score);
		rule.title 			= data.dictionary.rules[key].name;
		rule.description 	= data.dictionary.rules[key].info;
		rule.message 		= data.g[key].message;
		rule.components 	= data.g[key].components;
		rule.url		 	= data.g[key].url;
		rule.weight 		= data.dictionary.rules[key].weight;
			
		if(rule.score == undefined || rule.score == null) rule.score = "-";
		
		RULES.push(rule);
	}
	
	//===================================================
	// LoadStats
	//===================================================
	STATS_BY_TYPE = [];
	for(key in data.stats){

		var stats = {};
		stats.type 		= key;
		stats.requests 	= data.stats[key].r;
		stats.size 		= data.stats[key].w;
		
		STATS_BY_TYPE.push(stats);
	}
	
	//===================================================
	// Load Stats with cache
	//===================================================
	STATS_PRIMED_CACHE = [];
	for(key in data.stats_c){
		
		var stats = {};
		stats.type 		= key;
		stats.requests 	= data.stats_c[key].r;
		stats.size 		= data.stats_c[key].w;
		
		STATS_PRIMED_CACHE.push(stats);
	}
		
	//===================================================
	// Load Stats with cache
	//===================================================
	COMPONENTS = [];
	for(key in data.comps){
		
		var comp = {};
		
		comp.type 			= data.comps[key].type;		
		comp.size			= data.comps[key].size;
		//comp.gzipsize		= data.comps[key].gzip;
		comp.responsetime	= Math.round(data.comps[key].resp);
		comp.type 			= data.comps[key].type;
		comp.expires		= data.comps[key].expires;
		comp.url 			= decodeURIComponent(data.comps[key].url);
		
		//what's that?
		//comp.cr 			= data.comps[key].cr;
		
		COMPONENTS.push(comp);
	}
}

/******************************************************************
 * Adds additional information to the entries needed to build the 
 * gantt chart.
 * 
 * @param data the object in HAR format
 * @return nothing
 ******************************************************************/
function gantt_statistics_prepareGanttData(data){
	

	GANTT_SUMMARY_BY_DOMAINS = {};
	GANTT_SUMMARY_BY_DOMAIN_SEQUENCE = {};
	//----------------------------------
	// Variables
	var entries = data.log.entries; 
	var entriesCount = entries.length;
	
	var dateStartTime;
	var dateEndTime;
	var totalTimeMillis;

	if(entriesCount > 0){
		dateStartTime = new Date(entries[0].startedDateTime);
	}
	//----------------------------------
	// Find lastMillis
	var lastMillis = 0;
	for(var i = 0; i < entriesCount; i++ ){
		var entry = entries[i];
		
		dateEndTime = new Date(entry.startedDateTime);
		dateEndTime = new Date(dateEndTime.valueOf() + Math.ceil(entry.time));
		entry.endDateTime = dateEndTime.valueOf();

		if(lastMillis < entry.endDateTime){
			lastMillis = entry.endDateTime;
		}
		
	}
	
	totalTimeMillis = lastMillis - dateStartTime.valueOf();
	
	//----------------------------------
	// Loop Data

//   "timings": {
//        "blocked": 0,
//        "dns": -1,
//        "connect": -1,
//        "send": 0,
//        "wait": 265,
//        "receive": 5,
//        "ssl": -1
//    },

	let lastdomain = null;
	let sequenceCounter = 0;
	
	for(var i = 0; i < entriesCount; i++ ){
		
		//------------------------------------
		// Calculate Percentages
		let entry = entries[i];
		let startDate = new Date(entry.startedDateTime);
		let deltaMillis = startDate.valueOf() - dateStartTime.valueOf();
		let duration = entry.time;
		let timings = entry.timings;
		
//		let timings = entry.timings["delta"] = deltaMillis;
		entry.ganttdata = {
			"time": duration,	
			"delta": deltaMillis,
			"percentdelta": deltaMillis / totalTimeMillis * 100,
			"percentblocked": (entry.timings.blocked > 0) 	? entry.timings.blocked / duration * 100 : 0,
			"percentdns": (entry.timings.dns > 0) 			? entry.timings.dns / duration * 100 : 0,
			"percentconnect": (entry.timings.connect > 0) 	? entry.timings.connect / duration * 100 : 0,
			"percentsend": (entry.timings.send > 0) 		? entry.timings.send / duration * 100 : 0,
			"percentwait": (entry.timings.wait > 0) 		? entry.timings.wait / duration * 100 : 0,
			"percentreceive": (entry.timings.receive > 0) 	? entry.timings.receive / duration * 100 : 0,
			"percentssl": (entry.timings.ssl > 0) 			? entry.timings.ssl / duration * 100 : 0,
			"percenttime": duration / totalTimeMillis * 100
		}
		
		// Workaround: Delta + duration could not be greater than totalTime.
		// If it is, do this to avoid display issues.
		if(totalTimeMillis < (deltaMillis + duration)){
			duration = totalTimeMillis - deltaMillis;
			entry.ganttdata.percentTime = duration / totalTimeMillis * 100;
		}
		
		//------------------------------------
		// Calculate Summary by Domain
		let domain = entry.request.url.replace(/(.*?:\/\/)/, '').split("/")[0];
		
		if(GANTT_SUMMARY_BY_DOMAINS[domain] != null){
			GANTT_SUMMARY_BY_DOMAINS[domain].request_count 	+= 1;
			GANTT_SUMMARY_BY_DOMAINS[domain].total_time		+= entry.time;
			GANTT_SUMMARY_BY_DOMAINS[domain].total_content_size += entry.response.content.size;
	
			GANTT_SUMMARY_BY_DOMAINS[domain].blocked 		+= (entry.timings.blocked != -1) ? entry.timings.blocked : 0 ;
			GANTT_SUMMARY_BY_DOMAINS[domain].dns 			+= (entry.timings.dns != -1) ? entry.timings.dns : 0 ;
			GANTT_SUMMARY_BY_DOMAINS[domain].connect 		+= (entry.timings.connect != -1) ? entry.timings.connect : 0 ;
			GANTT_SUMMARY_BY_DOMAINS[domain].send 			+= (entry.timings.send != -1) ? entry.timings.send : 0 ;
			GANTT_SUMMARY_BY_DOMAINS[domain].wait 			+= (entry.timings.wait != -1) ? entry.timings.wait : 0  ;
			GANTT_SUMMARY_BY_DOMAINS[domain].receive 		+= (entry.timings.receive != -1) ? entry.timings.receive : 0 ;
			GANTT_SUMMARY_BY_DOMAINS[domain].ssl 			+= (entry.timings.ssl != -1) ? entry.timings.ssl : 0 ;
			GANTT_SUMMARY_BY_DOMAINS[domain].wait 			+= (entry.timings.wait != -1) ? entry.timings.wait : 0  ;
			
		} else{
			GANTT_SUMMARY_BY_DOMAINS[domain] = _.cloneDeep(entry.timings);
			GANTT_SUMMARY_BY_DOMAINS[domain].domain = domain;
			GANTT_SUMMARY_BY_DOMAINS[domain].request_count = 1;
			GANTT_SUMMARY_BY_DOMAINS[domain].total_time = entry.time;
			GANTT_SUMMARY_BY_DOMAINS[domain].total_content_size = entry.response.content.size;
			
		}
		
		//------------------------------------
		// Calculate Summary by Domain Sequence
		
		if(lastdomain != domain){ sequenceCounter += 1; }
		
		let sequencedDomain = sequenceCounter + " - " + domain; 
		
		if(GANTT_SUMMARY_BY_DOMAIN_SEQUENCE[sequencedDomain] != null){
			GANTT_SUMMARY_BY_DOMAIN_SEQUENCE[sequencedDomain].request_count 	+= 1;
			GANTT_SUMMARY_BY_DOMAIN_SEQUENCE[sequencedDomain].total_time		+= entry.time;
			GANTT_SUMMARY_BY_DOMAIN_SEQUENCE[sequencedDomain].total_content_size += entry.response.content.size;
	
			GANTT_SUMMARY_BY_DOMAIN_SEQUENCE[sequencedDomain].blocked 		+= (entry.timings.blocked != -1) ? entry.timings.blocked : 0 ;
			GANTT_SUMMARY_BY_DOMAIN_SEQUENCE[sequencedDomain].dns 			+= (entry.timings.dns != -1) ? entry.timings.dns : 0 ;
			GANTT_SUMMARY_BY_DOMAIN_SEQUENCE[sequencedDomain].connect 		+= (entry.timings.connect != -1) ? entry.timings.connect : 0 ;
			GANTT_SUMMARY_BY_DOMAIN_SEQUENCE[sequencedDomain].send 			+= (entry.timings.send != -1) ? entry.timings.send : 0 ;
			GANTT_SUMMARY_BY_DOMAIN_SEQUENCE[sequencedDomain].wait 			+= (entry.timings.wait != -1) ? entry.timings.wait : 0  ;
			GANTT_SUMMARY_BY_DOMAIN_SEQUENCE[sequencedDomain].receive 		+= (entry.timings.receive != -1) ? entry.timings.receive : 0 ;
			GANTT_SUMMARY_BY_DOMAIN_SEQUENCE[sequencedDomain].ssl 			+= (entry.timings.ssl != -1) ? entry.timings.ssl : 0 ;
			GANTT_SUMMARY_BY_DOMAIN_SEQUENCE[sequencedDomain].wait 			+= (entry.timings.wait != -1) ? entry.timings.wait : 0  ;
			
		} else{
			GANTT_SUMMARY_BY_DOMAIN_SEQUENCE[sequencedDomain] = _.cloneDeep(entry.timings);
			GANTT_SUMMARY_BY_DOMAIN_SEQUENCE[sequencedDomain].domain = sequencedDomain;
			GANTT_SUMMARY_BY_DOMAIN_SEQUENCE[sequencedDomain].request_count = 1;
			GANTT_SUMMARY_BY_DOMAIN_SEQUENCE[sequencedDomain].total_time = entry.time;
			GANTT_SUMMARY_BY_DOMAIN_SEQUENCE[sequencedDomain].total_content_size = entry.response.content.size;
			
		}
		lastdomain = domain;	
	}
}

/**************************************************************************************
 * Filter the rows of a table by the value of the search field.
 * This method is best used by triggering it on the onchange-event on the search field
 * itself.
 * The search field has to have an attached JQuery data object($().data(name, value)), ¨
 * pointing to the table that should be filtered.
 * 
 * @param searchField 
 * @return nothing
 *************************************************************************************/
function filterTable(searchField){
	
	var table = $(searchField).data("table");
	var input = searchField;
	
	filter = input.value.toUpperCase();

	table.find("tbody tr, >tr").each(function( index ) {
		  
		  if ($(this).html().toUpperCase().indexOf(filter) > -1) {
			  $(this).css("display", "");
		  } else {
			  $(this).css("display", "none");
			}
	});

}

/******************************************************************
 * Print a comparison table containing all the yslow results in
 * the given data.
 * 
 * @param parent JQuery object to append the comparison. 
 * @param data array containing multiple yslow results.
 *
 ******************************************************************/
function printComparison(parent, data){
	
	compareTableData = [];
	//-----------------------------------------
	// Get distinct List of Rules
	//-----------------------------------------
	uniqueRuleList = {};
	for(key in data){
		
		for(ruleName in data[key].JSON_RESULT.g){
			uniqueRuleList[ruleName] = {"Metric": ruleName};
		}
	}
	
	//-----------------------------------------
	// Create Rows
	//-----------------------------------------
	
	var resultNameRow = {"Metric": "Result Name"}; compareTableData.push(resultNameRow);
	var resultOpenRow = {"Metric": "View"}; compareTableData.push(resultOpenRow);
	var urlRow = {"Metric": "URL"}; compareTableData.push(urlRow);
	var scoreRow = {"Metric": "Score"}; compareTableData.push(scoreRow);
	var gradeRow = {"Metric": "Grade"}; compareTableData.push(gradeRow);
	var loadtimeRow = {"Metric": "Load Time"}; compareTableData.push(loadtimeRow);
	var sizeRow = {"Metric": "Page Size"}; compareTableData.push(sizeRow);
	var sizeCachedRow = {"Metric": "Page Size Cached"}; compareTableData.push(sizeCachedRow);
	var requestCountRow = {"Metric": "Total Requests"}; compareTableData.push(requestCountRow);
	var requestsCachableRow = {"Metric": "Cachable Requests"}; compareTableData.push(requestsCachableRow);
	
	//-------------------------------
	// Push rules to table
	for(ruleName in uniqueRuleList){
		compareTableData.push(uniqueRuleList[ruleName]);
	}

	for(key in data){

		var time = CFW.format.epochToTimestamp(data[key].TIME_CREATED);
		var result = data[key].JSON_RESULT;
		
		//----------------------------
		// Name Row
		resultNameRow[time]	= '<p>'+data[key].NAME+'</p>';
		
		//----------------------------
		// Name Row
		resultOpenRow[time]	= 
			 '<a class="btn btn-sm btn-primary m-1" target="_blank" href="./resultview?resultid='+data[key].PK_ID+'">Results</a>'
			+'<a class="btn btn-sm btn-primary m-1" target="_blank" href="./ganttchart?resultid='+data[key].PK_ID+'">Gantt Chart</a>';
		
		//----------------------------
		// URL Row
		url = CFW.http.secureDecodeURI(result.u);
		urlRow[time]	= '<a class="maxvw-30 word-break-word" target="_blank" href="'+url+'">'+url+'</a>';
		
		//----------------------------
		// Score Row
		var score = result.o; 
		scoreRow[time]	 = score + "%";
		
		//----------------------------
		// Grade Row
		var grade = getGrade(score);
		gradeRow[time]	 = '<span class="badge btn-'+GRADE_CLASS[grade]+'">'+grade+'</span>';
		
		//----------------------------
		// Other Rows
		sizeRow[time]	 			= result.w + " Bytes";
		sizeCachedRow[time]	 		= result.w_c + " Bytes";
		loadtimeRow[time]	 		= result.resp + "ms";
		requestCountRow[time]	 	= result.r;
		requestsCachableRow[time]	= result.r_c;
		
		//----------------------------
		// Rule Rows
		for(ruleName in uniqueRuleList){
				if(typeof result.g !== 'undefined' && typeof result.g[ruleName] !== 'undefined'){
				var ruleScore = result.g[ruleName].score;
				var ruleGrade = getGrade(ruleScore);
				uniqueRuleList[ruleName][time] = '<span class="badge btn-'+GRADE_CLASS[ruleGrade]+'">'+ruleGrade+'&nbsp;&sol;&nbsp;' + ruleScore + "%</span>";
			}else{
				uniqueRuleList[ruleName][time] = "N/A";
			}
		}
	}
	
	printTable(parent,compareTableData, "Comparison");
	
}

/******************************************************************
 * Create the dropdown for analyzing cookies or headers.
 * 
 * @param parent jQuery object to append the created dropdown
 * @param data HAR file data
 * @param type either "Cookies" or "Headers"
 ******************************************************************/
function gantt_statistics_createAnalyzeDropdown(parent, data, type){
		
	//---------------------------------------------
	// Loop entries and get distinct cookie names
	var entries = data.log.entries; 
	var entriesCount = entries.length;
	
	var distinctNames = {};
	for(var i = 0; i < entriesCount; i++ ){
		var currentEntry = entries[i];
		
		//------------------------------------
		// Loop request Cookies
		var requestArray = currentEntry.request[type.toLowerCase()];		
		if(requestArray != null && requestArray.length > 0){
			for(j = 0; j < requestArray.length; j++){
				var name = requestArray[j].name;
				distinctNames[name] = "";
			}
		}
		
		//------------------------------------
		// Loop Response Cookies
		var responseArray = currentEntry.response[type.toLowerCase()];		
		if(responseArray != null && responseArray.length > 0){
			for(j = 0; j < responseArray.length; j++){
				var name = responseArray[j].name;
				distinctNames[name] = "";
			}
		}
		
	}	
	
	//------------------------------------
	// Create Dropdown
	dropdownID = type+"Dropdown";
	var dropdownHTML = '<div class="dropdown" style="display: inline;" >' +
		'<button class="btn btn-primary dropdown-toggle" style="margin: 5px;" type="button" id="'+dropdownID+'" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">' +
	    'Analyze '+type+' <span class="caret"></span>' +
		'</button>' +
		'<ul class="dropdown-menu" aria-labelledby="'+dropdownID+'">';
			
	if(distinctNames != null){
		nameArray = Object.keys(distinctNames);
		for(i = 0; i < nameArray.length; i++){
			dropdownHTML += '<li class="dropdown-item" onclick="analyzeCookiesOrHeaders(\''+nameArray[i]+'\', \''+type+'\')"><a >'+nameArray[i]+'</a></li>';
		}
	}

	dropdownHTML += '</ul></div>';
	
	parent.append(dropdownHTML);
	$('#'+dropdownID).dropdown();    

}

/******************************************************************
 * Open a modal with details for one item in the gantt chart list.
 * 
 * @param key the key of the cookie or header to analies
 * @param "Cookies" or "Headers"
 ******************************************************************/
function analyzeCookiesOrHeaders(key, type){
	
	//-----------------------------------------
	// Initialize
	//-----------------------------------------

	resultHTML = '<table class="table table-striped">'
		+ '<thead><tr><td>Request Cookie Value</td><td>Response Cookie Value</td><td>URL</td></tr></thead>'
	
	//---------------------------------------------
	// Loop Entries
	data = HAR_DATA;
	var entries = data.log.entries; 
	var entriesCount = entries.length;
	
	for(var i = 0; i < entriesCount; i++ ){
		var currentEntry = entries[i];
		
		//------------------------------------
		// Loop Request Cookies and find Value
		var requestArray = currentEntry.request[type.toLowerCase()];	
		var requestValue = "";
		if(requestArray != null && requestArray.length > 0){
			for(j = 0; j < requestArray.length; j++){
				if(requestArray[j].name == key){
					requestValue = requestArray[j].value;
					break;
				}
			}
		}
		
		//--------------------------------------
		// Loop Response Cookies  and find Value
		var responseArray = currentEntry.response[type.toLowerCase()];	
		var responseValue = "";
		if(responseArray != null && responseArray.length > 0){
			for(j = 0; j < responseArray.length; j++){
				if(responseArray[j].name == key){
					responseValue = responseArray[j].value;
					break;
				}
			}
		}
		
		//--------------------------------------
		// Create Table Row
		if(requestValue != "" || responseValue != ""){
			resultHTML += '	<tr><td>'+requestValue+'</td><td>'+responseValue+'</td><td>'+CFW.http.secureDecodeURI(currentEntry.request.url)+'</td></tr>';
		}
	}	
	
	resultHTML += "</table>";
	
	CFW.ui.showModalMedium('Values for '+type+' "'+key+'"',
			resultHTML);
	
	
}

/******************************************************************
 * Print the gantt chart for the entries.
 * 
 ******************************************************************/
function gantt_statistics_openGanttStatistics(){
	
	if(URL_PARAMETERS.resultid != null){
		var w = window.open(CFW.http.getHostURL()+"/app/ganttchart?resultid="+URL_PARAMETERS.resultid);	
	}else{
		CFW.ui.showModalSmall('Limited Feature', 'This currently only works when the result is saved and opened from the History page.');
	}
	
}



/******************************************************************
 * Create the gantt chart part for the given metric.
 * 
 * @param entry the HAR entry
 * @param metric 
 * @return the HTML for the bar in the gantt chart
 ******************************************************************/
function gantt_statistics_createBarPart(entry, metric){
	
	var gd = entry.ganttdata;
	

	var percentString = "percent"+metric;
	

	if(gd[percentString] > 0){ 
		if(percentString == "percentdelta"){
			//workaround for wrong timing deviations
			var shortPercentDelta = (gd.percentdelta >= 1) ? Math.floor(gd.percentdelta-1): gd.percentdelta;
			return '<div class="ganttBlock '+percentString+'" style="width: '+shortPercentDelta+'%">&nbsp;</div>';
		}else{
			// Workaround: Reduce size by 5% with "/100*95" to minimize display issues
			return '<div class="ganttBlock '+percentString+'" style="width: '+Math.floor(gd[percentString]/100*95)+'%">&nbsp;</div>';
		}
	}else{
		return "";
	}
}

/******************************************************************
 * Open a modal with details for one item in the gantt chart list.
 * 
 * @param element the DOM element which was the source of the onclick
 * event. Has an attached har-entry with JQuery $().data()
 * 
 ******************************************************************/
function gantt_statistics_showDetailsModal(element){
	
	//-----------------------------------------
	// Initialize
	//-----------------------------------------
	var entry = $(element).data('entry');

	CURRENT_DETAILS_ENTRY = entry;
	
	//-----------------------------------------
	// Print Tabs
	//-----------------------------------------
	htmlString  = '<ul id="tabs" class="nav nav-tabs">';
	htmlString += '    <li class="nav-item"><a class="nav-link" href="#" onclick="gantt_statistics_updateDetailsModal(\'request\')">Request</a></li>';
	htmlString += '    <li class="nav-item"><a class="nav-link" href="#" onclick="gantt_statistics_updateDetailsModal(\'response\')">Response</a></li>';
	htmlString += '    <li class="nav-item"><a class="nav-link" href="#" onclick="gantt_statistics_updateDetailsModal(\'cookies\')">Cookies</a></li>';
	htmlString += '    <li class="nav-item"><a class="nav-link" href="#" onclick="gantt_statistics_updateDetailsModal(\'headers\')">Headers</a></li>';
	htmlString += '    <li class="nav-item"><a class="nav-link" href="#" onclick="gantt_statistics_updateDetailsModal(\'timings\')">Timings</a></li>';
	htmlString += '</ul>';
	
	htmlString += '<div id="ganttDetails"></div>';
	
	//-----------------------------------------
	// Show Modal and Update
	//-----------------------------------------
	CFW.ui.showModalMedium('Details', htmlString);
			
	gantt_statistics_updateDetailsModal('request');

	
}

/******************************************************************
 * Update the gantt details modal when clicking on a tab.
 * 
 * @param tab specify what tab should be printed.
 * 
 ******************************************************************/
function gantt_statistics_updateDetailsModal(tab){
	
	var target = $('#ganttDetails');
	target.html('');
	
	entry = CURRENT_DETAILS_ENTRY;
	
	var details = '';
	if(tab === 'request'){
		var request = entry.request;
		details += '<table class="table table-striped table-sm">';
		details += '	<tr><td><b>Timestamp:</b></td><td>'+entry.startedDateTime+'</td></tr>';
		details += '	<tr><td><b>Duration:</b></td><td>'+Math.ceil(entry.time)+'ms</td></tr>';
		details += '	<tr><td><b>Version:</b></td><td>'+request.httpVersion+'</td></tr>';
		details += '	<tr><td><b>Method:</b></td><td>'+request.method+'</td></tr>';
		details += '	<tr><td><b>URL:</b></td><td>'+request.url+'</td></tr>';
		
		details += convertNameValueArrayToRow("QueryParameters", request.queryString);
		details += convertNameValueArrayToRow("Headers", request.headers);
		details += convertNameValueArrayToRow("Cookies", request.cookies);
		
		if( typeof request.postData != "undefined" ){
			details += '	<tr><td><b>MimeType:</b></td><td>'+request.postData.mimeType+'</td></tr>';
			details += '	<tr><td><b>Content:</b></td><td><pre><code>'+request.postData.text.replace(/</g, "&lt;")+'</code></pre></td></tr>';
		}
		details += '</table>';
		
	}else 	if(tab === 'response'){
		var response = entry.response;
		details += '<table class="table table-striped table-sm">';
		details += '	<tr><td><b>Version:</b></td><td>'+response.httpVersion+'</td></tr>';
		details += '	<tr><td><b>Status:</b></td><td>'+createHTTPStatusBadge(response.status)+' '+response.statusText+'</td></tr>';
		details += '	<tr><td><b>RedirectURL:</b></td><td>'+response.redirectURL+'</td></tr>';
		details += '	<tr><td><b>ContentType:</b></td><td>'+response.content.mimeType+'</td></tr>';
		details += '	<tr><td><b>ContentSize:</b></td><td>'+response.content.size+' Bytes</td></tr>';
		details += '	<tr><td><b>TransferSize:</b></td><td>'+response._transferSize+' Bytes</td></tr>';
		details += convertNameValueArrayToRow("Headers", response.headers);
		details += convertNameValueArrayToRow("Cookies", response.cookies);
		
		if( typeof response.content.text != "undefined" ){
			details += '	<tr><td><b>Content:</b></td><td><pre class="maxvw-75">'+response.content.text.replace(/</g, "&lt;")+'</pre></td></tr>';
		}
		details += '</table>';
	
	}
	else if(tab === 'headers'){
		details += '<table class="table table-striped table-sm">';
		details += convertNameValueArrayToRow("Request Headers", entry.request.headers);
		details += convertNameValueArrayToRow("Response Headers", entry.response.headers);
		details += '</table>';
		
	}else if(tab === 'cookies'){
		details += '<table class="table table-striped table-sm">';
		details += convertNameValueArrayToRow("Request Cookies", entry.request.cookies);
		details += convertNameValueArrayToRow("Response Cookies", entry.response.cookies);
		details += '</table>';
		
	}else if(tab === 'timings'){
				
		details += gantt_statistics_createTimingDetails(entry);
		
	}
	
	target.append(details);
	
}

/******************************************************************
 * Create a table with the timings for the provided entry.
 * 
 * @return html string
 ******************************************************************/
function gantt_statistics_createTimingDetails(entry){
	var htmlString = '';
	htmlString += '<div>';
		htmlString += '<div class="ganttBlock ganttTimings" style="width: 100%">';
		htmlString += gantt_statistics_createBarPart(entry, "blocked");
		htmlString += gantt_statistics_createBarPart(entry, "dns");
		htmlString += gantt_statistics_createBarPart(entry, "connect");
		//htmlString += gantt_statistics_createBarPart(currentEntry, "ssl");
		htmlString += gantt_statistics_createBarPart(entry, "send");
		htmlString += gantt_statistics_createBarPart(entry, "wait");
		htmlString += gantt_statistics_createBarPart(entry, "receive");
		htmlString += '</div>';
		
		htmlString += '<table class="table table-striped table-sm">';
		htmlString += '<thead><tr><th>&nbsp;</th><th>Metric</th><th>Time</th><th>Percent</th></tr></thead>'
		var metrics = ['delta','blocked','dns','connect','ssl','send','wait','receive'];

		for(i = 0; i < metrics.length; i++){

			var metric = metrics[i];
			htmlString += '<tr>';
			htmlString += '	<td><div class="gantt-legend-square percent'+metric+'">&nbsp;</div></td>';
			htmlString += '	<td><b>'+metric+':</b></td>';
			htmlString += '	<td>'+Math.round(entry.timings[metric] * 100) / 100+' ms</td>';
				
				if(metric !== "delta"){
					htmlString += '	<td>'+Math.round(entry.ganttdata["percent"+metric] * 100) / 100+'%</td>'
				}else{
					htmlString += '	<td>&nbsp;</td>'
				}
		
			htmlString += '</tr>'; 
		}
		htmlString += '</table>';
		htmlString += '<p><b>TOTAL TIME: '+Math.round(entry.time * 100) / 100+' ms</b></p>';
	htmlString += '</div>';
	return htmlString;
}

/******************************************************************
 * Create the Legend for the gantt chart colors.
 * 
 * @return html string
 ******************************************************************/
function gantt_statistics_createChartLegend(){
	
	var metrics = ['blocked','dns','connect',/*'ssl,*/'send','wait','receive'];
	
	var legend = '<div class="gantt-legend">';
	
	for(i = 0; i < metrics.length; i++){
		legend += '<div class="gantt-legend-group">';
		legend += '		<div class="gantt-legend-square percent'+metrics[i]+'">&nbsp;</div>';
		legend += '		<span>'+metrics[i]+'</span>';
		legend += '</div>';
	}
	
	legend += '</div>';
	
	return legend;
	
}

/******************************************************************
 * Print the gantt chart for the entries.
 * 
 * @param parent JQuery object 
 * @param data HAR file data
 * 
 ******************************************************************/
function gantt_statistics_printChart(parent, data){
	
	//----------------------------------
	// Add title and description.
	parent.append("<h2>Gantt Chart</h2>");

	gantt_statistics_createAnalyzeDropdown(parent, data, "Cookies");
	gantt_statistics_createAnalyzeDropdown(parent, data, "Headers");
	
	//----------------------------------
	// Create Table Header
	var cfwTable = new CFWTable({filterable: true, narrow: true});;

	cfwTable.addHeaders(['&nbsp;','Timings','Status','Duration', 'Size','URL']);

	//----------------------------------
	// Create Rows
	var entries = data.log.entries; 
	var entriesCount = entries.length;
	for(var i = 0; i < entriesCount; i++ ){
		var currentEntry = entries[i];
		
		var row = $('<tr>');
		
		//--------------------------
		// Details Link
		var detailsLinkTD = $('<td>');
		var detailsLink = $('<a alt="Show Details" onclick="gantt_statistics_showDetailsModal(this)"><i class="fa fa-search"></i></a>');
		detailsLink.data("entry", currentEntry);
		detailsLinkTD.append(detailsLink);
		
		
		row.append(detailsLinkTD);
		
		//--------------------------
		// Gantt Chart Column

		var gd = currentEntry.ganttdata;
		//workaround for wrong timing deviations
		var shortPercentDelta = (gd.percentdelta >= 1) ? Math.floor(gd.percentdelta-1): gd.percentdelta;
		var shortPercentTime = (gd.percenttime >= 2) ? Math.floor(gd.percenttime-1): gd.percenttime;
				
		var ganttWrapper = $('<div class="ganttWrapper vw-25">');
		ganttWrapper.popover({
			trigger: 'hover',
			html: true,
			placement: 'top',
			boundary: 'window',
			//title: 'Details',
			sanitize: false,
			content: gantt_statistics_createTimingDetails(currentEntry)
		})
		
		ganttWrapper.append(
			  gantt_statistics_createBarPart(currentEntry, "delta")
			+'<div class="ganttBlock ganttTimings" style="width: '+shortPercentTime+'%">'
				+ gantt_statistics_createBarPart(currentEntry, "blocked")
				+ gantt_statistics_createBarPart(currentEntry, "dns")
				+ gantt_statistics_createBarPart(currentEntry, "connect")
					//rowString += gantt_statistics_createBarPart(currentEntry, "ssl");
				+ gantt_statistics_createBarPart(currentEntry, "send")
				+ gantt_statistics_createBarPart(currentEntry, "wait")
				+ gantt_statistics_createBarPart(currentEntry, "receive")
			+'</div>'
		);
		
		var cell = $('<td>');
		cell.append(ganttWrapper);
		row.append(cell);
		
		// --------------------------
		// Other Columns
		var  rowString = '';
		rowString += '<td>'+createHTTPStatusBadge(currentEntry.response.status)+'</td>';
		rowString += '<td><span class="word-wrap-none float-right">'+Math.round(currentEntry.time)+' ms</span></td>';
		rowString += '<td><span class="word-wrap-none float-right ">'+(currentEntry.response.content.size/1000).toFixed(1)+' KB</span></td>';
		
		rowString += '<td>'+CFW.http.secureDecodeURI(currentEntry.request.url)+'</td>';
		
		row.append(rowString);
		
		
		cfwTable.addRow(row);
	}
	
	var legendHTML = gantt_statistics_createChartLegend();
	parent.append(legendHTML);
	cfwTable.appendTo(parent);
	parent.append(legendHTML);
	
}

/******************************************************************
 * Print the list of roles;
 * 
 * @param data as returned by CFW.http.getJSON()
 * @return 
 ******************************************************************/
function gantt_statistics_printSummaryByDomains(parent){
	
	//--------------------------------
	// Create Table
	
	if(GANTT_SUMMARY_BY_DOMAINS != undefined){
		
		//--------------------------------
		// prepare Array
		let tableArray = [];
		for(key in GANTT_SUMMARY_BY_DOMAINS){
			tableArray.push(GANTT_SUMMARY_BY_DOMAINS[key]);
		}
		
		//-----------------------------------
		// Render Data
		var formatMillis = function(record, value) { 
			return '<span class="word-wrap-none float-right">'+( (value == -1) ? '-</span>' : Math.round(value)+' ms</span>'); };
		var rendererSettings = {
				data: tableArray,
			 	idfield: 'HOST',
			 	bgstylefield: null,
			 	textstylefield: null,
			 	titlefields: ['NAME'],
			 	titleformat: '{0}',
			 	visiblefields: ['domain', 'request_count', 'total_content_size', 'total_time', /*'delta',*/ 'blocked','dns','connect','ssl','send','wait','receive'],
			 	labels: {
			 		//delta: "Start Offset",
			 	},
			 	customizers: {
			 		total_time: formatMillis,
			 		delta: formatMillis,
			 		blocked: formatMillis,
			 		dns: formatMillis,
			 		connect: formatMillis,
			 		ssl: formatMillis,
			 		send: formatMillis,
			 		wait: formatMillis,
			 		receive: formatMillis,
			 		total_content_size: function(record, value) { return '<span class="word-wrap-none float-right">'+(value/1000).toFixed(1)+' KB</span>'; }
			 	},
//				bulkActions: {
//					"Edit": function (elements, records, values){ alert('Edit records '+values.join(',')+'!'); },
//					"Delete": function (elements, records, values){ $(elements).remove(); },
//				},
//				bulkActionsPos: "both",

				rendererSettings: {
					table: {
						narrow: true, 
						filterable: true,
						headerclasses: ["", "", "th-right", "th-right", "th-right", "th-right", "th-right", "th-right", "th-right", "th-right", "th-right", "th-right"]
					}
				},
			};
				
		var renderResult = CFW.render.getRenderer('table').render(rendererSettings);	
		
		parent.append('<h2>Summary by Domain</h2>');
		parent.append('<p>Table with summaries by domain. Gives a good overview from which domain the most data is recieved, as well as where the most time i spent.</p>');
		parent.append(renderResult);
		
	}
}

/******************************************************************
 * Print the list of roles;
 * 
 * @param data as returned by CFW.http.getJSON()
 * @return 
 ******************************************************************/
function gantt_statistics_printSummaryByDomainSequence(parent){
	
	//--------------------------------
	// Create Table
	
	if(GANTT_SUMMARY_BY_DOMAIN_SEQUENCE != undefined){
		
		//--------------------------------
		// prepare Array
		let tableArray = [];
		for(key in GANTT_SUMMARY_BY_DOMAIN_SEQUENCE){
			tableArray.push(GANTT_SUMMARY_BY_DOMAIN_SEQUENCE[key]);
		}
		
		//-----------------------------------
		// Render Data
		var formatMillis = function(record, value) { 
			return '<span class="word-wrap-none float-right">'+( (value == -1) ? '-</span>' : Math.round(value)+' ms</span>'); };
		var rendererSettings = {
				data: tableArray,
			 	idfield: 'HOST',
			 	bgstylefield: null,
			 	textstylefield: null,
			 	titlefields: ['NAME'],
			 	titleformat: '{0}',
			 	visiblefields: ['domain', 'request_count', 'total_content_size', 'total_time', /*'delta',*/ 'blocked','dns','connect','ssl','send','wait','receive'],
			 	labels: {
			 		//delta: "Start Offset",
			 	},
			 	customizers: {
			 		total_time: formatMillis,
			 		delta: formatMillis,
			 		blocked: formatMillis,
			 		dns: formatMillis,
			 		connect: formatMillis,
			 		ssl: formatMillis,
			 		send: formatMillis,
			 		wait: formatMillis,
			 		receive: formatMillis,
			 		total_content_size: function(record, value) { return '<span class="word-wrap-none float-right">'+(value/1000).toFixed(1)+' KB</span>'; }
			 	},
//				bulkActions: {
//					"Edit": function (elements, records, values){ alert('Edit records '+values.join(',')+'!'); },
//					"Delete": function (elements, records, values){ $(elements).remove(); },
//				},
//				bulkActionsPos: "both",

				rendererSettings: {
					table: {
						narrow: true, 
						filterable: true,
						headerclasses: ["", "", "th-right", "th-right", "th-right", "th-right", "th-right", "th-right", "th-right", "th-right", "th-right", "th-right"]
					}
				},
			};
				
		var renderResult = CFW.render.getRenderer('table').render(rendererSettings);	
		
		parent.append('<h2>Summary by Domain Sequence</h2>');
		parent.append('<p>Table with summaries by domain in sequence. Only consecutive calls to the same domain will be summarized. This can be useful to analyze page requests that "switch" between multiple servers to access the page, like a single sing-on mechanism.</p>');
		parent.append(renderResult);
		
	}
}

/******************************************************************
 * Print the list of roles;
 * 
 * @param data as returned by CFW.http.getJSON()
 * @return 
 ******************************************************************/
function gantt_statistics_printTerms(parent){
	
	var terms = [
		{term:"blocked", description:" Time spent in a queue waiting for a network connection.  (-1 if the timing does not apply to the current request)"},
		{term:"dns", description:" DNS resolution time. The time required to resolve a host name.  (-1 if the timing does not apply to the current request)"},
		{term:"connect", description:" Time required to create TCP connection. (-1 if the timing does not apply to the current request)"},
		{term:"send", description:" Time required to send HTTP request to the server."},
		{term:"wait", description:" Waiting for a response from the server."},
		{term:"receive", description:" Time required to read entire response from the server (or cache)."},
		{term:"ssl", description:" Time required for SSL/TLS negotiation. If this field is defined then the time is also included in the connect field (to ensure backward compatibility with HAR 1.1). (-1 if the timing does not apply to the current request)"},
	];
	
	var rendererSettings = {
			data: terms,

		 	customizers: {
		 		term: function(record, value) { return '<strong>'+value+'</strong>'; }
		 	},

		 	rendererSettings: {
				table: {
					narrow: true, 
					filterable: true,
				}
			},
		};
	
	var renderResult = CFW.render.getRenderer('table').render(rendererSettings);	
	
	parent.append('<h2>Terms</h2>');
	parent.append('<p>Description of some terms used on this page.</p>');
	parent.append(renderResult);
}

/******************************************************************
 * Converts a array with name/value pairs to a two column table row.
 * 
 * @param title the title that will be printed in the first column.
 * @param array containing objects with name/value pairs like
 * [{name: value}, {name: value} ...] 
 * @return html string
 ******************************************************************/
function convertNameValueArrayToRow(title, array){
	result = "";
	if(array != null && array.length > 0){
		result += '	<tr><td><b>'+title+':</b></td>';
		result += '<td><table class="table table-striped">';
		for(i = 0; i < array.length; i++){
			result += '	<tr><td><b>'+array[i].name+'</b></td><td>'+array[i].value+'</td></tr>';
		}
		result += '</table></td></tr>';
	}
	
	return result;
}

/******************************************************************
 * Returns a colored badge for the given HTTP status.
 * 
 * @param status the http status as integer 
 * @return badge as html
 ******************************************************************/
function createHTTPStatusBadge(status){
	
	var style = "";
	if		(status < 200)	{style = "info"; }
	else if (status < 300)	{style = "success"; }
	else if (status < 400)	{style = "warning"; }
	else 					{style = "danger"; }
	
	return '<span class="badge btn-'+style+'">'+status+"</span>";
}

/******************************************************************
 * Print the list of results found in the database.
 * 
 * @param parent JQuery object
 * @param data object containing the list of results.
 * 
 ******************************************************************/
function printResultList(parent, data){
	
	//======================================
	// Add title and description.
	parent.append("<h2>Result History</h2>");
	parent.append("<p>Click on the eye symbol to open a result. Select multiple results and hit compare to get a comparison.</p>");

	//======================================
	// Prepare actions
	var actionButtons = [];
		
	//-------------------------
	// View Button
	actionButtons.push(
		function (record, id){ 
			return '<a class="btn btn-primary btn-sm" alt="View Result" title="View Result"'
					+' href="./resultview?resultid='+id+'"><i class="fa fa-eye"></i></a>';

		});

	//-------------------------
	// Open Gantt Chart Button	
	actionButtons.push(
		function (record, id){
			return '<a class="btn btn-primary btn-sm" alt="View Gantt Chart" title="View Gantt Chart"'
				+' href="./ganttchart?resultid='+id+'"><i class="fas fa-signal fa-rotate-90"></i></a>';
	});
	
	//-------------------------
	// Open Link Button
	actionButtons.push(
		function (record, id){
			return '<a class="btn btn-primary btn-sm" target="_blank" alt="Open URL" title="Open URL"'
			+' href="'+CFW.http.secureDecodeURI(record.PAGE_URL)+'"><i class="fa fa-link"></i></a>';

		});
	

	//-------------------------
	// Save Result Button
	actionButtons.push(
		function (record, id){
			var regex = /.*?http.?:\/\/([^\/]*)/g;
			var matches = regex.exec(CFW.http.secureDecodeURI(record.PAGE_URL));
			
			var resultName = "result";
			if(matches != null){
				resultName = matches[1];
			}
						
			return '<a class="btn btn-primary btn-sm" target="_blank" alt="Download Result" title="Download Result" '
			+' href=./data?type=yslowresult&resultid='+id+' download="'+resultName+'_yslow_results.json"><i class="fa fa-save"></i></a>';
	});
	
	
	//-------------------------
	// Download HAR Button
	actionButtons.push(
		function (record, id){
			if (CFW.hasPermission("Download HAR")){
				
				var regex = /.*?http.?:\/\/([^\/]*)/g;
				var matches = regex.exec(CFW.http.secureDecodeURI(record.PAGE_URL));
				
				var resultName = "result";
				if(matches != null){
					resultName = matches[1];
				}
				
				return '<a class="btn btn-primary btn-sm" target="_blank" alt="Dowload HAR" title="Dowload HAR" '
				+ ' href=./data?type=hardownload&resultid='+id+' download="'+resultName+'.har"><i class="fa fa-download"> HAR</i></a>';
			}else{
				return '&nbsp;';
			}

		});
	

	//-------------------------
	// Delete Button
	actionButtons.push(
		function (record, id){
			//Delete Result
			if (CFW.hasPermission("Delete Result")){
				return '<button class="btn btn-danger btn-sm" alt="Delete Result" title="Delete Result" '
				+' onclick="deleteResults('+id+')"><i class="fa fa-trash"></i></button>';
			}else{
				return '&nbsp;';
			}
	});

	//-------------------------
	// Visible fields
	let visiblefields = ['TIME_CREATED', 'NAME', 'PAGE_URL'];
	if(data[0] != null && data[0].USERNAME != undefined){
		visiblefields = ['TIME_CREATED', 'USERNAME', 'NAME', 'PAGE_URL'];
	}
	
	//-----------------------------------
	// Render Data
	var rendererSettings = {
			data: data,
		 	idfield: 'PK_ID',
		 	bgstylefield: null,
		 	textstylefield: null,
		 	titlefields: ['NAME', 'PAGE_URL'],
		 	titleformat: '{0} {1}',
		 	visiblefields: visiblefields,
		 	labels: {
		 		PAGE_URL: "URL",
		 	},
		 	customizers: {
		 		TIME_CREATED: function(record, value) { return CFW.format.epochToTimestamp(value) },
		 		PAGE_URL: function(record, value) { 
		 			url = CFW.http.secureDecodeURI(value);
					return '<div class="maxvw-30 word-break-word">'+url+'</div>';
		 			}
		 	},
			actions: actionButtons,
			
			bulkActions: {
				"Compare": function (elements, records, values){ compareResults(elements, records, values); },
				"Delete": function (elements, records, values){ deleteResults(values.join(",")); },

			},
			bulkActionsPos: "both",
			
			rendererSettings: {
				dataviewer: {
					storeid: 'pageanalyzer-results',
					renderers: [
						{	label: 'Table',
							name: 'table',
							renderdef: {
								rendererSettings: {
									table: {filterable: false},
								},
							}
						},
						{	label: 'Smaller Table',
							name: 'table',
							renderdef: {
								rendererSettings: {
									table: {filterable: false, narrow: true},
								},
							}
						},
						{	label: 'Panels',
							name: 'panels',
							renderdef: {}
						},
						{	label: 'Cards',
							name: 'cards',
							renderdef: {}
						},
						{	label: 'CSV',
							name: 'csv',
							renderdef: {}
						},
						{	label: 'JSON',
							name: 'json',
							renderdef: {}
						}
					],
				},
			},
		};
	
	var renderResult = CFW.render.getRenderer('dataviewer').render(rendererSettings);	
	
	parent.append(renderResult);
	
}
/******************************************************************
 * Print the list of results found in the database.
 * 
 * @param parent JQuery object
 * @param data object containing the list of results.
 * 
 ******************************************************************/
function printResultList_old(parent, data){
	

	//----------------------------------
	// Create Table Header
	
	var cfwTable = new CFWTable({stickyheader: false});;
	
	cfwTable.addHeaders(['&nbsp;', 'Timestamp']);
	if(data[0] != null && data[0].USER_ID != undefined){
		cfwTable.addHeader('User');
	}
	
	cfwTable.addHeaders(['Name', 'URL', '&nbsp;', '&nbsp;', '&nbsp;', '&nbsp;']);
	if (CFW.hasPermission("Download HAR")){ cfwTable.addHeaders(['&nbsp;', '&nbsp;']); }
	if (CFW.hasPermission("Delete Result")){ cfwTable.addHeaders(['&nbsp;']); }
	//----------------------------------
	// Create Rows
	var resultCount = data.length;
	
	if(resultCount == 0){
		CFW.ui.addAlert("info", "Hmm... seems you don't have any results. Try to upload a HAR file oe analyze a URL.");
	}
	for(var i = 0; i < resultCount; i++ ){
		var currentData = data[i];
		var rowString = '<tr>';
		
		rowString += '<td><input class="resultSelectionCheckbox" type="checkbox" onchange="resultSelectionChanged();" value="'+currentData.PK_ID+'" /></td>';
		rowString += '<td>'+CFW.format.epochToTimestamp(currentData.TIME_CREATED)+'</td>';
		
		if(data[0] != null && data[0].USERNAME != undefined){
			rowString += '<td>'+currentData.USERNAME+'</td>';;
		}
		
		resultName = (currentData.NAME == "null") ? "" : currentData.NAME;
		rowString += '<td>'+resultName+'</td>';
		// URL Column
		url = CFW.http.secureDecodeURI(currentData.PAGE_URL);
		rowString += '<td class="maxvw-30 word-break-word">'+url+'</td>';
		
		// View Result Icon
		rowString += '<td><a class="btn btn-primary btn-sm" alt="View Result" title="View Result" href="./resultview?resultid='+currentData.PK_ID+'"><i class="fa fa-eye"></i></a></td>';
		
		// Gantt Chart Icon
		if (CFW.hasPermission("Download HAR")){
			rowString += '<td><a class="btn btn-primary btn-sm" alt="View Gantt Chart" title="View Gantt Chart" href="./ganttchart?resultid='+currentData.PK_ID+'"><i class="fas fa-signal fa-rotate-90"></i></a></td>';
		}
		// Link Icon
		rowString += '<td><a class="btn btn-primary btn-sm" target="_blank" alt="Open URL" title="Open URL" href="'+url+'"><i class="fa fa-link"></i></a></td>';
		
		// Save Result
		var regex = /.*?http.?:\/\/([^\/]*)/g;
		var matches = regex.exec(CFW.http.secureDecodeURI(currentData.PAGE_URL));
		
		var resultName = "result";
		if(matches != null){
			resultName = matches[1];
		}
		

		rowString += '<td><a class="btn btn-primary btn-sm" target="_blank" alt="Download Result" title="Download Result" href=./data?type=yslowresult&resultid='+currentData.PK_ID+' download="'+resultName+'_yslow_results.json"><i class="fa fa-save"></i></a></td>';
		
		//Download HAR
		if (CFW.hasPermission("Download HAR")){
			rowString += '<td><a class="btn btn-primary btn-sm" target="_blank" alt="Dowload HAR" title="Dowload HAR" href=./data?type=hardownload&resultid='+currentData.PK_ID+' download="'+resultName+'.har"><i class="fa fa-download"> HAR</i></a></td>';
		}
		
		//Delete Result
		if (CFW.hasPermission("Delete Result")){
			rowString += '<td><button class="btn btn-danger btn-sm" alt="Delete Result" title="Delete Result" onclick="CFW.ui.confirmExecute(\'Do you want to delete the results?\', \'Delete\', \'deleteResults('+currentData.PK_ID+')\')"><i class="fa fa-trash"></i></button></td>';
		}
		rowString += "</tr>";
		
		cfwTable.addRow(rowString);
	}
	
	cfwTable.appendTo(parent);
	
	//----------------------------------
	// Create Button
	var selectAllButton = $('<button id="selectAllButton" class="btn btn-secondary" onclick="'+"$('.resultSelectionCheckbox').prop('checked', true);resultSelectionChanged();"+'">Select All</button>');
	var deselectAllButton = $('<button id="deselectAllButton" class="btn btn-secondary" onclick="'+"$('.resultSelectionCheckbox').prop('checked', false);resultSelectionChanged();"+'">Deselect All</button>');
	var compareButton = $('<button id="resultCompareButton" class="btn btn-primary" onclick="compareResults();" disabled="disabled">Compare</button>');
	var deleteButton = $('<button id="resultDeleteButton" class="btn btn-danger" disabled="disabled">Delete</button>');
	deleteButton.attr('onclick', "CFW.ui.confirmExecute('Do you want to delete the selected results?', 'Delete', 'deleteResults(null)')");
	parent.append(selectAllButton);
	parent.append(deselectAllButton);
	parent.append(compareButton);
	if (CFW.hasPermission("Delete Result")){
		parent.append(deleteButton);
	}
}

/**************************************************************************************
 * Enables/Disables buttons on the result list depending on how many checkboxes are
 * selected.
 * 
 *************************************************************************************/
function resultSelectionChanged(){
		
	if($(".resultSelectionCheckbox:checked").length > 1){
		$("#resultCompareButton").attr("disabled", false);
	}else{
		$("#resultCompareButton").attr("disabled", "disabled");
	}
	
	if($(".resultSelectionCheckbox:checked").length > 0){
		$("#resultDeleteButton").attr("disabled", false);
	}else{
		$("#resultDeleteButton").attr("disabled", "disabled");
	}
}

/**************************************************************************************
 * Load the comparison page for the selected results.
 *************************************************************************************/
function compareResults(elements, records, values){
		
	if(values.length < 2){
		CFW.ui.addToastInfo('Select at least two results for the comparison.');
		return;
	}
	
	self.location = "./compare?resultids="+values.join(',');
	
}

/**************************************************************************************
 * Delete the selected results.
 * 
 *************************************************************************************/
function deleteResults(resultIDs){
	
	CFW.ui.confirmExecute('Are you sure you want to delete the selected results?', 'Do it!', function(){
			
		self.location = "./delete?resultids="+resultIDs;
	});
	
}

/**************************************************************************************
 * Print the details for the rule.
 * 
 * @param parent JQuery object
 * @param rule the rule from the yslow results to print the details for.
 *************************************************************************************/
function printRuleDetails(parent, rule){
	
	if(rule.grade != null){ 			parent.append('<p><strong>Grade:&nbsp;<span class="btn btn-'+GRADE_CLASS[rule.grade]+'">'+rule.grade+'</span></strong></p>');}	
	if(rule.score != null){ 			parent.append('<p><strong>Score:&nbsp;</strong>'+rule.score+'</p>');}
	if(rule.name != null){ 				parent.append('<p><strong>Name:&nbsp;</strong>'+rule.name+'</p>');}
	if(rule.title != null){ 			parent.append('<p><strong>Title:&nbsp;</strong>'+rule.title+'</p>');}
	if(rule.description != null){ 		parent.append('<p><strong>Description:&nbsp;</strong>'+rule.description+'</p>');}
	if(rule.weight != null){ 			parent.append('<p><strong>Weight:&nbsp;</strong>'+rule.weight+'</p>');}
	
	if(rule.message != null  
	&& rule.message != undefined
	&& rule.message.length > 0 ){  		parent.append('<p><strong>Message:&nbsp;</strong>'+rule.message+'</p>');}
	
	if(rule.components.length > 0){ 			
		parent.append('<p><strong>Details:</strong></p>');
		var list = $('<ul>');
		parent.append(list);
		for(var key in rule.components){
			var compText = "";
			try{
				compText = decodeURIComponent(rule.components[key]);
			}catch(err){
				compText = rule.components[key];
			}
			list.append('<li>'+compText+'</li>');
		}
	}
	
	if(rule.url != null){ parent.append('<p><strong>Read More:&nbsp;</strong><a target="_blank" href="'+rule.url+'">'+rule.url+'</a></p>');}
}
/**************************************************************************************
 * Create the panel for the given rule.
 * 
 * @param rule the rule from the yslow results to print the details for.
 * 
 *************************************************************************************/
function createRulePanel(rule){
	
	GLOBAL_COUNTER++;
	
	var gradeClass = GRADE_CLASS[rule.grade];
	var panel = $(document.createElement("div"));
	panel.addClass("card border-"+gradeClass);
	
//	<div class="card">
//	  <div class="card-header">
//	    Featured
//	  </div>
//	  <div class="card-body">
//	    <h5 class="card-title">Special title treatment</h5>
//	    <p class="card-text">With supporting text below as a natural lead-in to additional content.</p>
//	    <a href="#" class="btn btn-primary">Go somewhere</a>
//	  </div>
//	</div>
	//----------------------------
	// Create Header
	var panelHeader = $(document.createElement("div"));
	panelHeader.addClass("card-header bg-"+gradeClass);
	panelHeader.attr("id", "panelHead"+GLOBAL_COUNTER);
	//panelHeader.attr("role", "tab");
	panelHeader.append(
		'<span class="card-title text-light">'+
		/*style.icon+*/
		'<a role="button" data-toggle="collapse" data-target="#collapse'+GLOBAL_COUNTER+'">'+
		'<strong>Grade '+rule.grade+' ('+rule.score+'%):</strong>&nbsp;'+rule.title+
		'</a></span>'
	); 
	panelHeader.append(
			'<span class="text-light" style="float: right;">(Rule: ' + rule.name+ ')</span>'
		); 
	
	panel.append(panelHeader);
	
	//----------------------------
	// Create Collapse Container
	var collapseContainer = $(document.createElement("div"));
	collapseContainer.addClass("collapse");
	collapseContainer.attr("id", "collapse"+GLOBAL_COUNTER);
	//collapseContainer.attr("role", "tabpanel");
	collapseContainer.attr("aria-labelledby", "panelHead"+GLOBAL_COUNTER);
	
	panel.append(collapseContainer);
	
	//----------------------------
	// Create Body
	var panelBody = $(document.createElement("div"));
	panelBody.addClass("card-body");
	collapseContainer.append(panelBody);
	
	printRuleDetails(panelBody, rule);
	
	return {
		panel: panel,
		panelHeader: panelHeader,
		panelBody: panelBody
	};
}


/******************************************************************
 * Format the yslow results as plain text.
 * 
 * @param parent JQuery object
 * 
 ******************************************************************/
function printPlainText(parent){
	parent.append("<h3>Plain Text</h3>");
	
	var ruleCount = RULES.length;
	for(var i = 0; i < ruleCount; i++){
		var rule = RULES[i];
		var div = $("<div>") ;
		
		div.append('<h2 class="text-'+GRADE_CLASS[rule.grade]+'"><strong>'+rule.grade+'('+rule.score+'%):</strong>&nbsp;'+rule.title+'</h2>');
		
		printRuleDetails(div, rule);
		parent.append(div);
		
	}
}

/******************************************************************
 * Format the yslow results for a JIRA ticket.
 * 
 * @param parent JQuery object
 * 
 ******************************************************************/
function printJIRAText(parent){
	parent.append("<h3>JIRA Ticket Text</h3>");
	parent.append("<p>The text for each rule can be copy &amp; pasted into a JIRA ticket description, it will be formatted accordingly.</p>");
	
	var ruleCount = RULES.length;
	for(var i = 0; i < ruleCount; i++){
		var rule = RULES[i];
		var div = $("<div>") ;
		
		div.append('<h2 class="text-'+GRADE_CLASS[rule.grade]+'"><strong>'+rule.grade+'('+rule.score+'%):</strong>&nbsp;'+rule.title+'</h2>');
		
		if(rule.title != null){ 			div.append('*Title:*&nbsp;'+rule.title+'</br>');}
		if(rule.grade != null){ 			div.append('*Grade:*&nbsp;'+rule.grade+'</br>');}	
		if(rule.score != null){ 			div.append('*Score:*&nbsp;'+rule.score+'%</br>');}
		if(rule.description != null){ 		div.append('*Description:*&nbsp;</strong>'+rule.description+'</br>');}
		
		if(rule.message != null  
	    && rule.message != undefined
	    && rule.message.length > 0 ){ 		div.append('*Message:*&nbsp;'+rule.message+'</br>');}
		
		if(rule.components.length > 0){ 			
			div.append('*Details:*</br>');
			for(var key in rule.components){
				var compText = "";
				try{
					compText = decodeURIComponent(rule.components[key]);
				}catch(err){
					compText = rule.components[key];
				}
				div.append('<li>'+compText+'</li>');
			}
		}
		
		if(rule.url != null){ div.append('*Read More:*&nbsp;</strong>'+rule.url+'</br>');}
		
		parent.append(div);
		
	}
}

/******************************************************************
 * Format the yslow results as a CSV file.
 * 
 * @param parent JQuery object
 * @param data the data to be printed.
 ******************************************************************/
function printCSV(parent, data){
	
	parent.append("<h2>CSV Export</h2>");
	parent.append("<p>Click on the text to select everything.</p>");
	
	var pre = $('<pre>');
	parent.append(pre);
	
	var code = $('<code>');
	code.attr("onclick", "CFW.selection.selectElementContent(this)");
	pre.append(code);
	
	var headerRow = "";

		
	for(var key in data[0]){
		if(key != "components" && key != "description"){
			headerRow += key+';';
		}
	}
	code.append(headerRow+"</br>");
	
	var rowCount = data.length;
	for(var i = 0; i < rowCount; i++ ){
		var currentData = data[i];
		var row = "";
		
		for(var cellKey in currentData){
			if(cellKey != "components" && cellKey != "description"){
				row += '&quot;'+currentData[cellKey]+'&quot;;';
			}
		}
		code.append(row+"</br>");
	}
	parent.append(pre);
	
}

/******************************************************************
 * Format the yslow results as a JSON file.
 * 
 * @param parent JQuery object
 * @param data the data to be printed.
 ******************************************************************/
function printJSON(parent, data){
	
	parent.append("<h2>JSON</h2>");
	parent.append("<p>Click on the text to select everything.</p>");
	
	var pre = $('<pre>');
	parent.append(pre);
	
	var code = $('<code>');
	code.attr("onclick", "CFW.selection.selectElementContent(this)");
	pre.append(code);
	
	code.text(JSON.stringify(data, 
		function(key, value) {
	    if (key == 'description') {
            // Ignore description to reduce output size
            return;
	    }
	    return value;
	},2));
	
}

/******************************************************************
 * Format the yslow results as a html table.
 * 
 * @param parent JQuery object
 * @param data the data to be printed.
 * 
 ******************************************************************/
function printTable(parent, data, title){
	
	parent.append("<h3>"+title+"</h3>");
	
	var cfwTable = new CFWTable();

	// add all keys from the first object in the array as headers
	for(var key in data[0]){
		cfwTable.addHeader(key);
	}
	
	var ruleCount = data.length;
	
	for(var i = 0; i < ruleCount; i++ ){
		var currentData = data[i];
		var row = $('<tr>');
		
		for(var cellKey in currentData){
			if(cellKey != "components"){
				row.append('<td>'+currentData[cellKey]+'</td>');
			}else{
				var list = $('<ul>');
				for(var key in currentData.components){
					var compText = "";
					try{
						compText = decodeURIComponent(currentData.components[key]);
					}catch(err){
						compText = currentData.components[key];
					}
					list.append('<li>'+compText+'</li>');
				}
				var cell = $("<td>");
				cell.append(list);
				row.append(cell);
			}
		}
		cfwTable.addRow(row);
	}
	cfwTable.appendTo(parent);
	
}

/******************************************************************
 * Format the yslow results as panels.
 * 
 * @param parent JQuery object
 * 
 ******************************************************************/
function printPanels(parent){
	
	parent.append("<h3>Panels</h3>");
	parent.append("<p>Click on the panel title to expand for more details.</p>");
	
	var ruleCount = RULES.length;
	for(var i = 0; i < ruleCount; i++){
		var panelObject = createRulePanel(RULES[i]);
		
		if(parent != null){
			parent.append(panelObject.panel);
		}else{
			$("#content").append(panelObject.panel);
		}
	}
}

/**************************************************************************************
 * Print the summary for the yslow results.
 * 
 * @param parent JQuery object
 * 
 *************************************************************************************/
function printSummary(parent){
	
	parent.append("<h3>Summary</h3>");
	
	var list = $("<ul>");
	
	if(SUMMARY.grade != null){ 				list.append('<li><strong>Grade:&nbsp;<span class="btn btn-'+GRADE_CLASS[SUMMARY.grade]+'">'+SUMMARY.grade+'</strong></li>');}
	if(SUMMARY.totalScore != null){ 		list.append('<li><strong>Total Score:&nbsp;</strong>'+SUMMARY.totalScore+'%</li>');}
	if(SUMMARY.url != null){ 				list.append('<li><strong>URL:&nbsp;</strong><a class="word-break-word" href="'+SUMMARY.url+'">'+SUMMARY.url+'</a></li>');}
	if(SUMMARY.size != null){ 				list.append('<li><strong>Page Size:&nbsp;</strong>'+SUMMARY.size+' Bytes</li>');}
	if(SUMMARY.sizeCached != null){ 		list.append('<li><strong>Page Size(cached):&nbsp;</strong>'+SUMMARY.sizeCached+' Bytes</li>');}
	if(SUMMARY.requests != null){ 			list.append('<li><strong>Request Count:&nbsp;</strong>'+SUMMARY.requests+'</li>');}
	if(SUMMARY.requestsCachable != null){ 	list.append('<li><strong>Cachable Requests Count:&nbsp;</strong>'+SUMMARY.requestsCachable+'</li>');}
	if(SUMMARY.requestsFromCache != null
	&& SUMMARY.requestsFromCache > 0){ 		list.append('<li><strong>Requests Loaded from Cache:&nbsp;</strong>'+SUMMARY.requestsFromCache+'</li>');}
	if(SUMMARY.loadtime != null 
	&& SUMMARY.loadtime != "-1"){ 			list.append('<li><strong>Load Time:&nbsp;</strong>'+SUMMARY.loadtime+' ms</li>');}
	
	if(SUMMARY.ruleset != null){ 			list.append('<li><strong>YSlow Ruleset:&nbsp;</strong>'+SUMMARY.ruleset+'</li>');}
	
	parent.append(list);
	
}
/******************************************************************
 * 
 ******************************************************************/
function reset(){
	GLOBAL_COUNTER=0;
	$("#results").html("");
}

/******************************************************************
 * Main method for building the different views.
 * 
 * @param options Array with arguments:
 * 	{
 * 		data: 'yslowresult|resultlist|har|comparyslow', 
 * 		info: 'overview|grade|stats|resultlist|ganttchart|compareyslow|', 
 * 		view: 'table|panels|plaintext|jira|csv|json', 
 * 		stats: 'type|type_cached|components'
 *  }
 * @return 
 ******************************************************************/
function draw(options){
	
	reset();
	
	CFW.ui.toggleLoader(true);
	
	window.setTimeout( 
	function(){
	
		RESULTS_DIV = $("#results");
		
		//----------------------------------
		// Fetch Data if not already done
		//----------------------------------
		switch (options.data){
			case "yslowresult": 	if(YSLOW_RESULT == null) { fetchData(options);  return;} break;
			case "resultlist":		if(RESULT_LIST == null) { fetchData(options); return;} break;
			case "allresults":		if(RESULT_LIST == null) { fetchData(options); return;} break;
			case "har":				if(HAR_DATA == null) { fetchData(options); return;} break;
			case "compareyslow":	if(COMPARE_YSLOW == null) { fetchData(options); return;} break;
			
		}
		
		//----------------------------------
		// Fetch Data if not already done
		//----------------------------------
		switch(options.info + options.view){
		
			case "resultlist":		printResultList($(RESULTS_DIV), RESULT_LIST);
									break;
									
			case "ganttchart":		gantt_statistics_printChart(RESULTS_DIV, HAR_DATA);
									gantt_statistics_printSummaryByDomains(RESULTS_DIV);
									gantt_statistics_printSummaryByDomainSequence(RESULTS_DIV);
									gantt_statistics_printTerms(RESULTS_DIV);
									break;	
			
			case "compareyslow":	printComparison(RESULTS_DIV, COMPARE_YSLOW);
									break;	
									
			case "overview": 		printSummary(RESULTS_DIV);
									printTable(RESULTS_DIV, STATS_BY_TYPE, "Statistics by Component Type(Empty Cache)");
									printTable(RESULTS_DIV, STATS_PRIMED_CACHE, "Statistics by Component Type(Primed Cache)");
									printPanels(RESULTS_DIV);
									break;
									
			case "gradepanels": 	printPanels(RESULTS_DIV);
						  			break;
						  			
			case "gradetable": 		printTable(RESULTS_DIV, RULES, "Table: Grade by Rules");
	  								break;
	  								
			case "gradeplaintext":	printPlainText(RESULTS_DIV);
									break;
									
			case "gradejira":		printJIRAText(RESULTS_DIV);
									break;
									
			case "gradecsv":		printCSV(RESULTS_DIV, RULES);
									break;
									
			case "gradejson":		printJSON(RESULTS_DIV, RULES);
									break;						
									
			case "statstable":		
				switch(options.stats){
					case "type": 			printTable(RESULTS_DIV, STATS_BY_TYPE, "Statistics by Component Type(Empty Cache)");
											break;
									
					case "type_cached": 	printTable(RESULTS_DIV, STATS_PRIMED_CACHE, "Statistics by Component Type(Primed Cache)");
											break;
											
					case "components": 		printTable(RESULTS_DIV, COMPONENTS, "Components");
											break;
				}
				break;
								
			default:				RESULTS_DIV.text("Sorry some error occured, be patient while nobody is looking into it.");
		}
		
		CFW.ui.toggleLoader(false);
	}, 100);
	
	
}