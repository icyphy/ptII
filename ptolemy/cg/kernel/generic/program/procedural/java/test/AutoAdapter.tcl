# Test AutoAdapter
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2010 The Regents of the University of California.
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

if {[info procs testJavaCG] == "" } then {
    source [file join $PTII util testsuite testJavaCG.tcl]
}

# $PTII/ptolemy/actor/lib/comm/test/auto/HammingCodec.xml   Runs, but "Attempt to get data from an empty mailbox."
# $PTII/ptolemy/actor/lib/comm/test/auto/DeScrambler.xml  Runs, but "Attempt to get data from an empty mailbox."
#    The problem here is that we create an Opaque composite for an auto generated actor
#    that has an output connected to a relation that is connected to two inputs.  The
#    code does a getInside() on the output port of the Opaque twice, hence the empty mailbox.    

# $PTII/ptolemy/actor/lib/comm/test/auto/HadamardCode.xml  Fails to compile:
# HadamardCode.java:2180: ';' expected
#            HadamardCode_DotProduct_input2=32)Value();


# $PTII/ptolemy/actor/lib/string/test/auto/StringMatches.xml   Runs, but gets wrong results
#     Won't fix right now, the problem is backslash hell.
# $PTII/ptolemy/actor/lib/string/test/auto/StringIndexOf.xml   FSM, won't fix right now
# $PTII/ptolemy/actor/lib/string/test/auto/StringParameter.xml Fails to generate:
#    Failed to find open paren in ""${i}...""
#    Won't fix right now, we don't support $i, which is a complex number. 
# $PTII/ptolemy/actor/lib/string/test/auto/StringReplace2.xml  Fails to generate:
#    Won't fix right now, the problem is that one of the expressions is "a$$0b", which causes trouble.
# $PTII/ptolemy/actor/lib/string/test/auto/StringCompare2.xml  Fails to compile:
#    Problems with \o
set models [list \
		$PTII/ptolemy/actor/lib/test/auto/LookupTable.xml \
		$PTII/ptolemy/actor/lib/test/auto/Maximum.xml \
		$PTII/ptolemy/actor/lib/comm/test/auto/Scrambler1.xml \
		$PTII/ptolemy/actor/lib/string/test/auto/StringCompare.xml \
		$PTII/ptolemy/actor/lib/string/test/auto/StringFunction.xml \
		$PTII/ptolemy/actor/lib/string/test/auto/StringLength.xml \
		$PTII/ptolemy/actor/lib/string/test/auto/StringMatches2.xml \
		$PTII/ptolemy/actor/lib/string/test/auto/StringReplace.xml \
		$PTII/ptolemy/actor/lib/string/test/auto/StringSimpleReplace.xml \
		$PTII/ptolemy/actor/lib/string/test/auto/StringSubstring.xml \
		$PTII/ptolemy/actor/lib/string/test/auto/StringSubstring2.xml \
		$PTII/ptolemy/actor/lib/string/test/auto/StringSubstring3.xml \
	       ]

foreach model $models {
    testJavaCG $model
}