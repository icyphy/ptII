# Tests for the CTExecutionPhase class
#
# @Author: Haiyang Zheng
#
# @Version: $Id$
#
# @Copyright (c) 1999-2005 The Regents of the University of California.
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

# Ptolemy II bed, see /users/cxh/ptII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs enumToObjects] == "" } then {
     source enums.tcl
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
# CTExecutionPhase to String
test CTExecutionPhase-2.1 {} {

	set phase1 [[java::field ptolemy.domains.ct.kernel.CTExecutionPhase FIRING_DYNAMIC_ACTORS_PHASE] toString]
	set phase2 [[java::field ptolemy.domains.ct.kernel.CTExecutionPhase FIRING_EVENT_GENERATORS_PHASE] toString]
	set phase3 [[java::field ptolemy.domains.ct.kernel.CTExecutionPhase FIRING_STATE_TRANSITION_ACTORS_PHASE] toString]
	set phase4 [[java::field ptolemy.domains.ct.kernel.CTExecutionPhase GENERATING_EVENTS_PHASE] toString]
	set phase5 [[java::field ptolemy.domains.ct.kernel.CTExecutionPhase GENERATING_WAVEFORMS_PHASE] toString]
	set phase6 [[java::field ptolemy.domains.ct.kernel.CTExecutionPhase ITERATING_PURELY_DISCRETE_ACTORS_PHASE] toString]
	set phase7 [[java::field ptolemy.domains.ct.kernel.CTExecutionPhase POSTFIRING_EVENT_GENERATORS_PHASE] toString]
	set phase8 [[java::field ptolemy.domains.ct.kernel.CTExecutionPhase PREFIRING_DYNAMIC_ACTORS_PHASE] toString]
	set phase9 [[java::field ptolemy.domains.ct.kernel.CTExecutionPhase PRODUCING_OUTPUTS_PHASE] toString]
	set phase10 [[java::field ptolemy.domains.ct.kernel.CTExecutionPhase UNKNOWN_PHASE] toString]
	set phase11 [[java::field ptolemy.domains.ct.kernel.CTExecutionPhase UPDATING_CONTINUOUS_STATES_PHASE] toString]

    list $phase1 $phase2 $phase3 $phase4 $phase5 $phase6 $phase7 $phase8 $phase9 $phase10 $phase11

} {FIRING_DYNAMIC_ACTORS_PHASE FIRING_EVENT_GENERATORS_PHASE\
FIRING_STATE_TRANSITION_ACTORS_PHASE GENERATING_EVENTS_PHASE GENERATING_WAVEFORMS_PHASE\
ITERATING_PURELY_DISCRETE_ACTORS_PHASE POSTFIRING_EVENT_GENERATORS_PHASE\
PREFIRING_DYNAMIC_ACTORS_PHASE PRODUCING_OUTPUTS_PHASE UNKNOWN_PHASE\
UPDATING_CONTINUOUS_STATES_PHASE}
