# Test PolarToRectangular.
#
# @Author: Michael Leung
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
#### Test PolarToRectangular in an SDF model
#

test PolarToRectangular-1.1 {test 1} {
    set e0 [sdfModel 1]
    set const1 [java::new ptolemy.actor.lib.Const $e0 const1]
    set const2 [java::new ptolemy.actor.lib.Const $e0 const2]
    set rec1 [java::new ptolemy.actor.lib.Recorder $e0 rec1]
    set rec2 [java::new ptolemy.actor.lib.Recorder $e0 rec2]
    set conver [java::new ptolemy.actor.lib.conversions.PolarToRectangular \
                    $e0 conver]

    set value1 [getParameter $const1 value]
    $value1 setToken [java::new {ptolemy.data.DoubleToken double} 3.0]
    set value2 [getParameter $const2 value]
    $value2 setToken [java::new {ptolemy.data.DoubleToken double} 4.0]

    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $const1] output]             [java::field $conver magnitudeInput]

    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $const2] output]             [java::field $conver angleInput]

    $e0 connect \
            [java::field $conver xOutput] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec1] input]

    $e0 connect \
            [java::field $conver yOutput] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec2] input]

    [$e0 getManager] execute
    set result [list [enumToTokenValues [$rec1 getRecord 0]] \
         [enumToTokenValues [$rec2 getRecord 0]]]
    ptclose $result {-1.9609309 -2.2704075}
} {1}
