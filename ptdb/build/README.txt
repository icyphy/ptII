$Id$
ptdb test harness build directory

To run the tests:

1. Download Cobertura from http://cobertura.sourceforge.net/download.html
2. Untar in $PTII/vendors/misc
   cd $PTII/vendors/misc
   tar -zxf /tmp/cobertura-1.9.4.1-bin.tar.gz 

3. Update build.properties to point to Cobertura.  
   This step is only necessary if the version of Cobertura changes

4. Run the tests
   cd $PTII/ptdb/build
   ant

5. The test results will be in reports/junit-html/index.html

