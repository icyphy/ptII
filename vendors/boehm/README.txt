ptII/vendors/boehm/README.txt
$Id$

This directory contains the source code to the Boehm Garbage Collector
which is used by the C code generator.

The main page for the Garbage Collector is
http://www.hpl.hp.com/personal/Hans_Boehm/gc/

The source code itself came from
http://www.hpl.hp.com/personal/Hans_Boehm/gc/gc_source/gc.tar.gz

As of 4/03, we were using version 6.1

To install, do
 
cd $PTII/vendors/boehm
# Download
http://www.hpl.hp.com/personal/Hans_Boehm/gc/gc_source/
tar -zxf gc.tar.gz
cd gc6.1
./configure --prefix=$PTII
make
make check
make install
