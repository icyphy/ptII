$Id$
ptdb test harness build directory

To run the tests:

1. Download Cobertura from http://cobertura.sourceforge.net/download.html
2. Untar in $PTII/vendors/misc
     cd $PTII/vendors/misc
     tar -zxf /tmp/cobertura-2.0.3-bin.tar.gz 

3. Install Oracle Berkeley XML DB, see
   $PTII/ptdb/doc/OracleBerkeleyXMLDBMacOSXBuildInstructions.htm

4. Rerun configure so that the Oracle Berkeley XML DB shared libraries
   are found.
     cd $PTII
     ./configure

5. Update build.properties to point to Cobertura.  
   This step is only necessary if the version of Cobertura changes

6. Run the tests
     cd $PTII/ptdb/build
     ant

7. The test results will be in reports/junit-html/index.html

