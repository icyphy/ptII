# Tests for the Rounding Class
#
# @Author: Ed.Willink and Mike.Wirthlin
#
# @Version: $Id$
#
# @Copyright (c) 1998-2005 The Regents of the University of California.
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

# _Rounding.tcl is first so as to avoid problems with BigIntegers in
# Jacl

####################################################################

test Rounding-1.0 {names} {
    set round_ceiling [java::call ptolemy.math.Rounding forName "ceiling"];
    set round_convergent [java::call ptolemy.math.Rounding forName "convergent"];
    set round_down [java::call ptolemy.math.Rounding forName "down"];
    set round_floor [java::call ptolemy.math.Rounding forName "floor"];
    set round_general [java::call ptolemy.math.Rounding forName "general"];
    set round_half_ceiling [java::call ptolemy.math.Rounding forName "half_ceiling"];
    set round_half_down [java::call ptolemy.math.Rounding forName "half_down"];
    set round_half_even [java::call ptolemy.math.Rounding forName "half_even"];
    set round_half_floor [java::call ptolemy.math.Rounding forName "half_floor"];
    set round_half_up [java::call ptolemy.math.Rounding getName "half_up"];
    set round_nearest [java::call ptolemy.math.Rounding getName "nearest"];
    set round_truncate [java::call ptolemy.math.Rounding getName "truncate"];
    set round_unknown [java::call ptolemy.math.Rounding getName "unknown"];
    set round_unnecessary [java::call ptolemy.math.Rounding getName "unnecessary"];
    set round_up [java::call ptolemy.math.Rounding getName "up"];
    catch { set round_zzz [java::call ptolemy.math.Rounding getName "zzz"]; } msg
    set big_pos_even [java::new java.math.BigInteger "4" ]
    set big_pos_odd [java::new java.math.BigInteger "5" ]
    set big_neg_even [java::new java.math.BigInteger "-6" ]
    set big_neg_odd [java::new java.math.BigInteger "-5" ]

    set bi1 [ java::new java.math.BigDecimal "-2.00000" ]
    set bi2 [ java::new java.math.BigDecimal "-2.00001" ]
    set bi3 [ java::new java.math.BigDecimal "-2.49999" ]
    set bi4 [ java::new java.math.BigDecimal "-2.50000" ]
    set bi5 [ java::new java.math.BigDecimal "-2.50001" ]
    set bi6 [ java::new java.math.BigDecimal "-2.99999" ]

    set bi7 [ java::new java.math.BigDecimal "-1.00000" ]
    set bi8 [ java::new java.math.BigDecimal "-1.00001" ]
    set bi9 [ java::new java.math.BigDecimal "-1.49999" ]
    set bi10 [ java::new java.math.BigDecimal "-1.50000" ]
    set bi11 [ java::new java.math.BigDecimal "-1.50001" ]
    set bi12 [ java::new java.math.BigDecimal "-1.99999" ]

    set bi13 [ java::new java.math.BigDecimal "-0.00000" ]
    set bi14 [ java::new java.math.BigDecimal "-0.00001" ]
    set bi15 [ java::new java.math.BigDecimal "-0.49999" ]
    set bi16 [ java::new java.math.BigDecimal "-0.50000" ]
    set bi17 [ java::new java.math.BigDecimal "-0.50001" ]
    set bi18 [ java::new java.math.BigDecimal "-0.99999" ]

    set bi19 [ java::new java.math.BigDecimal "0.00000" ]
    set bi20 [ java::new java.math.BigDecimal "0.00001" ]
    set bi21 [ java::new java.math.BigDecimal "0.49999" ]
    set bi22 [ java::new java.math.BigDecimal "0.50000" ]
    set bi23 [ java::new java.math.BigDecimal "0.50001" ]
    set bi24 [ java::new java.math.BigDecimal "0.99999" ]

    set bi25 [ java::new java.math.BigDecimal "1.00000" ]
    set bi26 [ java::new java.math.BigDecimal "1.00001" ]
    set bi27 [ java::new java.math.BigDecimal "1.49999" ]
    set bi28 [ java::new java.math.BigDecimal "1.50000" ]
    set bi29 [ java::new java.math.BigDecimal "1.50001" ]
    set bi30 [ java::new java.math.BigDecimal "1.99999" ]

    set bi31 [ java::new java.math.BigDecimal "2.00000" ]
    set bi32 [ java::new java.math.BigDecimal "2.00001" ]
    set bi33 [ java::new java.math.BigDecimal "2.49999" ]
    set bi34 [ java::new java.math.BigDecimal "2.50000" ]
    set bi35 [ java::new java.math.BigDecimal "2.50001" ]
    set bi36 [ java::new java.math.BigDecimal "2.99999" ]


    list "
[ $round_ceiling toString ] 
[ $round_convergent toString ] 
[ $round_down toString ] 
[ $round_floor toString ] 
[ $round_general toString ] 
[ $round_half_ceiling toString ] 
[ $round_half_down toString ] 
[ $round_half_even toString ] 
[ $round_half_floor toString ] 
[ $round_half_up toString ] 
[ $round_nearest toString ] 
[ $round_truncate toString ] 
[ $round_unknown toString ] 
[ $round_unnecessary toString ] 
[ $round_up toString ] 
$msg "
} {{
ceiling 
half_even 
down 
floor 
up 
half_ceiling 
half_down 
half_even 
half_floor 
half_up 
half_ceiling 
floor 
up 
half_up 
up 
java.lang.IllegalArgumentException: Unknown rounding strategy "zzz". }}

