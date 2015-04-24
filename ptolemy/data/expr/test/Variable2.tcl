# Tests for the Variable class that are long standing failures.
#
# @Author: Edward A. Lee
#
# @Version $Id: Variable.tcl 67778 2013-10-26 15:50:13Z cxh $
#
# @Copyright (c) 1997-2015 The Regents of the University of California.
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

# MessageHandler tends to pop up messages about dependencies
java::call System setProperty ptolemy.ptII.batchMode true

######################################################################
####
#
test Variable2-13.0 {Test setting structured type} {
    set v [java::new ptolemy.data.expr.Variable]
    set nat [java::field ptolemy.data.type.BaseType UNKNOWN]
    set natArrayType [java::new ptolemy.data.type.ArrayType $nat]
    $v setTypeEquals $natArrayType

    set int0 [java::new ptolemy.data.IntToken 0]
    set int1 [java::new ptolemy.data.IntToken 1]
    set valArray [java::new {ptolemy.data.Token[]} 2 [list $int0 $int1]]
    set intArrayToken [java::new {ptolemy.data.ArrayToken} $valArray]

    set double0 [java::new ptolemy.data.DoubleToken 2.2]
    set double1 [java::new ptolemy.data.DoubleToken 3.3]
    set valArray [java::new {ptolemy.data.Token[]} 2 [list $double0 $double1]]
    set doubleArrayToken [java::new {ptolemy.data.ArrayToken} $valArray]

    $v setToken $intArrayToken
    $v setToken $doubleArrayToken

    list [[$v getType] toString] [[$v getToken] toString]
} {arrayType(double) {{2.2, 3.3}}}

######################################################################
####
#
test Variable2-13.2 {Test setting structured type} {
    set v [java::new ptolemy.data.expr.Variable]
    set nat [java::field ptolemy.data.type.BaseType UNKNOWN]
    set natArrayType [java::new ptolemy.data.type.ArrayType $nat]
    $v setTypeEquals $natArrayType

    set int0 [java::new ptolemy.data.IntToken 0]
    set int1 [java::new ptolemy.data.IntToken 1]
    set valArray [java::new {ptolemy.data.Token[]} 2 [list $int0 $int1]]
    set intArrayToken [java::new {ptolemy.data.ArrayToken} $valArray]

    set double0 [java::new ptolemy.data.DoubleToken 2.2]
    set double1 [java::new ptolemy.data.DoubleToken 3.3]
    set valArray [java::new {ptolemy.data.Token[]} 2 [list $double0 $double1]]
    set doubleArrayToken [java::new {ptolemy.data.ArrayToken} $valArray]
    $v setToken $doubleArrayToken
    $v setToken $intArrayToken

    list [[$v getType] toString] [[$v getToken] toString]
} {arrayType(int) {{0, 1}}}
