# Tests for the RecordToken class
#
# @Author: Yuhong Xiong and Elaine Cheong
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
test RecordToken-1.0 {Create an empty instance} {
    set l [java::new {String[]} {0} {}]
    set v [java::new {ptolemy.data.Token[]} {0} {}]

    set r [java::new {ptolemy.data.RecordToken} $l $v]
    $r toString
} {{}}

######################################################################
####
# 
test RecordToken-1.1 {Create a non-empty instance} {
    set l [java::new {String[]} {2} {{name} {value}}]

    set nt [java::new {ptolemy.data.StringToken String} foo]
    set vt [java::new {ptolemy.data.IntToken int} 5]
    set v [java::new {ptolemy.data.Token[]} 2 [list $nt $vt]]

    set r [java::new {ptolemy.data.RecordToken} $l $v]
    $r toString
} {{name="foo", value=5}}

######################################################################
####
# This test has been commented out because "{}" could either be an
# empty record or an empty array.
# 
# test RecordToken-1.2 {Create an empty instance from string} {
#     set r [java::new {ptolemy.data.RecordToken String} "{}"]
#     $r toString
# } {{}} {This is not possible because of ambiguities in the Expression language}

#######################################################################
####
# 
test RecordToken-1.3 {Create a non-empty instance from string} {
    set r [java::new {ptolemy.data.RecordToken String} "{name=\"bar\", value=6}"]
    list [$r toString] [$r length]
} {{{name="bar", value=6}} 2}

######################################################################
####
# 
test RecordToken-add.0 {Test add} {
    # first record is {name="foo", value=1, extra1=2}
    set l1 [java::new {String[]} {3} {{name} {value} {extra1}}]

    set nt1 [java::new {ptolemy.data.StringToken String} foo]
    set vt1 [java::new {ptolemy.data.IntToken int} 1]
    set et1 [java::new {ptolemy.data.IntToken int} 2]
    set v1 [java::new {ptolemy.data.Token[]} 3 [list $nt1 $vt1 $et1]]

    set r1 [java::new {ptolemy.data.RecordToken} $l1 $v1]

    # second record is {name="bar", extra2=8.5, value=5.5}
    set l2 [java::new {String[]} {3} {{name} {extra2} {value}}]

    set nt2 [java::new {ptolemy.data.StringToken String} bar]
    set et2 [java::new {ptolemy.data.DoubleToken double} 8.5]
    set vt2 [java::new {ptolemy.data.DoubleToken double} 5.5]
    set v2 [java::new {ptolemy.data.Token[]} 3 [list $nt2 $et2 $vt2]]

    set r2 [java::new {ptolemy.data.RecordToken} $l2 $v2]

    [$r1 add $r2] toString
} {{name="foobar", value=6.5}}

######################################################################
####
# 
test RecordToken-add.1 {Test adding with empty record} {
    # first record is empty
    set l [java::new {String[]} {0} {}]
    set v [java::new {ptolemy.data.Token[]} {0} {}]
    set r [java::new {ptolemy.data.RecordToken} $l $v]

    # second record is {name="foo", value=1, extra1=2}
    set l1 [java::new {String[]} {3} {{name} {value} {extra1}}]

    set nt1 [java::new {ptolemy.data.StringToken String} foo]
    set vt1 [java::new {ptolemy.data.IntToken int} 1]
    set et1 [java::new {ptolemy.data.IntToken int} 2]
    set v1 [java::new {ptolemy.data.Token[]} 3 [list $nt1 $vt1 $et1]]

    set r1 [java::new {ptolemy.data.RecordToken} $l1 $v1]

    [$r add $r1] toString
} {{}}

######################################################################
####
# 
test RecordToken-divide.0 {Test divide} {
    set r1 [java::new {ptolemy.data.RecordToken} {{value=2.0, extra1=2}}]

    set r2 [java::new {ptolemy.data.RecordToken} {{extra2=8.5, value=10.0}}]

    [$r2 divide $r1] toString
} {{value=5.0}}

