# Tests for the SDFCompositeActor class
#
# @Author: Christopher Hylands
#
# @Version: : NamedObj.tcl,v 1.33 1998/12/05 05:37:32 cxh Exp $
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

if {[string compare sdfModel [info procs sdfModel]] != 0} \
        then {
    source [file join $PTII util testsuite models.tcl]
} {}

#if {[info procs enumToObjects] == "" } then {
#     source enums.tcl
#}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test SDFCompositeActor-1.1 {SDF Bug when actors are connected through external ports} {

    set e0 [sdfModel 5]
    set const [java::new ptolemy.actor.lib.Const $e0 const]
    set scale [java::new ptolemy.actor.lib.Scale $e0 scale]
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 ramp]

    set port8 [java::new ptolemy.actor.TypedIOPort $e0 "port8" false true]
    $port8 setMultiport true

    $e0 connect $port8 \
	    [java::field [java::cast ptolemy.actor.lib.Source $ramp] output]


    $e0 connect $port8 \
	    [java::field [java::cast ptolemy.actor.lib.Transformer $scale] output]

    set relation5 [java::new ptolemy.actor.TypedIORelation $e0 relation5]

    set const_output [java::field [java::cast ptolemy.actor.lib.Source $const] output] 
    $const_output link $relation5

    set scale_input [java::field [java::cast ptolemy.actor.lib.Transformer $scale] input] 
    $scale_input link $relation5


    [$e0 getManager] execute
} {}



