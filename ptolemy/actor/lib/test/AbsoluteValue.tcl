# Test AbsoluteValue.
#
# @Author: Edward A. Lee
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
test AbsoluteValue-1.1 {test constructor and clone} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set absbase [java::new ptolemy.actor.lib.AbsoluteValue $e0 abs]
    set abs [java::cast ptolemy.actor.lib.AbsoluteValue [$absbase clone]]
    # Success here is just not throwing an exception.
    list {}
} {{}}

######################################################################
#### Test AbsoluteValue in an SDF model
#
test AbsoluteValue-2.1 {test with the default output values} {
    set e0 [sdfModel 5]
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setExpression {-2.0}
    $step setExpression {1.0}
    # Use clone of abs to make sure that is ok.
    $abs setContainer $e0
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $ramp] output] \
       [java::field [java::cast ptolemy.actor.lib.Transformer $abs] input]
    $e0 connect \
       [java::field \
       [java::cast ptolemy.actor.lib.Transformer $abs] output] \
       [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {2.0 1.0 0.0 1.0 2.0}

######################################################################
#### Check types of above model
#
test AbsoluteValue-2.2 {check types} {
    set rampOut [java::field [java::cast ptolemy.actor.lib.Source $ramp] \
	output]
    set absIn [java::field [java::cast ptolemy.actor.lib.Transformer $abs] \
	input]
    set absOut [java::field [java::cast ptolemy.actor.lib.Transformer $abs] \
	output]
    set recIn [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]

    list [[$rampOut getType] toString] [[$absIn getType] toString] \
	[[$absOut getType] toString] [[$recIn getType] toString]
} {double double double double}

######################################################################
#### Test int type
#
test AbsoluteValue-2.3 {test int type} {
    $init setExpression {-4}
    $step setExpression {2}
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {4 2 0 2 4}

######################################################################
#### Check types of above model
#
test AbsoluteValue-2.4 {check types} {
    list [[$rampOut getType] toString] [[$absIn getType] toString] \
	[[$absOut getType] toString] [[$recIn getType] toString]
} {int int int int}

######################################################################
#### Test complex type
#
test AbsoluteValue-2.5 {test complex type} {
    $init setExpression {0}
    $step setExpression {3+4i}
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {0.0 5.0 10.0 15.0 20.0}

######################################################################
#### Check types of above model
#
test AbsoluteValue-2.6 {check types} {
    list [[$rampOut getType] toString] [[$absIn getType] toString] \
	[[$absOut getType] toString] [[$recIn getType] toString]
} {complex complex double double}

######################################################################
#### Test long type
#
test AbsoluteValue-2.7 {test long type} {
    $init setExpression {-6l}
    $step setExpression {3}
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {6 3 0 3 6}

######################################################################
#### Check types of above model
#
test AbsoluteValue-2.8 {check types} {
    list [[$rampOut getType] toString] [[$absIn getType] toString] \
	[[$absOut getType] toString] [[$recIn getType] toString]
} {long long long long}

