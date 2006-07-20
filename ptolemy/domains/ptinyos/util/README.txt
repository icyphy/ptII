Warnings: Eclipse 3.0 might not work with CVS repositories in (or
linked in) ptII/vendors/ptinyos.  It deleted all of my CVS directories
in nesc and tinyos-1.x.  I have added a .cvsignore file to
ptII/vendors to see if we can avoid this problem in Eclipse 3.1.

I have symlinks in ptII/vendors/ptinyos for nesc and tinyos-1.x.

---------------------------------------------------------------------------
How to set up nesC in Eclipse 3.0 so that you can compile nesC Java
files from Eclipse:

These instructions assume that you have checked out nesc from sourceforge.

In $PTII/vendors/ptinyos
  cvs -d:ext:celaine@cvs.sourceforge.net:/cvsroot/nescc co nesc

1.  File | New | Project...
2.  Select a wizard: Java Project
    Click Next.
3.  Project name: nesc
    Select: Create project at external location
      Directory: /home/celaine/ptII/vendors/ptinyos/nesc
    Project layout: Use project folder as root for sources and class files
    Click Next
4.  Java Settings | Source Tab:
    Make sure "Allow output folders for source folders" is checked.
    Open nesc/tools/java
    Select "Output folder" and Edit...
    Source Folder Output Location: select "Specific output folder": tools/java
    Output folder should now be nesc/tools/java
    Click Finish
5.  Window | Show View | Navigator
    Right click on nesc project in Navigator pane on left hand side
    Build Project
6.  Class files should appear in nesc/tools/java/...

---------------------------------------------------------------------------
How to set up nesC in Eclipse 3.1 so that you can compile nesC Java
files from Eclipse:

These instructions assume that you have checked out nesc from sourceforge.

In $PTII/vendors/ptinyos
  cvs -d:ext:celaine@cvs.sourceforge.net:/cvsroot/nescc co nesc

1.  File | New | Project...
2.  Select a wizard: Java Project
    Click Next.
3.  Project name: nesc
    Select: Create project at external location
      Directory: /home/celaine/ptII/vendors/ptinyos/nesc
    Project layout: Use project folder as root for sources and class files
    Click Next
4.  Java Settings | Source Tab:
    Make sure "Allow output folders for source folders" is checked.
    Open nesc/tools/java
    Select "Output folder" and "Configure Output Folder Properties"
    Source Folder Output Location: select "Specific output folder": tools/java
    Output folder should now be nesc/tools/java
    Click Finish
5.  Window | Show View | Navigator
    Right click on nesc project in Navigator pane on left hand side
    Build Project
6.  Class files should appear in nesc/tools/java/...

---------------------------------------------------------------------------
To run or debug the java applications found in these subdirectories
under Eclipse, you must pass the following VM arguments:

  -Dorg.xml.sax.driver=org.apache.crimson.parser.XMLReaderImpl

You may substitute a different parser if desired.  See the SAX2 Driver
information here: 
  http://www.saxproject.org/quickstart.html

-----------------------------------------------------------------------------

The zip-opts.sh and unzip-opts.sh files are scripts to gather/extract
files from/to your tinyos-1.x tree with the necessary compiler options
that get parsed when nc2moml runs.  These option files (opts*) have to be
hand-updated to reflect needed include file paths.
