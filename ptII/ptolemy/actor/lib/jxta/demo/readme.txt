-------------------------README--------------------------
A simple CORBA + JXTA demo

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


To execute the model in an application:
1. Compile all the files on all the machines that are involved.
2. under ~/ptII/ptolemy/actor/lib/jxta/demo> run
        make Server
   which will run the steps below for you:
    java -classpath "../../lib/jxta.jar;../
../lib/log4j.jar;../../lib/beepcore.jar;../../lib/jxtasecurity.jar;../../lib/cr
yptix-asn1.jar;../../lib/cryptix32.jar;../../lib/jxtaptls.jar;../../lib/minimal
BC.jar;.;c:/cygwin/home/ellen_zh/ptII/" ptolemy.actor.lib.jxta.demo.corba.Model
Server

4.Possibly from another machine, say bennett.eecs.berkeley.edu,
under ~/ptII/ptolemy/actor/lib/jxta/demo> run
vergil corba/jxtaNonlinearClient.xml



To compare the result and the performance of CORBA, you may
run the demo in ptolemy/domains/ct/demo/SquareWave (with the
default parameter settings). That is essentially the same model
with out the nonlinear block. Please note the speed drop of the
CORBA demo is due to two factors: the network communication and
the model itself. The nonlinearity of the model makes the step
size much smaller comparing with the linear case.

---------------------------------------------------------------------
