# Tests for the Complex Class
#
# @Author: Edward A. Lee
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
test Complex-1.1 {constructors} {
    set c0 [java::new ptolemy.math.Complex]
    set c1 [java::new ptolemy.math.Complex -0.5]
    set c2 [java::new ptolemy.math.Complex 2.0 -3.0]
    list "[$c0 toString]\n[$c1 toString]\n[$c2 toString]"
} {{0.0 + 0.0i
-0.5 + 0.0i
2.0 - 3.0i}}

####################################################################
test Complex-2.1 {abs} {
    list "[$c0 abs]\n[$c1 abs]\n[$c2 abs]"
} {{0.0
0.5
3.60555127546}}

####################################################################
test Complex-2.2 {add (static version)} {
    set c22 [java::new ptolemy.math.Complex -1.1 -1.1]
    set c23 [java::call ptolemy.math.Complex add $c2 $c22]
    $c23 toString
} {0.8999999999999999 - 4.1i}

####################################################################
test Complex-2.3 {add (non-static version)} {
    $c23 add $c22
    $c23 toString
} {-0.20000000000000018 - 5.199999999999999i}

####################################################################
test Complex-2.4 {acos} {
    set c9 [java::call ptolemy.math.Complex acos $c2]
    $c9 toString
} {1.0001435424737994 + 1.983387029916533i}

####################################################################
test Complex-2.5 {acosh} {
    set c9 [java::call ptolemy.math.Complex acosh $c2]
    $c9 toString
} {1.9833870299165355 - 1.0001435424737972i}

####################################################################
test Complex-3.1 {angle} {
    list "[$c0 angle]\n[$c1 angle]\n[$c2 angle]"
} {{0.0
3.14159265359
-0.982793723247}}

####################################################################
test Complex-3.1.1 {asin} {
    set c9 [java::call ptolemy.math.Complex asin $c2]
    $c9 toString
} {0.5706527843210994 - 1.9833870299165355i}

####################################################################
test Complex-3.1.2 {asinh} {
    set c9 [java::call ptolemy.math.Complex asinh $c2]
    $c9 toString
} {1.9686379257930964 - 0.9646585044076028i}

####################################################################
test Complex-3.1.3 {atan} {
    set c9 [java::call ptolemy.math.Complex atan $c2]
    $c9 toString
} {1.4099210495965755 - 0.2290726829685388i}

####################################################################
test Complex-3.1.4 {atanh} {
    set c9 [java::call ptolemy.math.Complex atanh $c2]
    $c9 toString
} {0.14694666622552977 - 1.3389725222944935i}

####################################################################
test Complex-3.2 {csc} {
    set c9 [java::call ptolemy.math.Complex csc $c2]
    $c9 toString
} {0.09047320975320743 - 0.04120098628857413i}

####################################################################
test Complex-4.1 {conjugate (static version)} {
    set c0p [java::call ptolemy.math.Complex conjugate $c0]
    set c1p [java::call ptolemy.math.Complex conjugate $c1]
    set c2p [java::call ptolemy.math.Complex conjugate $c2]
    list "[$c0p toString]\n[$c1p toString]\n[$c2p toString]"
} {{0.0 + 0.0i
-0.5 + 0.0i
2.0 + 3.0i}}

####################################################################
test Complex-5.1 {conjugate (non-static version)} {
    $c0p conjugate
    $c1p conjugate
    $c2p conjugate
    list "[$c0p toString]\n[$c1p toString]\n[$c2p toString]"
} {{0.0 + 0.0i
-0.5 + 0.0i
2.0 - 3.0i}}

####################################################################
test Complex-5.1.0 {cot} {
    set c9 [java::call ptolemy.math.Complex cot $c2]
    $c9 toString
} {-0.0037397103763367905 + 0.9967577965693585i}

####################################################################
test Complex-5.1.1 {cos} {
    set c9 [java::call ptolemy.math.Complex cos $c2]
    $c9 toString
} {-4.189625690968808 + 9.109227893755339i}

####################################################################
test Complex-5.1.1 {cosh} {
    set c9 [java::call ptolemy.math.Complex cosh $c2]
    $c9 toString
} {-3.724545504915323 - 0.5118225699873846i}

####################################################################
test Complex-5.2 {divide (static version)} {
    $c2p conjugate
    set div [java::call ptolemy.math.Complex divide $c2 $c2p]
    $div toString
} {-0.38461538461538464 - 0.9230769230769231i}

####################################################################
test Complex-5.3 {divide (non-static version)} {
    $c2p divide $c2
    $c2p toString
} {-0.38461538461538464 + 0.9230769230769231i}

####################################################################
test Complex-5.3.1 {exp} {
    set ec2 [java::call ptolemy.math.Complex exp $c2]
    $ec2 toString
} {-7.315110094901103 - 1.0427436562359045i}

####################################################################
test Complex-5.4 {isNaN} {
    set inf [java::call ptolemy.math.Complex divide $c2 $c0]
    list [$c2 isNaN] [$inf isNaN]
} {0 1}

