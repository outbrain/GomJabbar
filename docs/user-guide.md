# User Guide

## Starting GomJabbar
To start the server simply call the suuplied startup script, 
optionally providing extra arguments needed for configuring optional integration like rundeck: 
```bash
export GJ_OPTIONS="<Gom Jabbar java options>"
./gomjabbar.sh
```
The minimal options must contain the config file location:
```bash
export GJ_OPTIONS="-Dcom.outbrain.gomjabbar.configFileUrl=<config file url> ..."
```
When rundeck is used, please pass in the following system properties like so:
```bash
export GJ_OPTS="-Dcom.outbrain.gomjabbar.rundeckAuthToken=<rundeck auth token> -Dcom.outbrain.gomjabbar.rundeckHost=<rundeck host>"
```
When consul isn't installed on localhost please pass in the consul url like so:
```bash
export GJ_OPTS="-Dcom.outbrain.ob1k.consul.agent.address=consul-50001:8500 ..."
```

## Selecting targets
...

## Triggering Failures
...

## Audit log
...

## Revert
...
