# GomJabbar - Chaos Monkey for your private cloud
[![Build Status](https://travis-ci.org/outbrain/GomJabbar.svg?branch=master)](https://travis-ci.org/outbrain/GomJabbar)

## What is GomJabbar?
GomJabbar is a service inspired by [Netflix's ChaosMonkey](https://github.com/Netflix/chaosmonkey), 
but unlike ChaosMonkey, it was designed to work with your private cloud infrastructure (i.e. your own data centers).

The service exposes endpoints that allow you to randomly select targets, trigger a selected fault, and revert when needed. 

## Why should I run GomJabbar?
You can find the Netflix explanation [here](https://github.com/Netflix/SimianArmy/wiki/Chaos-Monkey#why-run-chaos-monkey).
No point in copying that over ;)

The main idea is to reduce our fear from production (fear is the mind killer remember?). 
If you want to learn how to improve your code, monitoring, and alerting system, 
learn how to deal with production issues when you're awake and ready, this is the tool for you.

After running several chaos drills at Outbrain, I can assure you that doing this on a regular basis is extremely valueable.
During a midnight page most people will not fix anything, nor investigate too far, and the incident will usually end with a service restart.
During a chaos drill we look deeper into the root causes, and try to learn what we need to fix, and where we need to improve.
After every drill we conduct a quick take-in and implement the fixes as soon as possible.

Running GomJabbar helps us validate our assumptions, our infrastructure, our resilience, and our fixes.

## Supported faults
GomJabbar supports an extensible fault injection mechanism, along with a configuration based fault triggering commands and scripts.
The [example config file](config-template.yaml) contains examples ranging from harmless failures to graceful / graceless shutdowns and traffic control (network issues emulation).


## Integration
### Service Discovery
We currently integrate with [consul](https://www.consul.io/) out of the box, and provide a configuration based filtering for the targets. 
Future versions will integrate with other service discovery methods, and the tool was designed to easily support this. 

### Fault Automation
Gom Jabbar now integrates with [RunDeck](http://rundeck.org/), and [Ansible](http://docs.ansible.com/ansible/). 
Future versions may provide other automation tools, or a built-in ssh capabilities / agents.

## User Guide
[User Guide](docs/user-guide.md) 