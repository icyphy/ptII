# Tests for the FixPoint Class
#
# @Author: Bart Kienhuis
#
# @Version: $Id$
#
# @Copyright (c) 1998-2002 The Regents of the University of California.
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

test FixPoint-1.0 {constructors} {
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set p1 [java::new ptolemy.math.Precision "(4.32)" ]
    set c0 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 5.5734 $p0 ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 5.5734 $p1 ]
    set c2 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -4.23  $p0 ]
    set c3 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -4.23  $p1 ]
    list "
[$c0 toBitString] [ $c0 toString ][[$c0 getError] getDescription] 
[$c1 toBitString] [ $c1 toString ][[$c1 getError] getDescription] 
[$c2 toBitString] [ $c2 toString ][[$c2 getError] getDescription] 
[$c3 toBitString] [ $c3 toString ][[$c3 getError] getDescription] "
} {{
101.100100101101 5.573486328125 No overflow occurred 
101.10010010110010100101011110101000 5.573400000110269 No overflow occurred 
-101.110001010010 -4.22998046875 No overflow occurred 
-101.11000101000111101011100001010010 -4.2299999999813735 No overflow occurred }}

test FixPoint-1.1 {constructors} {
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set p1 [java::new ptolemy.math.Precision "(16/3)" ]
    set p2 [java::new ptolemy.math.Precision "(16/2)" ]
    set p3 [java::new ptolemy.math.Precision "(16/1)" ]
    set c0 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 5.5734 $p0 ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 5.5734 $p1 ]
    set c2 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 5.5734 $p2 ]
    set c3 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 5.5734 $p3 ]
    list "
[$c0 toBitString] [ $c0 toString ][[$c0 getError] getDescription] 
[$c1 toBitString] [ $c1 toString ][[$c1 getError] getDescription] 
[$c2 toBitString] [ $c2 toString ][[$c2 getError] getDescription] 
[$c3 toBitString] [ $c3 toString ][[$c3 getError] getDescription] "
} {{
101.100100101101 5.573486328125 No overflow occurred 
11.1111111111111 3.9998779296875 Overflow occurred 
1.11111111111111 1.99993896484375 Overflow occurred 
0.111111111111111 0.999969482421875 Overflow occurred }}

test FixPoint-1.2 {constructors} {
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set p1 [java::new ptolemy.math.Precision "(16/3)" ]
    set p2 [java::new ptolemy.math.Precision "(16/2)" ]
    set p3 [java::new ptolemy.math.Precision "(16/1)" ]
    set c0 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -5.5734 $p0 ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -5.5734 $p1 ]
    set c2 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -5.5734 $p2 ]
    set c3 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -5.5734 $p3 ]
    list "
[$c0 toBitString] [ $c0 toString ][[$c0 getError] getDescription] 
[$c1 toBitString] [ $c1 toString ][[$c1 getError] getDescription] 
[$c2 toBitString] [ $c2 toString ][[$c2 getError] getDescription] 
[$c3 toBitString] [ $c3 toString ][[$c3 getError] getDescription] "
} {{
-110.011011010011 -5.573486328125 No overflow occurred 
-100.0000000000000 -4.0 Overflow occurred 
-10.00000000000000 -2.0 Overflow occurred 
-1.000000000000000 -1.0 Overflow occurred }}

test FixPoint-1.3 {constructors} {
    set p0 [java::new ptolemy.math.Precision "(1.0)" ]
    set p1 [java::new ptolemy.math.Precision "(1.1)" ]
    set p2 [java::new ptolemy.math.Precision "(2.0)" ]
    set c0 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 1 $p0 ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -1 $p0 ]
    set c2 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 0 $p0 ]
    set c3 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 1 $p1 ]
    set c4 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -1 $p1 ]
    set c5 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 0 $p1 ]
    set c6 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 1 $p2 ]
    set c7 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -1 $p2 ]
    set c8 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 0 $p2 ]
    list "
