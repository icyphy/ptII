# Tests for the Fraction Class
#
# @Author: Edward A. Lee, Christopher Hylands
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

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then { 
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

####################################################################
test Fraction-1.1 {constructors} {
    set c0 [java::new ptolemy.math.Fraction]
    set c1 [java::new {ptolemy.math.Fraction int} 5]
    set c2 [java::new ptolemy.math.Fraction 5 -3]
    catch {[java::new ptolemy.math.Fraction 5 0]} s1
    set c3 [java::new {ptolemy.math.Fraction ptolemy.math.Fraction} $c1]
    set c4 [java::new ptolemy.math.Fraction 5 -15]
    set c5 [java::new ptolemy.math.Fraction 15 -3]
    set c6 [java::new ptolemy.math.Fraction 0 5]
    list [$c0 toString] [$c1 toString] [$c2 toString] $s1 [$c3 toString] \
	    [$c4 toString] [$c5 toString] [$c6 toString]
} {0/1 5/1 -5/3 {java.lang.ArithmeticException: Illegal Fraction: cannot have denominator of zero.} 5/1 -1/3 -5/1 0/1}

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
} {0 1 5 1 -5 3 5 1}

####################################################################
test Fraction-2.3 {add} {
    set c01 [$c0 add $c1]
    set c12 [$c1 add $c2]
    set c23 [$c2 add $c3]
    set c34 [$c3 add $c4]
    set c45 [$c4 add $c5]
    set c50 [$c5 add $c0]
    list [$c01 toString] [$c12 toString] [$c23 toString] [$c34 toString] \
	    [$c45 toString] [$c50 toString]
} {5/1 10/3 10/3 14/3 -16/3 -5/1}

####################################################################
test Fraction-2.4 {multiply} {
    set c01 [$c0 multiply $c1]
    set c12 [$c1 multiply $c2]
    set c23 [$c2 multiply $c3]
    set c34 [$c3 multiply $c4]
    set c45 [$c4 multiply $c5]
    set c50 [$c5 multiply $c0]
    list [$c01 toString] [$c12 toString] [$c23 toString] [$c34 toString] \
	    [$c45 toString] [$c50 toString]
} {0/1 -25/3 -25/3 -5/3 5/3 0/1}

####################################################################
test Fraction-2.5 {divide} {
    set c01 [$c0 divide $c1]
    set c12 [$c1 divide $c2]
    set c23 [$c2 divide $c3]
    set c34 [$c3 divide $c4]
    set c45 [$c4 divide $c5]
    catch {[set c50 [$c5 divide $c0]]} s1
    list [$c01 toString] [$c12 toString] [$c23 toString] [$c34 toString] \
	    [$c45 toString] $s1
} {0/1 -3/1 -1/3 -15/1 1/15 {java.lang.ArithmeticException: Division by zero!}}

####################################################################
test Fraction-2.6 {subtract} {
    set c01 [$c0 subtract $c1]
    set c12 [$c1 subtract $c2]
    set c23 [$c2 subtract $c3]
    set c34 [$c3 subtract $c4]
    set c45 [$c4 subtract $c5]
    set c50 [$c5 subtract $c0]
    list [$c01 toString] [$c12 toString] [$c23 toString] [$c34 toString] \
	    [$c45 toString] [$c50 toString]
} {-5/1 20/3 -20/3 16/3 14/3 -5/1}

####################################################################
test Fraction-2.7 {equals} {
    set c6 [java::new ptolemy.math.Fraction -1 3]
    set i0 [java::new {Integer int} 5]
    set c01 [$c0 equals $c1]
    set c12 [$c1 equals $c2]
    set c23 [$c2 equals $c3]
    set c34 [$c3 equals $c4]
    set c45 [$c4 equals $c5]
    set c46 [$c4 equals $c6]
    set ci0 [$c1 equals $i0]
    list $c01 $c12 $c23 $c34 $c45 $c46 $ci0
} {0 0 0 0 0 1 0}

####################################################################
test Fraction-2.8 {inverse} {
    catch {[set c01 [$c0 inverse]]} s1
    set c12 [$c1 inverse]
    set c23 [$c2 inverse]
    set c34 [$c3 inverse]
    set c45 [$c4 inverse]
    set c56 [$c5 inverse]
    list $s1 [$c12 toString] [$c23 toString] [$c34 toString] \
	    [$c45 toString] [$c56 toString]
} {{java.lang.ArithmeticException: Inverse of zero is undefined!} 1/5 -3/5 1/5 -3/1 -1/5}

####################################################################
test Fraction-2.9 {negate} {
    set c01 [$c0 negate]
    set c12 [$c1 negate]
    set c23 [$c2 negate]
    set c34 [$c3 negate]
    set c45 [$c4 negate]
    set c56 [$c5 negate]
    list [$c01 toString] [$c12 toString] [$c23 toString] [$c34 toString] \
	    [$c45 toString] [$c56 toString]
} {0/1 -5/1 5/3 -5/1 1/3 5/1}

