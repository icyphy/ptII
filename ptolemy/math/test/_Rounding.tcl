# Tests for the Rounding Class
#
# @Author: Ed.Willink
#
# @Version: $Id$
#
# @Copyright (c) 1998-2003 The Regents of the University of California.
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

# _Rounding.tcl is first so as to avoid problems with BigIntegers in
# Jacl

####################################################################

test Rounding-1.0 {names} {
    set round_ceiling [java::call ptolemy.math.Rounding forName "ceiling"];
    set round_convergent [java::call ptolemy.math.Rounding forName "convergent"];
    set round_down [java::call ptolemy.math.Rounding forName "down"];
    set round_floor [java::call ptolemy.math.Rounding forName "floor"];
    set round_half_ceiling [java::call ptolemy.math.Rounding forName "half_ceiling"];
    set round_half_down [java::call ptolemy.math.Rounding forName "half_down"];
    set round_half_even [java::call ptolemy.math.Rounding forName "half_even"];
    set round_half_floor [java::call ptolemy.math.Rounding forName "half_floor"];
    set round_half_up [java::call ptolemy.math.Rounding getName "half_up"];
    set round_nearest [java::call ptolemy.math.Rounding getName "nearest"];
    set round_truncate [java::call ptolemy.math.Rounding getName "truncate"];
    set round_unnecessary [java::call ptolemy.math.Rounding getName "unnecessary"];
    set round_up [java::call ptolemy.math.Rounding getName "up"];
    catch { set round_zzz [java::call ptolemy.math.Rounding getName "zzz"]; } msg
    set big_pos_even [java::new java.math.BigInteger "4" ]
    set big_pos_odd [java::new java.math.BigInteger "5" ]
    set big_neg_even [java::new java.math.BigInteger "-6" ]
    set big_neg_odd [java::new java.math.BigInteger "-5" ]
    list "
[ $round_ceiling toString ] 
[ $round_convergent toString ] 
[ $round_down toString ] 
[ $round_floor toString ] 
[ $round_half_ceiling toString ] 
[ $round_half_down toString ] 
[ $round_half_even toString ] 
[ $round_half_floor toString ] 
[ $round_half_up toString ] 
[ $round_nearest toString ] 
[ $round_truncate toString ] 
[ $round_unnecessary toString ] 
[ $round_up toString ] 
$msg "
} {{
ceiling 
half_even 
down 
floor 
half_ceiling 
half_down 
half_even 
half_floor 
half_up 
half_ceiling 
floor 
unnecessary 
up 
java.lang.IllegalArgumentException: Unknown rounding strategy "zzz". }}

####################################################################

test Rounding-2.0 {round_ceiling} {
    list "
[$big_pos_even toString] 0.00000 [[$round_ceiling quantize $big_pos_even 0.00000 ] toString] 
[$big_pos_even toString] 0.00001 [[$round_ceiling quantize $big_pos_even 0.00001 ] toString] 
[$big_pos_even toString] 0.49999 [[$round_ceiling quantize $big_pos_even 0.49999 ] toString] 
[$big_pos_even toString] 0.50000 [[$round_ceiling quantize $big_pos_even 0.50000 ] toString] 
[$big_pos_even toString] 0.50001 [[$round_ceiling quantize $big_pos_even 0.50001 ] toString] 
[$big_pos_even toString] 0.99999 [[$round_ceiling quantize $big_pos_even 0.99999 ] toString]
[$big_pos_odd toString] 0.00000 [[$round_ceiling quantize $big_pos_odd 0.00000 ] toString] 
[$big_pos_odd toString] 0.00001 [[$round_ceiling quantize $big_pos_odd 0.00001 ] toString] 
[$big_pos_odd toString] 0.49999 [[$round_ceiling quantize $big_pos_odd 0.49999 ] toString] 
[$big_pos_odd toString] 0.50000 [[$round_ceiling quantize $big_pos_odd 0.50000 ] toString] 
[$big_pos_odd toString] 0.50001 [[$round_ceiling quantize $big_pos_odd 0.50001 ] toString] 
[$big_pos_odd toString] 0.99999 [[$round_ceiling quantize $big_pos_odd 0.99999 ] toString]
[$big_neg_even toString] 0.00000 [[$round_ceiling quantize $big_neg_even 0.00000 ] toString] 
[$big_neg_even toString] 0.00001 [[$round_ceiling quantize $big_neg_even 0.00001 ] toString] 
[$big_neg_even toString] 0.49999 [[$round_ceiling quantize $big_neg_even 0.49999 ] toString] 
[$big_neg_even toString] 0.50000 [[$round_ceiling quantize $big_neg_even 0.50000 ] toString] 
[$big_neg_even toString] 0.50001 [[$round_ceiling quantize $big_neg_even 0.50001 ] toString] 
[$big_neg_even toString] 0.99999 [[$round_ceiling quantize $big_neg_even 0.99999 ] toString]
[$big_neg_odd toString] 0.00000 [[$round_ceiling quantize $big_neg_odd 0.00000 ] toString] 
[$big_neg_odd toString] 0.00001 [[$round_ceiling quantize $big_neg_odd 0.00001 ] toString] 
[$big_neg_odd toString] 0.49999 [[$round_ceiling quantize $big_neg_odd 0.49999 ] toString] 
[$big_neg_odd toString] 0.50000 [[$round_ceiling quantize $big_neg_odd 0.50000 ] toString] 
[$big_neg_odd toString] 0.50001 [[$round_ceiling quantize $big_neg_odd 0.50001 ] toString] 
[$big_neg_odd toString] 0.99999 [[$round_ceiling quantize $big_neg_odd 0.99999 ] toString] "
} {{
4 0.00000 4 
4 0.00001 5 
4 0.49999 5 
4 0.50000 5 
4 0.50001 5 
4 0.99999 5
5 0.00000 5 
5 0.00001 6 
5 0.49999 6 
5 0.50000 6 
5 0.50001 6 
5 0.99999 6
-6 0.00000 -6 
-6 0.00001 -5 
-6 0.49999 -5 
-6 0.50000 -5 
-6 0.50001 -5 
-6 0.99999 -5
-5 0.00000 -5 
-5 0.00001 -4 
-5 0.49999 -4 
-5 0.50000 -4 
-5 0.50001 -4 
-5 0.99999 -4 }}

