# Tests for the CTBaseIntegrator
#
# @Author: Jie Liu
#
# @Version: $Id$
#
# @Copyright (c) 1998-2003 The Regents of the University of California.
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####  Generally used director and composite actor.
#
set ca [java::new ptolemy.actor.TypedCompositeActor]
set dir [java::new ptolemy.domains.ct.kernel.CTMultiSolverDirector \
	$ca CTDirector]
$dir setCurrentStepSize 1.0


######################################################################
####  Test constructors.
#
test CTBaseIntegrator-1.1 {Construct a CTBaseIntegrator, get parameters} {
    set integ [java::new ptolemy.domains.ct.kernel.test.CTTestIntegrator \
	    $ca Integ]
    set param [java::cast ptolemy.data.expr.Parameter \
	    [$integ getAttribute initialState]]
    [java::cast ptolemy.data.DoubleToken [$param getToken]] \
	    doubleValue
} {0.0}

######################################################################
####  Test change of parameter.
#
test CTBaseIntegrator-2.1 {Construct a CTBaseIntegrator, get parameters} {
    $param setToken [java::new ptolemy.data.DoubleToken 1.0]
    [java::cast ptolemy.data.DoubleToken [ \
	    [java::cast ptolemy.data.expr.Parameter $param] getToken]] \
	    doubleValue
} {1.0}

######################################################################
####  check initialization
#
test CTBaseIntegrator-3.1 {check initialization} {
    $dir preinitialize
    $dir initialize
    list [$integ getState] [$integ getTentativeState] \
	    [$integ getDerivative]
} {1.0 1.0 0.0}

######################################################################
####  check history
#
test CTBaseIntegrator-4.1 {default history capacity} {
    list [$integ getHistoryCapacity]
} {0}

test CTBaseIntegrator-4.2 {check history} {
    $integ setHistoryCapacity 1
    $integ setTentativeState 1.0
    $integ setTentativeDerivative 0.0
    $integ postfire
    arrayToStrings [$integ getHistory 0]
} {1.0 0.0}

test CTBaseIntegrator-4.3 {override history} {
    $integ setTentativeState -1.0
    $integ setTentativeDerivative 0.0
    $integ postfire
    arrayToStrings [$integ getHistory 0]
} {-1.0 0.0}

test CTBaseIntegrator-4.4 {add history capacity} {
    $integ setHistoryCapacity 2
    $integ setTentativeState -2.0
    $integ setTentativeDerivative 0.0
    $integ postfire
    list [$integ getHistoryCapacity] [arrayToStrings [$integ getHistory 0]] \
	    [arrayToStrings [$integ getHistory 1]]
} {2 {-2.0 0.0} {-1.0 0.0}}

test CTBaseIntegrator-4.5 {override the history again} {
    $integ setTentativeState -3.0
    $integ setTentativeDerivative 0.0
    $integ postfire
    list [$integ getHistoryCapacity] [arrayToStrings [$integ getHistory 0]] \
	    [arrayToStrings [$integ getHistory 1]]
} {2 {-3.0 0.0} {-2.0 0.0}}

test CTBaseIntegrator-4.6 {access out of bounds} {
    catch {$integ getHistory 3} msg
    list $msg
} {{java.lang.IndexOutOfBoundsException: Index: 3, Size: 2}}

test CTBaseIntegrator-4.7 {rebalance} {
    #clear history infor except the last one.
    $integ setHistoryCapacity 1
    #set new capacity and add an entry
    $integ setHistoryCapacity 2
    $integ setTentativeState -2.0
    $integ setTentativeDerivative 1.0
    $integ postfire
    $dir setCurrentStepSize [expr {[$dir getCurrentStepSize] * 0.5}]
    $integ balanceHistory
    list [$integ getHistoryCapacity] [arrayToStrings [$integ getHistory 0]] \
	    [arrayToStrings [$integ getHistory 1]]
} {2 {-2.0 1.0} {-2.625 1.25}}

test CTBaseIntegrator-4.8 {rebalance: extrapolation} {  
    $integ setHistoryCapacity 1
    $integ setHistoryCapacity 2
    $integ setTentativeState -3.0
    $integ setTentativeDerivative 0.0
    $integ postfire
    $dir setCurrentStepSize [expr {[$dir getCurrentStepSize] * 2.1}]
    $integ balanceHistory
    list [$integ getHistoryCapacity] [arrayToStrings [$integ getHistory 0]] \
	    [arrayToStrings [$integ getHistory 1]]
} {2 {-3.0 0.0} {-0.9 2.1}}

test CTBaseIntegrator-4.9 {rebalance: interpolation and extrapolation} {  
    $integ setHistoryCapacity 0
    $dir setCurrentStepSize 1.0
    $integ setHistoryCapacity 3
    $integ setTentativeState 1.0
    $integ setTentativeDerivative 0.0
    $integ postfire
    $integ setTentativeState 2.0
    $integ setTentativeDerivative 1.0
    $integ postfire
    $integ setTentativeState 3.0
    $integ setTentativeDerivative 1.5
    $integ postfire
    $dir setCurrentStepSize [expr {[$dir getCurrentStepSize] * 1.5}]
    $integ balanceHistory
    list [$integ getHistoryCapacity] [arrayToStrings [$integ getHistory 0]] \
	    [arrayToStrings [$integ getHistory 1]] \
	    [arrayToStrings [$integ getHistory 2]]
} {3 {3.0 1.5} {1.375 1.25} {0.0 -1.0}}

######################################################################
#### test auxVariables
#
test CTBaseIntegrator-5.1 {test auxVariables} {
    $integ prefire
    arrayToStrings [$integ getAuxVariables]
} {0.0 0.0 0.0 0.0}

test CTBaseIntegrator-5.2 {set auxVariables} {
    $integ setAuxVariables 1 1.0
    arrayToStrings [$integ getAuxVariables]
} {0.0 1.0 0.0 0.0}

######################################################################
#### set and get states and derivatives
#
test CTBaseIntegrator-6.1 {set and get states and derivatives} {
    $integ setTentativeState 1.0
    $integ setTentativeDerivative 0.0
    $integ setHistoryCapacity 1
    $integ postfire
    list [$integ getState] [$integ getDerivative]
} {1.0 0.0}



