# Test FIRDouble.
#
# @Author: Bart KIenhuis
#
# @Version: $Id$
#
# @Copyright (c) 1997-2000 The Regents of the University of California.
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

######################################################################
#### Test FIRDouble in an SDF model
#

test FIRDouble-1.1 {Test FIRDouble} {
    set e0 [sdfModel 10 ]
    set pulse [java::new ptolemy.actor.lib.Pulse $e0 pulse]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    set conver [java::new ptolemy.domains.sdf.lib.FIRDouble \
                    $e0 FIRDouble ]

    # Get a clone of the FIRDouble to test cloning.
    set clone [java::cast ptolemy.domains.sdf.lib.FIRDouble [$conver clone]]
    $conver setContainer [java::null]
    $clone setName FIRDoubleclone
    $clone setContainer $e0

    $e0 connect \
      [java::field [java::cast ptolemy.actor.lib.Source $pulse] output] \
      [java::field [java::cast ptolemy.domains.sdf.lib.FIR $clone] input]

    $e0 connect \
     [java::field [java::cast ptolemy.domains.sdf.lib.FIR $clone] output] \
     [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]

    # Set the taps for the FIRDouble
    set taps [java::new {double[][]} 1 [list [list -0.040609 -0.001628 \
	0.17853 0.37665 0.37665 0.17853 -0.001628 -0.040609]] ]
    set tapMatrix [java::new {ptolemy.data.DoubleMatrixToken} $taps ]
    set tapParam [getParameter $clone taps]
    $tapParam setToken $tapMatrix

    [$e0 getManager] execute
    epsilonDiff \
	    {-0.040609 -0.001628 0.17853 0.37665 0.37665 0.17853 -0.001628 -0.040609 0.0 0.0} \
	    [enumToTokenValues [$rec getRecord 0]]

} {}
