# Tests for the RecordType class
#
# @Author: Yuhong Xiong, Elaine Cheong and Steve Neuendorffer
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
test RecordType-1.0 {Create an empty instance} {
    set l [java::new {String[]} {0} {}]
    set t [java::new {ptolemy.data.type.Type[]} {0} {}]

    set r [java::new {ptolemy.data.type.RecordType} $l $t]
    set empRT [java::cast ptolemy.data.type.RecordType [$r clone]]
    list [$r toString] [$empRT toString]
} {{{}} {{}}}

######################################################################
####
# 
test RecordType-1.1 {Create a non-empty instance} {
    set l [java::new {String[]} {2} {{name} {value}}]

    set nt [java::field ptolemy.data.type.BaseType STRING]
    set vt [java::field ptolemy.data.type.BaseType DOUBLE]
    set v [java::new {ptolemy.data.type.Type[]} 2 [list $nt $vt]]

    set r [java::new {ptolemy.data.type.RecordType} $l $v]
    set sdRT [java::cast ptolemy.data.type.RecordType [$r clone]]
    list [$r toString] [$sdRT toString]
} {{{name = string, value = double}} {{name = string, value = double}}}

######################################################################
####
# 
test RecordType-1.2 {Create an instance with an UNKNOWN field} {
    set l [java::new {String[]} {2} {{name} {value}}]

    set nt [java::field ptolemy.data.type.BaseType STRING]
    set vt [java::field ptolemy.data.type.BaseType UNKNOWN]
    set v [java::new {ptolemy.data.type.Type[]} 2 [list $nt $vt]]

    set r [java::new {ptolemy.data.type.RecordType} $l $v]
    set varRT [java::cast ptolemy.data.type.RecordType [$r clone]]
    list [$r toString] [$varRT toString]
} {{{name = string, value = unknown}} {{name = string, value = unknown}}}

######################################################################
####
# 
test RecordType-2.0 {Test convert} {
    # token is {name = "foo", value = 1, extra = 2.5}
    set l1 [java::new {String[]} {3} {{name} {value} {extra}}]

    set nt1 [java::new {ptolemy.data.StringToken String} foo]
    set vt1 [java::new {ptolemy.data.IntToken int} 1]
    set et1 [java::new {ptolemy.data.DoubleToken double} 2.5]
    set v1 [java::new {ptolemy.data.Token[]} 3 [list $nt1 $vt1 $et1]]

    set r1 [java::new {ptolemy.data.RecordToken} $l1 $v1]

    list [[$empRT convert $r1] toString] [[$sdRT convert $r1] toString] \
        [[[$r1 getType] convert $r1] toString]
} {{{}} {{name = "foo", value = 1.0}} {{extra = 2.5, name = "foo", value = 1}}}

######################################################################
####
# 
test RecordType-3.0 {Test get} {
    list [$empRT get name] [$empRT get value]
} {java0x0 java0x0}

######################################################################
####
# 
test RecordType-3.1 {Test get} {
    list [[$sdRT get name] toString] [[$sdRT get value] toString]
} {string double}

######################################################################
####
# 
test RecordType-3.2 {Test get} {
    list [[$varRT get name] toString] [[$varRT get value] toString]
} {string unknown}

######################################################################
####
# 
test RecordType-4.0 {Test isCompatible} {
    # token is {name = "foo", value = 1, extra = 2.5}
    set l1 [java::new {String[]} {3} {{name} {value} {extra}}]

    set nt1 [java::new {ptolemy.data.StringToken String} foo]
    set vt1 [java::new {ptolemy.data.IntToken int} 1]
    set et1 [java::new {ptolemy.data.DoubleToken double} 2.5]
    set v1 [java::new {ptolemy.data.Token[]} 3 [list $nt1 $vt1 $et1]]

    set r1 [java::new {ptolemy.data.RecordToken} $l1 $v1]

    list [$empRT isCompatible [$r1 getType]] \
         [$sdRT isCompatible [$r1 getType]] \
         [$varRT isCompatible [$r1 getType]]
} {1 1 1}

