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
    set TypeNothingToUnknown [java::cast ptolemy.data.type.FunctionType [$r clone]]
    list [$r toString] [$TypeNothingToUnknown toString]
} {{(function() unknown)} {(function() unknown)}}

######################################################################
####
# 
test FunctionType-1.1 {Create a non-empty instance} {
    set nt [java::field ptolemy.data.type.BaseType INT]
    set vt [java::field ptolemy.data.type.BaseType INT]
    set v [java::new {ptolemy.data.type.Type[]} 2 [list $nt $vt]]

    set r [java::new {ptolemy.data.type.FunctionType} $v $vt]
    set TypeIntIntToInt [java::cast ptolemy.data.type.FunctionType [$r clone]]
    list [$r toString] [$TypeIntIntToInt toString]
} {{(function(a0:int, a1:int) int)} {(function(a0:int, a1:int) int)}}

######################################################################
####
# 
test FunctionType-1.2 {Create an instance with String, double args} {
    set nt [java::field ptolemy.data.type.BaseType STRING]
    set vt [java::field ptolemy.data.type.BaseType DOUBLE]
    set v [java::new {ptolemy.data.type.Type[]} 2 [list $nt $vt]]

    set r [java::new {ptolemy.data.type.FunctionType} $v $vt]
    set TypeStringDoubleToDouble [java::cast ptolemy.data.type.FunctionType [$r clone]]
    list [$r toString] [$TypeStringDoubleToDouble toString]
} {{(function(a0:string, a1:double) double)} {(function(a0:string, a1:double) double)}}

######################################################################
####
# 
test FunctionType-1.2 {Create an instance with String, double args, int return} {
    set nt [java::field ptolemy.data.type.BaseType STRING]
    set vt [java::field ptolemy.data.type.BaseType DOUBLE]
    set it [java::field ptolemy.data.type.BaseType INT]
    set v [java::new {ptolemy.data.type.Type[]} 2 [list $nt $vt]]

    set r [java::new {ptolemy.data.type.FunctionType} $v $it]
    set TypeStringDoubleToInt [java::cast ptolemy.data.type.FunctionType [$r clone]]
    list [$r toString] [$TypeStringDoubleToInt toString]
} {{(function(a0:string, a1:double) int)} {(function(a0:string, a1:double) int)}}

######################################################################
####
# 
test FunctionType-1.3 {Create an instance with an UNKNOWN arg} {
    set nt [java::field ptolemy.data.type.BaseType STRING]
    set vt [java::field ptolemy.data.type.BaseType UNKNOWN]
    set v [java::new {ptolemy.data.type.Type[]} 2 [list $nt $vt]]

    set r [java::new {ptolemy.data.type.FunctionType} $v $vt]
    set TypeStringUnknownToUnknown [java::cast ptolemy.data.type.FunctionType [$r clone]]
    list [$r toString] [$TypeStringUnknownToUnknown toString]
} {{(function(a0:string, a1:unknown) unknown)} {(function(a0:string, a1:unknown) unknown)}}

