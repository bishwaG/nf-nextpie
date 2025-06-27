
# The `nf-nextpie` Plugin

This project provides a Nextflow plugin called `nf-nextpie` (adapted from [nf-hello](https://github.com/nextflow-io/nf-hello)) that serves as a client for the [Nextpie](https://github.com/bishwaG/Nextpie/) server. The plugin uploads a trace file (containing resource usage data) from a Nextflow pipeline to Nextpie for aggregated resource usage analysis and visualization.

The plugin includes a configuration file located at `plugins/nf-nextpie/src/main/nextflow/nextpie/config.json`. This file contains parameters required for `nf-nextpie` to communicate with the Nextpie server. By default, it assumes Nextpie is running on `localhost`. You can modify the parameters in the config file to suit your environment.

## Prerequisites

Since the plugin uploads a trace file along with other metadata (pipeline name, version, research group name, and project name), it is essential to enable trace file generation in a Nextflow pipeline. This can be done in one of the following ways:

- By supplying the `-with-trace` command-line option to Nextflow.
- By adding `-with-trace` to the pipeline's configuration file (`nextflow.config`).

## The Configuration File

After the plugin is first used with Nextflow (e.g., via `-plugins nf-nextpie@0.0.2`), it is downloaded into `$HOME/.nextflow/plugins/nf-nextpie-0.0.2`. The configuration file can be found at:

```
$HOME/.nextflow/plugins/nf-nextpie-0.0.2/classes/nextflow/nextpie/config.json
```

The default contents of `config.json` are:

```json
{
  "host": "localhost",
  "port": 5000,
  "api-key": "jWCr-uqJB9fO9s1Lj2QiydXs4fFY2M",
  "workflow-name-var": "workflow_name",
  "workflow-version-var": "workflow_ver"
}
```

### `host`

The hostname or IP address of the machine running the Nextpie server. The default is `localhost`, which refers to the local machine.

### `port`

The port on which the Nextpie server is running. Do not change this unless you know what you're doing or if the default port is already in use.

### `api-key`

An API key required for authentication. The client (`nf-nextpie`) uses this key to authenticate with the Nextpie server. In a production environment, it is highly recommended to generate a unique API key using the Nextpie GUI and replace the default value for security purposes.

### `workflow-name-var` and `workflow-version-var`

These are the names of the Nextflow variables that store the pipeline name and version, respectively. By default, they are set to `workflow_name` and `workflow_ver`, meaning these variables should exist in your pipeline's `params` scope (e.g., in `nextflow.config`).

If your pipeline uses different variable names, update the config file accordingly.

The plugin looks for these variables inside the `params` scope. You should define them as follows:

```groovy
params {
  workflow_name = 'my-workflow'
  workflow_ver  = '1.0.1'
}
```

## Default Behavior

By default, the plugin searches `name` and `version` variables within the `manifest` scope. If these  variables are present within the `manifest` scope , the plugin will **ignore** the `params.workflow_name` and `params.workflow_ver` values (that are within the `params` scope) and use the ones from the `manifest`.

**Example:**

```groovy
manifest {
  name    = 'my-workflow'
  version = '1.0.1'
}
```

## Integrating `nf-nextpie` with a Nextflow Pipeline

There are two ways to integrate `nf-nextpie` into a Nextflow pipeline:

### ✅ Option 1: Using `nextflow.config`

Add the following to the `plugins` block in `nextflow.config`:

```groovy
plugins {
  id 'nf-nextpie@0.0.2'
}
```

This allows all runs of the pipeline to use the plugin by default.

### ✅ Option 2: Using the Command Line

Supply the plugin using the command-line option for each run:

```bash
nextflow run mypipeline.nf -plugins nf-nextpie@0.0.2
```

## Running an Example Pipeline

Refer to the official [Nextpie documentation](https://github.com/bishwaG/Nextpie/blob/main/docs/nextflow-workflow.md) for details on running an example Nextflow pipeline.

## Building and Installing the Plugin

```bash
# Clone the repository
git clone https://github.com/bishwaG/nf-nextpie.git

# Run unit tests
make check

# Build the plugin
make

# Install the plugin to $HOME/.nextflow/plugins
make install

# Upload to GitHub and release a version
make upload
```

## Plugin Structure

```
├── plugins
│   ├── build.gradle
│   └── nf-nextpie                          # Plugin base directory
│       ├── build.gradle                    # Plugin's Gradle build file
│       └── src
│          ├── main                         # Plugin implementation sources
│          │   └── nextflow
│          │       └── nextpie
│          │           ├── config.json
│          │           ├── nextpieFactory.groovy
│          │           ├── nextpieObserver.groovy
│          │           └── nextpiePlugin.groovy
│          ├── resources
│          │   └── META-INF
│          │       ├── extensions.idx       # Declares plugin extension classes
│          │       └── MANIFEST.MF          # Defines plugin metadata and main class
│          └── test                         # Unit tests for the plugin
│              └── nextflow
│                  └── nextpie
│                      ├── HelloDslTest.groovy
│                      ├── MockHelpers.groovy
│                      ├── nextpieFactoryTest.groovy
│                      └── TestHelper.groovy
├── README.md
└── settings.gradle                         # Gradle project settings
```

## Plugin Classes

- `nextpieFactory` and `nextpieObserver`: Define custom behaviors in response to workflow events.
- `nextpiePlugin`: Serves as the plugin entry point.
