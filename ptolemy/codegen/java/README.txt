ptolemy/codegen/java/README.txt
$Id$

Description of Ptolemy Java Codegeneration

To choose the Java code generator, invoke
ptolemy.codegen.kernel.CodeGenerator
with the "-generatePackage ptolemy.codegen.java" arguments.

For example, to run ptcg:

$PTII/bin/ptcg -generatorPackage ptolemy.codegen.java $PTII/ptolemy/codegen/c/actor/lib/test/auto/Ramp.xml  

Design issues:

* This code was copied from c codegen, so there is plenty of
duplication.  The problem is that the C code generator code tends to
extend CCodeGenerator, so we can't do a simple subclass.

* For C code generation, the actors have a .java file that defines the
helper a .c file that contains the stubs that are included.  This
won't work for Java codegen, so we use a .j file for the stubs.  Since
the stub files don't compile, we should not call them .java files or
else Eclipse and other programs will try to compile them.

* A simple preprocessor similar to C's cpp was added to
JavaCodeGenerator.java.
This preprocessor looks for lines like
  #define foo
  #ifdef foo
  #endif
The preprocessor is very simple, either the variable is present or
not, the variable does not have values.  Combining variables
is not supported by #ifdef.  

The reason a preprocessor was included is so that the .j files
for the various types such as Array.j, Integer.j and Double.j
can conditionally include sections for the types that are present
in the generated code.

* JavaParseTreeCodegenerator.java (and CParseTreeCodeGenerator.java)
include duplicated code from JavaCodeGeneratorHelper.
The reason is that JavaParseTreeCodegenerator has no notion of
which helper is being used, yet types like Integer vs int are
different between Java and C.

* The code generator could be extended to use Ptolemy II Token types,
which have the various operations already defined.  However,
this would mean that various parts of Ptolemy such as the 
data.expr, graph and the unit system would also be included.
Instead, we ported the C codegen type operations to Java codegen.


