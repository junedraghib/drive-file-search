# drive-file-search

## Borneo Coding Assessment
### 1. Problem Statement
Design and build and application that can search documents from your cloud storage like google drive,
drop box based on the content inside the document.
Designed and implemented a robust solution to allow end user to directly interact with exposed search
APIs and google drive list &amp; change APIs also integrated a UI to search file on google drive based on file
content.
### 2. Tools &amp; APIs used
- Google Drive APIs: to retrieve files from Google Drive and watch for the change in files
- Adobe PDF Extract APIs: to retrieve pdf file content into text
- Elastic Search: to index the file contents into ES Indices and perform search effectively.
### 3. Processes

![FLOW Diagram!](/assests/flow.png "Flow diagram")

#### 3.1 Authentication to Google Drive APIs
Register and Retrieve Oauth2.0 client credentials to access Google Drive APIs stored access
token in local storage.
#### 3.2 Retrieve existing files from Google Drive

Used Google Drive: list API to retrieve all existing PDF files
#### 3.3 Extracting text content from PDF file
Used Adobe Extract APIs to extract text from retrieved files, adobe APIs writes to output stream
instead of returning JSON response, serialized JSON to consolidate all the text extracted from
different segments of the file.
#### 3.4 Load consolidated text extract into Elastic Search Indices
Once text is extracted and consolidated it is loaded into es index “fileindex” with es repositories
#### 3.5 Watch real-time file changes and update elastic search accordingly
Used Google Drive: change API to watch for real-time file changes in Google Drive and updated
ES Index if new file is added or existing file is removed, this process run on separate thread
continuously watching for changes this can be written in terms of batch job or cron jobs
### 4. Interaction with application
#### 4.1 UI
Implemented a minimalistic UI to search for files in google drive based on content
#### 4.2 APIs exposed
APIs are provided to interact directly with application

**GET /search/files?q=queryString**
queryString: the term you are searching files
return [{
&quot;id&quot;: &quot;14En_rrnd2fmn8tIy7LZ6DLMPUAA-LIYH&quot;,
&quot;fileName&quot;: &quot;filename&quot;,
&quot;webContentLink&quot;: &quot;webContentLink&quot;,
&quot;fileContent&quot;: &quot;file content”
}]

**GET /files: return files from drive**
return [{
&quot;id&quot;: &quot;13dPFLZJOqvyphDG2TecvDfuPK0Ns8CAW&quot;,
&quot;fileName&quot;: &quot;&quot;,
&quot;webContentLink&quot;: &quot;&quot;,
&quot;fileContent&quot;: &quot;&quot;
}]

**GET / chnagefiles**: watch for changes in file google drive and update ES Index

## How to run this project
### Run Elastic Search docker image 
`docker run --name es01 --net elastic -p 9200:9200 -p 9300:9300 -it docker.elastic.co/elasticsearch/elasticsearch:8.1.0`

### Run this spring boot application 

