How to set up nesc in Eclipse so that you can run 
  $PTII/ptolemy/domains/ptinyos/util/create-nescdumpjar.sh

These instructions assume that you have checked out nesc from sourceforge:

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
