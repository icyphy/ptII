# Tests for variable model analysis
#
# @Author: Steve Neuendorffer
#
# @Version: $Id$
#
# @Copyright (c) 2000-2003 The Regents of the University of California.
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

if {[info procs jdkClassPathSeparator] == "" } then { 
    source [file join $PTII util testsuite jdktools.tcl]
}

if {[string compare sdfModel [info procs sdfModel]] != 0} \
        then {
    source [file join $PTII util testsuite models.tcl]
} {}

if {[info procs test_clone] == "" } then { 
    source [file join $PTII util testsuite testParameters.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
#set VERBOSE 1

######################################################################
####
#

# First, do an SDF test just to be sure things are working
test FreeVariableModelAnalysis-1.1 {} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 Ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setToken [java::new ptolemy.data.DoubleToken 2.5]
    $step setExpression init

    set analysis [java::new ptolemy.actor.util.FreeVariableModelAnalysis $e0]
    list [lsort [listToStrings [$analysis getFreeVariables $e0]]] \
	[lsort [listToStrings [$analysis getFreeVariables $ramp]]]
} {{} {}}


test FreeVariableModelAnalysis-1.2 {} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set p0 [java::new ptolemy.data.expr.Parameter $e0 p0]
    $p0 setExpression 5
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 Ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setToken [java::new ptolemy.data.DoubleToken 2.5]
    $step setExpression p0

    set analysis [java::new ptolemy.actor.util.FreeVariableModelAnalysis $e0]
    list [lsort [listToStrings [$analysis getFreeVariables $e0]]] \
	[lsort [listToStrings [$analysis getFreeVariables $ramp]]]
} {{} p0}

test FreeVariableModelAnalysis-1.3 {} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set p0 [java::new ptolemy.data.expr.Parameter $e0 p0]
    set p1 [java::new ptolemy.data.expr.Parameter $e0 p1]
    $p0 setExpression p1
    $p1 setExpression 5
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 Ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setExpression p1
    $step setExpression p0

    set analysis [java::new ptolemy.actor.util.FreeVariableModelAnalysis $e0]
    list [lsort [listToStrings [$analysis getFreeVariables $e0]]] \
	[lsort [listToStrings [$analysis getFreeVariables $ramp]]]
} {{} {p0 p1}}


test FreeVariableModelAnalysis-1.4 {} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set p0 [java::new ptolemy.data.expr.Parameter $e0 p0]
    set p1 [java::new ptolemy.data.expr.Parameter $e0 p1]
    $p0 setExpression p1
    $p1 setExpression a
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 Ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setExpression p1
    $step setExpression p0

    set analysis [java::new ptolemy.actor.util.FreeVariableModelAnalysis $e0]
    list [lsort [listToStrings [$analysis getFreeVariables $e0]]] \
	[lsort [listToStrings [$analysis getFreeVariables $ramp]]]
} {a {p0 p1}}


test FreeVariableModelAnalysis-1.5 {} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set p0 [java::new ptolemy.data.expr.Parameter $e0 p0]
    set p1 [java::new ptolemy.data.expr.Parameter $e0 p1]
    $p0 setExpression p1
    $p1 setExpression a
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 Ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setExpression "p1 + a"
    $step setExpression p0

    set analysis [java::new ptolemy.actor.util.FreeVariableModelAnalysis $e0]
    list [lsort [listToStrings [$analysis getFreeVariables $e0]]] \
	[lsort [listToStrings [$analysis getFreeVariables $ramp]]]
} {a {a p0 p1}}

test FreeVariableModelAnalysis-1.6 {Note... not a correct model (circular dependancy)} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set p0 [java::new ptolemy.data.expr.Parameter $e0 p0]
    set p1 [java::new ptolemy.data.expr.Parameter $e0 p1]
    $p0 setExpression p1
    $p1 setExpression p0
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 Ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setExpression "p1 + a"
    $step setExpression p0

    set analysis [java::new ptolemy.actor.util.FreeVariableModelAnalysis $e0]
    list [lsort [listToStrings [$analysis getFreeVariables $e0]]] \
	[lsort [listToStrings [$analysis getFreeVariables $ramp]]]
} {a {a p0 p1}}

test FreeVariableModelAnalysis-1.7 {} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set p0 [java::new ptolemy.data.expr.Parameter $e0 p0]
    set p1 [java::new ptolemy.data.expr.Parameter $e0 p1]
    $p0 setExpression p0
    $p1 setExpression p1
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 Ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setExpression "p1 + a"
    $step setExpression 5

    set analysis [java::new ptolemy.actor.util.FreeVariableModelAnalysis $e0]
    list [lsort [listToStrings [$analysis getFreeVariables $e0]]] \
	[lsort [listToStrings [$analysis getFreeVariables $ramp]]]
} {{a p0 p1} {a p1}}