test Fraction-3.1 {lcm} {
    set a1 [java::call ptolemy.math.Fraction lcm 2 8]
    set a2 [java::call ptolemy.math.Fraction lcm -8 2]
    set a3 [java::call ptolemy.math.Fraction lcm 3 7]
    set a4 [java::call ptolemy.math.Fraction lcm 28 2]
    set a5 [java::call ptolemy.math.Fraction lcm 5 -2]
    set a6 [java::call ptolemy.math.Fraction lcm -8 -7]
    list $a1 $a2 $a3 $a4 $a5 $a6
} {8 -8 21 28 -10 56}

test Fraction-3.2 {check that the bounds work} {
    set c0 [java::new ptolemy.math.Fraction [java::field Integer MAX_VALUE] \
	    [java::field Integer MAX_VALUE]]
    set c1 [java::new ptolemy.math.Fraction [java::field Integer MAX_VALUE] \
	    1]
    catch {[set c2 [java::new ptolemy.math.Fraction \
	    [java::field Integer MAX_VALUE] 0]]} s2
    set c3 [java::new ptolemy.math.Fraction 0 \
	    [java::field Integer MAX_VALUE]]
    set c4 [java::new ptolemy.math.Fraction 1 \
	    [java::field Integer MAX_VALUE]]
    list [$c0 toString] [$c1 toString] $s2 [$c3 toString] [$c4 toString] 
} {1/1 2147483647/1 {java.lang.ArithmeticException: Illegal Fraction: cannot have denominator of zero.} 0/1 1/2147483647}

test Fraction-3.3 {check overflow add} {
    set c01 [$c0 add $c1]
    set c12 [$c1 add $c2]
    set c23 [$c2 add $c3]
    set c34 [$c3 add $c4]
    set c45 [$c4 add $c5]
    set c50 [$c5 add $c0]
    list [$c01 toString] [$c12 toString] [$c23 toString] [$c34 toString] \
	    [$c45 toString] [$c50 toString]
} {-2147483648/1 715827880/1 -5/3 1/2147483647 -2147483642/2147483647 -4/1}

####################################################################
test Fraction-3.4 {check overflow multiply} {
    set c01 [$c0 multiply $c1]
    set c12 [$c1 multiply $c2]
    set c23 [$c2 multiply $c3]
    set c34 [$c3 multiply $c4]
    set c45 [$c4 multiply $c5]
    set c50 [$c5 multiply $c0]
    list [$c01 toString] [$c12 toString] [$c23 toString] [$c34 toString] \
	    [$c45 toString] [$c50 toString]
} {2147483647/1 -715827881/1 0/1 0/1 -5/2147483647 -5/1}

####################################################################
test Fraction-3.5 {check overflow divide} {
    set c01 [$c0 divide $c1]
    set c12 [$c1 divide $c2]
    catch {[set c23 [$c2 divide $c3]]} s23
    set c34 [$c3 divide $c4]
    set c45 [$c4 divide $c5]
    set c50 [$c5 divide $c0]
    list [$c01 toString] [$c12 toString] $s23 [$c34 toString] \
	    [$c45 toString] [$c50 toString]
} {1/2147483647 -429496729/1 {java.lang.ArithmeticException: Division by zero!} 0/1 -1/2147483643 -5/1}

####################################################################
test Fraction-3.6 {check overflow subtract} {
    set c01 [$c0 subtract $c1]
    set c12 [$c1 subtract $c2]
    set c23 [$c2 subtract $c3]
    set c34 [$c3 subtract $c4]
    set c45 [$c4 subtract $c5]
    set c50 [$c5 subtract $c0]
    list [$c01 toString] [$c12 toString] [$c23 toString] [$c34 toString] \
	    [$c45 toString] [$c50 toString]
} {-2147483646/1 -715827882/1 -5/3 -1/2147483647 2147483644/2147483647 -6/1}

####################################################################
test Fraction-3.7 {check overflow equals} {
    set c6 [java::new {ptolemy.math.Fraction ptolemy.math.Fraction} $c4]
    set c01 [$c0 equals $c1]
    set c12 [$c1 equals $c2]
    set c23 [$c2 equals $c3]
    set c34 [$c3 equals $c4]
    set c45 [$c4 equals $c5]
    set c46 [$c4 equals $c6]
    list $c01 $c12 $c23 $c34 $c45 $c46
} {0 0 0 0 0 1}

####################################################################
test Fraction-3.8 {check overflow inverse} {
    set c01 [$c0 inverse]
    set c12 [$c1 inverse]
    set c23 [$c2 inverse]
    catch {[set c34 [$c3 inverse]]} s2
    set c45 [$c4 inverse]
    set c56 [$c5 inverse]
    list [$c01 toString] [$c12 toString] [$c23 toString] $s2 \
	    [$c45 toString] [$c56 toString]
} {1/1 1/2147483647 -3/5 {java.lang.ArithmeticException: Inverse of zero is undefined!} 2147483647/1 -1/5}

####################################################################
test Fraction-3.9 {check overflow negate} {
    set c01 [$c0 negate]
    set c12 [$c1 negate]
    set c23 [$c2 negate]
    set c34 [$c3 negate]
    set c45 [$c4 negate]
    set c56 [$c5 negate]
    list [$c01 toString] [$c12 toString] [$c23 toString] [$c34 toString] \
	    [$c45 toString] [$c56 toString]
} {-1/1 -2147483647/1 5/3 0/1 -1/2147483647 5/1}
