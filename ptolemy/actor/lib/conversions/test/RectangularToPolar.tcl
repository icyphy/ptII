# Test RectangularToPolar.
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
#### Test RectangularToPolar in an SDF model
#

test RectangularToPolar-1.1 {test 1} {
    set e0 [sdfModel 1]
    set const1 [java::new ptolemy.actor.lib.Const $e0 const1]
    set const2 [java::new ptolemy.actor.lib.Const $e0 const2]
    set rec1 [java::new ptolemy.actor.lib.Recorder $e0 rec1]
    set rec2 [java::new ptolemy.actor.lib.Recorder $e0 rec2]
    set conver [java::new ptolemy.actor.lib.conversions.RectangularToPolar \
                    $e0 conver]

    set value1 [getParameter $const1 value]
    $value1 setToken [java::new {ptolemy.data.DoubleToken double} 3.0]
    set value2 [getParameter $const2 value]
    $value2 setToken [java::new {ptolemy.data.DoubleToken double} 4.0]

# Since all of my ports are public, so I don't need to cast them.

    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $const1] output]             [java::field $conver xInput]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $const2] output]             [java::field $conver yInput]
    $e0 connect \
            [java::field $conver magnitudeOutput] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec1] input]
    $e0 connect \
            [java::field $conver angleOutput] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec2] input]

    [$e0 getManager] execute
    set result [list [enumToTokenValues [$rec1 getRecord 0]] \
         [enumToTokenValues [$rec2 getRecord 0]]]
    ptclose $result {5.0 0.927}
} {1}

######################################################################
#### Test RectangularToPolar in an SDF model
#

test RectangularToPolar-1.2 {test 2: testing both PolarToRec and RecToPolar} {
    set e0 [sdfModel 1]
    set const1 [java::new ptolemy.actor.lib.Const $e0 const1]
    set const2 [java::new ptolemy.actor.lib.Const $e0 const2]
    set rec1 [java::new ptolemy.actor.lib.Recorder $e0 rec1]
    set rec2 [java::new ptolemy.actor.lib.Recorder $e0 rec2]
    set conver1 [java::new ptolemy.actor.lib.conversions.RectangularToPolar \
                    $e0 conver1]
    set conver2 [java::new ptolemy.actor.lib.conversions.PolarToRectangular \
                    $e0 conver2]


    set value1 [getParameter $const1 value]
    $value1 setToken [java::new {ptolemy.data.DoubleToken double} 3.0]

    set value2 [getParameter $const2 value]
    $value2 setToken [java::new {ptolemy.data.DoubleToken double} 4.0]


    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $const1] output]             [java::field $conver1 xInput]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $const2] output]             [java::field $conver1 yInput]
    $e0 connect \
            [java::field $conver1 magnitudeOutput] \
	    [java::field $conver2 magnitudeInput]
    $e0 connect \
            [java::field $conver1 angleOutput] \
	    [java::field $conver2 angleInput]
    $e0 connect \
            [java::field $conver2 xOutput] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec1] input]
    $e0 connect \
            [java::field $conver2 yOutput] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec2] input]

    [$e0 getManager] execute
    set result [list [enumToTokenValues [$rec1 getRecord 0]] \
         [enumToTokenValues [$rec2 getRecord 0]]]
    ptclose $result {3.000 4.0}
} {1}
