# Tests for the StringParameter class
#
# @Author: Christopher Brooks
#
# @Version $Id$
#
# @Copyright (c) 2009 The Regents of the University of California.
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

# 
#


######################################################################
####
# 
test StringParameter-10.0 {test a nesting problem} {
    set top [java::new ptolemy.kernel.CompositeEntity]
    set p1 [java::new ptolemy.data.expr.StringParameter $top p1]
    $p1 setExpression "foo"
    set compositeEntity1 [java::new ptolemy.kernel.CompositeEntity $top compositeEntity1]
    set p2_1 [java::new ptolemy.data.expr.StringParameter $compositeEntity1 p2]
    $p2_1 setExpression "bar"

    set compositeEntity2 [java::new ptolemy.kernel.CompositeEntity $compositeEntity1 compositeActor2]
    set p2_2 [java::new ptolemy.data.expr.StringParameter $compositeEntity2 p2]
    $p2_2 setExpression "bif"
    
    set expressionActor [java::new ptolemy.actor.lib.Expression $compositeEntity2 expressionActor]
    set expressionParameter [java::field $expressionActor expression]
    $expressionParameter setExpression {$p2}
    $p2_2 validate
    $p2_2 getExpression

    #puts [$top exportMoML]
    $compositeEntity2 setContainer [java::null]
    $p1 setExpression "elvis"

} {}

test StringParameter-10.1 {test a nesting problem} {
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser reset
    $parser setMoMLFilters [java::null]
    $parser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    $parser addMoMLFilter [java::new \
	    ptolemy.moml.filter.RemoveGraphicalClasses]
    $parser purgeAllModelRecords
    set toplevel [$parser parseFile StringParameterDialogBug.xml]
    set toplevel [java::cast ptolemy.kernel.CompositeEntity $toplevel]
    set CompositeActor2 [$toplevel getEntity CompositeActor1.CompositeActor2]
    $CompositeActor2 setContainer [java::null]
    set p1 [java::cast ptolemy.data.expr.Parameter [$toplevel getAttribute p1]]
    $p1 setExpression {bif}
} {}
