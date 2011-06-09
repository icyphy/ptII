$Id$
Interface to the Agilent P1000, which uses IEEE 1588.
For details, see
http://ptolemy.eecs.berkeley.edu/presentations/06/PtidesOnIEEE158806.ppt

To rebuild:
  autoreconf --install
  mkdir build
  cd build
  ../configure
  make

If you would like to install to a particular location use the --prefix
argument with configure:
  ../configure --prefix=/tmp/p1000
  make install

