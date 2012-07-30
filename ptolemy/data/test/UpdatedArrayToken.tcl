# Tests for the UpdatedArrayToken class
#
# @Author: Christopher Brooks
#
# @Version $Id: ArrayToken.tcl 59854 2010-11-16 18:47:42Z crawl $
#
# @Copyright (c) 2012 The Regents of the University of California.
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

######################################################################
####
# 
test UpdatedArrayToken-1.0 {Create a string array} {
    set val0 [java::new ptolemy.data.StringToken AB]
    set val1 [java::new ptolemy.data.StringToken CD]
    set valArray [java::new {ptolemy.data.Token[]} 2 [list $val0 $val1]]
    set valToken [java::new {ptolemy.data.ArrayToken} $valArray]

    set result1 [$valToken toString]
    set val2 [java::new ptolemy.data.StringToken EF]
    set updatedArrayToken [java::new ptolemy.data.UpdatedArrayToken $valToken 0 $val2] 
    list $result1 [$updatedArrayToken toString]
} {{{"AB", "CD"}} {{"EF", "CD"}}}

test UpdatedArrayToken-2.0 {test equals()} {
    set updatedArrayToken2 [java::new ptolemy.data.UpdatedArrayToken $valToken 0 $val0] 
    set updatedArrayToken3 [java::new ptolemy.data.UpdatedArrayToken $updatedArrayToken  0 $val0] 
    list \
	[$updatedArrayToken2 equals $valToken] \
	[list \
	     [$valToken equals $updatedArrayToken] \
	     [$valToken equals $updatedArrayToken2] \
	     [$valToken equals $updatedArrayToken3]] \
	[list \
	     [$updatedArrayToken equals $valToken] \
	     [$updatedArrayToken equals $updatedArrayToken] \
	     [$updatedArrayToken equals $updatedArrayToken2] \
	     [$updatedArrayToken equals $updatedArrayToken3]] \
	[list \
	     [$updatedArrayToken2 equals $valToken] \
	     [$updatedArrayToken2 equals $updatedArrayToken] \
	     [$updatedArrayToken2 equals $updatedArrayToken2] \
	     [$updatedArrayToken2 equals $updatedArrayToken3]] \
	[list \
	     [$updatedArrayToken3 equals $valToken] \
	     [$updatedArrayToken3 equals $updatedArrayToken] \
	     [$updatedArrayToken3 equals $updatedArrayToken2] \
	     [$updatedArrayToken3 equals $updatedArrayToken3]]

} {1 {0 1 1} {0 1 0 0} {1 0 1 1} {1 0 1 1}}
