# Tests for the TotallyOrderedSet class
#
# @Author: Jie Liu
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

######################################################################
####  Generally used comparator.
#
set comparator [java::new ptolemy.domains.ct.kernel.util.DoubleComparator]

######################################################################
####
#
test TotallyOrderedSet-2.1 {Construct an empty set and check size} {
    set toset [java::new ptolemy.domains.ct.kernel.util.TotallyOrderedSet \
            $comparator]
    list [$toset size]
} {0}

test TotallyOrderedSet-2.2 {Construct an empty set and check emptiness} {
    #Note: use the above setup.
    list [$toset isEmpty]
} {1}

######################################################################
####  set up some Double objects
#

    set p1 [java::new {Double double} 0.0 ]
    set p2 [java::new {Double double} 0.1 ]
    set p3 [java::new {Double double} 0.2 ]
    set p4 [java::new {Double double} 3.0 ]
    set p5 [java::new {Double double} 4.0 ]
    set p6 [java::new {Double double} 7.6 ]
    set p7 [java::new {Double double} 8.9 ]
    set p8 [java::new {Double double} 50.0 ]
    set p9 [java::new {Double double} 999.1 ]
    set p10 [java::new {Double double} 999.3 ]
    set p11 [java::new {Double double} 999.8 ]
    set p12 [java::new {Double double} 1001.0 ]
    set p13 [java::new {Double double} 1002.1 ]
    set p14 [java::new {Double double} 1002.2 ]
    set p15 [java::new {Double double} 1002.3 ]
    set p16 [java::new {Double double} 1002.4 ]    
    set p1again [java::new {Double double} 0.0 ]
    set p6again [java::new {Double double} 7.6 ]
    set p3again [java::new {Double double} 0.2 ]

######################################################################
####
#
test TotallyOrderedSet-3.0 {insert 5 random elements in the set} {
    $toset insert $p4
    $toset insert $p6
    $toset insert $p3
    $toset insert $p10
    $toset insert $p1
    list [$toset at 0] \
            [$toset at 1] \
            [$toset at 2] \
	    [$toset at 3] \
            [$toset at 4] 
} {0.0 0.2 3.0 7.6 999.3}

######################################################################
####
#
test TotallyOrderedSet-3.1 {try to insert a duplicated one} {
    $toset insert $p3again
    list [$toset size] [$toset at 0] \
            [$toset at 1] \
            [$toset at 2] \
            [$toset at 3] \
            [$toset at 4] 
} {5 0.0 0.2 3.0 7.6 999.3}

######################################################################
####
#
test TotallyOrderedSet-4.1 {contains} {
    $toset contains $p1
} {1}

######################################################################
####
#
test TotallyOrderedSet-4.1 {contains a duplication} {
    $toset contains $p1again
} {1}

######################################################################
####
#
test TotallyOrderedSet-5.1 {get the first from the set and test size} {
    list [$toset first] [$toset size]
} {0.0 5}

######################################################################
####
#
test TotallyOrderedSet-5.2 { index of an existing element} {
    list [$toset indexOf $p6]
} {3}

######################################################################
####
#
test TotallyOrderedSet-5.2 { index of a duplicated element } {
    list [$toset indexOf $p6again]
} {3}

######################################################################
####
#
test TotallyOrderedSet-6.1 { remove the first element } {
    $toset removeFirst
    list [$toset size] [$toset at 0]  \
            [$toset at 1] \
            [$toset at 2] \
            [$toset at 3]
} {4 0.2 3.0 7.6 999.3}

######################################################################
####
#
test TotallyOrderedSet-6.2 { remove the second element } {
    $toset removeAt 2
    list [$toset size] [$toset at 0] \
            [$toset at 1] \
            [$toset at 2] 
} {3 0.2 3.0 999.3}