####################################################################
test Complex-5.4.1 {log} {
    set lc2 [java::call ptolemy.math.Complex log $c2]
    $lc2 toString
} {1.2824746787307684 - 0.982793723247329i}

####################################################################
test Complex-5.5 {multiply (static version)} {
    set c10 [java::new ptolemy.math.Complex -1.1 -1.1]
    set c8 [java::call ptolemy.math.Complex multiply $c2 $c10]
    $c8 toString
} {-5.5 + 1.1i}

####################################################################
test Complex-5.6 {multiply (non-static version)} {
    $c10 multiply $c2
    $c10 toString
} {-5.5 + 1.1i}

####################################################################
test Complex-6.1 {negate (static version)} {
    set c0p [java::call ptolemy.math.Complex negate $c0]
    set c1p [java::call ptolemy.math.Complex negate $c1]
    set c2p [java::call ptolemy.math.Complex negate $c2]
    list "[$c0p toString]\n[$c1p toString]\n[$c2p toString]"
} {{0.0 + 0.0i
0.5 + 0.0i
-2.0 + 3.0i}}

####################################################################
test Complex-7.1 {negate (non-static version)} {
    $c0p negate
    $c1p negate
    $c2p negate
    list "[$c0p toString]\n[$c1p toString]\n[$c2p toString]"
} {{0.0 + 0.0i
-0.5 + 0.0i
2.0 - 3.0i}}

####################################################################
test Complex-8.1 {polarToComplex} {
    set c3 [java::call ptolemy.math.Complex polarToComplex 1.0 [expr $PI/2]]
    set c4 [java::call ptolemy.math.Complex polarToComplex 0.0 [expr $PI/2]]
    set c5 [java::call ptolemy.math.Complex polarToComplex -1.0 [expr -$PI]]
    list "[$c3 toString]\n[$c4 toString]\n[$c5 toString]"
} {{-5.1034119692792285E-12 + 1.0i
0.0 + 0.0i
1.0 - 2.0694557179012918E-13i}}

####################################################################
test Complex-9.1 {pow} {
    set c6 [java::new ptolemy.math.Complex -0.5 0.9]
    set c7 [java::call ptolemy.math.Complex pow $c2 $c6]
    $c7 toString
} {-0.09534790752229648 + 1.2718528818533663i}

####################################################################
test Complex-10.1 {reciprocal (static version)} {
    set c8 [java::call ptolemy.math.Complex reciprocal $c2]
    $c8 toString
} {0.15384615384615385 + 0.23076923076923078i}

####################################################################
test Complex-10.2 {reciprocal (non-static version)} {
    set c9 [java::new ptolemy.math.Complex 2.0 -3.0]
    $c9 reciprocal
    $c9 toString
} {0.15384615384615385 + 0.23076923076923078i}

####################################################################
test Complex-11.1 {scale (static version)} {
    set c8 [java::call ptolemy.math.Complex scale $c2 2.5]
    $c8 toString
} {5.0 - 7.5i}

####################################################################
test Complex-11.2 {scale (non-static version)} {
    set c9 [java::new ptolemy.math.Complex 2.0 -3.0]
    $c9 scale 2.5
    $c9 toString
} {5.0 - 7.5i}

####################################################################
test Complex-11.2.0 {sec} {
    set c9 [java::call ptolemy.math.Complex sec $c2]
    $c9 toString
} {-0.04167496441114425 - 0.09061113719623758i}

####################################################################
test Complex-11.2.1 {sin} {
    set c9 [java::call ptolemy.math.Complex sin $c2]
    $c9 toString
} {9.15449914691143 + 4.168906959966566i}

####################################################################
test Complex-11.2.2 {sinh} {
    set c9 [java::call ptolemy.math.Complex sinh $c2]
    $c9 toString
} {-3.59056458998578 - 0.5309210862485199i}

####################################################################
test Complex-11.3 {sqrt (static version)} {
    set c8 [java::call ptolemy.math.Complex sqrt $c2]
    $c8 toString
} {1.6741492280355401 - 0.895977476129838i}

####################################################################
test Complex-11.4 {sqrt (non-static version)} {
    set c9 [java::new ptolemy.math.Complex 2.0 -3.0]
    $c9 sqrt
    $c9 toString
} {1.6741492280355401 - 0.895977476129838i}

####################################################################
test Complex-12.1 {subtract (static version)} {
    set c22 [java::new ptolemy.math.Complex -1.1 -1.1]
    set c23 [java::call ptolemy.math.Complex subtract $c2 $c22]
    $c23 toString
} {3.1 - 1.9i}

####################################################################
test Complex-12.2 {subtract (non-static version)} {
    $c23 subtract $c22
    $c23 toString
} {4.2 - 0.7999999999999998i}

####################################################################
test Complex-13.1 {tan} {
    set c9 [java::call ptolemy.math.Complex tan $c2]
    $c9 toString
} {-0.0037640256415040793 - 1.0032386273536096i}

####################################################################
test Complex-11.2.2 {tanh} {
    set c9 [java::call ptolemy.math.Complex tanh $c2]
    $c9 toString
} {0.965385879022133 + 0.009884375038322507i}
