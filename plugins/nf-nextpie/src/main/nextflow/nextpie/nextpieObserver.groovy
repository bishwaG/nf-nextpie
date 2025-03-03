/*
 * Copyright 2021, Seqera Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nextflow.nextpie

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nextflow.Session
import nextflow.trace.TraceObserver
import java.nio.file.Paths
import groovy.json.JsonSlurper
import java.nio.file.Files


// for nextpie
import java.nio.file.Files
import java.nio.file.Paths
import java.net.http.*
import java.net.URI
import java.net.http.HttpRequest.BodyPublishers

 /* Example workflow events observer
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@CompileStatic
class nextpieObserver implements TraceObserver {
	
    private String host
    private int port
    private String api_key
    private String trace_file
    private String workflow_name
    private String workflow_version
    private String group
    private String project
    
    @Override
    void onFlowCreate(Session session) {
        //log.info "Pipeline is starting! 🚀"
        
        // Get CLI params
        def conf = session.getConfig()
        println conf
        
        // get trace file
        def trace_file = conf.trace['file']
        println "Trace file: " + trace_file
        this.trace_file = trace_file
        
        // get workflow name and version
        this.workflow_name    = conf.params['workflow_name']
        this.workflow_version = conf.params['workflow_ver']
        this.group            = conf.params['group']
        this.project          = conf.params['name']
        
        // Get the plugin's directory path and point to config.json
        def pluginDir = Paths.get(getClass().protectionDomain.codeSource.location.toURI()).toString()
        def configFile = Paths.get(pluginDir, "/nextflow/hello/config.json")
        
        //println "Plugin Dir: " + pluginDir
        //println "Config File: " + configFile
        
        // Read file content as string
        def file = new File(configFile.toString())
        
        if (file.exists()) {
            println "Nextpie config file: " + configFile
            
            def content = new String(Files.readAllBytes(configFile))
            def json = new JsonSlurper().parseText(content)
            
            // get json data
            def host    = json['host']
            def port    = json['port']
            def api_key = json['api-key']
            
            
            println("Host: $host, Port: $port, API-key: $api_key")
            
            this.host    = host
            this.port    = port.toString().toInteger()
            this.api_key = api_key
            
        } else {
            println("⚠️ Nextpie config file not found. Using defaults.")
            this.host    = "localhost"
            this.port    = 5005
            this.api_key = "jWCr-uqJB9fO9s1Lj2QiydXs4fFY2M"
        }
    }

    @Override
    void onFlowComplete() {
        //log.info "Pipeline complete! 👋"
        
        def t_file = new File(this.trace_file.toString())
        if (t_file.exists()) {
            log.info "Pushing resource usage data to Nextpie!"
            log.info "Trace file: " + this.trace_file

	    // Construct URI
	    String uri = "http://${host}:${port}/api/v1.0/upload-data"
	    log.info "URI: " + uri
	    
	    // Build the HTTP client
	    def client = HttpClient.newHttpClient()

	    // Create the multipart body using HttpRequest.BodyPublishers
	    def boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW"
	    def body = new StringBuilder()
	    
	    body.append("--${boundary}\r\n")
	    body.append("Content-Disposition: form-data; name=\"Workflow\"\r\n\r\n")
	    body.append("${this.workflow_name }\r\n")
	    body.append("--${boundary}\r\n")
	    body.append("Content-Disposition: form-data; name=\"Version\"\r\n\r\n")
	    body.append("${this.workflow_version}\r\n")
	    body.append("--${boundary}\r\n")
	    body.append("Content-Disposition: form-data; name=\"Group\"\r\n\r\n")
	    body.append("${this.group}\r\n")
	    body.append("--${boundary}\r\n")
	    body.append("Content-Disposition: form-data; name=\"Project\"\r\n\r\n")
	    body.append("${this.project}\r\n")
	    body.append("--${boundary}\r\n")
	    body.append("Content-Disposition: form-data; name=\"File\"; filename=\"${t_file.name}\"\r\n")
	    body.append("Content-Type: text/plain\r\n\r\n")
	    
	    // Add file content
	    body.append(new String(Files.readAllBytes(Paths.get(t_file.absolutePath)), "UTF-8"))
	    body.append("\r\n--${boundary}--\r\n")
	    
	    // Prepare the HTTP request
	    def request = HttpRequest.newBuilder()
		    .uri(URI.create(uri))
		    .header("Content-Type", "multipart/form-data; boundary=${boundary}")
		    .header("X-API-KEY", this.api_key)  // Make sure APIkey is passed correctly
		    .POST(BodyPublishers.ofString(body.toString()))
		    .build()

	    try {
		// Send request and get response
		def response = client.send(request, HttpResponse.BodyHandlers.ofString())

		// Handle success
		if (response.statusCode() == 200) {
		    println "☑️ " + response.body()
		} else if (response.statusCode() == 401) {
		    println "UNAUTHORIZED (401)"
		} else if (response.statusCode() == 403) {
		    println "FORBIDDEN (403)"
		} else if (response.statusCode() == 404) {
		    println "NOT FOUND (404)"
		} else if (response.statusCode() == 503) {
		    println "Service Unavailable (503)"
		} else {
		    println "Unexpected status: ${response.statusCode()}"
		}

	    } catch (UnknownHostException e) {
		// This exception occurs if the host is unreachable or DNS resolution fails
		println "⚠️ Error: Unable to reach host."
	    } catch (IOException e) {
		// Handle other I/O exceptions such as connection issues
		println "⚠️ Error: Network issue or server not running."
	    } catch (Exception e) {
		// General error handler
		println "⚠️ Error: ${e.message}"
	    }
    
            
            
        } // file trace file exists close
        
        
        
        
        
    }
}