####################################################################
test Overflow-1.5 {clone} {
    set clone [$round_ceiling clone]
    list \
	[$clone equals $round_ceiling] \
	[expr {[$clone hashCode] == [$round_ceiling hashCode]}] \
	[$clone equals $round_convergent] \
	[expr {[$clone hashCode] == [$round_convergent hashCode]}]
} {1 1 0 0}



####################################################################

test Rounding-2.0 {round_ceiling} {

    list "
[$bi1 toString] [[$round_ceiling round $bi1] toString]
[$bi2 toString] [[$round_ceiling round $bi2] toString]
[$bi3 toString] [[$round_ceiling round $bi3] toString]
[$bi4 toString] [[$round_ceiling round $bi4] toString]
[$bi5 toString] [[$round_ceiling round $bi5] toString]
[$bi6 toString] [[$round_ceiling round $bi6] toString]
[$bi7 toString] [[$round_ceiling round $bi7] toString]
[$bi8 toString] [[$round_ceiling round $bi8] toString]
[$bi9 toString] [[$round_ceiling round $bi9] toString]
[$bi10 toString] [[$round_ceiling round $bi10] toString]
[$bi11 toString] [[$round_ceiling round $bi11] toString]
[$bi12 toString] [[$round_ceiling round $bi12] toString]
[$bi13 toString] [[$round_ceiling round $bi13] toString]
[$bi14 toString] [[$round_ceiling round $bi14] toString]
[$bi15 toString] [[$round_ceiling round $bi15] toString]
[$bi16 toString] [[$round_ceiling round $bi16] toString]
[$bi17 toString] [[$round_ceiling round $bi17] toString]
[$bi18 toString] [[$round_ceiling round $bi18] toString]
[$bi19 toString] [[$round_ceiling round $bi19] toString]
[$bi20 toString] [[$round_ceiling round $bi20] toString]
[$bi21 toString] [[$round_ceiling round $bi21] toString]
[$bi22 toString] [[$round_ceiling round $bi22] toString]
[$bi23 toString] [[$round_ceiling round $bi23] toString]
[$bi24 toString] [[$round_ceiling round $bi24] toString]
[$bi25 toString] [[$round_ceiling round $bi25] toString]
[$bi26 toString] [[$round_ceiling round $bi26] toString]
[$bi27 toString] [[$round_ceiling round $bi27] toString]
[$bi28 toString] [[$round_ceiling round $bi28] toString]
[$bi29 toString] [[$round_ceiling round $bi29] toString]
[$bi30 toString] [[$round_ceiling round $bi30] toString]
[$bi31 toString] [[$round_ceiling round $bi31] toString]
[$bi32 toString] [[$round_ceiling round $bi32] toString]
[$bi33 toString] [[$round_ceiling round $bi33] toString]
[$bi34 toString] [[$round_ceiling round $bi34] toString]
[$bi35 toString] [[$round_ceiling round $bi35] toString]
[$bi36 toString] [[$round_ceiling round $bi36] toString] "
} {{
-2.00000 -2
-2.00001 -2
-2.49999 -2
-2.50000 -2
-2.50001 -2
-2.99999 -2
-1.00000 -1
-1.00001 -1
-1.49999 -1
-1.50000 -1
-1.50001 -1
-1.99999 -1
0.00000 0
-0.00001 0
-0.49999 0
-0.50000 0
-0.50001 0
-0.99999 0
0.00000 0
0.00001 1
0.49999 1
0.50000 1
0.50001 1
0.99999 1
1.00000 1
1.00001 2
1.49999 2
1.50000 2
1.50001 2
1.99999 2
2.00000 2
2.00001 3
2.49999 3
2.50000 3
2.50001 3
2.99999 3 }}

