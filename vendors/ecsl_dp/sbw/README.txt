$Id$

1/05: Attila Vizhanyo writes:
>  Please find the SBW example in Matlab Simulink and ECSL-DP format.
>  I also enclosed the ECSL-DP metamodel, please make sure you import and
>  register this paradigm into GME prior to opening the SBW model.
>  If you have any questions, please let me know.


1. Install GME 4.11.10.exe
2. Start GME
3. Import the model (metamodel?):
  File -> Import XML, Browse to ECSL_DP-meta.xme
  Import to new Project: "Create project file" is ok, hit next
  Say open on the Stupid blank dialog
  Get a message "The XML file was successfully imported."

4. Generate the modeling lang/paradigm:
  Click on the little gear button or Open up a model and ...
  Save ECSL_DP.xmp and then keep clicking ok
  Ignore error messages about Dataflow and Stateflow

5. Import SBW model
  File -> Close project (or exit GME)
  File -> Import XML -> choose sbw_edp.xme, Next
  Click ok to bogus SBW dialog
  Click ok to "Could not locate paradigm" window
  Get a message "The XML file was successfully imported."
  Now we can look at the model in the r.h. tree
         SBW -> ComponentModels -> ComponentSheet
    should look like the Matlab model.

6. Install UDM 2.21
  from http://escher.isis.vanderbilt.edu/downloads/
  Installs in c:/Program Files/Isis


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

The solution is to uninstall GReAT, UDM and GME,
trash the registry entries (Search for GME) and reinstall
Then, click on the gear 

BTW This command works:
c:/Program\ Files/isis/great/bin/MetaGME2UML ECSL_DP.mga ECSL_DP_uml.mem

>  2. After this, re-register your ECSL-DP metamodel, because the
>  MetaGme2Uml interpreter might changed some role name registry settings
>  in your metamodel.
>          (The interpreter is quite rudimentary, it always displays this
>  message, even if re-registration is not necessary)

Close GME
Follow the instructions for "Import the model (metamodel?)" above
Follow the instructions for "Generate the modeling lang/paradigm" above

>  3. Open the ECSL-DP UML class diagram and run the UML2XML interpreter,
>  which will produce an ECSL-DP.xml file.
In GME, File ->Close Project
File -> Open project
Browse to "ECSL_DP_uml.mga" and open it
Right click on the "Root Folder" in the right hand tree widget, select Interpret.
  _or_ click on the icon in the toolbar that has the 
  "UML 2 UDM/XML Interpreter" tooltip
Save the file as ECSL_DP.xml

>  4. Run the UDM code generator, udm.exe, on this ECSL-DP.xml file. You'll
>  need the ECSL-DP.xsd file to perform the conversion below.

In a shell, run
c:/Program\ Files/ISIS/UDM/bin/udm.exe ECSL_DP.xml
ECSL_DP.xsd will be produced

>  5. Use the generated ECSL-DP.xml file to specify the <diagramname>
>  argument(!) of udmcopy:
>           udmcopy.exe sbw.mga sbw.xml ECSL-DP.xml
>  (Make sure ECSL-DP.xsd is in PATH.)

export PATH=".;${PATH}"

bash-2.05b$ c:/Program\ Files/ISIS/UDM/bin/udmcopy.exe sbw.mga sbw.xml ECSL-DP.xml
Error during parsing: 'ECSL-DP.xml'. Exception message is: Error at file '', line 0, column 0. Message: An exception occurred! Type:RuntimeException, Message:The primary document entity could not be opened. Id=c:\cxh\ptII\vendors\ecsl_dp\sbw/ECSL-DP.xml

Whoops, wrong file name
bash-2.05b$ c:/Program\ Files/ISIS/UDM/bin/udmcopy.exe sbw.mga sbw.xml ECSL_DP.xml
Error during parsing: 'ECSL_DP.xml'. Exception message is: Error at file 'c:\cxh\ptII\vendors\ecsl_dp\sbw/ECSL_DP.xml', line 6, column 44. Message: Unknown element 'Diagram'
bash-2.05b$ 


In MyComputer, add the current dir to the path.

Now we get:
bash-2.05b$ c:/Program\ Files/ISIS/UDM/bin/udmcopy.exe sbw.mga sbw.xml ECSL_DP.xml
Com exception: File could not be opened

bash-2.05b$ 

Ok, sbw.mga does not exist, see "Import SBW model" above

Run c:/Program\ Files/ISIS/UDM/bin/udmcopy.exe sbw.mga sbw.xml ECSL_DP.xml

sbw.xml is produced!!


Ok, now:
  ../XML2ECSL/bin/Xml2Ecsl_d.exe sbw.xml sbw_ECSL.xml
  Converting sbw.xml ...
  Matlab2EcslDP(): sbw.xml sbw_ECSL.xml
  Exception: Not found

The problem here is that we already have the ECSL file, so no need
to run XML2ECSL!

