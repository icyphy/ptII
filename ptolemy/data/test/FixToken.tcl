# Tests for the FixToken class
#
# @Author: Bart Kienhuis
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
test FixToken-1.0 {Create an empty instance} {
    set p [java::new ptolemy.data.FixToken]
    $p toString
} {ptolemy.data.FixToken(0.0)}

######################################################################
test FixToken-1.1 {Create a non-empty instance from two strings} {
    set p [java::new ptolemy.data.FixToken "(16/4)" 5.5734]
    $p toString
} {ptolemy.data.FixToken(5.5732421875)}

test FixToken-1.2 {Create a non-empty instance from two strings} {
    set p [java::new ptolemy.data.FixToken "(4^32)" 5.5734]
    $p toString
} {ptolemy.data.FixToken(5.573399998247623)}

test FixToken-1.3 {Create a non-empty instance from an String} {
    set ft [java::new ptolemy.data.FixToken "(4.12)" "7.7734"]
    $ft toString
} {ptolemy.data.FixToken(7.773193359375)}


######################################################################
test FixToken-2.1 {Test additive identity} {
    set p [java::new ptolemy.data.FixToken "(16/4)" 12.2]
    set token [$p zero]
    list [$token toString]
} {ptolemy.data.FixToken(0.0)}

test FixToken-2.2 {Test multiplicative identity} {
    set p [java::new ptolemy.data.FixToken "(16/4)" 12.2]
    set token [$p one]
    list [$token toString]
} {ptolemy.data.FixToken(1.0)}

######################################################################
test FixToken-3.1 {Test Addition} {
    set pa [java::new ptolemy.data.FixToken "(16/4)" 3.2334454232]
    set pb [java::new ptolemy.data.FixToken "(16/4)" -1.5454325]

    set res [$pa add $pb]
    
    list [$pa toString] [$pb toString] [$res toString]

} {ptolemy.data.FixToken(3.2333984375) ptolemy.data.FixToken(-1.54541015625) ptolemy.data.FixToken(1.68798828125)}

test FixToken-3.2 {Test Subtraction} {
    set pa [java::new ptolemy.data.FixToken "(16/4)" 3.2334454232]
    set pb [java::new ptolemy.data.FixToken "(16/4)" -1.5454325]

    set res [$pa subtract $pb]
    
    list [$res toString]

} {ptolemy.data.FixToken(4.77880859375)}

test FixToken-3.3 {Test Multiply} {
    set pa [java::new ptolemy.data.FixToken "(16/4)" 3.2334454232]
    set pb [java::new ptolemy.data.FixToken "(16/4)" -1.5454325]

    set res [$pa multiply $pb]
    
    list [$res toString]

} {ptolemy.data.FixToken(-4.996926784515381)}

test FixToken-3.4 {Test Divide} {
    set pa [java::new ptolemy.data.FixToken "(16/4)" 3.2334454232]
    set pb [java::new ptolemy.data.FixToken "(16/4)" -1.5454325]

    set res [$pa divide $pb]
    
    list [$res toString]

} {ptolemy.data.FixToken(-2.092041015625)}

######################################################################
test FixToken-4.1 {Change the precision of a FixToken} {
    set pa [java::new ptolemy.data.FixToken "(16/4)" 3.2334454232]
    set pk [ $pa scaleToPrecision "(32/16)"  ]   
    set j [$pk fixpointValue ]
    list "[$pa toString]\n[$pk toString]\n[$j getErrorDescription]"
} {{ptolemy.data.FixToken(3.2333984375)
ptolemy.data.FixToken(3.2333984375)
No Overflow Occurred}}

test FixToken-4.2 {Change the precision of a FixToken} {
    set pa [java::new ptolemy.data.FixToken "(16/4)" 3.2334454232]
    set pk [ $pa scaleToPrecision "(8/4)"  ]   
    set j [$pk fixpointValue ]
    list "[$pk toString]\n[$j getErrorDescription]"
} {{ptolemy.data.FixToken(3.1875)
Rounding Occurred}}


test FixToken-4.3 {Change the precision of a FixToken} {
    set pa [java::new ptolemy.data.FixToken "(16/4)" 3.2334454232]
    set pk [ $pa scaleToPrecision "(8/1)"  ]   
    set j [$pk fixpointValue ]
    list "[$pk toString]\n[$j getErrorDescription]"
} {{ptolemy.data.FixToken(1.9921875)
Overflow Occurred}}
