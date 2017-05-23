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
These take-ins lead to super valuable inputs, which we wouldn't collect any other way.