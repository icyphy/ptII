$Id: README.txt 183 2018-03-28 13:07:35Z gl.lasnier $

This model shows the usage of the SynchronizeToRealTime attribute in an
PtolemyII HLA/CERTI cooperation.

TC => Time Constrained
TR => Time Regulator

In the PTII-HLA/CERTI framework a Federate is involed in the time advancement
phase if it is TC + TR.

The simulation contains 3 models:
- clockDisplayer.xml: a Federate (TC + TR) using NER, which receives (RAV)
updates of HLA attribute value 'clockValue' from the 'clock' object class. The
updated value are simply displayed to show the elapsed time.

- clockTransmitter.xml: a Federate (TC + TR) using NER, which publishes (UAV)
updates of HLA attribute value 'clockValue' from the 'clock' object class.
Updated values are published each two units of time (see DiscreteClock actor
specification from the model).

- clockHLA.xml: a Federate (TC + TR) using NER. The ClockHLA federate is the
creator of the synchronization point used during the initialization of the
simulation to synchronize all federates. This federate has to be launched
in last position (its firing actor has to be the last created).

During the execution of this simulation, you can remove the
synchronizeToRealTime attribute deployed in the ClockHLA federate
(see ClockHLA.xml model). This action will show the different behavior of the
federates and the evolution of the HLA logical time of the federation
when one federate is synchronize to its computer clock (i.e. the realtime) or
not.
