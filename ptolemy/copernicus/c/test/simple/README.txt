
@author Ankush Varma
@version $Id$ 

A simple example to test C code generation from a ptolemy II model.


Command to run the c code generator on a model:

java ptolemy.copernicus.kernel.Copernicus -codeGenerator \"c\" simple.xml

Known Issue: The code generation causes 4 new direcories to be created in ptII:
java, javac, sun and org. These directories contain various java classfiles.
They are not created by the C code generator itself, but they seem to be
created when its called through ptolemy. These directories cause code
generation to recurse forever in soot.FastHierarchy.dfsvisit(). Removing the
directories eleminates the error. 


