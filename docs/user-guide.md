# User Guide

## Building
```bash
git clone https://github.com/outbrain/GomJabbar.git
cd GomJabbar
mvn clean install
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
