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
The URL of the API is the following. Call it to get more details:
- http://localhost:8888/analyzehar

# Generating HAR-Files
You can use the Developer Tools of Google Chrome or similar browsers:
1. Open Developer tools with Ctrl+Shift+I
2. Go to the tab "Network"
3. Reload the page you want to have a HAR File for to record the requests
4. Right Click a request in the results and select "Save all as HAR with content"



