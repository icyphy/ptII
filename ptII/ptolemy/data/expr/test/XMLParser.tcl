# Tests for the XMLParser class
#
# @Author: Christopher Brooks
#
# @Version $Id$
#
# @Copyright (c) 2006 The Regents of the University of California.
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

######################################################################
####
# 
test XMLParser-1.0 {Read file that has UTF-8 chars, test from Francesco Liuzzi} {

    set inputFile [java::new java.io.File XMLParserInput.xml]
    set inputURL [$inputFile toURL]
    set inputStream [$inputURL openStream]
    set inputReader [java::new java.io.BufferedReader [java::new java.io.InputStreamReader $inputStream]]
    set inputStringBuffer [java::new StringBuffer]
    set inputLine [$inputReader -noconvert readLine]
    while {$inputLine != [java::null]} {
        #puts "Line: [$inputLine toString]"
        $inputStringBuffer append "[$inputLine toString]\n"
        set inputLine [$inputReader -noconvert readLine]
    }
    $inputReader close
    set xmlParser [java::new ptolemy.data.expr.XMLParser]
    #puts "File is\n:[$inputStringBuffer toString]"
    set parse [$xmlParser parser [$inputStringBuffer toString]]
    set nodes [$parse getChildNodes]
    list [$parse getXmlEncoding] [$parse getXmlStandalone] [$parse getXmlVersion] [$parse getNodeName] [$nodes getLength]
} {UTF-8 0 1.0 #document 1}
