# Tests for the UnsignedByteToken class
#
# @Author: Yuhong Xiong
#
# @Version $Id$
#
# @Copyright (c) 1998-2002 The Regents of the University of California.
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
test UnsignedByteToken-1.0 {Create an empty instance} {
    set p [java::new ptolemy.data.UnsignedByteToken]
    $p toString
} {0}

######################################################################
####
# 
test UnsignedByteToken-1.1 {Create an instance with a value} {
    set token [java::new {ptolemy.data.UnsignedByteToken byte} 3]
    $token toString
} {3}

######################################################################
####
# 
test UnsignedByteToken-1.2 {Create an instance from a string value} {
    set token [java::new {ptolemy.data.UnsignedByteToken String} "5"]
    $token toString
} {5}

######################################################################
####
# 
test UnsignedByteToken-2.0 {Create a non-empty instance and query its value} {
    set token [java::new {ptolemy.data.UnsignedByteToken byte} 4]
    $token byteValue
} {4}

######################################################################
####
# 
test UnsignedByteToken-3.0 {Create an non-empty instance and add it to Strings} {
    set token1 [java::new ptolemy.data.StringToken "value is " ]
    set token2 [java::new {ptolemy.data.UnsignedByteToken byte} 6]
    set token3 [java::new ptolemy.data.StringToken "....." ]

    set token4 [$token1 add $token2]
    set token5 [$token2 add $token3]
    
    set token6 [$token4 add $token5]

    list [$token6 toString]
} {{"value is 66....."}}

######################################################################
####
# 
test UnsignedByteToken-4.0 {Test equals} {
    set t1 [java::new {ptolemy.data.UnsignedByteToken byte} 1]
    set t2 [java::new {ptolemy.data.UnsignedByteToken byte} 1]
    set t3 [java::new {ptolemy.data.UnsignedByteToken byte} 2]
    list [$t1 equals $t1] [$t1 equals $t2] [$t1 equals $t3]
} {1 1 0}

######################################################################
####
# 
test UnsignedByteToken-5.0 {Test hashCode} {
    set t1 [java::new {ptolemy.data.UnsignedByteToken byte} 1]
    set t2 [java::new {ptolemy.data.UnsignedByteToken byte} 1]
    set f [java::new {ptolemy.data.UnsignedByteToken byte} 2]
    list [$t1 hashCode] [$t2 hashCode] [$f hashCode]
} {1 1 2}

