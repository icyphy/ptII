# Test Connect
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

# If a file contains non-graphical tests, then it should be named .tcl
# If a file contains graphical tests, then it should be called .itcl
#
# It would be nice if the tests would work in a vanilla itkwish binary.
# Check for necessary classes and adjust the auto_path accordingly.
#

######################################################################
#### Connect
#

test Connect-1.0 {test adding a new entity and connecting it} {
    set e0 [sdfModel]
    set const [java::new ptolemy.actor.lib.Const $e0 const]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $const] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    set m [$e0 getManager]
    $m addChangeListener \
            [java::new ptolemy.kernel.event.StreamChangeListener]
    set dir [$e0 getDirector]
    $dir addDebugListener \
            [java::new ptolemy.kernel.util.StreamListener]
    $m initialize
    $m iterate
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 ramp]
    set c1 [java::new ptolemy.data.expr.SetParameter $e0 \
            [java::field $ramp step] 0.01]
    set c2 [java::new ptolemy.actor.event.Connect $e0 \
            [java::field [java::cast ptolemy.actor.lib.Source $ramp] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]]
    set c3 [java::new ptolemy.actor.event.InitializeActor $e0 $ramp]
    set c4 [java::new ptolemy.data.expr.SetParameter $e0 \
            [java::field $ramp init] 0.01]
    set changelist [java::new ptolemy.kernel.event.ChangeList $e0 "list"]
    $m requestChange $changelist
    $changelist add $c1
    $changelist add $c4
    $changelist add $c2
    $changelist add $c3

    $m iterate
    $m iterate
    $m iterate
    $m wrapup
    list [enumToTokenValues [$rec getRecord 0]] \
            [enumToTokenValues [$rec getRecord 1]]
} {{1 1 1 1} {_ 0.01 0.02 0.03}}
