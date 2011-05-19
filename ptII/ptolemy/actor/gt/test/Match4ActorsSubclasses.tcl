# Test DepthFirstTransformer.
#
# @Author: Thomas Huining Feng
#
# @Version: $Id$
#
# @Copyright (c) 1997-2009 The Regents of the University of California.
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

# Directory in which the patterns may be found
set p ../patterns/

######################################################################
####
#
test Match4ActorsSubclasses.1 {Test Match4ActorsSubclasses_rule.xml with Match4ActorsSubclasses_succ1.xml} {
    set matchResult [java::call ptolemy.actor.gt.GraphMatcher match ${p}Match4ActorsSubclasses_rule.xml ${p}Match4ActorsSubclasses_succ1.xml]
    [$matchResult getMatchResult] toString
} {{ptolemy.actor.gt.AtomicActorMatcher {.Match4ActorsSubclasses_rule.Pattern.A} = ptolemy.actor.lib.BooleanSwitch {.Match4ActorsSubclasses_succ1.BooleanSwitch}, ptolemy.actor.gt.AtomicActorMatcher {.Match4ActorsSubclasses_rule.Pattern.B} = ptolemy.actor.lib.Expression {.Match4ActorsSubclasses_succ1.Expression}, ptolemy.actor.gt.AtomicActorMatcher {.Match4ActorsSubclasses_rule.Pattern.C} = ptolemy.actor.lib.AbsoluteValue {.Match4ActorsSubclasses_succ1.AbsoluteValue}, ptolemy.actor.gt.AtomicActorMatcher {.Match4ActorsSubclasses_rule.Pattern.D} = ptolemy.actor.lib.gui.Display {.Match4ActorsSubclasses_succ1.Display2}, ptolemy.actor.gt.Pattern {.Match4ActorsSubclasses_rule.Pattern} = ptolemy.actor.TypedCompositeActor {.Match4ActorsSubclasses_succ1}, ptolemy.actor.gt.PortMatcher {.Match4ActorsSubclasses_rule.Pattern.A.criterion1} = ptolemy.actor.TypedIOPort {.Match4ActorsSubclasses_succ1.BooleanSwitch.trueOutput}, ptolemy.actor.gt.PortMatcher {.Match4ActorsSubclasses_rule.Pattern.A.criterion2} = ptolemy.actor.TypedIOPort {.Match4ActorsSubclasses_succ1.BooleanSwitch.falseOutput}, ptolemy.actor.gt.PortMatcher {.Match4ActorsSubclasses_rule.Pattern.B.criterion1} = ptolemy.actor.TypedIOPort {.Match4ActorsSubclasses_succ1.Expression.input}, ptolemy.actor.gt.PortMatcher {.Match4ActorsSubclasses_rule.Pattern.B.criterion2} = ptolemy.actor.TypedIOPort {.Match4ActorsSubclasses_succ1.Expression.output}, ptolemy.actor.gt.PortMatcher {.Match4ActorsSubclasses_rule.Pattern.C.criterion1} = ptolemy.actor.TypedIOPort {.Match4ActorsSubclasses_succ1.AbsoluteValue.input}, ptolemy.actor.gt.PortMatcher {.Match4ActorsSubclasses_rule.Pattern.C.criterion2} = ptolemy.actor.TypedIOPort {.Match4ActorsSubclasses_succ1.AbsoluteValue.output}, ptolemy.actor.gt.PortMatcher {.Match4ActorsSubclasses_rule.Pattern.D.criterion1} = ptolemy.actor.TypedIOPort {.Match4ActorsSubclasses_succ1.Display2.input}}}

test Match4ActorsSubclasses.2 {Test Match4ActorsSubclasses_rule.xml with Match4ActorsSubclasses_fail2.xml} {
    set matchResult [java::call ptolemy.actor.gt.GraphMatcher match ${p}Match4ActorsSubclasses_rule.xml ${p}Match4ActorsSubclasses_fail2.xml]
    [$matchResult getMatchResult] toString
} {{}}

test Match4ActorsSubclasses.3 {Test Match4ActorsSubclasses_rule.xml with Match4ActorsSubclasses_succ3.xml} {
    set matchResult [java::call ptolemy.actor.gt.GraphMatcher match ${p}Match4ActorsSubclasses_rule.xml ${p}Match4ActorsSubclasses_succ3.xml]
    [$matchResult getMatchResult] toString
} {{ptolemy.actor.gt.AtomicActorMatcher {.Match4ActorsSubclasses_rule.Pattern.A} = ptolemy.actor.lib.BooleanSwitch {.Match4ActorsSubclasses_succ3.BooleanSwitch}, ptolemy.actor.gt.AtomicActorMatcher {.Match4ActorsSubclasses_rule.Pattern.B} = ptolemy.actor.lib.AddSubtract {.Match4ActorsSubclasses_succ3.AddSubtract}, ptolemy.actor.gt.AtomicActorMatcher {.Match4ActorsSubclasses_rule.Pattern.C} = ptolemy.actor.lib.AbsoluteValue {.Match4ActorsSubclasses_succ3.AbsoluteValue}, ptolemy.actor.gt.AtomicActorMatcher {.Match4ActorsSubclasses_rule.Pattern.D} = ptolemy.actor.lib.gui.Display {.Match4ActorsSubclasses_succ3.Display2}, ptolemy.actor.gt.Pattern {.Match4ActorsSubclasses_rule.Pattern} = ptolemy.actor.TypedCompositeActor {.Match4ActorsSubclasses_succ3}, ptolemy.actor.gt.PortMatcher {.Match4ActorsSubclasses_rule.Pattern.A.criterion1} = ptolemy.actor.TypedIOPort {.Match4ActorsSubclasses_succ3.BooleanSwitch.trueOutput}, ptolemy.actor.gt.PortMatcher {.Match4ActorsSubclasses_rule.Pattern.A.criterion2} = ptolemy.actor.TypedIOPort {.Match4ActorsSubclasses_succ3.BooleanSwitch.falseOutput}, ptolemy.actor.gt.PortMatcher {.Match4ActorsSubclasses_rule.Pattern.B.criterion1} = ptolemy.actor.TypedIOPort {.Match4ActorsSubclasses_succ3.AddSubtract.minus}, ptolemy.actor.gt.PortMatcher {.Match4ActorsSubclasses_rule.Pattern.B.criterion2} = ptolemy.actor.TypedIOPort {.Match4ActorsSubclasses_succ3.AddSubtract.output}, ptolemy.actor.gt.PortMatcher {.Match4ActorsSubclasses_rule.Pattern.C.criterion1} = ptolemy.actor.TypedIOPort {.Match4ActorsSubclasses_succ3.AbsoluteValue.input}, ptolemy.actor.gt.PortMatcher {.Match4ActorsSubclasses_rule.Pattern.C.criterion2} = ptolemy.actor.TypedIOPort {.Match4ActorsSubclasses_succ3.AbsoluteValue.output}, ptolemy.actor.gt.PortMatcher {.Match4ActorsSubclasses_rule.Pattern.D.criterion1} = ptolemy.actor.TypedIOPort {.Match4ActorsSubclasses_succ3.Display2.input}}}
