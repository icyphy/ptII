ptII/vendors/misc/x10/README.txt
$Id$

This directory is for interface libraries for x10.
To download them, go to:
   http://www.agaveblue.org/projects/x10/download.html
and download tjx10p-13.zip. (Version 1.3) Unzip this file 
in this directory ($PTII/vendors/misc/x10/)
and rerun configure.

The configure script looks for the library at
$PTII/vendors/misc/x10/tjx10p-13/lib/x10.jar
and
$PTII/vendors/misc/x10/tjx10p-12/lib/x10.jar
and
$PTII/vendors/misc/x10/tjx10p-11/lib/x10.jar

Note that this library also requires javax.comm be installed to work properly.
javax.comm can be found at
http://java.sun.com/products/javacomm/