test Rounding-2.1 {round_floor} {
    list "
[$bi1 toString] [[$round_floor round $bi1] toString]
[$bi2 toString] [[$round_floor round $bi2] toString]
[$bi3 toString] [[$round_floor round $bi3] toString]
[$bi4 toString] [[$round_floor round $bi4] toString]
[$bi5 toString] [[$round_floor round $bi5] toString]
[$bi6 toString] [[$round_floor round $bi6] toString]
[$bi7 toString] [[$round_floor round $bi7] toString]
[$bi8 toString] [[$round_floor round $bi8] toString]
[$bi9 toString] [[$round_floor round $bi9] toString]
[$bi10 toString] [[$round_floor round $bi10] toString]
[$bi11 toString] [[$round_floor round $bi11] toString]
[$bi12 toString] [[$round_floor round $bi12] toString]
[$bi13 toString] [[$round_floor round $bi13] toString]
[$bi14 toString] [[$round_floor round $bi14] toString]
[$bi15 toString] [[$round_floor round $bi15] toString]
[$bi16 toString] [[$round_floor round $bi16] toString]
[$bi17 toString] [[$round_floor round $bi17] toString]
[$bi18 toString] [[$round_floor round $bi18] toString]
[$bi19 toString] [[$round_floor round $bi19] toString]
[$bi20 toString] [[$round_floor round $bi20] toString]
[$bi21 toString] [[$round_floor round $bi21] toString]
[$bi22 toString] [[$round_floor round $bi22] toString]
[$bi23 toString] [[$round_floor round $bi23] toString]
[$bi24 toString] [[$round_floor round $bi24] toString]
[$bi25 toString] [[$round_floor round $bi25] toString]
[$bi26 toString] [[$round_floor round $bi26] toString]
[$bi27 toString] [[$round_floor round $bi27] toString]
[$bi28 toString] [[$round_floor round $bi28] toString]
[$bi29 toString] [[$round_floor round $bi29] toString]
[$bi30 toString] [[$round_floor round $bi30] toString]
[$bi31 toString] [[$round_floor round $bi31] toString]
[$bi32 toString] [[$round_floor round $bi32] toString]
[$bi33 toString] [[$round_floor round $bi33] toString]
[$bi34 toString] [[$round_floor round $bi34] toString]
[$bi35 toString] [[$round_floor round $bi35] toString]
[$bi36 toString] [[$round_floor round $bi36] toString] "
} {{
-2.00000 -2
-2.00001 -3
-2.49999 -3
-2.50000 -3
-2.50001 -3
-2.99999 -3
-1.00000 -1
-1.00001 -2
-1.49999 -2
-1.50000 -2
-1.50001 -2
-1.99999 -2
0.00000 0
-0.00001 -1
-0.49999 -1
-0.50000 -1
-0.50001 -1
-0.99999 -1
0.00000 0
0.00001 0
0.49999 0
0.50000 0
0.50001 0
0.99999 0
1.00000 1
1.00001 1
1.49999 1
1.50000 1
1.50001 1
1.99999 1
2.00000 2
2.00001 2
2.49999 2
2.50000 2
2.50001 2
2.99999 2 }}

