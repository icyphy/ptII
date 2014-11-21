# Tests for the DateToken class
#
# @Author: Edward A. Lee, Neil Smyth, Yuhong Xiong
#
# @Version: $Id$
#
# @Copyright (c) 1997-2014 The Regents of the University of California.
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
test DateToken-2.1 {Create a Date for the current time} {
    set now [java::new ptolemy.data.DateToken]
    # Converting to a Long has problems in Tcl, so we compare
    # the day of week, month and year.
    #set nowms [[$now -noconvert getTimeInMilliseconds] -noconvert getTime]
    set clockSeconds [clock seconds]
    set clockDMY [clock format $clockSeconds -format "%a %b %d"]
    regexp "$clockDMY.*" [$now toString] 
} {1}

######################################################################
####
# 
test DateToken-2.2 {Get the current time and make sure it is not null} {
    set p [java::new ptolemy.data.DateToken]
    set r1 [expr { [$p getValue] == [java::null] }]
    set r2 [$p isNil]
    list $r1 $r2
} {0 0}

######################################################################
####
# 
test DateToken-3.0 {Test convert } {

    # Create date with long token
    set dateLong1 [java::new {ptolemy.data.DateToken long} 1]

    # Create with a DateToken
    set dateDateLong1 [java::call ptolemy.data.DateToken convert $dateLong1]

    list [[$dateLong1 isEqualTo $dateDateLong1] toString]
} {true}

######################################################################
####
# 
test DateToken-3.1 {convert from a string to a DateToken does not work, which is correct} {
    # Marten wrote:
    # <cxh@eecs.berkeley.edu> wrote:
    # > I added a convert() method to DateToken which seems to work well. I did hack
    # > in a check for StringToken which will try to instantiate a DateToken.  I'm
    # > not sure if that is right.
    # This seems wrong to me. It is not generally possible to convert a
    # String into a Date. Also, the type lattice doesn't permit that
    # conversion. Type inference is supposed to yield a typing of which the
    # automatic type conversions that it imposes during run time work
    # without exception. We should not misuse the conversion method to build
    # a customized parser.
    set string1 [java::new {ptolemy.data.StringToken String} "Wed Dec 31 16:00:00.001 -0800 1969"]
    catch {java::call ptolemy.data.DateToken convert $string1} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.StringToken '"Wed Dec 31 16:00:00.001 -0800 1969"' to the type date because the type of the token is higher or incomparable with the given type.}}

######################################################################
####
# 
test DateToken-10.0 {Test isEqualTo} {
    set t1 [java::new {ptolemy.data.DateToken long} 0]
    set t2 [java::new {ptolemy.data.DateToken long} 0]
    set t3 [java::new {ptolemy.data.DateToken long} 1]
    list [[$t1 isEqualTo $t1] toString] \
	[[$t1 isEqualTo $t2] toString] \
	[[$t2 isEqualTo $t1] toString] \
	[[$t1 isEqualTo $t3] toString]
} {true true true false}

######################################################################
####
# 
test DateToken-9.0 {Test isGreatThan} {
    set t1 [java::new {ptolemy.data.DateToken long} 1]
    set t2 [java::new {ptolemy.data.DateToken long} 2]
    set t3 [java::new {ptolemy.data.DateToken long} 3]
    list [[$t1 isGreaterThan $t1] toString] \
	[[$t1 isGreaterThan $t2] toString] \
	[[$t1 isGreaterThan $t3] toString] \
	[[$t2 isGreaterThan $t1] toString] \
	[[$t2 isGreaterThan $t3] toString] \
	[[$t3 isGreaterThan $t3] toString]
} {false false false true false false}

######################################################################
####
# not supported anymore
# 
#test DateToken-9.1 {Test isGreaterThan with other types} {
#    set long1 [java::new {ptolemy.data.LongToken long} 1]
#    set int2 [java::new {ptolemy.data.IntToken int} 2]
#    set short3 [java::new {ptolemy.data.ShortToken short} 3]
#
#    set t2 [java::new {ptolemy.data.DateToken long} 2]
#    list [[$t2 isGreaterThan $long1] toString] \
#	[[$t2 isGreaterThan $int2] toString] \
#	[[$t2 isGreaterThan $short3] toString]
#} {true false false}

######################################################################
####
# 
test DateToken-10.0 {Test isLessThan} {
    set t1 [java::new {ptolemy.data.DateToken long} 1]
    set t2 [java::new {ptolemy.data.DateToken long} 2]
    set t3 [java::new {ptolemy.data.DateToken long} 3]
    list [[$t1 isLessThan $t1] toString] \
	[[$t1 isLessThan $t2] toString] \
	[[$t1 isLessThan $t3] toString] \
	[[$t2 isLessThan $t1] toString] \
	[[$t2 isLessThan $t3] toString] \
	[[$t3 isLessThan $t3] toString]
} {false true true false true false}

