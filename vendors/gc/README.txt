ptII/vendors/boehm/README.txt
$Id$

This directory contains the source code to the Boehm Garbage Collector
which is used by the C code generator.

The main page for the Garbage Collector is
http://www.hpl.hp.com/personal/Hans_Boehm/gc/

The source code itself came from
http://www.hpl.hp.com/personal/Hans_Boehm/gc/gc_source/gc.tar.gz

As of 1/05, we were using version 6.4.


If you have cvs access to the the Ptolemy tree, you can
download a modified copy of gc6.1 with:

cd $PTII/vendors/gc
cvs -d :ext:gigasource.eecs.berkeley.edu:/home/cvs co gc
cd gc
./configure --prefix=$PTII
make
make check
make install


Or, if you do not have cvs access to the Ptolemy tree, do

cd $PTII/vendors/gc

# Download version 6.4 from
# http://www.hpl.hp.com/personal/Hans_Boehm/gc/gc_source/

tar -zxf gc.tar.gz
cd gc6.4
./configure --prefix=$PTII
make
make check
make install


Note that under Windows, PTII cannot have a backslash in it or
else make will have problems.

