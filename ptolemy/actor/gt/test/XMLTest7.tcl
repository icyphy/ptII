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
test XMLTest-7.1 {Test rule7.xml with host7_1.xml} {
    set matchResult [java::call ptolemy.actor.gt.GraphMatcher match rule7.xml host7_1.xml]
    [$matchResult getMatchResult] toString
} {{ptolemy.actor.TypedIOPort {.rule7.Pattern.AtomicActorMatcher.criterion1} = ptolemy.actor.TypedIOPort {.host7.Const.trigger}, ptolemy.actor.TypedIOPort {.rule7.Pattern.AtomicActorMatcher.criterion2} = ptolemy.actor.TypedIOPort {.host7.Const.output}, ptolemy.actor.TypedIOPort {.rule7.Pattern.AtomicActorMatcher2.criterion1} = ptolemy.actor.TypedIOPort {.host7.Const2.trigger}, ptolemy.actor.TypedIOPort {.rule7.Pattern.CompositeActorMatcher.AtomicActorMatcher.criterion1} = ptolemy.actor.TypedIOPort {.host7.CompositeActor.Const.output}, ptolemy.actor.TypedIOPort {.rule7.Pattern.CompositeActorMatcher.port} = ptolemy.actor.TypedIOPort {.host7.CompositeActor.port}, ptolemy.actor.TypedIORelation {.rule7.Pattern.CompositeActorMatcher.relation} = ptolemy.actor.TypedIORelation {.host7.CompositeActor.relation}, ptolemy.actor.TypedIORelation {.rule7.Pattern.relation2} = ptolemy.actor.TypedIORelation {.host7.relation3}, ptolemy.actor.TypedIORelation {.rule7.Pattern.relation3} = ptolemy.actor.TypedIORelation {.host7.relation4}, ptolemy.actor.TypedIORelation {.rule7.Pattern.relation} = ptolemy.actor.TypedIORelation {.host7.relation}, ptolemy.actor.gt.AtomicActorMatcher {.rule7.Pattern.AtomicActorMatcher2} = ptolemy.actor.lib.Const {.host7.Const2}, ptolemy.actor.gt.AtomicActorMatcher {.rule7.Pattern.AtomicActorMatcher} = ptolemy.actor.lib.Const {.host7.Const}, ptolemy.actor.gt.AtomicActorMatcher {.rule7.Pattern.CompositeActorMatcher.AtomicActorMatcher} = ptolemy.actor.lib.Const {.host7.CompositeActor.Const}, ptolemy.actor.gt.CompositeActorMatcher {.rule7.Pattern.CompositeActorMatcher} = ptolemy.actor.TypedCompositeActor {.host7.CompositeActor}, ptolemy.actor.gt.Pattern {.rule7.Pattern} = ptolemy.actor.TypedCompositeActor {.host7}}}

test XMLTest-6.2 {Test rule7.xml with host7_2.xml} {
    set matchResult [java::call ptolemy.actor.gt.GraphMatcher match rule7.xml host7_2.xml]
    [$matchResult getMatchResult] toString
} {{}}
