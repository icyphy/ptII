# Tests for the Variable class
#
# @Author: Xiaojun Liu
#
# @Version: $Id$
#
# @Copyright (c) 1998-1999  The Regents of the University of California.
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
test Variable-0.1 {Basic smoke test} {

    # suppose we have three inputs to this entity, namely
    # in1, in2, in3, and two outputs, namely out1, out2
    set e [java::new ptolemy.kernel.Entity parent]

    # this list has variables representing inputs' status
    set inlist1 [java::new ptolemy.automata.util.VariableList $e inputS]
    # this list has variables representing inputs' values
    set inlist2 [java::new ptolemy.automata.util.VariableList $e inputV]
    # this list has two variables holding values for two outputs
    set outlist [java::new ptolemy.automata.util.VariableList $e output]

    # create variables in inlist1
    set in1s [java::new ptolemy.automata.util.Variable $inlist1 in1]
    set in2s [java::new ptolemy.automata.util.Variable $inlist1 in2]
    set in3s [java::new ptolemy.automata.util.Variable $inlist1 in3]
    
    # create variables in inlist2, note the variables' names are the
    # same as those in inlist1
    set in1v [java::new ptolemy.automata.util.Variable $inlist2 in1]
    set in2v [java::new ptolemy.automata.util.Variable $inlist2 in2]
    set in3v [java::new ptolemy.automata.util.Variable $inlist2 in3]

    # create variables in outlist
    set out1 [java::new ptolemy.automata.util.Variable $outlist out1]
    set out2 [java::new ptolemy.automata.util.Variable $outlist out2]

    # out1 is a trigger event, so has inputs' status as scope
    $out1 addToScope $inlist1
    $out1 setExpression "in1 & in2 | !in3"
    # out2 is a trigger condition, so has inputs' values as scope
    $out2 addToScope $inlist2
    $out2 setExpression "in1 + in2 - in3"

    # these boolean tokens are used to set values of status 
    # variables in inlist1
    set toka [java::new {ptolemy.data.BooleanToken boolean} true]
    set tokb [java::new {ptolemy.data.BooleanToken boolean} false]
    set tokc [java::new {ptolemy.data.BooleanToken boolean} true]
    set tokd [java::new {ptolemy.data.BooleanToken boolean} true]

    # inlist1's variables will not propogate change
    $inlist1 setReportChange false

    # set values of inlist1's variables 
    $in1s setToken $toka
    $in2s setToken $tokb
    $in3s setToken $tokc

    # evaluate out1 and get its value
    $out1 evaluate
    set msg1 [[$out1 getToken] toString]
    
    # change the value of a variable and reevaluate
    $in2s setToken $tokd
    $out1 evaluate
    set msg2 [[$out1 getToken] toString]

    # these data tokens are used to set values of variables in 
    # inlist2
    set tok1 [java::new {ptolemy.data.DoubleToken double} 3.0]
    set tok2 [java::new {ptolemy.data.IntToken int} 1]
    set tok3 [java::new {ptolemy.data.IntToken int} 2]
    set tok4 [java::new {ptolemy.data.IntToken int} 3]

    # inlist2's variables will not propogate change
    $inlist2 setReportChange false

    $in1v setToken $tok1
    $in2v setToken $tok2
    $in3v setToken $tok3

    $out2 evaluate
    set msg3 [[$out2 getToken] toString]

    $in3v setToken $tok4
    $out2 evaluate
    set msg4 [[$out2 getToken] toString]

    list $msg1 $msg2 $msg3 $msg4
} {ptolemy.data.BooleanToken(false) ptolemy.data.BooleanToken(true) ptolemy.data.DoubleToken(2.0) ptolemy.data.DoubleToken(1.0)}