test Rounding-2.1 {round_down} {
    list "
[$big_pos_even toString] 0.00000 [[$round_down quantize $big_pos_even 0.00000 ] toString] 
[$big_pos_even toString] 0.00001 [[$round_down quantize $big_pos_even 0.00001 ] toString] 
[$big_pos_even toString] 0.49999 [[$round_down quantize $big_pos_even 0.49999 ] toString] 
[$big_pos_even toString] 0.50000 [[$round_down quantize $big_pos_even 0.50000 ] toString] 
[$big_pos_even toString] 0.50001 [[$round_down quantize $big_pos_even 0.50001 ] toString] 
[$big_pos_even toString] 0.99999 [[$round_down quantize $big_pos_even 0.99999 ] toString]
[$big_pos_odd toString] 0.00000 [[$round_down quantize $big_pos_odd 0.00000 ] toString] 
[$big_pos_odd toString] 0.00001 [[$round_down quantize $big_pos_odd 0.00001 ] toString] 
[$big_pos_odd toString] 0.49999 [[$round_down quantize $big_pos_odd 0.49999 ] toString] 
[$big_pos_odd toString] 0.50000 [[$round_down quantize $big_pos_odd 0.50000 ] toString] 
[$big_pos_odd toString] 0.50001 [[$round_down quantize $big_pos_odd 0.50001 ] toString] 
[$big_pos_odd toString] 0.99999 [[$round_down quantize $big_pos_odd 0.99999 ] toString]
[$big_neg_even toString] 0.00000 [[$round_down quantize $big_neg_even 0.00000 ] toString] 
[$big_neg_even toString] 0.00001 [[$round_down quantize $big_neg_even 0.00001 ] toString] 
[$big_neg_even toString] 0.49999 [[$round_down quantize $big_neg_even 0.49999 ] toString] 
[$big_neg_even toString] 0.50000 [[$round_down quantize $big_neg_even 0.50000 ] toString] 
[$big_neg_even toString] 0.50001 [[$round_down quantize $big_neg_even 0.50001 ] toString] 
[$big_neg_even toString] 0.99999 [[$round_down quantize $big_neg_even 0.99999 ] toString]
[$big_neg_odd toString] 0.00000 [[$round_down quantize $big_neg_odd 0.00000 ] toString] 
[$big_neg_odd toString] 0.00001 [[$round_down quantize $big_neg_odd 0.00001 ] toString] 
[$big_neg_odd toString] 0.49999 [[$round_down quantize $big_neg_odd 0.49999 ] toString] 
[$big_neg_odd toString] 0.50000 [[$round_down quantize $big_neg_odd 0.50000 ] toString] 
[$big_neg_odd toString] 0.50001 [[$round_down quantize $big_neg_odd 0.50001 ] toString] 
[$big_neg_odd toString] 0.99999 [[$round_down quantize $big_neg_odd 0.99999 ] toString] "
} {{
4 0.00000 4 
4 0.00001 4 
4 0.49999 4 
4 0.50000 4 
4 0.50001 4 
4 0.99999 4
5 0.00000 5 
5 0.00001 5 
5 0.49999 5 
5 0.50000 5 
5 0.50001 5 
5 0.99999 5
-6 0.00000 -6 
-6 0.00001 -5 
-6 0.49999 -5 
-6 0.50000 -5 
-6 0.50001 -5 
-6 0.99999 -5
-5 0.00000 -5 
-5 0.00001 -4 
-5 0.49999 -4 
-5 0.50000 -4 
-5 0.50001 -4 
-5 0.99999 -4 }}

