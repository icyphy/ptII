fmusdk

http://www.functional-mockup-interface.org/fmi.html says:

"The FMU SDK is a free software development kit provided by QTronic to
demonstrate basic use of Functional Mockup Units (FMUs) as defined by
"FMI for Model Exchange 1.0". The FMU SDK can also serve as starting
point for developing applications that export or import FMUs."

This is a port of the Windows fmusdk 1.0.2 from to Mac OS X and Linux.

The original Windows fmusdk sources were download from
http://www.qtronic.de/doc/fmusdk.zip

This port includes Linux changes by Michael Tiller for fmusdk1.0,
see http://github.com/mtiller/fmusdk

The Mac OS X/Linux port to fmusdk1.0.2 was done by Christopher Brooks,
see https://github.com/cxbrooks/fmusdk


Installation
------------

fmusdk requires a zip binary.  The sources are configured to use 7z.

To install 7z:

1. Go to http://leifertin.info/app/eZ7z/ and install ez7z.
2. Copy the 7za binary to /usr/local/bin/7z:
   sudo cp /Volumes/Ez7z\ 2.12/Ez7z.app/Contents/*/7za /usr/local/bin/7z

To build fmusdk:

1. git clone https://github.com/cxbrooks/fmusdk.git
2. cd fmusdk/src
3. make
4. cd ..

To run:
1. ./fmusim me fmu/me/bouncingBall.fmu 5 0.1 0 c





