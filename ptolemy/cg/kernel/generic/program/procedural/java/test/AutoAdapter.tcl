# Test AutoAdapter
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2010-2011 The Regents of the University of California.
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

# Currently, we can only generate code for models that have SDFDirectors:
#   cd $PTII
#   find . -name auto > /tmp/a
#   awk '{print "ls " $1 "/*.xml"}' /tmp/a > /tmp/d
#   sh /tmp/d > /tmp/models
#   grep SDFDirector `cat /tmp/models` | awk -F : '{print "$PTII/bin/ptcg -language java", $1}' | grep -v /cg/ | grep -v /codegen/ | grep -v /jai/ | grep -v /jmf/ > /tmp/doit
#  grep domains/sdf /tmp/doit > /tmp/doit.sdf
#  grep -v domains /tmp/doit > /tmp/doit.2
#  rm ~/cg/*
#  sh /tmp/doit.2 >& /tmp/doit.2.out &
#  grep AutoAdapter ~/cg/*.java | awk -F : '{print $1}' | sort | uniq


# $PTII/ptolemy/actor/lib/comm/test/auto/HammingCodec.xml   Runs, but "Attempt to get data from an empty mailbox." from fire()
# $PTII/ptolemy/actor/lib/comm/test/auto/HadamardCode.xml  Runs, but "Attempt to get data from an empty mailbox."  Uses arrays, fails in SequenceToArray.fire()
# $PTII/ptolemy/actor/lib/comm/test/auto/Scrambler.xml  Runs, but "Attempt to get data from an empty mailbox." fails in SequenceToArray.fire()
# $PTII/ptolemy/actor/lib/comm/test/auto/ViterbiDecoderHard.xml  Runs, but "Attempt to get data from an empty mailbox." Fails in ConvolutionalCoder.fire()
# $PTII/ptolemy/actor/lib/comm/test/auto/ViterbiDecoderSoft.xml  Runs, but "Attempt to get data from an empty mailbox." 
#    The problem here is that we create an Opaque composite for an auto generated actor
#    that has an output connected to a relation that is connected to two inputs.  The
#    code does a getInside() on the output port of the Opaque twice, hence the empty mailbox.    
#    Fails in ConvolutionalCoder.fire()
# $PTII/ptolemy/actor/lib/comm/test/auto/HadamardCode.xml  Runs, but gets "Cannot put a token in a full mailbox."
# $PTII/ptolemy/actor/lib/comm/test/auto/HuffmanCoder.xml  Fails to compile, uses DDF
# $PTII/ptolemy/actor/lib/comm/test/auto/HuffmanDecoder.xml  Fails to compile, uses DDF
# $PTII/ptolemy/actor/lib/comm/test/auto/LempelZivCoder.xml  Fails to compile, uses DDF
# $PTII/ptolemy/actor/lib/comm/test/auto/Slicer.xml  Runs, but gets "Cannot put a token in a full mailbox."
# $PTII/ptolemy/actor/lib/comm/test/auto/TrellisDecoder.xml  Runs, but gets "Cannot put a token in a full mailbox."

# $PTII/ptolemy/actor/lib/hoc/test/auto/ApplyFunction.xml   Fails to compile, needs function types
# $PTII/ptolemy/actor/lib/hoc/test/auto/BackgroundExecution.xml  Fails to compile, uses DE
# $PTII/ptolemy/actor/lib/hoc/test/auto/Case.xml  Fails to compile, uses DE

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

# $PTII/ptolemy/domains/sdf/lib/test/auto/ArrayToSequence.xml: Attempt to get data from an empty mailbox. Fails in SequenceToArray.fire().
# $PTII/ptolemy/domains/sdf/lib/test/auto/AutoCorrelation.xml: Attempt to get data from an empty mailbox. Fails in Autocorrelation.fire()
# $PTII/ptolemy/domains/sdf/lib/test/auto/VariableFIR.xml: Attempt to get data from an empty mailbox. Fails in SequenceToArray.fire();


set models [list \
		$PTII/ptolemy/actor/lib/colt/test/auto/AllColt.xml \
		$PTII/ptolemy/actor/lib/conversions/test/auto/StringToIntArray.xml \
		$PTII/ptolemy/actor/lib/conversions/test/auto/FixToDoubleAndBack.xml \
		$PTII/ptolemy/actor/lib/conversions/test/auto/StringToIntArray.xml \
		$PTII/ptolemy/actor/lib/hoc/test/auto/Case1.xml \
		$PTII/ptolemy/actor/lib/hoc/test/auto/Case2.xml \
		$PTII/ptolemy/actor/lib/test/auto/AbsoluteValue.xml \
		$PTII/ptolemy/actor/lib/test/auto/FileWriter1.xml \
		$PTII/ptolemy/actor/lib/test/auto/Gaussian1.xml \
		$PTII/ptolemy/actor/lib/test/auto/Gaussian2.xml \
		$PTII/ptolemy/actor/lib/test/auto/Lattice.xml \
		$PTII/ptolemy/actor/lib/test/auto/LookupTable.xml \
		$PTII/ptolemy/actor/lib/test/auto/MaxIndex.xml \
		$PTII/ptolemy/actor/lib/test/auto/Maximum.xml \
		$PTII/ptolemy/actor/lib/test/auto/Multiplexor.xml \
		$PTII/ptolemy/actor/lib/test/auto/PhaseUnwrap.xml \
		$PTII/ptolemy/actor/lib/test/auto/ReadFile1.xml \
		$PTII/ptolemy/actor/lib/test/auto/Sinewave.xml \
		$PTII/ptolemy/actor/lib/test/auto/Sinewave2.xml \
		$PTII/ptolemy/actor/lib/test/auto/Sinewave3.xml \
		$PTII/ptolemy/actor/lib/test/auto/UnaryMathFunction.xml \
		$PTII/ptolemy/actor/lib/test/auto/WallClockTime.xml \
		$PTII/ptolemy/actor/lib/test/auto/sizedarray1.xml \
		$PTII/ptolemy/actor/lib/comm/test/auto/DeScrambler.xml \
		$PTII/ptolemy/actor/lib/comm/test/auto/Scrambler1.xml \
		$PTII/ptolemy/actor/lib/hoc/test/auto/MultiInstanceComposite.xml \
		$PTII/ptolemy/actor/lib/string/test/auto/StringCompare.xml \
		$PTII/ptolemy/actor/lib/string/test/auto/StringFunction.xml \
		$PTII/ptolemy/actor/lib/string/test/auto/StringLength.xml \
		$PTII/ptolemy/actor/lib/string/test/auto/StringMatches2.xml \
		$PTII/ptolemy/actor/lib/string/test/auto/StringReplace.xml \
		$PTII/ptolemy/actor/lib/string/test/auto/StringSimpleReplace.xml \
		$PTII/ptolemy/actor/lib/string/test/auto/StringSubstring.xml \
		$PTII/ptolemy/actor/lib/string/test/auto/StringSubstring2.xml \
		$PTII/ptolemy/actor/lib/string/test/auto/StringSubstring3.xml \
		$PTII/ptolemy/actor/lib/string/test/auto/StringSubstring3.xml \
		$PTII/ptolemy/domains/sdf/lib/test/auto/DotProduct.xml \
		$PTII/ptolemy/domains/sdf/test/auto/time1.xml \
		$PTII/ptolemy/domains/sdf/test/auto/time2.xml \
		$PTII/ptolemy/domains/sdf/test/auto/time5.xml \
		$PTII/ptolemy/moml/filter/test/auto/modulation2.xml \
	       ]

foreach model $models {
    testJavaCG $model
}
