# Test Gaussian
#
# @Author: Edward A. Lee
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

######################################################################
####
#
test Gaussian-1.1 {test constructor} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set g [java::new ptolemy.actor.lib.Gaussian $e0 g]
    set seed [getParameter $g seed]
    set mean [getParameter $g mean]
    set standardDeviation [getParameter $g standardDeviation]

    set seedVal [[$seed getToken] toString]
    set meanVal [[$mean getToken] toString]
    set standardDeviation [[$standardDeviation getToken] toString]

    list $seedVal $meanVal $standardDeviation
} {0L 0.0 1.0}

test Gaussian-1.2 {test clone} {
    set g2 [java::cast ptolemy.actor.lib.Gaussian [$g clone [$e0 workspace]]]
    $seed setExpression {2l}
    set seed [getParameter $g2 seed]
    [$seed getToken] toString
} {0L}

######################################################################
#### Test Gaussian in an SDF model
#
test Gaussian-2.1 {test without seed set} {
    set e0 [sdfModel]
    set g [java::new ptolemy.actor.lib.Gaussian $e0 g]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $g] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    [$e0 getManager] execute
    set first [enumToTokenValues [$rec getRecord 0]]
    # Have to be sure enough time passes that we don't accidentally
    # get the same seed, which is selected based on the passage of time.
    sleep 1
    [$e0 getManager] execute
    set second [enumToTokenValues [$rec getRecord 0]]
    expr {$first != $second}
} {1}

test Gaussian-2.2 {test with seed set} {
    set seed [getParameter $g seed]
    $seed setExpression {2l}   
    [$e0 getManager] execute
    set first [enumToTokenValues [$rec getRecord 0]]
    [$e0 getManager] execute
    set second [enumToTokenValues [$rec getRecord 0]]
    expr {$first == $second}
} {1}

test Gaussian-2.3 {test set mean to an int} {
    set mean [getParameter $g mean]
    $mean setExpression {42}   
    [$e0 getManager] execute
    set first [enumToTokenValues [$rec getRecord 0]]
    [$e0 getManager] execute
    set second [enumToTokenValues [$rec getRecord 0]]
    expr {$first == $second}
} {1}

