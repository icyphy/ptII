$Id$
This directory contains targets for code generation.

How to add a new target
-----------------------
The target is selected by the target parameter in the
ptolemy.codegen.kernel.CodeGenerator class.

If the target parameter is set to something other than "default",
then during code generation at the top level a helper class for
the target is instantiated.  For example, if the target is 
"arduino", then the helper class would be
ptolemy.codegen.c.targets.arduino.ArduinoTarget.

The target class has a C file associated with it, for example
$PTII/ptolemy/codegen/c/targets/arduino/ArduinoTarget.c.
That file contains code blocks that are used during code generation.

Tasks:
1) Create a directory for the new target
   cd $PTII/ptolemy/codegen/c/targets
   mkdir arduino

2) Create an associated target file.  In this case, the java file
   just calls super:
   ptolemy/codegen/c/targets/arduino/ArduinoTarget.java contains:

    public ArduinoTarget(ptolemy.actor.TypedCompositeActor actor) {
        super(actor);
    } 

4) Create a .c file that has the following optional sections
/*** sharedBlock ***/
// To be included at the top 
#include <WProgram.h>
/**/

5) Create a file name "makefile.in" in the target directory.
   This file is used during compilation of the generated code.
   It is easiest to start with 
     ptolemy/codegen/c/makefile.in

6) Create a small test model, drag in a code generator, set the
   target and generate code.

