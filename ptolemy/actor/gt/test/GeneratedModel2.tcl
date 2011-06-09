# Test DepthFirstTransformer.
#
# @Author: Thomas Huining Feng
#
# @Version: $Id$
#
# @Copyright (c) 1997-2008 The Regents of the University of California.
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
test GeneratedModel2.1 {An lhs with a CompositeActor matching an empty host} {
    set e0 [sdfModel 3]
    set matcher [java::new ptolemy.actor.gt.GraphMatcher]
    set lhs [java::new ptolemy.actor.gt.Pattern $e0 lhs]
    set host [java::new ptolemy.actor.gt.Pattern $e0 host]
    
    java::new ptolemy.actor.gt.ContainerIgnoringAttribute $lhs "ContainerIgnoring"
    java::new ptolemy.actor.gt.HierarchyFlatteningAttribute $lhs "HierarchyFlattening"
    java::new ptolemy.actor.gt.RelationCollapsingAttribute $lhs "RelationCollapsing"
    
    set lhsA1 [java::new ptolemy.actor.gt.AtomicActorMatcher $lhs A1]
    set lhsA1Attr [java::cast ptolemy.actor.gt.GTIngredientsAttribute [$lhsA1 getAttribute criteria]]
    set lhsA1PortRuleList [java::new ptolemy.actor.gt.GTIngredientList $lhsA1Attr]
    set lhsA1PortRule [java::new ptolemy.actor.gt.ingredients.criteria.PortCriterion $lhsA1PortRuleList "" "" false true false ""]
    $lhsA1PortRule setPortNameEnabled false
    $lhsA1PortRule setPortTypeEnabled false
    $lhsA1PortRuleList add [java::cast ptolemy.actor.gt.GTIngredient $lhsA1PortRule]
    $lhsA1Attr setExpression [$lhsA1PortRuleList toString]
    
    set lhsC1 [java::new ptolemy.actor.TypedCompositeActor $lhs C1]
    set lhsC1Port1 [java::new ptolemy.actor.TypedIOPort $lhsC1 input true false]
    
    set lhsC1A1 [java::new ptolemy.actor.gt.AtomicActorMatcher $lhsC1 A1]
    set lhsC1A1Attr [java::cast ptolemy.actor.gt.GTIngredientsAttribute [$lhsC1A1 getAttribute criteria]]
    set lhsC1A1PortRuleList [java::new ptolemy.actor.gt.GTIngredientList $lhsC1A1Attr]
    set lhsC1A1PortRule [java::new ptolemy.actor.gt.ingredients.criteria.PortCriterion $lhsC1A1PortRuleList "" "" true false true ""]
    $lhsC1A1PortRule setPortNameEnabled false
    $lhsC1A1PortRule setPortTypeEnabled false
    $lhsC1A1PortRuleList add [java::cast ptolemy.actor.gt.GTIngredient $lhsC1A1PortRule]
    $lhsC1A1Attr setExpression [$lhsC1A1PortRuleList toString]
    
    set lhsR1 [java::new ptolemy.actor.TypedIORelation $lhs R1]
    [java::cast ptolemy.kernel.Port [[$lhsA1 portList] get 0]] link $lhsR1
    $lhsC1Port1 link $lhsR1
    
    set lhsC1R1 [java::new ptolemy.actor.TypedIORelation $lhsC1 C1R1]
    $lhsC1Port1 link $lhsC1R1
    [java::cast ptolemy.kernel.Port [[$lhsC1A1 portList] get 0]] link $lhsC1R1
    
    $matcher match $lhs $host
    [$matcher getMatchResult] toString
} {{}}

test GeneratedModel2.2 {An lhs with a CompositeActor matching a host without the CompositeActor} {
    set hostA1 [java::new ptolemy.actor.gt.AtomicActorMatcher $host A1]
    set hostA1Attr [java::cast ptolemy.actor.gt.GTIngredientsAttribute [$hostA1 getAttribute criteria]]
    set hostA1PortRuleList [java::new ptolemy.actor.gt.GTIngredientList $hostA1Attr]
    set hostA1PortRule [java::new ptolemy.actor.gt.ingredients.criteria.PortCriterion $hostA1PortRuleList "" "" false true false ""]
    $hostA1PortRule setPortNameEnabled false
    $hostA1PortRule setPortTypeEnabled false
    $hostA1PortRuleList add [java::cast ptolemy.actor.gt.GTIngredient $hostA1PortRule]
    $hostA1Attr setExpression [$hostA1PortRuleList toString]
    
    set hostA2 [java::new ptolemy.actor.gt.AtomicActorMatcher $host A2]
    set hostA2Attr [java::cast ptolemy.actor.gt.GTIngredientsAttribute [$hostA2 getAttribute criteria]]
    set hostA2PortRuleList [java::new ptolemy.actor.gt.GTIngredientList $hostA2Attr]
    set hostA2PortRule [java::new ptolemy.actor.gt.ingredients.criteria.PortCriterion $hostA2PortRuleList "" "" true false true ""]
    $hostA2PortRule setPortNameEnabled false
    $hostA2PortRule setPortTypeEnabled false
    $hostA2PortRuleList add [java::cast ptolemy.actor.gt.GTIngredient $hostA2PortRule]
    $hostA2Attr setExpression [$hostA2PortRuleList toString]
    
    set hostR1 [java::new ptolemy.actor.TypedIORelation $host R1]
    [java::cast ptolemy.kernel.Port [[$hostA1 portList] get 0]] link $hostR1
    [java::cast ptolemy.kernel.Port [[$hostA2 portList] get 0]] link $hostR1
    
    $matcher match $lhs $host
    [$matcher getMatchResult] toString
} {{ptolemy.actor.gt.AtomicActorMatcher {.top.lhs.A1} = ptolemy.actor.gt.AtomicActorMatcher {.top.host.A1}, ptolemy.actor.gt.AtomicActorMatcher {.top.lhs.C1.A1} = ptolemy.actor.gt.AtomicActorMatcher {.top.host.A2}, ptolemy.actor.gt.Pattern {.top.lhs} = ptolemy.actor.gt.Pattern {.top.host}, ptolemy.actor.gt.PortMatcher {.top.lhs.A1.criterion1} = ptolemy.actor.gt.PortMatcher {.top.host.A1.criterion1}, ptolemy.actor.gt.PortMatcher {.top.lhs.C1.A1.criterion1} = ptolemy.actor.gt.PortMatcher {.top.host.A2.criterion1}}}

