# Tests for the OrderedRecordToken class
#
# @Author: Yuhong Xiong and Elaine Cheong
#
# @Version $Id: RecordToken.tcl 57040 2010-01-27 20:52:32Z cxh $
#
# @Copyright (c) 1997-2012 The Regents of the University of California.
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
test OrderedRecordToken-toString.2 {Test toString with spaces} {
    set l [java::new {String[]} {2} {{with spaces} {value}}]

    set nt [java::new {ptolemy.data.StringToken String} foo]
    set vt [java::new {ptolemy.data.IntToken int} 5]
    set v [java::new {ptolemy.data.Token[]} 2 [list $nt $vt]]

    set r1 [java::new {ptolemy.data.OrderedRecordToken} $l $v]
    set result1 [$r1 toString]
    set r2 [java::new {ptolemy.data.OrderedRecordToken} $result1]
    set result2 [$r2 toString]
    list $result1 $result2 [$r1 equals $r2]
} {{["with spaces" = "foo", value = 5]} {["with spaces" = "foo", value = 5]} 1}
