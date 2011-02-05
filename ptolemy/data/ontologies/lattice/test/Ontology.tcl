# Tests for the ProduceLatticeCPO class
#
# @Author: Ben Lickly
#
# @Version $Id$
#
# @Copyright (c) 2011 The Regents of the University of California.
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


######################################################################
####
#
test Ontology-1.0 {An empty ontology is a lattice} {
    set emptyOnt [java::new {ptolemy.data.ontologies.Ontology} [java::null]]
    set emptyCPO [$emptyOnt getConceptGraph]

    list [$emptyOnt isLattice] [$emptyCPO isLattice]
} {1 1}

######################################################################
####
#
test ProductLatticeOntology-1.0 {An empty product lattice ontology is a lattice} {
    set emptyProdOnt [java::new {ptolemy.data.ontologies.lattice.ProductLatticeOntology} [java::null]]
    set emptyProdCPO [$emptyProdOnt getConceptGraph]

    list [$emptyProdOnt isLattice] [$emptyProdCPO isLattice]
} {1 1}

######################################################################
####
#
test Lattices-1.0 {A one element lattice is a lattice} {
    set singletonOnt [java::new {ptolemy.data.ontologies.Ontology} [java::null]]
    set singletonConcept [java::new {ptolemy.data.ontologies.FiniteConcept} $singletonOnt {A}]

    list [$singletonOnt isLattice] [[$singletonOnt getConceptGraph] isLattice]
} {1 1}
