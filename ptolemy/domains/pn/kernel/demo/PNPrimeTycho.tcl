# PNPrime Tycho front end
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1998 The Regents of the University of California.
# All rights reserved.
# 
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
# 
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
# 
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
# 
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
#######################################################################

# This is the Tycho side of the PNPrime Demo.
# We start up the JRPC daemon and then run the PNPrime demo
# in a remote Tcl Blend process.
# The remote process writes the dag files out and then
# the local Tycho process views them

proc PNPrimeTycho { {numberOfCycles 10}} {
    global TYCHO
    global jrpc
    if ![info exists jrpc] {
	puts "Please wait while the remote Tcl Blend Process starts up."
	flush stdout
	update

	# Generate a "random" port number. This is awful!
	set str [clock clicks]
	set len [string length $str]
	set portnum 1[string range $str [expr $len-4] end]
	puts "Portnum = $portnum"
        update
	
	set jrpc [::tycho::JRPC [::tycho::autoName .jrpc] -portnum $portnum]
	puts "Remote Tcl Blend Process started."
    }
    set initialDAGFileName [::tycho::tmpFileName initial .dag]
    set finalDAGFileName [::tycho::tmpFileName final .dag]

    $jrpc send "set initialDAGFileName $initialDAGFileName"
    $jrpc send "set finalDAGFileName $finalDAGFileName"
    $jrpc send "set numberOfCycles $numberOfCycles"
    puts [$jrpc send "source \
	    [file join $TYCHO java pt domains pn kernel demo \
	    PNPrimeExample.tcl]"]

    ::tycho::File::openContext $initialDAGFileName
    ::tycho::File::openContext $finalDAGFileName

    file delete $initialDAGFileName
    file delete $finalDAGFileName    
}
