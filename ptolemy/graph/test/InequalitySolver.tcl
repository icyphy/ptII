# Tests for InequalityTerm, Inequality, and InequalitySolver
#
# @Author: Yuhong Xiong
#
# $Id$
#
# @Copyright (c) 1997-2000 The Regents of the University of California.
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

# Tycho test bed, see $TYCHO/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then { 
    source testDefs.tcl
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# If a file contains non-graphical tests, then it should be named .tcl
# If a file contains graphical tests, then it should be called .itcl
# 
# It would be nice if the tests would work in a vanilla itkwish binary.
# Check for necessary classes and adjust the auto_path accordingly.
#

######################################################################
#### iterToInfo
# Convert an Iterator to a list. If the Iterator contains
# InequalityTerms, the list contains the information returned by
# getInfo() of the terms; if the Iterator contains Inequalities,
# the list contains pairs of information for the lesser and greater
# terms of the Inequality.
#
proc iterToInfo {iter} {
    set result {}
    if {$iter != [java::null]} {
        while {[$iter hasNext] == 1} {
            set elem [$iter next]
	    if [ java::instanceof $elem ptolemy.graph.InequalityTerm] {
		lappend result [termToInfo $elem]
	    } else {
		if [ java::instanceof $elem ptolemy.graph.Inequality] {
		    set ineqelem [java::cast ptolemy.graph.Inequality $elem]
		    set lesser [$ineqelem getLesserTerm]
		    set greater [$ineqelem getGreaterTerm]
		    set ineq {}
		    lappend ineq [termToInfo $lesser]
		    lappend ineq [termToInfo $greater]
		    lappend result $ineq
		}
	    }

        }
    }
    return $result
}

proc termToInfo {term} {
    if [ java::instanceof $term ptolemy.graph.test.TestConstant] {
	set cterm [java::cast ptolemy.graph.test.TestConstant $term]
    } else { 
	set cterm [java::cast ptolemy.graph.test.TestVariable $term]
    }
    return [$cterm getInfo]
}


######################################################################
####
# 
test InequalitySolver-2.1 {construct the 4-point CPO in the design doc.} {
    set cpo [java::new ptolemy.graph.DirectedAcyclicGraph]
    set w [java::new {java.lang.String String} w]
    set x [java::new {java.lang.String String} x]
    set y [java::new {java.lang.String String} y]
    set z [java::new {java.lang.String String} z]
    $cpo add $w
    $cpo add $x
    $cpo add $y
    $cpo add $z
    $cpo addEdge $x $w
    $cpo addEdge $y $w
    $cpo addEdge $z $x
    $cpo addEdge $z $y
    $cpo description
} {{ptolemy.graph.DirectedAcyclicGraph
  {w}
  {x w}
  {y w}
  {z x y}
}}

######################################################################
####
# 
test InequalitySolver-2.2 {construct inequality constraints} {
    set tw [java::new ptolemy.graph.test.TestConstant $w]
    $tw setName W
    set tx [java::new ptolemy.graph.test.TestConstant $x]
    $tx setName X
    set ta [java::new ptolemy.graph.test.TestVariable]
    $ta setName A
    set tb [java::new ptolemy.graph.test.TestVariable]
    $tb setName B

    set iaw [java::new ptolemy.graph.Inequality $ta $tw]
    set ibx [java::new ptolemy.graph.Inequality $tb $tx]
    set iba [java::new ptolemy.graph.Inequality $tb $ta]
    set iab [java::new ptolemy.graph.Inequality $ta $tb]

    list [$tw isSettable] [$ta isSettable] [$tx getAssociatedObject] \
	[$tb getAssociatedObject] \
	[$tw isValueAcceptable] [$ta isValueAcceptable] \
	[list [$iaw toString] [$ibx toString] [$iba toString] [$iab toString]]
} {0 1 x java0x0 1 1 {{ptolemy.graph.test.TestVariableA(variable)_null <= ptolemy.graph.test.TestConstantW(constant)_w} {ptolemy.graph.test.TestVariableB(variable)_null <= ptolemy.graph.test.TestConstantX(constant)_x} {ptolemy.graph.test.TestVariableB(variable)_null <= ptolemy.graph.test.TestVariableA(variable)_null} {ptolemy.graph.test.TestVariableA(variable)_null <= ptolemy.graph.test.TestVariableB(variable)_null}}}

######################################################################
####
# 
test InequalitySolver-2.3 {solver for the least solution} {
    set s [java::new ptolemy.graph.InequalitySolver $cpo]
    $s addInequality $iaw
    $s addInequality $ibx
    $s addInequality $iba
    $s addInequality $iab

    set sat [$s solveLeast]

    # using lsort to order some enumerations
    list $sat [$ta getValue] [$tb getValue] \
	 [lsort [iterToInfo [$s bottomVariables]]] \
	 [iterToInfo [$s topVariables]] \
	 [lsort [iterToInfo [$s variables]]] \
	 [iterToInfo [$s unsatisfiedInequalities]]
} {1 z z {A(variable)_z B(variable)_z} {} {A(variable)_z B(variable)_z} {}}

