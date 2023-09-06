# PageAnalyzer
PageAnalyzer is a web application for analyzing website performance improvements based on yslow.
It saves the the original HAR file and the results to a database and lets you compare changes over time.

# System Requirements
Following system requirements are needed to run Page Analyzer, as it uses JavaFX to run the HAR file analysis:
- OS: Windows
- Java: JDK 8

# Setup
1. Download Binaries from the releases
2. Unzip the file to the target location.
3. (Optional) If you have multiple versions of Java on the machine, you might need to update the start.bat:

    `C:\Program Files\Java\jdk1.8.0_231\bin\java" -cp ".\lib\*;.\extensions\*" com.xresch.cfw._main._Main`

4. Use start.bat to start application.
5. Open localhost:8888
6. Login with admin/admin
7. Sanity Check:
   - If successful, you see the menu item "HAR Upload"
   - If setup is wrong, you will only see the menu points "Dashboard" and "Admin". This is most probably caused by using wrong Java or OS.

# API Endpoint
The API Endpoint allows you analyze HAR files without storing them.
The details of the API:
- **URL:** http://localhost:8888/analyzehar
- **Content Type:** multipart/form-data
- **Fieldname:** The .har-File content has to be put into the request body with the field name "harFile".

**CURL Example:** Following is a curl example of how to upload a local file:

    `curl -X POST "http://localhost:8888/analyzehar" -F "harFile=@.\localhost.har;type=application/json"`

**Return Value:** A json object containing the yslow results. Following are the definitions of the fields:
- w: size
- o: overall score
- u: url
- r: total number of requests
- s: space id of the page
- i: id of the ruleset used
- lt: page load time
- g: scores of all rules in the ruleset
- rules: Description and weight of the rules
- w_c: page weight with primed cache
- r_c: number of requests with primed cache
- stats: number of requests and weight grouped by component type
- stats_c: number of request and weight of components group by component type with primed cache
- comps: array of all the components found on the page
- grades: 100 >= A >= 90 > B >= 80 > C >= 70 > D >= 60 > E >= 50 > F >= 0 > N/A = -1
- dictionary: contains all the above fields

# Generating HAR-Files
You can use the Developer Tools of Google Chrome or similar browsers:
1. Open Developer tools with Ctrl+Shift+I
2. Go to the tab "Network"
3. Reload the page you want to have a HAR File for to record the requests
4. Right Click a request in the results and select "Save all as HAR with content"