test Rounding-2.2 {round_down} {
    list "
[$bi1 toString] [[$round_down round $bi1] toString]
[$bi2 toString] [[$round_down round $bi2] toString]
[$bi3 toString] [[$round_down round $bi3] toString]
[$bi4 toString] [[$round_down round $bi4] toString]
[$bi5 toString] [[$round_down round $bi5] toString]
[$bi6 toString] [[$round_down round $bi6] toString]
[$bi7 toString] [[$round_down round $bi7] toString]
[$bi8 toString] [[$round_down round $bi8] toString]
[$bi9 toString] [[$round_down round $bi9] toString]
[$bi10 toString] [[$round_down round $bi10] toString]
[$bi11 toString] [[$round_down round $bi11] toString]
[$bi12 toString] [[$round_down round $bi12] toString]
[$bi13 toString] [[$round_down round $bi13] toString]
[$bi14 toString] [[$round_down round $bi14] toString]
[$bi15 toString] [[$round_down round $bi15] toString]
[$bi16 toString] [[$round_down round $bi16] toString]
[$bi17 toString] [[$round_down round $bi17] toString]
[$bi18 toString] [[$round_down round $bi18] toString]
[$bi19 toString] [[$round_down round $bi19] toString]
[$bi20 toString] [[$round_down round $bi20] toString]
[$bi21 toString] [[$round_down round $bi21] toString]
[$bi22 toString] [[$round_down round $bi22] toString]
[$bi23 toString] [[$round_down round $bi23] toString]
[$bi24 toString] [[$round_down round $bi24] toString]
[$bi25 toString] [[$round_down round $bi25] toString]
[$bi26 toString] [[$round_down round $bi26] toString]
[$bi27 toString] [[$round_down round $bi27] toString]
[$bi28 toString] [[$round_down round $bi28] toString]
[$bi29 toString] [[$round_down round $bi29] toString]
[$bi30 toString] [[$round_down round $bi30] toString]
[$bi31 toString] [[$round_down round $bi31] toString]
[$bi32 toString] [[$round_down round $bi32] toString]
[$bi33 toString] [[$round_down round $bi33] toString]
[$bi34 toString] [[$round_down round $bi34] toString]
[$bi35 toString] [[$round_down round $bi35] toString]
[$bi36 toString] [[$round_down round $bi36] toString] "
} {{
-2.00000 -2
-2.00001 -2
-2.49999 -2
-2.50000 -2
-2.50001 -2
-2.99999 -2
-1.00000 -1
-1.00001 -1
-1.49999 -1
-1.50000 -1
-1.50001 -1
-1.99999 -1
0.00000 0
-0.00001 0
-0.49999 0
-0.50000 0
-0.50001 0
-0.99999 0
0.00000 0
0.00001 0
0.49999 0
0.50000 0
0.50001 0
0.99999 0
1.00000 1
1.00001 1
1.49999 1
1.50000 1
1.50001 1
1.99999 1
2.00000 2
2.00001 2
2.49999 2
2.50000 2
2.50001 2
2.99999 2 }}

test Rounding-2.3 {round_up} {
    list "
[$bi1 toString] [[$round_up round $bi1] toString]
[$bi2 toString] [[$round_up round $bi2] toString]
[$bi3 toString] [[$round_up round $bi3] toString]
[$bi4 toString] [[$round_up round $bi4] toString]
[$bi5 toString] [[$round_up round $bi5] toString]
[$bi6 toString] [[$round_up round $bi6] toString]
[$bi7 toString] [[$round_up round $bi7] toString]
[$bi8 toString] [[$round_up round $bi8] toString]
[$bi9 toString] [[$round_up round $bi9] toString]
[$bi10 toString] [[$round_up round $bi10] toString]
[$bi11 toString] [[$round_up round $bi11] toString]
[$bi12 toString] [[$round_up round $bi12] toString]
[$bi13 toString] [[$round_up round $bi13] toString]
[$bi14 toString] [[$round_up round $bi14] toString]
[$bi15 toString] [[$round_up round $bi15] toString]
[$bi16 toString] [[$round_up round $bi16] toString]
[$bi17 toString] [[$round_up round $bi17] toString]
[$bi18 toString] [[$round_up round $bi18] toString]
[$bi19 toString] [[$round_up round $bi19] toString]
[$bi20 toString] [[$round_up round $bi20] toString]
[$bi21 toString] [[$round_up round $bi21] toString]
[$bi22 toString] [[$round_up round $bi22] toString]
[$bi23 toString] [[$round_up round $bi23] toString]
[$bi24 toString] [[$round_up round $bi24] toString]
[$bi25 toString] [[$round_up round $bi25] toString]
[$bi26 toString] [[$round_up round $bi26] toString]
[$bi27 toString] [[$round_up round $bi27] toString]
[$bi28 toString] [[$round_up round $bi28] toString]
[$bi29 toString] [[$round_up round $bi29] toString]
[$bi30 toString] [[$round_up round $bi30] toString]
[$bi31 toString] [[$round_up round $bi31] toString]
[$bi32 toString] [[$round_up round $bi32] toString]
[$bi33 toString] [[$round_up round $bi33] toString]
[$bi34 toString] [[$round_up round $bi34] toString]
[$bi35 toString] [[$round_up round $bi35] toString]
[$bi36 toString] [[$round_up round $bi36] toString] "
} {{
-2.00000 -2
-2.00001 -3
-2.49999 -3
-2.50000 -3
-2.50001 -3
-2.99999 -3
-1.00000 -1
-1.00001 -2
-1.49999 -2
-1.50000 -2
-1.50001 -2
-1.99999 -2
0.00000 0
-0.00001 -1
-0.49999 -1
-0.50000 -1
-0.50001 -1
-0.99999 -1
0.00000 0
0.00001 1
0.49999 1
0.50000 1
0.50001 1
0.99999 1
1.00000 1
1.00001 2
1.49999 2
1.50000 2
1.50001 2
1.99999 2
2.00000 2
2.00001 3
2.49999 3
2.50000 3
2.50001 3
2.99999 3 }}

