# Tests for the WirelessDirector class
#
# @Author: Christopher Brooks, based on SDFDirector.tcl by Stephen Neuendorffer
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
#### Test the constructor
#
test WirelessDirector-1.1 {Test the constructor} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set e0 [java::new ptolemy.actor.TypedCompositeActor $w]
    $e0 setName E0
    set d3 [java::new ptolemy.domains.wireless.kernel.WirelessDirector $e0 D3]
    list [$d3 getFullName]
} {.E0.D3}

######################################################################
#### Test newReceiver
#
test WirelessDirector-2.1 {newReceiver} {
    # Uses 1.1 above
    set receiver [java::cast ptolemy.domains.wireless.kernel.WirelessReceiver \
	[$d3 newReceiver]]
    list [java::isnull [$receiver getProperties]]
} {1}

######################################################################
#### Test newReceiver
#
test WirelessDirector-2.1 {newReceiver} {
    # Uses 1.1 above
    set receiver [java::cast ptolemy.domains.wireless.kernel.WirelessReceiver \
	[$d3 newReceiver]]
    list [java::isnull [$receiver getProperties]]
} {1}

test WirelessDirector-2.2 {newReceiver with debugging} {
    # Uses 1.1 above
    set listener [java::new ptolemy.kernel.util.RecorderListener]
    $d3 addDebugListener $listener
    set receiver [java::cast ptolemy.domains.wireless.kernel.WirelessReceiver \
	[$d3 newReceiver]]
    list [$listener getMessages]
} {{Creating new WirelessReceiver.
}}
