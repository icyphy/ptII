Janette Cardoso, August 2022

Same models as in TimeAdvancing2FederatesInternalEvents, but using only TAR. The goal is playing with 
Time Step and lookahead easily.

This is a Federation with a 2 federates: 
- f2 generates internal events (but does not send any UAV)
- f1 generates only stopTime, but has its time (HLA and PTII) constrained by f2.

Intructions for running the federation:

First of all set Ptolemy and CERTI environment variables as explained in the 
manual in $PTII/org/hlacerti/manual-ptii-hla.pdf.

Open the file Federation2federatesAdvancingTimeTAR.xml with the links to above models and
some explanation.
