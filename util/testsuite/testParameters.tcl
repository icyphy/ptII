# Tcl procs to help with testing parameters
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1999-2000 The Regents of the University of California.
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

# To use this file, add the following code to your test file
# just after the source of testDefs.tcl
# 
# if {[info procs _testClone] == "" } then { 
#    source [file join $PTII util testsuite testParameters.tcl]
# }

# Unfortunately, changes between Tcl Blend 1.0 and 1.1 require
# the use of java::cast
# 
# In Tcl Blend 1.0, the following code worked
#  set newobj [$ramp clone]
#  set initVal [[[$newobj getAttribute init] getToken] doubleValue]
#  set stepVal [[[$newobj getAttribute step] getToken] doubleValue]
#
# In Tcl Blend 1.1, the following code is necessary:
#    set newobj [java::cast ptolemy.actor.lib.Ramp [$ramp clone]]
#
#    set initVal [[java::cast ptolemy.data.DoubleToken \
#	    [[java::cast ptolemy.data.expr.Parameter \
#	    [$newobj getAttribute init]] \
#            getToken]] doubleValue]
#
#    set stepVal [[java::cast ptolemy.data.DoubleToken \
#	    [[java::cast ptolemy.data.expr.Parameter \
#	    [$newobj getAttribute step]] \
#            getToken]] doubleValue]
#
# As a result, we created a few helper procs which are shorthand
# for the above glop.

########################################################
#### _testClone
# Call clone on object, then cast it to the right type
#
proc _testClone {object} {
    return [java::cast [java::info class $object] [$object clone]]
}

########################################################
#### _testDoubleValue
# Get the doubleValue of attributeName from object
#
proc _testDoubleValue {object attributeName} {
    set attribute [$object getAttribute $attributeName]
    if { $attribute == [java::null] } {
	error "test_doubleValue: Attribute '$attributeName' not found in $object" 
    }
    return [[java::cast ptolemy.data.DoubleToken \
	    [[java::cast ptolemy.data.expr.Parameter \
	    $attribute]  getToken]] doubleValue]
}

# Tcl procs to help with testing parameters
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1999-2000 The Regents of the University of California.
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

# To use this file, add the following code to your test file
# just after the source of testDefs.tcl
# 
# if {[info procs _testClone] == "" } then { 
#    source [file join $PTII util testsuite testParameters.tcl]
# }

# Unfortunately, changes between Tcl Blend 1.0 and 1.1 require
# the use of java::cast
# 
# In Tcl Blend 1.0, the following code worked
#  set newobj [$ramp clone]
#  set initVal [[[$newobj getAttribute init] getToken] doubleValue]
#  set stepVal [[[$newobj getAttribute step] getToken] doubleValue]
#
# In Tcl Blend 1.1, the following code is necessary:
#    set newobj [java::cast ptolemy.actor.lib.Ramp [$ramp clone]]
#
#    set initVal [[java::cast ptolemy.data.DoubleToken \
#	    [[java::cast ptolemy.data.expr.Parameter \
#	    [$newobj getAttribute init]] \
#            getToken]] doubleValue]
#
#    set stepVal [[java::cast ptolemy.data.DoubleToken \
#	    [[java::cast ptolemy.data.expr.Parameter \
#	    [$newobj getAttribute step]] \
#            getToken]] doubleValue]
#
# As a result, we created a few helper procs which are shorthand
# for the above glop.


########################################################
#### _testClone
# Call clone on object, then cast it to the right type
#
proc _testClone {object} {
    return [java::cast [java::info class $object] [$object clone]]
}

########################################################
#### _testDoubleValue
# Get the doubleValue of attributeName from object
#
proc _testDoubleValue {object attributeName} {
    set attribute [$object getAttribute $attributeName]
    if { $attribute == [java::null] } {
	error "test_doubleValue: Attribute '$attributeName' not found in $object" 
    }
    return [[java::cast ptolemy.data.DoubleToken \
	    [[java::cast ptolemy.data.expr.Parameter \
	    $attribute]  getToken]] doubleValue]
}


########################################################
#### _testSetToken
# Set the attribute to the token
#
proc _testSetToken {attribute token} {
    [java::cast ptolemy.data.expr.Parameter $attribute] setToken $token
}