test Rounding-2.4 {round_half_up} {
    list "
[$bi1 toString] [[$round_half_up round $bi1] toString]
[$bi2 toString] [[$round_half_up round $bi2] toString]
[$bi3 toString] [[$round_half_up round $bi3] toString]
[$bi4 toString] [[$round_half_up round $bi4] toString]
[$bi5 toString] [[$round_half_up round $bi5] toString]
[$bi6 toString] [[$round_half_up round $bi6] toString]
[$bi7 toString] [[$round_half_up round $bi7] toString]
[$bi8 toString] [[$round_half_up round $bi8] toString]
[$bi9 toString] [[$round_half_up round $bi9] toString]
[$bi10 toString] [[$round_half_up round $bi10] toString]
[$bi11 toString] [[$round_half_up round $bi11] toString]
[$bi12 toString] [[$round_half_up round $bi12] toString]
[$bi13 toString] [[$round_half_up round $bi13] toString]
[$bi14 toString] [[$round_half_up round $bi14] toString]
[$bi15 toString] [[$round_half_up round $bi15] toString]
[$bi16 toString] [[$round_half_up round $bi16] toString]
[$bi17 toString] [[$round_half_up round $bi17] toString]
[$bi18 toString] [[$round_half_up round $bi18] toString]
[$bi19 toString] [[$round_half_up round $bi19] toString]
[$bi20 toString] [[$round_half_up round $bi20] toString]
[$bi21 toString] [[$round_half_up round $bi21] toString]
[$bi22 toString] [[$round_half_up round $bi22] toString]
[$bi23 toString] [[$round_half_up round $bi23] toString]
[$bi24 toString] [[$round_half_up round $bi24] toString]
[$bi25 toString] [[$round_half_up round $bi25] toString]
[$bi26 toString] [[$round_half_up round $bi26] toString]
[$bi27 toString] [[$round_half_up round $bi27] toString]
[$bi28 toString] [[$round_half_up round $bi28] toString]
[$bi29 toString] [[$round_half_up round $bi29] toString]
[$bi30 toString] [[$round_half_up round $bi30] toString]
[$bi31 toString] [[$round_half_up round $bi31] toString]
[$bi32 toString] [[$round_half_up round $bi32] toString]
[$bi33 toString] [[$round_half_up round $bi33] toString]
[$bi34 toString] [[$round_half_up round $bi34] toString]
[$bi35 toString] [[$round_half_up round $bi35] toString]
[$bi36 toString] [[$round_half_up round $bi36] toString] "
} {{
-2.00000 -2
-2.00001 -2
-2.49999 -2
-2.50000 -3
-2.50001 -3
-2.99999 -3
-1.00000 -1
-1.00001 -1
-1.49999 -1
-1.50000 -2
-1.50001 -2
-1.99999 -2
0.00000 0
-0.00001 0
-0.49999 0
-0.50000 -1
-0.50001 -1
-0.99999 -1
0.00000 0
0.00001 0
0.49999 0
0.50000 1
0.50001 1
0.99999 1
1.00000 1
1.00001 1
1.49999 1
1.50000 2
1.50001 2
1.99999 2
2.00000 2
2.00001 2
2.49999 2
2.50000 3
2.50001 3
2.99999 3 }}

