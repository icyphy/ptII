ptII/doc/uml/README.txt
Version: $Id$

This directory contains Visio 5 uml diagrams.
We are using Visio 5, not Visio 2000.
Visio 2000 is not backward compatible, it is possible to view
Visio 5 uml diagrams, but not update them.

Note that one exception is that the giotto chapter uses Visio 2000.

The contents of this directory are not shipped with the release.

We convert the uml files to wmf files and import them by reference
into the Framemaker files in ../design/src

The easiest way to update the Visio files is to:
1. Print out the Visio file
2. Look at the javadoc output for the appropriate classes.
   In this way, we also proof the javadoc output

   * Don't show overridden methods.  If a child class overrides foo(),
     then foo() need not be included in the child class.
 
   * If you use a different package, then you need not include all the
     methods in that package.  The package should be have dotted lines.
     Consider specifying the full package name of classes in different
     packages.

   * Private variables: Don't include implementation details.  Include
     only important functional details.  NamedObj has a private string
     that is the name, it should be included.

   * Static attributes are underlined. 
     In Visio, this is called Class-Scope:

     Class-Scope Check to indicate that the attribute is a class-scope
     attribute rather than an instance-scope attribute. Class-scope
     attributes are underlined when they appear on class or type shapes in
     a UML diagram.

   * Classes do not inherit from interfaces, they extend interfaces,
     so the line between a class and an interface should be dashed,
     not solid.  If an interface extends another interface, the
     line is usually solid?

   * Inner classes should have a dot separated notation: "Outer.Inner",
     not "Outer$Inner".  "Outer.Inner" is how inner classes are
     specified in the javadoc output.

3. Mark up the printed copy
4. Run Visio and update the file
5. Commit your changes
6. Update the appropriate figure in the Framemaker documentation.
   * We use Windows Meta File (WMF) format
   * Always import the contents, do not import by reference
   1. In Visio, Edit->Copy Drawing
   2. In Frame, delete the old image by clicking on it and hitting
      delete
   3. In Frame, select Edit->Paste Special and then select Metafile
   4. If you resize, try to keep the aspect ratio the same so that
      the letters do not get squashed.  The best way to do this is
      to select Graphics -> Scale
   


Figure	UML file		Page within that file
1.2	packages.uml		packages
1.3	packages.uml		domains
1.5	uml.uml			summary

3.1	actor.lib.uml		actor.lib
3.2     actor.lib.gui.uml
3.3	??? Token class
3.6	actor.lib.uml		Sources
3.7	actor.lib.uml		Sinks
3.8	actor.lib.logic.uml	Page-1
3.9	actor.lib.conversions.uml  Page-1     "Copy Document" menu not present?

5.9	moml.uml		Logical View

6.2	actor.gui.vsd		Applets

7.2	kernel.vsd		key-kernel	key classes in kernel
						Note that the vsd page is a
						a copy of the 
						page.  Update kernel.vsd:kernel
						instead of updating this page.
7.3	kernel.util.vsd		kernel.util				
7.4	kernel.vsd		kernel
7.13	kernel.vsd		mutations

8.2	actor.vsd		ports
8.10    actor.util.vsd		actor.util
8.12	actor.vsd		director
8.17	actor.vsd		scheduler

9.1	data.vsd		token
9.3	data.expr.vsd		data.expr
9.4	data.expr.vsd		Parser
9.6	fixpoint.vsd		Logical View

10.1	graph.vsd		graph

11.4	data.type.vsd		token
11.5	actor.vsd		typed

13.12	uml/vergil.vsd		frames and tableaux

14.15   ct.vsd			ct.packages
14.16	ct.vsd			kernel.util
14.19	ct.vsd			solver
14.17   ct.vsd			Page-1
14.18	ct.vsd			director

15.4	de.vsd			domains.de.kernel
15.5	de.vsd			domains.de.lib

16.10	sdf.vsd			sdf.kernel

17.4	fsm.vsd			Logical View
17.10	???					"FSM kernel classes that
						 support modal models"

18.?    visio2000/giotto.vsd
18.5    ct.vsd			Logical View

19.5	ddeLocalTime.vsd	Logical View
19.6	ddeDeadlock.vsd		Logical View

20.1	pn.kernel.vsd		Logical View


Visio Hints
      * To resize the page or drawing area, hold down the control button
        and drag the edge.  You can also use File->Page Setup
      * Sometimes, when one copies the drawing the bottom line or
        the left most line is missing.  The workaround is to draw
	a very short line segment in the bottom right corner so that
	the very short line segment is missing instead of the bottom edge.
	I usually zoom up to 400% or 1600% to draw the shortest possible
	line segment
      