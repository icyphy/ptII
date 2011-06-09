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
test Match3Actors.1 {Test Match3Actors_rule.xml with Match3Actors_succ1.xml} {
    set matchResult [java::call ptolemy.actor.gt.GraphMatcher match ${p}Match3Actors_rule.xml ${p}Match3Actors_succ1.xml]
    [$matchResult getMatchResult] toString
} {{ptolemy.actor.gt.AtomicActorMatcher {.Match3Actors_rule.Pattern.A} = ptolemy.actor.lib.Const {.Match3Actors_succ1.A}, ptolemy.actor.gt.AtomicActorMatcher {.Match3Actors_rule.Pattern.B} = ptolemy.actor.lib.Const {.Match3Actors_succ1.B}, ptolemy.actor.gt.AtomicActorMatcher {.Match3Actors_rule.Pattern.C} = ptolemy.actor.lib.gui.Display {.Match3Actors_succ1.C}, ptolemy.actor.gt.Pattern {.Match3Actors_rule.Pattern} = ptolemy.actor.TypedCompositeActor {.Match3Actors_succ1}, ptolemy.actor.gt.PortMatcher {.Match3Actors_rule.Pattern.A.criterion1} = ptolemy.actor.TypedIOPort {.Match3Actors_succ1.A.output}, ptolemy.actor.gt.PortMatcher {.Match3Actors_rule.Pattern.B.criterion1} = ptolemy.actor.TypedIOPort {.Match3Actors_succ1.B.output}, ptolemy.actor.gt.PortMatcher {.Match3Actors_rule.Pattern.C.criterion1} = ptolemy.actor.TypedIOPort {.Match3Actors_succ1.C.input}}}

test Match3Actors.2 {Test Match3Actors_rule.xml with Match3Actors_fail2.xml} {
    set matchResult [java::call ptolemy.actor.gt.GraphMatcher match ${p}Match3Actors_rule.xml ${p}Match3Actors_fail2.xml]
    [$matchResult getMatchResult] toString
} {{}}

test Match3Actors.3 {Test Match3Actors_rule.xml with Match3Actors_succ3.xml} {
    set matchResult [java::call ptolemy.actor.gt.GraphMatcher match ${p}Match3Actors_rule.xml ${p}Match3Actors_succ3.xml]
    [$matchResult getMatchResult] toString
} {{ptolemy.actor.gt.AtomicActorMatcher {.Match3Actors_rule.Pattern.A} = ptolemy.actor.lib.Const {.Match3Actors_succ3.A}, ptolemy.actor.gt.AtomicActorMatcher {.Match3Actors_rule.Pattern.B} = ptolemy.actor.lib.Const {.Match3Actors_succ3.B}, ptolemy.actor.gt.AtomicActorMatcher {.Match3Actors_rule.Pattern.C} = ptolemy.actor.lib.gui.Display {.Match3Actors_succ3.CompositeActor.C}, ptolemy.actor.gt.Pattern {.Match3Actors_rule.Pattern} = ptolemy.actor.TypedCompositeActor {.Match3Actors_succ3}, ptolemy.actor.gt.PortMatcher {.Match3Actors_rule.Pattern.A.criterion1} = ptolemy.actor.TypedIOPort {.Match3Actors_succ3.A.output}, ptolemy.actor.gt.PortMatcher {.Match3Actors_rule.Pattern.B.criterion1} = ptolemy.actor.TypedIOPort {.Match3Actors_succ3.B.output}, ptolemy.actor.gt.PortMatcher {.Match3Actors_rule.Pattern.C.criterion1} = ptolemy.actor.TypedIOPort {.Match3Actors_succ3.CompositeActor.C.input}}}

test Match3Actors.4 {Test Match3Actors_rule.xml with Match3Actors_succ4.xml} {
    set matchResult [java::call ptolemy.actor.gt.GraphMatcher match ${p}Match3Actors_rule.xml ${p}Match3Actors_succ4.xml]
    [$matchResult getMatchResult] toString
} {{ptolemy.actor.gt.AtomicActorMatcher {.Match3Actors_rule.Pattern.A} = ptolemy.actor.lib.Const {.Match3Actors_succ4.CompositeActor.A}, ptolemy.actor.gt.AtomicActorMatcher {.Match3Actors_rule.Pattern.B} = ptolemy.actor.lib.Const {.Match3Actors_succ4.CompositeActor.B}, ptolemy.actor.gt.AtomicActorMatcher {.Match3Actors_rule.Pattern.C} = ptolemy.actor.lib.gui.Display {.Match3Actors_succ4.C}, ptolemy.actor.gt.Pattern {.Match3Actors_rule.Pattern} = ptolemy.actor.TypedCompositeActor {.Match3Actors_succ4}, ptolemy.actor.gt.PortMatcher {.Match3Actors_rule.Pattern.A.criterion1} = ptolemy.actor.TypedIOPort {.Match3Actors_succ4.CompositeActor.A.output}, ptolemy.actor.gt.PortMatcher {.Match3Actors_rule.Pattern.B.criterion1} = ptolemy.actor.TypedIOPort {.Match3Actors_succ4.CompositeActor.B.output}, ptolemy.actor.gt.PortMatcher {.Match3Actors_rule.Pattern.C.criterion1} = ptolemy.actor.TypedIOPort {.Match3Actors_succ4.C.input}}}

