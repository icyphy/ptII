# Tests for the BinaryTree class
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
test BinaryTree-2.1 {Constructor tests} {
    set t1 [java::new ptolemy.domains.sdf.lib.huffman.BinaryTree]
    set t2 [java::new ptolemy.domains.sdf.lib.huffman.BinaryTree]
    set t3 [java::new ptolemy.domains.sdf.lib.huffman.BinaryTree]
    set t4 [java::new ptolemy.domains.sdf.lib.huffman.BinaryTree]
    set t5 [java::new ptolemy.domains.sdf.lib.huffman.BinaryTree]
    set t6 [java::new ptolemy.domains.sdf.lib.huffman.BinaryTree]
    
   
    list [$t1 toString] [$t2 toString] [$t3 toString] [$t4 toString]
} {tree(null,null) tree(null,null) tree(null,null) tree(null,null)}

######################################################################
####
#
test BinaryTree-3.1 {Test addLeft and addRight } {
    # NOTE: Use the setup above
    $t1 addLeft $t2 
    $t3 addLeft $t1 
    $t4 addRight $t5
    
    list [$t1 toString] [$t3 toString] [$t4 toString] 
} {tree(tree(null,null),null) tree(tree(tree(null,null),null),null) tree(null,tree(null,null))}

######################################################################
####
#

test BinaryTree-4.1 {Test getRoot and getParent} {
    # NOTE: Uses the setup above
    set a1 [$t1 getRoot]
    set a2 [$t6 getParent]
    set a3 [$t2 getParent]
    list [$a1 toString] [expr {$a2 == [java::null]}] [$a3 toString] 
} {tree(tree(tree(null,null),null),null) 1 tree(tree(null,null),null)}

######################################################################
####
#

test BinaryTree-4.2 {Test addLeft and getLeft and removeLeft} {
    # NOTE: Uses the setup above
  
    set b1 [$t1 getLeft]
    $t3 removeLeft
    # NOTE: Test the illegal exception for addLeft
    catch {[$t4 addLeft $t2]} s1

    list [$b1 toString] [$t3 toString] [$t4 toString] $s1
} {tree(null,null) tree(null,null) tree(null,tree(null,null)) {ptolemy.kernel.util.IllegalActionException: BinaryTree: Cannot add the tree tree(null,null) to the left branch because tree(null,null) has a parent already.}}

######################################################################
####
#

test BinaryTree-4.3 {Test removeRight and removeLeft} {
    # NOTE: remove all the tree relationship set above

    $t1 removeLeft
    $t4 removeRight

    list [$t1 toString] [$t2 toString] [$t3 toString] [$t4 toString] 
} {tree(null,null) tree(null,null) tree(null,null) tree(null,null)}

######################################################################
####
#

test BinaryTree-4.4 {Test addRight and getRight and removeRight} {
    # NOTE: reconstruct tree relations 
    $t3 addRight $t4   
    $t2 addRight $t3
    $t1 addRight $t2
      
    set c1 [$t3 getRight] 
    # NOTE: test the illegal Action Exception
    $t5 addRight $t6
    catch {[$t4 addRight $t6]} s2

    list [$t3 toString] [$t2 toString] [$c1 toString] $s2 [$t4 toString]

} {tree(null,tree(null,null)) tree(null,tree(null,tree(null,null))) tree(null,null) {ptolemy.kernel.util.IllegalActionException: BinaryTree: Cannot add the tree tree(null,null) to the right branch because tree(null,null) has a parent already.} tree(null,null)}

######################################################################
####
#
test BinaryTree-4.5 {Test addLeftand removeRight} {
    # NOTE: use the tree constructed above
    $t4 addLeft $t5
    $t1 removeRight 
    list [$t4 toString] [$t1 toString]
    
} {tree(tree(null,tree(null,null)),null) tree(null,null)}

######################################################################
####
#
test BinaryTree-5.1 {Test removeLeft and removeRight} {
    $t4 removeLeft
    $t5 removeRight
    list [$t4 toString] [$t5 toString] [$t6 toString]
} {tree(null,null) tree(null,null) tree(null,null)}

######################################################################
####
#

test BinaryTree-5.2 {Test isLeaf} {
    # NOTE: Use the setup above 
    $t5 addLeft $t6

    set e1 [$t4 isLeaf]
    set e2 [$t5 isLeaf]
    set e3 [$t6 isLeaf]
    list $e1 $e2 $e3
} {1 0 1}

######################################################################
####
#