test Rounding-2.2 {round_floor} {
    list "
[$big_pos_even toString] 0.00000 [[$round_floor quantize $big_pos_even 0.00000 ] toString] 
[$big_pos_even toString] 0.00001 [[$round_floor quantize $big_pos_even 0.00001 ] toString] 
[$big_pos_even toString] 0.49999 [[$round_floor quantize $big_pos_even 0.49999 ] toString] 
[$big_pos_even toString] 0.50000 [[$round_floor quantize $big_pos_even 0.50000 ] toString] 
[$big_pos_even toString] 0.50001 [[$round_floor quantize $big_pos_even 0.50001 ] toString] 
[$big_pos_even toString] 0.99999 [[$round_floor quantize $big_pos_even 0.99999 ] toString]
[$big_pos_odd toString] 0.00000 [[$round_floor quantize $big_pos_odd 0.00000 ] toString] 
[$big_pos_odd toString] 0.00001 [[$round_floor quantize $big_pos_odd 0.00001 ] toString] 
[$big_pos_odd toString] 0.49999 [[$round_floor quantize $big_pos_odd 0.49999 ] toString] 
[$big_pos_odd toString] 0.50000 [[$round_floor quantize $big_pos_odd 0.50000 ] toString] 
[$big_pos_odd toString] 0.50001 [[$round_floor quantize $big_pos_odd 0.50001 ] toString] 
[$big_pos_odd toString] 0.99999 [[$round_floor quantize $big_pos_odd 0.99999 ] toString]
[$big_neg_even toString] 0.00000 [[$round_floor quantize $big_neg_even 0.00000 ] toString] 
[$big_neg_even toString] 0.00001 [[$round_floor quantize $big_neg_even 0.00001 ] toString] 
[$big_neg_even toString] 0.49999 [[$round_floor quantize $big_neg_even 0.49999 ] toString] 
[$big_neg_even toString] 0.50000 [[$round_floor quantize $big_neg_even 0.50000 ] toString] 
[$big_neg_even toString] 0.50001 [[$round_floor quantize $big_neg_even 0.50001 ] toString] 
[$big_neg_even toString] 0.99999 [[$round_floor quantize $big_neg_even 0.99999 ] toString]
[$big_neg_odd toString] 0.00000 [[$round_floor quantize $big_neg_odd 0.00000 ] toString] 
[$big_neg_odd toString] 0.00001 [[$round_floor quantize $big_neg_odd 0.00001 ] toString] 
[$big_neg_odd toString] 0.49999 [[$round_floor quantize $big_neg_odd 0.49999 ] toString] 
[$big_neg_odd toString] 0.50000 [[$round_floor quantize $big_neg_odd 0.50000 ] toString] 
[$big_neg_odd toString] 0.50001 [[$round_floor quantize $big_neg_odd 0.50001 ] toString] 
[$big_neg_odd toString] 0.99999 [[$round_floor quantize $big_neg_odd 0.99999 ] toString] "
} {{
4 0.00000 4 
4 0.00001 4 
4 0.49999 4 
4 0.50000 4 
4 0.50001 4 
4 0.99999 4
5 0.00000 5 
5 0.00001 5 
5 0.49999 5 
5 0.50000 5 
5 0.50001 5 
5 0.99999 5
-6 0.00000 -6 
-6 0.00001 -6 
-6 0.49999 -6 
-6 0.50000 -6 
-6 0.50001 -6 
-6 0.99999 -6
-5 0.00000 -5 
-5 0.00001 -5 
-5 0.49999 -5 
-5 0.50000 -5 
-5 0.50001 -5 
-5 0.99999 -5 }}

