# Tests for the new type system
#
# @Author: Steve Neuendorffer
#
# @Version $Id$
#
# @Copyright (c) 1997-1999 The Regents of the University of California.
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
} {}

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
test TypeResolver-1.0 {datatypes} {
    set t1 [java::new ptolemy.data.type.DataType \
	    [java::field ptolemy.data.type.Data INT]]
    set t2 [java::new ptolemy.data.type.DataType]
    list [$t1 toString] [$t2 toString]
} {}

test TypeResolver-1.1 {datatypes} {
    set i1 [java::new ptolemy.graph.Inequality $t1 $t2]
    
    set l1 [java::new collections.LinkedList]
    $l1 insertLast $i1
    set e1 [$l1 elements]
    set s1 [java::new ptolemy.data.type.DataTypeResolver]
    set c1 [$s1 resolveTypes $e1]

    list [$t1 toString] [$t2 toString] [$c1 hasMoreElements]
} {}

######################################################################
####
# 
test TypeResolver-2.0 {arraytypes} {
    set dim [java::new {int[]} {2} {1 1}]
    set tdim [java::new ptolemy.data.type.Dimension 2 $dim]
    set t1 [java::new ptolemy.data.type.DimensionType $tdim]
    set t2 [java::new ptolemy.data.type.DimensionType]

    list [$t1 toString] [$t2 toString]
} {}

test TypeResolver-2.1 {arraytypes} {
    set i1 [java::new ptolemy.graph.Inequality $t1 $t2]
    
    set l1 [java::new collections.LinkedList]
    $l1 insertLast $i1
    set e1 [$l1 elements]
    set s1 [java::new ptolemy.data.type.DimensionTypeResolver]
    set c1 [$s1 resolveTypes $e1]

    list [$t1 toString] [$t2 toString] [$c1 hasMoreElements]
} {}

######################################################################
####
# 
test TypeResolver-3.0 {typesystem} {
    set data1 [java::new ptolemy.data.type.DataType \
	    [java::field ptolemy.data.type.Data INT]]
    set data2 [java::new ptolemy.data.type.DataType]

    set dim [java::new {int[]} {2} {1 1}]
    set tdim [java::new ptolemy.data.type.Dimension 2 $dim]
    set dim1 [java::new ptolemy.data.type.DimensionType $tdim]
    set dim2 [java::new ptolemy.data.type.DimensionType]

    set t1 [java::new ptolemy.data.type.ArrayType $data1 $dim1]
    set t2 [java::new ptolemy.data.type.ArrayType $data2 $dim2]

    set i1 [java::new ptolemy.graph.Inequality $t1 $t2]
    
    set l1 [java::new collections.LinkedList]
    $l1 insertLast $i1
    set e1 [$l1 elements]
    set s1 [java::new ptolemy.data.type.TypeSystem]
    set c1 [$s1 resolveTypes $e1]

    list [$t1 toString] [$t2 toString] [$c1 hasMoreElements]
} {}

