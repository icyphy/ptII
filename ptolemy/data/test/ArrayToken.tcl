# Tests for the ArrayToken class
#
# @Author: Yuhong Xiong
#
# @Version $Id$
#
# @Copyright (c) 1997-2001 The Regents of the University of California.
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
test ArrayToken-1.0 {Create a string array} {
    set val0 [java::new ptolemy.data.StringToken AB]
    set val1 [java::new ptolemy.data.StringToken CD]
    set valArray [java::new {ptolemy.data.Token[]} 2 [list $val0 $val1]]
    set valToken [java::new {ptolemy.data.ArrayToken} $valArray]

    $valToken toString
} {{"AB", "CD"}}

test ArrayToken-1.0 {Create an int array using expression} {
    set valToken [java::new {ptolemy.data.ArrayToken String} "{1, 2, 3}"]
    $valToken toString
} {{1, 2, 3}}

######################################################################
####
# 
test ArrayToken-2.0 {test add} {
    set t1 [java::new {ptolemy.data.ArrayToken String} "{1, 2, 3}"]
    set t2 [java::new {ptolemy.data.ArrayToken String} "{4, 5, 6}"]
    set tadd [$t1 add $t2]
    $tadd toString
} {{5, 7, 9}}

test ArrayToken-2.1 {test subtract} {
    set t1 [java::new {ptolemy.data.ArrayToken String} "{1, 2, 3}"]
    set t2 [java::new {ptolemy.data.ArrayToken String} "{0.5, 1.5, 6.0}"]
    set tadd [$t1 subtract $t2]
    $tadd toString
} {{0.5, 0.5, -3.0}}

