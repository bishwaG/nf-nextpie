
# The plugin `nf-nextpie` 
 
The project contains a Nextflow plugin (adapted from `nf-plugin`) called `nf-nextpie` which serves as a client for [Nextpie](https://github.com/bishwaG/Nextpie/) server. The plugin uploads (trace.txt file) resource usage data from a nextflow pipeline to Nextpie for aggregated resource usage analysis and visualization.

The plugin readily comes with a config file `plugins/nf-nextpie/src/main/nextflow/nextpie/config.json`. It contains parameters needed for Nextpie with default values. The default config file assumes that Nextpie is running in `localhost`. The config file parameters can be modified according to one's need. 

## Config file
After the first use of the plugin in Nextflow using `-plugins nf-nextpie@0.0.1`, Nextflow will download the plugin to `$HOME/.nextflow/plugins/nf-nextpie-0.0.1`. The config file will be found from `$HOME/.nextflow/plugins/nf-nextpie-0.0.1/classes/nextflow/nextpie/config.json`. Following is the content of `config.json`. 

```
{
  "host": "localhost",
  "port": 5000,
  "api-key": "jWCr-uqJB9fO9s1Lj2QiydXs4fFY2M",
  "workflow-name-var" : "workflow_name",
  "worfklow-version-var" : "workflow_ver"
}
```
### `host`
This is a hostname/IP address of a machine running Nextpie. The default value is `localhost` which meaning you should be running Nextpie in the current machine.

### `port`
This is the port in which Nextpie is running. Do not change the port until and unless you know what you are doing.

### `api-key`
This is a API key need for authntication. Using this key the client (nf-plugin) authenticates itself with Nextpie server. In production use, generate an unique API key from Nextpie's GUI and replace the defaut one. This is highly recommend for security reasons.

### `workflow-name-var` and `worfklow-version-var`
This is the name of Nextflow variable you used in the pipeline to store pipeline name. The default calue is `workflow_name` which means you have a variable named `workflow_name` in your Nextflow pipeline (eg. in `nextflow.config`). You can change the value `workflow_name` in `$HOME/.nextflow/plugins/nf-nextpie-0.0.1/classes/nextflow/nextpie/config.json` if you have different variable. Similary, the `worfklow-version-var` contains a variable name (default: `workflow_ver`) used in Nextflow pipeline to store pipeline version. This can also be changed according to your need.

Then default config parameters can be overwritten by provided commandline parameters `--host`, `--port`, `--api_key`, `--workflow_name`, and `--workflow_ver` via Nextflow's commandline. The following is an example.

```
/PATH/TO/nextflow run /path/to/main.nf \
  --host 192.168.0.5 \
  --port 80 \
  --api_key HGnm4sdfiJHH06 \
  --workflow_name myPipeline \
  --workflow_ver 0.0.1
  ....
```

## Plugin structure

```
├── plugins
│   ├── build.gradle
│   └── nf-nextpie                          #The plugin base directory.
│       ├── build.gradle                    #Plugin Gradle build file.
│       └── src
│          ├── main                        #The plugin implementation sources.
│          │   └── nextflow
│          │       └── nextpie
│          │           ├── config.json
│          │           ├── nextpieFactory.groovy
│          │           ├── nextpieObserver.groovy
│          │           └── nextpiePlugin.groovy
│          ├── resources
│          │   └── META-INF
│          │       ├── extensions.idx       #This file declares one or more extension classes 
│          │       │                        #provided by the plugin. Each line should contain 
│          │       │                        # the fully qualified name of a Java class that 
│          │       │                        #implements the org.pf4j.ExtensionPoint interface.
│          │       │
│          │       └── MANIFEST.MF          #Manifest file defining the plugin attributes
│          │                                #e.g. name, version, etc. The attribute Plugin-Class
│          │                                #declares the plugin main class. This class should 
│          │                                #extend the base class nextflow.plugin.BasePlugin 
│          │                                #e.g. `nextflow.nextpie.nextpiePlugin`.
│          │
│          └── test                         #The plugin unit tests.                  
│              └── nextflow
│                  └── nextpie
│                      ├── HelloDslTest.groovy
│                      ├── MockHelpers.groovy
│                      ├── nextpieFactoryTest.groovy
│                      └── TestHelper.groovy
│       
├── README.md
└──  settings.gradle                         #Gradle project settings.
```

## Plugin classes

- `nextpieFactory` and `nextpieObserver`: shows how to react to workflow events with custom behavior
- `nextpiePlugin`: the plugin entry point

## Running the plugin

1. Clone the Nextpie repository:
    ```bash
    cd $HOME
    git clone https://github.com/bishwaG/Nextpie.git
    ```
  
2. Configure the plugin build to use the local Nextflow code:
    ```bash
    cd Nextpie/assets/example-workflow/test-runs/
    ```
  
4. Run the Nextflow example workflow that comes with [Nextpie](https://github.com/bishwaG/Nextpie/).
```
./nextflow run \
   ../main.nf \
   --fastqs 'fastq/*_R{1,2}*.fastq.gz' \
   --name "test_project" \
   --group "test_research_group" \
   -resume
```

> NOTE: `$HOME/Nextpie/cd assets/example-workflow/nextflow.config` contains `plugins { id 'nf-nextpie' }` which will tell Nextflow to pull `nf-nextpie` during a runtime.  The config file `nextflow.config` also contains variables `workflow_name` to store pipeline name and `workflow_ver` to store pipeline version. 


## Integrating `nf-nextpie` with a Nextflow pipeline.

To use the plugin in any Nexflow pipleine add the following content in `nextflow.config` file of your Nextflow pipeline. This way the pipeline will use the plugin during every run.

```
plugins {
  id 'nf-nextpie'
}
```

If you do not want to ad the plugin to a pipeline, but want to use in run-by-run basis provide `-plugins nf-nextpie@0.0.1` parameter in Nextflow's command-line.

## Building and installing the plugin

```
## Clone the repo
git clone https://github.com/bishwaG/nf-nextpie.git

## Unit testing
make check

## build the plugin
make

## Install the plugin
## Installs to $HOME/.nextflow/plugins
make install

## Upload to Git and make a version release
make upload
```

