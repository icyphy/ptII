# Test Equals.
#
# @Author: John Li
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
####
#
test Equals-1.1 {test constructor and clone} {
    set e0 [sdfModel 1]
    set equals [java::new ptolemy.actor.lib.logic.Equals $e0 equals]
    set newobj [java::cast ptolemy.actor.lib.logic.Equals [$equals clone]]
    # Success here is just not throwing an exception.
    list {}
} {{}}

######################################################################
#### Test Equals in an SDF model
#
test Equals-2.1 {test equality of two different constant integers} {

    # set boolean [java::new ptolemy.data.BooleanToken true]
    # set booleanClass [$boolean getClass]
    # $value setTypeEquals $booleanClass
    # $value2 setTypeEquals $booleanClass
    # $value setExpression true
    # $value2 setExpression true
    # won't allow value.type to be set to BooleanToken...

    set const [java::new ptolemy.actor.lib.Const $e0 const]
    set const2 [java::new ptolemy.actor.lib.Const $e0 const2]
    set value [getParameter $const value]
    set value2 [getParameter $const2 value]
    $value2 setExpression {2.0}

    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    set upperPort [java::field $equals upperPort]
    set lowerPort [java::field $equals lowerPort]
    set r1 [$e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $const] output] \
       $upperPort]
    set r2 [$e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $const2] output] \
       $lowerPort]
      $e0 connect \
       [java::field $equals output] \
       [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {false}

test Equals-2.2  {test equality of two Boolean Tokens} {

    $value setToken [java::new ptolemy.data.BooleanToken true]
    $value2 setToken [java::new ptolemy.data.BooleanToken true]

    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {true}
