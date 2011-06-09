# Tests for the TypedAtomicActor class
#
# @Author: Edward A. Lee, Yuhong Xiong
#
# $Id$
#
# @Copyright (c) 2000-2006 The Regents of the University of California.
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

# NOTE:  All of the following tests use this director,
# pretty much as a dummy.
set director [java::new ptolemy.actor.Director]
set manager [java::new ptolemy.actor.Manager]

######################################################################
####
#
test TypedAtomicActor-1.1 {Constructor tests} {
    # The sole purpose of this test is to exercise TypedAtomicActor(workspace)
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setManager $manager
    $e0 setDirector $director
    $e0 setName E0
    set w [java::new ptolemy.kernel.util.Workspace W]
    set e1 [java::new ptolemy.actor.TypedAtomicActor]

    set e2 [java::new ptolemy.actor.TypedAtomicActor $w]
    set e3 [java::new ptolemy.actor.TypedAtomicActor $e0 E3]
    list [$e1 getFullName] [$e2 getFullName] [$e3 getFullName] \
	    [expr {[$e2 workspace] == $w}]
} {. . .E0.E3 1}

######################################################################
####
#
test TypedAtomicActor-2.1 {Test array valued parameter and port} {
    set e0 [sdfModel 5]
    set actor [java::new ptolemy.actor.TypedAtomicActor $e0 actor]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]

    # add parameter and port to actor
    set param [java::new ptolemy.data.expr.Parameter $actor param]
    set output [java::new ptolemy.actor.TypedIOPort $actor output false true]

    # set both the parameter and the output port to array type
    set unk [java::field ptolemy.data.type.BaseType UNKNOWN]
    set unkArrayType [java::new ptolemy.data.type.ArrayType $unk]
    $param setTypeEquals $unkArrayType
    $output setTypeEquals $unkArrayType

    # set the type of the parameter and output to be the same
    $output setTypeSameAs $param

    # set initial value of parameter
    $param setExpression {{1, 2}}

    # connect and execute
    set recIn [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    $e0 connect $output $recIn
    [$e0 getManager] execute

    list [[$param getType] toString] [[$output getType] toString] \
         [[$recIn getType] toString]
} {arrayType(int) arrayType(int) arrayType(int)}

######################################################################
####
#
test TypedAtomicActor-2.2 {Change initial value to double array} {
    $param setExpression {{1.5, 2.5}}
    [$e0 getManager] execute

    list [[$param getType] toString] [[$output getType] toString] \
         [[$recIn getType] toString]
} {arrayType(double) arrayType(double) arrayType(double)}

######################################################################
####
#
test TypedAtomicActor-3.1 {clone} {
   set actor2 [java::new ptolemy.actor.TypedAtomicActor $e0 actor2]
   catch {$actor2 clone} msg
   list $msg
} {{java.lang.CloneNotSupportedException: clone() is not supported in actors, call clone(Workspace workspace) instead. Sometimes actors are mistakenly written to have a clone() method instead of a clone(Workspace workspace) method.}}
