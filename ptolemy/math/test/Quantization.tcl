# Tests for the Quantization Class
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

####################################################################

test Quantization-1.0 {gets} {
    set q0 [java::new ptolemy.math.FixPointQuantization "2.1,modulo,nearest" ]
    list "
[$q0 getEpsilonValue]
[$q0 getExactOverflow]
[$q0 getExactRounding]
[$q0 getFractionBitLength]
[$q0 getIntegerBitLength]
[$q0 getMantissaBitLength]
[$q0 getExponentBitLength]
[$q0 getNumberOfBits]
[$q0 getNumberOfLevels]
[$q0 getMaximumValue]
[$q0 getMinimumValue]
[[$q0 getOverflow] toString]
[[$q0 getRounding] toString]
[$q0 getTinyValue] "
} {{
0.5
0
0
1
2
3
0
3
8.0
1.5
-2.0
modulo
half_ceiling
0.5 }}

####################################################################
test Quantization-2.0 {clone} {
    set clone [$q0 clone]
    $clone equals $q0
} {1}