######################################################################
####
# 
test RecordToken-equals.0 {test equals} {
    set l [java::new {String[]} {2} {{value1} {value2}}]
    set v1 [java::new {ptolemy.data.IntToken int} 5]
    set v2 [java::new {ptolemy.data.DoubleToken double} 3.5]
    set v [java::new {ptolemy.data.Token[]} 2 [list $v1 $v2]]
    set r1 [java::new {ptolemy.data.RecordToken} $l $v]

    set la [java::new {String[]} {2} {{value1} {value2}}]
    set v1a [java::new {ptolemy.data.IntToken int} 5]
    set v2a [java::new {ptolemy.data.DoubleToken double} 3.5]
    set va [java::new {ptolemy.data.Token[]} 2 [list $v1 $v2]]
    set r2 [java::new {ptolemy.data.RecordToken} $la $va]

    set lb [java::new {String[]} {2} {{value1} {value2}}]
    set v1b [java::new {ptolemy.data.IntToken int} 5]
    set v2b [java::new {ptolemy.data.DoubleToken double} 9.5]
    set vb [java::new {ptolemy.data.Token[]} 2 [list $v1b $v2b]]
    set r3 [java::new {ptolemy.data.RecordToken} $lb $vb]

    list [$r1 equals $r1] [$r1 equals $r2] [$r1 equals $r3]
} {1 1 0}

######################################################################
####
# 
test RecordToken-get.0 {Test get} {
    # record is empty
    set l [java::new {String[]} {0} {}]
    set v [java::new {ptolemy.data.Token[]} {0} {}]
    set r [java::new {ptolemy.data.RecordToken} $l $v]

    $r get foo
} {java0x0}

######################################################################
####
# 
test RecordToken-get.1 {Test get} {
    set l [java::new {String[]} {2} {{name} {value}}]

    set nt [java::new {ptolemy.data.StringToken String} foo]
    set vt [java::new {ptolemy.data.IntToken int} 5]
    set v [java::new {ptolemy.data.Token[]} 2 [list $nt $vt]]

    set r [java::new {ptolemy.data.RecordToken} $l $v]

    list [[$r get name] toString] [[$r get value] toString]
} {{"foo"} 5}

######################################################################
####
# 
test RecordToken-getType.0 {Test getType} {
    # record is empty
    set l [java::new {String[]} {0} {}]
    set v [java::new {ptolemy.data.Token[]} {0} {}]
    set r [java::new {ptolemy.data.RecordToken} $l $v]

    [$r getType] toString
} {{}}

######################################################################
####
# 
test RecordToken-getType.1 {Test getType} {
    set l [java::new {String[]} {2} {{name} {value}}]

    set nt [java::new {ptolemy.data.StringToken String} foo]
    set vt [java::new {ptolemy.data.IntToken int} 5]
    set v [java::new {ptolemy.data.Token[]} 2 [list $nt $vt]]

    set r [java::new {ptolemy.data.RecordToken} $l $v]

    [$r getType] toString
} {{name=string, value=int}}

######################################################################
####
# 
test RecordToken-hashcode.0 {test hashCode} {
    # use t1, t2, t3 above
    list [$r1 hashCode] [$r2 hashCode] [$r3 hashCode]
} {8 8 14}

######################################################################
####
# 
test RecordToken-isEqualTo.0 {Test isEqualTo and isCloseTo} {
    # record is empty
    set l1 [java::new {String[]} {0} {}]
    set v1 [java::new {ptolemy.data.Token[]} {0} {}]
    set r1 [java::new {ptolemy.data.RecordToken} $l1 $v1]

    # 2nd record is {name="foo", value=5}
    set l2 [java::new {String[]} {2} {{name} {value}}]

    set nt2 [java::new {ptolemy.data.StringToken String} foo]
    set vt2 [java::new {ptolemy.data.IntToken int} 5]
    set v2 [java::new {ptolemy.data.Token[]} 2 [list $nt2 $vt2]]

    set r2 [java::new {ptolemy.data.RecordToken} $l2 $v2]

    # 3rd record is the same as the 2nd: {name="foo", value=5}
    set l3 [java::new {String[]} {2} {{name} {value}}]

    set nt3 [java::new {ptolemy.data.StringToken String} foo]
    set vt3 [java::new {ptolemy.data.IntToken int} 5]
    set v3 [java::new {ptolemy.data.Token[]} 2 [list $nt3 $vt3]]

    set r3 [java::new {ptolemy.data.RecordToken} $l3 $v3]

    list [[$r1 isEqualTo $r2] toString] \
	    [[$r2 isEqualTo $r3] toString] \
	    [[$r1 isCloseTo $r2] toString] \
	    [[$r2 isCloseTo $r3] toString] \

} {false true false true}