test Rounding-2.5 {round_half_down} {
    list "
[$bi1 toString] [[$round_half_down round $bi1] toString]
[$bi2 toString] [[$round_half_down round $bi2] toString]
[$bi3 toString] [[$round_half_down round $bi3] toString]
[$bi4 toString] [[$round_half_down round $bi4] toString]
[$bi5 toString] [[$round_half_down round $bi5] toString]
[$bi6 toString] [[$round_half_down round $bi6] toString]
[$bi7 toString] [[$round_half_down round $bi7] toString]
[$bi8 toString] [[$round_half_down round $bi8] toString]
[$bi9 toString] [[$round_half_down round $bi9] toString]
[$bi10 toString] [[$round_half_down round $bi10] toString]
[$bi11 toString] [[$round_half_down round $bi11] toString]
[$bi12 toString] [[$round_half_down round $bi12] toString]
[$bi13 toString] [[$round_half_down round $bi13] toString]
[$bi14 toString] [[$round_half_down round $bi14] toString]
[$bi15 toString] [[$round_half_down round $bi15] toString]
[$bi16 toString] [[$round_half_down round $bi16] toString]
[$bi17 toString] [[$round_half_down round $bi17] toString]
[$bi18 toString] [[$round_half_down round $bi18] toString]
[$bi19 toString] [[$round_half_down round $bi19] toString]
[$bi20 toString] [[$round_half_down round $bi20] toString]
[$bi21 toString] [[$round_half_down round $bi21] toString]
[$bi22 toString] [[$round_half_down round $bi22] toString]
[$bi23 toString] [[$round_half_down round $bi23] toString]
[$bi24 toString] [[$round_half_down round $bi24] toString]
[$bi25 toString] [[$round_half_down round $bi25] toString]
[$bi26 toString] [[$round_half_down round $bi26] toString]
[$bi27 toString] [[$round_half_down round $bi27] toString]
[$bi28 toString] [[$round_half_down round $bi28] toString]
[$bi29 toString] [[$round_half_down round $bi29] toString]
[$bi30 toString] [[$round_half_down round $bi30] toString]
[$bi31 toString] [[$round_half_down round $bi31] toString]
[$bi32 toString] [[$round_half_down round $bi32] toString]
[$bi33 toString] [[$round_half_down round $bi33] toString]
[$bi34 toString] [[$round_half_down round $bi34] toString]
[$bi35 toString] [[$round_half_down round $bi35] toString]
[$bi36 toString] [[$round_half_down round $bi36] toString] "
} {{
-2.00000 -2
-2.00001 -2
-2.49999 -2
-2.50000 -2
-2.50001 -3
-2.99999 -3
-1.00000 -1
-1.00001 -1
-1.49999 -1
-1.50000 -1
-1.50001 -2
-1.99999 -2
0.00000 0
-0.00001 0
-0.49999 0
-0.50000 0
-0.50001 -1
-0.99999 -1
0.00000 0
0.00001 0
0.49999 0
0.50000 0
0.50001 1
0.99999 1
1.00000 1
1.00001 1
1.49999 1
1.50000 1
1.50001 2
1.99999 2
2.00000 2
2.00001 2
2.49999 2
2.50000 2
2.50001 3
2.99999 3 }}

