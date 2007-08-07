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
test XMLTest-1.1 {Test rule1.xml with host1.1.xml} {
    set matchResult [java::call ptolemy.actor.gt.RecursiveGraphMatcher match rule1.xml host1.1.xml]
    [$matchResult getMatchResult] toString
} {{.Rule.Left Hand Side.A.a:[.Rule.Left Hand Side.relation, .Rule.Left Hand Side.A.a] = .host1.A.output:[.host1.relation, .host1.A.output], .Rule.Left Hand Side.A.a:[.Rule.Left Hand Side.relation, .Rule.Left Hand Side.C.c] = .host1.A.output:[.host1.relation, .host1.C.input], .Rule.Left Hand Side.B.b:[.Rule.Left Hand Side.relation2, .Rule.Left Hand Side.B.b] = .host1.B.output:[.host1.relation2, .host1.B.output], .Rule.Left Hand Side.B.b:[.Rule.Left Hand Side.relation2, .Rule.Left Hand Side.C.c] = .host1.B.output:[.host1.relation2, .host1.C.input], .Rule.Left Hand Side.C.c:[.Rule.Left Hand Side.relation, .Rule.Left Hand Side.A.a] = .host1.C.input:[.host1.relation, .host1.A.output], .Rule.Left Hand Side.C.c:[.Rule.Left Hand Side.relation, .Rule.Left Hand Side.C.c] = .host1.C.input:[.host1.relation, .host1.C.input], .Rule.Left Hand Side.C.c:[.Rule.Left Hand Side.relation2, .Rule.Left Hand Side.B.b] = .host1.C.input:[.host1.relation2, .host1.B.output], ptolemy.actor.TypedIOPort {.Rule.Left Hand Side.A.a} = ptolemy.actor.TypedIOPort {.host1.A.output}, ptolemy.actor.TypedIOPort {.Rule.Left Hand Side.B.b} = ptolemy.actor.TypedIOPort {.host1.B.output}, ptolemy.actor.TypedIOPort {.Rule.Left Hand Side.C.c} = ptolemy.actor.TypedIOPort {.host1.C.input}, ptolemy.actor.gt.AtomicActorMatcher {.Rule.Left Hand Side.A} = ptolemy.actor.lib.Const {.host1.A}, ptolemy.actor.gt.AtomicActorMatcher {.Rule.Left Hand Side.B} = ptolemy.actor.lib.Const {.host1.B}, ptolemy.actor.gt.AtomicActorMatcher {.Rule.Left Hand Side.C} = ptolemy.actor.lib.gui.Display {.host1.C}, ptolemy.actor.gt.CompositeActorMatcher {.Rule.Left Hand Side} = ptolemy.actor.TypedCompositeActor {.host1}}}

test XMLTest-1.2 {Test rule1.xml with host1.2.xml} {
    set matchResult [java::call ptolemy.actor.gt.RecursiveGraphMatcher match rule1.xml host1.2.xml]
    [$matchResult getMatchResult] toString
} {{}}