test Rounding-2.3 {round_half_ceiling} {
    list "
[$big_pos_even toString] 0.00000 [[$round_half_ceiling quantize $big_pos_even 0.00000 ] toString] 
[$big_pos_even toString] 0.00001 [[$round_half_ceiling quantize $big_pos_even 0.00001 ] toString] 
[$big_pos_even toString] 0.49999 [[$round_half_ceiling quantize $big_pos_even 0.49999 ] toString] 
[$big_pos_even toString] 0.50000 [[$round_half_ceiling quantize $big_pos_even 0.50000 ] toString] 
[$big_pos_even toString] 0.50001 [[$round_half_ceiling quantize $big_pos_even 0.50001 ] toString] 
[$big_pos_even toString] 0.99999 [[$round_half_ceiling quantize $big_pos_even 0.99999 ] toString]
[$big_pos_odd toString] 0.00000 [[$round_half_ceiling quantize $big_pos_odd 0.00000 ] toString] 
[$big_pos_odd toString] 0.00001 [[$round_half_ceiling quantize $big_pos_odd 0.00001 ] toString] 
[$big_pos_odd toString] 0.49999 [[$round_half_ceiling quantize $big_pos_odd 0.49999 ] toString] 
[$big_pos_odd toString] 0.50000 [[$round_half_ceiling quantize $big_pos_odd 0.50000 ] toString] 
[$big_pos_odd toString] 0.50001 [[$round_half_ceiling quantize $big_pos_odd 0.50001 ] toString] 
[$big_pos_odd toString] 0.99999 [[$round_half_ceiling quantize $big_pos_odd 0.99999 ] toString]
[$big_neg_even toString] 0.00000 [[$round_half_ceiling quantize $big_neg_even 0.00000 ] toString] 
[$big_neg_even toString] 0.00001 [[$round_half_ceiling quantize $big_neg_even 0.00001 ] toString] 
[$big_neg_even toString] 0.49999 [[$round_half_ceiling quantize $big_neg_even 0.49999 ] toString] 
[$big_neg_even toString] 0.50000 [[$round_half_ceiling quantize $big_neg_even 0.50000 ] toString] 
[$big_neg_even toString] 0.50001 [[$round_half_ceiling quantize $big_neg_even 0.50001 ] toString] 
[$big_neg_even toString] 0.99999 [[$round_half_ceiling quantize $big_neg_even 0.99999 ] toString]
[$big_neg_odd toString] 0.00000 [[$round_half_ceiling quantize $big_neg_odd 0.00000 ] toString] 
[$big_neg_odd toString] 0.00001 [[$round_half_ceiling quantize $big_neg_odd 0.00001 ] toString] 
[$big_neg_odd toString] 0.49999 [[$round_half_ceiling quantize $big_neg_odd 0.49999 ] toString] 
[$big_neg_odd toString] 0.50000 [[$round_half_ceiling quantize $big_neg_odd 0.50000 ] toString] 
[$big_neg_odd toString] 0.50001 [[$round_half_ceiling quantize $big_neg_odd 0.50001 ] toString] 
[$big_neg_odd toString] 0.99999 [[$round_half_ceiling quantize $big_neg_odd 0.99999 ] toString] "
} {{
4 0.00000 4 
4 0.00001 4 
4 0.49999 4 
4 0.50000 5 
4 0.50001 5 
4 0.99999 5
5 0.00000 5 
5 0.00001 5 
5 0.49999 5 
5 0.50000 6 
5 0.50001 6 
5 0.99999 6
-6 0.00000 -6 
-6 0.00001 -6 
-6 0.49999 -6 
-6 0.50000 -5 
-6 0.50001 -5 
-6 0.99999 -5
-5 0.00000 -5 
-5 0.00001 -5 
-5 0.49999 -5 
-5 0.50000 -4 
-5 0.50001 -4 
-5 0.99999 -4 }}

