ptII/doc/uml/README.txt
Version: $Id$

This directory contains Visio 5 uml diagrams.
We are using Visio 5, not Visio 2000.
Visio 2000 is not backward compatible, it is possible to view
Visio 5 uml diagrams, but not update them.


The contents of this directory are not shipped with the release.

We convert the uml files to wmf files and import them by reference
into the Framemaker files in ../design/src

The easiest way to update the Visio files is to:
1. Print out the Visio file
2. Look at the javadoc output for the appropriate classes.
   In this way, we also proof the javadoc output

   * Don't show overridden methods.  If a child class overrides foo(),
     then foo() need not be included in the child class 
 
   * If you use a different package, then you need not include all the
     methods in that package.  The package should be have dotted lines.
 
   * Private variables: Don't include implementation details.  Include
     only important functional details.  NamedObj has a private string
     that is the name, it should be included.

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
      the letters do not get squashed
   


Figure	UML file		Page within that file
1.2	packages.uml		packages
1.3	packages.uml		domains
1.5	uml.uml			summary

3.1	actor.lib.uml		actor.lib
3.2     actor.lib.gui.uml
3.3	??? Token class
3.6	actor.lib.uml		Sources
3.7	actor.lib.uml		Sinks