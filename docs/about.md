# Failure Testing for your private cloud

Here at Outbrain we manage quite a large scale deployment of hundreds of services / modules, 
and thousands of hosts. We practive CI/CD, and implemented quite a sound infrastructure, 
which we believe is scalable, performant, and resilient. 
We do however experience many production issues on a daily basis, just like any other large scale organization.
You simply can't ensure a 100% fault free system - servers will crash, run out of disk space, 
lose connectivity to the network, and software will experience bugs, and erroneous conditions. 
Our job as software engineers is to anticipate these conditions, and design our code to handle them gracefully.

For quite a long time we were looking into ways of improving our resilience, and validate our assumptions, using a tool like Netflix's [Chaos Monkey](https://github.com/Netflix/chaosmonkey).
We also wanted to make sure our alerting system actually triggers when things go wrong.
The main problem we were facing is tha Chaos Monkey is a tool that was designed to work with cloud infrastructure, 
while we maintain our own private cloud.

The main motivation for developing such a tool, is that failures have the tendency of occurring when you're least prepared, and in the least desirable time - 
e.g. Friday nights, when you're out having a pint with your buddies. Now, to be honest with ourself, 
when things fail during inconvinient times, we don't always roll our sleeves and dive in to look for the root cause. 
Many times the incident will end after a service restart, and once the alerts clear we forget about it.

Wouldn't it be great if we could have "chaos drills", where we could practice handling failures, 
test and validate our assumptions, and learn how to improve our infrastructure?

## Chaos Drills at Outbrain

We built [GomJabbar](https://github.com/outbrain/GomJabbar) exactly for the reasons specified above. 
Once a week, at a well known time, mid day, we randomly select a few targets where we trigger failures.
At this point, the system should either auto-detect the failures, and auto-heal / bypass them.
In some cases alerts should be triggered to let teams know that a manual intervention is required.

After each chaos drill we conduct a quick take-in session for each of the triggered failures, 
and investigate whether the system handled the failure case correctly, whether we installed a proper alerting strategy,
and whether the team knew how to handle, and how to investigate the issue. 
These take-ins lead to super valuable inputs, which we probably wouldn't collect any other way.

### How did we kick this off?

Before we started running the chaos drills, there were a lot of concerns about the value of such drills, and the time it will require.
What we did to mitigate this, was to understand what was bothering the teams. So here goes:
* There's an obvious need to avoid unnecessary damage.
  * We've created filters to ensure only approved targets get to participate in the drills. 
  This has a side effect of pre-marking areas in the code we need to take care of.
   * We currently schedule drills via statuspage.io to let teams get ready, and if there's a special kind of fault we're going to trigger
   we explain it in advance.
   * We started out from minor faults like graceful shutdowns, continued to graceless shutdowns, 
   and moved on to more interesting testing like faulty network emulation.
* We've measure the time teams spent on these drills. In practice, all teams spent very little time on these drills.
   Most of the time spent, was on ensuring we have proper alerting, and correct resilience features in the clients, 
   and this is actually something you need to do anyway. A little time was spent on understanding the failures.
   At the end of the day, I've heard no complaints about interruptions, nor time waste.
* We've made sure teams, and engineers on call were not left on their own. We wanted everybody to learn 
from this drill, and when they were'nt sure how to proceed, we jumped in to help. It's important
to make everyone feel safe about this drill, and remind everybody that we only want to learn and improve.

   
All that said, it's important to remember that we basically simulate issues that occur on a daily basis.
It's only that when we do that in a controlled manner, it's easier to observe where are our blind spots, what knowledge are we lacking,
and what we need to improve.
 
### What next?

Up until now, this drill was executed in a semi-automatic procedure. The next level is to let the teams run this drill 
on a fixed interval, at a well known time. We will also add new kinds of failures, like disk space issues, power failures, etc.