# Test Expression.
#
# @Author: Yuhong Xiong, Edward A. Lee
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# If a file contains non-graphical tests, then it should be named .tcl
# If a file contains graphical tests, then it should be called .itcl
#
# It would be nice if the tests would work in a vanilla itkwish binary.
# Check for necessary classes and adjust the auto_path accordingly.
#

######################################################################
####
#
test Expression-1.1 {test clone} {
    set e0 [sdfModel 3]
    set exprmaster [java::new ptolemy.actor.lib.Expression $e0 expr]
    set expr [_testClone $exprmaster]
    $exprmaster setContainer [java::null]
    $expr setContainer $e0
    $expr description 1
} {ptolemy.actor.lib.Expression}

test Expression-2.1 {run with default empty expression} {
    set in1 [java::new ptolemy.actor.TypedIOPort $expr in1 true false]
    set ramp1 [java::new ptolemy.actor.lib.Ramp $e0 ramp1]   
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    set r1 [$e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $ramp1] output] \
            $in1]
    $e0 connect \
            [java::field $expr output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    set m [$e0 getManager]
    catch {$m execute} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: .top.expr:
Expression yields a null result: null}}

test Expression-3.1 {run with a simple expression} {
    set expression [java::field $expr expression]
    $expression setExpression "iteration + 5"
    $m execute
    enumToTokenValues [$rec getRecord 0]
} {6 7 8}

# FIXME: The following test fails because of limitations in the
# type system.  When types can propagate through expressions.
# test Expression-4.1 {run with a simple expression} {
#     $expression setExpression "time + 5"
#     $m execute
#     enumToTokenValues [$rec getRecord 0]
# } {6 7 8}

test Expression-5.1 {run with a simple expression} {
    set rampinit [java::field $ramp1 init]
    $rampinit setExpression "0.0"
    $expression setExpression "time + 5"
    $m execute
    enumToTokenValues [$rec getRecord 0]
} {5.0 5.0 5.0}

test Expression-6.1 {run with a simple expression} {
    set rampinit [java::field $ramp1 init]
    $expression setExpression "in1 + 5"
    $m execute
    enumToTokenValues [$rec getRecord 0]
} {5.0 6.0 7.0}

test Expression-7.1 {run with two inputs} {
    set ramp2 [java::new ptolemy.actor.lib.Ramp $e0 ramp2]   
    set in2 [java::new ptolemy.actor.TypedIOPort $expr in2 true false]
    set r1 [$e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $ramp2] output] \
            $in2]
    $expression setExpression "in1 + in2"
    $m execute
    enumToTokenValues [$rec getRecord 0]
} {0.0 2.0 4.0}
