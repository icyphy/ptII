# Tests for the Huffman CodeBook actor.
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
test CodeBook-2.1 {This the huffman Encoder} {
    # Create a pnModel
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set manager [java::new ptolemy.actor.Manager]
    set director [java::new ptolemy.domains.pn.kernel.PNDirector]
    $e0 setDirector $director
    $e0 setName top
    $e0 setManager $manager

    set pulse [java::new ptolemy.actor.lib.Pulse $e0 pulse]
    set codebook [java::new ptolemy.domains.sdf.lib.huffman.CodeBook $e0 codebook]
    $codebook addDebugListener [java::new ptolemy.kernel.util.StreamListener]
    set rec [java::new ptolemy.actor.lib.Sink $e0 rec]
    
    set values [java::new {int[][]} {1 30} [list [list 1 2 1 8 4 5 6 7 8 2 2 4 6 2 8 1 8 1 3 5 6 8 1 2 8 8 5 2 6 2]]]
    set valuesParam [getParameter $pulse values]
    $valuesParam setToken [java::new ptolemy.data.IntMatrixToken $values]
    
    set indexes [java::new {int[][]} {1 30} [list [list 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29]]]
    set indexesParam [getParameter $pulse indexes]
    $indexesParam setToken [java::new ptolemy.data.IntMatrixToken $indexes]

    set sizeParam [getParameter $codebook trainingSequenceSize]
    $sizeParam setToken [java::new ptolemy.data.IntToken 30]

    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $pulse] output] \
            [java::field $codebook input]
    ##$e0 connect \
    ##        [java::field $codebook output] \
    ##        [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    

    [$e0 getManager] execute
    
} {}
######################################################################
####
#

