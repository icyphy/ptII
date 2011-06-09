# Tests for the ParameterSet class
#
# @Author: Christopher Brooks
#
# @Version $Id$
#
# @Copyright (c) 2006-2009 The Regents of the University of California.
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


######################################################################
####
# 
test ParameterSet-1.0 {Read in fileReadingAttribute.txt} {
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    set e2 [java::new ptolemy.kernel.ComponentEntity $e1 "e2"]
    set a [java::new ptolemy.actor.parameters.ParameterSet $e2 "a"]
    #set a [java::new ptolemy.data.expr.ScopeExtendingAttribute $e2 "a"]

    set p1 [java::new ptolemy.data.expr.Parameter $e1 "p"]
    set p2 [java::new ptolemy.data.expr.Parameter $a "p"]
    set p3 [java::new ptolemy.data.expr.Parameter $e2 "p"]

    $p1 setExpression "5"
    $p2 setExpression "7"
    $p3 setExpression "p"

    set r1 [list \
		[[$p1 getToken] toString] \
		[[$p2 getToken] toString] \
		[[$p3 getToken] toString]]
    set fileOrURL [java::cast ptolemy.data.expr.FileParameter [$a getAttribute fileOrURL]]
    set URL [[java::new java.io.File TestParameterSet.txt] \
	toURL]
    $fileOrURL setExpression [$URL toString]
    $a read

    set r2 [list \
		[[$p1 getToken] toString] \
		[[$p2 getToken] toString] \
		[[$p3 getToken] toString]]

    $a setContainer $e2

    set r3 [list \
		[[$p1 getToken] toString] \
		[[$p2 getToken] toString] \
		[[$p3 getToken] toString]]

    list $r1 $r2 $r3
} {{5 7 7} {5 42 42} {5 42 42}}

######################################################################
####
# 
test ParameterSet-2.1 {Check out ModelScope methods on e2} {
    set attribute [java::call ptolemy.data.expr.ModelScope \
		       {getScopedAttribute ptolemy.kernel.util.Attribute \
			    ptolemy.kernel.util.NamedObj String} \
		       [java::null] $e2 p]

    set object  [java::call ptolemy.data.expr.ModelScope \
		       {getScopedObject \
			    ptolemy.kernel.util.NamedObj String} \
		       $e2 p]

    list [$attribute toString] [$object toString]
} {{ptolemy.data.expr.Parameter {..e2.p} 42} {ptolemy.data.expr.Parameter {..e2.p} 42}} 

######################################################################
####
# 
test ParameterSet-2.2 {Check out the scope} {
    set const [java::new ptolemy.actor.lib.Const $e1 const]
    set parameter [getParameter $const value]
    $parameter setExpression "p"
    set attribute [java::call ptolemy.data.expr.ModelScope \
		       {getScopedAttribute ptolemy.kernel.util.Attribute \
			    ptolemy.kernel.util.NamedObj String} \
		       [java::null] $e1 p]

    set object  [java::call ptolemy.data.expr.ModelScope \
		       {getScopedObject \
			    ptolemy.kernel.util.NamedObj String} \
		       $e1 p]

    list [$attribute toString] [$object toString]
} {{ptolemy.data.expr.Parameter {..p} 5} {ptolemy.data.expr.Parameter {..p} 5}} 
