# Test DoubleToFix.
#
# @Author: Bart Kienhuis, Contributor: Ed Willink
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
#### Test DoubleToFix in an SDF model
#

test DoubleToFix-1.1 {Test DoubleToFix} {
    set e0 [sdfModel 1]
    set const [java::new ptolemy.actor.lib.Const $e0 const]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    set conver [java::new ptolemy.actor.lib.conversions.DoubleToFix \
                    $e0 conver]

    # Get a clone to test cloning.
    set clone [java::cast ptolemy.actor.lib.conversions.DoubleToFix \
		   [$conver clone [$e0 workspace]]]
    $conver {setContainer ptolemy.kernel.CompositeEntity} [java::null]
    $clone setName d2fClone
    $clone {setContainer ptolemy.kernel.CompositeEntity} $e0

    set value [getParameter $const value]
    $value setToken [java::new {ptolemy.data.DoubleToken double} 3.0]

    set precision [java::field $clone precision]
    $precision setExpression {[5, 4]}

    $e0 connect \
      [java::field [java::cast ptolemy.actor.lib.Source $const] output] \
      [java::field [java::cast ptolemy.actor.lib.Transformer $clone] input]

    $e0 connect \
     [java::field [java::cast ptolemy.actor.lib.Transformer $clone] output] \
     [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]

    [$e0 getManager] execute
    set result [list [enumToTokenValues [$rec getRecord 0]]]
    #ptclose $result {-1.961 -2.270}
} {fix(3.0,5,4)}

test DoubleToFix-2.1 {Test the Round Mode} {

    set value [getParameter $const value]
    $value setToken [java::new {ptolemy.data.DoubleToken double} 0.9]

    set precision [java::field $clone precision]
    $precision setExpression {[3, 2]}

    set rounding [java::field $clone rounding]
    $rounding setExpression {nearest}

    [$e0 getManager] execute
    set result [list [enumToTokenValues [$rec getRecord 0]]]

} {fix(1.0,3,2)}

test DoubleToFix-3.1 {Test the Truncate Mode} {

    set value [getParameter $const value]
    $value setToken [java::new {ptolemy.data.DoubleToken double} 0.9]

    set precision [java::field $clone precision]
    $precision setExpression {[3, 2]}

    set rounding [java::field $clone rounding]
    $rounding setExpression {truncate}

    [$e0 getManager] execute 
    set result [list [enumToTokenValues [$rec getRecord 0]]]

} {fix(0.5,3,2)}

test DoubleToFix-3.2 {Test the Modulo Mode} {

    set value [getParameter $const value]
    $value setToken [java::new {ptolemy.data.DoubleToken double} 3.9]

    set precision [java::field $clone precision]
    $precision setExpression {[3, 2]}

    set overflow [java::field $clone overflow]
    $overflow setExpression {modulo}

    [$e0 getManager] execute 
    set result [list [enumToTokenValues [$rec getRecord 0]]]

} {fix(-0.5,3,2)}

test DoubleToFix-3.3 {Test the Saturate Mode} {

    set value [getParameter $const value]
    $value setToken [java::new {ptolemy.data.DoubleToken double} 3.9]

    set precision [java::field $clone precision]
    $precision setExpression {[3, 2]}

    set overflow [java::field $clone overflow]
    $overflow setExpression {saturate}

    [$e0 getManager] execute 
    set result [list [enumToTokenValues [$rec getRecord 0]]]

} {fix(1.5,3,2)}
