# Test FixToDouble.
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
#### Test FixToDouble in an SDF model
#

test FixToDouble-1.1 {Test FixToDouble} {
    set e0 [sdfModel 1]
    set const [java::new ptolemy.actor.lib.Const $e0 const]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    set conver [java::new ptolemy.actor.lib.conversions.FixToDouble \
                    $e0 conver]

    # Get a clone to test cloning.
    set clone [java::cast ptolemy.actor.lib.conversions.FixToDouble \
		   [$conver clone [$e0 workspace]]]
    $conver {setContainer ptolemy.kernel.CompositeEntity} [java::null]
    $clone setName f2dClone
    $clone {setContainer ptolemy.kernel.CompositeEntity} $e0

    set value [getParameter $const value]
    $value setToken [java::new {ptolemy.data.FixToken double int int} 4.0 5 2 ]

    # FixToDouble no longer has a precision parameter.
    # set precision [getParameter $clone precision]
    # $precision setExpression "\[5, 3\]"
   
    $e0 connect \
      [java::field [java::cast ptolemy.actor.lib.Source $const] output] \
      [java::field [java::cast ptolemy.actor.lib.Transformer $clone] input]

    $e0 connect \
     [java::field [java::cast ptolemy.actor.lib.Transformer $clone] output] \
     [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]

    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]

} {1.875}

######################################################################
#### Test FixToDouble in an SDF model

test FixToDouble-3.1 {Test rescaling to other Precision with saturate \
	overflow} {

    set e0 [sdfModel 12]
    set pulse [java::new ptolemy.actor.lib.Pulse $e0 pulse]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    set conver [java::new ptolemy.actor.lib.conversions.FixToFix \
                    $e0 conver]
    set represent [java::new ptolemy.actor.lib.conversions.FixToDouble \
                    $e0 represent]

    $e0 connect \
      [java::field [java::cast ptolemy.actor.lib.Source $pulse] output] \
      [java::field [java::cast ptolemy.actor.lib.Transformer $conver] input]

    $e0 connect \
      [java::field [java::cast ptolemy.actor.lib.Transformer $conver] output] \
      [java::field [java::cast ptolemy.actor.lib.Transformer $represent] input]

    $e0 connect \
     [java::field [java::cast ptolemy.actor.lib.Transformer $represent] output] \
     [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]

    set val0 [java::new {ptolemy.data.FixToken double int int} 4.0 5 3]
    set val1 [java::new {ptolemy.data.FixToken double int int} 3.0 5 3]
    set val2 [java::new {ptolemy.data.FixToken double int int} 2.0 5 3]
    set val3 [java::new {ptolemy.data.FixToken double int int} 1.0 5 3]
    set val4 [java::new {ptolemy.data.FixToken double int int} 0.8 5 3]
    set val5 [java::new {ptolemy.data.FixToken double int int} 0.3 5 3]
    set val6 [java::new {ptolemy.data.FixToken double int int} -0.3 5 3]
    set val7 [java::new {ptolemy.data.FixToken double int int} -0.8 5 3]
    set val8 [java::new {ptolemy.data.FixToken double int int} -1.0 5 3]
    set val9 [java::new {ptolemy.data.FixToken double int int} -2.0 5 3]
    set val10 [java::new {ptolemy.data.FixToken double int int} -3.0 5 3]
    set val11 [java::new {ptolemy.data.FixToken double int int} -4.2 5 3]
    set valArray [java::new {ptolemy.data.Token[]} 12 [list $val0 $val1 \
	$val2 $val3 $val4 $val5 $val6 $val7 $val8 $val9 $val10 $val11]]
    set valToken [java::new {ptolemy.data.ArrayToken} $valArray]

    set valuesParam [getParameter $pulse values]
    $valuesParam setToken $valToken
 
    set indexesParam [getParameter $pulse indexes]
    $indexesParam setExpression {{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}}

    set precision [getParameter $conver precision]
    $precision setExpression "\[3, 2\]"

    set overflow [java::field $conver overflow ]
    $overflow setExpression {saturate}
   
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]

} {1.5 1.5 1.5 1.0 1.0 0.5 0.0 -0.5 -1.0 -2.0 -2.0 -2.0}

test FixToDouble-3.2 {Test rescaling to other Precision with zero saturate \
	 overflow} {

    set overflow [java::field $conver overflow ]
    $overflow setExpression {to_zero}
   
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]

} {0.0 0.0 0.0 1.0 1.0 0.5 0.0 -0.5 -1.0 -2.0 0.0 0.0}

test FixToDouble-3.3 {Test rescaling to other Precision with modulo \
	 overflow} {

    set overflow [java::field $conver overflow ]
    $overflow setExpression {modulo}
   
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]

} {0.0 -1.0 -2.0 1.0 1.0 0.5 0.0 -0.5 -1.0 -2.0 1.0 0.0}

test FixToDouble-3.3 {Test rescaling to other Precision with grow \
	 overflow} {

    set overflow [java::field $conver overflow ]
    $overflow setExpression {grow}
   
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]

} {4.0 3.0 2.0 1.0 1.0 0.5 0.0 -0.5 -1.0 -2.0 -3.0 -4.0}

test FixToDouble-3.4 {Test rescaling to other Precision with floor \
	 rounding} {

    set rounding [java::field $conver rounding ]
    $rounding setExpression {floor}
   
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]

} {3.5 3.0 2.0 1.0 0.5 0.0 -0.5 -1.0 -1.0 -2.0 -3.0 -4.0}
