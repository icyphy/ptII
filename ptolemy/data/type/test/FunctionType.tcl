# Tests for the FunctionType class
#
# @Author: Steve Neuendorffer
#
# @Version $Id$
#
# @Copyright (c) 1997-2003 The Regents of the University of California.
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

# If a file contains non-graphical tests, then it should be named .tcl
# If a file contains graphical tests, then it should be called .itcl
# 
# It would be nice if the tests would work in a vanilla itkwish binary.
# Check for necessary classes and adjust the auto_path accordingly.
#

######################################################################
####
# 
test FunctionType-1.0 {Create an empty instance} {
    set t [java::new {ptolemy.data.type.Type[]} {0} {}]
    set vt [java::field ptolemy.data.type.BaseType UNKNOWN]
 
    set r [java::new {ptolemy.data.type.FunctionType} $t $vt]
    set empRT [java::cast ptolemy.data.type.FunctionType [$r clone]]
    list [$r toString] [$empRT toString]
} {{function() unknown} {function() unknown}}

######################################################################
####
# 
test FunctionType-1.1 {Create a non-empty instance} {
    set nt [java::field ptolemy.data.type.BaseType STRING]
    set vt [java::field ptolemy.data.type.BaseType DOUBLE]
    set v [java::new {ptolemy.data.type.Type[]} 2 [list $nt $vt]]

    set r [java::new {ptolemy.data.type.FunctionType} $v $vt]
    set sdRT [java::cast ptolemy.data.type.FunctionType [$r clone]]
    list [$r toString] [$sdRT toString]
} {{function(a0:string, a1:double) double} {function(a0:string, a1:double) double}}

######################################################################
####
# 
test FunctionType-1.2 {Create an instance with an UNKNOWN field} {
    set nt [java::field ptolemy.data.type.BaseType STRING]
    set vt [java::field ptolemy.data.type.BaseType UNKNOWN]
    set v [java::new {ptolemy.data.type.Type[]} 2 [list $nt $vt]]

    set r [java::new {ptolemy.data.type.FunctionType} $v $vt]
    set varRT [java::cast ptolemy.data.type.FunctionType [$r clone]]
    list [$r toString] [$varRT toString]
} {{function(a0:string, a1:unknown) unknown} {function(a0:string, a1:unknown) unknown}}

######################################################################
####
# 
test FunctionType-2.0 {Test convert} {
    set r1 [java::new {ptolemy.data.FunctionToken} "function(x,y,z) x+y+z"]

    list [[$empRT convert $r1] toString] [[$sdRT convert $r1] toString] \
         [[$varRT convert $r1] toString]
} {{(function(x, y, z) (x+y+z))} {(function(x, y, z) (x+y+z))} {(function(x, y, z) (x+y+z))}}

######################################################################
####
# 
test FunctionType-3.0 {Test get} {
    list [$empRT getArgType 0] [$empRT getArgType 1]
} {java0x0 java0x0}

######################################################################
####
# 
test FunctionType-3.1 {Test get} {
    list [[$sdRT getArgType 0] toString] [[$sdRT getArgType 1] toString]
} {string double}

######################################################################
####
# 
test FunctionType-3.2 {Test get} {
    list [[$varRT getArgType 0] toString] [[$varRT getArgType 1] toString]
} {string unknown}

######################################################################
####
# 
test FunctionType-4.0 {Test isCompatible} {
    set r1 [java::new {ptolemy.data.FunctionToken} "function(x,y,z) x+y+z"]

    list [$empRT isCompatible [$r1 getType]] \
         [$sdRT isCompatible [$r1 getType]] \
         [$varRT isCompatible [$r1 getType]]
} {1 1 1}

######################################################################
####
# 
test FunctionType-4.1 {Test isCompatible} {
    set r1 [java::new {ptolemy.data.FunctionToken} "function(x,y,z) x+y+z"]

    list [$empRT isCompatible [$r1 getType]] \
         [$sdRT isCompatible [$r1 getType]] \
         [$varRT isCompatible [$r1 getType]]
} {1 0 1}

