# Test ChoiceStyle
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2001-2003 The Regents of the University of California.
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

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


test ChoiceStyle-1.0 {test constructor and initial value} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set n [java::new ptolemy.kernel.util.NamedObj N]
    set a [java::new ptolemy.actor.gui.style.ChoiceStyle]
    #set b [java::new ptolemy.actor.gui.style.ChoiceStyle $w]
    set c [java::new ptolemy.actor.gui.style.ChoiceStyle $n C]
    set d [java::new ptolemy.actor.gui.style.ChoiceStyle $n D]
    list [$a description] \
	    [$c description] \
	    [$d description] \
} {{ptolemy.actor.gui.style.ChoiceStyle {.} attributes {
}} {ptolemy.actor.gui.style.ChoiceStyle {.N.C} attributes {
}} {ptolemy.actor.gui.style.ChoiceStyle {.N.D} attributes {
}}}

test ChoiceStyle-2.0 {settable} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set n [java::new ptolemy.kernel.util.NamedObj N]
    set n2 [java::new ptolemy.kernel.util.NamedObj N2]
    set a [java::new ptolemy.actor.gui.style.ChoiceStyle]
    set stringAttribute [java::new ptolemy.kernel.util.StringAttribute \
	    $n "MyStringAttribute"]
    # c has no attributes
    set c [java::new ptolemy.actor.gui.style.ChoiceStyle $n C]
    # d does have attributes

    set d [java::new ptolemy.actor.gui.style.ChoiceStyle $n D]


    set stringAttribute1 [java::new ptolemy.kernel.util.StringAttribute \
	    $d "MyStringAttribute1"]
    set stringAttribute2 [java::new ptolemy.kernel.util.StringAttribute \
	    $d "MyStringAttribute2"]
    list [$c acceptable $stringAttribute] \
	    [$d acceptable $stringAttribute]
} {0 1}

test ChoiceStyle-2.0 {addEntry} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set n [java::new ptolemy.kernel.util.NamedObj N]
    set stringAttribute [java::new ptolemy.kernel.util.StringAttribute \
	    $n "MyStringAttribute"]
    $stringAttribute setExpression "stringAttributeExpr"
    set choiceStyle \
	    [java::new ptolemy.actor.gui.style.ChoiceStyle $stringAttribute D]

    set stringAttribute1 [java::new ptolemy.kernel.util.StringAttribute \
	    $choiceStyle "MyStringAttribute1"]
    $stringAttribute1 setExpression "stringAttribute1Expr"

    set stringAttribute2 [java::new ptolemy.kernel.util.StringAttribute \
	    $choiceStyle "MyStringAttribute2"]
    $stringAttribute2 setExpression "stringAttribute2Expr"

    set frame [java::new javax.swing.JFrame]
    set pane [$frame getContentPane]
    set ptolemyQuery [java::new ptolemy.actor.gui.PtolemyQuery $n]
    #$ptolemyQuery addStyledEntry $stringAttribute
    $choiceStyle addEntry $ptolemyQuery
    $pane add $ptolemyQuery
    $frame pack
    $frame setVisible true
} {}