test Rounding-2.4 {round_half_down} {
    list "
[$big_pos_even toString] 0.00000 [[$round_half_down quantize $big_pos_even 0.00000 ] toString] 
[$big_pos_even toString] 0.00001 [[$round_half_down quantize $big_pos_even 0.00001 ] toString] 
[$big_pos_even toString] 0.49999 [[$round_half_down quantize $big_pos_even 0.49999 ] toString] 
[$big_pos_even toString] 0.50000 [[$round_half_down quantize $big_pos_even 0.50000 ] toString] 
[$big_pos_even toString] 0.50001 [[$round_half_down quantize $big_pos_even 0.50001 ] toString] 
[$big_pos_even toString] 0.99999 [[$round_half_down quantize $big_pos_even 0.99999 ] toString]
[$big_pos_odd toString] 0.00000 [[$round_half_down quantize $big_pos_odd 0.00000 ] toString] 
[$big_pos_odd toString] 0.00001 [[$round_half_down quantize $big_pos_odd 0.00001 ] toString] 
[$big_pos_odd toString] 0.49999 [[$round_half_down quantize $big_pos_odd 0.49999 ] toString] 
[$big_pos_odd toString] 0.50000 [[$round_half_down quantize $big_pos_odd 0.50000 ] toString] 
[$big_pos_odd toString] 0.50001 [[$round_half_down quantize $big_pos_odd 0.50001 ] toString] 
[$big_pos_odd toString] 0.99999 [[$round_half_down quantize $big_pos_odd 0.99999 ] toString]
[$big_neg_even toString] 0.00000 [[$round_half_down quantize $big_neg_even 0.00000 ] toString] 
[$big_neg_even toString] 0.00001 [[$round_half_down quantize $big_neg_even 0.00001 ] toString] 
[$big_neg_even toString] 0.49999 [[$round_half_down quantize $big_neg_even 0.49999 ] toString] 
[$big_neg_even toString] 0.50000 [[$round_half_down quantize $big_neg_even 0.50000 ] toString] 
[$big_neg_even toString] 0.50001 [[$round_half_down quantize $big_neg_even 0.50001 ] toString] 
[$big_neg_even toString] 0.99999 [[$round_half_down quantize $big_neg_even 0.99999 ] toString]
[$big_neg_odd toString] 0.00000 [[$round_half_down quantize $big_neg_odd 0.00000 ] toString] 
[$big_neg_odd toString] 0.00001 [[$round_half_down quantize $big_neg_odd 0.00001 ] toString] 
[$big_neg_odd toString] 0.49999 [[$round_half_down quantize $big_neg_odd 0.49999 ] toString] 
[$big_neg_odd toString] 0.50000 [[$round_half_down quantize $big_neg_odd 0.50000 ] toString] 
[$big_neg_odd toString] 0.50001 [[$round_half_down quantize $big_neg_odd 0.50001 ] toString] 
[$big_neg_odd toString] 0.99999 [[$round_half_down quantize $big_neg_odd 0.99999 ] toString] "
} {{
4 0.00000 4 
4 0.00001 4 
4 0.49999 4 
4 0.50000 4 
4 0.50001 5 
4 0.99999 5
5 0.00000 5 
5 0.00001 5 
5 0.49999 5 
5 0.50000 5 
5 0.50001 6 
5 0.99999 6
-6 0.00000 -6 
-6 0.00001 -6 
-6 0.49999 -6 
-6 0.50000 -5 
-6 0.50001 -5 
-6 0.99999 -5
-5 0.00000 -5 
-5 0.00001 -5 
-5 0.49999 -5 
-5 0.50000 -4 
-5 0.50001 -4 
-5 0.99999 -4 }}

