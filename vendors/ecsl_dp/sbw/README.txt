1/05: Attila Vizhanyo writes:
  Please find the SBW example in Matlab Simulink and ECSL-DP format.
  I also enclosed the ECSL-DP metamodel, please make sure you import and
  register this paradigm into GME prior to opening the SBW model.
  If you have any questions, please let me know.

Install GME 4.11.10.exe
Start GME
Import the model (metamodel?):
  File -> Import XML, Browse to ECSL_DP-meta.xme
  Import to new Project: "Create project file" is ok, hit next
  Say open on the Stupid blank dialog
  Get a message "The XML file was successfully imported."

Generate the modeling lang/paradigm:
  Click on the little gear button or Open up a model and ...
  Save ECSL_DP.xmp and then keep clicking ok
  Ignore error messages about Dataflow and Stateflow

Import SBW model
  File -> Close project (or exit GME)
  File -> Import XML -> choose sbw_edp.xme, Next
  Click ok to bogus SBW dialog
  Click ok to "Could not locate paradigm" window
  Get a message "The XML file was successfully imported."
  Now we can look at the model in the r.h. tree
         SBW -> ComponentModels -> ComponentSheet
    should look like the Matlab model.


------
Install UDM 2.21
  from http://escher.isis.vanderbilt.edu/downloads/
  Installs in c:/Program Files/Isis

------
Attila Vizhanyo writes:
>  Here is how to convert the SBW mga file into an xml file.
>  
>  1. Open the ECSL-DP metamodel in GME, and run the MetaGme2Uml
>  interpreter to get an ECSL-DP UML class diagram.

Follow the instructions for "Import the model (metamodel?)" above
Follow the instructions for "Generate the modeling lang/paradigm" above

File -> Import XML, Browse to ECSL_DP-meta.xme
In GME, click on the uml button to the left of the X
The tooltip says:  "Converts GME metamodel to equivalent UML Class diagram"

I get "MetaGME2Uml Com Exception: The paradigm is not registered"

The message comes from
c:/Program Files/GME/Interfaces/Mga.idl:
[helpstring("The paradigm is not registered")]
		E_MGA_PARADIGM_NOTREG			= 0x87650011,


BTW - MetaGme2Uml is part of GREAT
http://www.isis.vanderbilt.edu/Projects/Mobies/downloads.asp?req=Downloads/ESCM-1.3R.pdf

bash-2.05b$ c:/Program\ Files/isis/great/bin/MetaGME2UML ECSL_DP.mga ECSL_DP_uml.mga
MetaGME to UML Converter Utility v1.3.3
(c) 2004 Institute for Integrated Systems
Vanderbilt University

Exception occured: Com exception: The paradigm is not registered

bash-2.05b$ 

This command works:
c:/Program\ Files/isis/great/bin/MetaGME2UML ECSL_DP.mga ECSL_DP_uml.mem

>  2. After this, re-register your ECSL-DP metamodel, because the
>  MetaGme2Uml interpreter might changed some role name registry settings
>  in your metamodel.
>          (The interpreter is quite rudimentary, it always displays this
>  message, even if re-registration is not necessary)
>  3. Open the ECSL-DP UML class diagram and run the UML2XML interpreter,
>  which will produce an ECSL-DP.xml file.
>  4. Run the UDM code generator, udm.exe, on this ECSL-DP.xml file. You'll
>  need the ECSL-DP.xsd file to perform the conversion below.
>  5. Use the generated ECSL-DP.xml file to specify the <diagramname>
>  argument(!) of udmcopy:
>           udmcopy.exe sbw.mga sbw.xml ECSL-DP.xml
  (Make sure ECSL-DP.xsd is in PATH.)