######################################################################
####
# 
test FunctionType-2.0 {Test convert} {
    set r1 [java::new {ptolemy.data.FunctionToken} "function(x,y,z) x+y+z"]

    list [[$TypeNothingToUnknown convert $r1] toString] [[$TypeIntIntToInt convert $r1] toString] \
         [[$TypeStringDoubleToDouble convert $r1] toString]
} {{(function(x, y, z) (x+y+z))} {(function(x, y, z) (x+y+z))} {(function(x, y, z) (x+y+z))}} {Yeah, We haven't written the conversion yet.}

######################################################################
####
# 
test FunctionType-3.0 {Test get} {
    list [$TypeNothingToUnknown getArgType 0] [$TypeNothingToUnknown getArgType 1]
} {java0x0 java0x0}

######################################################################
####
# 
test FunctionType-3.1 {Test get} {
    list [[$TypeIntIntToInt getArgType 0] toString] [[$TypeIntIntToInt getArgType 1] toString]
} {int int}

######################################################################
####
# 
test FunctionType-3.2 {Test get} {
    list [[$TypeStringDoubleToDouble getArgType 0] toString] [[$TypeStringDoubleToDouble getArgType 1] toString]
} {string double}

######################################################################
####
# 
test FunctionType-4.0 {Test isCompatible} {
    set r1 [java::new {ptolemy.data.FunctionToken} "function(x,y,z) x+y+z"]
    
    list [$TypeNothingToUnknown isCompatible [$r1 getType]] \
         [$TypeIntIntToInt isCompatible [$r1 getType]] \
         [$TypeStringDoubleToDouble isCompatible [$r1 getType]]
} {0 0 0}

######################################################################
####
# 
test FunctionType-4.1 {Test isCompatible} {
    set r1 [java::new {ptolemy.data.FunctionToken} "function(x:int,y:int) x+y"]

    list [$TypeNothingToUnknown isCompatible [$r1 getType]] \
         [$TypeIntIntToInt isCompatible [$r1 getType]] \
         [$TypeStringDoubleToDouble isCompatible [$r1 getType]]
} {0 1 0}

######################################################################
####
# 
test FunctionType-4.2 {Test isCompatible} {
    set r1 [java::new {ptolemy.data.FunctionToken} "function(x:string,y:double) 3"]

    list [$TypeNothingToUnknown isCompatible [$r1 getType]] \
         [$TypeIntIntToInt isCompatible [$r1 getType]] \
         [$TypeStringDoubleToDouble isCompatible [$r1 getType]]
} {0 1 1}

######################################################################
####
# 
test FunctionType-5.0 {Test isConstant} {
    list [$TypeNothingToUnknown isConstant] [$TypeIntIntToInt isConstant] [$TypeStringDoubleToDouble isConstant]
} {1 1 1}

######################################################################
####
# 
test FunctionType-5.1 {Test equals} {
    list [$TypeNothingToUnknown equals $TypeIntIntToInt] [$TypeIntIntToInt equals $TypeStringDoubleToDouble] \
         [$TypeStringDoubleToDouble equals $TypeNothingToUnknown]
} {0 0 0}

######################################################################
####
# 
test FunctionType-5.2 {Test equals} {
    set t [java::new {ptolemy.data.type.Type[]} {0} {}]
    set vt [java::field ptolemy.data.type.BaseType UNKNOWN]

    set r [java::new {ptolemy.data.type.FunctionType} $t $vt]
    $TypeNothingToUnknown equals $r
} {1}

######################################################################
####
# 
test FunctionType-5.3 {Test equals} {
    set nt [java::field ptolemy.data.type.BaseType STRING]
    set vt [java::field ptolemy.data.type.BaseType DOUBLE]
    set v [java::new {ptolemy.data.type.Type[]} 2 [list $nt $vt]]

    set r [java::new {ptolemy.data.type.FunctionType} $v $vt]
    $r equals $TypeIntIntToInt
} {0}

######################################################################
####
# 
test FunctionType-5.4 {Test equals} {
    set nt [java::field ptolemy.data.type.BaseType STRING]
    set vt [java::field ptolemy.data.type.BaseType DOUBLE]
    set v [java::new {ptolemy.data.type.Type[]} 2 [list $nt $vt]]

    set r [java::new {ptolemy.data.type.FunctionType} $v $vt]
    $TypeStringDoubleToDouble equals $r
} {1}

######################################################################
####
# 
test FunctionType-6.0 {Test isInstantiable} {
    list [$TypeNothingToUnknown isInstantiable] [$TypeIntIntToInt isInstantiable] [$TypeStringUnknownToUnknown isInstantiable]
} {1 1 0}

######################################################################
####
# 
test FunctionType-7.0 {Test isSubstitutionInstance} {
    list [$TypeNothingToUnknown isSubstitutionInstance $TypeIntIntToInt] \
	[$TypeIntIntToInt isSubstitutionInstance $TypeNothingToUnknown] \
	[$TypeIntIntToInt isSubstitutionInstance $TypeStringDoubleToDouble] \
	[$TypeStringDoubleToDouble isSubstitutionInstance $TypeIntIntToInt] \
	[$TypeStringDoubleToDouble isSubstitutionInstance $TypeStringDoubleToInt] \
	[$TypeStringDoubleToInt isSubstitutionInstance $TypeStringDoubleToDouble] \
	[$TypeStringDoubleToInt isSubstitutionInstance $TypeIntIntToInt] \
	[$TypeIntIntToInt isSubstitutionInstance $TypeStringDoubleToInt] \
	[$TypeStringDoubleToDouble isSubstitutionInstance $TypeNothingToUnknown] \
	[$TypeStringDoubleToDouble isSubstitutionInstance $TypeStringDoubleToDouble] \
	[$TypeStringDoubleToDouble isSubstitutionInstance $TypeStringUnknownToUnknown] \
	[$TypeStringUnknownToUnknown isSubstitutionInstance $TypeStringDoubleToDouble] \
	
} {0 0 0 0 0 0 0 0 0 1 0 1}

######################################################################
####
# 
test FunctionType-8.0 {Test initialize} {
    set unknown [java::field ptolemy.data.type.BaseType UNKNOWN]
    $TypeNothingToUnknown initialize $unknown
    $TypeNothingToUnknown toString
} {(function() unknown)}

######################################################################
####
# 
test FunctionType-8.1 {Test initialize} {
    set unknown [java::field ptolemy.data.type.BaseType UNKNOWN]
    $TypeStringDoubleToDouble initialize $unknown
    $TypeStringDoubleToDouble toString
} {(function(a0:string, a1:double) double)}

######################################################################
####
# 
test FunctionType-8.2 {Test initialize} {
    set unknown [java::field ptolemy.data.type.BaseType UNKNOWN]
    $TypeStringUnknownToUnknown initialize $unknown
    $TypeStringUnknownToUnknown toString
} {(function(a0:string, a1:unknown) unknown)}

######################################################################
####
# 
test FunctionType-9.0 {Test updateType} {
    catch {$TypeNothingToUnknown updateType $TypeStringDoubleToDouble} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: FunctionType.updateType: This type is a constant and the argument is not the same as this type. This type: (function() unknown) argument: (function(a0:string, a1:double) double)}}

######################################################################
####
# 
test FunctionType-9.1 {Test updateType} {
    catch {$TypeStringDoubleToDouble updateType $TypeNothingToUnknown} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: FunctionType.updateType: This type is a constant and the argument is not the same as this type. This type: (function(a0:string, a1:double) double) argument: (function() unknown)}}

######################################################################
####
# 
test FunctionType-9.2 {Test updateType} {
    catch {$TypeStringDoubleToDouble updateType $TypeStringUnknownToUnknown} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: FunctionType.updateType: This type is a constant and the argument is not the same as this type. This type: (function(a0:string, a1:double) double) argument: (function(a0:string, a1:unknown) unknown)}}

######################################################################
####
# 
test FunctionType-9.3 {Test updateType} {
    $TypeStringUnknownToUnknown updateType $TypeStringDoubleToDouble
    $TypeStringUnknownToUnknown toString
} {(function(a0:string, a1:double) double)}