test Rounding-2.5 {round_half_even} {
    list "
[$big_pos_even toString] 0.00000 [[$round_half_even quantize $big_pos_even 0.00000 ] toString] 
[$big_pos_even toString] 0.00001 [[$round_half_even quantize $big_pos_even 0.00001 ] toString] 
[$big_pos_even toString] 0.49999 [[$round_half_even quantize $big_pos_even 0.49999 ] toString] 
[$big_pos_even toString] 0.50000 [[$round_half_even quantize $big_pos_even 0.50000 ] toString] 
[$big_pos_even toString] 0.50001 [[$round_half_even quantize $big_pos_even 0.50001 ] toString] 
[$big_pos_even toString] 0.99999 [[$round_half_even quantize $big_pos_even 0.99999 ] toString]
[$big_pos_odd toString] 0.00000 [[$round_half_even quantize $big_pos_odd 0.00000 ] toString] 
[$big_pos_odd toString] 0.00001 [[$round_half_even quantize $big_pos_odd 0.00001 ] toString] 
[$big_pos_odd toString] 0.49999 [[$round_half_even quantize $big_pos_odd 0.49999 ] toString] 
[$big_pos_odd toString] 0.50000 [[$round_half_even quantize $big_pos_odd 0.50000 ] toString] 
[$big_pos_odd toString] 0.50001 [[$round_half_even quantize $big_pos_odd 0.50001 ] toString] 
[$big_pos_odd toString] 0.99999 [[$round_half_even quantize $big_pos_odd 0.99999 ] toString]
[$big_neg_even toString] 0.00000 [[$round_half_even quantize $big_neg_even 0.00000 ] toString] 
[$big_neg_even toString] 0.00001 [[$round_half_even quantize $big_neg_even 0.00001 ] toString] 
[$big_neg_even toString] 0.49999 [[$round_half_even quantize $big_neg_even 0.49999 ] toString] 
[$big_neg_even toString] 0.50000 [[$round_half_even quantize $big_neg_even 0.50000 ] toString] 
[$big_neg_even toString] 0.50001 [[$round_half_even quantize $big_neg_even 0.50001 ] toString] 
[$big_neg_even toString] 0.99999 [[$round_half_even quantize $big_neg_even 0.99999 ] toString]
[$big_neg_odd toString] 0.00000 [[$round_half_even quantize $big_neg_odd 0.00000 ] toString] 
[$big_neg_odd toString] 0.00001 [[$round_half_even quantize $big_neg_odd 0.00001 ] toString] 
[$big_neg_odd toString] 0.49999 [[$round_half_even quantize $big_neg_odd 0.49999 ] toString] 
[$big_neg_odd toString] 0.50000 [[$round_half_even quantize $big_neg_odd 0.50000 ] toString] 
[$big_neg_odd toString] 0.50001 [[$round_half_even quantize $big_neg_odd 0.50001 ] toString] 
[$big_neg_odd toString] 0.99999 [[$round_half_even quantize $big_neg_odd 0.99999 ] toString] "
} {{
4 0.00000 4 
4 0.00001 4 
4 0.49999 4 
4 0.50000 4 
4 0.50001 5 
4 0.99999 5
5 0.00000 5 
5 0.00001 5 
5 0.49999 5 
5 0.50000 6 
5 0.50001 6 
5 0.99999 6
-6 0.00000 -6 
-6 0.00001 -6 
-6 0.49999 -6 
-6 0.50000 -6 
-6 0.50001 -5 
-6 0.99999 -5
-5 0.00000 -5 
-5 0.00001 -5 
-5 0.49999 -5 
-5 0.50000 -4 
-5 0.50001 -4 
-5 0.99999 -4 }}

test Rounding-2.6 {round_half_floor} {
    list "
[$big_pos_even toString] 0.00000 [[$round_half_floor quantize $big_pos_even 0.00000 ] toString] 
[$big_pos_even toString] 0.00001 [[$round_half_floor quantize $big_pos_even 0.00001 ] toString] 
[$big_pos_even toString] 0.49999 [[$round_half_floor quantize $big_pos_even 0.49999 ] toString] 
[$big_pos_even toString] 0.50000 [[$round_half_floor quantize $big_pos_even 0.50000 ] toString] 
[$big_pos_even toString] 0.50001 [[$round_half_floor quantize $big_pos_even 0.50001 ] toString] 
[$big_pos_even toString] 0.99999 [[$round_half_floor quantize $big_pos_even 0.99999 ] toString]
[$big_pos_odd toString] 0.00000 [[$round_half_floor quantize $big_pos_odd 0.00000 ] toString] 
[$big_pos_odd toString] 0.00001 [[$round_half_floor quantize $big_pos_odd 0.00001 ] toString] 
[$big_pos_odd toString] 0.49999 [[$round_half_floor quantize $big_pos_odd 0.49999 ] toString] 
[$big_pos_odd toString] 0.50000 [[$round_half_floor quantize $big_pos_odd 0.50000 ] toString] 
[$big_pos_odd toString] 0.50001 [[$round_half_floor quantize $big_pos_odd 0.50001 ] toString] 
[$big_pos_odd toString] 0.99999 [[$round_half_floor quantize $big_pos_odd 0.99999 ] toString]
[$big_neg_even toString] 0.00000 [[$round_half_floor quantize $big_neg_even 0.00000 ] toString] 
[$big_neg_even toString] 0.00001 [[$round_half_floor quantize $big_neg_even 0.00001 ] toString] 
[$big_neg_even toString] 0.49999 [[$round_half_floor quantize $big_neg_even 0.49999 ] toString] 
[$big_neg_even toString] 0.50000 [[$round_half_floor quantize $big_neg_even 0.50000 ] toString] 
[$big_neg_even toString] 0.50001 [[$round_half_floor quantize $big_neg_even 0.50001 ] toString] 
[$big_neg_even toString] 0.99999 [[$round_half_floor quantize $big_neg_even 0.99999 ] toString]
[$big_neg_odd toString] 0.00000 [[$round_half_floor quantize $big_neg_odd 0.00000 ] toString] 
[$big_neg_odd toString] 0.00001 [[$round_half_floor quantize $big_neg_odd 0.00001 ] toString] 
[$big_neg_odd toString] 0.49999 [[$round_half_floor quantize $big_neg_odd 0.49999 ] toString] 
[$big_neg_odd toString] 0.50000 [[$round_half_floor quantize $big_neg_odd 0.50000 ] toString] 
[$big_neg_odd toString] 0.50001 [[$round_half_floor quantize $big_neg_odd 0.50001 ] toString] 
[$big_neg_odd toString] 0.99999 [[$round_half_floor quantize $big_neg_odd 0.99999 ] toString] "
} {{
4 0.00000 4 
4 0.00001 4 
4 0.49999 4 
4 0.50000 4 
4 0.50001 5 
4 0.99999 5
5 0.00000 5 
5 0.00001 5 
5 0.49999 5 
5 0.50000 5 
5 0.50001 6 
5 0.99999 6
-6 0.00000 -6 
-6 0.00001 -6 
-6 0.49999 -6 
-6 0.50000 -6 
-6 0.50001 -5 
-6 0.99999 -5
-5 0.00000 -5 
-5 0.00001 -5 
-5 0.49999 -5 
-5 0.50000 -5 
-5 0.50001 -4 
-5 0.99999 -4 }}

