$Id$
The files in this directory came from 

Distribution Warning -- Per direction from DARPA and AFRL, the
software and documentation downloaded from this web site shall be used
only by those researchers working on the SEC program.  Exceptions will
be handled on a case by case basis by DARPA or AFRL.


These files are checked into the CVS repository
README.txt - this file
Build 0_1 Building and Installing.htm - Installation instructions

These files are not checked in, you can find them in
~ptII/vendors/ocp/OCP_B0.1
AlphaOCP.tar.gz	  - 10 Mb tar file version

Note that if you untar the tar file, be sure to create an empty
directory first, as the tar file will create multiple files in the
current directory when it is untar'd

You could do something like:
cd $PTII/vendors/ocp/OCP_B0.1
scp carson:~ptII/vendors/ocp/OCP_B0.1/AlphaOCP.tar.gz .
mkdir OCP
cd OCP
gtar -zxf ../AlphaOCP.tar.gz .

If you don't have gtar, then try using tar

