# Test SquareRoot.
#
# @Author: Shankar Rao
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
test SquareRoot-1.1 {test constructor and clone} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set sqrtbase [java::new ptolemy.actor.lib.SquareRoot $e0 sqrt]
    set sqrt [java::cast ptolemy.actor.lib.SquareRoot [$sqrtbase clone]]
    # Success here is just not throwing an exception.
    list {}
} {{}}

######################################################################
#### Test SquareRoot in an SDF model
#
test SquareRoot-2.1 {test with the default output values} {
    set e0 [sdfModel 5]
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setExpression {0.0}
    $step setExpression {1.0}
    # Use clone of sqrt to make sure that is ok.
    $sqrt setContainer $e0
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $ramp] output] \
       [java::field [java::cast ptolemy.actor.lib.Transformer $sqrt] input]
    $e0 connect \
       [java::field \
       [java::cast ptolemy.actor.lib.Transformer $sqrt] output] \
       [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    [$e0 getManager] execute
    ptclose [enumToTokenValues [$rec getRecord 0]] \
            {0.0 1.000 1.414 1.732 2.000} \
            0.001
} {1}






