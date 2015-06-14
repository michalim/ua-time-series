# Project Summary #

This project was formed while we trying to interpret different activities in [Wubble World](http://www.wubble-world.com).  Wubble World is a virtual environment with simulated physics, in which softbots, called wubbles, interact with objects.  Wubble World is instrumented to collect distances, velocities, locations, colors, sizes, and other sensory information and represent them with propositions such as `Above(wubble,box)` (the wubble is above the box) and `PVM(wubble)` (the wubble is experiencing positive vertical motion).

We recorded several different instances of the wubble performing an activity, like _jump over_.  The plan was to develop an incremental, on-line learning algorithm that would extract the _essence_ of the activity.

The algorithms in my dissertation are designed to work with _propositional multivariate time series_ or PMTS's.  Think of a PMTS as a matrix in which every row represents a proposition, every column represents a moment  in time, and every cell _(i,t)_ contains _1_ or _0_ depending on whether proposition _P_<sub>i</sub>  is true at time _t_.  Consecutive moments during which proposition _P_<sub>i</sub> is true are called the _fluent_ _P_<sub>i</sub>, as illustrated in the figure.

![http://ua-time-series.googlecode.com/files/diagram-small.png](http://ua-time-series.googlecode.com/files/diagram-small.png)

This software contains implementations of the algorithms discussed in each of the cited publications.  This software can be used to classify and recognize a wide range of activities, from handwriting recognition to simulated agent activity recognition.

# Project Support #

YourKit is kindly supporting open source projects with its full-featured Java Profiler.
YourKit, LLC is the creator of innovative and intelligent tools for profiling Java and .NET applications. Take a look at YourKit's leading software products:

[YourKit Java Profiler](http://www.yourkit.com/java/profiler/index.jsp)<br>
<a href='http://www.yourkit.com/.net/profiler/index.jsp'>YourKit .NET Profiler</a>

<h1>Related Publications</h1>

<ol><li>Wesley Kerr, Anh Tran, and Paul Cohen.  (2011) Activity Recognition with Finite State Machines.  In Proceedings of the International Joint Conferences on Artificial Intelligence (IJCAI 2011).<br>
</li><li>Wesley Kerr, Paul Cohen, and Niall Adams.  (2011)  Recognizing Players’ Activities and Hidden State.  In Proceedings of Foundations of Digital Games (FDG 2011).<br>
</li><li>Wesley Kerr. (2010) Learning to Recognize Agent Activities and Intentions.  Ph.D. dissertation, University of Arizona, Tucson, AZ, USA.<br>
</li><li>Wesley Kerr, Paul Cohen. (2010) Recognizing Behaviors and the Internal State of the Participants. In IEEE International Conference of Development and Learning (ICDL 2010).</li></ol>

<h1>Project Statistics</h1>

<wiki:gadget url="http://www.ohloh.net/p/585107/widgets/project_basic_stats.xml" height="220" border="1"/><br>
<br>
<h1>Related Projects</h1>

<a href='http://www.wubble-world.com'>Wubble World</a> -- The original 3D Wubble World simulation<br>
<br>
<a href='http://code.google.com/p/wubbleworld2d/'>Wubble World 2D</a> -- An agent based simulation used to generate different activities that we could learn from.