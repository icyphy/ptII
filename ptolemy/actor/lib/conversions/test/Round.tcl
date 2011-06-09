# Test Round
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2006 The Regents of the University of California.
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

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

######################################################################
#### Test Round 
#
test Round-2.1 {test with the default output values} {
    set e0 [sdfModel 5]
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 ramp]
    set round [java::new ptolemy.actor.lib.conversions.Round $e0 round]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $ramp] output] \
            [java::field [java::cast ptolemy.actor.lib.Transformer $round] input]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Transformer $round] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]

    set init [getParameter $ramp init]
    $init setToken [java::new ptolemy.data.DoubleToken 2.5]

    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {3 4 5 6 7}

######################################################################
#### Test Round 
#
test Round-2.2 {NaNs throw an exception} {
    # uses 2.1 above	
    $init setToken [java::new ptolemy.data.DoubleToken "NaN"]
    set step [getParameter $ramp step]
    $step setToken [java::new ptolemy.data.DoubleToken "NaN"]

    catch {[$e0 getManager] execute} msg
    list $msg	
} {{ptolemy.kernel.util.IllegalActionException: Input is Double.NaN, there is no way to represent a NaN as an integer.}}


######################################################################
#### ceil
#
test Round-3.0 {Test ceil} {
    # uses 2.1 above	
    $init setToken [java::new ptolemy.data.DoubleToken "1.0"]
    set step [getParameter $ramp step]
    $step setToken [java::new ptolemy.data.DoubleToken "1.3"]

    set function [java::field $round function]
    $function setExpression "ceil"

    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {1 3 4 5 7} 

######################################################################
#### floor
#
test Round-3.1 {test floor} {
    # uses 2.1 above	
    set function [java::field $round function]
    $function setExpression "floor"

    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {1 2 3 4 6}

######################################################################
#### truncate
#
test Round-3.2 {truncate} {
    # uses 2.1 above	
    set function [java::field $round function]
    $function setExpression "truncate"

    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {1 2 3 4 6}
