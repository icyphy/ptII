# Test OpenCV SimpleSample.
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2016 The Regents of the University of California.
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

######################################################################
####
#
test SimpleSample-1.1 {Test loading of OpenCV Shared library} {
    set nativeLibraryName [java::field org.opencv.core.Core NATIVE_LIBRARY_NAME]
    list "Success"
} {Success} 

######################################################################
####
#
test SimpleSample-2.1 {A simple sample model} {
    # Based on http://docs.opencv.org/2.4/doc/tutorials/introduction/desktop_java/java_dev_intro.html.

    # Uses nativeLibraryName from 1.1 above.
    java::call org.ptolemy.opencv.OpenCVLoader loadOpenCV $nativeLibraryName
    set scalar [java::new {org.opencv.core.Scalar double} 0]
    // OpenCV 3.4.1 requires the cast of the Mat constructor.
    set m [java::new {org.opencv.core.Mat int int int org.opencv.core.Scalar} 4 10 [java::field org.opencv.core.CvType CV_8UC1] $scalar]
    set mr1 [$m row 1]
    $mr1 setTo [java::new {org.opencv.core.Scalar double} 1]
    set mc5 [$m col 5]
    $mc5 setTo [java::new {org.opencv.core.Scalar double} 5]
    $m dump
} {[  0,   0,   0,   0,   0,   5,   0,   0,   0,   0;
   1,   1,   1,   1,   1,   5,   1,   1,   1,   1;
   0,   0,   0,   0,   0,   5,   0,   0,   0,   0;
   0,   0,   0,   0,   0,   5,   0,   0,   0,   0]}
