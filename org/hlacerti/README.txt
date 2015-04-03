-- README for Ptolemy II - HLA/CERTI cooperation
-- @version $Id$
-- @Author: Gilles Lasnier (gilles.lasnier@isae.fr)

####################
## Purpose:

This directory contains a Ptolemy II development environment to allow 
"co-simulation" between a HLA/CERTI Federation and Ptolemy DE simulation 
models.

HLA (High-Level Architecture) is a general purpose architecture for distributed 
computer simulation systems. In HLA systems, the RTI (RunTime Infrastructure) 
manages data exchange between simulations. CERTI is an Open Source HLA RTI.
JCERTI is a Java CERTI bindings. 

This environment integrates a Ptolemy model as a Federate of the 
HLA/CERTI Federation. Three kinds of "co-simulation" are possible:
- 1. one full-Federate in Ptolemy involved in the Federation
- 2. one simple Federate in Ptolemy which models the functionnal 
     part of a Federate. The skeleton part of the Federate is another
     Federate that could be implemented in C++, Java, Python or Ptolemy II.
- 3. one full-Federate in Ptolemy is used to model the network
     of a HLA/CERTI Federation.
     
The demo directory provides some examples of Ptolemy II models
based on co-simulation strategies described above.

####################
## Install:

To install the software CERTI (and JCERTI), follow the instructions here:

  http://savannah.nongnu.org/cvs/?group=certi

To execute a Ptolemy II demo, open each .xml files in a subdirectory of the
ptolemy/apps/hlaCerti/demo/ directory.

- You can launch the RTIG process manually, by running the command "rtig" 
in a terminal with the CERTI environment set. Be careful, you have to
be in the directory of the demo where is the .fed file.

- You can also let a Ptolemy model to launch the RTIG process automatically.
In this case put the parameter "CERTI_HOME=/my/certi/env/path" in the model
or in the HlaManager attribute deployed the model.

- Then, launch the simulation. Be careful, the model which contains the
HlaManager that indicates that it is the creator of the synchronization point 
has to be run in last. The first model that is launched is the one creating 
the subprocess to launch the RTIG.

####################
## Documentation:

Overall information about the Ptolemy II - HLA/CERTI cooperation can be
found here:

  GL: FIXME: TODO technical report

Reference documentation that may be consulted:
 -   GL: FIXME: TODO Publication RTCSA'2013 or DS-RT'2013
 -   GL: FIXME: TODO Add Papier Siron
 -   GL: FIXME: HLA Standard

####################
## Others:

The current implementation is composed of:
- an attribute (in the Ptolemy world), the HlaManager which manages interaction 
  with the HLA/CERTI tool
- two actors, HlaPublisher and HlaSubscriber which allows to publish/subscribe to
  a HLA attribute (in the HLA taxonomy) in a Federation
- one java class CertiRtig which handles the launch of the CERTI/RTIG process
  from Ptolemy model
For more information see:

   ptolemy/apps/hlaCerti/lib/*.java

These entities are deployed in the Vergil library in the "Co-Simulation" 
directory, for more information see:

   ptolemy/apps/hlaCerti/lib/hlaentities.xml
   
and,

   ptolemy/configs/cosimumation.xml.