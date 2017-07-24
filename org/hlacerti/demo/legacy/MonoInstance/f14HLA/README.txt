J. Cardoso, July, 24, 2017:
This simulation is very slow!
--------------------------

README of the F14 HLA demo

-- Author: Gilles Lasnier - SUPAERO ISAE - gilles.lasnier@gmail.com

-- $Id: README.txt 70349 2014-10-12 22:45:57Z cxh $


DOCUMENTATION:
 
 - This folder contains the F14 HLA demo. This demo is based on the one provided
   by MATLAB. This demo shows how we can distribute a PtolemyII simulation using
   HLA/CERTI (see ./initial-centralized-f14/f14_initial.xml). 
   The co-simulation framework PtolemyII - HLA/CERTI is used for this demo.

REQUIREMENTS:

 - CERTI environment installed on your computer
 - PtolemyII framework installed on your computer

EXECUTION:

 1. Open a terminal, source the script myCERTI_env.sh provided by CERTI
 2. Launch the Eclipse configured for PtolemyII
 3. Launch VERGIL, from Eclipse if you have installed from the svn
 4. From VERGIL (Ptolemy's graphical editor) opens the model f14HLA.xml, then
    follow the instructions to open the different Ptolemy federate models
 5. Run each Ptolemy federates with respect to the order given in f14HLA.xml