# Tests for the Fraction Class
#
# @Author: Edward A. Lee, Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1998-1999 The Regents of the University of California.
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

####################################################################
test Fraction-1.1 {constructors} {
    set c0 [java::new ptolemy.math.Fraction]
    set c1 [{java::new ptolemy.math.Fraction int} 5]
    set c2 [java::new ptolemy.math.Fraction 5 -3]
    catch {[java::new ptolemy.math.Fraction 5 0]} s1
    set c3 [{java::new ptolemy.math.Fraction ptolemy.math.Fraction} $c1]
    set c4 [java::new ptolemy.math.Fraction 5 -15]
    set c5 [java::new ptolemy.math.Fraction 15 -3]
    list [$c0 toString] [$c1 toString] [$c2 toString] $s1 [$c3 toString] \
	    [$c4 toString] [$c5 toString]
} {}

test Fraction-1.2 {getNumerator and getDenominator} {
    set n0 [$c0 getNumerator]
    set d0 [$c0 getDenominator]
    set n1 [$c1 getNumerator]
    set d1 [$c1 getDenominator]
    set n2 [$c2 getNumerator]
    set d2 [$c2 getDenominator]
    set n3 [$c3 getNumerator]
    set d3 [$c3 getDenominator]
    list $n0 $d0 $n1 $d1 $n2 $d2 $n3 $d3
} {}

####################################################################
test Fraction-2.3 {add} {
    set c01 [add $c0 $c1]
    set c12 [add $c1 $c2]
    set c23 [add $c2 $c3]
    set c34 [add $c3 $c4]
    set c45 [add $c4 $c5]
    set c50 [add $c5 $c0]
    list [$c01 toString] [$c12 toString] [$c23 toString] [$c34 toString] \
	    [$c45 toString] [$c50 toString]
} {)

####################################################################
test Fraction-2.4 {multiply} {
    set c01 [multiply $c0 $c1]
    set c12 [multiply $c1 $c2]
    set c23 [multiply $c2 $c3]
    set c34 [multiply $c3 $c4]
    set c45 [multiply $c4 $c5]
    set c50 [multiply $c5 $c0]
    list [$c01 toString] [$c12 toString] [$c23 toString] [$c34 toString] \
	    [$c45 toString] [$c50 toString]
} {)

####################################################################
test Fraction-2.5 {divide} {
    set c01 [divide $c0 $c1]
    set c12 [divide $c1 $c2]
    set c23 [divide $c2 $c3]
    set c34 [divide $c3 $c4]
    set c45 [divide $c4 $c5]
    set c50 [divide $c5 $c0]
    list [$c01 toString] [$c12 toString] [$c23 toString] [$c34 toString] \
	    [$c45 toString] [$c50 toString]
} {)

####################################################################
test Fraction-2.6 {subtract} {
    set c01 [subtract $c0 $c1]
    set c12 [subtract $c1 $c2]
    set c23 [subtract $c2 $c3]
    set c34 [subtract $c3 $c4]
    set c45 [subtract $c4 $c5]
    set c50 [subtract $c5 $c0]
    list [$c01 toString] [$c12 toString] [$c23 toString] [$c34 toString] \
	    [$c45 toString] [$c50 toString]
} {)

####################################################################
test Fraction-2.7 {equals} {
    set c6 [java::new ptolemy.math.Fraction -1 3]
    set c01 [equals $c0 $c1]
    set c12 [equals $c1 $c2]
    set c23 [equals $c2 $c3]
    set c34 [equals $c3 $c4]
    set c45 [equals $c4 $c5]
    set c46 [equals $c4 $c6]
    list [$c01 toString] [$c12 toString] [$c23 toString] [$c34 toString] \
	    [$c45 toString] [$c46 toString]
} {)

####################################################################
test Fraction-2.8 {inverse} {
    set c01 [inverse $c0]
    set c12 [inverse $c1]
    set c23 [inverse $c2]
    set c34 [inverse $c3]
    set c45 [inverse $c4]
    set c56 [inverse $c5]
    list [$c01 toString] [$c12 toString] [$c23 toString] [$c34 toString] \
	    [$c45 toString] [$c56 toString]
} {)

####################################################################
test Fraction-2.9 {negate} {
    set c01 [negate $c0]
    set c12 [negate $c1]
    set c23 [negate $c2]
    set c34 [negate $c3]
    set c45 [negate $c4]
    set c56 [negate $c5]
    list [$c01 toString] [$c12 toString] [$c23 toString] [$c34 toString] \
	    [$c45 toString] [$c56 toString]
} {}

