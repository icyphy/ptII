# Test Ramp.
#
# @Author: Yuhong Xiong
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
test Ramp-1.1 {test clone} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 Ramp]
    $ramp typeConstraints

    #set newobj [$ramp clone]
    #set initVal [[[$newobj getAttribute init] getToken] doubleValue]
    #set stepVal [[[$newobj getAttribute step] getToken] doubleValue]

    set newobj [_testClone $ramp]
    set initVal [_testDoubleValue $newobj init]
    set stepVal [_testDoubleValue $newobj step]
    list $initVal $stepVal
} {0.0 1.0}

test Ramp-1.2 {test clone} {
    set orginit [$ramp getAttribute init]
    set dToken [java::new {ptolemy.data.DoubleToken double} 3.0]

    #$orginit setToken $dToken
    [java::cast ptolemy.data.expr.Parameter $orginit] \
	    setToken $dToken
    
    #set orgInitVal [[[$ramp getAttribute init] getToken] doubleValue]
    #set initVal [[[$newobj getAttribute init] getToken] doubleValue]
    set orgInitVal [_testDoubleValue $ramp init]
    set initVal [_testDoubleValue $newobj init]

    list $orgInitVal $initVal
} {3.0 0.0}

