# Test ArrayToSequence.
#
# @Author: Yuhong Xiong
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
test ArrayToSequence-1.1 {test constructor and clone} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set a2sbase [java::new ptolemy.domains.sdf.lib.ArrayToSequence $e0 a2sbase]
    set a2s [java::cast ptolemy.domains.sdf.lib.ArrayToSequence \
		 [$a2sbase clone [$e0 workspace]]]
    $a2s setName a2s
    # Success here is just not throwing an exception.
    list {}
} {{}}

######################################################################
#### Test ArrayToSequence in an SDF model
#
test ArrayToSequence-2.1 {test double array} {
    set e0 [sdfModel 3]

    # put in a Ramp
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setExpression {-2.0}
    $step setExpression {1.0}
    set rampOut [java::field [java::cast ptolemy.actor.lib.Source $ramp] \
								output]

    # Use a SequenceToArray to generate ArrayToken
    set s2a [java::new ptolemy.domains.sdf.lib.SequenceToArray $e0 s2a]
    $s2a setName s2a
    $s2a {setContainer ptolemy.kernel.CompositeEntity} $e0
    set s2aIn [java::field [java::cast ptolemy.domains.sdf.lib.SDFTransformer $s2a] input]
    set s2aOut [java::field [java::cast ptolemy.domains.sdf.lib.SDFTransformer $s2a] output]
    [java::field $s2a arrayLength] setExpression {2}

    # Use the ArrayToSequence clone
    $a2s {setContainer ptolemy.kernel.CompositeEntity} $e0
    $a2s setName a2s
    set a2sIn [java::field [java::cast ptolemy.domains.sdf.lib.SDFTransformer $a2s] input]
    set a2sOut [java::field [java::cast ptolemy.domains.sdf.lib.SDFTransformer $a2s] output]
    [java::field $a2s arrayLength] setExpression {2}

    # put in a Recorder
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    set recIn [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]

    $e0 connect $rampOut $s2aIn
    $e0 connect $s2aOut $a2sIn
    $e0 connect $a2sOut $recIn
# FIXME: Why is this needed?
# $e0 validateSettables
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {-2.0 -1.0 0.0 1.0 2.0 3.0}

######################################################################
#### Check types of above model
#
test ArrayToSequence-2.2 {check types} {
    list [[$rampOut getType] toString] [[$s2aIn getType] toString] \
	[[$s2aOut getType] toString] [[$a2sIn getType] toString] \
	[[$a2sOut getType] toString] [[$recIn getType] toString]
} {double double {{double}} {{double}} double double}

######################################################################
#### Test string array
#
test ArrayToSequence-2.3 {test string array} {
    $init setExpression {"A"}
    $step setExpression {"B"}
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {{"A"} {"AB"} {"ABB"} {"ABBB"} {"ABBBB"} {"ABBBBB"}}

######################################################################
#### Check types of above model
#
test ArrayToSequence-2.4 {check types} {
    list [[$rampOut getType] toString] [[$s2aIn getType] toString] \
	[[$s2aOut getType] toString] [[$a2sIn getType] toString] \
	[[$a2sOut getType] toString] [[$recIn getType] toString]
} {string string {{string}} {{string}} string string}

######################################################################
#### Test cascading SequenceToArray and ArrayToSequence
#
test ArrayToSequence-2.5 {test cascading SequenceToArray and ArrayToSequence} {
    # clone s2a2
    set s2a2 [java::cast ptolemy.domains.sdf.lib.SequenceToArray \
		  [$s2a clone [$e0 workspace]]]
    $s2a2 setName s2a2
    $s2a2 {setContainer ptolemy.kernel.CompositeEntity} $e0
    set s2a2In [java::field [java::cast ptolemy.domains.sdf.lib.SDFTransformer $s2a2] input]
    set s2a2Out [java::field [java::cast ptolemy.domains.sdf.lib.SDFTransformer $s2a2] output]

    # clone a2s2
    set a2s2 [java::cast ptolemy.domains.sdf.lib.ArrayToSequence \
		  [$a2s clone [$e0 workspace]]]
    $a2s2 setName a2s2
    $a2s2 {setContainer ptolemy.kernel.CompositeEntity} $e0
    set a2s2In [java::field [java::cast ptolemy.domains.sdf.lib.SDFTransformer $a2s2] input]
    set a2s2Out [java::field [java::cast ptolemy.domains.sdf.lib.SDFTransformer $a2s2] output]

    # insert the new SequenceToArray before the Recorder
    $s2aOut unlinkAll
    $a2sIn unlinkAll
    $e0 connect $s2aOut $s2a2In
    $e0 connect $s2a2Out $a2s2In
    $e0 connect $a2s2Out $a2sIn

    $init setExpression {0}
    $step setExpression {1}

    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {0 1 2 3 4 5 6 7 8 9 10 11}

######################################################################
#### Check types of above model
#
test ArrayToSequence-2.6 {check types} {
    list [[$rampOut getType] toString] [[$s2aIn getType] toString] \
	[[$s2aOut getType] toString] [[$s2a2In getType] toString] \
	[[$s2a2Out getType] toString] [[$a2s2In getType] toString] \
	[[$a2s2Out getType] toString] [[$a2sIn getType] toString] \
	[[$a2sOut getType] toString] [[$recIn getType] toString]
} {int int {{int}} {{int}} {{{int}}} {{{int}}} {{int}} {{int}} int int}

######################################################################
#### Test array of array of string
#
test ArrayToSequence-2.7 {test array of array of string} {
    $init setExpression {"C"}
    $step setExpression {"D"}

    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {{"C"} {"CD"} {"CDD"} {"CDDD"} {"CDDDD"} {"CDDDDD"} {"CDDDDDD"} {"CDDDDDDD"} {"CDDDDDDDD"} {"CDDDDDDDDD"} {"CDDDDDDDDDD"} {"CDDDDDDDDDDD"}}

######################################################################
#### Check types of above model
#
test ArrayToSequence-2.8 {check types} {
    list [[$rampOut getType] toString] [[$s2aIn getType] toString] \
	[[$s2aOut getType] toString] [[$s2a2In getType] toString] \
	[[$s2a2Out getType] toString] [[$a2s2In getType] toString] \
	[[$a2s2Out getType] toString] [[$a2sIn getType] toString] \
	[[$a2sOut getType] toString] [[$recIn getType] toString]
} {string string {{string}} {{string}} {{{string}}} {{{string}}} {{string}} {{string}} string string}
