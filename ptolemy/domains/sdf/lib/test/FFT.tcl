# Test FFT.
#
# @Author: Bart KIenhuis
#
# @Version: $Id$
#
# @Copyright (c) 1997-2003 The Regents of the University of California.
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
#### Test FFT in an SDF model
#

test FFT-1.1 {Test FFT} {
    set e0 [sdfModel 1 ]
    set pulse [java::new ptolemy.actor.lib.Pulse $e0 pulse]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    set conver [java::new ptolemy.domains.sdf.lib.FFT \
                    $e0 FFT ]

    # Get a clone of the FFT to test cloning.
    set clone [java::cast ptolemy.domains.sdf.lib.FFT \
		   [$conver clone [$e0 workspace]]]
    $conver {setContainer ptolemy.kernel.CompositeEntity} [java::null]
    $clone setName FFTclone
    $clone {setContainer ptolemy.kernel.CompositeEntity} $e0
 
    $e0 connect \
      [java::field [java::cast ptolemy.actor.lib.Source $pulse] output] \
      [java::field [java::cast ptolemy.domains.sdf.lib.SDFTransformer $clone] input]

    $e0 connect \
     [java::field [java::cast ptolemy.domains.sdf.lib.SDFTransformer $clone] output] \
     [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]

    # Set the taps for the FFT
    set order [java::new ptolemy.data.IntToken 3]
    set orderParam [getParameter $clone order]
    $orderParam setToken $order

    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]

} {{1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i}}
