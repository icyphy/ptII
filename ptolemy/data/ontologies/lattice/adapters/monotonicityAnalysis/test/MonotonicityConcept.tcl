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

proc simpleSquareOntology {} {
    set ont [java::new {ptolemy.data.ontologies.Ontology} [java::null]]

    # Concepts
    set bot [java::new {ptolemy.data.ontologies.FiniteConcept} $ont "bot"]
    set top [java::new {ptolemy.data.ontologies.FiniteConcept} $ont "top"]
    set left [java::new {ptolemy.data.ontologies.FiniteConcept} $ont "left"]
    set right [java::new {ptolemy.data.ontologies.FiniteConcept} $ont "right"]
    set infinite [java::new {ptolemy.data.ontologies.FlatScalarTokenRepresentativeConcept} $ont "infinite"]

    # Relations
    link $ont $bot $left
    link $ont $bot $right
    link $ont $left $top
    link $ont $right $top

    link $ont $bot $infinite
    link $ont $infinite $top

    return $ont
}
######################################################################
####
#
test OntologyCreation {An ontology with a flat representative is still a lattice} {
    list [[simpleSquareOntology] isLattice]
} {1}

test MonotonicityConceptCreation {We can create a simple MonotonicityConcept} {
    set lattice [simpleSquareOntology]
    set monotonicityConcept [java::call {ptolemy.data.ontologies.lattice.adapters.monotonicityAnalysis.MonotonicityConcept} createMonotonicityConcept $lattice]
    $monotonicityConcept toString
} {{}}
