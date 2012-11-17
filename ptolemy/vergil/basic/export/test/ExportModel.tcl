# Tests for the ExportModel.tcl
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2012 The Regents of the University of California.
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

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}

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


java::call System setProperty ptolemy.ptII.doNotExit true

test ExportModel-1.1 {Generate a gif} {
    if {[file exists modulation.gif]} {
	file delete -force modulation.gif
    }
    set modelFile $PTII/ptolemy/moml/demo/modulation.xml
    set args [java::new {String[]} {1} [list $modelFile]]
    java::call ptolemy.vergil.basic.export.ExportModel main $args
    set results [file exists modulation.gif]
    file delete -force modulation.gif
    list $results
} {1}

test ExportModel-2.1-h {Test -h: Generate a help message} {
    set args [java::new {String[]} {1} [list {-h}]]
    jdkCapture {
	java::call ptolemy.vergil.basic.export.ExportModel main $args
    } results
    list $results
} {{Usage:
java -classpath $PTII ptolemy.vergil.basic.export.ExportModel [-help|-h|--help] | [-copyJavaScript] [-force] [-open] [-openComposites] [-run] [-save] [-web] [-whiteBackground] [GIF|gif|HTM*|htm*|PNG|png] model.xml
Command line arguments are: 
 -help      Print this message.
 -copyJavaScriptFiles  Copy .js files.  Useful only with -web and htm* format.
 -force     Delete the target file or directory before generating the results.
 -open      Open the generated file.
 -openComposites       Open any composites before exporting the model.
 -run       Run the model before exporting. -web and htm*: plots are also generated.
 -save      Save the model before closing.
 -timeOut milliseconds   Timeout in milliseconds.
 -web  Common web export args. Short for: -force -copyJavaScriptFiles -open -openComposites htm.
 -whiteBackground      Set the background color to white.
 GIF|gif|HTM*|htm*|PNG|png The file format.
 model.xml  The Ptolemy model. (Required)
To export html suitable for the Ptolemy website, invoke 
Java with -Dptolemy.ptII.exportHTML.usePtWebsite=true
For example:
export JAVAFLAGS=-Dptolemy.ptII.exportHTML.usePtWebsite=true
$PTII/bin/ptweb $PTII/ptolemy/moml/demo/modulation.xml
To include a link to a sanitizedModelName.jnlp file,
set -Dptolemy.ptII.exportHTML.linkToJNLP=true
}}

test ExportModel-2.1-directory {Test -o: Generate a png in a different directory} {
    set outputFile [java::call java.io.File createTempFile ExportModel .png]
    set outputFileName [$outputFile toString]
    puts $outputFileName
    set modelFile $PTII/ptolemy/moml/demo/modulation.xml
    set args [java::new {String[]} {4} [list {-force} {png} $modelFile $outputFileName]]
    
    java::call ptolemy.vergil.basic.export.ExportModel main $args
    set results [list [file exists $outputFileName] [expr {[file size $outputFileName] > 1}]]
    file delete -force $outputFileName
    list $results
} {{1 1}}


test ExportModel-2.1-web {Test -web: Generate an html file} {
    if {[file exists modulation]} { 
	file delete -force modulation
    }
    set modelFile $PTII/ptolemy/moml/demo/modulation.xml
    set args [java::new {String[]} {2} [list {-web} $modelFile]]
    java::call ptolemy.vergil.basic.export.ExportModel main $args
    set results [file exists modulation/index.html]
    file delete -force modulation
    list $results
} {1}


