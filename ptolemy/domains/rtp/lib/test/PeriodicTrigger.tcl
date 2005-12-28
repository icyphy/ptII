# Tests for the PeriodicTrigger.tcl
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2005 The Regents of the University of California.
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

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then { 
    source testDefs.tcl
} {}

if {[string compare listToStrings [info procs listToStrings]] == 1} then { 
    source $PTII/util/testsuite/enums.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
# 
test PeriodicTrigger-1.1 {Check out clone} {
    set top [java::new ptolemy.kernel.CompositeEntity]

    # rtp is probably not in the configuration, so we do a quick check of clone
    set periodicTrigger [java::new ptolemy.domains.rtp.lib.PeriodicTrigger \
	$top "My PeriodicTrigger"]	
    set w [java::new ptolemy.kernel.util.Workspace]
    set clone [java::cast ptolemy.domains.rtp.lib.PeriodicTrigger \
	[$periodicTrigger clone $w]]
    set frequency [java::field $clone frequency] 
    set type [$frequency getType]
    list [$periodicTrigger getFullName] [$clone getFullName] [$type toString]
} {{..My PeriodicTrigger} {.My PeriodicTrigger} double}