test Rounding-2.6 {round_half_even} {
    list "
[$bi1 toString] [[$round_half_even round $bi1] toString]
[$bi2 toString] [[$round_half_even round $bi2] toString]
[$bi3 toString] [[$round_half_even round $bi3] toString]
[$bi4 toString] [[$round_half_even round $bi4] toString]
[$bi5 toString] [[$round_half_even round $bi5] toString]
[$bi6 toString] [[$round_half_even round $bi6] toString]
[$bi7 toString] [[$round_half_even round $bi7] toString]
[$bi8 toString] [[$round_half_even round $bi8] toString]
[$bi9 toString] [[$round_half_even round $bi9] toString]
[$bi10 toString] [[$round_half_even round $bi10] toString]
[$bi11 toString] [[$round_half_even round $bi11] toString]
[$bi12 toString] [[$round_half_even round $bi12] toString]
[$bi13 toString] [[$round_half_even round $bi13] toString]
[$bi14 toString] [[$round_half_even round $bi14] toString]
[$bi15 toString] [[$round_half_even round $bi15] toString]
[$bi16 toString] [[$round_half_even round $bi16] toString]
[$bi17 toString] [[$round_half_even round $bi17] toString]
[$bi18 toString] [[$round_half_even round $bi18] toString]
[$bi19 toString] [[$round_half_even round $bi19] toString]
[$bi20 toString] [[$round_half_even round $bi20] toString]
[$bi21 toString] [[$round_half_even round $bi21] toString]
[$bi22 toString] [[$round_half_even round $bi22] toString]
[$bi23 toString] [[$round_half_even round $bi23] toString]
[$bi24 toString] [[$round_half_even round $bi24] toString]
[$bi25 toString] [[$round_half_even round $bi25] toString]
[$bi26 toString] [[$round_half_even round $bi26] toString]
[$bi27 toString] [[$round_half_even round $bi27] toString]
[$bi28 toString] [[$round_half_even round $bi28] toString]
[$bi29 toString] [[$round_half_even round $bi29] toString]
[$bi30 toString] [[$round_half_even round $bi30] toString]
[$bi31 toString] [[$round_half_even round $bi31] toString]
[$bi32 toString] [[$round_half_even round $bi32] toString]
[$bi33 toString] [[$round_half_even round $bi33] toString]
[$bi34 toString] [[$round_half_even round $bi34] toString]
[$bi35 toString] [[$round_half_even round $bi35] toString]
[$bi36 toString] [[$round_half_even round $bi36] toString] "
} {{
-2.00000 -2
-2.00001 -2
-2.49999 -2
-2.50000 -2
-2.50001 -3
-2.99999 -3
-1.00000 -1
-1.00001 -1
-1.49999 -1
-1.50000 -2
-1.50001 -2
-1.99999 -2
0.00000 0
-0.00001 0
-0.49999 0
-0.50000 0
-0.50001 -1
-0.99999 -1
0.00000 0
0.00001 0
0.49999 0
0.50000 0
0.50001 1
0.99999 1
1.00000 1
1.00001 1
1.49999 1
1.50000 2
1.50001 2
1.99999 2
2.00000 2
2.00001 2
2.49999 2
2.50000 2
2.50001 3
2.99999 3 }}

test Rounding-2.7 {round_half_ceiling} {
    list "
[$bi1 toString] [[$round_half_ceiling round $bi1] toString]
[$bi2 toString] [[$round_half_ceiling round $bi2] toString]
[$bi3 toString] [[$round_half_ceiling round $bi3] toString]
[$bi4 toString] [[$round_half_ceiling round $bi4] toString]
[$bi5 toString] [[$round_half_ceiling round $bi5] toString]
[$bi6 toString] [[$round_half_ceiling round $bi6] toString]
[$bi7 toString] [[$round_half_ceiling round $bi7] toString]
[$bi8 toString] [[$round_half_ceiling round $bi8] toString]
[$bi9 toString] [[$round_half_ceiling round $bi9] toString]
[$bi10 toString] [[$round_half_ceiling round $bi10] toString]
[$bi11 toString] [[$round_half_ceiling round $bi11] toString]
[$bi12 toString] [[$round_half_ceiling round $bi12] toString]
[$bi13 toString] [[$round_half_ceiling round $bi13] toString]
[$bi14 toString] [[$round_half_ceiling round $bi14] toString]
[$bi15 toString] [[$round_half_ceiling round $bi15] toString]
[$bi16 toString] [[$round_half_ceiling round $bi16] toString]
[$bi17 toString] [[$round_half_ceiling round $bi17] toString]
[$bi18 toString] [[$round_half_ceiling round $bi18] toString]
[$bi19 toString] [[$round_half_ceiling round $bi19] toString]
[$bi20 toString] [[$round_half_ceiling round $bi20] toString]
[$bi21 toString] [[$round_half_ceiling round $bi21] toString]
[$bi22 toString] [[$round_half_ceiling round $bi22] toString]
[$bi23 toString] [[$round_half_ceiling round $bi23] toString]
[$bi24 toString] [[$round_half_ceiling round $bi24] toString]
[$bi25 toString] [[$round_half_ceiling round $bi25] toString]
[$bi26 toString] [[$round_half_ceiling round $bi26] toString]
[$bi27 toString] [[$round_half_ceiling round $bi27] toString]
[$bi28 toString] [[$round_half_ceiling round $bi28] toString]
[$bi29 toString] [[$round_half_ceiling round $bi29] toString]
[$bi30 toString] [[$round_half_ceiling round $bi30] toString]
[$bi31 toString] [[$round_half_ceiling round $bi31] toString]
[$bi32 toString] [[$round_half_ceiling round $bi32] toString]
[$bi33 toString] [[$round_half_ceiling round $bi33] toString]
[$bi34 toString] [[$round_half_ceiling round $bi34] toString]
[$bi35 toString] [[$round_half_ceiling round $bi35] toString]
[$bi36 toString] [[$round_half_ceiling round $bi36] toString] "
} {{
-2.00000 -2
-2.00001 -2
-2.49999 -2
-2.50000 -2
-2.50001 -3
-2.99999 -3
-1.00000 -1
-1.00001 -1
-1.49999 -1
-1.50000 -1
-1.50001 -2
-1.99999 -2
0.00000 0
-0.00001 0
-0.49999 0
-0.50000 0
-0.50001 -1
-0.99999 -1
0.00000 0
0.00001 0
0.49999 0
0.50000 1
0.50001 1
0.99999 1
1.00000 1
1.00001 1
1.49999 1
1.50000 2
1.50001 2
1.99999 2
2.00000 2
2.00001 2
2.49999 2
2.50000 3
2.50001 3
2.99999 3 }}

