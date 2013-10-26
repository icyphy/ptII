# Tests for the UnionType class
#
# @Author: Christopher Brooks, based on RecordType.tcl by Yuhong Xiong, Elaine Cheong and Steve Neuendorffer
#
# @Version $Id$
#
# @Copyright (c) 2012-2013 The Regents of the University of California.
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

# 
#

######################################################################
####
# 
test UnionType-1.0 {Create an empty instance} {
    set l [java::new {String[]} {0} {}]
    set t [java::new {ptolemy.data.type.Type[]} {0} {}]

    set r [java::new {ptolemy.data.type.UnionType} $l $t]
    set emptyUnionType [java::cast ptolemy.data.type.UnionType [$r clone]]
    list [$r toString] [$emptyUnionType toString]
} {{{||}} {{||}}}

######################################################################
####
# 
test UnionType-1.1 {Create a non-empty instance} {
    set l [java::new {String[]} {2} {{name} {value}}]

    set nt [java::field ptolemy.data.type.BaseType STRING]
    set vt [java::field ptolemy.data.type.BaseType DOUBLE]
    set v [java::new {ptolemy.data.type.Type[]} 2 [list $nt $vt]]

    set r [java::new {ptolemy.data.type.UnionType} $l $v]
    set sdUnionType [java::cast ptolemy.data.type.UnionType [$r clone]]
    list [$r toString] [$sdUnionType toString]
} {{{|name = string, value = double|}} {{|name = string, value = double|}}}

######################################################################
####
# 
test UnionType-1.2 {Create an instance with an UNKNOWN field} {
    set l [java::new {String[]} {2} {{name} {value}}]

    set nt [java::field ptolemy.data.type.BaseType STRING]
    set vt [java::field ptolemy.data.type.BaseType UNKNOWN]
    set v [java::new {ptolemy.data.type.Type[]} 2 [list $nt $vt]]

    set r [java::new {ptolemy.data.type.UnionType} $l $v]
    set varUnionType [java::cast ptolemy.data.type.UnionType [$r clone]]
    list [$r toString] [$varUnionType toString]
} {{{|name = string, value = unknown|}} {{|name = string, value = unknown|}}}

######################################################################
####
# NOTE: I changed the following test so that lossless conversions are
# performed rather than lossy ones.  EAL 6/19/08.
test UnionType-2.0 {Test convert} {
    set r1 [java::new {ptolemy.data.UnionToken} {{|name = "foo", value = 1, extra = 2.5|}}]

    catch {$emptyUnionType convert $r1} errMsg

    list \
	[[$sdUnionType convert $r1] toString] "\n" \
        [[[$r1 getType] convert $r1] toString] "\n" \
	$errMsg
} {{{|name = "foo"|}} {
} {{|name = "foo"|}} {
} {java.lang.IllegalArgumentException: Conversion is not supported from ptolemy.data.UnionToken '{|name = "foo"|}' to the type {||}.}}

######################################################################
####
# NOTE: I changed the following test so that lossless conversions are
# performed rather than lossy ones.  EAL 6/19/08.
test UnionType-2.0.5 {Test convert: Converting from something with more records to less records fails} {
    set r5 [java::new {ptolemy.data.UnionToken} {{|name = "foo", value = 1|}}]

    catch {[[$emptyUnionType convert $r5] toString]} errMsg

    # 
    list \
        [[$sdUnionType convert $r5] toString] "\n" \
	[[[$r1 getType] convert $r5] toString] "\n" \
        [[[$r5 getType] convert $r5] toString] "\n" \
        [[[$r5 getType] convert $r1] toString] "\n" \
	$errMsg
} {{{|name = "foo"|}} {
} {{|name = "foo"|}} {
} {{|name = "foo"|}} {
} {{|name = "foo"|}} {
} {java.lang.IllegalArgumentException: Conversion is not supported from ptolemy.data.UnionToken '{|name = "foo"|}' to the type {||}.}}



######################################################################
####
# 
test UnionType-3.0 {Test get} {
    list [$emptyUnionType get name] [$emptyUnionType get value]
} {java0x0 java0x0}

######################################################################
####
# 
test UnionType-3.1 {Test get} {
    list [[$sdUnionType get name] toString] [[$sdUnionType get value] toString]
} {string double}

######################################################################
####
# 
test UnionType-3.2 {Test get} {
    list [[$varUnionType get name] toString] [[$varUnionType get value] toString]
} {string unknown}

######################################################################
####
# 
test UnionType-4.0 {Test isCompatible} {
    set r1 [java::new {ptolemy.data.UnionToken} {{|name = "foo", value = 1, extra = 2.5|}}]

    list [$emptyUnionType isCompatible [$r1 getType]] \
         [$sdUnionType isCompatible [$r1 getType]] \
         [$varUnionType isCompatible [$r1 getType]]
} {0 1 1}

######################################################################
####
# 
test UnionType-4.1 {Test isCompatible} {
    set r1 [java::new {ptolemy.data.UnionToken} {{|name = "foo", value = "bar", extra = 2.5|}}]

    list [$emptyUnionType isCompatible [$r1 getType]] \
         [$sdUnionType isCompatible [$r1 getType]] \
         [$varUnionType isCompatible [$r1 getType]]
} {0 1 1}

######################################################################
####
# 
test UnionType-5.0 {Test isConstant} {
    list [$emptyUnionType isConstant] [$sdUnionType isConstant] [$varUnionType isConstant]
} {1 1 0}

