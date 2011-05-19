# Tests for the FiringEvent class
#
# @Author: Christopher Hylands (tests only)
#
# @Version: $Id$
#
# @Copyright (c) 2002-2005 The Regents of the University of California.
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

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test FiringEvent-1.0 {Constructor} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set d1 [java::new ptolemy.actor.Director]
    $d1 setName D1
    set d2 [java::new ptolemy.actor.Director $w]
    $d2 setName D2
    set e0 [java::new ptolemy.actor.CompositeActor $w]
    $e0 setName E0
    set d3 [java::new ptolemy.actor.Director $e0 D3]

    
    set firingEventType [java::field ptolemy.actor.FiringEvent BEFORE_POSTFIRE]
    set fe [java::new ptolemy.actor.FiringEvent $d3 $e0 $firingEventType]

    list \
	    [objectsToNames [list \
	    [$fe getActor] [$fe getDirector] \
	    [$fe getSource]]] \
	    [[$fe getType] toString] \
	    [$fe toString]

} {{E0 D3 D3} {FiringEventType(will be postfired)} {The actor .E0 will be postfired.}}


######################################################################
####
#
test FiringEvent-1.0 {Constructor with multiplicity} {
    # Uses setup in 1.0 above

    set firingEventType [java::field ptolemy.actor.FiringEvent AFTER_POSTFIRE]

    # construct with a multiplicity of 2
    set fe [java::new ptolemy.actor.FiringEvent $d3 $e0 $firingEventType 2]

    list \
	    [objectsToNames [list \
	    [$fe getActor] [$fe getDirector] \
	    [$fe getSource]]] \
	    [[$fe getType] toString] \
	    [$fe toString]

} {{E0 D3 D3} {FiringEventType(was postfired)} {The actor .E0 was postfired 2 times.}}
