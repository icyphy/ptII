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

To check out the OCP_B1_0 release, run these commands:

cd $PTII/vendors/ocp
cvs -d :ext:gigasource.eecs.berkeley.edu:/home/cvs co -r OCP_B1_0 ocp

To check out the older OCP_BO_2 version:
cvs -d :ext:gigasource.eecs.berkeley.edu:/home/cvs co -r OCP_B0_2 ocp


Nits about this code:
1) The tar file creates multiple files and directories in the current
directory. 
Most software releases create all their files and directories in a
single subdirectory so that the current directory does not
accidentally get files overwritten

The top level directory name should include the version of the release
(e.g. OCP_B1_0)

2) The website uses numeric account names, which are a pain to lookup
every time I need to access the website


Updating
--------

To update the ocp repository, I grabbed a tar file from the boeing
site and then I used cvs's vendor or tracking third party sources
feature:

mkdir ocp
cd ocp
gtar -zxf /tmp/OCP_B1_0.tar.gz

cvs -t -d :ext:gigasource.eecs.berkeley.edu:/home/cvs import -m "Import of OCP_B1_0" ocp OCP OCP_B1_0