######################################################################
####
# 
test RecordToken-isEqualTo.1 {Test isCloseTo and isEqualTo} {
    set epsilon 0.001
    set oldEpsilon [java::field ptolemy.math.Complex EPSILON]
    java::field ptolemy.math.Complex EPSILON $epsilon

    # Use r2 from above

    # 4th record is the close to the 2nd: {name="foo", value=5.0}
    set l4 [java::new {String[]} {2} {{name} {value}}]

    set nt4 [java::new {ptolemy.data.StringToken String} foo]
    # Look! It is a double instead of a int!
    set vt4 [java::new {ptolemy.data.DoubleToken double} \
	    [expr {5.0 + (0.1 * $epsilon) } ] ]
    set v4 [java::new {ptolemy.data.Token[]} 2 [list $nt4 $vt4]]

    set r4 [java::new {ptolemy.data.RecordToken} $l4 $v4]

    set res1 [[$r3 isEqualTo $r4] toString]
    set res2 [[$r4 isEqualTo $r4] toString]
    # Since the vt3 field of r3 is an Int, this will return false
    # because the IntToken.isCloseTo defaults to isEqualTo
    set res3 [[$r3 isCloseTo $r4] toString]
    set res4 [[$r4 isCloseTo $r3] toString]

    java::field ptolemy.math.Complex EPSILON $oldEpsilon
    list $res1 $res2 $res3 $res4
} {false true true true}

######################################################################
####
# 
test RecordToken-labelSet.0 {Test labelSet} {
    # record is empty
    set l1 [java::new {String[]} {0} {}]
    set v1 [java::new {ptolemy.data.Token[]} {0} {}]
    set r1 [java::new {ptolemy.data.RecordToken} $l1 $v1]

   listToStrings [$r1 labelSet]
} {}

######################################################################
####
# 
test RecordToken-labelSet.1 {Test labelSet} {
    # 2nd record is {name="foo", value=5}
    set l2 [java::new {String[]} {2} {{name} {value}}]

    set nt2 [java::new {ptolemy.data.StringToken String} foo]
    set vt2 [java::new {ptolemy.data.IntToken int} 5]
    set v2 [java::new {ptolemy.data.Token[]} 2 [list $nt2 $vt2]]

    set r2 [java::new {ptolemy.data.RecordToken} $l2 $v2]

    lsort [listToStrings [$r2 labelSet]]
} {name value}

######################################################################
####
# 
test RecordToken-merge.0 {Test merge} {
    # first record is {name=2.5, value=1, extra1=2}
    set l1 [java::new {String[]} {3} {{name} {value} {extra1}}]

    set nt1 [java::new {ptolemy.data.DoubleToken double} 2.5]
    set vt1 [java::new {ptolemy.data.IntToken int} 1]
    set et1 [java::new {ptolemy.data.IntToken int} 2]
    set v1 [java::new {ptolemy.data.Token[]} 3 [list $nt1 $vt1 $et1]]

    set r1 [java::new {ptolemy.data.RecordToken} $l1 $v1]

    # second record is {name=4, extra2=8.5, value=5.5}
    set l2 [java::new {String[]} {3} {{name} {extra2} {value}}]

    set nt2 [java::new {ptolemy.data.IntToken int} 4]
    set et2 [java::new {ptolemy.data.DoubleToken double} 8.5]
    set vt2 [java::new {ptolemy.data.DoubleToken double} 5.5]
    set v2 [java::new {ptolemy.data.Token[]} 3 [list $nt2 $et2 $vt2]]

    set r2 [java::new {ptolemy.data.RecordToken} $l2 $v2]

    [$r1 merge $r1 $r2] toString
} {{extra1=2, extra2=8.5, name=2.5, value=1}}

