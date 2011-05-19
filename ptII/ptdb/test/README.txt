$Id$

This directory uses PowerMock from http://code.google.com/p/powermock/


To setup Ptolemy to use PowerMock:

cd /tmp
 wget http://powermock.googlecode.com/files/powermock-easymock-junit-1.3.8.zip
 cd $PTII/vendors/misc
 unzip /tmp/powermock-easymock-junit-1.3.8.zip
 cd $PTII
 ./configure

To run a test from the command line:
 cd $PTII/ptdb/test
 make


To run a test from Eclipse:
 Refresh Eclipse (F5), browse to ptdb/test, right click on TestClassA.java and select Run As | JUnit test.
 

 
