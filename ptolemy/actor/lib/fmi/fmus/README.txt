ptolemy/actor/lib/fmi/fmus/README.txt
$Id$

This directory contains a the source code for Functional Mockup Units
(FMUs) used by Ptolemy II.

An FMU is a zip file that contains a modelDescription.xml file and one
or more platform-dependent library files.


File Locations
--------------
In general, the .fmu file and the shared libraries are *not* checked
in to version control.  Instead, the .fmu file is checked in at
$PTII/ptolemy/actor/lib/fmi/test/auto/.

To update the .fmu file, cd into the fmu directory and run 'make update'.
This will create the .fmu file and *merge* it in with the .fmu file in
$PTII/ptolemy/actor/lib/fmi/test/auto/.

Tests are also checked into $PTII/ptolemy/actor/lib/fmi/test/auto/.

Demos are checked in to subdirectories of $PTII/ptolemy/actor/lib/fmi/demo/.

The reason that .fmu files in the $PTII/ptolemy/actor/lib/fmus/*
directories are not checked in is because if a user runs make at the
top level, these .fmu files will be created and will typically differ
from .fmu files created by other users.  Then, if the user does a svn
commit, the version control system will note the difference and check
in the change, which is unnecessary.  Checking in these .fmu files is
like checking in .o files.

We have a 'make update' rule, which intentionally updates the .fmu in
test/auto, which the user invokes to signify that they *intend* to
update the fmu and commit it.  This is like checking a library or jar
file.

Note that if you change the .c files associated with the FMU, then
you should probably remove associated .fmu file in the auto directory
so that when a model imports the .fmu on a different platform it will
build the shared library and reflect your changes.  In other words, 
if you change fmus/inc20pt/src/sources/inc20pt.c, do

rm $PTII/ptolemy/actor/lib/fmi/test/auto/inc20pt.fmu
cd $PTII/ptolemy/actor/lib/fmi/fmus/inc20pt
rm *.fmu
make update
svn commit -m "Updating fmu after inc20pt.c was modified to xxx and yyy." $PTII/ptolemy/actor/lib/fmi/test/auto/inc20pt.fmu


Naming conventions
------------------
Note that fmu files, tests and demos should all follow the Ptolemy II
naming convention.  Specifically, camelCase should be used:

Right:
stairStep.fmu
stairStep.xml

Wrong:
stair-step.fmu  
stair_step.xml

Currently, demo names seem to start with "FMU", it would be good to stick with this.

Also, FMI-1.0 FMUS and models typically end with a "1": dqME1.fmu is a FMI-1.0 FMU.


Creating a new FMU
------------------
The high level steps are:
* Create the fmu 
* Create a demo that uses it
* Create a test

1) Create the fmu

2) The easiest thing to do is to copy the files from an existing fmu and
change them.  

Also $PTII/ptolemy/actor/lib/fmi/fmus/template/mkfmudir is a script that
will build a FMI-2.0 Co-Simulation directory.

The remaining steps outline what is done to create an fmu directory by hand.

3) The .c file that defines the fmu-specific methods of your FMU should
be renamed to match the name of the FMU.  For example
ptolemy/actor/lib/fmi/fmu/stairsA/ has 
ptolemy/actor/lib/fmi/fmus/stairsA/src/sources/stairsA.c

4) For each FMU, be sure to get a new guid from http://guid.us
 - change guid in the modelDescription.xml file
 - and in the .c file that defines fmu-specific methods of
   your FMU. (stairsA.c)

5) For FMI 1.0, update the MODEL_IDENTIFIER and FMIAPI_FUNCTION_PREFIX values in the 
.c file:
--start--
// The model identifier string.
#define MODEL_IDENTIFIER stairsA

// Used by FMI < 2.0.  See fmiFunctions.h
#define FMIAPI_FUNCTION_PREFIX stairsA_
--end--

For FMI 2.0, update FMI_FUNCTION_PREFIX in the .c file. 
--start--
// Unfortunately this file will compile to different symbols if
// compiled in a static link library or compiled as a dll.
// See fmiFunctions.h
#ifdef FMI_STATIC_OR_C_FILE  // FMI_STATIC_OR_C_FILE is a Ptolemy-specific extension.
#define FMI_FUNCTION_PREFIX helloWorldME2_
#endif
--end--

(Note: Check that the guid was updated in the .c file, see step 5 above.)


6) Update FMU_NAME in fmi/fmus/stairsA/src/sources/makefile
   and in fmi/fmus/stairsA/makefile

7) cd to the top level directory of the fmu and run make update
  cd $PTII/ptolemy/actor/lib/fmi/fmus/stairsA
  make update
Your .fmu file should appear $PTII/ptolemy/actor/lib/fmi/fmus/test/auto/stairsA.fmu

8) Create a demo directory and a demo model with a name that matches
the directory name.  For example, the demo directory is 
ptolemy/actor/lib/fmi/demo/FMUStairs so the demo is
ptolemy/actor/lib/fmi/demo/FMUStairs/FMUStairs.xml

Each demo directory *must* have a model that matches the name so that
users know where to start.

9) Copy a makefile from an adjacent demo directory and update it.

10) Add the demo to $PTII/ptolemy/configs/doc/completeDemos.htm Each
demo has a top level director, for FMUs, it is typically the
Continuous director.  Thus, FMU models typically go into the
continuous section of completeDemos.htm.

11) Add a test to $PTII/ptolemy/actor/lib/fmi/test/auto/.  Typically,
the name of the test matches the name of the demo.  The test has any
plotter actor replaced with Test actors.


Checklist before committing
---------------------------

_ The name of the directory and the name of the .c file should match.

_ Make sure that you are not committing the platform-specific
directory in binaries.  For example, src/binaries/darwin64

_ *Do not* check in the .fmu file as
ptolemy/actor/lib/fmi/fmus/xxx/*.fmu.  Instead, the .fmu file is
checked in to $PTII/ptolemy/actor/lib/fmi/test/auto/ and updated by
running "make update".

_ Add a test to ptolemy/actor/lib/fmi/test/auto/.  Ideally, the name
of the test has "FMU" as a prefix and then the name of the fmu.

_ Add the new FMU to ptolemy/actor/lib/fmi/fmus/makefile in the either
FMU_CS or FMUS_ME. (Hint: check the modelDescription.xml file).

_ Add the new FMU to ptolemy/actor/lib/fmi/fmus/makefile to
PTCLASSALLJARS so that we jar up the sources for use in the
installers.

---End---
