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
test XMLTest-1.1 {Test rule1.xml with host1_1.xml} {
    set matchResult [java::call ptolemy.actor.gt.GraphMatcher match rule1.xml host1_1.xml]
    [$matchResult getMatchResult] toString
} {{ptolemy.actor.TypedIOPort {.rule1.Pattern.A.Criterion1} = ptolemy.actor.TypedIOPort {.host1.A.output}, ptolemy.actor.TypedIOPort {.rule1.Pattern.B.Criterion1} = ptolemy.actor.TypedIOPort {.host1.B.output}, ptolemy.actor.TypedIOPort {.rule1.Pattern.C.Criterion1} = ptolemy.actor.TypedIOPort {.host1.C.input}, ptolemy.actor.gt.AtomicActorMatcher {.rule1.Pattern.A} = ptolemy.actor.lib.Const {.host1.A}, ptolemy.actor.gt.AtomicActorMatcher {.rule1.Pattern.B} = ptolemy.actor.lib.Const {.host1.B}, ptolemy.actor.gt.AtomicActorMatcher {.rule1.Pattern.C} = ptolemy.actor.lib.gui.Display {.host1.C}, ptolemy.actor.gt.Pattern {.rule1.Pattern} = ptolemy.actor.TypedCompositeActor {.host1}}}

test XMLTest-1.2 {Test rule1.xml with host1_2.xml} {
    set matchResult [java::call ptolemy.actor.gt.GraphMatcher match rule1.xml host1_2.xml]
    [$matchResult getMatchResult] toString
} {{}}

test XMLTest-1.3 {Test rule1.xml with host1_3.xml} {
    set matchResult [java::call ptolemy.actor.gt.GraphMatcher match rule1.xml host1_3.xml]
    [$matchResult getMatchResult] toString
} {{ptolemy.actor.TypedIOPort {.rule1.Pattern.A.Criterion1} = ptolemy.actor.TypedIOPort {.model.A.output}, ptolemy.actor.TypedIOPort {.rule1.Pattern.B.Criterion1} = ptolemy.actor.TypedIOPort {.model.B.output}, ptolemy.actor.TypedIOPort {.rule1.Pattern.C.Criterion1} = ptolemy.actor.TypedIOPort {.model.CompositeActor.C.input}, ptolemy.actor.gt.AtomicActorMatcher {.rule1.Pattern.A} = ptolemy.actor.lib.Const {.model.A}, ptolemy.actor.gt.AtomicActorMatcher {.rule1.Pattern.B} = ptolemy.actor.lib.Const {.model.B}, ptolemy.actor.gt.AtomicActorMatcher {.rule1.Pattern.C} = ptolemy.actor.lib.gui.Display {.model.CompositeActor.C}, ptolemy.actor.gt.Pattern {.rule1.Pattern} = ptolemy.actor.TypedCompositeActor {.model}}}

test XMLTest-1.4 {Test rule1.xml with host1_4.xml} {
    set matchResult [java::call ptolemy.actor.gt.GraphMatcher match rule1.xml host1_4.xml]
    [$matchResult getMatchResult] toString
} {{ptolemy.actor.TypedIOPort {.rule1.Pattern.A.Criterion1} = ptolemy.actor.TypedIOPort {.host1.CompositeActor.A.output}, ptolemy.actor.TypedIOPort {.rule1.Pattern.B.Criterion1} = ptolemy.actor.TypedIOPort {.host1.CompositeActor.B.output}, ptolemy.actor.TypedIOPort {.rule1.Pattern.C.Criterion1} = ptolemy.actor.TypedIOPort {.host1.C.input}, ptolemy.actor.gt.AtomicActorMatcher {.rule1.Pattern.A} = ptolemy.actor.lib.Const {.host1.CompositeActor.A}, ptolemy.actor.gt.AtomicActorMatcher {.rule1.Pattern.B} = ptolemy.actor.lib.Const {.host1.CompositeActor.B}, ptolemy.actor.gt.AtomicActorMatcher {.rule1.Pattern.C} = ptolemy.actor.lib.gui.Display {.host1.C}, ptolemy.actor.gt.Pattern {.rule1.Pattern} = ptolemy.actor.TypedCompositeActor {.host1}}}

