# Tests for deep codegen
#
# @Author: Steve Neuendorffer, Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2000-2005 The Regents of the University of California.
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

# Ptolemy II bed, see /users/cxh/ptII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs jdkClassPathSeparator] == "" } then { 
    source [file join $PTII util testsuite jdktools.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
#set VERBOSE 1

if {[info procs sootCodeGeneration] == "" } then { 
    source [file join $PTII util testsuite codegen.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


######################################################################
####
#

proc _doUnboxingTest {className} {

}

proc _doExecuteTest {className} {
    global PTII
    set outputDir [file join [pwd] testOutput $className]
    puts "$PTII;$outputDir"
    exec java -classpath "$PTII;$outputDir" $className
}
  
proc _type {name} {
    java::field ptolemy.data.type.BaseType $name
}

set lattice [java::new ptolemy.copernicus.java.TypeSpecializerAnalysis\$JavaTypeLattice]

test JavaTypeLattice-1.1 {Test compare} {
    $lattice compare [_type DOUBLE] [_type DOUBLE]
} {0}

test JavaTypeLattice-1.2 {Test compare} {
    $lattice compare [_type INT] [_type DOUBLE]
} {2}

test JavaTypeLattice-1.3 {Test compare} {
    $lattice compare [_type LONG] [_type DOUBLE]
} {2}

test JavaTypeLattice-1.4 {Test compare} {
    $lattice compare [_type GENERAL] [_type DOUBLE]
} {1}

test JavaTypeLattice-1.5 {Test compare} {
    $lattice compare [_type LONG] [_type GENERAL]
} {-1}


test JavaTypeLattice-2.1 {Test leastUpperBound} {
    [$lattice leastUpperBound [_type DOUBLE] [_type DOUBLE]] toString
} {double}

test JavaTypeLattice-2.2 {Test leastUpperBound} {
    [$lattice leastUpperBound [_type INT] [_type DOUBLE]] toString
} {general}

test JavaTypeLattice-2.3 {Test leastUpperBound} {
    [$lattice leastUpperBound [_type LONG] [_type DOUBLE]] toString
} {general}

test JavaTypeLattice-2.4 {Test leastUpperBound} {
    [$lattice leastUpperBound [_type GENERAL] [_type DOUBLE]] toString
} {general}

test JavaTypeLattice-2.5 {Test leastUpperBound} {
    [$lattice leastUpperBound [_type LONG] [_type GENERAL]] toString
} {general}

test JavaTypeLattice-2.6 {Test leastUpperBound} {
    [$lattice leastUpperBound [_type UNKNOWN] [_type DOUBLE]] toString
} {double}

test JavaTypeLattice-2.7 {Test leastUpperBound} {
    [$lattice leastUpperBound [_type LONG] [_type UNKNOWN]] toString
} {long}


test JavaTypeLattice-2.1 {Test greatestLowerBound} {
    [$lattice greatestLowerBound [_type DOUBLE] [_type DOUBLE]] toString
} {double}

test JavaTypeLattice-2.2 {Test greatestLowerBound} {
    [$lattice greatestLowerBound [_type INT] [_type DOUBLE]] toString
} {unknown}

test JavaTypeLattice-2.3 {Test greatestLowerBound} {
    [$lattice greatestLowerBound [_type LONG] [_type DOUBLE]] toString
} {unknown}

test JavaTypeLattice-2.4 {Test greatestLowerBound} {
    [$lattice greatestLowerBound [_type GENERAL] [_type DOUBLE]] toString
} {double}

test JavaTypeLattice-2.5 {Test greatestLowerBound} {
    [$lattice greatestLowerBound [_type LONG] [_type GENERAL]] toString
} {long}

test JavaTypeLattice-2.6 {Test greatestLowerBound} {
    [$lattice greatestLowerBound [_type UNKNOWN] [_type DOUBLE]] toString
} {unknown}

test JavaTypeLattice-2.7 {Test greatestLowerBound} {
    [$lattice greatestLowerBound [_type LONG] [_type UNKNOWN]] toString
} {unknown}