test XMLTest-1.3 {Test rule1.xml with host1.3.xml} {
    set matchResult [java::call ptolemy.actor.gt.RecursiveGraphMatcher match rule1.xml host1.3.xml]
    [$matchResult getMatchResult] toString
} {{.Rule.Left Hand Side.A.a:[.Rule.Left Hand Side.relation, .Rule.Left Hand Side.A.a] = .model.A.output:[.model.relation, .model.A.output], .Rule.Left Hand Side.A.a:[.Rule.Left Hand Side.relation, .Rule.Left Hand Side.C.c] = .model.A.output:[.model.relation, .model.CompositeActor.compositeInput, .model.CompositeActor.relation, .model.CompositeActor.C.input], .Rule.Left Hand Side.B.b:[.Rule.Left Hand Side.relation2, .Rule.Left Hand Side.B.b] = .model.B.output:[.model.relation2, .model.B.output], .Rule.Left Hand Side.B.b:[.Rule.Left Hand Side.relation2, .Rule.Left Hand Side.C.c] = .model.B.output:[.model.relation2, .model.CompositeActor.compositeInput, .model.CompositeActor.relation, .model.CompositeActor.C.input], .Rule.Left Hand Side.C.c:[.Rule.Left Hand Side.relation, .Rule.Left Hand Side.A.a] = .model.CompositeActor.C.input:[.model.CompositeActor.relation, .model.CompositeActor.compositeInput, .model.relation, .model.A.output], .Rule.Left Hand Side.C.c:[.Rule.Left Hand Side.relation, .Rule.Left Hand Side.C.c] = .model.CompositeActor.C.input:[.model.CompositeActor.relation, .model.CompositeActor.C.input], .Rule.Left Hand Side.C.c:[.Rule.Left Hand Side.relation2, .Rule.Left Hand Side.B.b] = .model.CompositeActor.C.input:[.model.CompositeActor.relation, .model.CompositeActor.compositeInput, .model.relation2, .model.B.output], ptolemy.actor.TypedIOPort {.Rule.Left Hand Side.A.a} = ptolemy.actor.TypedIOPort {.model.A.output}, ptolemy.actor.TypedIOPort {.Rule.Left Hand Side.B.b} = ptolemy.actor.TypedIOPort {.model.B.output}, ptolemy.actor.TypedIOPort {.Rule.Left Hand Side.C.c} = ptolemy.actor.TypedIOPort {.model.CompositeActor.C.input}, ptolemy.actor.gt.AtomicActorMatcher {.Rule.Left Hand Side.A} = ptolemy.actor.lib.Const {.model.A}, ptolemy.actor.gt.AtomicActorMatcher {.Rule.Left Hand Side.B} = ptolemy.actor.lib.Const {.model.B}, ptolemy.actor.gt.AtomicActorMatcher {.Rule.Left Hand Side.C} = ptolemy.actor.lib.gui.Display {.model.CompositeActor.C}, ptolemy.actor.gt.CompositeActorMatcher {.Rule.Left Hand Side} = ptolemy.actor.TypedCompositeActor {.model}}}

test XMLTest-1.4 {Test rule1.xml with host1.4.xml} {
    set matchResult [java::call ptolemy.actor.gt.RecursiveGraphMatcher match rule1.xml host1.4.xml]
    [$matchResult getMatchResult] toString
} {{.Rule.Left Hand Side.A.a:[.Rule.Left Hand Side.relation, .Rule.Left Hand Side.A.a] = .host1.CompositeActor.A.output:[.host1.CompositeActor.relation2, .host1.CompositeActor.A.output], .Rule.Left Hand Side.A.a:[.Rule.Left Hand Side.relation, .Rule.Left Hand Side.C.c] = .host1.CompositeActor.A.output:[.host1.CompositeActor.relation2, .host1.CompositeActor.output1, .host1.relation, .host1.C.input], .Rule.Left Hand Side.B.b:[.Rule.Left Hand Side.relation2, .Rule.Left Hand Side.B.b] = .host1.CompositeActor.B.output:[.host1.CompositeActor.relation, .host1.CompositeActor.B.output], .Rule.Left Hand Side.B.b:[.Rule.Left Hand Side.relation2, .Rule.Left Hand Side.C.c] = .host1.CompositeActor.B.output:[.host1.CompositeActor.relation, .host1.CompositeActor.output2, .host1.relation2, .host1.C.input], .Rule.Left Hand Side.C.c:[.Rule.Left Hand Side.relation, .Rule.Left Hand Side.A.a] = .host1.C.input:[.host1.relation, .host1.CompositeActor.output1, .host1.relation, .host1.CompositeActor.A.output], .Rule.Left Hand Side.C.c:[.Rule.Left Hand Side.relation, .Rule.Left Hand Side.C.c] = .host1.C.input:[.host1.relation, .host1.C.input], .Rule.Left Hand Side.C.c:[.Rule.Left Hand Side.relation2, .Rule.Left Hand Side.B.b] = .host1.C.input:[.host1.relation2, .host1.CompositeActor.output2, .host1.relation2, .host1.CompositeActor.B.output], ptolemy.actor.TypedIOPort {.Rule.Left Hand Side.A.a} = ptolemy.actor.TypedIOPort {.host1.CompositeActor.A.output}, ptolemy.actor.TypedIOPort {.Rule.Left Hand Side.B.b} = ptolemy.actor.TypedIOPort {.host1.CompositeActor.B.output}, ptolemy.actor.TypedIOPort {.Rule.Left Hand Side.C.c} = ptolemy.actor.TypedIOPort {.host1.C.input}, ptolemy.actor.gt.AtomicActorMatcher {.Rule.Left Hand Side.A} = ptolemy.actor.lib.Const {.host1.CompositeActor.A}, ptolemy.actor.gt.AtomicActorMatcher {.Rule.Left Hand Side.B} = ptolemy.actor.lib.Const {.host1.CompositeActor.B}, ptolemy.actor.gt.AtomicActorMatcher {.Rule.Left Hand Side.C} = ptolemy.actor.lib.gui.Display {.host1.C}, ptolemy.actor.gt.CompositeActorMatcher {.Rule.Left Hand Side} = ptolemy.actor.TypedCompositeActor {.host1}}}

