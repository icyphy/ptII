$Id$
This directory contains OCP installations that came from
https://www.citis.mdc.com

Distribution Warning -- Per direction from DARPA and AFRL, the
software and documentation downloaded from this web site shall be used
only by those researchers working on the SEC program.  Exceptions will
be handled on a case by case basis by DARPA or AFRL.

Currently, the ocp is checked in to the cvs repository on gigasource.
To check out a copy, you must have a cvs account on gigasource, see
http://www.gigascale.org/softdevel/faq/1/

The command to run is
cd $PTII/vendors/ocp
cvs -d :ext:gigasource.eecs.berkeley.edu:/home/cvs co ocp


To update the ocp repository, I used cvs's vendor or tracking third
party sources feature:
mkdir ocp
cd ocp
gtar -zxf /tmp/OCP_B0_2.tar.gz

cvs -d :ext:gigasource.eecs.berkeley.edu:/home/cvs import -m "Import of OCP_B0_2" ocp OCP OCP_B0_2
