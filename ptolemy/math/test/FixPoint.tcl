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
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set p1 [java::new ptolemy.math.Precision "(4^32)" ]
    set c0 [java::call ptolemy.math.Quantizer round 5.5734 $p0 ]
    set c1 [java::call ptolemy.math.Quantizer round 5.5734 $p1 ]
    list "[$c0 toBitString]\n[$c1 toBitString]"
} {{101.100100101101
101.1001001011001010010101111010}}

####################################################################

test FixPoint-2.1 {add} {
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set p1 [java::new ptolemy.math.Precision "(4^32)" ]
    set c20 [java::call ptolemy.math.Quantizer round 5.5734 $p0 ]
    set c21 [java::call ptolemy.math.Quantizer round -4.23  $p0 ]
    set c22 [$c20 add $c21]
    $c22 toBitString
} {1.10101111111}


test FixPoint-2.2 {add} {
    set p2 [java::new ptolemy.math.Precision "(16/5)" ]
    set c24 [java::call ptolemy.math.Quantizer round 5.5734 $p0 ]
    set c25 [java::call ptolemy.math.Quantizer round -4.23  $p2 ]
    set c26 [$c24 add $c25]
    $c26 toBitString
} {1.10101111111}

test FixPoint-2.3 {add} {
    set p3 [java::new ptolemy.math.Precision "(16/3)" ]
    set c24 [java::call ptolemy.math.Quantizer round 5.5734 $p0 ]
    set c25 [java::call ptolemy.math.Quantizer round -4.23  $p3]
    set c26 [$c24 add $c25]
    $c26 toBitString
} {1.1001001011010}

test FixPoint-2.4 {add} {
    set p4 [java::new ptolemy.math.Precision "(14/5)" ]
    set c24 [java::call ptolemy.math.Quantizer round 5.5734 $p0 ]
    set c25 [java::call ptolemy.math.Quantizer round -4.23  $p4]
    set c26 [$c24 add $c25]
    $c26 toBitString
} {1.10101111101}

test FixPoint-2.5 {add} {
    set p5 [java::new ptolemy.math.Precision "(14/3)" ]
    set c24 [java::call ptolemy.math.Quantizer round 5.5734 $p0 ]
    set c25 [java::call ptolemy.math.Quantizer round -4.23  $p5]
    set c26 [$c24 add $c25]
    $c26 toBitString
} {1.100100101101}

####################################################################

test FixPoint-3.1 {subtract} {
    set p6 [java::new ptolemy.math.Precision "(16/6)" ]
    set c31 [java::call ptolemy.math.Quantizer round 5.5734 $p6 ]
    set c32 [java::call ptolemy.math.Quantizer round -4.23  $p6 ]
    set c33 [$c31 subtract $c32]
    $c33 toBitString
} {1001.1100110111}

test FixPoint-3.2 {subtract} {
    set c31 [java::call ptolemy.math.Quantizer round 5.5734 $p6 ]
    set c32 [java::call ptolemy.math.Quantizer round -4.23  $p0 ]
    set c33 [$c31 subtract $c32]
    $c33 toBitString
} {1001.110011011010}

####################################################################


test FixPoint-4.1 {multiply} {
    set c41 [java::call ptolemy.math.Quantizer round 5.5734 $p6 ]
    set c42 [java::call ptolemy.math.Quantizer round 4.23   $p6 ]
    set c43 [$c41 multiply $c42]
    $c43 toBitString
} {10111.10010011110100100100}

test FixPoint-4.2 {multiply} {
    set c44 [java::call ptolemy.math.Quantizer round 7.5734 $p0 ]
    set c45 [java::call ptolemy.math.Quantizer round -7.23  $p0 ]
    set c46 [$c44 multiply $c45]
    $c46 toBitString
} {-110111.1111100110110001101010}

test FixPoint-4.3 {multiply} {
    set c47 [java::call ptolemy.math.Quantizer round 15.5734 $p6 ]
    set c48 [java::call ptolemy.math.Quantizer round 7.23    $p0 ]
    set c49 [$c47 multiply $c48]
    $c49 toBitString
} {1110000.100110000001111111101000}


####################################################################

test FixPoint-5.1 {divide} {
    set c51 [java::call ptolemy.math.Quantizer round 5.5734 $p6 ]
    set c52 [java::call ptolemy.math.Quantizer round 4.23   $p6 ]
    set c53 [$c51 divide $c52]
    $c53 toBitString
} {1.101000101}

test FixPoint-5.2 {divide} {
    set c54 [java::call ptolemy.math.Quantizer round 7.5734 $p0 ]
    set c55 [java::call ptolemy.math.Quantizer round -7.23  $p0 ]
    set c56 [$c54 divide $c55]
    $c56 toBitString
} {-10.111100111101}

test FixPoint-5.3 {divide} {
    set p7 [java::new ptolemy.math.Precision "(32/4)" ]	
    set c57 [java::call ptolemy.math.Quantizer round 7.5734 $p7 ]
    set c58 [java::call ptolemy.math.Quantizer round -7.23  $p7 ]
    set c59 [$c57 divide $c58]
    $c59 toBitString
} {-10.1111001111010111010001000100}

####################################################################
