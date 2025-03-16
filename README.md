# The plugin `nf-nextpie` 
 
This project contains a Nextflow plugin called `nf-nextpie` which serves as a client for [Nextpie](https://github.com/bishwaG/Nextpie/) server. The project has been adapted from `nf-plugin`. The plugin readily comes with a config file `plugins/nf-nextpie/src/main/nextflow/nextpie/config.json`. It contains default values of Nextpie server. The default config file assumes that Nextpie is running in `localhost`. The config file parameters can be modified according to one's need. 

Then default config parameters can be overwritten by provided commandline parameters `--host`, `--port`, and `--api_key` vi Nextflow's commandline. The following is an example.

```
/PATH/TO/nextflow run /path/to/main.nf \
  --host 192.168.0.5 \
  --port 80 \
  --api_key HGnm4sdfiJHH06
  ....
```

## Plugin structure

```
├── plugins
│   ├── build.gradle
│   └── nf-nextpie                          #The plugin base directory.
│       ├── build.gradle                    #Plugin Gradle build file.
│       └── src
│          ├── main                        #The plugin implementation sources.
│          │   └── nextflow
│          │       └── nextpie
│          │           ├── config.json
│          │           ├── nextpieFactory.groovy
│          │           ├── nextpieObserver.groovy
│          │           └── nextpiePlugin.groovy
│          ├── resources
│          │   └── META-INF
│          │       ├── extensions.idx       #This file declares one or more extension classes 
│          │       │                        #provided by the plugin. Each line should contain 
│          │       │                        # the fully qualified name of a Java class that 
│          │       │                        #implements the org.pf4j.ExtensionPoint interface.
│          │       │
│          │       └── MANIFEST.MF          #Manifest file defining the plugin attributes
│          │                                #e.g. name, version, etc. The attribute Plugin-Class
│          │                                #declares the plugin main class. This class should 
│          │                                #extend the base class nextflow.plugin.BasePlugin 
│          │                                #e.g. `nextflow.nextpie.nextpiePlugin`.
│          │
│          └── test                         #The plugin unit tests.                  
│              └── nextflow
│                  └── nextpie
│                      ├── HelloDslTest.groovy
│                      ├── MockHelpers.groovy
│                      ├── nextpieFactoryTest.groovy
│                      └── TestHelper.groovy
│       
├── README.md
└──  settings.gradle                         #Gradle project settings.
```

## Plugin classes

- `nextpieFactory` and `nextpieObserver`: shows how to react to workflow events with custom behavior

- `nextpiePlugin`: the plugin entry point

## Unit testing 

To run your unit tests, run the following command in the project root directory (ie. where the file `settings.gradle` is located):

```bash
./gradlew check
```

## Running the plugin

To build and test the plugin during development, configure a local Nextflow build with the following steps:

1. Clone the Nextpie repository:
    ```bash
    git clone https://github.com/bishwaG/Nextpie.git
    ```
  
2. Configure the plugin build to use the local Nextflow code:
    ```bash
    cd Nextpie/cd assets/example-workflow/test-runs/
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

## Integrating with a Nextflow pipeline.

To use the plugin in any Nexflow pipleine add the following content in `nextflow.config` file of your Nextflow pipeline.

```
plugins {
  id 'nf-nextpie'
}

```




