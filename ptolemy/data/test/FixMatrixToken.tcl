# Tests for the FixMatrixToken class
#
# @Author: Yuhong Xiong
#
# @Version $Id$
#
# @Copyright (c) 1997-2002 The Regents of the University of California.
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

# If a file contains non-graphical tests, then it should be named .tcl
# If a file contains graphical tests, then it should be called .itcl
# 
# It would be nice if the tests would work in a vanilla itkwish binary.
# Check for necessary classes and adjust the auto_path accordingly.
#

######################################################################
####
# 
test FixMatrixToken-1.1 {Create a non-empty instance from a String} {
    set p [java::new {ptolemy.data.FixMatrixToken String} "fix(\[1.0, 2.0; 3.5, 4.5\], 8, 4)"]
    $p toString
} {[fix(1.0,8,4), fix(2.0,8,4); fix(3.5,8,4), fix(4.5,8,4)]}

######################################################################
####
# 
test FixMatrixToken-2.0 {Test equals} {
    set p1 [java::new {ptolemy.data.FixMatrixToken String} "fix(\[1.0, 2.0; 3.5, 4.5\], 8, 4)"]
    set p2 [java::new {ptolemy.data.FixMatrixToken String} "fix(\[1.0, 2.0; 3.5, 4.5\], 8, 4)"]
    set p3 [java::new {ptolemy.data.FixMatrixToken String} "fix(\[1.0, 2.0; 3.0, 4.0\], 8, 4)"]
    list [$p1 equals $p1] [$p1 equals $p2] [$p1 equals $p3]
} {1 1 0}

######################################################################
####
# 
test FixMatrixToken-3.0 {Test hashCode} {
    set p1 [java::new {ptolemy.data.FixMatrixToken String} "fix(\[1.0, 2.0; 3.5, 4.2\], 8, 4)"]
    set p2 [java::new {ptolemy.data.FixMatrixToken String} "fix(\[1.0, 2.0; 3.5, 4.2\], 8, 4)"]
    set p3 [java::new {ptolemy.data.FixMatrixToken String} "fix(\[1.0, 2.0; 3.0, 6.0\], 8, 4)"]
    list [$p1 hashCode] [$p2 hashCode] [$p3 hashCode]
} {10 10 12}

