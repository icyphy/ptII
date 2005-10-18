$Id$

This directory contains the ECSL_DP release from Vanderbilt.

10/18/2005 - How to build!
1. Download UDM from
   http://escher.isis.vanderbilt.edu/downloads?tool=UDM
   We used UDM-2.20
   http://escher.isis.vanderbilt.edu/downloads/adown?tool=UDM/2.20/UdmBinary-2.20_setup.exe

2. You will need to replace
 c:/Program Files/ISIS/UDM/3rdparty/antlr/antlr-2.7.1/antlr_D.lib with
   antlr/antlr-2.7.1/lib/antlr_D.lib

cp antlr/antlr-2.7.1/lib/antlr_D.lib c:/Program\ Files/ISIS/UDM/3rdparty/antlr/antlr-2.7.1/lib/

3. In Microsoft Visual Studio 6, open up MDL2XML/src/MDL2XML.dsw
   and compile.

4. In Microsoft Visual Studio 6, open up XML2ECSL/src/Xml2Ecsl.dsw
   and compile.

5. cd to ptolemy and run 'make demo'


on 8/19, Attila Vizhanyo <viza@isis.vanderbilt.edu> wrote: 

--start
  I am Attila Vizhanyo and I am in charge of maintaining the XML2ECSL
  translator. I have redesigned heavily the translator recently, so the
  translator is not yet stable, we have some known bugs, it is not
  thoroughly tested, etc..

  Please find the ECSL_DP.zip in attachment.

  You can find the translator files under the Xml2Ecsl folder. I included
  both the binaries and the sources for you. In each folder you'll find a
  readme.txt explaining which file does what. Two things are worth
  mentioning here:
  (1) if you want to execute the translator, there is an example input
  model for the translator available. The output of the translator is a
  GME model, thus you have to register the ECSL_DP paradigm in GME prior
  to executing the translator.=20
  (2) if you want to build the translator sources, you need to have UDM
  installed on your machine. You can download UDM from:
	http://www.isis.vanderbilt.edu/projects/mobies/downloads.asp

  I understand that you need some way of description of the ECSLDP
  metamodel.
  I put the ECSL DP files under the ECSL_DP folder.

  Please let me know if you have any questions, or problems.
--end--

10/19
You need GME 4.8.25

XML2ECSL users might find these documents useful:

Sandeep Neema, Gabor Karsai, Embedded Control Systems Language for Distributed Processing(ECSL-DP) ISIS-04-505
http://www.isis.vanderbilt.edu/publications/archive/Neema_S_5_12_2004_Embedded_C.pdf

Mobies assesment of tools, includes a description of ECSL
http://vehicle.me.berkeley.edu/mobies/interaction/IntegratedBerkeleyCommentsv2.1.doc

ECSL (Embedded Control System Modeling Language) can be found at
http://www.isis.vanderbilt.edu/projects/mobies/downloads.asp
That download includes MDL2XML 
