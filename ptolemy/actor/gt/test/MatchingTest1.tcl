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
test MatchingTest-1.1 {An empty graph with an empty lhs} {
    set e0 [sdfModel 3]
    set transformer [java::new ptolemy.actor.gt.RecursiveGraphMatcher]
    set lhs [java::new ptolemy.actor.gt.CompositeActorMatcher $e0 lhs]
    set host [java::new ptolemy.actor.TypedCompositeActor $e0 host]
    
    $transformer match $lhs $host
    [$transformer getMatchResult] toString
} {{ptolemy.actor.gt.CompositeActorMatcher {.top.lhs} = ptolemy.actor.TypedCompositeActor {.top.host}}}

test MatchingTest-1.2 {An lhs with 2 actor matchers} {
    set lhsA1 [java::new ptolemy.actor.gt.AtomicActorMatcher $lhs A1]
    set lhsA2 [java::new ptolemy.actor.gt.AtomicActorMatcher $lhs A2]
    
    $transformer match $lhs $host
    [$transformer getMatchResult] toString
} {{}}

test MatchingTest-1.3 {A graph with 2 actors and an lhs with 2 matchers} {
    set hostA1 [java::new ptolemy.actor.lib.Const $host A1]
    set hostA2 [java::new ptolemy.actor.lib.Const $host A2]
    
    $transformer match $lhs $host
    [$transformer getMatchResult] toString
} {{ptolemy.actor.gt.AtomicActorMatcher {.top.lhs.A1} = ptolemy.actor.lib.Const {.top.host.A1}, ptolemy.actor.gt.AtomicActorMatcher {.top.lhs.A2} = ptolemy.actor.lib.Const {.top.host.A2}, ptolemy.actor.gt.CompositeActorMatcher {.top.lhs} = ptolemy.actor.TypedCompositeActor {.top.host}}}

test MatchingTest-1.4 {A graph with 2 actors and an lhs with 3 actors} {
    set lhsA3 [java::new ptolemy.actor.gt.AtomicActorMatcher $lhs A3]
    
    $transformer match $lhs $host
    [$transformer getMatchResult] toString
} {{}}

test MatchingTest-1.5 {A graph with 3 actors and an lhs with 3 actors} {
    set hostA3 [java::new ptolemy.actor.lib.gui.Display $host A3]
    
    $transformer match $lhs $host
    [$transformer getMatchResult] toString
} {{ptolemy.actor.gt.AtomicActorMatcher {.top.lhs.A1} = ptolemy.actor.lib.Const {.top.host.A1}, ptolemy.actor.gt.AtomicActorMatcher {.top.lhs.A2} = ptolemy.actor.lib.Const {.top.host.A2}, ptolemy.actor.gt.AtomicActorMatcher {.top.lhs.A3} = ptolemy.actor.lib.gui.Display {.top.host.A3}, ptolemy.actor.gt.CompositeActorMatcher {.top.lhs} = ptolemy.actor.TypedCompositeActor {.top.host}}}

test MatchingTest-1.6 {Ports added to all the 3 matchers} {
    set lhsA1PortRule [java::new ptolemy.actor.gt.rules.PortRule "output" "type" false true false]
    set lhsA1PortRuleList [java::new ptolemy.actor.gt.RuleList]
    $lhsA1PortRuleList add [java::cast ptolemy.actor.gt.Rule $lhsA1PortRule]
    set lhsA1Attr [java::cast ptolemy.actor.gt.RuleListAttribute [$lhsA1 getAttribute ruleList]]
    $lhsA1Attr setExpression [$lhsA1PortRuleList toString]
    
    set lhsA2PortRule [java::new ptolemy.actor.gt.rules.PortRule "output" "type" false true false]
    set lhsA2PortRuleList [java::new ptolemy.actor.gt.RuleList]
    $lhsA2PortRuleList add [java::cast ptolemy.actor.gt.Rule $lhsA2PortRule]
    set lhsA2Attr [java::cast ptolemy.actor.gt.RuleListAttribute [$lhsA2 getAttribute ruleList]]
    $lhsA2Attr setExpression [$lhsA2PortRuleList toString]
    
    set lhsA3PortRule [java::new ptolemy.actor.gt.rules.PortRule "input" "type" true false true]
    set lhsA3PortRuleList [java::new ptolemy.actor.gt.RuleList]
    $lhsA3PortRuleList add [java::cast ptolemy.actor.gt.Rule $lhsA3PortRule]
    set lhsA3Attr [java::cast ptolemy.actor.gt.RuleListAttribute [$lhsA3 getAttribute ruleList]]
    $lhsA3Attr setExpression [$lhsA3PortRuleList toString]
    
    $transformer match $lhs $host
    [$transformer getMatchResult] toString
} {{ptolemy.actor.TypedIOPort {.top.lhs.A1.output} = ptolemy.actor.TypedIOPort {.top.host.A1.output}, ptolemy.actor.TypedIOPort {.top.lhs.A2.output} = ptolemy.actor.TypedIOPort {.top.host.A2.output}, ptolemy.actor.TypedIOPort {.top.lhs.A3.input} = ptolemy.actor.TypedIOPort {.top.host.A3.input}, ptolemy.actor.gt.AtomicActorMatcher {.top.lhs.A1} = ptolemy.actor.lib.Const {.top.host.A1}, ptolemy.actor.gt.AtomicActorMatcher {.top.lhs.A2} = ptolemy.actor.lib.Const {.top.host.A2}, ptolemy.actor.gt.AtomicActorMatcher {.top.lhs.A3} = ptolemy.actor.lib.gui.Display {.top.host.A3}, ptolemy.actor.gt.CompositeActorMatcher {.top.lhs} = ptolemy.actor.TypedCompositeActor {.top.host}}}

