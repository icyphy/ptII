# Reload all the accessors.
#
# @Author: Christopher Brooks
#
# $Id$
#
# @Copyright (c) 2017 The Regents of the University of California.
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

test reloadAllAccessors-1.1 {Reload all the accessors} {
    puts "About to invoke make reloadAllAccessors at  [clock format [clock seconds]]"
    set output [exec -stderrok make reloadAllAccessors]
    puts $output
    puts "Done invoking make reloadAllAccessors at  [clock format [clock seconds]]"
    # Success is not crashing
    list {}
} {{}}

test reloadAllAccessors-2.1 {Check that commonHost.js has been updated} {
    set accessorsSize [file size $PTII/org/terraswarm/accessor/accessors/web/hosts/common/commonHost.js]
    set capeCodeSize [file size $PTII/ptolemy/actor/lib/jjs/commonHost.js]
    if { $accessorsSize != $capeCodeSize } {
        error "The size of $PTII/org/terraswarm/accessor/accessors/web/hosts/common/commonHost.js ($accessorsSize) is not the same as the size of $PTII/ptolemy/actor/lib/jjs/commonHost.js ($capeCodeSize)"
    }
    list {}
} {{}}
