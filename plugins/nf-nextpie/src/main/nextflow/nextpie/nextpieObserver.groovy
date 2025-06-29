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
    private Boolean trace_enabled
    private String trace_file
    private String workflow_name
    private String workflow_version
    private String group
    private String project
    
    @Override
    void onFlowCreate(Session session) {
        //log.info "Pipeline is starting! 🚀"
        
        // ANSI escape codes for colors
        def red = "\033[31m"
        def green = "\033[32m"
        def yellow = "\033[33m"
        def blue = "\033[34m"
        def reset = "\033[0m"  // Reset to default
        
        
        // Get CLI params
        def conf = session.getConfig()
        //println conf
        
        // get trace file
        def trace_file    = conf.trace['file']
        def trace_enabled = conf.trace['enabled']
        
        this.trace_file = trace_file
        this.trace_enabled = trace_enabled
        
        //println "[NEXTPIE] Trace enabled: " + this.trace_file
        //println "[NEXTPIE] Trace file: " + this.trace_enabled
        
        if(!trace_enabled){
           def err_msg = "Upload will be skipped because trace.enable is set to false.\n"
           err_msg    += "          Set trace.enable=true and trace.file=/path/to/Trace.txt in nextflow.config.\n"
           err_msg += "          Example:\n"
           err_msg += "          trace { enabled   = true\n"
           err_msg += "                  overwrite = true\n"
           err_msg += "                  file      = \"\${params.outDir}/pipeline_info/Trace.txt\"\n"
           err_msg += "          }"
           println "${red}[NEXTPIE]${reset} ${err_msg}"
        }
        
        
        // get workflow name and version
        this.workflow_name    = conf.params['workflow_name']
        this.workflow_version = conf.params['workflow_ver']
        this.group            = conf.params['group']
        this.project          = conf.params['name']
        
        if( this.workflow_name  !== null && this.workflow_version !== null && this.trace_enabled){
        println "${green}[NEXTPIE]${reset} Pipeline name${green} ${this.workflow_name} ${reset}and version label${green} ${this.workflow_version} ${reset}found in params scope."
        }
        
        // get workflow name and version from manifest
        def manifest = session.config.get('manifest')
        
        if (manifest && this.trace_enabled) {
        	def manifestName = conf.manifest['name']
		def manifestVer  = conf.manifest['version']
		
		if( manifestName!== "" && manifestVer !==""){
		println "${green}[NEXTPIE]${reset} Pipeline name${green} ${manifestName} ${reset}and version label${green} ${manifestVer} ${reset}found in manifest scope."
		println "${green}[NEXTPIE]${reset} ${yellow}The variables workflow_name and workflow_ver from the params scope will be ignored if they exist.${reset}"
		this.workflow_name = manifestName
		this.workflow_version = manifestVer
        	}
        
        }
        
        // Get the plugin's directory path and point to config.json
        def pluginDir = Paths.get(getClass().protectionDomain.codeSource.location.toURI()).toString()
        def configFile = Paths.get(pluginDir, "/nextflow/nextpie/config.json")
        
        //println "Plugin Dir: " + pluginDir
        //println "Config File: " + configFile
        
        // Read file content as string
        def file = new File(configFile.toString())
        
        if (file.exists()) {
            if(this.trace_enabled)
                println "${green}[NEXTPIE]${reset} Config file: " + configFile
            
            def content = new String(Files.readAllBytes(configFile))
            def json = new JsonSlurper().parseText(content)
            
            // get json data using keys
            def host    = json['host']
            def port    = json['port']
            def api_key = json['api-key']
            def workflow_name  = json['workflow-name-var']
            def workflow_ver   = json['workflow-version-var']
            
            
            // if provided via commandline or provided via nextflow.config params.workflow_name and params.workflow_ver
            if(this.workflow_name==null || this.workflow_version == null){
                def error_msg  = "${red}[NEXTPIE] Cannot extract workflow name and version from the pipeline.\n\n"
                error_msg += "OPTION 1: Add ${workflow_name} and ${workflow_ver} inside params scope in nextflow.config\n"
                error_msg += "          Example:\n\n"
                error_msg += "          params { workflow_name = 'my-workflow'\n"
                error_msg += "                   workflow_ver  = '1.0.1'}\n\n"
                
                error_msg += "OPTION 2: Add name and version inside manifest scope in nextflow.config. This is the \n"
                error_msg += "          recommended approach and takes precedence over the first option.\n"
                error_msg += "          Example:\n\n"
                error_msg += "          manifest { name     = 'my-workflow'\n"
                error_msg += "                     version  = '1.0.1'}\n"
                
                log.error(error_msg)
                
                System.exit(1)
            }
            
            //println(this.workflow_name)
            //println(this.workflow_version)
            //println("Host: $host, Port: $port, API-key: $api_key")
            
            this.host    = host
            this.port    = port.toString().toInteger()
            this.api_key = api_key
            
        } else {
            println("${green}[NEXTPIE]${reset} ⚠️ Config file not found. Using defaults.")
            this.host          = "localhost"
            this.port          = 5000
            this.api_key       = "jWCr-uqJB9fO9s1Lj2QiydXs4fFY2M"
            this.workflow_name = conf.params['workflow_name']
            this.workflow_version  = conf.params['workflow_ver']
        }
    }

    @Override
    void onFlowComplete() {
        //log.info "Pipeline complete! 👋"
        // ANSI escape codes for colors
        def red = "\033[31m"
        def green = "\033[32m"
        def yellow = "\033[33m"
        def blue = "\033[34m"
        def reset = "\033[0m"  // Reset to default
        
        def t_file = new File(this.trace_file.toString())
        
        if (!t_file.exists()){
            def error_msg  = "${red}[NEXTPIE]${reset} Trace file ${this.trace_file} does not exist.\n"
            log.error(error_msg)
        }
        if (t_file.exists() && this.trace_enabled) {
            log.info "${green}[NEXTPIE]${reset} Uploading usage data!"
            log.info "${green}[NEXTPIE]${reset} Trace file: " + this.trace_file

	    // Construct URI
	    String uri = "http://${host}:${port}/api/v1.0/upload-data"
	    log.info "${green}[NEXTPIE]${reset} URI: " + uri
	    
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
		    println "${green}[NEXTPIE]${reset} Response:\n " + response.body()
		} else {
		    println "[${red}[NEXTPIE]${reset} Unexpected status: ${response.statusCode()} message:\n${response.body()}"
		}

	    } catch (UnknownHostException e) {
		// This exception occurs if the host is unreachable or DNS resolution fails
		println "${red}[NEXTPIE]${reset} Error: Unable to reach host."
	    } catch (IOException e) {
		// Handle other I/O exceptions such as connection issues
		println "${red}[NEXTPIE]${reset} Error: Network issue or Nextpie server is not live."
	    } catch (Exception e) {
		// General error handler
		println "${red}[NEXTPIE]${reset} Error: ${e.message}"
	    }
    
            
            
        } // file trace file exists close
        
        
        
        
        
    }
}
