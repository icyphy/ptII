# Test DepthFirstTransformer.
#
# @Author: Thomas Huining Feng
#
# @Version: $Id$
#
# @Copyright (c) 1997-2005 The Regents of the University of California.
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
#                       PT_COPYRIGHT_VERSION_2
#                       COPYRIGHTENDKEY
#######################################################################

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

######################################################################
####
#
test XMLTest-5.1 {Test rule5.xml with host5_1.xml} {
    set matchResult [java::call ptolemy.actor.gt.GraphMatcher match rule5.xml host5_1.xml]
    [$matchResult getMatchResult] toString
} {{ptolemy.actor.TypedIOPort {.rule5.Pattern.A.criterion1} = ptolemy.actor.TypedIOPort {.host5.BooleanSwitch.trueOutput}, ptolemy.actor.TypedIOPort {.rule5.Pattern.A.criterion2} = ptolemy.actor.TypedIOPort {.host5.BooleanSwitch.falseOutput}, ptolemy.actor.TypedIOPort {.rule5.Pattern.B.criterion1} = ptolemy.actor.TypedIOPort {.host5.Expression.input}, ptolemy.actor.TypedIOPort {.rule5.Pattern.B.criterion2} = ptolemy.actor.TypedIOPort {.host5.Expression.output}, ptolemy.actor.TypedIOPort {.rule5.Pattern.C.criterion1} = ptolemy.actor.TypedIOPort {.host5.AbsoluteValue.input}, ptolemy.actor.TypedIOPort {.rule5.Pattern.C.criterion2} = ptolemy.actor.TypedIOPort {.host5.AbsoluteValue.output}, ptolemy.actor.TypedIOPort {.rule5.Pattern.D.criterion1} = ptolemy.actor.TypedIOPort {.host5.Display2.input}, ptolemy.actor.gt.AtomicActorMatcher {.rule5.Pattern.A} = ptolemy.actor.lib.BooleanSwitch {.host5.BooleanSwitch}, ptolemy.actor.gt.AtomicActorMatcher {.rule5.Pattern.B} = ptolemy.actor.lib.Expression {.host5.Expression}, ptolemy.actor.gt.AtomicActorMatcher {.rule5.Pattern.C} = ptolemy.actor.lib.AbsoluteValue {.host5.AbsoluteValue}, ptolemy.actor.gt.AtomicActorMatcher {.rule5.Pattern.D} = ptolemy.actor.lib.gui.Display {.host5.Display2}, ptolemy.actor.gt.Pattern {.rule5.Pattern} = ptolemy.actor.TypedCompositeActor {.host5}}}

test XMLTest-5.2 {Test rule5.xml with host5_2.xml} {
    set matchResult [java::call ptolemy.actor.gt.GraphMatcher match rule5.xml host5_2.xml]
    [$matchResult getMatchResult] toString
} {{}}

test XMLTest-5.3 {Test rule5.xml with host5_3.xml} {
    set matchResult [java::call ptolemy.actor.gt.GraphMatcher match rule5.xml host5_3.xml]
    [$matchResult getMatchResult] toString
} {{ptolemy.actor.TypedIOPort {.rule5.Pattern.A.criterion1} = ptolemy.actor.TypedIOPort {.host5.BooleanSwitch.trueOutput}, ptolemy.actor.TypedIOPort {.rule5.Pattern.A.criterion2} = ptolemy.actor.TypedIOPort {.host5.BooleanSwitch.falseOutput}, ptolemy.actor.TypedIOPort {.rule5.Pattern.B.criterion1} = ptolemy.actor.TypedIOPort {.host5.AddSubtract.minus}, ptolemy.actor.TypedIOPort {.rule5.Pattern.B.criterion2} = ptolemy.actor.TypedIOPort {.host5.AddSubtract.output}, ptolemy.actor.TypedIOPort {.rule5.Pattern.C.criterion1} = ptolemy.actor.TypedIOPort {.host5.AbsoluteValue.input}, ptolemy.actor.TypedIOPort {.rule5.Pattern.C.criterion2} = ptolemy.actor.TypedIOPort {.host5.AbsoluteValue.output}, ptolemy.actor.TypedIOPort {.rule5.Pattern.D.criterion1} = ptolemy.actor.TypedIOPort {.host5.Display2.input}, ptolemy.actor.gt.AtomicActorMatcher {.rule5.Pattern.A} = ptolemy.actor.lib.BooleanSwitch {.host5.BooleanSwitch}, ptolemy.actor.gt.AtomicActorMatcher {.rule5.Pattern.B} = ptolemy.actor.lib.AddSubtract {.host5.AddSubtract}, ptolemy.actor.gt.AtomicActorMatcher {.rule5.Pattern.C} = ptolemy.actor.lib.AbsoluteValue {.host5.AbsoluteValue}, ptolemy.actor.gt.AtomicActorMatcher {.rule5.Pattern.D} = ptolemy.actor.lib.gui.Display {.host5.Display2}, ptolemy.actor.gt.Pattern {.rule5.Pattern} = ptolemy.actor.TypedCompositeActor {.host5}}}
