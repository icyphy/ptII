# Tests for the ExtendedMath
#
# @Author: Christopher Hylands, Jeff Tsay
#
# @Version: $Id$
#
# @Copyright (c) 1998-2000 The Regents of the University of California.
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
#### ExtendedMathApply
# Apply mathop to a number of arguments, returning the results.
#
proc ExtendedMathApply {mathoperation expectedresults {testvalues {}}} {
    set results {}
    set operationresults {}
    if {$testvalues == {} } {
	set testvalues [list -1.01 -0.5 0 0.5 .99 1.2 \
		[java::field java.lang.Math PI]]
    }
    foreach testvalue $testvalues expectedresult $expectedresults {
	if [catch { set operationresult [java::call ptolemy.math.ExtendedMath \
		$mathoperation $testvalue]} errMsg] {
	    set diffresult $errMsg
	} else {
	    lappend operationresults $operationresult
	    set diffresult [epsilonDiff $operationresult $expectedresult]
	}
	if {"$diffresult" != ""} {
	    lappend results $diffresult
	}
    }

    if {"$results" != ""} {
	# If we got an error, include the results that was actually returned
	set results "Result was:$operationresults\nErrors:$results"
    }

    return $results
}

####################################################################
test ExtendedMath-1.1 {acosh} {
    ExtendedMathApply acosh {0.622362503715 1.81152627246} \
	    [list 1.2 [java::field java.lang.Math PI]]
} {}

####################################################################
test ExtendedMath-1.2 {acosh} {
    catch {java::call ptolemy.math.ExtendedMath acosh 0.99} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ExtendedMath.acosh: Argument is required to be greater than 1.  Got 0.99}}

####################################################################
test ExtendedMath-2.0 {asinh} {
    ExtendedMathApply asinh {-0.888427006734 -0.48121182506 0.0 0.48121182506 0.874284812187 1.01597313418 1.86229574331}
} {}

####################################################################
test ExtendedMath-3.0 {cosh} {
    ExtendedMathApply cosh {1.55490999729 1.12762596521 1.0 1.12762596521 1.53140558169 1.81065556732 11.5919532755}
} {}

test ExtendedMath-5.0 {log2} {
    ExtendedMathApply log2  {1.0 1.80735492205760 14.82113618574405 -8.11778737810714} {2.0 3.5 28947.2 0.0036}
} {}

test ExtendedMath-6.0 {log10} {
    ExtendedMathApply log10  {1.0 0.54406804435028 4.46160656172981 -2.44369749923271} {10.0 3.5 28947.2 0.0036}
} {}

####################################################################
test ExtendedMath-7.0 {sinh} {
    ExtendedMathApply sinh {-1.19069101772 -0.521095305494 0.0 0.521095305494 1.15982889066 1.50946135541 11.5487393573}
} {}

####################################################################
test ExtendedMath-8.0 {sgn} {
    ExtendedMathApply sgn {-1 -1 1 1 1 1 1}
} {}
