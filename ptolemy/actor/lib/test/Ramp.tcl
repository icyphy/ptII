# Test Ramp.
#
# @Author: Yuhong Xiong
#
# @Version: $Id$
#
# @Copyright (c) 1997-2000 The Regents of the University of California.
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

# Tycho test bed, see $TYCHO/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

######################################################################
####
#
test Ramp-1.1 {test clone} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 Ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setToken [java::new ptolemy.data.DoubleToken 2.5]
    $step setExpression init

    set newobj [java::cast ptolemy.actor.lib.Ramp [$ramp clone]]
    set newInit [getParameter $newobj init]
    set newStep [getParameter $newobj step]
    set initVal [[$newInit getToken] stringValue]
    set stepVal [[$newStep getToken] stringValue]

    list $initVal $stepVal
} {2.5 2.5}

test Ramp-1.2 {test clone} {
    $init setExpression 5.5
    set stepValue [[$step getToken] stringValue]
    set newStepValue [[$newStep getToken] stringValue]

    list $stepValue $newStepValue
} {5.5 2.5}

######################################################################
#### Test Ramp in an SDF model
#
test Ramp-2.1 {test with the default output values} {
    set e0 [sdfModel 5]
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 ramp]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $ramp] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {0 1 2 3 4}

test Ramp-2.1 {test with strings} {
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setExpression {"a"}
    $step setExpression {"b"}
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {a ab abb abbb abbbb}
