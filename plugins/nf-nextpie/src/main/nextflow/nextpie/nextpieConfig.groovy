package nextflow.nextpie

class nextpieConfig {

    private String host
    private int port
    private String apiKey
    private String workflowNameVar
    private String workflowVersionVar

    nextpieConfig(Map map){
        host = map.containsKey("host") ? map.host : "localhost"
        port = map.containsKey("port") ? map.port as int : 5000
        apiKey = map.containsKey("api_key") ? map.api_key : "jWCr-uqJB9fO9s1Lj2QiydXs4fFY2M"
        workflowNameVar= map.containsKey("workflow_name") ? map.workflow_name : "workflow_name"
        workflowVersionVar= map.containsKey("workflow_ver") ? map.workflow_ver : "workflow_ver"
    }

    String getHost() {
        return host
    }

    int getPort() {
        return port
    }

    String getApiKey() {
        return apiKey
    }

    String getWorkflowNameVar() {
        return workflowNameVar
    }

    String getWorkflowVersionVar() {
        return workflowVersionVar
    }


    @Override
    String toString() {
        return "nextpieConfig host='$host:$port'"
    }
}
