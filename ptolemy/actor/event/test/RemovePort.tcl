# Test RemovePort
#
# @Author: Edward A. Lee
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

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}
# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
#### RemovePort
#

test RemovePort-1.0 {test removing a port} {
    set e0 [sdfModel 3]

    # Expression actor
    set expr [java::new ptolemy.actor.lib.Expression $e0 expr]
    set in1 [java::new ptolemy.actor.TypedIOPort $expr in1 true false]
    set in2 [java::new ptolemy.actor.TypedIOPort $expr in2 true false]
    set expression [java::field $expr expression]
    $expression setExpression "in1+in2"

    set ramp2 [java::new ptolemy.actor.lib.Ramp $e0 ramp2]   
    set ramp1 [java::new ptolemy.actor.lib.Ramp $e0 ramp1]   
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]

    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $ramp1] output] \
            $in1
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $ramp2] output] \
            $in2
    $e0 connect \
            [java::field $expr output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    set m [$e0 getManager]
    $m initialize
    $m iterate
    set c1 [java::new ptolemy.actor.event.RemovePort $e0 $in1]
    set c2 [java::new ptolemy.actor.event.RemoveActor $e0 $ramp1]
    set c3 [java::new ptolemy.data.expr.SetParameter \
            $e0 $expression "in2 + 5"]
    $m requestChange $c1
    $m requestChange $c2
    $m requestChange $c3
    $m iterate
    $m iterate
    $m wrapup
    enumToTokenValues [$rec getRecord 0]
} {0 6 7}
