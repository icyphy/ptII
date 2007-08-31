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
test XMLTest-3.1 {Test rule3.xml with host3.1.xml} {
    set matchResult [java::call ptolemy.actor.gt.RecursiveGraphMatcher match rule3.xml host3.1.xml]
    [$matchResult getMatchResult] toString
} {{}}

test XMLTest-3.2 {Test rule3.xml with host3.2.xml} {
    set matchResult [java::call ptolemy.actor.gt.RecursiveGraphMatcher match rule3.xml host3.2.xml]
    [$matchResult getMatchResult] toString
} {{}}

test XMLTest-3.3 {Test rule3.xml with host3.3.xml} {
    set matchResult [java::call ptolemy.actor.gt.RecursiveGraphMatcher match rule3.xml host3.3.xml]
    [$matchResult getMatchResult] toString
} {{ptolemy.actor.TypedIOPort {.rule3.Left Hand Side.CompositeActorMatcher.AtomicActorMatcher.output} = ptolemy.actor.TypedIOPort {.host3.CompositeActor.Const.output}, ptolemy.actor.TypedIOPort {.rule3.Left Hand Side.CompositeActorMatcher.port} = ptolemy.actor.TypedIOPort {.host3.CompositeActor.port}, ptolemy.actor.TypedIOPort {.rule3.Left Hand Side.CompositeActorMatcher2.AtomicActorMatcher.input} = ptolemy.actor.TypedIOPort {.host3.CompositeActor2.Display.input}, ptolemy.actor.TypedIOPort {.rule3.Left Hand Side.CompositeActorMatcher2.port} = ptolemy.actor.TypedIOPort {.host3.CompositeActor2.port}, ptolemy.actor.gt.AtomicActorMatcher {.rule3.Left Hand Side.CompositeActorMatcher.AtomicActorMatcher} = ptolemy.actor.lib.Const {.host3.CompositeActor.Const}, ptolemy.actor.gt.AtomicActorMatcher {.rule3.Left Hand Side.CompositeActorMatcher2.AtomicActorMatcher} = ptolemy.actor.lib.gui.Display {.host3.CompositeActor2.Display}, ptolemy.actor.gt.CompositeActorMatcher {.rule3.Left Hand Side.CompositeActorMatcher2} = ptolemy.actor.TypedCompositeActor {.host3.CompositeActor2}, ptolemy.actor.gt.CompositeActorMatcher {.rule3.Left Hand Side.CompositeActorMatcher} = ptolemy.actor.TypedCompositeActor {.host3.CompositeActor}, ptolemy.actor.gt.CompositeActorMatcher {.rule3.Left Hand Side} = ptolemy.actor.TypedCompositeActor {.host3}, ptolemy.domains.sdf.kernel.SDFDirector {.rule3.Left Hand Side.CompositeActorMatcher.SDF Director} = ptolemy.domains.sdf.kernel.SDFDirector {.host3.CompositeActor.SDF Director}, ptolemy.domains.sdf.kernel.SDFDirector {.rule3.Left Hand Side.CompositeActorMatcher2.SDF Director} = ptolemy.domains.sdf.kernel.SDFDirector {.host3.CompositeActor2.SDF Director}}}

test XMLTest-3.4 {Test rule3.xml with host3.4.xml} {
    set matchResult [java::call ptolemy.actor.gt.RecursiveGraphMatcher match rule3.xml host3.4.xml]
    [$matchResult getMatchResult] toString
} {{ptolemy.actor.TypedIOPort {.rule3.Left Hand Side.CompositeActorMatcher.AtomicActorMatcher.output} = ptolemy.actor.TypedIOPort {.host3.CompositeActor.Const.output}, ptolemy.actor.TypedIOPort {.rule3.Left Hand Side.CompositeActorMatcher.port} = ptolemy.actor.TypedIOPort {.host3.CompositeActor.port}, ptolemy.actor.TypedIOPort {.rule3.Left Hand Side.CompositeActorMatcher2.AtomicActorMatcher.input} = ptolemy.actor.TypedIOPort {.host3.CompositeActor3.Display.input}, ptolemy.actor.TypedIOPort {.rule3.Left Hand Side.CompositeActorMatcher2.port} = ptolemy.actor.TypedIOPort {.host3.CompositeActor3.port}, ptolemy.actor.gt.AtomicActorMatcher {.rule3.Left Hand Side.CompositeActorMatcher.AtomicActorMatcher} = ptolemy.actor.lib.Const {.host3.CompositeActor.Const}, ptolemy.actor.gt.AtomicActorMatcher {.rule3.Left Hand Side.CompositeActorMatcher2.AtomicActorMatcher} = ptolemy.actor.lib.gui.Display {.host3.CompositeActor3.Display}, ptolemy.actor.gt.CompositeActorMatcher {.rule3.Left Hand Side.CompositeActorMatcher2} = ptolemy.actor.TypedCompositeActor {.host3.CompositeActor3}, ptolemy.actor.gt.CompositeActorMatcher {.rule3.Left Hand Side.CompositeActorMatcher} = ptolemy.actor.TypedCompositeActor {.host3.CompositeActor}, ptolemy.actor.gt.CompositeActorMatcher {.rule3.Left Hand Side} = ptolemy.actor.TypedCompositeActor {.host3}, ptolemy.domains.sdf.kernel.SDFDirector {.rule3.Left Hand Side.CompositeActorMatcher.SDF Director} = ptolemy.domains.sdf.kernel.SDFDirector {.host3.CompositeActor.SDF Director}, ptolemy.domains.sdf.kernel.SDFDirector {.rule3.Left Hand Side.CompositeActorMatcher2.SDF Director} = ptolemy.domains.sdf.kernel.SDFDirector {.host3.CompositeActor3.SDF Director}}}
