# Tests for the Audio
#
# @Author: Christopher Hylands, Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1999-2003 The Regents of the University of California.
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

####
#
test Audio-2.0 {Audio(byte []): Generate a .au file from an empty byte array} {
    set audioByteArray [java::new {byte[]} 0 {}]

    set audio [java::new {ptolemy.media.Audio {byte[]}} $audioByteArray]
    set fos [java::new {java.io.FileOutputStream java.lang.String} "tmp.au"]
    set dos [java::new java.io.DataOutputStream $fos]
    $audio write $dos
    # Check out the toLinear method
    set intArray [$audio toLinear 0]
    $dos close
    # It would be nice if we could diff two files somehome
    list [$audio toString] [$dos size] [jdkPrintArray $intArray]
} {{file ID tag = .snd
offset = 37
size = 0
format code = 1
sampleRate = 8000
number of channels = 1
info field = Ptolemy audio} 37 {}}

####
#
test Audio-2.1 {Audio(byte []): Generate a .au file from a byte array} {
    set audioByteArray [java::new {byte[]} 5 {-127 -63 0 63 127 }]

    set audio [java::new {ptolemy.media.Audio {byte[]}} $audioByteArray]
    set fos [java::new {java.io.FileOutputStream java.lang.String} "tmp.au"]
    set dos [java::new java.io.DataOutputStream $fos]
    $audio write $dos
    $dos close
    set doubleArray [$audio toDouble 0]
    # Try getting an out of range channel
    set doubleArray2 [$audio toDouble 1]
    set intArray [$audio toLinear 0]

    # It would be nice if we could diff two files somehome
    list "[$audio toString]\n\
	    [$dos size]\n\
	    <[epsilonDiff [jdkPrintArray $doubleArray] \
	    {0.967611336032 0.0566801619433 -1.0 -0.0607287449393 0.0}]>\n\
	    <[epsilonDiff [jdkPrintArray $intArray] \
	    {30592 1792 -31616 -1920 0}]>\n\
	    [expr {[java::null] == $doubleArray2}]"
} {{file ID tag = .snd
offset = 37
size = 5
format code = 1
sampleRate = 8000
number of channels = 1
info field = Ptolemy audio
 42
 <>
 <>
 1}} 

####
#
test Audio-2.2 {Audio(double []): Generate a .au file from a double array} {
    set audioDoubleArray [java::new {double[]} 5 {-1.0 0.5 0 0.5 1.0}]

    set audio [java::new {ptolemy.media.Audio {double[]}} $audioDoubleArray]
    set fos [java::new {java.io.FileOutputStream java.lang.String} "tmp.au"]
    set dos [java::new java.io.DataOutputStream $fos]
    $audio write $dos
    $dos close
    # It would be nice if we could diff two files somehome
    list [$audio toString] [$dos size]
} {{file ID tag = .snd
offset = 37
size = 5
format code = 1
sampleRate = 8000
number of channels = 1
info field = Ptolemy audio} 42}


######################################################################
####
#
test Audio-2.3 {Audio(DataInputStream) write(DataOutputStream): \
	Read in an audio file, write it back out} {
    set fis [java::new {java.io.FileInputStream String} "bark.au"]
    set dis [java::new java.io.DataInputStream $fis]
    set audio [java::new {ptolemy.media.Audio java.io.DataInputStream} $dis]

    set fos [java::new {java.io.FileOutputStream java.lang.String} "tmp.au"]
    set dos [java::new java.io.DataOutputStream $fos]
    $audio write $dos
    $dos close
    $fos close
    $dis close
    $fis close
   # It would be nice if we could diff these two files somehome
    list [$audio toString] [$dos size]
} {{file ID tag = .snd
offset = 40
size = 2367
format code = 1
sampleRate = 8000
number of channels = 1
info field = terrier bark} 2407}

######################################################################
####
#
test Audio-2.4 {Audio(DataInputStream): Try to read a non audio file} {
    set fis [java::new {java.io.FileInputStream String} "Audio.tcl"]
    set dis [java::new java.io.DataInputStream $fis]
    catch {set audio [java::new {ptolemy.media.Audio java.io.DataInputStream} $dis]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.media.Audio: bad magic number in stream header.  Not an audio file?}}


######################################################################
#### Audio_CreateAUFile
# Creates a custom .au file to our specifications
#
proc Audio_CreateAUFile {name offset size format sampleRate numChannels} {
    file delete -force $name
    set fos [java::new {java.io.FileOutputStream java.lang.String} $name]
    set dos [java::new java.io.DataOutputStream $fos]
    # The magic number, which should be 0x2E736E64, '.snd'
    $dos writeByte 0x2E
    $dos writeByte 0x73
    $dos writeByte 0x6E
    $dos writeByte 0x64
    $dos writeInt $offset
    $dos writeInt $size
    $dos writeInt $format
    $dos writeInt $sampleRate
    $dos writeInt $numChannels
    $dos close
    $fos close
}

