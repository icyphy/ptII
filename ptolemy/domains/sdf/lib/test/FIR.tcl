# Test FIR.
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
#### Test FIR in an SDF model
#

test FIR-1.1 {Test FIR for double FIR} {
    set e0 [sdfModel 10 ]
    set pulse [java::new ptolemy.actor.lib.Pulse $e0 pulse]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    set conver [java::new ptolemy.domains.sdf.lib.FIR \
                    $e0 FIR ]

    # Get a clone of the FIR to test cloning.
    set clone [java::cast ptolemy.domains.sdf.lib.FIR \
		   [$conver clone [$e0 workspace]]]
    $conver {setContainer ptolemy.kernel.CompositeEntity} [java::null]
    $clone setName FIRclone
    $clone {setContainer ptolemy.kernel.CompositeEntity} $e0

    $e0 connect \
      [java::field [java::cast ptolemy.actor.lib.Source $pulse] output] \
      [java::field [java::cast ptolemy.domains.sdf.lib.SDFTransformer $clone] input]

    $e0 connect \
     [java::field [java::cast ptolemy.domains.sdf.lib.SDFTransformer $clone] output] \
     [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]

    # Set the taps for the FIR
#    set taps [java::new {double[][]} 1 [list [list -0.040609 -0.001628 \
#	0.17853 0.37665 0.37665 0.17853 -0.001628 -0.040609]] ]
#    set tapMatrix [java::new {ptolemy.data.DoubleMatrixToken} $taps ]
    set tapParam [getParameter $clone taps]
#    $tapParam setToken $tapMatrix
    $tapParam setExpression {{-0.040609, -0.001628, 0.17853, 0.37665, 0.37665, 0.17853, -0.001628, -0.040609}}

    [$e0 getManager] execute
    epsilonDiff \
	    {-0.040609 -0.001628 0.17853 0.37665 0.37665 0.17853 -0.001628 -0.040609 0.0 0.0} \
	    [enumToTokenValues [$rec getRecord 0]]

} {}


test FIR-2.1 {Test FIR type exeception} {

#    $tapParam setExpression {fix([-0.040609, -0.001628, 0.17853, 0.37665, 0.37665, 0.17853, -0.001628, -0.040609], 6, 2)}

    set p [java::new ptolemy.math.Precision "(6/2)" ]
    set q [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -0.040609 $p ]
    set t1 [java::new ptolemy.data.FixToken $q ]
    set q [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -0.001628 $p ]
    set t2 [java::new ptolemy.data.FixToken $q ]

    set valArray [java::new {ptolemy.data.Token[]} 2 [list $t1 $t2]]
    set fixArrayToken [java::new {ptolemy.data.ArrayToken} $valArray]

    $tapParam setToken $fixArrayToken

    catch { [$e0 getManager] execute } msg

    # Note, this order of the error message might be platform dependent
    set containsException [regexp \
	    {ptolemy.actor.TypeConflictException: Type conflicts occurred in .top on the following inequalities:} \
	    $msg]
    set containsFIRclone [regexp \
	    {(ptolemy.actor.TypedIOPort {.top.FIRclone.output}, scalar)} \
	    $msg]
    set containsRecInput [regexp \
	    {(ptolemy.actor.TypedIOPort {.top.rec.input}, scalar)} \
	    $msg]
    list $containsException $containsFIRclone $containsRecInput
} {1 1 1}

test FIR-3.1 {Test FIR for FIX datatype} {
    set e0 [sdfModel 10 ]
    set pulse [java::new ptolemy.actor.lib.Pulse $e0 pulse]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    set fir [java::new ptolemy.domains.sdf.lib.FIR \
                    $e0 FIR ]
    set f2d [java::new ptolemy.actor.lib.conversions.FixToDouble \
	$e0 f2d ]
    set d2f [java::new ptolemy.actor.lib.conversions.DoubleToFix \
	$e0 d2f ]

    $e0 connect \
      [java::field [java::cast ptolemy.actor.lib.Source $pulse] output] \
      [java::field [java::cast ptolemy.actor.lib.Transformer $d2f] input]

    $e0 connect \
      [java::field [java::cast ptolemy.actor.lib.Transformer $d2f] output] \
      [java::field [java::cast ptolemy.domains.sdf.lib.SDFTransformer $fir] input]

    $e0 connect \
      [java::field [java::cast ptolemy.domains.sdf.lib.SDFTransformer $fir] output] \
      [java::field [java::cast ptolemy.actor.lib.Transformer $f2d] input]

    $e0 connect \
      [java::field [java::cast ptolemy.actor.lib.Transformer $f2d] output] \
      [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]

    # Set the taps for the FIR
    set tapParam [getParameter $fir taps]
#    $tapParam setExpression {fix([-0.040609, -0.001628, 0.17853, 0.37665, 0.37665, 0.17853, -0.001628, -0.040609], 6, 2)}

    set p [java::new ptolemy.math.Precision "(6/2)" ]
    set q [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -0.040609 $p ]
    set t1 [java::new ptolemy.data.FixToken $q ]
    set q [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -0.001628 $p ]
    set t2 [java::new ptolemy.data.FixToken $q ]

    set q [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 0.17853 $p ]
    set t3 [java::new ptolemy.data.FixToken $q ]
    set q [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 0.37665 $p ]
    set t4 [java::new ptolemy.data.FixToken $q ]

    set valArray [java::new {ptolemy.data.Token[]} 8 [list $t1 $t2 $t3 $t4 $t4 $t3 $t2 $t1]]
    set fixArrayToken [java::new {ptolemy.data.ArrayToken} $valArray]

    $tapParam setToken $fixArrayToken
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]

} {-0.0625 0.0 0.1875 0.375 0.375 0.1875 0.0 -0.0625 0.0 0.0}