######################################################################
####
# 
test RecordToken-merge.1 {Test merge with empty record} {
    # first record is empty
    set l [java::new {String[]} {0} {}]
    set v [java::new {ptolemy.data.Token[]} {0} {}]
    set r [java::new {ptolemy.data.RecordToken} $l $v]

    # second record is {name="foo", value=1, extra1=2}
    set l1 [java::new {String[]} {3} {{name} {value} {extra1}}]

    set nt1 [java::new {ptolemy.data.StringToken String} foo]
    set vt1 [java::new {ptolemy.data.IntToken int} 1]
    set et1 [java::new {ptolemy.data.IntToken int} 2]
    set v1 [java::new {ptolemy.data.Token[]} 3 [list $nt1 $vt1 $et1]]

    set r1 [java::new {ptolemy.data.RecordToken} $l1 $v1]

    [$r merge $r $r1] toString
} {{extra1=2, name="foo", value=1}}

test RecordToken-merge.4.2 {Test merge with two empty records} {
    [$r merge $r $r] toString
} {{}}


######################################################################
####
# 
test RecordToken-modulo.0 {Test modulo} {
    set r1 [java::new {ptolemy.data.RecordToken} {{value=2.0, extra1=2}}]

    set r2 [java::new {ptolemy.data.RecordToken} {{extra2=8.5, value=5.5}}]

    [$r2 modulo $r1] toString
} {{value=1.5}}

######################################################################
####
# 
test RecordToken-multiply.0 {Test multiply} {
    # first record is {name="foo", value=1, extra1=2}
    set l1 [java::new {String[]} {3} {{name} {value} {extra1}}]

    set nt1 [java::new {ptolemy.data.StringToken String} foo]
    set vt1 [java::new {ptolemy.data.IntToken int} 1]
    set et1 [java::new {ptolemy.data.IntToken int} 2]
    set v1 [java::new {ptolemy.data.Token[]} 3 [list $nt1 $vt1 $et1]]

    set r1 [java::new {ptolemy.data.RecordToken} $l1 $v1]

    # second record is {name="bar", extra2=8.5, value=5.5}
    set l2 [java::new {String[]} {3} {{name} {extra2} {value}}]

    set nt2 [java::new {ptolemy.data.StringToken String} bar]
    set et2 [java::new {ptolemy.data.DoubleToken double} 8.5]
    set vt2 [java::new {ptolemy.data.DoubleToken double} 5.5]
    set v2 [java::new {ptolemy.data.Token[]} 3 [list $nt2 $et2 $vt2]]

    set r2 [java::new {ptolemy.data.RecordToken} $l2 $v2]

    catch {[$r1 multiply $r2] toString} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: multiply operation not supported between ptolemy.data.RecordToken '{extra1=2, name="foo", value=1}' and ptolemy.data.RecordToken '{extra2=8.5, name="bar", value=5.5}'
Because:
multiply operation not supported between ptolemy.data.StringToken '"foo"' and ptolemy.data.StringToken '"bar"'}}

######################################################################
####
# 
test RecordToken-multiply.1 {Test multiply} {
    # first record is {name=2.5, value=1, extra1=2}
    set l1 [java::new {String[]} {3} {{name} {value} {extra1}}]

    set nt1 [java::new {ptolemy.data.DoubleToken double} 2.5]
    set vt1 [java::new {ptolemy.data.IntToken int} 1]
    set et1 [java::new {ptolemy.data.IntToken int} 2]
    set v1 [java::new {ptolemy.data.Token[]} 3 [list $nt1 $vt1 $et1]]

    set r1 [java::new {ptolemy.data.RecordToken} $l1 $v1]

    # second record is {name=4, extra2=8.5, value=5.5}
    set l2 [java::new {String[]} {3} {{name} {extra2} {value}}]

    set nt2 [java::new {ptolemy.data.IntToken int} 4]
    set et2 [java::new {ptolemy.data.DoubleToken double} 8.5]
    set vt2 [java::new {ptolemy.data.DoubleToken double} 5.5]
    set v2 [java::new {ptolemy.data.Token[]} 3 [list $nt2 $et2 $vt2]]

    set r2 [java::new {ptolemy.data.RecordToken} $l2 $v2]

    [$r1 multiply $r2] toString
} {{name=10.0, value=5.5}}

