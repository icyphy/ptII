# Tests for the HuffLeaf class
#
# @Author: Michael Leung
#
# @Version: $Id$
#
# @Copyright (c) 1999-2000 The Regents of the University of California.
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
test HuffLeaf-2.1 {Constructor tests} {
    set l1 [java::new ptolemy.domains.sdf.lib.huffman.HuffLeaf A 0.1]
    set l2 [java::new ptolemy.domains.sdf.lib.huffman.HuffLeaf B 0.2]
    set l3 [java::new ptolemy.domains.sdf.lib.huffman.HuffLeaf C 0.5]
    
    list [$l1 toString] [$l2 toString] [$l3 toString]
} {{Leaf:0.1 data:A} {Leaf:0.2 data:B} {Leaf:0.5 data:C}}

######################################################################
####
#
test HuffLeaf-3.1 {Test getData() and getProb() } {
    # NOTE: Use the setup above
    set a1 [$l1 getData]
    set a2 [$l1 getProb]
    
    list $a1 $a2 
} {A 0.1}

######################################################################
####
#

test HuffLeaf-4.1 {Test setProb and setData} {
    # NOTE: Uses the setup above
    $l1 setProb 0.3
    $l1 setData D
    list [$l1 toString]
} {{Leaf:0.3 data:D}}

######################################################################
####
#
