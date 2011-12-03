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
#   grep SDFDirector `cat /tmp/models` | awk -F : '{ print $1}' | grep -v /cg/ | grep -v /codegen/ | grep -v /jai/ | grep -v /jmf/ > /tmp/sdf0
#  grep domains/sdf /tmp/sdf0 > /tmp/sdf1
#  grep -v domains /tmp/sdf0 > /tmp/sdf2
#  cat /tmp/sdf1 /tmp/sdf2 | sort | 


#  grep AutoAdapter ~/cg/*.java | 

# $PTII/lbnl/test/auto/CRoom.xml Hangs

# Incorrect results:
# ptolemy/actor/lib/colt/test/auto/AllColt.xml Incorrect results
# $PTII/ptolemy/actor/lib/colt/test/auto/LazyVariableBug.xml Incorrect results

# Fails to compile:
# $PTII/ptolemy/actor/lib/comm/test/auto/HadamardCode1.xml

set models [list \
$PTII/ptolemy/actor/lib/colt/test/auto/AllColt.xml \
$PTII/ptolemy/actor/lib/colt/test/auto/BinomialSelector_tests.xml \
$PTII/ptolemy/actor/lib/colt/test/auto/ColtBinomialSelectorManyTrials.xml \
$PTII/ptolemy/actor/lib/colt/test/auto/LazyVariableBug.xml \
$PTII/ptolemy/actor/lib/comm/test/auto/DeScrambler.xml \
$PTII/ptolemy/actor/lib/comm/test/auto/HadamardCode.xml \
$PTII/ptolemy/actor/lib/comm/test/auto/HadamardCode1.xml \
$PTII/ptolemy/actor/lib/comm/test/auto/HammingCodec.xml \
$PTII/ptolemy/actor/lib/comm/test/auto/Scrambler.xml \
$PTII/ptolemy/actor/lib/comm/test/auto/Scrambler1.xml \
$PTII/ptolemy/actor/lib/comm/test/auto/Slicer.xml \
$PTII/ptolemy/actor/lib/comm/test/auto/TrellisDecoder.xml \
$PTII/ptolemy/actor/lib/comm/test/auto/ViterbiDecoderHard.xml \
$PTII/ptolemy/actor/lib/comm/test/auto/ViterbiDecoderSoft.xml \
$PTII/ptolemy/actor/lib/conversions/test/auto/ComplexToCartesianAndBack.xml \
$PTII/ptolemy/actor/lib/conversions/test/auto/ExpressioToToken.xml \
$PTII/ptolemy/actor/lib/conversions/test/auto/FixToDoubleAndBack.xml \
$PTII/ptolemy/actor/lib/conversions/test/auto/InUnitsOf.xml \
$PTII/ptolemy/actor/lib/conversions/test/auto/PolarToCartesianAndBack.xml \
$PTII/ptolemy/actor/lib/conversions/test/auto/PolarToComplexAndBack.xml \
$PTII/ptolemy/actor/lib/conversions/test/auto/Round.xml \
$PTII/ptolemy/actor/lib/conversions/test/auto/StringToIntArray.xml \
$PTII/ptolemy/actor/lib/conversions/test/auto/StringToUnsignedByteArray.xml \
$PTII/ptolemy/actor/lib/conversions/test/auto/TokenToExpression.xml \
$PTII/ptolemy/actor/lib/hoc/test/auto/ApplyFFTTest.xml \
$PTII/ptolemy/actor/lib/hoc/test/auto/ApplyFunction.xml \
$PTII/ptolemy/actor/lib/hoc/test/auto/Case1.xml \
$PTII/ptolemy/actor/lib/hoc/test/auto/Case2.xml \
$PTII/ptolemy/actor/lib/hoc/test/auto/Case3.xml \
$PTII/ptolemy/actor/lib/hoc/test/auto/Case4.xml \
$PTII/ptolemy/actor/lib/hoc/test/auto/Case6.xml \
$PTII/ptolemy/actor/lib/hoc/test/auto/CaseString2.xml \
$PTII/ptolemy/actor/lib/hoc/test/auto/ConcurrentExecutionTimeSDF.xml \
$PTII/ptolemy/actor/lib/hoc/test/auto/DFTSubSetTest.xml \
$PTII/ptolemy/actor/lib/hoc/test/auto/IterateOverArray.xml \
$PTII/ptolemy/actor/lib/hoc/test/auto/IterateOverArray10.xml \
$PTII/ptolemy/actor/lib/hoc/test/auto/IterateOverArray11.xml \
$PTII/ptolemy/actor/lib/hoc/test/auto/IterateOverArray12.xml \
$PTII/ptolemy/actor/lib/hoc/test/auto/IterateOverArray13.xml \
$PTII/ptolemy/actor/lib/hoc/test/auto/IterateOverArray14.xml \
$PTII/ptolemy/actor/lib/hoc/test/auto/IterateOverArray2.xml \
$PTII/ptolemy/actor/lib/hoc/test/auto/IterateOverArray3.xml \
$PTII/ptolemy/actor/lib/hoc/test/auto/IterateOverArray4.xml \
$PTII/ptolemy/actor/lib/hoc/test/auto/IterateOverArray5.xml \
$PTII/ptolemy/actor/lib/hoc/test/auto/IterateOverArray6.xml \
$PTII/ptolemy/actor/lib/hoc/test/auto/IterateOverArray7.xml \
$PTII/ptolemy/actor/lib/hoc/test/auto/IterateOverArray8.xml \
$PTII/ptolemy/actor/lib/hoc/test/auto/IterateOverArray9.xml \
$PTII/ptolemy/actor/lib/hoc/test/auto/ModelReference.xml \
$PTII/ptolemy/actor/lib/hoc/test/auto/ModelReference3.xml \
$PTII/ptolemy/actor/lib/hoc/test/auto/ModelReference4.xml \
$PTII/ptolemy/actor/lib/hoc/test/auto/MultiInstanceComposite.xml \
$PTII/ptolemy/actor/lib/hoc/test/auto/MultiInstanceComposite2.xml \
$PTII/ptolemy/actor/lib/hoc/test/auto/RunCompositeActor.xml \
$PTII/ptolemy/actor/lib/hoc/test/auto/RunCompositeActor2.xml \
$PTII/ptolemy/actor/lib/image/test/auto/ImageReaderImageDisplay.xml \
$PTII/ptolemy/actor/lib/image/test/auto/ImageReaderImageRotateImageToString.xml \
$PTII/ptolemy/actor/lib/image/test/auto/ImageReaderImageToString.xml \
$PTII/ptolemy/actor/lib/image/test/auto/ImageReaderURLImageToString.xml \
$PTII/ptolemy/actor/lib/io/test/auto/DirectoryListing.xml \
$PTII/ptolemy/actor/lib/io/test/auto/FileReader.xml \
$PTII/ptolemy/actor/lib/io/test/auto/ReadCSV1.xml \
$PTII/ptolemy/actor/lib/io/test/auto/ReadCSV2.xml \
$PTII/ptolemy/actor/lib/io/test/auto/ReadCSV3.xml \
$PTII/ptolemy/actor/lib/io/test/auto/ReadCSV4.xml \
$PTII/ptolemy/actor/lib/io/test/auto/ReadCSV5.xml \
$PTII/ptolemy/actor/lib/io/test/auto/ReadCSV6.xml \
$PTII/ptolemy/actor/lib/io/test/auto/ReadExpressions.xml \
$PTII/ptolemy/actor/lib/io/test/auto/ReadLineInClasspath.xml \
$PTII/ptolemy/actor/lib/io/test/auto/ReadLineInPTII.xml \
$PTII/ptolemy/actor/lib/io/test/auto/ReadLineInSameDirectory.xml \
$PTII/ptolemy/actor/lib/io/test/auto/ReadLineInSubDirectory.xml \
$PTII/ptolemy/actor/lib/javasound/test/auto/testAudioCapture.xml \
$PTII/ptolemy/actor/lib/javasound/test/auto/testAudioCapture_AudioPlayer.xml \
$PTII/ptolemy/actor/lib/javasound/test/auto/testAudioPlayer.xml \
$PTII/ptolemy/actor/lib/javasound/test/auto/testAudioReader.xml \
$PTII/ptolemy/actor/lib/javasound/test/auto/testAudioReaderAudioPlayer.xml \
$PTII/ptolemy/actor/lib/javasound/test/auto/testAudioWriter.xml \
$PTII/ptolemy/actor/lib/mail/test/auto/SendMail.xml \
$PTII/ptolemy/actor/lib/net/test/auto/Dummy.xml \
$PTII/ptolemy/actor/lib/python/test/auto/PythonClone.xml \
$PTII/ptolemy/actor/lib/python/test/auto/PythonReadFile.xml \
$PTII/ptolemy/actor/lib/python/test/auto/PythonScale.xml \
$PTII/ptolemy/actor/lib/python/test/auto/PythonStop.xml \
$PTII/ptolemy/actor/lib/python/test/auto/PythonTerminate.xml \
$PTII/ptolemy/actor/lib/python/test/auto/methodCall.xml \
$PTII/ptolemy/actor/lib/python/test/auto/testPythonClass.xml \
$PTII/ptolemy/actor/lib/security/test/auto/KeyReader.xml \
$PTII/ptolemy/actor/lib/security/test/auto/KeyWriter.xml \
$PTII/ptolemy/actor/lib/security/test/auto/Signature.xml \
$PTII/ptolemy/actor/lib/security/test/auto/Symmetric.xml \
$PTII/ptolemy/actor/lib/string/test/auto/StringCompare.xml \
$PTII/ptolemy/actor/lib/string/test/auto/StringCompare2.xml \
$PTII/ptolemy/actor/lib/string/test/auto/StringFunction.xml \
$PTII/ptolemy/actor/lib/string/test/auto/StringLength.xml \
$PTII/ptolemy/actor/lib/string/test/auto/StringMatches.xml \
$PTII/ptolemy/actor/lib/string/test/auto/StringMatches2.xml \
$PTII/ptolemy/actor/lib/string/test/auto/StringParameter.xml \
$PTII/ptolemy/actor/lib/string/test/auto/StringReplace.xml \
$PTII/ptolemy/actor/lib/string/test/auto/StringReplace2.xml \
$PTII/ptolemy/actor/lib/string/test/auto/StringSimpleReplace.xml \
$PTII/ptolemy/actor/lib/string/test/auto/StringSubstring.xml \
$PTII/ptolemy/actor/lib/string/test/auto/StringSubstring2.xml \
$PTII/ptolemy/actor/lib/string/test/auto/StringSubstring3.xml \
$PTII/ptolemy/actor/lib/test/auto/AbsoluteValue.xml \
$PTII/ptolemy/actor/lib/test/auto/Accumulator.xml \
$PTII/ptolemy/actor/lib/test/auto/Accumulator2.xml \
$PTII/ptolemy/actor/lib/test/auto/Accumulator3.xml \
$PTII/ptolemy/actor/lib/test/auto/ArrayAppend.xml \
$PTII/ptolemy/actor/lib/test/auto/ArrayAppend2.xml \
$PTII/ptolemy/actor/lib/test/auto/ArrayAppend3.xml \
$PTII/ptolemy/actor/lib/test/auto/ArrayAppend4.xml \
$PTII/ptolemy/actor/lib/test/auto/ArrayAppend5.xml \
$PTII/ptolemy/actor/lib/test/auto/ArrayAppend6.xml \
$PTII/ptolemy/actor/lib/test/auto/ArrayAverage.xml \
$PTII/ptolemy/actor/lib/test/auto/ArrayContains.xml \
$PTII/ptolemy/actor/lib/test/auto/ArrayElement.xml \
$PTII/ptolemy/actor/lib/test/auto/ArrayElementAsMatrix.xml \
$PTII/ptolemy/actor/lib/test/auto/ArrayExtract.xml \
$PTII/ptolemy/actor/lib/test/auto/ArrayExtract2.xml \
$PTII/ptolemy/actor/lib/test/auto/ArrayLength.xml \
$PTII/ptolemy/actor/lib/test/auto/ArrayLevelCrossing.xml \
$PTII/ptolemy/actor/lib/test/auto/ArrayLevelCrossing2.xml \
$PTII/ptolemy/actor/lib/test/auto/ArrayPeakSearch.xml \
$PTII/ptolemy/actor/lib/test/auto/ArrayPeakSearch2.xml \
$PTII/ptolemy/actor/lib/test/auto/ArrayPeakSearch3.xml \
$PTII/ptolemy/actor/lib/test/auto/ArrayRemoveElement.xml \
$PTII/ptolemy/actor/lib/test/auto/ArraySort.xml \
$PTII/ptolemy/actor/lib/test/auto/ArraySum.xml \
$PTII/ptolemy/actor/lib/test/auto/ArrayToElements.xml \
$PTII/ptolemy/actor/lib/test/auto/BooleanMultiplexor.xml \
$PTII/ptolemy/actor/lib/test/auto/BusActors.xml \
$PTII/ptolemy/actor/lib/test/auto/ClassWrapper.xml \
$PTII/ptolemy/actor/lib/test/auto/CommDistDiscard.xml \
$PTII/ptolemy/actor/lib/test/auto/Commutator.xml \
$PTII/ptolemy/actor/lib/test/auto/Commutator2.xml \
$PTII/ptolemy/actor/lib/test/auto/Commutator3.xml \
$PTII/ptolemy/actor/lib/test/auto/Compare.xml \
$PTII/ptolemy/actor/lib/test/auto/ComplexDivide.xml \
$PTII/ptolemy/actor/lib/test/auto/ComputeHistogram.xml \
$PTII/ptolemy/actor/lib/test/auto/Const.xml \
$PTII/ptolemy/actor/lib/test/auto/Const2.xml \
$PTII/ptolemy/actor/lib/test/auto/Const3.xml \
$PTII/ptolemy/actor/lib/test/auto/CurrentTimeSDF.xml \
$PTII/ptolemy/actor/lib/test/auto/DB.xml \
$PTII/ptolemy/actor/lib/test/auto/DelayTime.xml \
$PTII/ptolemy/actor/lib/test/auto/Differential.xml \
$PTII/ptolemy/actor/lib/test/auto/DisplayArray.xml \
$PTII/ptolemy/actor/lib/test/auto/Distributor.xml \
$PTII/ptolemy/actor/lib/test/auto/Distributor2.xml \
$PTII/ptolemy/actor/lib/test/auto/DownSample.xml \
$PTII/ptolemy/actor/lib/test/auto/ElementsToArray.xml \
$PTII/ptolemy/actor/lib/test/auto/Equals.xml \
$PTII/ptolemy/actor/lib/test/auto/Exec.xml \
$PTII/ptolemy/actor/lib/test/auto/ExecEnvironment.xml \
$PTII/ptolemy/actor/lib/test/auto/ExecRunDemos.xml \
$PTII/ptolemy/actor/lib/test/auto/Expression1.xml \
$PTII/ptolemy/actor/lib/test/auto/Expression10.xml \
$PTII/ptolemy/actor/lib/test/auto/Expression11.xml \
$PTII/ptolemy/actor/lib/test/auto/Expression12.xml \
$PTII/ptolemy/actor/lib/test/auto/Expression13.xml \
$PTII/ptolemy/actor/lib/test/auto/Expression14.xml \
$PTII/ptolemy/actor/lib/test/auto/Expression15.xml \
$PTII/ptolemy/actor/lib/test/auto/Expression16.xml \
$PTII/ptolemy/actor/lib/test/auto/Expression17.xml \
$PTII/ptolemy/actor/lib/test/auto/Expression18.xml \
$PTII/ptolemy/actor/lib/test/auto/Expression2.xml \
$PTII/ptolemy/actor/lib/test/auto/Expression3.xml \
$PTII/ptolemy/actor/lib/test/auto/Expression4.xml \
$PTII/ptolemy/actor/lib/test/auto/Expression5.xml \
$PTII/ptolemy/actor/lib/test/auto/Expression6.xml \
$PTII/ptolemy/actor/lib/test/auto/Expression7.xml \
$PTII/ptolemy/actor/lib/test/auto/Expression8.xml \
$PTII/ptolemy/actor/lib/test/auto/Expression9.xml \
$PTII/ptolemy/actor/lib/test/auto/ExpressionFix.xml \
$PTII/ptolemy/actor/lib/test/auto/ExpressionFix2.xml \
$PTII/ptolemy/actor/lib/test/auto/ExpressionFix3.xml \
$PTII/ptolemy/actor/lib/test/auto/FileWriter1.xml \
$PTII/ptolemy/actor/lib/test/auto/FileWriter2.xml \
$PTII/ptolemy/actor/lib/test/auto/FileWriter3.xml \
$PTII/ptolemy/actor/lib/test/auto/Gaussian.xml \
$PTII/ptolemy/actor/lib/test/auto/Gaussian1.xml \
$PTII/ptolemy/actor/lib/test/auto/Gaussian2.xml \
$PTII/ptolemy/actor/lib/test/auto/GradientAdaptiveLattice.xml \
$PTII/ptolemy/actor/lib/test/auto/IIR.xml \
$PTII/ptolemy/actor/lib/test/auto/Lattice.xml \
$PTII/ptolemy/actor/lib/test/auto/LazyAOCTestLazy.xml \
$PTII/ptolemy/actor/lib/test/auto/LazyAOCTestNonLazy.xml \
$PTII/ptolemy/actor/lib/test/auto/LazyInnerClass.xml \
$PTII/ptolemy/actor/lib/test/auto/LazyPubSub.xml \
$PTII/ptolemy/actor/lib/test/auto/LazySubClassModel.xml \
$PTII/ptolemy/actor/lib/test/auto/LevelCrossTest1.xml \
$PTII/ptolemy/actor/lib/test/auto/LevelCrossTest10.xml \
$PTII/ptolemy/actor/lib/test/auto/LevelCrossTest11.xml \
$PTII/ptolemy/actor/lib/test/auto/LevelCrossTest12.xml \
$PTII/ptolemy/actor/lib/test/auto/LevelCrossTest13.xml \
$PTII/ptolemy/actor/lib/test/auto/LevelCrossTest2.xml \
$PTII/ptolemy/actor/lib/test/auto/LevelCrossTest3.xml \
$PTII/ptolemy/actor/lib/test/auto/LevelCrossTest4.xml \
$PTII/ptolemy/actor/lib/test/auto/LevelCrossTest5.xml \
$PTII/ptolemy/actor/lib/test/auto/LevelCrossTest6.xml \
$PTII/ptolemy/actor/lib/test/auto/LevelCrossTest7.xml \
$PTII/ptolemy/actor/lib/test/auto/LevelCrossTest8.xml \
$PTII/ptolemy/actor/lib/test/auto/LevelCrossTest9.xml \
$PTII/ptolemy/actor/lib/test/auto/LevinsonDurbin.xml \
$PTII/ptolemy/actor/lib/test/auto/LevinsonDurbin2.xml \
$PTII/ptolemy/actor/lib/test/auto/LevinsonDurbin3.xml \
$PTII/ptolemy/actor/lib/test/auto/Limiter.xml \
$PTII/ptolemy/actor/lib/test/auto/LinearDifferenceEquationSystem.xml \
$PTII/ptolemy/actor/lib/test/auto/LookupTable.xml \
$PTII/ptolemy/actor/lib/test/auto/MathFunction2.xml \
$PTII/ptolemy/actor/lib/test/auto/MathFunction3.xml \
$PTII/ptolemy/actor/lib/test/auto/MatrixCropBoolean.xml \
$PTII/ptolemy/actor/lib/test/auto/MatrixCropComplex.xml \
$PTII/ptolemy/actor/lib/test/auto/MatrixCropDouble.xml \
$PTII/ptolemy/actor/lib/test/auto/MatrixCropFix.xml \
$PTII/ptolemy/actor/lib/test/auto/MatrixCropInt.xml \
$PTII/ptolemy/actor/lib/test/auto/MatrixCropLong.xml \
$PTII/ptolemy/actor/lib/test/auto/MaxIndex.xml \
$PTII/ptolemy/actor/lib/test/auto/Maximum.xml \
$PTII/ptolemy/actor/lib/test/auto/MaximumComplex.xml \
$PTII/ptolemy/actor/lib/test/auto/MethodCallTest.xml \
$PTII/ptolemy/actor/lib/test/auto/Minimum.xml \
$PTII/ptolemy/actor/lib/test/auto/MinimumComplex.xml \
$PTII/ptolemy/actor/lib/test/auto/MinimumMultiports.xml \
$PTII/ptolemy/actor/lib/test/auto/MovingAverage.xml \
$PTII/ptolemy/actor/lib/test/auto/MultipleLinksToSameRelation.xml \
$PTII/ptolemy/actor/lib/test/auto/Multiplexor.xml \
$PTII/ptolemy/actor/lib/test/auto/MultiportBroadcastTest.xml \
$PTII/ptolemy/actor/lib/test/auto/NilToken.xml \
$PTII/ptolemy/actor/lib/test/auto/NilTokenNonStrictTest.xml \
$PTII/ptolemy/actor/lib/test/auto/NilTokenRecord.xml \
$PTII/ptolemy/actor/lib/test/auto/NilTokenTypeTest.xml \
$PTII/ptolemy/actor/lib/test/auto/OrderedRecordDisassembler.xml \
$PTII/ptolemy/actor/lib/test/auto/OrderedRecordDisassembler2.xml \
$PTII/ptolemy/actor/lib/test/auto/OrderedRecordUpdater.xml \
$PTII/ptolemy/actor/lib/test/auto/OrderedRecordUpdater2.xml \
$PTII/ptolemy/actor/lib/test/auto/PhaseUnwrap.xml \
$PTII/ptolemy/actor/lib/test/auto/PublisherClassNoParameter.xml \
$PTII/ptolemy/actor/lib/test/auto/PublisherClassParameter.xml \
$PTII/ptolemy/actor/lib/test/auto/PublisherNonStrictTest.xml \
$PTII/ptolemy/actor/lib/test/auto/PublisherSubscriber.xml \
$PTII/ptolemy/actor/lib/test/auto/PublisherSubscriber10.xml \
$PTII/ptolemy/actor/lib/test/auto/PublisherSubscriber11.xml \
$PTII/ptolemy/actor/lib/test/auto/PublisherSubscriber12.xml \
$PTII/ptolemy/actor/lib/test/auto/PublisherSubscriber13.xml \
$PTII/ptolemy/actor/lib/test/auto/PublisherSubscriber14.xml \
$PTII/ptolemy/actor/lib/test/auto/PublisherSubscriber2.xml \
$PTII/ptolemy/actor/lib/test/auto/PublisherSubscriber3.xml \
$PTII/ptolemy/actor/lib/test/auto/PublisherSubscriber4.xml \
$PTII/ptolemy/actor/lib/test/auto/PublisherSubscriber5.xml \
$PTII/ptolemy/actor/lib/test/auto/PublisherSubscriber6.xml \
$PTII/ptolemy/actor/lib/test/auto/PublisherSubscriber7.xml \
$PTII/ptolemy/actor/lib/test/auto/PublisherSubscriberChannelVariablesAOC2.xml \
$PTII/ptolemy/actor/lib/test/auto/PublisherSubscriberExpression.xml \
$PTII/ptolemy/actor/lib/test/auto/PublisherSubscriberOpaque.xml \
$PTII/ptolemy/actor/lib/test/auto/PublisherSubscriberOpaqueWidthInference.xml \
$PTII/ptolemy/actor/lib/test/auto/PublisherTestSubscriber.xml \
$PTII/ptolemy/actor/lib/test/auto/Ramp1.xml \
$PTII/ptolemy/actor/lib/test/auto/RampFiringLimitSDF.xml \
$PTII/ptolemy/actor/lib/test/auto/ReadFile1.xml \
$PTII/ptolemy/actor/lib/test/auto/ReadFile2.xml \
$PTII/ptolemy/actor/lib/test/auto/Reader.xml \
$PTII/ptolemy/actor/lib/test/auto/RecordDisassember.xml \
$PTII/ptolemy/actor/lib/test/auto/RecordDisassembler.xml \
$PTII/ptolemy/actor/lib/test/auto/RecordDisassembler2.xml \
$PTII/ptolemy/actor/lib/test/auto/RecordUpdater.xml \
$PTII/ptolemy/actor/lib/test/auto/RecordUpdater2.xml \
$PTII/ptolemy/actor/lib/test/auto/RecursiveLattice.xml \
$PTII/ptolemy/actor/lib/test/auto/Scale.xml \
$PTII/ptolemy/actor/lib/test/auto/Scale2.xml \
$PTII/ptolemy/actor/lib/test/auto/Scale3.xml \
$PTII/ptolemy/actor/lib/test/auto/ScaleArray.xml \
$PTII/ptolemy/actor/lib/test/auto/ScaleArray2.xml \
$PTII/ptolemy/actor/lib/test/auto/ScaleMatrix.xml \
$PTII/ptolemy/actor/lib/test/auto/SetVariable.xml \
$PTII/ptolemy/actor/lib/test/auto/SetVariable2.xml \
$PTII/ptolemy/actor/lib/test/auto/SetVariable3.xml \
$PTII/ptolemy/actor/lib/test/auto/Sinewave.xml \
$PTII/ptolemy/actor/lib/test/auto/Sinewave2.xml \
$PTII/ptolemy/actor/lib/test/auto/Sinewave3.xml \
$PTII/ptolemy/actor/lib/test/auto/Sleep.xml \
$PTII/ptolemy/actor/lib/test/auto/SleepMultipleFire.xml \
$PTII/ptolemy/actor/lib/test/auto/StopSDF.xml \
$PTII/ptolemy/actor/lib/test/auto/StringConstant.xml \
$PTII/ptolemy/actor/lib/test/auto/SubscriptionAggregator.xml \
$PTII/ptolemy/actor/lib/test/auto/SubscriptionAggregator2.xml \
$PTII/ptolemy/actor/lib/test/auto/SubscriptionAggregator3.xml \
$PTII/ptolemy/actor/lib/test/auto/SubscriptionAggregatorMultiply.xml \
$PTII/ptolemy/actor/lib/test/auto/Test.xml \
$PTII/ptolemy/actor/lib/test/auto/TestSimple.xml \
$PTII/ptolemy/actor/lib/test/auto/ThrowModelError.xml \
$PTII/ptolemy/actor/lib/test/auto/TriangularDistTestModel.xml \
$PTII/ptolemy/actor/lib/test/auto/TrigFunction.xml \
$PTII/ptolemy/actor/lib/test/auto/TrigFunctionInverse.xml \
$PTII/ptolemy/actor/lib/test/auto/TrigFunctionTan.xml \
$PTII/ptolemy/actor/lib/test/auto/URLDirectoryReader.xml \
$PTII/ptolemy/actor/lib/test/auto/URLDirectoryReader3.xml \
$PTII/ptolemy/actor/lib/test/auto/UnaryMathFunction.xml \
$PTII/ptolemy/actor/lib/test/auto/Uniform.xml \
$PTII/ptolemy/actor/lib/test/auto/UtilityFunctions.xml \
$PTII/ptolemy/actor/lib/test/auto/VariableSleep.xml \
$PTII/ptolemy/actor/lib/test/auto/VectorAssemblerDisassemblerSDF.xml \
$PTII/ptolemy/actor/lib/test/auto/WallClockTime.xml \
$PTII/ptolemy/actor/lib/test/auto/WidthTestWithSubscriberPublisher.xml \
$PTII/ptolemy/actor/lib/test/auto/WidthTestWithSubscriberPublisher2.xml \
$PTII/ptolemy/actor/lib/test/auto/WidthTestWithSubscriberPublisher3.xml \
$PTII/ptolemy/actor/lib/test/auto/array.xml \
$PTII/ptolemy/actor/lib/test/auto/array2.xml \
$PTII/ptolemy/actor/lib/test/auto/arrayType.xml \
$PTII/ptolemy/actor/lib/test/auto/arrayType10.xml \
$PTII/ptolemy/actor/lib/test/auto/arrayType11.xml \
$PTII/ptolemy/actor/lib/test/auto/arrayType12.xml \
$PTII/ptolemy/actor/lib/test/auto/arrayType13.xml \
$PTII/ptolemy/actor/lib/test/auto/arrayType14.xml \
$PTII/ptolemy/actor/lib/test/auto/arrayType15.xml \
$PTII/ptolemy/actor/lib/test/auto/arrayType16.xml \
$PTII/ptolemy/actor/lib/test/auto/arrayType17.xml \
$PTII/ptolemy/actor/lib/test/auto/arrayType18.xml \
$PTII/ptolemy/actor/lib/test/auto/arrayType19.xml \
$PTII/ptolemy/actor/lib/test/auto/arrayType2.xml \
$PTII/ptolemy/actor/lib/test/auto/arrayType20.xml \
$PTII/ptolemy/actor/lib/test/auto/arrayType21.xml \
$PTII/ptolemy/actor/lib/test/auto/arrayType22.xml \
$PTII/ptolemy/actor/lib/test/auto/arrayType23.xml \
$PTII/ptolemy/actor/lib/test/auto/arrayType24.xml \
$PTII/ptolemy/actor/lib/test/auto/arrayType25.xml \
$PTII/ptolemy/actor/lib/test/auto/arrayType26.xml \
$PTII/ptolemy/actor/lib/test/auto/arrayType27.xml \
$PTII/ptolemy/actor/lib/test/auto/arrayType28.xml \
$PTII/ptolemy/actor/lib/test/auto/arrayType3.xml \
$PTII/ptolemy/actor/lib/test/auto/arrayType4.xml \
$PTII/ptolemy/actor/lib/test/auto/arrayType5.xml \
$PTII/ptolemy/actor/lib/test/auto/arrayType6.xml \
$PTII/ptolemy/actor/lib/test/auto/arrayType7.xml \
$PTII/ptolemy/actor/lib/test/auto/arrayType8.xml \
$PTII/ptolemy/actor/lib/test/auto/arrayType9.xml \
$PTII/ptolemy/actor/lib/test/auto/cast.xml \
$PTII/ptolemy/actor/lib/test/auto/compositeMultiPort.xml \
$PTII/ptolemy/actor/lib/test/auto/expressionCastInference.xml \
$PTII/ptolemy/actor/lib/test/auto/expressionCastInference2.xml \
$PTII/ptolemy/actor/lib/test/auto/expressionInference.xml \
$PTII/ptolemy/actor/lib/test/auto/expression_bug.xml \
$PTII/ptolemy/actor/lib/test/auto/factorial.xml \
$PTII/ptolemy/actor/lib/test/auto/filterTest1.xml \
$PTII/ptolemy/actor/lib/test/auto/funcApplyInConst.xml \
$PTII/ptolemy/actor/lib/test/auto/funcApplyInExpr.xml \
$PTII/ptolemy/actor/lib/test/auto/function.xml \
$PTII/ptolemy/actor/lib/test/auto/function2.xml \
$PTII/ptolemy/actor/lib/test/auto/function3.xml \
$PTII/ptolemy/actor/lib/test/auto/functionClosureTest1.xml \
$PTII/ptolemy/actor/lib/test/auto/functionClosureTest2.xml \
$PTII/ptolemy/actor/lib/test/auto/functionClosureTest3.xml \
$PTII/ptolemy/actor/lib/test/auto/functionClosureTest4.xml \
$PTII/ptolemy/actor/lib/test/auto/functionClosureTest5.xml \
$PTII/ptolemy/actor/lib/test/auto/functionClosureTest6.xml \
$PTII/ptolemy/actor/lib/test/auto/functionsOnArcs.xml \
$PTII/ptolemy/actor/lib/test/auto/functionsOnArcsHOF.xml \
$PTII/ptolemy/actor/lib/test/auto/logic.xml \
$PTII/ptolemy/actor/lib/test/auto/matrixType.xml \
$PTII/ptolemy/actor/lib/test/auto/matrixType10.xml \
$PTII/ptolemy/actor/lib/test/auto/matrixType11.xml \
$PTII/ptolemy/actor/lib/test/auto/matrixType12.xml \
$PTII/ptolemy/actor/lib/test/auto/matrixType13.xml \
$PTII/ptolemy/actor/lib/test/auto/matrixType14.xml \
$PTII/ptolemy/actor/lib/test/auto/matrixType2.xml \
$PTII/ptolemy/actor/lib/test/auto/matrixType3.xml \
$PTII/ptolemy/actor/lib/test/auto/matrixType4.xml \
$PTII/ptolemy/actor/lib/test/auto/matrixType5.xml \
$PTII/ptolemy/actor/lib/test/auto/matrixType6.xml \
$PTII/ptolemy/actor/lib/test/auto/matrixType7.xml \
$PTII/ptolemy/actor/lib/test/auto/matrixType8.xml \
$PTII/ptolemy/actor/lib/test/auto/matrixType9.xml \
$PTII/ptolemy/actor/lib/test/auto/paraFunctest.xml \
$PTII/ptolemy/actor/lib/test/auto/paraFunctest2.xml \
$PTII/ptolemy/actor/lib/test/auto/parameterPassing.xml \
$PTII/ptolemy/actor/lib/test/auto/record.xml \
$PTII/ptolemy/actor/lib/test/auto/sizedarray1.xml \
$PTII/ptolemy/actor/lib/test/auto/sizedarray2.xml \
$PTII/ptolemy/actor/lib/test/auto/twoPublishers.xml \
$PTII/ptolemy/actor/lib/vhdl/test/auto/FixConst.xml \
$PTII/ptolemy/actor/lib/xslt/test/auto/XSLTransformerTest.xml \
$PTII/ptolemy/actor/parameters/test/auto/CompositeSDF.xml \
$PTII/ptolemy/actor/parameters/test/auto/ParameterSetExpression.xml \
$PTII/ptolemy/actor/parameters/test/auto/ParameterSetOverride.xml \
$PTII/ptolemy/actor/parameters/test/auto/ParameterSetTest.xml \
$PTII/ptolemy/actor/parameters/test/auto/ScopeExtendingAttributeExpression.xml \
$PTII/ptolemy/actor/parameters/test/auto/ScopeExtendingAttributeTest.xml \
$PTII/ptolemy/actor/parameters/test/auto/TwoRamps.xml \
$PTII/ptolemy/actor/parameters/test/auto/TwoRampsComposite.xml \
$PTII/ptolemy/actor/parameters/test/auto/TwoRampsTypes.xml \
$PTII/ptolemy/actor/ptalon/test/auto/EightChannelFFT.xml \
$PTII/ptolemy/actor/ptalon/test/auto/gameOfLife.xml \
$PTII/ptolemy/caltrop/test/auto/FunctionClosures.xml \
$PTII/ptolemy/caltrop/test/auto/FunctionDefinition.xml \
$PTII/ptolemy/caltrop/test/auto/Primes.xml \
$PTII/ptolemy/caltrop/test/auto/SDFDDI.xml \
$PTII/ptolemy/data/unit/test/auto/CGSUnitBase0.xml \
$PTII/ptolemy/moml/filter/test/auto/modulation2.xml \
	       ]

foreach model $models {
    testJavaCG $model
}
doneTests

echo "To find all the failed tests, use grep \"==== Auto Automatic\" AllSDF.out | awk '{print $NF}' | sort | uniq | awk -F / '{print $NF}' | sed 's/.xml//' > | sort /tmp/failed" 
echo "To find all the tests for which code was generated: "ls -1d ~/cg/* | wc -l"
echo "To find all the AutoAdapter tests: grep TypedCompositeActor ~/cg/*/*.java | awk -F : '{print $1}' | sort | uniq | awk -F / '{print $NF}'  | sed 's/.java//' | sort > /tmp/auto"
echo "To find all the AutoAdapter tests: comm -12 /tmp/failed /tmp/auto "