######################################################################
####
# 
test RecordToken-multiply.2 {Test multiply, reverse the order} {
    [$r2 multiply $r1] toString
} {{name=10.0, value=5.5}}

######################################################################
####
# 
test RecordToken-multiply.3 {Test multiplying with empty record} {
    # first record is empty
    set l [java::new {String[]} {0} {}]
    set v [java::new {ptolemy.data.Token[]} {0} {}]
    set r [java::new {ptolemy.data.RecordToken} $l $v]

    # second record is {name="foo", value=1, extra1=2}
    set l1 [java::new {String[]} {3} {{name} {value} {extra1}}]

    set nt1 [java::new {ptolemy.data.StringToken String} foo]
    set vt1 [java::new {ptolemy.data.IntToken int} 1]
    set et1 [java::new {ptolemy.data.IntToken int} 2]
    set v1 [java::new {ptolemy.data.Token[]} 3 [list $nt1 $vt1 $et1]]

    set r1 [java::new {ptolemy.data.RecordToken} $l1 $v1]

    [$r multiply $r1] toString
} {{}}

######################################################################
####
# 
test RecordToken-multiply.4 {Test multiplying with empty record, reverse order} {
    [$r1 multiply $r] toString
} {{}}

######################################################################
####
# 
test RecordToken-one.0 {Test one} {
    # record is empty
    set l [java::new {String[]} {0} {}]
    set v [java::new {ptolemy.data.Token[]} {0} {}]
    set r [java::new {ptolemy.data.RecordToken} $l $v]

    [$r one] toString
} {{}}

######################################################################
####
# 
test RecordToken-one.1 {Test one} {
    set l [java::new {String[]} {2} {{name} {value}}]

    set nt [java::new {ptolemy.data.StringToken String} foo]
    set vt [java::new {ptolemy.data.IntToken int} 5]
    set v [java::new {ptolemy.data.Token[]} 2 [list $nt $vt]]

    set r [java::new {ptolemy.data.RecordToken} $l $v]

    catch {$r one} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Multiplicative identity not supported on ptolemy.data.StringToken.}}

######################################################################
####
# 
test RecordToken-one.2 {Test one} {
    set l [java::new {String[]} {2} {{value1} {value2}}]

    set v1 [java::new {ptolemy.data.IntToken int} 5]
    set v2 [java::new {ptolemy.data.DoubleToken double} 3.5]
    set v [java::new {ptolemy.data.Token[]} 2 [list $v1 $v2]]

    set r [java::new {ptolemy.data.RecordToken} $l $v]

    [$r one] toString
} {{value1=1, value2=1.0}}

######################################################################
####
# 
test RecordToken-subtract.0 {Test subtract} {
    # first record is {name="foo", value=1, extra1=2}
    set l1 [java::new {String[]} {3} {{name} {value} {extra1}}]

    set nt1 [java::new {ptolemy.data.StringToken String} foo]
    set vt1 [java::new {ptolemy.data.IntToken int} 1]
    set et1 [java::new {ptolemy.data.IntToken int} 2]
    set v1 [java::new {ptolemy.data.Token[]} 3 [list $nt1 $vt1 $et1]]

    set r1 [java::new {ptolemy.data.RecordToken} $l1 $v1]

    # second record is {name="bar", extra2=8.5, value=5.5}
    set l2 [java::new {String[]} {3} {{name} {extra2} {value}}]

    set nt2 [java::new {ptolemy.data.StringToken String} bar]
    set et2 [java::new {ptolemy.data.DoubleToken double} 8.5]
    set vt2 [java::new {ptolemy.data.DoubleToken double} 5.5]
    set v2 [java::new {ptolemy.data.Token[]} 3 [list $nt2 $et2 $vt2]]

    set r2 [java::new {ptolemy.data.RecordToken} $l2 $v2]

    catch {[$r1 subtract $r2] toString} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: subtract operation not supported between ptolemy.data.RecordToken '{extra1=2, name="foo", value=1}' and ptolemy.data.RecordToken '{extra2=8.5, name="bar", value=5.5}'
Because:
subtract operation not supported between ptolemy.data.StringToken '"foo"' and ptolemy.data.StringToken '"bar"'}}

