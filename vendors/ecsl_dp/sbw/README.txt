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

I get "MetaGME2Uml Com Exception: The paradign is not registered"

BTW - MetaGme2Uml is part of GREAT
http://www.isis.vanderbilt.edu/Projects/Mobies/downloads.asp?req=Downloads/ESCM-1.3R.pdf

  2. After this, re-register your ECSL-DP metamodel, because the
  MetaGme2Uml interpreter might changed some role name registry settings
  in your metamodel.
          (The interpreter is quite rudimentary, it always displays this
  message, even if re-registration is not necessary)
  3. Open the ECSL-DP UML class diagram and run the UML2XML interpreter,
  which will produce an ECSL-DP.xml file.
  4. Run the UDM code generator, udm.exe, on this ECSL-DP.xml file. You'll
  need the ECSL-DP.xsd file to perform the conversion below.
  5. Use the generated ECSL-DP.xml file to specify the <diagramname>
  argument(!) of udmcopy:
           udmcopy.exe sbw.mga sbw.xml ECSL-DP.xml
  (Make sure ECSL-DP.xsd is in PATH.)




