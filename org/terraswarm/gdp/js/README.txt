org/terraswarm/gdp/js/README.txt
$Id$

This directory contains a JavaScript interface to the Global Data Plane.

To run a simple reader demo:

1. Get access to the gdp repo from Eric.

2. Install the gdp:
   cd $PTII/org/terraswarm/gdp
   make src/gdp
   cd src/gdp
   make 
   
   If the installation does not work, see the gdp README file at
   $PTII/org/terraswarm/gdp/src/gdp/README

   For Macs, install MacPorts from https://www.macports.org/ and then do

   sudo -i port install libevent
   sudo -i port install jansson

   The re-run:

   cd $PTII/org/terraswarm/gdp/src/gdp
   make

   Success is defined as having shared libraries in the libs/ directory, for example:

   bash-3.2$ ls  $PTII/org/terraswarm/gdp/src/gdp/libs
   libep.2.0.dylib	libep.so.2.0		libgdp.so.1
   libep.so		libgdp.1.0.dylib	libgdp.so.1.0
   libep.so.2		libgdp.so

3. Start up the gdp daemon:

   gdpd/gdpd &

4. Install Node.js from http://nodejs.org/   

5. cd to this directory and run 'make run':

   cd $PTII/org/terraswarm/gdp/js/
   make run

   This will do the following
   a) Copy files from gcl/ to /var/tmp/gcl
   b) Use the node package manager (npm) to install the ffi, ref, and ref-array packages.
      This installation occurs in the current directory and occurs only once.
   c) Invoke node on simpleReader.js

See also
http://www.terraswarm.org/swarmos/wiki/Main/GDPJavaScriptInterface