######################################################################
####
#
test Audio-2.5 {Audio(DataInputStream): .snd header, bogus offset} {
    Audio_CreateAUFile tmp2_5.au 99999 1000 1 8000 1
    set fis [java::new {java.io.FileInputStream String} "tmp2_5.au"]
    set dis [java::new java.io.DataInputStream $fis]
    catch {set audio [java::new {ptolemy.media.Audio java.io.DataInputStream} $dis]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.media.Audio: offset value '99999' is out of range 0-10000}}


######################################################################
####
#
test Audio-2.6 {Audio(DataInputStream): .snd header, bogus format} {
    Audio_CreateAUFile tmp2.au 99 1000 2 8000 1
    set fis [java::new {java.io.FileInputStream String} "tmp2.au"]
    set dis [java::new java.io.DataInputStream $fis]
    catch {set audio [java::new {ptolemy.media.Audio java.io.DataInputStream} $dis]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.media.Audio: Sorry, only 8-bit mu-law encoded data can be read.}}


######################################################################
####
#
test Audio-2.7 {Audio(DataInputStream): .snd header, bogus channels} {
    Audio_CreateAUFile tmp2_7.au 99 1000 1 8000 2
    set fis [java::new {java.io.FileInputStream String} "tmp2_7.au"]
    set dis [java::new java.io.DataInputStream $fis]
    catch {set audio [java::new {ptolemy.media.Audio java.io.DataInputStream} $dis]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.media.Audio: Sorry, only one-channel audio data can be read.}}



######################################################################
####
#
test Audio-3.2 {writeRaw(DataOutputStream): 
                     Read in an audio file, write it back out as raw data} {
    # Use the $audio from Audio-2.1
    set fos [java::new {java.io.FileOutputStream java.lang.String} "tmp.au"]
    set dos [java::new java.io.DataOutputStream $fos]
    $audio writeRaw $dos
    $dos close
    list [$audio toString] [$dos size]
} {{file ID tag = .snd
offset = 40
size = 2367
format code = 1
sampleRate = 8000
number of channels = 1
info field = terrier bark} 2367}


######################################################################
####
#
test Audio-4.1 {writeAudio(): Generate a .au file from an array} {
    set audioArray [java::new {double[]} 5 {-1.0 0.5 0 0.5 1.0}]

    set fos [java::new {java.io.FileOutputStream String} "array.au"]
    set dos [java::new java.io.DataOutputStream $fos]
    java::call ptolemy.media.Audio writeAudio $audioArray $dos
    list [$audio toString] [$dos size]
} {{file ID tag = .snd
offset = 40
size = 2367
format code = 1
sampleRate = 8000
number of channels = 1
info field = terrier bark} 42}


######################################################################
####
#
test Audio-5.1 {lin2mu: operate on various data ranges} {
    set result {}
    foreach input  [list -99999 -32636 -32635 -32634 -1 0 \
	    1 32634 32635 32636 99999] {
	lappend result [java::call ptolemy.media.Audio lin2mu $input]
    }
    list $result
} {{0 0 0 0 127 -1 -1 -128 -128 -128 -128}}


######################################################################
####
#
test Audio-5.2 {lin2mu: setZeroTrap} {
    set result {}
    java::call ptolemy.media.Audio setZeroTrap True
    foreach input  [list -99999 -32636 -32635 -32634 -1 0 \
	    1 32634 32635 32636 99999] {
	lappend result [java::call ptolemy.media.Audio lin2mu $input]
    }
    list $result
} {{2 2 2 2 127 -1 -1 -128 -128 -128 -128}}


######################################################################
####
#
test Audio-6.1 {readAudio(): Read in by calling readAudio, \
	then create an Audio object} {
    # Uses array.au from 4.1 above
    set fis [java::new {java.io.FileInputStream String} "array.au"]
    set dis [java::new java.io.DataInputStream $fis]
    set doubleArray [java::call ptolemy.media.Audio readAudio $dis]
    $dis close
    $fis close
    set audio [java::new {ptolemy.media.Audio double[]} $doubleArray]
    list [$audio toString]
} {{file ID tag = .snd
offset = 37
size = 5
format code = 1
sampleRate = 8000
number of channels = 1
info field = Ptolemy audio}}

sleep 2
if [catch {file delete -force tmp.au tmp.raw array.au tmp2.au tmp2_5.au} msg] {
    puts "deleteing files failed: $msg"
}
