# Test OrderedMerge
#
# @Author: Christopher Hylands, based on Ramp.tcl by Yuhong Xiong
#
# @Version: $Id$
#
# @Copyright (c) 1997-2003 The Regents of the University of California.
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

test OrderedMerge-2.1 {test with the default output values} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set manager [java::new ptolemy.actor.Manager]
    set director [java::new ptolemy.domains.pn.kernel.PNDirector]
    $e0 setDirector $director
    $e0 setName top
    $e0 setManager $manager

    $director addDebugListener \
	[java::new ptolemy.kernel.util.StreamListener]


    set orderedMerge [java::new ptolemy.actor.lib.OrderedMerge $e0 OrderedMerge]
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 Ramp]
    set ramp2 [java::new ptolemy.actor.lib.Ramp $e0 Ramp2]
    set step [getParameter $ramp2 step]
    $step setExpression 1.5

    set test2 [java::new ptolemy.actor.lib.Test $e0 Test2]
    set correctValues [getParameter $test2 correctValues]
    $correctValues setExpression {"{0.0, 0.0, 1.0, 1.5, 2.0, 3.0, 3.0, 4.0}"}

    $ramp addDebugListener \
	[java::new ptolemy.kernel.util.StreamListener]

    $ramp2 addDebugListener \
	[java::new ptolemy.kernel.util.StreamListener]

    $orderedMerge addDebugListener \
	[java::new ptolemy.kernel.util.StreamListener]

    $test2 addDebugListener \
	[java::new ptolemy.kernel.util.StreamListener]

    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $ramp] output] \
            [java::field $orderedMerge inputA]

    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $ramp2] output] \
            [java::field $orderedMerge inputB]


    $e0 connect \
            [java::field $orderedMerge output] \
            [java::field [java::cast ptolemy.actor.lib.Transformer $test2] output]

    [$e0 getManager] execute
} {}
