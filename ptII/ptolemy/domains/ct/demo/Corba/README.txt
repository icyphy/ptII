-------------------------README--------------------------
A simple CORBA demo

This directory contains a simple CORBA demo that uses the
Ptolemy II CORBA infrastructure in the ptolemy.actor.corba
package.

The demo models a second order continuous time system with
a nonlinear actor in the feedback loop. As shown bellow:

 +--------+                              +------+
 | Square |    +---+    +---+    +---+   | Non- |
 |  wave  |--->| + |--->| I |-+->| I |-->|linear|--+
 +--------+    +---+    +---+ |  +---+   +------+  |
                 ^            |                    |
                 |     +---+  |                    |
                 +-----| G |<-+                    |
                 |     +---+                       |
                 |               +---+             |
                 +---------------| G |<------------+
                                 +---+
where, Squarewave is a square wave input source,
       + is an add actor,
       I is an integrator,
       G is an scale actor, and
       Nonlinear is a nonlinear function.
The Nonlinear actor is a remote actor that implement the
ptolemy.actor.corba.util.CorbaActor interface.
(The interface is regenerated from CorbaActor.idl, and locates
at ptolemy.domains.ct.demo.Corba.util.CorbaActor) The
implementation of the interface is in NonlinearServant.java

The ModelServer is a server that starts the servant and wait
for client requests. The class is in ModelServer.java

A client can be either an applet or an application, implemented
in NonlinearClient.java and NonlinearClientApplication.java,
respectively.

You need JDK1.3 to run the demo.

To execute the model in the applet, please follow these steps:

0. If there are generated files in the ./util directory, do:
   prompt> idlj -td ../../../../.. -pkgPrefix util \
	ptolemy.domains.ct.demo.Corba -fall CorbaActor.idl

1. Compile the files, (which is probably done.)
	make

2. You can either run
	make demo_applet
   which will run the steps below for you, follow the steps below by hand

3. Select a port for the name service, say 1050
4. Start the java transient name server by:

   prompt> tnameserv -ORBInitialPort 1050 &

   Note: tnameserv is part of JDK.
   	 If the command line arguments are not provided,
   	 the default ORBInitialPort is 900.

5. Start the model server by:

   prompt> java -cp $PTII ptolemy.domains.ct.demo.Corba.ModelServer \
	-ORBInitialPort 1050 &

   where "$PTII" is replaced by the location of your Ptolemy II installation.

6. Start the client model using appletviewer:

   prompt> appletviewer NonlinearClient.htm

   The default parameter settings should work. If you change the
   ORBInitialPort, please specify it in the query box.

   Note: The JVM restricts an applet to access any network resource
   other than the one machine is from. So all the above programs
   should be running on the same machine.

To execute the model in an application:
1. Compile all the files on all the machines that are involved.
2. Select a port for the name service, say 1050
3. You can either run
	make demo_application
   which will run the steps below for you, follow the steps below by hand

4. Start the transient name server from some machine, say
   denon.eecs.berkeley.edu

   denon> tnameserv -ORBInitialPort 1050

5. Possibly from another machine, say bennett.eecs.berkeley.edu running csh
  start the model server. (Don't forget the CLASSPATH)

   bennett> setenv CLASSPATH=$PTII
   bennett> java ptolemy.domains.ct.demo.Corba.ModelServer -ORBInitialHost
   denon.eecs.berkeley.edu -ORBInitialPort 1050

6. Possibly from a third machine, say lie.eecs.berkeley.edu
   (a NT box running bash) start the client:

   lie> CLASSPATH=$PTII
   lie> java ptolemy.domains.ct.demo.Corba.NonlinearClientApplication \
	 -ORBInitialHost denon.eecs.berkeley.edu -ORBInitialPort 1050

To compare the result and the performance of CORBA, you may
run the demo in ptolemy/domains/ct/demo/SquareWave (with the
default parameter settings). That is essentially the same model
with out the nonlinear block. Please note the speed drop of the
CORBA demo is due to two factors: the network communication and
the model itself. The nonlinearity of the model makes the step
size much smaller comparing with the linear case.

---------------------------------------------------------------------