test XMLTest-1.5 {Test rule1.xml with host1.5.xml} {
    set matchResult [java::call ptolemy.actor.gt.RecursiveGraphMatcher match rule1.xml host1.5.xml]
    [$matchResult getMatchResult] toString
} {{.Rule.Left Hand Side.A.a:[.Rule.Left Hand Side.relation, .Rule.Left Hand Side.A.a] = .host1.CompositeActor.A.output:[.host1.CompositeActor.relation2, .host1.CompositeActor.A.output], .Rule.Left Hand Side.A.a:[.Rule.Left Hand Side.relation, .Rule.Left Hand Side.C.c] = .host1.CompositeActor.A.output:[.host1.CompositeActor.relation2, .host1.CompositeActor.output1, .host1.relation, .host1.CompositeActor2.compositeInput, .host1.CompositeActor2.relation, .host1.CompositeActor2.C.input], .Rule.Left Hand Side.B.b:[.Rule.Left Hand Side.relation2, .Rule.Left Hand Side.B.b] = .host1.CompositeActor.B.output:[.host1.CompositeActor.relation, .host1.CompositeActor.B.output], .Rule.Left Hand Side.B.b:[.Rule.Left Hand Side.relation2, .Rule.Left Hand Side.C.c] = .host1.CompositeActor.B.output:[.host1.CompositeActor.relation, .host1.CompositeActor.output2, .host1.relation2, .host1.CompositeActor2.compositeInput, .host1.CompositeActor2.relation, .host1.CompositeActor2.C.input], .Rule.Left Hand Side.C.c:[.Rule.Left Hand Side.relation, .Rule.Left Hand Side.A.a] = .host1.CompositeActor2.C.input:[.host1.CompositeActor2.relation, .host1.CompositeActor2.compositeInput, .host1.relation, .host1.CompositeActor.output1, .host1.relation, .host1.CompositeActor.A.output], .Rule.Left Hand Side.C.c:[.Rule.Left Hand Side.relation, .Rule.Left Hand Side.C.c] = .host1.CompositeActor2.C.input:[.host1.CompositeActor2.relation, .host1.CompositeActor2.C.input], .Rule.Left Hand Side.C.c:[.Rule.Left Hand Side.relation2, .Rule.Left Hand Side.B.b] = .host1.CompositeActor2.C.input:[.host1.CompositeActor2.relation, .host1.CompositeActor2.compositeInput, .host1.relation2, .host1.CompositeActor.output2, .host1.relation2, .host1.CompositeActor.B.output], ptolemy.actor.TypedIOPort {.Rule.Left Hand Side.A.a} = ptolemy.actor.TypedIOPort {.host1.CompositeActor.A.output}, ptolemy.actor.TypedIOPort {.Rule.Left Hand Side.B.b} = ptolemy.actor.TypedIOPort {.host1.CompositeActor.B.output}, ptolemy.actor.TypedIOPort {.Rule.Left Hand Side.C.c} = ptolemy.actor.TypedIOPort {.host1.CompositeActor2.C.input}, ptolemy.actor.gt.AtomicActorMatcher {.Rule.Left Hand Side.A} = ptolemy.actor.lib.Const {.host1.CompositeActor.A}, ptolemy.actor.gt.AtomicActorMatcher {.Rule.Left Hand Side.B} = ptolemy.actor.lib.Const {.host1.CompositeActor.B}, ptolemy.actor.gt.AtomicActorMatcher {.Rule.Left Hand Side.C} = ptolemy.actor.lib.gui.Display {.host1.CompositeActor2.C}, ptolemy.actor.gt.CompositeActorMatcher {.Rule.Left Hand Side} = ptolemy.actor.TypedCompositeActor {.host1}}}

