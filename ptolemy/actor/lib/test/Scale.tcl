# Test Scale.
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

# Tycho test bed, see $TYCHO/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

######################################################################
####
#
test Scale-1.1 {test constructor and clone} {
    set e0 [sdfModel 5]
    set scalebase [java::new ptolemy.actor.lib.Scale $e0 scale]
    set scale [java::cast ptolemy.actor.lib.Scale [$scalebase clone]]
    $scalebase setContainer [java::null]
    $scale setContainer $e0
    # Success here is just not throwing an exception.
    list {}
} {{}}

######################################################################
#### Test Scale in an SDF model
#
test Scale-2.1 {test with the default parameter values} {
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    set factor [getParameter $scale factor]
    # Use clone of scale to make sure that is ok.
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $ramp] output] \
       [java::field [java::cast ptolemy.actor.lib.Transformer $scale] input]
    $e0 connect \
       [java::field \
       [java::cast ptolemy.actor.lib.Transformer $scale] output] \
       [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {0 1 2 3 4}

test Scale-2.2 {test with the alternative parameter values} {
    $factor setExpression {0.1}
    [$factor getToken] stringValue
    [$e0 getManager] execute
    ptclose [enumToTokenValues [$rec getRecord 0]] {0.0 0.1 0.2 0.3 0.4}
} {1}

test Scale-2.3 {test with the alternative parameter values} {
    $step setExpression {0.1}
    [$factor getToken] stringValue
    [$e0 getManager] execute
    ptclose [enumToTokenValues [$rec getRecord 0]] {0.0 0.01 0.02 0.03 0.04}
} {1}

######################################################################
#### Test Scale with matrices
#
# test Scale-3.1 {test with matrices} {
#     set e0 [sdfModel 1]
#     set const [java::new ptolemy.actor.lib.Const $e0 const]
#     set value [getParameter $const value]
#     $value setExpression {[1; 2]}
# 
#     set scale [java::new ptolemy.actor.lib.Scale $e0 scale]
#     set factor [getParameter $scale factor]
#     $factor setExpression {[2, 3; 4, 5]}
# 
#     set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
#     $e0 connect \
#        [java::field [java::cast ptolemy.actor.lib.Source $const] output] \
#        [java::field [java::cast ptolemy.actor.lib.Transformer $scale] input]
#     $e0 connect \
#        [java::field \
#        [java::cast ptolemy.actor.lib.Transformer $scale] output] \
#        [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
#     [$e0 getManager] execute
#     enumToTokenValues [$rec getRecord 0]    
# } {}