######################################################################
####
# 
test RecordType-4.1 {Test isCompatible} {
    # token is {name = "foo", value = "bar", extra = 2.5}
    set l1 [java::new {String[]} {3} {{name} {value} {extra}}]

    set nt1 [java::new {ptolemy.data.StringToken String} foo]
    set vt1 [java::new {ptolemy.data.StringToken String} bar]
    set et1 [java::new {ptolemy.data.DoubleToken double} 2.5]
    set v1 [java::new {ptolemy.data.Token[]} 3 [list $nt1 $vt1 $et1]]

    set r1 [java::new {ptolemy.data.RecordToken} $l1 $v1]

    list [$empRT isCompatible [$r1 getType]] \
         [$sdRT isCompatible [$r1 getType]] \
         [$varRT isCompatible [$r1 getType]]
} {1 0 1}

######################################################################
####
# 
test RecordType-5.0 {Test isConstant} {
    list [$empRT isConstant] [$sdRT isConstant] [$varRT isConstant]
} {1 1 0}

######################################################################
####
# 
test RecordType-5.1 {Test equals} {
    list [$empRT equals $sdRT] [$sdRT equals $varRT] \
         [$varRT equals $empRT]
} {0 0 0}

######################################################################
####
# 
test RecordType-5.1 {Test equals} {
    set l [java::new {String[]} {0} {}]
    set t [java::new {ptolemy.data.type.Type[]} {0} {}]

    set r [java::new {ptolemy.data.type.RecordType} $l $t]
    $empRT equals $r
} {1}

######################################################################
####
# 
test RecordType-5.2 {Test equals} {
    set l [java::new {String[]} {2} {{name} {value}}]

    set nt [java::field ptolemy.data.type.BaseType STRING]
    set vt [java::field ptolemy.data.type.BaseType DOUBLE]
    set v [java::new {ptolemy.data.type.Type[]} 2 [list $nt $vt]]

    set r [java::new {ptolemy.data.type.RecordType} $l $v]
    $r equals $sdRT
} {1}

######################################################################
####
# 
test RecordType-5.3 {Test equals} {
    set l [java::new {String[]} {2} {{name} {value}}]

    set nt [java::field ptolemy.data.type.BaseType STRING]
    set vt [java::field ptolemy.data.type.BaseType UNKNOWN]
    set v [java::new {ptolemy.data.type.Type[]} 2 [list $nt $vt]]

    set r [java::new {ptolemy.data.type.RecordType} $l $v]
    $varRT equals $r
} {1}

######################################################################
####
# 
test RecordType-6.0 {Test isInstantiable} {
    list [$empRT isInstantiable] [$sdRT isInstantiable] [$varRT isInstantiable]
} {1 1 0}

######################################################################
####
# 
test RecordType-7.0 {Test isSubstitutionInstance} {
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
test RecordType-8.0 {Test initialize} {
    set unknown [java::field ptolemy.data.type.BaseType UNKNOWN]
    $empRT initialize $unknown
    $empRT toString
} {{}}

######################################################################
####
# 
test RecordType-8.1 {Test initialize} {
    set unknown [java::field ptolemy.data.type.BaseType UNKNOWN]
    $sdRT initialize $unknown
    $sdRT toString
} {{name = string, value = double}}

######################################################################
####
# 
test RecordType-8.2 {Test initialize} {
    set unknown [java::field ptolemy.data.type.BaseType UNKNOWN]
    $varRT initialize $unknown
    $varRT toString
} {{name = string, value = unknown}}

######################################################################
####
# 
test RecordType-9.0 {Test updateType} {
    catch {$empRT updateType $sdRT} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: RecordType.updateType: This type is a constant and the argument is not the same as this type. This type: {} argument: {name = string, value = double}}}

######################################################################
####
# 
test RecordType-9.1 {Test updateType} {
    catch {$sdRT updateType $empRT} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: RecordType.updateType: This type is a constant and the argument is not the same as this type. This type: {name = string, value = double} argument: {}}}

######################################################################
####
# 
test RecordType-9.2 {Test updateType} {
    catch {$sdRT updateType $varRT} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: RecordType.updateType: This type is a constant and the argument is not the same as this type. This type: {name = string, value = double} argument: {name = string, value = unknown}}}

######################################################################
####
# 
test RecordType-9.3 {Test updateType} {
    $varRT updateType $sdRT
    $varRT toString
} {{name = string, value = double}}