[$c0 toBitString] [ $c0 toString ][[$c0 getError] getDescription]
[$c1 toBitString] [ $c1 toString ][[$c1 getError] getDescription] 
[$c2 toBitString] [ $c2 toString ][[$c2 getError] getDescription]
[$c3 toBitString] [ $c3 toString ][[$c3 getError] getDescription]
[$c4 toBitString] [ $c4 toString ][[$c4 getError] getDescription] 
[$c5 toBitString] [ $c5 toString ][[$c5 getError] getDescription]
[$c6 toBitString] [ $c6 toString ][[$c6 getError] getDescription]
[$c7 toBitString] [ $c7 toString ][[$c7 getError] getDescription] 
[$c8 toBitString] [ $c8 toString ][[$c8 getError] getDescription] "
} {{
0 0.0 Overflow occurred
-1 -1.0 No overflow occurred 
0 0.0 No overflow occurred
0.1 0.5 Overflow occurred
-1.0 -1.0 No overflow occurred 
0.0 0.0 No overflow occurred
1 1.0 No overflow occurred
-1 -1.0 No overflow occurred 
0 0.0 No overflow occurred }}

test FixPoint-1.4 {constructors} {
    catch { set p0 [java::new ptolemy.math.Precision "(0.0)" ] } msg
    set c0 [java::call ptolemy.math.Quantizer \
		{round double ptolemy.math.Precision} 1 $p0 ]
    list $msg
} {{java.lang.IllegalArgumentException: Incorrect definition of Precision. A FixPoint requires the use of at least a single integer bit to represent the sign.}}


####################################################################

test FixPoint-2.1 {add} {
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set p1 [java::new ptolemy.math.Precision "(4.32)" ]
    set c20 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 5.5734 $p0 ]
    set c21 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -4.23  $p0 ]
    set c22 [$c20 add $c21]
    list "[$c22 toBitString] [[$c22 getPrecision] toString]"
} {{1.010101111111 (4.12)}}


test FixPoint-2.2 {add} {
    set p2 [java::new ptolemy.math.Precision "(16/5)" ]
    set c24 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 5.5734 $p0 ]
    set c25 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -4.23  $p2 ]
    set c26 [$c24 add $c25]
    list "[$c26 toBitString] [[$c26 getPrecision] toString]"
} {{1.010101111111 (5.12)}}

test FixPoint-2.3 {add} {
    set p3 [java::new ptolemy.math.Precision "(16/3)" ]
    set c24 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 5.5734 $p0 ]
    set c25 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -4.23  $p3]
    set c26 [$c24 add $c25]
    list "[$c26 toBitString] [[$c26 getPrecision] toString]"
} {{1.1001001011010 (4.13)}}

test FixPoint-2.4 {add} {
    set p4 [java::new ptolemy.math.Precision "(14/5)" ]
    set c24 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 5.5734 $p0 ]
    set c25 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -4.23  $p4]
    set c26 [$c24 add $c25]
    list "[$c26 toBitString] [[$c26 getPrecision] toString]"
} {{1.010101111101 (5.12)}}

test FixPoint-2.5 {add} {
    set p5 [java::new ptolemy.math.Precision "(14/3)" ]
    set c24 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 5.5734 $p0 ]
    set c25 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -4.23  $p5]
    set c26 [$c24 add $c25]
    list "[$c26 toBitString] [[$c26 getPrecision] toString]"
} {{1.100100101101 (4.12)}}

####################################################################

test FixPoint-3.1 {subtract} {
    set p6 [java::new ptolemy.math.Precision "(16/6)" ]
    set c31 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 5.5734 $p6 ]
    set c32 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -4.23  $p6 ]
    set c33 [$c31 subtract $c32]
    list "[$c33 toBitString] [[$c33 getPrecision] toString]"
} {{1001.1100110111 (6.10)}}

