# Tests for the ProduceLatticeCPO class
#
# @Author: Ben Lickly
#
# @Version $Id$
#
# @Copyright (c) 2011-2011 The Regents of the University of California.
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
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
#######################################################################

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then { 
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

proc link {ont a b} {
  set link [$ont newRelation [concat [$a toString] " to " [$b toString]]]
  [$a getOutgoingPort] link $link
  [$b getIncomingPort] link $link
}

proc setupSimpleSquareOntology {} {
    set ont [java::new {ptolemy.data.ontologies.Ontology} [java::null]]

    # Concepts
    set bot [java::new {ptolemy.data.ontologies.FiniteConcept} $ont "bot"]
    set top [java::new {ptolemy.data.ontologies.FiniteConcept} $ont "top"]
    set left [java::new {ptolemy.data.ontologies.FiniteConcept} $ont "left"]
    set right [java::new {ptolemy.data.ontologies.FiniteConcept} $ont "right"]
    set infinite [java::new {ptolemy.data.ontologies.FlatTokenRepresentativeConcept} $ont "infinite"]

    # Relations
    link $ont $bot $left
    link $ont $bot $right
    link $ont $left $top
    link $ont $right $top

    link $ont $bot $infinite
    link $ont $infinite $top
    return [list $ont $bot $top $left $right $infinite]
}

proc simpleSquareOntology {} {
    return [lindex [setupSimpleSquareOntology] 0]
}
######################################################################
####
#
test OntologyCreation {An ontology with a flat representative is still a lattice} {
    list [[simpleSquareOntology] isLattice]
} {1}

test MonotonicityConceptCreation-1.0 {We can create an empty MonotonicityConcept} {
    set lattice [simpleSquareOntology]
    set monotonicityConcept [java::call {ptolemy.data.ontologies.lattice.adapters.monotonicityAnalysis.MonotonicityConcept} createMonotonicityConcept $lattice]
    $monotonicityConcept toString
} {{}}

test MonotonicityConceptCreation-2.0 {We can create a single element MonotonicityConcept} {
    set ont [setupSimpleSquareOntology]
    set lattice [lindex $ont 0]
    set left [lindex $ont 3]
    set monotonicityConcept [java::call {ptolemy.data.ontologies.lattice.adapters.monotonicityAnalysis.MonotonicityConcept} createMonotonicityConcept $lattice]
    $monotonicityConcept putMonotonicity {x} $left
    $monotonicityConcept toString
} {{x = left}}

test MonotonicityConceptCreation-3.0 {We can create a single infinite element MonotonicityConcept} {
    set ont [setupSimpleSquareOntology]
    set lattice [lindex $ont 0]
    set infiniteRep [lindex $ont 5]
    set monotonicityConcept [java::call {ptolemy.data.ontologies.lattice.adapters.monotonicityAnalysis.MonotonicityConcept} createMonotonicityConcept $lattice]
    set infinite [java::call {ptolemy.data.ontologies.FlatTokenInfiniteConcept} createFlatTokenInfiniteConcept $lattice $infiniteRep [java::new {ptolemy.data.IntToken} 42]]
    $monotonicityConcept putMonotonicity {x} $infinite
    $monotonicityConcept toString
} {{x = infinite_42}}

test MonotonicityConceptCreation-4.0 {We can create a dual infinite element MonotonicityConcept} {
    set ont [setupSimpleSquareOntology]
    set lattice [lindex $ont 0]
    set infiniteRep [lindex $ont 5]
    set monotonicityConcept [java::call {ptolemy.data.ontologies.lattice.adapters.monotonicityAnalysis.MonotonicityConcept} createMonotonicityConcept $lattice]
    set infinite42 [java::call {ptolemy.data.ontologies.FlatTokenInfiniteConcept} createFlatTokenInfiniteConcept $lattice $infiniteRep [java::new {ptolemy.data.IntToken} 42]]
    set infinite43 [java::call {ptolemy.data.ontologies.FlatTokenInfiniteConcept} createFlatTokenInfiniteConcept $lattice $infiniteRep [java::new {ptolemy.data.IntToken} 43]]
    $monotonicityConcept putMonotonicity {x} $infinite42
    $monotonicityConcept putMonotonicity {y} $infinite43
    list [$infinite42 toString] [$infinite43 toString] [$monotonicityConcept toString]
} {infinite_42 infinite_43 {{x = infinite_42, y = infinite_43}}}

test MonotonicityLeastUpperBound-1.0 {We can take the upper bound of two conflicting monotonicity concepts and they will promote to top.} {
    set ont [setupSimpleSquareOntology]
    set lattice [lindex $ont 0]
    set infiniteRep [lindex $ont 5]
    set monotonicityConcept1 [java::call {ptolemy.data.ontologies.lattice.adapters.monotonicityAnalysis.MonotonicityConcept} createMonotonicityConcept $lattice]
    set monotonicityConcept2 [java::call {ptolemy.data.ontologies.lattice.adapters.monotonicityAnalysis.MonotonicityConcept} createMonotonicityConcept $lattice]
    set infinite42 [java::call {ptolemy.data.ontologies.FlatTokenInfiniteConcept} createFlatTokenInfiniteConcept $lattice $infiniteRep [java::new {ptolemy.data.IntToken} 42]]
    set infinite43 [java::call {ptolemy.data.ontologies.FlatTokenInfiniteConcept} createFlatTokenInfiniteConcept $lattice $infiniteRep [java::new {ptolemy.data.IntToken} 43]]
    $monotonicityConcept1 putMonotonicity {x} $infinite42
    $monotonicityConcept2 putMonotonicity {x} $infinite43
    set lub [$monotonicityConcept1 leastUpperBound $monotonicityConcept2]
    list [$monotonicityConcept1 toString] [$monotonicityConcept2 toString] [$lub toString]
} {{{x = infinite_42}} {{x = infinite_43}} {{x = top}}}

