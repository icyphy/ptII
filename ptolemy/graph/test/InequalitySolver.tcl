# Tests for InequalityTerm, Inequality, and InequalitySolver
#
# @Author: Yuhong Xiong
#
# $Id$
#
# @Copyright (c) 1997-1998 The Regents of the University of California.
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
    set tx [java::new ptolemy.graph.test.TestConstant $x]
    set ta [java::new ptolemy.graph.test.TestVariable]
    set tb [java::new ptolemy.graph.test.TestVariable]

    set iaw [java::new ptolemy.graph.Inequality $ta $tw]
    set ibx [java::new ptolemy.graph.Inequality $tb $tx]
    set iba [java::new ptolemy.graph.Inequality $tb $ta]
    set iab [java::new ptolemy.graph.Inequality $ta $tb]

    list [$tw isSettable] [$ta isSettable]
} {0 1}

######################################################################
####
# 
test InequalitySolver-2.3 {solver the above constraints} {
    set s [java::new ptolemy.graph.InequalitySolver $cpo]
    $s addInequality $iaw
    $s addInequality $ibx
    $s addInequality $iba
    $s addInequality $iab

    $s solveLeast
    list [$ta getValue] [$tb getValue] \
} {z z}