######################################################################
####
# 
test FunctionType-5.0 {Test isConstant} {
    list [$empRT isConstant] [$sdRT isConstant] [$varRT isConstant]
} {1 1 0}

######################################################################
####
# 
test FunctionType-5.1 {Test equals} {
    list [$empRT equals $sdRT] [$sdRT equals $varRT] \
         [$varRT equals $empRT]
} {0 0 0}

######################################################################
####
# 
test FunctionType-5.2 {Test equals} {
    set t [java::new {ptolemy.data.type.Type[]} {0} {}]
    set vt [java::field ptolemy.data.type.BaseType UNKNOWN]

    set r [java::new {ptolemy.data.type.FunctionType} $t $vt]
    $empRT equals $r
} {1}

######################################################################
####
# 
test FunctionType-5.3 {Test equals} {
    set nt [java::field ptolemy.data.type.BaseType STRING]
    set vt [java::field ptolemy.data.type.BaseType DOUBLE]
    set v [java::new {ptolemy.data.type.Type[]} 2 [list $nt $vt]]

    set r [java::new {ptolemy.data.type.FunctionType} $v $vt]
    $r equals $sdRT
} {1}

######################################################################
####
# 
test FunctionType-5.4 {Test equals} {
    set nt [java::field ptolemy.data.type.BaseType STRING]
    set vt [java::field ptolemy.data.type.BaseType UNKNOWN]
    set v [java::new {ptolemy.data.type.Type[]} 2 [list $nt $vt]]

    set r [java::new {ptolemy.data.type.FunctionType} $v $vt]
    $varRT equals $r
} {1}

######################################################################
####
# 
test FunctionType-6.0 {Test isInstantiable} {
    list [$empRT isInstantiable] [$sdRT isInstantiable] [$varRT isInstantiable]
} {1 1 0}

######################################################################
####
# 
test FunctionType-7.0 {Test isSubstitutionInstance} {
    list [$empRT isSubstitutionInstance $sdRT] \
         [$sdRT isSubstitutionInstance $empRT] \
         [$sdRT isSubstitutionInstance $varRT] \
         [$varRT isSubstitutionInstance $sdRT] \
         [$varRT isSubstitutionInstance $empRT] \
         [$empRT isSubstitutionInstance $varRT]
} {0 0 0 1 0 0}

######################################################################
####
# 
test FunctionType-8.0 {Test initialize} {
    set unknown [java::field ptolemy.data.type.BaseType UNKNOWN]
    $empRT initialize $unknown
    $empRT toString
} {function() unknown}

######################################################################
####
# 
test FunctionType-8.1 {Test initialize} {
    set unknown [java::field ptolemy.data.type.BaseType UNKNOWN]
    $sdRT initialize $unknown
    $sdRT toString
} {function(a0:string, a1:double) double}

######################################################################
####
# 
test FunctionType-8.2 {Test initialize} {
    set unknown [java::field ptolemy.data.type.BaseType UNKNOWN]
    $varRT initialize $unknown
    $varRT toString
} {function(a0:string, a1:unknown) unknown}

######################################################################
####
# 
test FunctionType-9.0 {Test updateType} {
    catch {$empRT updateType $sdRT} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: FunctionType.updateType: This type is a constant and the argument is not the same as this type. This type: function() unknown argument: function(a0:string, a1:double) double}}

######################################################################
####
# 
test FunctionType-9.1 {Test updateType} {
    catch {$sdRT updateType $empRT} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: FunctionType.updateType: This type is a constant and the argument is not the same as this type. This type: function(a0:string, a1:double) double argument: function() unknown}}

######################################################################
####
# 
test FunctionType-9.2 {Test updateType} {
    catch {$sdRT updateType $varRT} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: FunctionType.updateType: This type is a constant and the argument is not the same as this type. This type: (string, double) -> double argument: (string, unknown) -> unknown}}

######################################################################
####
# 
test FunctionType-9.3 {Test updateType} {
    $varRT updateType $sdRT
    $varRT toString
} {function(a0:string, a1:double) double}

