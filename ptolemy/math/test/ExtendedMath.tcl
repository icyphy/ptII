# Tests for the ExtendedMath
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1998 The Regents of the University of California.
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

# NOTE: there is way too much resolution in these numeric tests.
#  The results are unlikely to be the same on all platforms.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then { 
    source testDefs.tcl
} {}

set PI [java::field java.lang.Math PI]

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

####################################################################
test ExtendedMath-1.1 {acosh} {
    epsilonDiff [java::call ptolemy.math.ExtendedMath acosh 1.2] 0.622362503715
} {}

####################################################################
test ExtendedMath-1.2 {acosh} {
    catch {java::call ptolemy.math.ExtendedMath acosh 0.99} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ExtendedMath.acosh: Argument is required to be greater than 1.  Got 0.99}}

####################################################################
test ExtendedMath-2.1 {asinh} {
    epsilonDiff [java::call ptolemy.math.ExtendedMath asinh 1.2] 1.01597313418
} {}

####################################################################
test ExtendedMath-2.2 {asinh} {
    epsilonDiff [java::call ptolemy.math.ExtendedMath asinh .99] 0.874284812187
} {}

####################################################################
test ExtendedMath-2.3 {asinh} {
    epsilonDiff [java::call ptolemy.math.ExtendedMath asinh -0.5] -0.48121182506
} {}