######################################################################
####
# 
test RecordToken-subtract.1 {Test subtract} {
    # first record is {name=2.5, value=1, extra1=2}
    set l1 [java::new {String[]} {3} {{name} {value} {extra1}}]

    set nt1 [java::new {ptolemy.data.DoubleToken double} 2.5]
    set vt1 [java::new {ptolemy.data.IntToken int} 1]
    set et1 [java::new {ptolemy.data.IntToken int} 2]
    set v1 [java::new {ptolemy.data.Token[]} 3 [list $nt1 $vt1 $et1]]

    set r1 [java::new {ptolemy.data.RecordToken} $l1 $v1]

    # second record is {name=4, extra2=8.5, value=5.5}
    set l2 [java::new {String[]} {3} {{name} {extra2} {value}}]

    set nt2 [java::new {ptolemy.data.IntToken int} 4]
    set et2 [java::new {ptolemy.data.DoubleToken double} 8.5]
    set vt2 [java::new {ptolemy.data.DoubleToken double} 5.5]
    set v2 [java::new {ptolemy.data.Token[]} 3 [list $nt2 $et2 $vt2]]

    set r2 [java::new {ptolemy.data.RecordToken} $l2 $v2]

    [$r1 subtract $r2] toString
} {{name=-1.5, value=-4.5}}

######################################################################
####
# 
test RecordToken-subtract.3 {Test subtract, reverse the order} {
    [$r2 subtract $r1] toString
} {{name=1.5, value=4.5}}

######################################################################
####
# 
test RecordToken-subtract.4 {Test subtracting with empty record} {
    # first record is empty
    set l [java::new {String[]} {0} {}]
    set v [java::new {ptolemy.data.Token[]} {0} {}]
    set r [java::new {ptolemy.data.RecordToken} $l $v]

    # second record is {name="foo", value=1, extra1=2}
    set l1 [java::new {String[]} {3} {{name} {value} {extra1}}]

    set nt1 [java::new {ptolemy.data.StringToken String} foo]
    set vt1 [java::new {ptolemy.data.IntToken int} 1]
    set et1 [java::new {ptolemy.data.IntToken int} 2]
    set v1 [java::new {ptolemy.data.Token[]} 3 [list $nt1 $vt1 $et1]]

    set r1 [java::new {ptolemy.data.RecordToken} $l1 $v1]

    [$r subtract $r1] toString
} {{}}

######################################################################
####
# 
test RecordToken-subtract.5 {Test subtracting with empty record, reverse order} {
    [$r1 subtract $r] toString
} {{}}

######################################################################
####
# 
test RecordToken-zero.0 {Test zero} {
    # record is empty
    set l [java::new {String[]} {0} {}]
    set v [java::new {ptolemy.data.Token[]} {0} {}]
    set r [java::new {ptolemy.data.RecordToken} $l $v]

    [$r zero] toString
} {{}}

######################################################################
####
# 
test RecordToken-zero.1 {Test zero} {
    set l [java::new {String[]} {2} {{name} {value}}]

    set nt [java::new {ptolemy.data.StringToken String} foo]
    set vt [java::new {ptolemy.data.IntToken int} 5]
    set v [java::new {ptolemy.data.Token[]} 2 [list $nt $vt]]

    set r [java::new {ptolemy.data.RecordToken} $l $v]

    [$r zero] toString
} {{name="", value=0}}

######################################################################
####
# 
test RecordToken-zero.2 {Test zero} {
    set l [java::new {String[]} {2} {{value1} {value2}}]

    set v1 [java::new {ptolemy.data.IntToken int} 5]
    set v2 [java::new {ptolemy.data.DoubleToken double} 3.5]
    set v [java::new {ptolemy.data.Token[]} 2 [list $v1 $v2]]

    set r [java::new {ptolemy.data.RecordToken} $l $v]

    [$r zero] toString
} {{value1=0, value2=0.0}}



