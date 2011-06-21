$Id$
To run these tests, you must have mosquitto installed, see 
ptserver/control/PtolemyServer.java

Under Mac OS X, the mosquitto binary is in
/usr/local/sbin/mosquitto, so you may need to add 
/usr/local/sbin to your path.

One way to do this is to edit ~/.MacOSX/environment.plist and
add /usr/local/bin

To temporarily add /usr/local/sbin to your path under bash:
export PATH=${PATH}:/usr/local/sbin