test Rounding-2.7 {round_half_up} {
    list "
[$big_pos_even toString] 0.00000 [[$round_half_up quantize $big_pos_even 0.00000 ] toString] 
[$big_pos_even toString] 0.00001 [[$round_half_up quantize $big_pos_even 0.00001 ] toString] 
[$big_pos_even toString] 0.49999 [[$round_half_up quantize $big_pos_even 0.49999 ] toString] 
[$big_pos_even toString] 0.50000 [[$round_half_up quantize $big_pos_even 0.50000 ] toString] 
[$big_pos_even toString] 0.50001 [[$round_half_up quantize $big_pos_even 0.50001 ] toString] 
[$big_pos_even toString] 0.99999 [[$round_half_up quantize $big_pos_even 0.99999 ] toString]
[$big_pos_odd toString] 0.00000 [[$round_half_up quantize $big_pos_odd 0.00000 ] toString] 
[$big_pos_odd toString] 0.00001 [[$round_half_up quantize $big_pos_odd 0.00001 ] toString] 
[$big_pos_odd toString] 0.49999 [[$round_half_up quantize $big_pos_odd 0.49999 ] toString] 
[$big_pos_odd toString] 0.50000 [[$round_half_up quantize $big_pos_odd 0.50000 ] toString] 
[$big_pos_odd toString] 0.50001 [[$round_half_up quantize $big_pos_odd 0.50001 ] toString] 
[$big_pos_odd toString] 0.99999 [[$round_half_up quantize $big_pos_odd 0.99999 ] toString]
[$big_neg_even toString] 0.00000 [[$round_half_up quantize $big_neg_even 0.00000 ] toString] 
[$big_neg_even toString] 0.00001 [[$round_half_up quantize $big_neg_even 0.00001 ] toString] 
[$big_neg_even toString] 0.49999 [[$round_half_up quantize $big_neg_even 0.49999 ] toString] 
[$big_neg_even toString] 0.50000 [[$round_half_up quantize $big_neg_even 0.50000 ] toString] 
[$big_neg_even toString] 0.50001 [[$round_half_up quantize $big_neg_even 0.50001 ] toString] 
[$big_neg_even toString] 0.99999 [[$round_half_up quantize $big_neg_even 0.99999 ] toString]
[$big_neg_odd toString] 0.00000 [[$round_half_up quantize $big_neg_odd 0.00000 ] toString] 
[$big_neg_odd toString] 0.00001 [[$round_half_up quantize $big_neg_odd 0.00001 ] toString] 
[$big_neg_odd toString] 0.49999 [[$round_half_up quantize $big_neg_odd 0.49999 ] toString] 
[$big_neg_odd toString] 0.50000 [[$round_half_up quantize $big_neg_odd 0.50000 ] toString] 
[$big_neg_odd toString] 0.50001 [[$round_half_up quantize $big_neg_odd 0.50001 ] toString] 
[$big_neg_odd toString] 0.99999 [[$round_half_up quantize $big_neg_odd 0.99999 ] toString] "
} {{
4 0.00000 4 
4 0.00001 4 
4 0.49999 4 
4 0.50000 5 
4 0.50001 5 
4 0.99999 5
5 0.00000 5 
5 0.00001 5 
5 0.49999 5 
5 0.50000 6 
5 0.50001 6 
5 0.99999 6
-6 0.00000 -6 
-6 0.00001 -6 
-6 0.49999 -6 
-6 0.50000 -6 
-6 0.50001 -5 
-6 0.99999 -5
-5 0.00000 -5 
-5 0.00001 -5 
-5 0.49999 -5 
-5 0.50000 -5 
-5 0.50001 -4 
-5 0.99999 -4 }}