######################################################################
####
# 
test InequalitySolver-2.4 {solver for the greatest solution} {
    set sat [$s solveGreatest]
    list $sat [$ta getValue] [$tb getValue] \
	 [iterToInfo [$s bottomVariables]] [iterToInfo [$s topVariables]] \
         [lsort [iterToInfo [$s variables]]] \
	 [iterToInfo [$s unsatisfiedInequalities]]
} {1 x x {} {} {A(variable)_x B(variable)_x} {}}

######################################################################
####
# 
test InequalitySolver-2.5 {constraints with no solution} {
    set ty [java::new ptolemy.graph.test.TestConstant $y]
    $ty setName Y
    set tz [java::new ptolemy.graph.test.TestConstant $z]
    $tz setName Z

    set iaw [java::new ptolemy.graph.Inequality $ta $tw]
    set iwa [java::new ptolemy.graph.Inequality $tw $ta]
    set ibz [java::new ptolemy.graph.Inequality $tb $tz]
    set iyb [java::new ptolemy.graph.Inequality $ty $tb]

    set s1 [java::new ptolemy.graph.InequalitySolver $cpo]
    $s1 addInequality $iaw
    $s1 addInequality $iwa
    $s1 addInequality $ibz
    $s1 addInequality $iyb

    set sat [$s1 solveLeast]
    list $sat [$ta getValue] [$tb getValue] \
	 [iterToInfo [$s1 bottomVariables]] [iterToInfo [$s1 topVariables]] \
	 [iterToInfo [$s1 unsatisfiedInequalities]]
} {0 w y {} A(variable)_w {{B(variable)_y Z(constant)_z}}}

######################################################################
####
# 
test InequalitySolver-2.6 {solve for greatest solutino for above} {
    set sat [$s1 solveGreatest]
    list $sat [$ta getValue] [$tb getValue] \
         [iterToInfo [$s1 bottomVariables]] [iterToInfo [$s1 topVariables]] \
         [iterToInfo [$s1 unsatisfiedInequalities]]
} {0 w z B(variable)_z A(variable)_w {{Y(constant)_y B(variable)_z}}}

######################################################################
####
# 
test InequalitySolver-3.1 {solve constraints on TypeLattic} {
    # This comes from a real topology in DE SamplerSystem.
    set tem [java::new ptolemy.data.type.TypeLattice]
    set lattice [$tem lattice]

    # init. type terms
    set pDouble [java::new ptolemy.actor.TypedIOPort]
    $pDouble setTypeEquals [[java::new ptolemy.data.DoubleToken] getClass]
    set tDouble [$pDouble getTypeTerm]

    set ps1c [java::new ptolemy.actor.TypedIOPort]
    set ts1c [$ps1c getTypeTerm]
    set ps1d [java::new ptolemy.actor.TypedIOPort]
    set ts1d [$ps1d getTypeTerm]
    set ps1o [java::new ptolemy.actor.TypedIOPort]
    set ts1o [$ps1o getTypeTerm]

    set ps2c [java::new ptolemy.actor.TypedIOPort]
    set ts2c [$ps2c getTypeTerm]
    set ps2d [java::new ptolemy.actor.TypedIOPort]
    set ts2d [$ps2d getTypeTerm]
    set ps2o [java::new ptolemy.actor.TypedIOPort]
    set ts2o [$ps2o getTypeTerm]

    # setup type constraints
    set d_s1c [java::new ptolemy.graph.Inequality $tDouble $ts1c]
    set d_s2c [java::new ptolemy.graph.Inequality $tDouble $ts2c]

    set s1d_s1o [java::new ptolemy.graph.Inequality $ts1d $ts1o]
    set s1c_s1o [java::new ptolemy.graph.Inequality $ts1c $ts1o]
    set s1o_d [java::new ptolemy.graph.Inequality $ts1o $tDouble]

    set s2d_s2o [java::new ptolemy.graph.Inequality $ts2d $ts2o]
    set s2c_s2o [java::new ptolemy.graph.Inequality $ts2c $ts2o]
    set s2o_d [java::new ptolemy.graph.Inequality $ts2o $tDouble]

    set d_s1d [java::new ptolemy.graph.Inequality $tDouble $ts1d]
    set d_s2d [java::new ptolemy.graph.Inequality $tDouble $ts2d]

    # solver constraints
    set s [java::new ptolemy.graph.InequalitySolver $lattice]
    $s addInequality $d_s1c
    $s addInequality $d_s2c
    $s addInequality $s1d_s1o
    $s addInequality $s1c_s1o
    $s addInequality $s1o_d
    $s addInequality $s2d_s2o
    $s addInequality $s2c_s2o
    $s addInequality $s2o_d
    $s addInequality $d_s1d
    $s addInequality $d_s2d

    set sat [$s solveLeast]
    list $sat \
	 [[$ps1c getType] toString] \
	 [[$ps1d getType] toString] \
	 [[$ps1o getType] toString] \
	 [[$ps2c getType] toString] \
	 [[$ps2d getType] toString] \
	 [[$ps2o getType] toString]
} {1 double double double double double double} 

