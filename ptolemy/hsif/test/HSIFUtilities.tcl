# Tests for the HSIFUtilities class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2003 The Regents of the University of California.
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

# Load the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#

test HSIFUtilities-1.1 {Convert the SwimmingPool example} {
    java::call ptolemy.hsif.HSIFUtilities HSIFToMoML \
	../demo/SwimmingPool/SwimmingPool.xml SwimmingPool_moml.xml
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parseFile SwimmingPool_moml.xml]
    set composite [java::cast ptolemy.actor.CompositeActor $toplevel]
    set director [$composite getDirector]	
    list [$director toString]
} {{ptolemy.domains.ct.kernel.CTMixedSignalDirector {.new_swimmingpool.CT Director}}}

test HSIFUtilities-1.2 {Convert the Thermostat example using main to increase code coverage} {
    set args [java::new {String[]} 2 \
	[list "../demo/Thermostat/Thermostat.xml" "Thermostat_moml.xml"]]
    java::call ptolemy.hsif.HSIFUtilities main $args
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parseFile Thermostat_moml.xml]
    set composite [java::cast ptolemy.actor.CompositeActor $toplevel]
    set director [$composite getDirector]	
    list [$director toString]
} {{ptolemy.domains.ct.kernel.CTMixedSignalDirector {.Thermostat.CT Director}}}