test GeneratedModel2.3 {An lhs with a CompositeActor matching a host with a CompositeActor and an extra actor} {
    set hostC1 [java::new ptolemy.actor.TypedCompositeActor $host C1]
    set hostC1Port1 [java::new ptolemy.actor.TypedIOPort $hostC1 input true false]
    
    set hostC1A1 [java::new ptolemy.actor.gt.AtomicActorMatcher $hostC1 A1]
    set hostC1A1Attr [java::cast ptolemy.actor.gt.GTIngredientsAttribute [$hostC1A1 getAttribute criteria]]
    set hostC1A1PortRuleList [java::new ptolemy.actor.gt.GTIngredientList $hostC1A1Attr]
    set hostC1A1PortRule [java::new ptolemy.actor.gt.ingredients.criteria.PortCriterion $hostC1A1PortRuleList "" "" true false true ""]
    $hostC1A1PortRule setPortNameEnabled false
    $hostC1A1PortRule setPortTypeEnabled false
    $hostC1A1PortRuleList add [java::cast ptolemy.actor.gt.GTIngredient $hostC1A1PortRule]
    $hostC1A1Attr setExpression [$hostC1A1PortRuleList toString]
    
    set hostC1R1 [java::new ptolemy.actor.TypedIORelation $hostC1 C1R1]
    $hostC1Port1 link $hostC1R1
    [java::cast ptolemy.kernel.Port [[$hostC1A1 portList] get 0]] link $hostC1R1
    $hostC1Port1 link $hostR1
    
    $matcher match $lhs $host
    [$matcher getMatchResult] toString
} {{ptolemy.actor.gt.AtomicActorMatcher {.top.lhs.A1} = ptolemy.actor.gt.AtomicActorMatcher {.top.host.A1}, ptolemy.actor.gt.AtomicActorMatcher {.top.lhs.C1.A1} = ptolemy.actor.gt.AtomicActorMatcher {.top.host.A2}, ptolemy.actor.gt.Pattern {.top.lhs} = ptolemy.actor.gt.Pattern {.top.host}, ptolemy.actor.gt.PortMatcher {.top.lhs.A1.criterion1} = ptolemy.actor.gt.PortMatcher {.top.host.A1.criterion1}, ptolemy.actor.gt.PortMatcher {.top.lhs.C1.A1.criterion1} = ptolemy.actor.gt.PortMatcher {.top.host.A2.criterion1}}}

test GeneratedModel2.4 {Add an SDF director to the lhs CompositeActor} {
    $lhsC1 setDirector [java::new ptolemy.domains.sdf.kernel.SDFDirector]
    
    $matcher match $lhs $host
    [$matcher getMatchResult] toString
} {{}}

test GeneratedModel2.5 {Add an SDF director to the host CompositeActor} {
    $hostC1 setDirector [java::new ptolemy.domains.sdf.kernel.SDFDirector]
    
    $matcher match $lhs $host
    [$matcher getMatchResult] toString
} {{ptolemy.actor.TypedCompositeActor {.top.lhs.C1} = ptolemy.actor.TypedCompositeActor {.top.host.C1}, ptolemy.actor.TypedIOPort {.top.lhs.C1.input} = ptolemy.actor.TypedIOPort {.top.host.C1.input}, ptolemy.actor.gt.AtomicActorMatcher {.top.lhs.A1} = ptolemy.actor.gt.AtomicActorMatcher {.top.host.A1}, ptolemy.actor.gt.AtomicActorMatcher {.top.lhs.C1.A1} = ptolemy.actor.gt.AtomicActorMatcher {.top.host.C1.A1}, ptolemy.actor.gt.Pattern {.top.lhs} = ptolemy.actor.gt.Pattern {.top.host}, ptolemy.actor.gt.PortMatcher {.top.lhs.A1.criterion1} = ptolemy.actor.gt.PortMatcher {.top.host.A1.criterion1}, ptolemy.actor.gt.PortMatcher {.top.lhs.C1.A1.criterion1} = ptolemy.actor.gt.PortMatcher {.top.host.C1.A1.criterion1}, ptolemy.domains.sdf.kernel.SDFDirector {.top.lhs.C1.} = ptolemy.domains.sdf.kernel.SDFDirector {.top.host.C1.}}}
