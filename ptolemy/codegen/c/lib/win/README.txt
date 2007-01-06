$Id$

libjvm.dll.a is an import library file needed when using gcc to
compile C code which calls JNI functions.

The reason is: 

"Sun's distribution of the Java Development Kit includes a file in the
lib/ directory called jvm.lib. This is a Win32 static library against
which compilers like VC++ can link. However, gcc can't handle the .lib
file (as far as I know), so we need to create a UNIX-y import library
(a .a file) against which we can link. "

I found this from 
http://www.inonit.com/cygwin/jni/invocationApi/archive.html
You can find more information there.

To create this,
1) first create a file named jvm.def with the following content:
EXPORTS
JNI_CreateJavaVM@12
JNI_GetDefaultJavaVMInitArgs@4
JNI_GetCreatedJavaVMs@12

2)run the command:
dlltool --input-def jvm.def --kill-at --dllname jvm.dll --output-lib libjvm.dll.a

This is specific to cygwin. 
