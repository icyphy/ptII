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
test MatchHierarchy2Actors2Composites.1 {Test MatchHierarchy2Actors2Composites_rule.xml with MatchHierarchy2Actors2Composites_fail1.xml} {
    set matchResult [java::call ptolemy.actor.gt.GraphMatcher match ${p}MatchHierarchy2Actors2Composites_rule.xml ${p}MatchHierarchy2Actors2Composites_fail1.xml]
    [$matchResult getMatchResult] toString
} {{}}

test MatchHierarchy2Actors2Composites.2 {Test MatchHierarchy2Actors2Composites_rule.xml with MatchHierarchy2Actors2Composites_fail2.xml} {
    set matchResult [java::call ptolemy.actor.gt.GraphMatcher match ${p}MatchHierarchy2Actors2Composites_rule.xml ${p}MatchHierarchy2Actors2Composites_fail2.xml]
    [$matchResult getMatchResult] toString
} {{}}

test MatchHierarchy2Actors2Composites.3 {Test MatchHierarchy2Actors2Composites_rule.xml with MatchHierarchy2Actors2Composites_succ3.xml} {
    set matchResult [java::call ptolemy.actor.gt.GraphMatcher match ${p}MatchHierarchy2Actors2Composites_rule.xml ${p}MatchHierarchy2Actors2Composites_succ3.xml]
    [$matchResult getMatchResult] toString
} {{ptolemy.actor.TypedIOPort {.MatchHierarchy2Actors2Composites_rule.Pattern.CompositeActorMatcher.port} = ptolemy.actor.TypedIOPort {.MatchHierarchy2Actors2Composites_succ3.CompositeActor.port}, ptolemy.actor.TypedIOPort {.MatchHierarchy2Actors2Composites_rule.Pattern.CompositeActorMatcher2.port} = ptolemy.actor.TypedIOPort {.MatchHierarchy2Actors2Composites_succ3.CompositeActor2.port}, ptolemy.actor.gt.AtomicActorMatcher {.MatchHierarchy2Actors2Composites_rule.Pattern.CompositeActorMatcher.AtomicActorMatcher} = ptolemy.actor.lib.Const {.MatchHierarchy2Actors2Composites_succ3.CompositeActor.Const}, ptolemy.actor.gt.AtomicActorMatcher {.MatchHierarchy2Actors2Composites_rule.Pattern.CompositeActorMatcher2.AtomicActorMatcher} = ptolemy.actor.lib.gui.Display {.MatchHierarchy2Actors2Composites_succ3.CompositeActor2.Display}, ptolemy.actor.gt.CompositeActorMatcher {.MatchHierarchy2Actors2Composites_rule.Pattern.CompositeActorMatcher2} = ptolemy.actor.TypedCompositeActor {.MatchHierarchy2Actors2Composites_succ3.CompositeActor2}, ptolemy.actor.gt.CompositeActorMatcher {.MatchHierarchy2Actors2Composites_rule.Pattern.CompositeActorMatcher} = ptolemy.actor.TypedCompositeActor {.MatchHierarchy2Actors2Composites_succ3.CompositeActor}, ptolemy.actor.gt.Pattern {.MatchHierarchy2Actors2Composites_rule.Pattern} = ptolemy.actor.TypedCompositeActor {.MatchHierarchy2Actors2Composites_succ3}, ptolemy.actor.gt.PortMatcher {.MatchHierarchy2Actors2Composites_rule.Pattern.CompositeActorMatcher.AtomicActorMatcher.criterion1} = ptolemy.actor.TypedIOPort {.MatchHierarchy2Actors2Composites_succ3.CompositeActor.Const.output}, ptolemy.actor.gt.PortMatcher {.MatchHierarchy2Actors2Composites_rule.Pattern.CompositeActorMatcher2.AtomicActorMatcher.criterion1} = ptolemy.actor.TypedIOPort {.MatchHierarchy2Actors2Composites_succ3.CompositeActor2.Display.input}, ptolemy.domains.sdf.kernel.SDFDirector {.MatchHierarchy2Actors2Composites_rule.Pattern.CompositeActorMatcher.SDF Director} = ptolemy.domains.sdf.kernel.SDFDirector {.MatchHierarchy2Actors2Composites_succ3.CompositeActor.SDF Director}, ptolemy.domains.sdf.kernel.SDFDirector {.MatchHierarchy2Actors2Composites_rule.Pattern.CompositeActorMatcher2.SDF Director} = ptolemy.domains.sdf.kernel.SDFDirector {.MatchHierarchy2Actors2Composites_succ3.CompositeActor2.SDF Director}}}