test FixPoint-3.2 {subtract} {
    set c31 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 5.5734 $p6 ]
    set c32 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -4.23  $p0 ]
    set c33 [$c31 subtract $c32]
    list "[$c33 toBitString] [[$c33 getPrecision] toString]"
} {{1001.110011011010 (6.12)}}

####################################################################

test FixPoint-4.1 {multiply} {
    set c41 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 5.5734 $p6 ]
    set c42 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 4.23   $p6 ]
    set c43 [$c41 multiply $c42]
    list "[$c43 toBitString] [[$c43 getPrecision] toString]"
} {{10111.10010011110100100100 (6.20)}}

test FixPoint-4.2 {multiply} {
    set c44 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 7.5734 $p0 ]
    set c45 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -7.23  $p0 ]
    set c46 [$c44 multiply $c45]
    list "[$c46 toBitString] [[$c46 getPrecision] toString]"    
} {{-110111.001111100110110001101010 (7.24)}}

test FixPoint-4.3 {multiply} {
    set c47 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 15.5734 $p6 ]
    set c48 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 7.23    $p0 ]
    set c49 [$c47 multiply $c48]
    list "[$c49 toBitString] [[$c49 getPrecision] toString]"   
} {{1110000.100110000001111111101000 (8.24)}}

####################################################################

test FixPoint-5.1 {divide} {
    set c51 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 5.5734 $p6 ]
    set c52 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 4.23   $p6 ]
    set c53 [$c51 divide $c52]
    list "[$c53 toBitString] [[$c53 getPrecision] toString]"    
} {{1.0101000101 (6.10)}}

test FixPoint-5.2 {divide} {
    set c54 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 7.5734 $p0 ]
    set c55 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -7.23  $p0 ]
    set c56 [$c54 divide $c55]
    list "[$c56 toBitString] [[$c56 getPrecision] toString]"    
} {{-10.111100111101 (4.12)}}

test FixPoint-5.3 {divide} {
    set p7 [java::new ptolemy.math.Precision "(32/4)" ]	
    set c57 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 7.5734 $p7 ]
    set c58 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -7.23  $p7 ]
    set c59 [$c57 divide $c58]
    list "[$c59 toBitString] [[$c59 getPrecision] toString]"    
} {{-10.1111001111010111010001000100 (4.28)}}

####################################################################

test FixPoint-6.1 {equal} {
    set p8  [java::new ptolemy.math.Precision "(32/5)" ]	
    set p9  [java::new ptolemy.math.Precision "(33/5)" ]	
    set p10 [java::new ptolemy.math.Precision "(30/4)" ]	
    
    set c61 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 7.5734 $p7 ]	
    set c62 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 7.5734 $p8 ]
    set c63 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 7.5734 $p9 ]
    set c64 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 7.5734 $p10 ]

    list \
	    [$c61 {equals ptolemy.math.FixPoint} $c61 ] \
	    [$c61 {equals ptolemy.math.FixPoint} $c62 ] \
	    [$c61 {equals ptolemy.math.FixPoint} $c63 ] \
	    [$c61 {equals ptolemy.math.FixPoint} $c64 ] \
	    [$c62 {equals ptolemy.math.FixPoint} $c61 ]


} {1 1 1 0 1}

####################################################################

test FixPoint-7.1 {absolute} {

    list \
	    [[$c20 abs] toBitString ] \
	    [[$c21 abs] toBitString ]\
	    [[$c61 abs] toBitString ]

} {101.100100101101 100.001110101110 111.1001001011001010010101111010}

test FixPoint-7.2 {truncate} {
    set p6 [java::new ptolemy.math.Precision "(6/3)" ]
    set c65 [java::call ptolemy.math.Quantizer \
	    {truncate double ptolemy.math.Precision} -2.4 $p6 ]
    set c66 [java::call ptolemy.math.Quantizer \
	    {truncate double ptolemy.math.Precision} 2.4 $p6 ]
    list [$c65 toBitString] [$c65 doubleValue] [$c66 toBitString] [$c66 doubleValue] 
} {-11.101 -2.375 10.011 2.375}
