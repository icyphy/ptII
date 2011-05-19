This package contains a preliminary definition of CorbaActor.idl.
The java classes that are generated automatically from this IDL
is in the ./util directory.

In normal cased, you don't have to re-generator the java classes.
If you do want to re-generate, use the following command.
(You need JDK1.3 to do it)
prompt>  idlj -td ../../.. -pkgPrefix util ptolemy.actor.corba
-fall CorbaActor.idl

idlj exists in your JDK1.3/bin directory.
