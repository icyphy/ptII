# Tests for the FixPoint Class
#
# @Author: Bart Kienhuis
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

set PI [java::field java.lang.Math PI]

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

####################################################################
test FixPoint-1.1 {constructors} {
    set c0 [java::new ptolemy.math.FixPoint]
    set c1 [java::new ptolemy.math.FixPoint "(16/4)" 5.5734 ]
    set c2 [java::new ptolemy.math.FixPoint "(4^32)" 5.5734 ]
    list "[$c0 toString]\n[$c1 toString]\n[$c2 toString]"
} {{0.0
101.100100101100
101.1001001011001010010101111010}}

####################################################################
test FixPoint-2.1 {add} {
    set c21 [java::new ptolemy.math.FixPoint "(16/4)" 5.5734 ]
    set c22 [java::new ptolemy.math.FixPoint "(16/4)" -4.23 ]
    set c23 [$c21 add $c22]
    $c23 toString
} {1.10101111110}


test FixPoint-2.2 {add} {
    set c24 [java::new ptolemy.math.FixPoint "(16/4)" 5.5734 ]
    set c25 [java::new ptolemy.math.FixPoint "(16/5)" -4.23  ]
    set c26 [$c24 add $c25]
    $c26 toString
} {1.10101111110}

test FixPoint-2.3 {add} {
    set c24 [java::new ptolemy.math.FixPoint "(16/4)" 5.5734 ]
    set c25 [java::new ptolemy.math.FixPoint "(16/3)" -4.23  ]
    set c26 [$c24 add $c25]
    $c26 toString
} {1.1001001011000}

test FixPoint-2.4 {add} {
    set c24 [java::new ptolemy.math.FixPoint "(16/4)" 5.5734 ]
    set c25 [java::new ptolemy.math.FixPoint "(14/5)" -4.23  ]
    set c26 [$c24 add $c25]
    $c26 toString
} {1.10110000100}

test FixPoint-2.5 {add} {
    set c24 [java::new ptolemy.math.FixPoint "(16/4)" 5.5734 ]
    set c25 [java::new ptolemy.math.FixPoint "(14/3)" -4.23  ]
    set c26 [$c24 add $c25]
    $c26 toString
} {1.100100101100}

####################################################################
test FixPoint-3.1 {subtract} {
    set c31 [java::new ptolemy.math.FixPoint "(16/6)" 5.5734 ]
    set c32 [java::new ptolemy.math.FixPoint "(16/6)" -4.23 ]
    set c33 [$c31 subtract $c32]
    $c33 toString
} {1001.1100110110}

test FixPoint-3.2 {subtract} {
    set c31 [java::new ptolemy.math.FixPoint "(16/6)" 5.5734 ]
    set c32 [java::new ptolemy.math.FixPoint "(16/4)" -4.23 ]
    set c33 [$c31 subtract $c32]
    $c33 toString
} {1001.110011011010}

####################################################################
test FixPoint-4.1 {multiply} {
    set c41 [java::new ptolemy.math.FixPoint "(16/6)" 5.5734 ]
    set c42 [java::new ptolemy.math.FixPoint "(16/6)" 4.23 ]
    set c43 [$c41 multiply $c42]
    $c43 toString
} {10111.10010010011011011001}

test FixPoint-4.2 {multiply} {
    set c44 [java::new ptolemy.math.FixPoint "(16/4)" 7.5734 ]
    set c45 [java::new ptolemy.math.FixPoint "(16/4)" -7.23 ]
    set c46 [$c44 multiply $c45]
    $c46 toString
} {-110111.1111101110000000011000}

test FixPoint-4.2 {multiply} {
    set c47 [java::new ptolemy.math.FixPoint "(16/6)" 15.5734 ]
    set c48 [java::new ptolemy.math.FixPoint "(16/4)" 7.23 ]
    set c49 [$c47 multiply $c48]
    $c49 toString
} {1110000.100110000001111111101000}

####################################################################
test FixPoint-5.1 {divide} {
    set c51 [java::new ptolemy.math.FixPoint "(16/6)" 5.5734 ]
    set c52 [java::new ptolemy.math.FixPoint "(16/6)" 4.23 ]
    set c53 [$c51 divide $c52]
    $c53 toString
} {1.101000101}

test FixPoint-5.2 {divide} {
    set c54 [java::new ptolemy.math.FixPoint "(16/4)" 7.5734 ]
    set c55 [java::new ptolemy.math.FixPoint "(16/4)" -7.23 ]
    set c56 [$c54 divide $c55]
    $c56 toString
} {-10.111100111110}

test FixPoint-5.3 {divide} {
    set c57 [java::new ptolemy.math.FixPoint "(32/4)" 7.5734 ]
    set c58 [java::new ptolemy.math.FixPoint "(32/4)" -7.23 ]
    set c59 [$c57 divide $c58]
    $c59 toString
} {-10.1111001111010111010001000100}

####################################################################

test FixPoint-6.1 {scale up SATURATE} {
     set c61 [java::new ptolemy.math.FixPoint "(32/4)" 7.5734 ]
     $c61 setRounding "SATURATE"     
     set c62 [$c61 scaleToPrecision "(6.4)" ]
     set c63 [$c62 scaleToPrecision "(1.3)" ]
     set c64 [$c63 scaleToPrecision "(1^30)" ]
    list "[$c61 toString]\n[$c61 getErrorDescription]\n[$c62 toString]\n[$c62 getErrorDescription]\n[$c63 toString]\n[$c63 getErrorDescription]\n[$c64 toString]\n[$c64 getErrorDescription]" } {{111.1001001011001010010101111010
No Overflow Occurred
111.1001
Rounding Occurred
1.111
Overflow Occurred
1.11111111111111111111111111111
Overflow Occurred}}

test FixPoint-6.2 {scale up SATURATE} {
     set c61 [java::new ptolemy.math.FixPoint "(32/4)" 7.625 ]
     $c61 setRounding "SATURATE"     
     set c62 [$c61 scaleToPrecision "(6.4)" ] 
     set c63 [$c61 scaleToPrecision "(2.2)" ] 
     list "[$c61 toString]\n[$c62 toString]\n[$c62 getErrorDescription]\n[$c63 toString]\n[$c63 getErrorDescription]"    
 } {{111.1010000000000000000000000000
111.1010
No Overflow Occurred
11.11
Overflow Occurred}}

test FixPoint-6.3 {scale up ZERO_SATURATE} {
    set c61 [java::new ptolemy.math.FixPoint "(32/4)" 7.625 ]
    $c61 setRounding "ZERO_SATURATE"     
    set c62 [$c61 scaleToPrecision "(6.4)" ] 
    set c63 [$c61 scaleToPrecision "(2.2)" ] 
    list "[$c61 toString]\n[$c62 toString]\n[$c62 getErrorDescription]\n[$c63 toString]\n[$c63 getErrorDescription]"    
} {{111.1010000000000000000000000000
111.1010
No Overflow Occurred
0.0
Overflow Occurred}}

test FixPoint-6.4 {scale TRUNCATED} {
    set c61 [java::new ptolemy.math.FixPoint "(32/4)" 7.625 ]
    $c61 setRounding "TRUNCATE"     
    set c62 [$c61 scaleToPrecision "(6.4)" ] 
    set c63 [$c61 scaleToPrecision "(2.2)" ] 
    list "[$c61 toString]\n[$c62 toString]\n[$c62 getErrorDescription]\n[$c63 toString]\n[$c63 getErrorDescription]"    
} {{111.1010000000000000000000000000
111.1010
No Overflow Occurred
11.10
Overflow Occurred}}


####################################################################
