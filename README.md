
# The `nf-nextpie` Plugin

This project provides a Nextflow plugin called `nf-nextpie` (adapted from [nf-hello](https://github.com/nextflow-io/nf-hello)) that serves as a client for the [Nextpie](https://github.com/bishwaG/Nextpie/) server. The plugin uploads a trace file (containing resource usage data) from a Nextflow pipeline to Nextpie for aggregated resource usage analysis and visualization.

The plugin includes a configuration file located at `plugins/nf-nextpie/src/main/nextflow/nextpie/config.json`. This file contains parameters required for `nf-nextpie` to communicate with the Nextpie server. By default, it assumes Nextpie is running on `localhost`. You can modify the parameters in the config file to suit your environment.

[](assets/images/nf-nextpie.png)

## Prerequisites

Since the plugin uploads a trace file along with other metadata (pipeline name, version, research group name, and project name), it is essential to enable trace file generation in a Nextflow pipeline. This can be done in one of the following ways:

- By supplying the `-with-trace` command-line option to Nextflow.
- By adding `-with-trace` to the pipeline's configuration file (`nextflow.config`).

Additionally, Nextpie expects the following default columns to be present in the trace file. If you have custom columns, ensure that the trace file still includes the default columns with values in the expected format. However, there is no strict requirement for the order of the columns.

| task_id | hash | native_id | name | status | exit | submit | duration | realtime | %cpu | peak_rss | peak_vmem | rchar | wchar |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 5 | 9b/fa305b | - | workflow_summary | COMPLETED | - | 2023-10-04T12:03:41.000 | 22ms | 8ms | - | - | - | - | - |
| 6 | f5/b4dee1 | 18151 | version_collection | COMPLETED | 0 | 2023-10-04T12:03:41.000 | 36.9s | 18.7s | 30.80% | 57 MB | 397.5 MB | 26.5 MB | 27 KB |
| 1 | 91/d15668 | 7319786 | fastqc (Sample_1) | COMPLETED | 0 | 2023-10-04T12:03:41.000 | 20m 24s | 20m 15s | 100.30% | 364.8 MB | 3.5 GB | 9.5 GB | 2.1 MB |
| 2 | e6/12653d | 7319785 | fastqc (Sample_2) | COMPLETED | 0 | 2023-10-04T12:03:41.000 | 26m 19s | 26m | 101.10% | 418.2 MB | 3.5 GB | 12.1 GB | 2.1 MB |
| 3 | a5/b975da | 7319781 | trim_galore (Sample_1) | COMPLETED | 0 | 2023-10-04T12:03:41.000 | 39m 15s | 38m 55s | 436.10% | 4.3 GB | 6 GB | 331.6 GB | 322 GB |
| 4 | 06/ad2faa | 7319782 | trim_galore (Sample_2) | COMPLETED | 0 | 2023-10-04T12:03:41.000 | 49m 31s | 48m 58s | 440.30% | 4.4 GB | 6 GB | 429.5 GB | 417.2 GB |
| 7 | 7f/7d1fc9 | 7319792 | star (Sample_1) | COMPLETED | 0 | 2023-10-04T12:42:56.000 | 1h 8m 37s | 1h 7m 49s | 614.90% | 36.2 GB | 38.7 GB | 194.4 GB | 65.9 GB |
| 8 | 89/7ad716 | 7319956 | featureCounts (Sample_1) | COMPLETED | 0 | 2023-10-04T13:51:33.000 | 32m 50s | 7m 4s | 445.50% | 937.1 MB | 1.6 GB | 25.9 GB | 4.8 GB |
| 9 | e7/58347f | 7319820 | star (Sample_2) | COMPLETED | 0 | 2023-10-04T12:53:12.000 | 1h 37m 56s | 1h 37m 33s | 702.80% | 36.4 GB | 39 GB | 255.2 GB | 95 GB |
| 10 | 7f/6efca0 | 7320052 | featureCounts (Sample_2) | COMPLETED | 0 | 2023-10-04T14:31:10.000 | 28m 34s | 17m 35s | 341.00% | 1.3 GB | 1.9 GB | 55.5 GB | 21.9 GB |

> ⚠️  NOTE: By default, Nextflow trace files have the formats shown above. **Nextpie** supports only these default formats. If you have configured custom formats for dates, times, or other columns, **Nextpie may not parse them correctly**, and this may result in unexpected behavior.
 
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

> ⚠️ NOTE: Do not modify the `workflow-name-var` and `workflow-version-var` variables. These are not user-configurable parameters.


These are the names of the Nextflow variables that store the pipeline name and version, respectively. Their values are expected to be set to `workflow_name` and `workflow_ver`, meaning these variables must exist within your pipeline's `params` scope (e.g., in `nextflow.config`).

The plugin searches for these variables in the `params` scope. Therefore, `workflow_name` and `workflow_ver` should be defined as follows:

```groovy
params {
  workflow_name = 'my-workflow'
  workflow_ver  = '1.0.1'
}
```


The plugin looks for `name` and `version` in the `manifest` scope, and for `workflow_name` and `workflow_ver` in the `params` scope. If `name` and `version` are present in `manifest`, the plugin will **ignore** `workflow_name` and `workflow_ver` from `params` and instead use the values from `manifest`.

If you're using [nf-schema](https://github.com/nextflow-io/nf-schema) in your pipeline, leveraging the `manifest` scope is often more advantageous, as it avoids the need for intrusive modifications to your Nextflow pipeline. As long as the `name` and `version` variables are defined in the `manifest` scope, they will be automatically picked up by the plugin.

While `nf-nextpie` can retrieve the workflow name and version from the `params` scope, using the `manifest` scope is highly recommended. It is considered best practice to store pipeline metadata—such as `name`, `version`, `affiliation`, `email`, `github`, and similar fields—within the `manifest` block.
**Example:**

```groovy
manifest {
  name    = 'my-workflow'
  version = '1.0.1'
}
```
> NOTE: Both `params` and `manifest` scopes are defined in the Nextflow pipeline's configuration file (`nextflow.config`).
> 
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
