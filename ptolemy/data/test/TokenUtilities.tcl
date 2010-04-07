# Tests for the TokenUtilities
#
# @Author: Christopher Brooks
#
# @Version $Id$
#
# @Copyright (c) 2010 The Regents of the University of California.
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
test TokenUtilities-1.0 {Compare Doubles vs DoubleTokens} {
    # Based on a test case by Deepak Shankar
    set aDouble [java::call -noconvert Double valueOf "3.75e-9"]
    set bDouble [java::call -noconvert Double valueOf "1.5e-14"]

    # Multiply two numbers and get the results.  
    # We can't just use expr here because Jacl converts things to strings
    set resultsMultiply [java::call ptolemy.data.test.TokenUtilitiesTest doubleMultiply $aDouble $bDouble]
    set resultsDouble [java::call -noconvert Double valueOf $resultsMultiply]

    set aDoubleToken [java::new {ptolemy.data.DoubleToken String} "3.75e-9"]
    set bDoubleToken [java::new {ptolemy.data.DoubleToken String} "1.5e-14"]

    set resultsDoubleToken [$aDoubleToken multiply $bDoubleToken]

    # The first value is "wrong", the 3 other values are right
    list \
	[expr { [$aDouble doubleValue] * [$bDouble doubleValue]}] \
	$resultsMultiply \
	[$resultsDouble toString] \
	[$resultsDoubleToken toString]
} {5.625e-23 5.624999999999999E-23 5.624999999999999E-23 5.624999999999999E-23}
