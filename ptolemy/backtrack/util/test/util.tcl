# Tests for the XmlHandler class
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2005 The Regents of the University of California.
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

# Load the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[string compare jdkCapture [info procs jdkCapture]] == 1} then {
    source [file join $PTII util testsuite jdktools.tcl]
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

#
# Invoke the main method of className and return the results of stdout.
proc util {className} {
    jdkCapture {    	
        if [catch {
		java::call ptolemy.backtrack.util.test.$className main \
		    [java::new {String[]} 0]} errMsg] {
	    puts $errMsg
	}
    } results
    return $results
}

######################################################################
####
#

test Random-1.1 {} {
    util RandomTest1
} {0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 
}

test Random-2.1 {} {
    util RandomTest2
} {0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 
}

test TreeMap-1.1 {} {
    util TreeMapTest1
} {{0=20, 1=19, 2=18, 3=17, 4=16, 5=15, 6=14, 7=13, 8=12, 9=11, 10=10, 11=9, 12=8, 13=7, 14=6, 15=5, 16=4, 17=3, 18=2, 19=1}
{0=20, 1=19, 2=18, 3=17, 4=16, 5=15, 6=14, 7=13, 8=12, 9=11, 10=10, 11=9, 12=8, 13=7, 14=6, 15=5, 16=4, 17=3, 18=2}
{0=20, 1=19, 2=18, 3=17, 4=16, 5=15, 6=14, 7=13, 8=12, 9=11, 10=10, 11=9, 12=8, 13=7, 14=6, 15=5, 16=4, 17=3}
{0=20, 1=19, 2=18, 3=17, 4=16, 5=15, 6=14, 7=13, 8=12, 9=11, 10=10, 11=9, 12=8, 13=7, 14=6, 15=5, 16=4}
{0=20, 1=19, 2=18, 3=17, 4=16, 5=15, 6=14, 7=13, 8=12, 9=11, 10=10, 11=9, 12=8, 13=7, 14=6, 15=5}
{0=20, 1=19, 2=18, 3=17, 4=16, 5=15, 6=14, 7=13, 8=12, 9=11, 10=10, 11=9, 12=8, 13=7, 14=6}
{0=20, 1=19, 2=18, 3=17, 4=16, 5=15, 6=14, 7=13, 8=12, 9=11, 10=10, 11=9, 12=8, 13=7}
{0=20, 1=19, 2=18, 3=17, 4=16, 5=15, 6=14, 7=13, 8=12, 9=11, 10=10, 11=9, 12=8}
{0=20, 1=19, 2=18, 3=17, 4=16, 5=15, 6=14, 7=13, 8=12, 9=11, 10=10, 11=9}
{0=20, 1=19, 2=18, 3=17, 4=16, 5=15, 6=14, 7=13, 8=12, 9=11, 10=10}
{0=20, 1=19, 2=18, 3=17, 4=16, 5=15, 6=14, 7=13, 8=12, 9=11}
{0=20, 1=19, 2=18, 3=17, 4=16, 5=15, 6=14, 7=13, 8=12}
{0=20, 1=19, 2=18, 3=17, 4=16, 5=15, 6=14, 7=13}
{0=20, 1=19, 2=18, 3=17, 4=16, 5=15, 6=14}
{0=20, 1=19, 2=18, 3=17, 4=16, 5=15}
{0=20, 1=19, 2=18, 3=17, 4=16}
{0=20, 1=19, 2=18, 3=17}
{0=20, 1=19, 2=18}
{0=20, 1=19}
{0=20}
{}
}

test TreeMap-2.1 {} {
    util TreeMapTest2
} {{}
{19=1}
{18=2, 19=1}
{17=3, 18=2, 19=1}
{16=4, 17=3, 18=2, 19=1}
{15=5, 16=4, 17=3, 18=2, 19=1}
{14=6, 15=5, 16=4, 17=3, 18=2, 19=1}
{13=7, 14=6, 15=5, 16=4, 17=3, 18=2, 19=1}
{12=8, 13=7, 14=6, 15=5, 16=4, 17=3, 18=2, 19=1}
{11=9, 12=8, 13=7, 14=6, 15=5, 16=4, 17=3, 18=2, 19=1}
{10=10, 11=9, 12=8, 13=7, 14=6, 15=5, 16=4, 17=3, 18=2, 19=1}
{9=11, 10=10, 11=9, 12=8, 13=7, 14=6, 15=5, 16=4, 17=3, 18=2, 19=1}
{8=12, 9=11, 10=10, 11=9, 12=8, 13=7, 14=6, 15=5, 16=4, 17=3, 18=2, 19=1}
{7=13, 8=12, 9=11, 10=10, 11=9, 12=8, 13=7, 14=6, 15=5, 16=4, 17=3, 18=2, 19=1}
{6=14, 7=13, 8=12, 9=11, 10=10, 11=9, 12=8, 13=7, 14=6, 15=5, 16=4, 17=3, 18=2, 19=1}
{5=15, 6=14, 7=13, 8=12, 9=11, 10=10, 11=9, 12=8, 13=7, 14=6, 15=5, 16=4, 17=3, 18=2, 19=1}
{4=16, 5=15, 6=14, 7=13, 8=12, 9=11, 10=10, 11=9, 12=8, 13=7, 14=6, 15=5, 16=4, 17=3, 18=2, 19=1}
{3=17, 4=16, 5=15, 6=14, 7=13, 8=12, 9=11, 10=10, 11=9, 12=8, 13=7, 14=6, 15=5, 16=4, 17=3, 18=2, 19=1}
{2=18, 3=17, 4=16, 5=15, 6=14, 7=13, 8=12, 9=11, 10=10, 11=9, 12=8, 13=7, 14=6, 15=5, 16=4, 17=3, 18=2, 19=1}
{1=19, 2=18, 3=17, 4=16, 5=15, 6=14, 7=13, 8=12, 9=11, 10=10, 11=9, 12=8, 13=7, 14=6, 15=5, 16=4, 17=3, 18=2, 19=1}
{0=20, 1=19, 2=18, 3=17, 4=16, 5=15, 6=14, 7=13, 8=12, 9=11, 10=10, 11=9, 12=8, 13=7, 14=6, 15=5, 16=4, 17=3, 18=2, 19=1}
}

test TreeMap-3.1 {} {
    util TreeMapTest3
} {{0=20, 1=19, 2=18, 3=17, 4=16, 5=15, 6=14, 7=13, 8=12, 9=11, 10=10, 11=9, 12=8, 13=7, 14=6, 15=5, 16=4, 17=3, 18=2, 19=1}
{0=20, 1=19, 2=18, 3=17, 4=16, 5=15, 6=14, 7=13, 8=12, 9=11, 10=10, 11=9, 12=8, 13=7, 14=6, 15=5, 16=4, 17=3, 18=2, 19=1}
{0=20, 1=19, 2=18, 3=17, 4=16, 5=15, 6=14, 7=13, 8=12, 9=11, 10=10, 11=9, 12=8, 13=7, 14=6, 15=5, 16=4, 17=3, 18=2, 19=1}
{0=20, 1=19, 2=18, 3=17, 4=16, 5=15, 6=14, 7=13, 8=12, 9=11, 10=10, 11=9, 12=8, 13=7, 14=6, 15=5, 16=4, 17=3, 18=2, 19=1}
}
