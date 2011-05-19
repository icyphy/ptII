$Id$
Tests for Eclipse and AWT
Information about Eclipse and AWT can be found at
http://chess.eecs.berkeley.edu/ptolemy/wiki/Ptolemy/EclipseAndAWT

This directory contains files copied from other locations.

It is recommended you look at the files in the order below.

By default, ptolemy/apps/eclipse/awt is excluded from the build path.
The easiest thing to do is to:
1) In a shell, find your Ptolemy II installation
2) export PTII=`pwd`; ./configure
3) In the package explorer, refresh by hitting F5
4) Right click on the project, select Build Path | Configure Build Path
5) Edit the list of excluded directories and *remove* ptolemy/apps/eclipse/awt
   This will make it so that ptolemy/apps/eclipse/awt gets compiled.
   
 NOTE: Under Mac OS X, there are problems with the Plug-in Development Environment
 (PDE) and Eclipse.  This problem manifests itself in compile errors.
 
It is easiest to run these files inside Eclipse by right clicking
on the .java file and selecting 'Run As'.

Snippet155.java
From http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/DrawanXusingAWTGraphics.htm
Snippet155 draws an X using AWT

Snippet135.java
From http://dev.eclipse.org/viewcvs/index.cgi/org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet135.java?view=co
This class is a SWT/Swing example that brings up a file tree browser

AWTPlotApplication.java
Based on Snippet135.java
AWTPlotApplication places a PtPlot window inside SWT.
Note that there are no menu choices.

SWTVergilApplication.java
Based on Snipped135.java
SWTVergilApplication places a ptolemy.vergil.actor.ActorGraphModel inside SWT.
See the code for comments.



