
ptolemy/domains/ptinyos/demo/README.txt

$Id$

The PtinyOS demonstrations require that ../util/nc2moml be run.
See ../util/nc2moml/index.htm for details.

Each demonstration directory has the following structure:

For the X/X.xml demos, the target is set to "pc" instead of "ptII" and 
simulation set to off so that it will only generate the code and compile 
TOSSIM by itself.  The ncapp2moml scripts are still designed to be able to 
generate this (but the flag for it is currently set to generate the 
-InWireless level).  This allows the user to use Viptos as an editing (and 
loading code onto the hardware if you set the target to "<name of 
hardware> install") environment only, w/o simulation.

For the X/X-InDE.xml demos, this is designed to test the basic port 
functionality of the MicaActor w/o involving all the Wireless Director 
complications.

For the X/X-InWireless.xml demos, this is supposed to be the full demo 
with wireless.  However, nc2app2moml may fail to generate this correctly 
if MicaBoard.xml changes, since it parses MicaBoard.xml and looks for the 
presence of certain class names (mostly the director at each level).

Keeping the three types of demos allows us to have full test of all of 
the levels of functionality.