test XMLTest-1.5 {Test rule1.xml with host1_5.xml} {
    set matchResult [java::call ptolemy.actor.gt.GraphMatcher match rule1.xml host1_5.xml]
    [$matchResult getMatchResult] toString
} {{ptolemy.actor.TypedIOPort {.rule1.Pattern.A.Criterion1} = ptolemy.actor.TypedIOPort {.host1.CompositeActor.A.output}, ptolemy.actor.TypedIOPort {.rule1.Pattern.B.Criterion1} = ptolemy.actor.TypedIOPort {.host1.CompositeActor.B.output}, ptolemy.actor.TypedIOPort {.rule1.Pattern.C.Criterion1} = ptolemy.actor.TypedIOPort {.host1.CompositeActor2.C.input}, ptolemy.actor.gt.AtomicActorMatcher {.rule1.Pattern.A} = ptolemy.actor.lib.Const {.host1.CompositeActor.A}, ptolemy.actor.gt.AtomicActorMatcher {.rule1.Pattern.B} = ptolemy.actor.lib.Const {.host1.CompositeActor.B}, ptolemy.actor.gt.AtomicActorMatcher {.rule1.Pattern.C} = ptolemy.actor.lib.gui.Display {.host1.CompositeActor2.C}, ptolemy.actor.gt.Pattern {.rule1.Pattern} = ptolemy.actor.TypedCompositeActor {.host1}}}

test XMLTest-1.6 {Test rule1.xml with host1_6.xml} {
    set matchResult [java::call ptolemy.actor.gt.GraphMatcher match rule1.xml host1_6.xml]
    [$matchResult getMatchResult] toString
} {{}}

test XMLTest-1.7 {Test rule1.xml with host1_7.xml} {
    set matchResult [java::call ptolemy.actor.gt.GraphMatcher match rule1.xml host1_7.xml]
    [$matchResult getMatchResult] toString
} {{ptolemy.actor.TypedIOPort {.rule1.Pattern.A.Criterion1} = ptolemy.actor.TypedIOPort {.host1.CompositeActor.A.output}, ptolemy.actor.TypedIOPort {.rule1.Pattern.B.Criterion1} = ptolemy.actor.TypedIOPort {.host1.CompositeActor.B.output}, ptolemy.actor.TypedIOPort {.rule1.Pattern.C.Criterion1} = ptolemy.actor.TypedIOPort {.host1.C.input}, ptolemy.actor.gt.AtomicActorMatcher {.rule1.Pattern.A} = ptolemy.actor.lib.Const {.host1.CompositeActor.A}, ptolemy.actor.gt.AtomicActorMatcher {.rule1.Pattern.B} = ptolemy.actor.lib.Const {.host1.CompositeActor.B}, ptolemy.actor.gt.AtomicActorMatcher {.rule1.Pattern.C} = ptolemy.actor.lib.gui.Display {.host1.C}, ptolemy.actor.gt.Pattern {.rule1.Pattern} = ptolemy.actor.TypedCompositeActor {.host1}}}

test XMLTest-1.8 {Test rule1.xml with host1.8_xml} {
    set matchResult [java::call ptolemy.actor.gt.GraphMatcher match rule1.xml host1_8.xml]
    [$matchResult getMatchResult] toString
} {{ptolemy.actor.TypedIOPort {.rule1.Pattern.A.Criterion1} = ptolemy.actor.TypedIOPort {.host1.CompositeActor.A.output}, ptolemy.actor.TypedIOPort {.rule1.Pattern.B.Criterion1} = ptolemy.actor.TypedIOPort {.host1.CompositeActor.B.output}, ptolemy.actor.TypedIOPort {.rule1.Pattern.C.Criterion1} = ptolemy.actor.TypedIOPort {.host1.C.input}, ptolemy.actor.gt.AtomicActorMatcher {.rule1.Pattern.A} = ptolemy.actor.lib.Const {.host1.CompositeActor.A}, ptolemy.actor.gt.AtomicActorMatcher {.rule1.Pattern.B} = ptolemy.actor.lib.Const {.host1.CompositeActor.B}, ptolemy.actor.gt.AtomicActorMatcher {.rule1.Pattern.C} = ptolemy.actor.lib.gui.Display {.host1.C}, ptolemy.actor.gt.Pattern {.rule1.Pattern} = ptolemy.actor.TypedCompositeActor {.host1}}}
