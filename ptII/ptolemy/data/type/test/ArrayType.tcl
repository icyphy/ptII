# Tests for the ArrayType class
#
# @Author: Yuhong Xiong
#
# @Version $Id$
#
# @Copyright (c) 1997-2007 The Regents of the University of California.
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
test ArrayType-1.0 {Create a {unknown}} {
    set nat [java::field ptolemy.data.type.BaseType UNKNOWN]
    set natArrayTypeMaster [java::new ptolemy.data.type.ArrayType $nat]

    set natArrayType [java::cast ptolemy.data.type.ArrayType \
                                                [$natArrayTypeMaster clone]]

    list [$natArrayTypeMaster toString] [$natArrayType toString] \
         [[$natArrayTypeMaster getElementType] toString] \
         [[$natArrayType getElementType] toString]
} {arrayType(unknown) arrayType(unknown) unknown unknown}

######################################################################
####
# 
test ArrayType-1.1 {Create a {string}} {
    set str [java::field ptolemy.data.type.BaseType STRING]
    set strArrayTypeMaster [java::new ptolemy.data.type.ArrayType $str]

    set strArrayType [java::cast ptolemy.data.type.ArrayType \
                                                 [$strArrayTypeMaster clone]]

    list [$strArrayTypeMaster toString] [$strArrayType toString] \
         [[$strArrayTypeMaster getElementType] toString] \
         [[$strArrayType getElementType] toString]
} {arrayType(string) arrayType(string) string string}

######################################################################
####
# 
test ArrayType-1.2 {Create a {general}} {
    set gen [java::field ptolemy.data.type.BaseType GENERAL]
    set genArrayTypeMaster [java::new ptolemy.data.type.ArrayType $gen]

    set genArrayType [java::cast ptolemy.data.type.ArrayType \
                                                 [$genArrayTypeMaster clone]]

    list [$genArrayTypeMaster toString] [$genArrayType toString] \
         [[$genArrayTypeMaster getElementType] toString] \
         [[$genArrayType getElementType] toString]
} {arrayType(general) arrayType(general) general general}

######################################################################
####
# 
test ArrayType-2.0 {Test isCompatible} {
    set int0 [java::new ptolemy.data.IntToken 0]
    set int1 [java::new ptolemy.data.IntToken 1]
    set valArray [java::new {ptolemy.data.Token[]} 2 [list $int0 $int1]]
    set intArrayToken [java::new {ptolemy.data.ArrayToken} $valArray]

    list [$natArrayType isCompatible [$intArrayToken getType]] \
         [$strArrayType isCompatible [$intArrayToken getType]]
} {1 1}
####
# 
test ArrayType-2.0.5 {Test isCompatible with GENERAL} {
    # Connecting a const with an array of ints to a Display was failing.
    list [$natArrayType isCompatible [java::field ptolemy.data.type.BaseType GENERAL]] \
        [$strArrayType isCompatible [java::field ptolemy.data.type.BaseType GENERAL]] \
        [$genArrayType isCompatible [java::field ptolemy.data.type.BaseType GENERAL]]
} {1 1 1}

######################################################################
####
# 
test ArrayType-2.1 {Test convert} {
    set int0 [java::new ptolemy.data.IntToken 0]
    set int1 [java::new ptolemy.data.IntToken 1]
    set valArray [java::new {ptolemy.data.Token[]} 2 [list $int0 $int1]]
    set intArrayToken [java::new {ptolemy.data.ArrayToken} $valArray]

    set c1 [$natArrayType convert $intArrayToken]
    set c2 [$strArrayType convert $intArrayToken]

    list [[$c1 getType] toString] [$c1 toString] \
         [[$c2 getType] toString] [$c2 toString]
} {arrayType(int,2) {{0, 1}} arrayType(string,2) {{"0", "1"}}}

######################################################################
####
# 
test ArrayType-3.0 {Test update} {
    $natArrayType updateType $strArrayType
    $natArrayType toString
} {arrayType(string)}

######################################################################
####
# 
test ArrayType-3.1 {Test initialize} {
    # continue from above test
    $natArrayType initialize $nat
    $natArrayType toString
} {arrayType(unknown)}

######################################################################
####
# 
test ArrayType-4.0 {Test isConstant} {
    list [$natArrayType isConstant] [$strArrayType isConstant]
} {0 1}

######################################################################
####
# 
test ArrayType-5.0 {Test equals} {
    list [$natArrayType equals $natArrayTypeMaster] \
         [$strArrayType equals $strArrayTypeMaster] \
         [$natArrayType equals $strArrayType]
} {1 1 0}

######################################################################
####
# 
test ArrayType-6.0 {Test isInstantiable} {
    list [$natArrayType isInstantiable] [$strArrayType isInstantiable]
} {0 1}

######################################################################
####
# 
test ArrayType-7.0 {Test isSubstitutionInstance} {
    list [$natArrayType isSubstitutionInstance $strArrayType] \
         [$strArrayType isSubstitutionInstance $natArrayType]
} {1 0}

