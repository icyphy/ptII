# CAUTION: automatically generated file by a rule in ptcommon.mk
# This file will source all the Tcl files that use Java. 
# This file will source the tcl files list in the
# makefile SIMPLE_JTESTS and GRAPHICAL_JTESTS variables
# This file is different from all.itcl in that all.itcl
# will source all the .itcl files in the current directory
#
# Set the following to avoid endless calls to exit
if {![info exists reallyExit]} {set reallyExit 0}
# Exiting when there are no more windows is wrong
#::tycho::TopLevel::exitWhenNoMoreWindows 0
# If there is no update command, define a dummy proc.  Jacl needs this
if {[info command update] == ""} then { 
    proc update {} {}
}
#Do an update so that we are sure tycho is done displaying
update
set savedir "[pwd]"
if {"FixToDouble.tcl DoubleToFix.tcl ComplexToReal.tcl PolarToRectangular.tcl RealToComplex.tcl RectangularToPolar.tcl" != ""} {foreach i [list FixToDouble.tcl DoubleToFix.tcl ComplexToReal.tcl PolarToRectangular.tcl RealToComplex.tcl RectangularToPolar.tcl] {puts $i; cd "$savedir"; if [ file exists $i ] {source $i}}}
puts stderr dummy.tcl
cd "$savedir"
if [ file exists dummy.tcl ] {source dummy.tcl}
catch {doneTests}
exit
