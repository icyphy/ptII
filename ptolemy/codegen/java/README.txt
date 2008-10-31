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