test Match3Actors.5 {Test Match3Actors_rule.xml with Match3Actors_succ5.xml} {
    set matchResult [java::call ptolemy.actor.gt.GraphMatcher match ${p}Match3Actors_rule.xml ${p}Match3Actors_succ5.xml]
    [$matchResult getMatchResult] toString
} {{ptolemy.actor.gt.AtomicActorMatcher {.Match3Actors_rule.Pattern.A} = ptolemy.actor.lib.Const {.Match3Actors_succ5.CompositeActor.A}, ptolemy.actor.gt.AtomicActorMatcher {.Match3Actors_rule.Pattern.B} = ptolemy.actor.lib.Const {.Match3Actors_succ5.CompositeActor.B}, ptolemy.actor.gt.AtomicActorMatcher {.Match3Actors_rule.Pattern.C} = ptolemy.actor.lib.gui.Display {.Match3Actors_succ5.CompositeActor2.C}, ptolemy.actor.gt.Pattern {.Match3Actors_rule.Pattern} = ptolemy.actor.TypedCompositeActor {.Match3Actors_succ5}, ptolemy.actor.gt.PortMatcher {.Match3Actors_rule.Pattern.A.criterion1} = ptolemy.actor.TypedIOPort {.Match3Actors_succ5.CompositeActor.A.output}, ptolemy.actor.gt.PortMatcher {.Match3Actors_rule.Pattern.B.criterion1} = ptolemy.actor.TypedIOPort {.Match3Actors_succ5.CompositeActor.B.output}, ptolemy.actor.gt.PortMatcher {.Match3Actors_rule.Pattern.C.criterion1} = ptolemy.actor.TypedIOPort {.Match3Actors_succ5.CompositeActor2.C.input}}}

test Match3Actors.6 {Test Match3Actors_rule.xml with Match3Actors_fail6.xml} {
    set matchResult [java::call ptolemy.actor.gt.GraphMatcher match ${p}Match3Actors_rule.xml ${p}Match3Actors_fail6.xml]
    [$matchResult getMatchResult] toString
} {{}}

test Match3Actors.7 {Test Match3Actors_rule.xml with Match3Actors_succ7.xml} {
    set matchResult [java::call ptolemy.actor.gt.GraphMatcher match ${p}Match3Actors_rule.xml ${p}Match3Actors_succ7.xml]
    [$matchResult getMatchResult] toString
} {{ptolemy.actor.gt.AtomicActorMatcher {.Match3Actors_rule.Pattern.A} = ptolemy.actor.lib.Const {.Match3Actors_succ7.CompositeActor.A}, ptolemy.actor.gt.AtomicActorMatcher {.Match3Actors_rule.Pattern.B} = ptolemy.actor.lib.Const {.Match3Actors_succ7.CompositeActor.B}, ptolemy.actor.gt.AtomicActorMatcher {.Match3Actors_rule.Pattern.C} = ptolemy.actor.lib.gui.Display {.Match3Actors_succ7.C}, ptolemy.actor.gt.Pattern {.Match3Actors_rule.Pattern} = ptolemy.actor.TypedCompositeActor {.Match3Actors_succ7}, ptolemy.actor.gt.PortMatcher {.Match3Actors_rule.Pattern.A.criterion1} = ptolemy.actor.TypedIOPort {.Match3Actors_succ7.CompositeActor.A.output}, ptolemy.actor.gt.PortMatcher {.Match3Actors_rule.Pattern.B.criterion1} = ptolemy.actor.TypedIOPort {.Match3Actors_succ7.CompositeActor.B.output}, ptolemy.actor.gt.PortMatcher {.Match3Actors_rule.Pattern.C.criterion1} = ptolemy.actor.TypedIOPort {.Match3Actors_succ7.C.input}}}

test Match3Actors.8 {Test Match3Actors_rule.xml with Match3Actors_succ8} {
    set matchResult [java::call ptolemy.actor.gt.GraphMatcher match ${p}Match3Actors_rule.xml ${p}Match3Actors_succ8.xml]
    [$matchResult getMatchResult] toString
} {{ptolemy.actor.gt.AtomicActorMatcher {.Match3Actors_rule.Pattern.A} = ptolemy.actor.lib.Const {.Match3Actors_succ8.CompositeActor.A}, ptolemy.actor.gt.AtomicActorMatcher {.Match3Actors_rule.Pattern.B} = ptolemy.actor.lib.Const {.Match3Actors_succ8.CompositeActor.B}, ptolemy.actor.gt.AtomicActorMatcher {.Match3Actors_rule.Pattern.C} = ptolemy.actor.lib.gui.Display {.Match3Actors_succ8.C}, ptolemy.actor.gt.Pattern {.Match3Actors_rule.Pattern} = ptolemy.actor.TypedCompositeActor {.Match3Actors_succ8}, ptolemy.actor.gt.PortMatcher {.Match3Actors_rule.Pattern.A.criterion1} = ptolemy.actor.TypedIOPort {.Match3Actors_succ8.CompositeActor.A.output}, ptolemy.actor.gt.PortMatcher {.Match3Actors_rule.Pattern.B.criterion1} = ptolemy.actor.TypedIOPort {.Match3Actors_succ8.CompositeActor.B.output}, ptolemy.actor.gt.PortMatcher {.Match3Actors_rule.Pattern.C.criterion1} = ptolemy.actor.TypedIOPort {.Match3Actors_succ8.C.input}}}
