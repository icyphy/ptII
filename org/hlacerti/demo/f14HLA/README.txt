README of the F14 HLA PRISE demo

-- Author: Gilles Lasnier - ISAE SUPAERO - gilles.lasnier@gmail.com
           Janette Cadoso - ISAE SUPAERO - cardoso@isae.fr (updated on May, 9,2016;
	   July,25,2017)

-- $Id: README.txt 76454 2017-07-27 07:52:59Z cardoso@isae.fr $


DOCUMENTATION:
 
 - This folder contains two F14 HLA PRISE demos: 
   -- AllFederatesNER: all federates use NER as HLA time management.
   -- AllFederatesTAR: all federates use TAR as HLA time management.
   Open f14HLA.xml in this folder or the f14HlaNER.xml/f14HlaTAR.xml in the above folders.
	
   You can change the HLA time management in each federate separately. When using
   TAR, if the lookahead is bigger than the sampling time of periodicSamplers (federate
   Aircraft) the results are too different from the centralized model and can be unstable.

These demos work from revision 74766 to the current revision 76434 on July, 25, 2017.

 - The F14 HLA PRISE demo is based on the centralized PtolemyII simulation 
   (see ./initial-centralized-f14/f14_initial.xml based on a MATLAB demo f14).
   This demo shows how we can execute distributed and
   hardware-in-the-loop simulation using PtolemyII and the PRISE platform based
   on HLA/CERTI. The co-simulation framework PtolemyII - HLA/CERTI is used for
   this demo.

 - This demo may be executed using a Ptolemy federate as stick, or the PRISE
   stick C++ federate that is connected to a the PRO FLIGHT YOKE SYSTEM
   (hardware material from the PRISE platform).

REQUIREMENTS:

 - CERTI environment installed on your computer
 - PtolemyII framework installed on your computer
 - CMAKE build system tool, to compile the PRISE-Stick C++ federate (You need to
have the PRO FLIGHT YOKE SYSTEM (real stick) for running with the real joystick.)
 - C++ compiler, to compile the PRISE-Stick C++ federate
 - Up to revision 74794, use only lookahead > 0

EXECUTION:

1. Make sure you had sourced the script myCERTI_env.sh provided by CERTI
2. Run the command
   $PTII/bin/vergil $PTII/org/certi/demo/f14HLAr74766/f14HLAr74766.xml
3. Open each one of the three modes and run them in the order they appear.


