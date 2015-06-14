# Introduction #

The original code for this project was written in Allegro Common Lisp, and although I still love lisp, we couldn't afford the hefty price tag to continue development in Allegro.  Rather than switch to SBCL or some other free Lisp implementation, I made the plunge and rewrote the system in Java.  I didn't feel like switching all of the existing data files into a format more friendly for Java, so the input format is still very reminiscent of Lisp code.

This page provides minimal documentation so that you can read, create, and modify your own input files.

# Details #

We are going to use the example from my dissertation as our sample input file:

```
(1 (("distance-decreasing(agent,box)" 1 20)
    ("forward(agent)" 0 11)
    ("speed-decreasing(agent)" 11 20)
    ("collision(agent,box)" 20 21)))
(2 (("distance-decreasing(agent,box)" 1 16)
    ("forward(agent)" 0 8)
    ("speed-decreasing(agent)" 8 16)
    ("collision(agent,box)" 16 17)
    ("distance-decreasing(agent,box2)" 1 7)
    ("distance-stable(agent,box2)" 7 9)
    ("distance-increasing(agent,box2)" 9 16)))
(3 (("distance-decreasing(agent,box)" 1 10)
    ("forward(agent)" 0 5)
    ("speed-decreasing(agent)" 5 10)
    ("distance-decreasing(agent,box2)" 1 10)
    ("collision(agent,box)" 10 11)))
(4 (("distance-decreasing(agent,box)" 1 20)
    ("forward(agent)" 0 15)
    ("speed-decreasing(agent)" 15 20)
    ("turn-left(agent)" 9 12)
    ("turn-right(agent)" 5 9)
    ("turn-right(agent)" 12 15)
    ("distance-decreasing(agent,box2)" 1 8)
    ("distance-increasing(agent,box2)" 13 20)))
(5 (("distance-decreasing(agent,box)" 1 5)
    ("distance-decreasing(agent,box)" 11 18)
    ("forward(agent)" 0 14)
    ("speed-decreasing(agent)" 14 18)
    ("turn-right(agent)" 5 7)
    ("turn-left(agent)" 10 13)
    ("distance-decreasing(agent,box2)" 1 6)
    ("distance-increasing(agent,box2)" 14 18)
    ("distance-stable(agent,box2)" 6 14)))		 
```

In this example we are looking at five different instances of an approach activity.  Each instance is a list of two things, a _unique identifier_ (in this case just the index of the instance) and a list of the _fluents_ within that instance.  A fluent is a tuple containing three things: a proposition, the time at which the proposition becomes true, and the time at which the proposition becomes false.

To see the corresponding visual image for the first instance, we include the picture that is also include on the project summary.

![http://ua-time-series.googlecode.com/files/diagram-small.png](http://ua-time-series.googlecode.com/files/diagram-small.png)

The propositions in this example contain relations between objects in some simulator.  All of the instances have a common pattern of the _agent_ approaching the _box_ that is found by the time series code.