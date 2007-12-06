# Test DepthFirstTransformer.
#
# @Author: Thomas Huining Feng
#
# @Version: $Id$
#
# @Copyright (c) 1997-2007 The Regents of the University of California.
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
test XMLTest-6.1 {Test rule6.xml with host6_1.xml} {
    set matchResult [java::call ptolemy.actor.gt.GraphMatcher match rule6.xml host6_1.xml]
    [$matchResult getMatchResult] toString
} {{ptolemy.actor.TypedIOPort {.rule6.Pattern.CompositeActorMatcher.AtomicActorMatcher.criterion1} = ptolemy.actor.TypedIOPort {.host6.CompositeActor2.Const.output}, ptolemy.actor.TypedIOPort {.rule6.Pattern.CompositeActorMatcher.port} = ptolemy.actor.TypedIOPort {.host6.CompositeActor2.port}, ptolemy.actor.TypedIOPort {.rule6.Pattern.CompositeActorMatcher2.AtomicActorMatcher.criterion1} = ptolemy.actor.TypedIOPort {.host6.CompositeActor.Const.trigger}, ptolemy.actor.TypedIOPort {.rule6.Pattern.CompositeActorMatcher2.CompositeActorMatcher.AtomicActorMatcher.criterion1} = ptolemy.actor.TypedIOPort {.host6.CompositeActor.Const2.trigger}, ptolemy.actor.TypedIOPort {.rule6.Pattern.CompositeActorMatcher2.port} = ptolemy.actor.TypedIOPort {.host6.CompositeActor.port}, ptolemy.actor.gt.AtomicActorMatcher {.rule6.Pattern.CompositeActorMatcher.AtomicActorMatcher} = ptolemy.actor.lib.Const {.host6.CompositeActor2.Const}, ptolemy.actor.gt.AtomicActorMatcher {.rule6.Pattern.CompositeActorMatcher2.AtomicActorMatcher} = ptolemy.actor.lib.Const {.host6.CompositeActor.Const}, ptolemy.actor.gt.AtomicActorMatcher {.rule6.Pattern.CompositeActorMatcher2.CompositeActorMatcher.AtomicActorMatcher} = ptolemy.actor.lib.Const {.host6.CompositeActor.Const2}, ptolemy.actor.gt.CompositeActorMatcher {.rule6.Pattern.CompositeActorMatcher2} = ptolemy.actor.TypedCompositeActor {.host6.CompositeActor}, ptolemy.actor.gt.CompositeActorMatcher {.rule6.Pattern.CompositeActorMatcher} = ptolemy.actor.TypedCompositeActor {.host6.CompositeActor2}, ptolemy.actor.gt.Pattern {.rule6.Pattern} = ptolemy.actor.TypedCompositeActor {.host6}}}

test XMLTest-6.2 {Test rule6.xml with host6_2.xml} {
    set matchResult [java::call ptolemy.actor.gt.GraphMatcher match rule6.xml host6_2.xml]
    [$matchResult getMatchResult] toString
} {{ptolemy.actor.TypedIOPort {.rule6.Pattern.CompositeActorMatcher.AtomicActorMatcher.criterion1} = ptolemy.actor.TypedIOPort {.host6.CompositeActor2.Const.output}, ptolemy.actor.TypedIOPort {.rule6.Pattern.CompositeActorMatcher.port} = ptolemy.actor.TypedIOPort {.host6.CompositeActor2.port}, ptolemy.actor.TypedIOPort {.rule6.Pattern.CompositeActorMatcher2.AtomicActorMatcher.criterion1} = ptolemy.actor.TypedIOPort {.host6.CompositeActor.Const.trigger}, ptolemy.actor.TypedIOPort {.rule6.Pattern.CompositeActorMatcher2.CompositeActorMatcher.AtomicActorMatcher.criterion1} = ptolemy.actor.TypedIOPort {.host6.CompositeActor.CompositeActor.Const.trigger}, ptolemy.actor.TypedIOPort {.rule6.Pattern.CompositeActorMatcher2.port} = ptolemy.actor.TypedIOPort {.host6.CompositeActor.port}, ptolemy.actor.gt.AtomicActorMatcher {.rule6.Pattern.CompositeActorMatcher.AtomicActorMatcher} = ptolemy.actor.lib.Const {.host6.CompositeActor2.Const}, ptolemy.actor.gt.AtomicActorMatcher {.rule6.Pattern.CompositeActorMatcher2.AtomicActorMatcher} = ptolemy.actor.lib.Const {.host6.CompositeActor.Const}, ptolemy.actor.gt.AtomicActorMatcher {.rule6.Pattern.CompositeActorMatcher2.CompositeActorMatcher.AtomicActorMatcher} = ptolemy.actor.lib.Const {.host6.CompositeActor.CompositeActor.Const}, ptolemy.actor.gt.CompositeActorMatcher {.rule6.Pattern.CompositeActorMatcher2} = ptolemy.actor.TypedCompositeActor {.host6.CompositeActor}, ptolemy.actor.gt.CompositeActorMatcher {.rule6.Pattern.CompositeActorMatcher} = ptolemy.actor.TypedCompositeActor {.host6.CompositeActor2}, ptolemy.actor.gt.Pattern {.rule6.Pattern} = ptolemy.actor.TypedCompositeActor {.host6}}}
