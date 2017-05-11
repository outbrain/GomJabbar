# GomJabbar - Chaos Monkey for your private cloud
[![Build Status](https://travis-ci.org/outbrain/GomJabbar.svg?branch=master)](https://travis-ci.org/outbrain/GomJabbar)

## What is GomJabbar?
GomJabbar is a service inspired by [Netflix's ChaosMonkey](https://github.com/Netflix/chaosmonkey), 
but unlike ChaosMonkey, it was designed to work with your private cloud infrastructure (i.e. your own data centers).

The service exposes endpoints that allow you to randomly select targets, and trigger a selected fault. 

## Integration
### Service Discovery
We currently integrate with [consul](https://www.consul.io/) out of the box, and provide a configuration based filtering for the targets. 
Future versions will integrate with other service discovery methods, and the tool was designed to easily support this. 

### Fault Automation
Gom Jabbar now integrates with [RunDeck](http://rundeck.org/), and [Ansible](http://docs.ansible.com/ansible/). 
Future versions may provide other automation tools, or a built-in ssh capabilities / agents.

## Why should I run GomJabbar?
[TBD]