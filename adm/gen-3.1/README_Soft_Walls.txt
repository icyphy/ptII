$Id$
README for Soft Walls demonstration.

This directory contains a simple 2 dimensional simulation of 
Soft Walls no-fly zone.

    
Building
--------
The simulation requires:
    Java 1.4 SDK or later - http://java.sun.com
    Java 3D               - http://java.sun.com/products/java-media/3D/
    Ptolemy II 3.0.2      - http://ptolemy.eecs.berkeley.edu/ptII

1. Download and install Java
2. Download and install Ptolemy II 3.0.2
3. Untar this file over the Ptolemy II directory:
     cd $PTII
     tar -xf softwalls.tar.gz
4. Build the Soft Walls actors in $PTII/ptolemy/apps/softwalls:
     cd $PTII/ptolemy/apps/softwalls
     make
5. Run the model:
     vergil fixedTime/fixedTime.xml

Running
-------
When the applet starts up, a small Java Applet Window will appear
outside your browser.

This window _must_have_the_focus_ (be on the top
and have the title bar highlighted) so that it can accept your left
and right arrow key strokes that will steer the plane.

If you do not steer the plane, then as it approaches the red
no-fly zone, it will steer away from it.

If you try to steer the plane into the red no-fly zone, then as it
approaches the no-fly zone, the blending controller will provide
more and more input, preventing the plane entering the no-fly zone.

Limitations
-----------

* Note that when running the model, the small Java window in that
pops up outside this window must have the focus so that it can
get the left arrow and right arrow keystrokes.


* The dynamics of the plane are not simulated - real commercial
jetliners cannot turn on a dime.  The point here is that we are
blending the control - as the plane gets closer to the no-fly zone, it
ignores the pilot's commands more and more until it eventually turns
away.
Control can be regained by steering away from the no-fly zone.

* The wingtip of the plane might end up in the no-fly zone.
The point here is that the center of the plane stays out of the no-fly
zone.

* You might experience out of memory errors.  There are four data
sets
of roughly 1,000,000 doubles each that consume quite a bit of memory.
If the model appears to hang, try restarting your browser.