######################################################################
####
# 
test UnionType-5.1 {Test equals} {
    list [$emptyUnionType equals $sdUnionType] [$sdUnionType equals $varUnionType] \
         [$varUnionType equals $emptyUnionType]
} {0 0 0}

######################################################################
####
# 
test UnionType-5.1 {Test equals} {
    set l [java::new {String[]} {0} {}]
    set t [java::new {ptolemy.data.type.Type[]} {0} {}]

    set r [java::new {ptolemy.data.type.UnionType} $l $t]
    $emptyUnionType equals $r
} {1}

######################################################################
####
# 
test UnionType-5.2 {Test equals} {
    set l [java::new {String[]} {2} {{name} {value}}]

    set nt [java::field ptolemy.data.type.BaseType STRING]
    set vt [java::field ptolemy.data.type.BaseType DOUBLE]
    set v [java::new {ptolemy.data.type.Type[]} 2 [list $nt $vt]]

    set r [java::new {ptolemy.data.type.UnionType} $l $v]
    $r equals $sdUnionType
} {1}

######################################################################
####
# 
test UnionType-5.3 {Test equals} {
    set l [java::new {String[]} {2} {{name} {value}}]

    set nt [java::field ptolemy.data.type.BaseType STRING]
    set vt [java::field ptolemy.data.type.BaseType UNKNOWN]
    set v [java::new {ptolemy.data.type.Type[]} 2 [list $nt $vt]]

    set r [java::new {ptolemy.data.type.UnionType} $l $v]
    $varUnionType equals $r
} {1}

######################################################################
####
# 
test UnionType-6.0 {Test isInstantiable} {
    list [$emptyUnionType isInstantiable] [$sdUnionType isInstantiable] [$varUnionType isInstantiable]
} {1 1 0}

######################################################################
####
# 
test UnionType-7.0 {Test isSubstitutionInstance} {
    list [$emptyUnionType isSubstitutionInstance $sdUnionType] \
         [$sdUnionType isSubstitutionInstance $emptyUnionType] \
         [$sdUnionType isSubstitutionInstance $varUnionType] \
         [$varUnionType isSubstitutionInstance $sdUnionType] \
         [$varUnionType isSubstitutionInstance $emptyUnionType] \
         [$emptyUnionType isSubstitutionInstance $varUnionType]
} {0 0 0 1 0 0}

######################################################################
####
# 
test UnionType-8.0 {Test initialize} {
    set unknown [java::field ptolemy.data.type.BaseType UNKNOWN]
    $emptyUnionType initialize $unknown
    $emptyUnionType toString
} {{||}}

######################################################################
####
# 
test UnionType-8.1 {Test initialize} {
    set unknown [java::field ptolemy.data.type.BaseType UNKNOWN]
    $sdUnionType initialize $unknown
    $sdUnionType toString
} {{|name = string, value = double|}}

######################################################################
####
# 
test UnionType-8.2 {Test initialize} {
    set unknown [java::field ptolemy.data.type.BaseType UNKNOWN]
    $varUnionType initialize $unknown
    $varUnionType toString
} {{|name = string, value = unknown|}}

######################################################################
####
# 
test UnionType-9.0 {Test updateType} {
    catch {$emptyUnionType updateType $sdUnionType} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: UnionType.updateType: This type is a constant and the argument is not the same as this type. This type: {||} argument: {|name = string, value = double|}}}

######################################################################
####
# 
test UnionType-9.1 {Test updateType} {
    catch {$sdUnionType updateType $emptyUnionType} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: UnionType.updateType: This type is a constant and the argument is not the same as this type. This type: {|name = string, value = double|} argument: {||}}}

######################################################################
####
# 
test UnionType-9.2 {Test updateType} {
    catch {$sdUnionType updateType $varUnionType} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: UnionType.updateType: This type is a constant and the argument is not the same as this type. This type: {|name = string, value = double|} argument: {|name = string, value = unknown|}}}

######################################################################
####
# 
test UnionType-9.3 {Test updateType} {
    $varUnionType updateType $sdUnionType
    $varUnionType toString
} {{|name = string, value = double|}}

test UnionType-10.3 {Test leastUpperBound} {
    # Test for r63712.
    # "Fixed a very subtle bug where _leastUpperBound did not return
    # the right result if the types being compared were, for example,
    # {|x=unknown|} and {|x=int|}. Previously, this returned
    # {|x=unknown|}. But the LUB is {|x=int|}."

    set labels [java::new {String[]} {1} {{x}}]
    set unknownType [java::field ptolemy.data.type.BaseType UNKNOWN]
    set types [java::new {ptolemy.data.type.Type[]} 1 [list $unknownType]]

    set xUnknown [java::new {ptolemy.data.type.UnionType} $labels $types]

    set labels [java::new {String[]} {1} {{x}}]
    set intType [java::field ptolemy.data.type.BaseType INT]
    set types [java::new {ptolemy.data.type.Type[]} 1 [list $intType]]

    set xInt [java::new {ptolemy.data.type.UnionType} $labels $types]

    list \
	[$xUnknown toString] "\n" \
	[$xInt toString] "\n" \
	[[java::call ptolemy.data.type.TypeLattice leastUpperBound $xUnknown $xInt] toString]]
} {{{|x = unknown|}} {
} {{|x = int|}} {
} {{|x = int|}]}}

