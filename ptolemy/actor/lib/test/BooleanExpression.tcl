# Test BooleanExpression.
#
# @Author: Yuhong Xiong, Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997-2001 The Regents of the University of California.
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
test BooleanExpression-1.1 {test clone} {
    set e0 [sdfModel 3]
    set exprmaster [java::new ptolemy.actor.lib.BooleanExpression $e0 expr]
    set expr [_testClone $exprmaster]
    $exprmaster setContainer [java::null]
    $expr setContainer $e0
    $expr description 1
    # Make sure that clone is giving us a boolean
    set output [java::field [java::cast ptolemy.actor.lib.Expression $expr] \
	    output]
    set outputType [$output getType]
    list [$expr description 1] \
	    [$outputType toString]
} {ptolemy.actor.lib.BooleanExpression boolean}

test BooleanExpression-2.1 {run with default empty BooleanExpression} {
    set in1 [java::new ptolemy.actor.TypedIOPort $expr in1 true false]
    set ramp1 [java::new ptolemy.actor.lib.Ramp $e0 ramp1]   
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    set r1 [$e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $ramp1] output] \
            $in1]
    $e0 connect \
	    [java::field [java::cast ptolemy.actor.lib.Expression $expr] \
	    output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    set m [$e0 getManager]
    catch {$m execute} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: .top.expr:
Expression yields a null result: }}

test BooleanExpression-3.1 {run with a simple BooleanExpression} {
    set BooleanExpression [java::field \
	    [java::cast ptolemy.actor.lib.Expression $expr] \
	    expression]
    $BooleanExpression setExpression "iteration == 2"
    $m execute
    enumToTokenValues [$rec getRecord 0]
} {false true false}

test BooleanExpression-5.1 {run with a non BooleanExpression} {
    set rampinit [java::field $ramp1 init]
    $rampinit setExpression "0.0"
    $BooleanExpression setExpression "time + 5"
    catch {$m execute} err
    list $err
} {{ptolemy.kernel.util.IllegalActionException: Run-time type checking failed. Token type: double, port: .top.expr.output, port type: boolean}}