test Rounding-2.8 {round_half_floor} {
    list "
[$bi1 toString] [[$round_half_floor round $bi1] toString]
[$bi2 toString] [[$round_half_floor round $bi2] toString]
[$bi3 toString] [[$round_half_floor round $bi3] toString]
[$bi4 toString] [[$round_half_floor round $bi4] toString]
[$bi5 toString] [[$round_half_floor round $bi5] toString]
[$bi6 toString] [[$round_half_floor round $bi6] toString]
[$bi7 toString] [[$round_half_floor round $bi7] toString]
[$bi8 toString] [[$round_half_floor round $bi8] toString]
[$bi9 toString] [[$round_half_floor round $bi9] toString]
[$bi10 toString] [[$round_half_floor round $bi10] toString]
[$bi11 toString] [[$round_half_floor round $bi11] toString]
[$bi12 toString] [[$round_half_floor round $bi12] toString]
[$bi13 toString] [[$round_half_floor round $bi13] toString]
[$bi14 toString] [[$round_half_floor round $bi14] toString]
[$bi15 toString] [[$round_half_floor round $bi15] toString]
[$bi16 toString] [[$round_half_floor round $bi16] toString]
[$bi17 toString] [[$round_half_floor round $bi17] toString]
[$bi18 toString] [[$round_half_floor round $bi18] toString]
[$bi19 toString] [[$round_half_floor round $bi19] toString]
[$bi20 toString] [[$round_half_floor round $bi20] toString]
[$bi21 toString] [[$round_half_floor round $bi21] toString]
[$bi22 toString] [[$round_half_floor round $bi22] toString]
[$bi23 toString] [[$round_half_floor round $bi23] toString]
[$bi24 toString] [[$round_half_floor round $bi24] toString]
[$bi25 toString] [[$round_half_floor round $bi25] toString]
[$bi26 toString] [[$round_half_floor round $bi26] toString]
[$bi27 toString] [[$round_half_floor round $bi27] toString]
[$bi28 toString] [[$round_half_floor round $bi28] toString]
[$bi29 toString] [[$round_half_floor round $bi29] toString]
[$bi30 toString] [[$round_half_floor round $bi30] toString]
[$bi31 toString] [[$round_half_floor round $bi31] toString]
[$bi32 toString] [[$round_half_floor round $bi32] toString]
[$bi33 toString] [[$round_half_floor round $bi33] toString]
[$bi34 toString] [[$round_half_floor round $bi34] toString]
[$bi35 toString] [[$round_half_floor round $bi35] toString]
[$bi36 toString] [[$round_half_floor round $bi36] toString] "
} {{
-2.00000 -2
-2.00001 -2
-2.49999 -2
-2.50000 -3
-2.50001 -3
-2.99999 -3
-1.00000 -1
-1.00001 -1
-1.49999 -1
-1.50000 -2
-1.50001 -2
-1.99999 -2
0.00000 0
-0.00001 0
-0.49999 0
-0.50000 -1
-0.50001 -1
-0.99999 -1
0.00000 0
0.00001 0
0.49999 0
0.50000 0
0.50001 1
0.99999 1
1.00000 1
1.00001 1
1.49999 1
1.50000 1
1.50001 2
1.99999 2
2.00000 2
2.00001 2
2.49999 2
2.50000 2
2.50001 3
2.99999 3 }}