test XMLTest-1.6 {Test rule1.xml with host1.6.xml} {
    set matchResult [java::call ptolemy.actor.gt.RecursiveGraphMatcher match rule1.xml host1.6.xml]
    [$matchResult getMatchResult] toString
} {{}}

test XMLTest-1.7 {Test rule1.xml with host1.7.xml} {
    set matchResult [java::call ptolemy.actor.gt.RecursiveGraphMatcher match rule1.xml host1.7.xml]
    [$matchResult getMatchResult] toString
} {{.Rule.Left Hand Side.A.a:[.Rule.Left Hand Side.relation, .Rule.Left Hand Side.A.a] = .host1.CompositeActor.A.output:[.host1.CompositeActor.relation2, .host1.CompositeActor.A.output], .Rule.Left Hand Side.A.a:[.Rule.Left Hand Side.relation, .Rule.Left Hand Side.C.c] = .host1.CompositeActor.A.output:[.host1.CompositeActor.relation2, .host1.CompositeActor.output1, .host1.relation, .host1.C.input], .Rule.Left Hand Side.B.b:[.Rule.Left Hand Side.relation2, .Rule.Left Hand Side.B.b] = .host1.CompositeActor.B.output:[.host1.CompositeActor.relation, .host1.CompositeActor.B.output], .Rule.Left Hand Side.B.b:[.Rule.Left Hand Side.relation2, .Rule.Left Hand Side.C.c] = .host1.CompositeActor.B.output:[.host1.CompositeActor.relation, .host1.CompositeActor.output2, .host1.relation2, .host1.C.input], .Rule.Left Hand Side.C.c:[.Rule.Left Hand Side.relation, .Rule.Left Hand Side.A.a] = .host1.C.input:[.host1.relation, .host1.CompositeActor.output1, .host1.relation, .host1.CompositeActor.A.output], .Rule.Left Hand Side.C.c:[.Rule.Left Hand Side.relation, .Rule.Left Hand Side.C.c] = .host1.C.input:[.host1.relation, .host1.C.input], .Rule.Left Hand Side.C.c:[.Rule.Left Hand Side.relation2, .Rule.Left Hand Side.B.b] = .host1.C.input:[.host1.relation2, .host1.CompositeActor.output2, .host1.relation2, .host1.CompositeActor.B.output], ptolemy.actor.TypedIOPort {.Rule.Left Hand Side.A.a} = ptolemy.actor.TypedIOPort {.host1.CompositeActor.A.output}, ptolemy.actor.TypedIOPort {.Rule.Left Hand Side.B.b} = ptolemy.actor.TypedIOPort {.host1.CompositeActor.B.output}, ptolemy.actor.TypedIOPort {.Rule.Left Hand Side.C.c} = ptolemy.actor.TypedIOPort {.host1.C.input}, ptolemy.actor.gt.AtomicActorMatcher {.Rule.Left Hand Side.A} = ptolemy.actor.lib.Const {.host1.CompositeActor.A}, ptolemy.actor.gt.AtomicActorMatcher {.Rule.Left Hand Side.B} = ptolemy.actor.lib.Const {.host1.CompositeActor.B}, ptolemy.actor.gt.AtomicActorMatcher {.Rule.Left Hand Side.C} = ptolemy.actor.lib.gui.Display {.host1.C}, ptolemy.actor.gt.CompositeActorMatcher {.Rule.Left Hand Side} = ptolemy.actor.TypedCompositeActor {.host1}}}

