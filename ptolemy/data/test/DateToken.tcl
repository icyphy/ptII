# Tests for the DateToken class
#
# @Author: Edward A. Lee, Neil Smyth, Yuhong Xiong
#
# @Version: $Id: DateToken.tcl 57040 2010-01-27 20:52:32Z cxh $
#
# @Copyright (c) 1997-2008 The Regents of the University of California.
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
    set nowms [[$now -noconvert getValue] -noconvert getTime]
    set clockSeconds [clock seconds]
    set clockDMY [clock format $clockSeconds -format "%a %b %d"]
    regexp "^\"$clockDMY.*" [$now toString] 
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
test DateToken-3.0 {Test isEqualTo} {
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
test DateToken-5.0 {Nil Date tokens} {
    set nilDate [java::new ptolemy.data.DateToken "nil"]
    set nil2Date [java::new ptolemy.data.DateToken "nil"]
    set nullDate [java::new ptolemy.data.DateToken [java::null]]
    list [[$nilDate isEqualTo $nilDate] toString] \
	[[$nilDate isEqualTo $nil2Date] toString] \
	[[$nilDate isEqualTo $nullDate] toString] \
	[$nilDate isNil] \
	[$nullDate isNil]
} {false false false 1 0}


