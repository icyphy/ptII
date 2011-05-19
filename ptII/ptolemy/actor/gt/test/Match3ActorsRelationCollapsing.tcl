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
test Match3ActorsRelationCollapsing.1 {Test Match3ActorsRelationCollapsing_rule.xml with Match3ActorsRelationCollapsing_succ1.xml} {
    set matchResult [java::call ptolemy.actor.gt.GraphMatcher match ${p}Match3ActorsRelationCollapsing_rule.xml ${p}Match3ActorsRelationCollapsing_succ1.xml]
    [$matchResult getMatchResult] toString
} {{ptolemy.actor.TypedIOPort {.Match3ActorsRelationCollapsing_rule.Pattern.CompositeActorMatcher.port} = ptolemy.actor.TypedIOPort {.Match3ActorsRelationCollapsing_succ1.CompositeActor.port}, ptolemy.actor.TypedIORelation {.Match3ActorsRelationCollapsing_rule.Pattern.CompositeActorMatcher.relation} = ptolemy.actor.TypedIORelation {.Match3ActorsRelationCollapsing_succ1.CompositeActor.relation}, ptolemy.actor.TypedIORelation {.Match3ActorsRelationCollapsing_rule.Pattern.relation2} = ptolemy.actor.TypedIORelation {.Match3ActorsRelationCollapsing_succ1.relation3}, ptolemy.actor.TypedIORelation {.Match3ActorsRelationCollapsing_rule.Pattern.relation3} = ptolemy.actor.TypedIORelation {.Match3ActorsRelationCollapsing_succ1.relation4}, ptolemy.actor.TypedIORelation {.Match3ActorsRelationCollapsing_rule.Pattern.relation} = ptolemy.actor.TypedIORelation {.Match3ActorsRelationCollapsing_succ1.relation}, ptolemy.actor.gt.AtomicActorMatcher {.Match3ActorsRelationCollapsing_rule.Pattern.AtomicActorMatcher2} = ptolemy.actor.lib.Const {.Match3ActorsRelationCollapsing_succ1.Const2}, ptolemy.actor.gt.AtomicActorMatcher {.Match3ActorsRelationCollapsing_rule.Pattern.AtomicActorMatcher} = ptolemy.actor.lib.Const {.Match3ActorsRelationCollapsing_succ1.Const}, ptolemy.actor.gt.AtomicActorMatcher {.Match3ActorsRelationCollapsing_rule.Pattern.CompositeActorMatcher.AtomicActorMatcher} = ptolemy.actor.lib.Const {.Match3ActorsRelationCollapsing_succ1.CompositeActor.Const}, ptolemy.actor.gt.CompositeActorMatcher {.Match3ActorsRelationCollapsing_rule.Pattern.CompositeActorMatcher} = ptolemy.actor.TypedCompositeActor {.Match3ActorsRelationCollapsing_succ1.CompositeActor}, ptolemy.actor.gt.Pattern {.Match3ActorsRelationCollapsing_rule.Pattern} = ptolemy.actor.TypedCompositeActor {.Match3ActorsRelationCollapsing_succ1}, ptolemy.actor.gt.PortMatcher {.Match3ActorsRelationCollapsing_rule.Pattern.AtomicActorMatcher.criterion1} = ptolemy.actor.TypedIOPort {.Match3ActorsRelationCollapsing_succ1.Const.trigger}, ptolemy.actor.gt.PortMatcher {.Match3ActorsRelationCollapsing_rule.Pattern.AtomicActorMatcher.criterion2} = ptolemy.actor.TypedIOPort {.Match3ActorsRelationCollapsing_succ1.Const.output}, ptolemy.actor.gt.PortMatcher {.Match3ActorsRelationCollapsing_rule.Pattern.AtomicActorMatcher2.criterion1} = ptolemy.actor.TypedIOPort {.Match3ActorsRelationCollapsing_succ1.Const2.trigger}, ptolemy.actor.gt.PortMatcher {.Match3ActorsRelationCollapsing_rule.Pattern.CompositeActorMatcher.AtomicActorMatcher.criterion1} = ptolemy.actor.TypedIOPort {.Match3ActorsRelationCollapsing_succ1.CompositeActor.Const.output}}}

test Match3ActorsRelationCollapsing.2 {Test Match3ActorsRelationCollapsing_rule.xml with Match3ActorsRelationCollapsing_fail2.xml} {
    set matchResult [java::call ptolemy.actor.gt.GraphMatcher match ${p}Match3ActorsRelationCollapsing_rule.xml ${p}Match3ActorsRelationCollapsing_fail2.xml]
    [$matchResult getMatchResult] toString
} {{}}
