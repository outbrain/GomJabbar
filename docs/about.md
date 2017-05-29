# Failure Testing for your private cloud (or why we've created GomJabbar)

**TL;DR** Chaos Drills cab contribute a lot to your services resilience, and it's actually quite a fun activity.
 We've built a tool called [GomJabbar](https://github.com/outbrain/GomJabbar) to help you run those drills.
---
 
Here at Outbrain we manage quite a large scale deployment of hundreds of services / modules, 
and thousands of hosts. We practice CI/CD, and implemented quite a sound infrastructure, 
which we believe is scalable, performant, and resilient. 
We do however experience many production issues on a daily basis, just like any other large scale organization.
You simply can't ensure a 100% fault free system. Servers will crash, run out of disk space, 
and lose connectivity to the network. Software will experience bugs, and erroneous conditions. 
Our job as software engineers is to anticipate these conditions, and design our code to handle them gracefully.

For quite a long time we were looking into ways of improving our resilience, and validate our assumptions, using a tool like Netflix's [Chaos Monkey](https://github.com/Netflix/chaosmonkey).
We also wanted to make sure our alerting system actually triggers when things go wrong.
The main problem we were facing is that Chaos Monkey is a tool that was designed to work with cloud infrastructure, 
while we maintain our own private cloud.

The main motivation for developing such a tool, is that failures have the tendency of occurring when you're least prepared, 
and in the least desirable time, e.g. Friday nights, when you're out having a pint with your buddies. 
Now, to be honest with ourselves, when things fail during inconvenient times, 
we don't always roll our sleeves and dive in to look for the root cause. 
Many times the incident will end after a service restart, and once the alerts clear we forget about it.

Wouldn't it be great if we could have "chaos drills", where we could practice handling failures, 
test and validate our assumptions, and learn how to improve our infrastructure?

## Chaos Drills at Outbrain

We built [GomJabbar](https://github.com/outbrain/GomJabbar) exactly for the reasons specified above. 
Once a week, at a well known time, mid day, we randomly select a few targets where we trigger failures.
At this point, the system should either auto-detect the failures, and auto-heal, or bypass them.
In some cases alerts should be triggered to let teams know that a manual intervention is required.

After each chaos drill we conduct a quick take-in session for each of the triggered failures, 
and ask ourselves the following questions:
1. Did the system handle the failure case correctly? 
1. Was our alerting strategy effective?
1. Did the team have the knowledge to handle, and troubleshoot the failure?
1. Was the issue investigated thoroughly?
 
These take-ins lead to super valuable inputs, which we probably wouldn't collect any other way.

### How did we kick this off?

Before we started running the chaos drills, there were a lot of concerns about the value of such drills, 
and the time it will require. Well, since eliminating our fear from production is one of the key goals of this activity, 
we had to take care of that first. 

```text
    "I must not fear. 
    Fear is the mind-killer. 
    Fear is the little-death that brings total obliteration. 
    I will face my fear. 
    I will permit it to pass over me and through me. 
    And when it has gone past I will turn the inner eye to see its path. 
    Where the fear has gone there will be nothing. Only I will remain." 
    
    (Litany Against Fear - Frank Herbert - Dune)
```

Se we started a series of chats with the teams, in order to understand what was bothering them, and found ways to mitigate it. 
So here goes:

* There's an obvious need to avoid unnecessary damage.
  * We've created filters to ensure only approved targets get to participate in the drills. 
  This has a side effect of pre-marking areas in the code we need to take care of.
   * We currently schedule drills via statuspage.io, so teams know when to be ready, and if the time is inappropriate, 
   we reschedule.
   * When we introduce a new kind of fault, we let everybody know, and explain what should they prepare for in advance.
   * We started out from minor faults like graceful shutdowns, continued to graceless shutdowns, 
   and moved on to more interesting testing like faulty network emulation.
* We've measured the time teams spent on these drills, and it turned out to be negligible.
   Most of the time was spent on preparations. For example ensuring we have proper alerting, 
   and correct resilience features in the clients.
   This is actually something you need to do anyway. At the end of the day, I've heard no complaints about interruptions, nor time waste.
* We've made sure teams, and engineers on call were not left on their own. We wanted everybody to learn 
from this drill, and when they were'nt sure how to proceed, we jumped in to help. It's important
to make everyone feel safe about this drill, and remind everybody that we only want to learn and improve.

   
All that said, it's important to remember that we basically simulate failures that occur on a daily basis.
It's only that when we do that in a controlled manner, it's easier to observe where are our blind spots, what knowledge are we lacking,
and what we need to improve.
 
### Our roadmap - What next?

* Up until now, this drill was executed in a semi-automatic procedure. The next level is to let the teams run this drill 
on a fixed interval, at a well known time. 
* Add new kinds of failures, like disk space issues, power failures, etc.
* So far, we were only brave enough to run this on applicative nodes, and there's no reason to stop there. 
Data-stores, load-balancers, network switches, and the like are also on our radar in the near future.
* Multi-target failure injection. 
For example, inject a failure to a percentage of the instances of some module in a random cluster. 
Yes, even a full cluster outage should be tested at some point, in case you were asking yourself. 

## The GomJabbar Internals

GomJabbar is basically an integration between a discovery system, a (fault) command execution scheduler, 
and your desired configuration. 

Upon startup, GomJabbar drills down via the discovery system, fetches the clusters, modules, and their instances, 
and passes each via the filters provided in the configuration files. This process is also performed periodically.
We currently support discovery via [consul](https://www.consul.io/), 
but adding other methods of discovery is quite trivial.

When a users wishes to trigger faults, GomJabbar selects a random target, and returns it to the user, 
along with a token that identifies this target.
The user can then trigger one of the configured fault commands, or scripts, on the random target.
At this point GomJabbar uses the configured `CommandExecutor` in order to execute the remote commands on the target hosts.

GomJabbar also maintains a audit log of all executions, which allows you to revert quickly in the face of a real production issue,
or an unexpected catastrophe cause by this tool.

## What have we learned so far?

If you've read so far, you may be asking yourself what's in it for me? What kind of lessons can I learn from these drills?

We've actually found and fixed many issues by running these drills, and here's what we can share:

1. We had broken monitoring and alerting around the detection of the integrity of our production environment.
We wanted to make sure that everything that runs in our data-centers is managed, and at a well known (version, health, etc).
We've found that we didn't compute the diff between the desired state, and the actual state properly, 
due to reliance on bogus data-sources. This sort of bug attacked us from two sides: 
once when we triggered graceful shutdowns, and once for graceless shutdowns.

1. We've found services that had no owner, became obsolete, and were basically running unattended in production. The horror.

1. During the faulty network emulations, we've found that we had clients that didn't implement proper resilience features,
and caused cascading failures in the consumers several layers up our service stack. 
We've also noticed that in some cases, the high latency also cascaded. 
This was fixed by adding proper timeouts, double-dispatch, and circuit-breakers.
    
1. We've also found that these drills motivated developers to improve their knowledge about the metrics we expose, 
logs, and the troubleshooting tools we provide. 

## Conclusion

We've found the chaos drills to be an incredibly useful technique, which helps us improve our resilience and integrity, 
while helping everybody learn about how things work.
We're by no means anywhere near perfection. 
I'm actually pretty sure we'll find many many more issues we need to take care of.
We're hoping this exciting new tool will help us move to the next level, and we hope you find it useful too ;) 