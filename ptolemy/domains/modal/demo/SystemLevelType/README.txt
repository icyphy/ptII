This directory contains the interface automaton models for some Ptolemy II
components with respect to token passing. They are:

SDFActor.xml: An SDF sink actor. This actor does not call hasToken() before
              calling get().
PolyActor.xml: A Polymorphic actor. This actor calls hasToken() before
               calling get().
SDFDomain: The combination of SDF receiver and scheduler.
DEDomain: The combination of DE  receiver and scheduler.

All the other automata are the compositions of two of the automata above.
For example, SDFDomain_SDFActor.xml is the composition of SDFDomain.xml
and SDFActor.xml. These compositions are computed by the
InterfaceAutomaton class, but the Vergil layout is done manually.

