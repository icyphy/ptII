# Test SetParameter
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
#### SetParameter
#

test SetParameter-1.0 {test set parameter without queueing} {
    set e0 [sdfModel]
    set const [java::new ptolemy.actor.lib.Const $e0 const]
    set value [java::field $const value]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $const] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    set m [$e0 getManager]
    $m initialize
    $m iterate
    set c [java::new ptolemy.actor.event.SetParameter $e0 $value 2]
    $c execute
    $m iterate
    $m wrapup
    enumToTokenValues [$rec getRecord 0]
} {1 2}

test SetParameter-2.0 {change parameter type} {
    $m initialize
    $m iterate
    # Notice that this forces type resolution to be redone.
    set c [java::new ptolemy.actor.event.SetParameter $e0 $value {0.01}]
    $c execute
    $m iterate
    $m wrapup
    enumToTokenValues [$rec getRecord 0]
} {2 0.01}

test SetParameter-3.0 {queue change with the manager} {
    $m initialize
    $m iterate
    set c [java::new ptolemy.actor.event.SetParameter $e0 $value {"a"}]
    $m requestChange $c
    $m iterate
    $m wrapup
    enumToTokenValues [$rec getRecord 0]
} {0.01 a}

test SetParameter-3.1 {queue erroneous change with the manager} {
    $m initialize
    $m iterate
    set c [java::new ptolemy.actor.event.SetParameter $e0 $value {x}]
    $m requestChange $c
    catch {$m iterate} msg
    $m wrapup
    list [enumToTokenValues [$rec getRecord 0]] $msg
} {a {ptolemy.kernel.event.ChangeFailedException: .top:
Change request failed: Change value of parameter .top.const.value to x
ptolemy.kernel.util.IllegalActionException: Error parsing expression "x":
The ID x is undefined.}}

test SetParameter-4.0 {queue a change list} {
    $value setExpression {"b"}
    $m initialize
    $m iterate
    set listener [java::new ptolemy.kernel.event.StreamChangeListener]
    $m addChangeListener $listener
    set c1 [java::new ptolemy.actor.event.SetParameter $e0 $value {"x"}]
    set c2 [java::new ptolemy.actor.event.SetParameter $e0 $value {"y"}]
    set changelist [java::new ptolemy.kernel.event.ChangeList $e0 "list"]
    $m requestChange $changelist
    $changelist add $c1
    $changelist add $c2
    $m iterate
    $m wrapup
    enumToTokenValues [$rec getRecord 0]
} {b y}