######################################################################
####
# 
# not supported anymore
#
#test DateToken-10.1 {Test isLessThan with other types} {
#    set long1 [java::new {ptolemy.data.LongToken long} 1]
#    set int2 [java::new {ptolemy.data.IntToken int} 2]
#    set short3 [java::new {ptolemy.data.ShortToken short} 3]
#
#    set t2 [java::new {ptolemy.data.DateToken long} 2]
#    list [[$t2 isLessThan $long1] toString] \
#	[[$t2 isLessThan $int2] toString] \
#	[[$t2 isLessThan $short3] toString]
#} {false false true}


######################################################################
####
# 
test DateToken-14.0 {Add two Dates} {
    set t1 [java::new {ptolemy.data.DateToken long} 1]
    set t2 [java::new {ptolemy.data.DateToken long} 2]
    catch {$t1 add $t2} err
    list $err
} {{ptolemy.kernel.util.IllegalActionException: add operation not supported between ptolemy.data.DateToken 'date("Wed Dec 31 16:00:00.001000000 -0800 1969")' and ptolemy.data.DateToken 'date("Wed Dec 31 16:00:00.002000000 -0800 1969")'}}

######################################################################
####
# 
test DateToken-14.1 {Divide two Dates} {
    set t1 [java::new {ptolemy.data.DateToken long} 1]
    set t2 [java::new {ptolemy.data.DateToken long} 2]
    catch {$t1 divide $t2} err
    list $err
} {{ptolemy.kernel.util.IllegalActionException: divide operation not supported between ptolemy.data.DateToken 'date("Wed Dec 31 16:00:00.001000000 -0800 1969")' and ptolemy.data.DateToken 'date("Wed Dec 31 16:00:00.002000000 -0800 1969")'}}

######################################################################
####
# 
test DateToken-14.2 {Modulo two Dates} {
    set t1 [java::new {ptolemy.data.DateToken long} 1]
    set t2 [java::new {ptolemy.data.DateToken long} 2]
    catch {$t1 modulo $t2} err
    list $err
} {{ptolemy.kernel.util.IllegalActionException: modulo operation not supported between ptolemy.data.DateToken 'date("Wed Dec 31 16:00:00.001000000 -0800 1969")' and ptolemy.data.DateToken 'date("Wed Dec 31 16:00:00.002000000 -0800 1969")'}}

######################################################################
####
# 
test DateToken-14.3 {Multiply two Dates} {
    set t1 [java::new {ptolemy.data.DateToken long} 1]
    set t2 [java::new {ptolemy.data.DateToken long} 2]
    catch {$t1 multiply $t2} err
    list $err
} {{ptolemy.kernel.util.IllegalActionException: multiply operation not supported between ptolemy.data.DateToken 'date("Wed Dec 31 16:00:00.001000000 -0800 1969")' and ptolemy.data.DateToken 'date("Wed Dec 31 16:00:00.002000000 -0800 1969")'}}

######################################################################
####
# 
test DateToken-14.4 {Subtract two Dates} {
    set t1 [java::new {ptolemy.data.DateToken long} 1]
    set t2 [java::new {ptolemy.data.DateToken long} 2]
    catch {$t1 subtract $t2} err
    list $err
} {{ptolemy.kernel.util.IllegalActionException: subtract operation not supported between ptolemy.data.DateToken 'date("Wed Dec 31 16:00:00.001000000 -0800 1969")' and ptolemy.data.DateToken 'date("Wed Dec 31 16:00:00.002000000 -0800 1969")'}}

######################################################################
####
# 
test DateToken-14.5 {compare two Dates} {
    set t1 [java::new {ptolemy.data.DateToken long} 1]
    set t2 [java::new {ptolemy.data.DateToken long} 2]
    set res [$t1 isCloseTo $t2] 
    list [$res toString]
} {false}
######################################################################
####
# 
test DateToken-14.6 {compare two Dates} {
    set t1 [java::new {ptolemy.data.DateToken long} 1]
    set t2 [java::new {ptolemy.data.DateToken long} 1]
    set res [$t1 isCloseTo $t2] 
    list [$res toString]
} {true}


######################################################################
####
# not supported anymore
# 
# test DateToken-15.0 {Create a date, get the toString, then try to recreate the date} {
#     set t1 [java::new {ptolemy.data.DateToken long} 1]
#     set stringt1 [$t1 -noconvert toString]
#     set t1again [java::new {ptolemy.data.DateToken String} $stringt1]
# 
    # FIXME: oddly, Date.getTime(), which returns the number of ms. since the Epoch returns different numbers
#     set t1ms [[$t1 getValue] getTime]
#     set t1againms [[$t1again getValue] getTime]
# 
#     list [$stringt1 equals [$t1again toString]] \
# 	$t1ms $t1againms
# } {1 1 1}


######################################################################
####
# 
test DateToken-25.0 {Nil Date tokens} {
    set nilDate [java::new ptolemy.data.DateToken "nil"]
    set nil2Date [java::new ptolemy.data.DateToken "nil"]
    set nullDate [java::new ptolemy.data.DateToken [java::null]]
    list [[$nilDate isEqualTo $nilDate] toString] \
	[[$nilDate isEqualTo $nil2Date] toString] \
	[[$nilDate isEqualTo $nullDate] toString] \
	[$nilDate isNil] \
	[$nullDate isNil]
} {false false false 1 0}