test MatchHierarchy2Actors2Composites.4 {Test MatchHierarchy2Actors2Composites_rule.xml with MatchHierarchy2Actors2Composites_succ4.xml} {
    set matchResult [java::call ptolemy.actor.gt.GraphMatcher match ${p}MatchHierarchy2Actors2Composites_rule.xml ${p}MatchHierarchy2Actors2Composites_succ4.xml]
    [$matchResult getMatchResult] toString
} {{ptolemy.actor.TypedIOPort {.MatchHierarchy2Actors2Composites_rule.Pattern.CompositeActorMatcher.port} = ptolemy.actor.TypedIOPort {.MatchHierarchy2Actors2Composites_succ4.CompositeActor.port}, ptolemy.actor.TypedIOPort {.MatchHierarchy2Actors2Composites_rule.Pattern.CompositeActorMatcher2.port} = ptolemy.actor.TypedIOPort {.MatchHierarchy2Actors2Composites_succ4.CompositeActor3.port}, ptolemy.actor.gt.AtomicActorMatcher {.MatchHierarchy2Actors2Composites_rule.Pattern.CompositeActorMatcher.AtomicActorMatcher} = ptolemy.actor.lib.Const {.MatchHierarchy2Actors2Composites_succ4.CompositeActor.Const}, ptolemy.actor.gt.AtomicActorMatcher {.MatchHierarchy2Actors2Composites_rule.Pattern.CompositeActorMatcher2.AtomicActorMatcher} = ptolemy.actor.lib.gui.Display {.MatchHierarchy2Actors2Composites_succ4.CompositeActor3.Display}, ptolemy.actor.gt.CompositeActorMatcher {.MatchHierarchy2Actors2Composites_rule.Pattern.CompositeActorMatcher2} = ptolemy.actor.TypedCompositeActor {.MatchHierarchy2Actors2Composites_succ4.CompositeActor3}, ptolemy.actor.gt.CompositeActorMatcher {.MatchHierarchy2Actors2Composites_rule.Pattern.CompositeActorMatcher} = ptolemy.actor.TypedCompositeActor {.MatchHierarchy2Actors2Composites_succ4.CompositeActor}, ptolemy.actor.gt.Pattern {.MatchHierarchy2Actors2Composites_rule.Pattern} = ptolemy.actor.TypedCompositeActor {.MatchHierarchy2Actors2Composites_succ4}, ptolemy.actor.gt.PortMatcher {.MatchHierarchy2Actors2Composites_rule.Pattern.CompositeActorMatcher.AtomicActorMatcher.criterion1} = ptolemy.actor.TypedIOPort {.MatchHierarchy2Actors2Composites_succ4.CompositeActor.Const.output}, ptolemy.actor.gt.PortMatcher {.MatchHierarchy2Actors2Composites_rule.Pattern.CompositeActorMatcher2.AtomicActorMatcher.criterion1} = ptolemy.actor.TypedIOPort {.MatchHierarchy2Actors2Composites_succ4.CompositeActor3.Display.input}, ptolemy.domains.sdf.kernel.SDFDirector {.MatchHierarchy2Actors2Composites_rule.Pattern.CompositeActorMatcher.SDF Director} = ptolemy.domains.sdf.kernel.SDFDirector {.MatchHierarchy2Actors2Composites_succ4.CompositeActor.SDF Director}, ptolemy.domains.sdf.kernel.SDFDirector {.MatchHierarchy2Actors2Composites_rule.Pattern.CompositeActorMatcher2.SDF Director} = ptolemy.domains.sdf.kernel.SDFDirector {.MatchHierarchy2Actors2Composites_succ4.CompositeActor3.SDF Director}}}
