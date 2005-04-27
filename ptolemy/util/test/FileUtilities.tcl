# Tests for the FileUtilities class
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2004-2005 The Regents of the University of California.
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


set tmpFile FileUtilities-1.1.tmp

######################################################################
####
#
test FileUtilities-1.1 {binaryCopyURLToFile} {
    set sourceURL [java::new java.net.URL file:./makefile]

    file delete -force $tmpFile
    set fileExists0 [file exists $tmpFile]

    set destinationFile [java::new java.io.File $tmpFile]
    set results [java::call ptolemy.util.FileUtilities binaryCopyURLToFile \
		     $sourceURL $destinationFile]
		 
    list $fileExists0 $results \
	[expr {[file size makefile] == [file size $tmpFile]}]

} {0 1 1}

######################################################################
####
#
test FileUtilities-1.2 {binaryCopyURLToFile URL does not exist} {

    set sourceURL [java::new java.net.URL file:./doesnotexist ]

    set destinationFile [java::new java.io.File doesnotexist2]

    set fileExists0 [file exists doesnotexist]
    catch {java::call ptolemy.util.FileUtilities binaryCopyURLToFile \
	       $sourceURL $destinationFile} errMsg
    regsub {Exception:.*doesnot} $errMsg {Exception: ./doesnot} r2
    regsub {No such file or directory} $r2 {The system cannot find the file specified} r3
    list $fileExists0 $r3
} {0 {java.io.FileNotFoundException: ./doesnotexist (The system cannot find the file specified)}}


######################################################################
####
#
test FileUtilities-1.3 {binaryCopyURLToFile URL does not exist. sameFile } {

    set sourceURL [java::new java.net.URL file:./doesnotexist ]

    set destinationFile [java::new java.io.File doesnotexist]

    set fileExists0 [file exists doesnotexist]

    java::call ptolemy.util.FileUtilities binaryCopyURLToFile \
              	     $sourceURL $destinationFile

    list $fileExists0 $results
} {0 1}


######################################################################
####
#
test FileUtilities-1.4 {binaryCopyURLToFile same file} {

    # Depends on 1.1 above1
    set sourceURL [java::new java.net.URL file:./$tmpFile ]

    set destinationFile [java::new java.io.File $tmpFile]

    set fileExists0 [file exists doesnotexist]
    set results [java::call ptolemy.util.FileUtilities binaryCopyURLToFile \
		     $sourceURL $destinationFile]
    list $fileExists0 $results
} {0 0}


######################################################################
####
#
test FileUtilities-2.1 {nameToFile: null and "" name} {
    set file1 [java::call ptolemy.util.FileUtilities nameToFile \
	[java::null] [java::null]]
    set file2 [java::call ptolemy.util.FileUtilities nameToFile \
	"" [java::null]]
    list [java::isnull $file1] [java::isnull $file2]
} {1 1}

######################################################################
####
#
test FileUtilities-2.2 {nameToFile: open a non-absolute file with a null base} {
    set file1 [java::call ptolemy.util.FileUtilities nameToFile \
	makefile [java::null]]
    list [$file1 toString]
} {makefile}

######################################################################
####
#
test FileUtilities-2.3 {nameToFile: open a non-absolute file with a non-null base} {
    set baseURI [java::new java.net.URI .]
    set file1 [java::call ptolemy.util.FileUtilities nameToFile \
	makefile $baseURI]
    list [$file1 toString]
} {makefile}

test FileUtilities-2.5 {nameToFile: open a non-absolute file with different base} {
    set baseURI [java::new java.net.URI file:///. ]
    set file1 [java::call ptolemy.util.FileUtilities nameToFile \
	makefile $baseURI]
    list [$file1 toString]
} {makefile}

######################################################################
####
#
test FileUtilities-8.1 {nameToURL with nulls} {
    set url1 [java::call ptolemy.util.FileUtilities nameToURL \
	[java::null] [java::null] [java::null]]
    set url2 [java::call ptolemy.util.FileUtilities nameToURL \
	"" [java::null] [java::null]]
    list [java::isnull $url1] [java::isnull $url2] 
} {1 1}

######################################################################
####
#
test FileUtilities-8.2 {nameToURL} {
    set url1 [java::call ptolemy.util.FileUtilities nameToURL \
	"xxxxxxCLASSPATHxxxxxx/ptolemy/util/FileUtilities.java" \
	[java::null] [java::null]]
    set url2 [java::call ptolemy.util.FileUtilities nameToURL \
	"\$CLASSPATH/ptolemy/util/FileUtilities.java" \
	[java::null] [java::null]]	
    list [$url1 sameFile $url2]
} {1}

######################################################################
####
#
test FileUtilities-8.3 {nameToURL with a classloader} {
    set classLoader [java::call ClassLoader getSystemClassLoader]
    set url1 [java::call ptolemy.util.FileUtilities nameToURL \
	"xxxxxxCLASSPATHxxxxxx/ptolemy/util/FileUtilities.java" \
	[java::null] $classLoader]
    set url2 [java::call ptolemy.util.FileUtilities nameToURL \
	"\$CLASSPATH/ptolemy/util/FileUtilities.java" \
	[java::null] $classLoader]	
    list [$url1 sameFile $url2]
} {1}

######################################################################
####
#
test FileUtilities-8.4 {nameToURL that does not exist with no base URI} {
    set fileExists0 [file exists doesnotexist]
    # FIXME: should this throw an exception because doesnotexist is not found?
    set url1 [java::call ptolemy.util.FileUtilities nameToURL \
	    file:///doesnotexist [java::null] [java::null]]
    list $fileExists0 [$url1 toString]
} {0 file:/doesnotexist}

######################################################################
####
#
test FileUtilities-8.5 {nameToURL that does not exist with a base URI} {
    set fileExists0 [file exists doesnotexist]
    set baseURI [java::new java.net.URI .]
    # FIXME: should this throw an exception because doesnotexist is not found?
    set url1 [java::call ptolemy.util.FileUtilities nameToURL \
	file:///doesnotexist $baseURI [java::null]]
    list $fileExists0 [$url1 toString]
} {0 file:///doesnotexist}

######################################################################
####
#
test FileUtilities-8.6 {nameToURL that does not exist with a base URI} {
    set fileExists0 [file exists doesnotexist]
    set baseURI [java::new java.net.URI .]
    catch {
        set url1 [java::call ptolemy.util.FileUtilities nameToURL \
	    doesnotexist $baseURI [java::null]]
    } errMsg
    list $errMsg
} {{java.io.IOException: Problem with URI format in 'doesnotexist'. This can happen if the 'doesnotexist' is not absolute and is not present relative to the directory in which the specified model was read (which was '.')}}

######################################################################
####
#
test FileUtilities-8.7 {nameToURL try to read a local stream} {
    # FileParameter.asURL had a bug that was trigger by
    # nameToURL passing back URLs like file://c:/foo/bar
    set baseURL [java::call ptolemy.util.FileUtilities nameToURL \
		     {$CLASSPATH/ptolemy/util/test/makefile} \
		     [java::null] [java::null]]
    set baseURI [java::new java.net.URI [$baseURL toString]]
    set url1 [java::call ptolemy.util.FileUtilities nameToURL \
	makefile $baseURI [java::null]]
    set inputStream [$url1 openStream]
    list [expr {[$inputStream available] > 0}]
} {1}

