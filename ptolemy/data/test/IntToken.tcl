# Tests for the IntToken class
#
# @Author: Neil Smyth
#
# @Version $Id$
#
# @Copyright (c) 1997- The Regents of the University of California.
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
test IntToken-1.0 {Create an empty instance} {
    set p [java::new ptolemy.data.IntToken]
    $p toString
} {ptolemy.data.IntToken(0)}

######################################################################
####
# 
test IntToken-1.1 {Create a non-empty instance from an int} {
    set p [java::new {ptolemy.data.IntToken int} 5]
    $p toString
} {ptolemy.data.IntToken(5)}

######################################################################
####
# 
test IntToken-1.0 {Create a non-empty instance from an String} {
    set p [java::new {ptolemy.data.IntToken String} "7"]
    $p toString
} {ptolemy.data.IntToken(7)}

######################################################################
####
# 
test IntToken-2.0 {Create a non-empty instance and query its value as an int} {
    set p [java::new {ptolemy.data.IntToken int} 3]
    set res1 [$p intValue]
    set res2 [$p getValue]
    list $res1 $res2
} {3 3}

######################################################################
####
# 
test IntToken-2.1 {Create a non-empty instance and query its value as a double} {
    set p [java::new {ptolemy.data.IntToken int} 12]
    $p doubleValue
} {12.0}

######################################################################
####
# 
test IntToken-2.2 {Create a non-empty instance and query its value as a long} {
    set p [java::new {ptolemy.data.IntToken int} 12]
    $p longValue
} {12}

######################################################################
####
# 
test IntToken-2.3 {Create a non-empty instance and query its value as a string} {
    set p [java::new {ptolemy.data.IntToken int} 12]
    $p stringValue
} {12}

######################################################################
####
# 
#test IntToken-2.4 {Create a non-empty instance and query its value as a complex#} {
#    set p [java::new {ptolemy.data.IntToken int} 12]
#    $p complexValue
#} {12}

######################################################################
####
# 
