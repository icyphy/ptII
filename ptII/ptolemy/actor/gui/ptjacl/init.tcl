# Initialization for the Tcl Shell
# $Id$

# Provide really basic help
proc help {} {
    puts "To get help, use the help menu above."
    puts " which will open \$PTII/actor/gui/ptjacl/help.htm"
    puts "To list all the commands, run 'info commands'"
    puts "To list all the procs, run 'info procs'"
}

# Set the PTII variable
if {![info exist PTII]} {
    # If we are here, then we are probably running jacl and we can't
    # read environment variables
    set PTII [java::call ptolemy.data.expr.UtilityFunctions \
		  getProperty "ptolemy.ptII.dir"]
}

# Source the test definitions
source [java::call ptolemy.data.expr.UtilityFunctions \
	    findFile "util/testsuite/testDefs.tcl"]