test Rounding-2.8 {round_unnecessary} {
    catch { [$round_unnecessary quantize $big_pos_even 0.00001 ] } msg
    list "
[$big_neg_odd toString] 0.00000 [[$round_unnecessary quantize $big_neg_odd 0.0000 ] toString] 
$msg "
} {{
-5 0.00000 -5 
java.lang.ArithmeticException: Rounding necessary. }}

test Rounding-2.9 {round_up} {
    list "
[$big_pos_even toString] 0.00000 [[$round_up quantize $big_pos_even 0.00000 ] toString] 
[$big_pos_even toString] 0.00001 [[$round_up quantize $big_pos_even 0.00001 ] toString] 
[$big_pos_even toString] 0.49999 [[$round_up quantize $big_pos_even 0.49999 ] toString] 
[$big_pos_even toString] 0.50000 [[$round_up quantize $big_pos_even 0.50000 ] toString] 
[$big_pos_even toString] 0.50001 [[$round_up quantize $big_pos_even 0.50001 ] toString] 
[$big_pos_even toString] 0.99999 [[$round_up quantize $big_pos_even 0.99999 ] toString]
[$big_pos_odd toString] 0.00000 [[$round_up quantize $big_pos_odd 0.00000 ] toString] 
[$big_pos_odd toString] 0.00001 [[$round_up quantize $big_pos_odd 0.00001 ] toString] 
[$big_pos_odd toString] 0.49999 [[$round_up quantize $big_pos_odd 0.49999 ] toString] 
[$big_pos_odd toString] 0.50000 [[$round_up quantize $big_pos_odd 0.50000 ] toString] 
[$big_pos_odd toString] 0.50001 [[$round_up quantize $big_pos_odd 0.50001 ] toString] 
[$big_pos_odd toString] 0.99999 [[$round_up quantize $big_pos_odd 0.99999 ] toString]
[$big_neg_even toString] 0.00000 [[$round_up quantize $big_neg_even 0.00000 ] toString] 
[$big_neg_even toString] 0.00001 [[$round_up quantize $big_neg_even 0.00001 ] toString] 
[$big_neg_even toString] 0.49999 [[$round_up quantize $big_neg_even 0.49999 ] toString] 
[$big_neg_even toString] 0.50000 [[$round_up quantize $big_neg_even 0.50000 ] toString] 
[$big_neg_even toString] 0.50001 [[$round_up quantize $big_neg_even 0.50001 ] toString] 
[$big_neg_even toString] 0.99999 [[$round_up quantize $big_neg_even 0.99999 ] toString]
[$big_neg_odd toString] 0.00000 [[$round_up quantize $big_neg_odd 0.00000 ] toString] 
[$big_neg_odd toString] 0.00001 [[$round_up quantize $big_neg_odd 0.00001 ] toString] 
[$big_neg_odd toString] 0.49999 [[$round_up quantize $big_neg_odd 0.49999 ] toString] 
[$big_neg_odd toString] 0.50000 [[$round_up quantize $big_neg_odd 0.50000 ] toString] 
[$big_neg_odd toString] 0.50001 [[$round_up quantize $big_neg_odd 0.50001 ] toString] 
[$big_neg_odd toString] 0.99999 [[$round_up quantize $big_neg_odd 0.99999 ] toString] "
} {{
4 0.00000 4 
4 0.00001 5 
4 0.49999 5 
4 0.50000 5 
4 0.50001 5 
4 0.99999 5
5 0.00000 5 
5 0.00001 6 
5 0.49999 6 
5 0.50000 6 
5 0.50001 6 
5 0.99999 6
-6 0.00000 -6 
-6 0.00001 -6 
-6 0.49999 -6 
-6 0.50000 -6 
-6 0.50001 -6 
-6 0.99999 -6
-5 0.00000 -5 
-5 0.00001 -5 
-5 0.49999 -5 
-5 0.50000 -5 
-5 0.50001 -5 
-5 0.99999 -5 }}