test MatchingTest-1.7 {Relations added to the lhs} {
    set lhsR1 [java::new ptolemy.actor.TypedIORelation $lhs "R1"]
    [java::cast ptolemy.kernel.Port [[$lhsA1 portList] get 0]] link $lhsR1
    [java::cast ptolemy.kernel.Port [[$lhsA3 portList] get 0]] link $lhsR1
    set lhsR2 [java::new ptolemy.actor.TypedIORelation $lhs "R2"]
    [java::cast ptolemy.kernel.Port [[$lhsA2 portList] get 0]] link $lhsR2
    [java::cast ptolemy.kernel.Port [[$lhsA3 portList] get 0]] link $lhsR2
    
    $transformer match $lhs $host
    [$transformer getMatchResult] toString
} {{}}

test MatchingTest-1.8 {Relations added to the graph} {
    set hostR1 [java::new ptolemy.actor.TypedIORelation $host "R1"]
    [java::cast ptolemy.kernel.Port [[$hostA1 portList] get 0]] link $hostR1
    [java::cast ptolemy.kernel.Port [[$hostA3 portList] get 0]] link $hostR1
    set hostR2 [java::new ptolemy.actor.TypedIORelation $host "R2"]
    [java::cast ptolemy.kernel.Port [[$hostA2 portList] get 0]] link $hostR2
    [java::cast ptolemy.kernel.Port [[$hostA3 portList] get 0]] link $hostR2
    
    $transformer match $lhs $host
    [$transformer getMatchResult] toString
} {{ptolemy.actor.TypedIOPort {.top.lhs.A1.output} = ptolemy.actor.TypedIOPort {.top.host.A1.output}, ptolemy.actor.TypedIOPort {.top.lhs.A2.output} = ptolemy.actor.TypedIOPort {.top.host.A2.output}, ptolemy.actor.TypedIOPort {.top.lhs.A3.input} = ptolemy.actor.TypedIOPort {.top.host.A3.input}, ptolemy.actor.gt.AtomicActorMatcher {.top.lhs.A1} = ptolemy.actor.lib.Const {.top.host.A1}, ptolemy.actor.gt.AtomicActorMatcher {.top.lhs.A2} = ptolemy.actor.lib.Const {.top.host.A2}, ptolemy.actor.gt.AtomicActorMatcher {.top.lhs.A3} = ptolemy.actor.lib.gui.Display {.top.host.A3}, ptolemy.actor.gt.CompositeActorMatcher {.top.lhs} = ptolemy.actor.TypedCompositeActor {.top.host}}}

test MatchingTest-1.9 {LHS's A1 has an input port} {
    set lhsA1PortRule2 [java::new ptolemy.actor.gt.rules.PortRule "input" "type" true false false]
    $lhsA1PortRuleList add [java::cast ptolemy.actor.gt.Rule $lhsA1PortRule2]
    $lhsA1Attr setExpression [$lhsA1PortRuleList toString]
    
    $transformer match $lhs $host
    [$transformer getMatchResult] toString
} {{ptolemy.actor.TypedIOPort {.top.lhs.A1.input} = ptolemy.actor.TypedIOPort {.top.host.A1.trigger}, ptolemy.actor.TypedIOPort {.top.lhs.A1.output} = ptolemy.actor.TypedIOPort {.top.host.A1.output}, ptolemy.actor.TypedIOPort {.top.lhs.A2.output} = ptolemy.actor.TypedIOPort {.top.host.A2.output}, ptolemy.actor.TypedIOPort {.top.lhs.A3.input} = ptolemy.actor.TypedIOPort {.top.host.A3.input}, ptolemy.actor.gt.AtomicActorMatcher {.top.lhs.A1} = ptolemy.actor.lib.Const {.top.host.A1}, ptolemy.actor.gt.AtomicActorMatcher {.top.lhs.A2} = ptolemy.actor.lib.Const {.top.host.A2}, ptolemy.actor.gt.AtomicActorMatcher {.top.lhs.A3} = ptolemy.actor.lib.gui.Display {.top.host.A3}, ptolemy.actor.gt.CompositeActorMatcher {.top.lhs} = ptolemy.actor.TypedCompositeActor {.top.host}}}

test MatchingTest-1.10 {LHS's A3 has an output port} {
    set lhsA3PortRule2 [java::new ptolemy.actor.gt.rules.PortRule "output" "type" false true false]
    $lhsA3PortRuleList add [java::cast ptolemy.actor.gt.Rule $lhsA3PortRule2]
    $lhsA3Attr setExpression [$lhsA3PortRuleList toString]
    
    $transformer match $lhs $host
    [$transformer getMatchResult] toString
} {{}}

test MatchingTest-1.11 {Host has another Const A4} {
    set hostA4 [java::new ptolemy.actor.lib.Const $host A4]
    
    $transformer match $lhs $host
    [$transformer getMatchResult] toString
} {{}}
