# Test Maximum.
#
# @Author: Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997-1999 The Regents of the University of California.
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
test Maximum-1.1 {test constructor and clone} {
    set e0 [sdfModel 5]
    set baseobj [java::new ptolemy.actor.lib.Maximum $e0 baseobj]
    set maximum [java::cast ptolemy.actor.lib.Maximum [$baseobj clone]]
    $maximum setName maximum
    $maximum setContainer $e0
    $baseobj setContainer [java::null]
    # Success here is just not throwing an exception.
    list {}
} {{}}

######################################################################
#### Test Maximum in an SDF model
#
test Maximum-2.1 {test maximum} {
    set pulse [java::new ptolemy.actor.lib.Pulse $e0 pulse]
    set values [getParameter $pulse values]
    set indexes [getParameter $pulse indexes]
    $values setExpression {[-2, -1, 0, 1, 2]}
    $indexes setExpression {[0, 1, 2, 3, 4]}
    set const [java::new ptolemy.actor.lib.Const $e0 const]
    set value [getParameter $const value]
    $value setExpression {0.0}
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    set input [java::field [java::cast ptolemy.actor.lib.Transformer \
            $maximum] input]
    set r1 [$e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $pulse] output] \
       $input]
    set r2 [$e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $const] output] \
       $input]
    $e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Transformer \
            $maximum] output] \
       [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {0.0 0.0 0.0 1.0 2.0}
