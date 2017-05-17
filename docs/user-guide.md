# User Guide

## Building
```bash
git clone https://github.com/outbrain/GomJabbar.git
cd GomJabbar
mvn clean install
```

## Configuration
The project ships with a [sample configuration file](../config-template.yaml) to get you started.
The configuration allows you to filter out targets you don't want to test by including and excluding clusters, modules, and tags.

### Failure Execution 
Specify the remote comman executor class. This can be one of the class specified below or your own implementation.
```yaml
execution:
#  command_executor_factory: com.outbrain.gomjabbar.execution.RundeckCommandExecutor
  command_executor_factory: com.outbrain.gomjabbar.execution.AnsibleCommandExecutor
```

#### Creating a custom CommandExecutor
Implement `CommandExecutor`:
Specify a class name implementing a `public static CommandExecutor createCommandExecutor()` method, that will return your implementation.

### Specifying Target Filters
In order to avoid unnecessary risk you can filter out targets by including/excluding clusters, modules, and tags:

```yaml
filters:
  clusters:
    include:    # a list of clusters to be included in the targets
    # if empty, all non excluded clusters are included
    exclude:
    # a list of clusters to be excluded from the targets (can be empty)

  modules:
    include:
    # a list of modules to be included in the targets
    # if empty, all non excluded modules are included
    exclude:
    # a list of modules to be excluded from the targets (can be empty)

  tags:
    include:
    # a list of tags targets must have to be included in the targets
    # if empty, only excluded tags are considered.
    # example:
    #- production
    #- safe-for-chaos
    exclude:
    # a list of tags used for excluding targets containing these tags (can be empty)
```

The items that are included and not in the exclude list will become valid targets for fault injection.

Notes:
* An empty filter, (i.e. not specifying any filtering rules), means EVERYTHING - use the entire discoverable healthy inventory.
* Tags are a collection, thus the filtering rule  is service tags intersection with the filter include tags must not be empty,
and the service tags intersection with the filter exclusion tags must be empty (disjoint).
* We include only healthy targets. If a module instance is failing it's health check, 
it's considered invalid for fault injection for obvious reasons.

### Adding your failure commands
You can specify which failure commands you'd like to execute.
The keys are later used to trigger faults.
Revert commands are optional.

As you can see in the example below, you may use the following tokens in your commands:
* `$(module}` - will be replaced with the consul module name
* `$(host}` - will be replaced with the host name / address specified in consul
* All [system properties](https://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html) keys will be replaced with their appropriate values, e.g. `${user.dir}`. 
For example, this can be used for passing secrets.


```yaml
commands:
  harmless_remote_command:
    description: "Runs a harmless shell command on remote targets - should take about 5 sec to complete"
    fail: "echo 'homeDir=${user.dir} module=${module} host=${host}'; for i in `seq 1 5`; do echo $i; sleep 1; done\n"
    revert: "echo 'reverted'"

  graceful_shutdown:
    description: "Gracefully shuts down services using init.d"
    fail: "sudo service ${module} stop"
    revert: "sudo service ${module} start"

  graceless_shutdown:
    description: "Brutally kills service instances"
    fail: "sudo pkill -9 -f ${module}"
    revert: "sudo service ${module} start"

  traffic_controller:
    description: "Introduces high latency, and packet loss"
    fail: "DEV=`sudo route | grep default | awk \"'{print $NF}'\"`; sudo tc qdisc add dev $DEV root netem delay 300ms loss 5%; sudo tc -s qdisc"
    revert: "sudo tc qdisc del dev `route | grep default | awk \"'{print $NF}'`\" root; sudo tc -s qdisc"
```

### Adding your failure command scripts (fetch from url)
You may also specify scripts that will be fetched from a URL and executed at the target host.
Revert script is optional.

```yaml
scripts:
  My_Fault_Script:
    description: "Fails stuff"
    fail:
      URL: "http://my.script.com/script.sh"
      args: "-f foo -b bar"
    revert:
      URL: "http://my.script.com/script.sh"
      args: "-revert"
```

## Starting GomJabbar
To start the server simply call the suuplied startup script, 
optionally providing extra arguments needed for configuring optional integration like rundeck: 
```bash
export GJ_OPTIONS="<Gom Jabbar java options>"
./gomjabbar.sh
```

### The GJ_OPTIONS
The minimal options must contain the config file location:
```bash
export GJ_OPTIONS="-Dcom.outbrain.gomjabbar.configFileUrl=<config file url> ..."
```
When rundeck is used, please pass in the following system properties as well:
* `-Dcom.outbrain.gomjabbar.rundeckAuthToken=<rundeck auth token>`
* `-Dcom.outbrain.gomjabbar.rundeckHost=<rundeck host>`

When consul isn't installed on `localhost` please pass in the consul url like so:
`-Dcom.outbrain.ob1k.consul.agent.address=consul-50001:8500`


## REST API
GomJabbar currently implements a REST API that allows you to fetch random targets, 
trigger faults on these targets, view the actions (audit) log, and revert.

Future versions may add a UI, and auto-failure-scheduling, but hey - this is Sparta ;)

### Selecting targets
Selecting target is done via the `/SelectTarget` endpoint.
Targets are cached for 2 hours, and can be used to trigger faults later using the generated token.

```bash
$ curl "http://localhost:8080/gj/api/selectTarget" | json_pp
{
   "-2898334020417800152" : {
      "instanceCount" : 16,
      "module" : "MyModule",
      "host" : "some-host.mycompany.com",
      "tags" : [
         "all",
         "hostType-metal",
         "cluster-dc1",
         "servicetype-ob1k",
         "environment-prod",
         "httpPort-8080",
         "contextPath-/my"
      ]
   }
}
```

### Fault Options
If you don't remember the faults you configured, you can review them using the `/faultOptions` endpoint:

```bash
$ curl "http://localhost:8080/gj/api/faultOptions" | json_pp 
{
   "com.outbrain.gomjabbar.faults.DummyFault" : "I'm just used to debug the flow ;)",
   "graceful_shutdown" : "Gracefully shuts down services using init.d",
   "harmless_remote_command" : "Runs a harmless shell command on remote targets - should take about 5 sec to complete",
   "traffic_controller" : "Introduces high latency, and packet loss",
   "graceless_shutdown" : "Brutally kills service instances"
}
```

### Triggering Failures
Once you're happy with the random target, you can trigger a failure using the `/trigger` endpoint:

```bash
$ curl "http://localhost:8080/gj/api/trigger?targetToken=<target-token>&faultId=<fault-id>"
```
For example, using the generated token above, we can trigger a graceless shutdown like so:

```bash
$ curl "http://localhost:8080/gj/api/trigger?targetToken=2898334020417800152&faultId=graceless_shutdown"
```

### Audit log
The `/log` endpoint displays the triggered fault since startup:

```bash
$ curl "http://localhost:8080/gj/api/log" | json_pp 
{
   "fault8904757121490943784" : {
      "faultInjectorId" : "graceless_shutdown",
      "target" : {
        "module" : "MyModule",
        "host" : "some-host.mycompany.com",
        "tags" : [
           "all",
           "hostType-metal",
           "cluster-dc1",
           "servicetype-ob1k",
           "environment-prod",
           "httpPort-8080",
           "contextPath-/my"
        ]
      }
   }
}
```

### Revert
You may `/revert` a single triggered fault:

```bash
$ curl "http://localhost:8080/gj/api/revert?faultId=fault8904757121490943784" 
```

Or all triggered faults:
```bash
curl "http://localhost:8080/gj/api/revertAll"
```