test XMLTest-1.8 {Test rule1.xml with host1.8.xml} {
    set matchResult [java::call ptolemy.actor.gt.RecursiveGraphMatcher match rule1.xml host1.8.xml]
    [$matchResult getMatchResult] toString
} {{.Rule.Left Hand Side.A.a:[.Rule.Left Hand Side.relation, .Rule.Left Hand Side.A.a] = .host1.CompositeActor.A.output:[.host1.CompositeActor.relation2, .host1.CompositeActor.A.output], .Rule.Left Hand Side.A.a:[.Rule.Left Hand Side.relation, .Rule.Left Hand Side.C.c] = .host1.CompositeActor.A.output:[.host1.CompositeActor.relation2, .host1.CompositeActor.output1, .host1.relation, .host1.C.input], .Rule.Left Hand Side.B.b:[.Rule.Left Hand Side.relation2, .Rule.Left Hand Side.B.b] = .host1.CompositeActor.B.output:[.host1.CompositeActor.relation, .host1.CompositeActor.B.output], .Rule.Left Hand Side.B.b:[.Rule.Left Hand Side.relation2, .Rule.Left Hand Side.C.c] = .host1.CompositeActor.B.output:[.host1.CompositeActor.relation, .host1.CompositeActor.output2, .host1.relation2, .host1.C.input], .Rule.Left Hand Side.C.c:[.Rule.Left Hand Side.relation, .Rule.Left Hand Side.A.a] = .host1.C.input:[.host1.relation, .host1.CompositeActor.output1, .host1.CompositeActor.relation2, .host1.CompositeActor.A.output], .Rule.Left Hand Side.C.c:[.Rule.Left Hand Side.relation, .Rule.Left Hand Side.C.c] = .host1.C.input:[.host1.relation, .host1.C.input], .Rule.Left Hand Side.C.c:[.Rule.Left Hand Side.relation2, .Rule.Left Hand Side.B.b] = .host1.C.input:[.host1.relation2, .host1.CompositeActor.output2, .host1.relation2, .host1.CompositeActor.B.output], ptolemy.actor.TypedIOPort {.Rule.Left Hand Side.A.a} = ptolemy.actor.TypedIOPort {.host1.CompositeActor.A.output}, ptolemy.actor.TypedIOPort {.Rule.Left Hand Side.B.b} = ptolemy.actor.TypedIOPort {.host1.CompositeActor.B.output}, ptolemy.actor.TypedIOPort {.Rule.Left Hand Side.C.c} = ptolemy.actor.TypedIOPort {.host1.C.input}, ptolemy.actor.gt.AtomicActorMatcher {.Rule.Left Hand Side.A} = ptolemy.actor.lib.Const {.host1.CompositeActor.A}, ptolemy.actor.gt.AtomicActorMatcher {.Rule.Left Hand Side.B} = ptolemy.actor.lib.Const {.host1.CompositeActor.B}, ptolemy.actor.gt.AtomicActorMatcher {.Rule.Left Hand Side.C} = ptolemy.actor.lib.gui.Display {.host1.C}, ptolemy.actor.gt.CompositeActorMatcher {.Rule.Left Hand Side} = ptolemy.actor.TypedCompositeActor {.host1}}}
