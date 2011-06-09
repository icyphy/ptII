# Tests for the FileParameter class
#
# @Author: Christopher Brooks
#
# @Version $Id$
#
# @Copyright (c) 1997-2010 The Regents of the University of California.
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
test FileParameter-2.0 {Check constructor} {
    set e [java::new {ptolemy.kernel.Entity String} entity]
    set param4 [java::new ptolemy.data.expr.FileParameter $e id1]
    
    set name4 [$param4 getFullName]
    list $name4
} {.entity.id1}

######################################################################
####
# 
test FileParameter-3.0 {asFile} {
    set e [java::new {ptolemy.kernel.Entity String} entity]
    set fileParam3 [java::new ptolemy.data.expr.FileParameter $e myFileParam3]
    list [java::isnull [$fileParam3 asFile]]
} {1}

######################################################################
####
# 
test FileParameter-3.1 {asFile on $PTII} {
    set e3_1 [java::new {ptolemy.kernel.Entity String} entity3_1]
    set fileParam3_1 [java::new ptolemy.data.expr.FileParameter $e3_1 \
			  myFileParam3_1]
    $fileParam3_1 setExpression {$PTII}
    set file3_1 [$fileParam3_1 asFile]
    set uri3_1 [[$file3_1 getCanonicalFile] toURI]

    # Compare against $PTII
    set ptIIFile [java::new java.io.File $PTII]
    set ptIIURI [[$ptIIFile getCanonicalFile] toURI]

    list [$uri3_1 compareTo $ptIIURI] \
	[java::isnull [$fileParam3_1 getBaseDirectory]]
} {0 1}

######################################################################
####
# 
test FileParameter-3.2 {asFile on $CLASSPATH} {
    set e3_2 [java::new {ptolemy.kernel.Entity String} entity3_2]
    set fileParam3_2 [java::new ptolemy.data.expr.FileParameter $e3_2 \
			  myFileParam3_2]
    $fileParam3_2 setExpression {$CLASSPATH/ptolemy/data/expr/FileParameter.java}
    set file3_2 [$fileParam3_2 asFile]
    set uri3_2 [[$file3_2 getCanonicalFile] toURI]

    # Compare against $PTII
    set ptIIFile [java::new java.io.File $PTII/ptolemy/data/expr/FileParameter.java]
    set ptIIURI [[$ptIIFile getCanonicalFile] toURI]

    list [$uri3_2 compareTo $ptIIURI] \
	[java::isnull [$fileParam3_2 getBaseDirectory]]
} {0 1}

######################################################################
####
# 
test FileParameter-4.0 {asURL} {
    set e [java::new {ptolemy.kernel.Entity String} entity]
    set fileParam4 [java::new ptolemy.data.expr.FileParameter $e myFileParam4]
    list [java::isnull [$fileParam4 asURL]]
} {1}

######################################################################
####
# 
test FileParameter-4.1 {asURL on $PTII} {
    set e4_1 [java::new {ptolemy.kernel.Entity String} entity4_1]
    set fileParam4_1 [java::new ptolemy.data.expr.FileParameter $e4_1 \
			  myFileParam4_1]
    $fileParam4_1 setExpression {$PTII}
    set url4_1 [$fileParam4_1 asURL]

    set file4_1 [[java::new java.io.File [$url4_1 getFile]] getCanonicalFile]
    set ptIIFile [[java::new java.io.File $PTII] getCanonicalFile]
    
    list [$file4_1 equals $ptIIFile] \
	[java::isnull [$fileParam4_1 getBaseDirectory]]
} {1 1}

######################################################################
####
# 
test FileParameter-4.2 {asURL on a parameter w/ no protocol and no basedir} {
    set e4_2 [java::new {ptolemy.kernel.Entity String} entity4_2]
    set fileParam4_2 [java::new ptolemy.data.expr.FileParameter $e4_2 \
			  myFileParam4_2]
    $fileParam4_2 setExpression {doesnotexist}
    catch {[$fileParam4_2 asURL]} err4_2
    list $err4_2
} {{ptolemy.kernel.util.IllegalActionException: Cannot read file 'doesnotexist'
  in .entity4_2.myFileParam4_2
Because:
no protocol: doesnotexist}}

######################################################################
####
# 
test FileParameter-5.0 {clone} {
    set e5 [java::new {ptolemy.kernel.Entity String} entity5]
    set fileParam5 [java::new ptolemy.data.expr.FileParameter $e5 myFileParam5]
    set e5_0 [java::new {ptolemy.kernel.Entity String} entity5_0]
    set cloneFileParam5 [$fileParam5 clone [$e5_0 workspace]]
    # FIXME: test that the baseDirectory, reader and writer are closed
    list [$fileParam5 toString] [$cloneFileParam5 toString]
} {{ptolemy.data.expr.FileParameter {.entity5.myFileParam5} ""} {ptolemy.data.expr.FileParameter {.myFileParam5} ""}}

######################################################################
####
# 
test FileParameter-5.1 {clone: test that the baseDirectory, reader and writer are different than the source} {
    set e5_1a [java::new {ptolemy.kernel.Entity String} entity5]
    set fileParam5_1a [java::new ptolemy.data.expr.FileParameter $e5_1a \
			myFileParam5_1a]

    $fileParam5_1a setExpression {$CWD/makefile}
    set currentDirectoryFile [java::new java.io.File ./]
    set currentDirectoryURI  [[$currentDirectoryFile toURL] toURI]
    $fileParam5_1a setBaseDirectory $currentDirectoryURI

    set baseDirectory5_1a [$fileParam5_1a getBaseDirectory] 
    set reader5_1a [$fileParam5_1a openForReading]
    set writer5_1a [$fileParam5_1a openForWriting true]

    set e5_1b [java::new {ptolemy.kernel.Entity String} entity5_1b]
    set fileParam5_1b [java::cast ptolemy.data.expr.FileParameter \
			   [$fileParam5_1a clone [$e5_1b workspace]]]

    set baseDirectory5_1b [$fileParam5_1b getBaseDirectory] 
    set reader5_1b [$fileParam5_1b openForReading]
    set writer5_1b [$fileParam5_1b openForWriting true]

    # In the clone, the baseDirectory should be null,
    # reader and writer should be non-null, but different 
    list \
	[java::isnull $baseDirectory5_1a] \
	[java::isnull $reader5_1a] \
	[$reader5_1a equals $reader5_1b] \
	[java::isnull $baseDirectory5_1b] \
	[java::isnull $writer5_1a] \
	[$writer5_1a equals $writer5_1b]
} {0 0 0 1 0 0}

test FileParameter-6.0 {close on a new FileParameter} {
    set e6 [java::new {ptolemy.kernel.Entity String} entity6]
    set fileParam6 [java::new ptolemy.data.expr.FileParameter $e6 myFileParam6]
    # Sucess is not throwing an exception
    $fileParam6 close
} {}

######################################################################
####
# 
test FileParameter-7.0 {getBaseDirectory on a new FileParameter} {
    set e7 [java::new {ptolemy.kernel.Entity String} entity7]
    set fileParam7 [java::new ptolemy.data.expr.FileParameter $e7 myFileParam7]
    list [java::isnull [$fileParam7 getBaseDirectory]]
} {1}

######################################################################
####
# 
test FileParameter-7.1 {getBaseDirectory is null for simple FileParameters} {
    set e7_1 [java::new {ptolemy.kernel.Entity String} entity7_1]
    set fileParam7_1 [java::new ptolemy.data.expr.FileParameter $e7_1 \
			myFileParam7_1]
    $fileParam7_1 setExpression {$CWD/makefile}
    set reader7_1 [$fileParam7_1 openForReading]
    set r1 [$reader7_1 ready]
    set r2 [$reader7_1 readLine]
    $fileParam7_1 close
    catch {[$reader7_1 ready]} r3
    # Note that getBaseDirectory is null here
    list $r1 $r2 "\n" $r3 "\n" \
	[java::isnull [$fileParam7_1 getBaseDirectory]] \

} {1 {# Makefile for the Java classes used to test the Ptolemy parser classes} {
} {java.io.IOException: Stream closed} {
} 1}


######################################################################
####
# 
test FileParameter-7.2 {getBaseDirectory is not null if URIAttribute.getModelURI() returns something} {

    # Create a composite entity that has an attribute named "_uri"
    set top7_2 [java::new ptolemy.kernel.CompositeEntity]
    $top7_2 setName myTop7_2

    set uriAttribute [java::new ptolemy.kernel.attributes.URIAttribute \
			  $top7_2 _uri]

    set currentDirectoryFile [java::new java.io.File ./]
    set currentDirectoryURI  [[$currentDirectoryFile toURL] toURI]
    $uriAttribute setURI $currentDirectoryURI

    set e7_2 [java::new ptolemy.kernel.CompositeEntity $top7_2 e7_2]

    # Create a FileParameter w/o calling setBaseDirectory
    set fileParam7_2 [java::new ptolemy.data.expr.FileParameter $e7_2 \
			myFileParam7_2]

    $fileParam7_2 setExpression makefile
    set reader7_2 [$fileParam7_2 openForReading]

    set r1 [$reader7_2 ready]
    set r2 [$reader7_2 readLine]
    $fileParam7_2 close
    catch {[$reader7_2 ready]} r3

    # FIXME: we could do more here with checking the value of getBaseDirectory
    list $r1 $r2 "\n" $r3 \
	[java::isnull [$fileParam7_2 getBaseDirectory]]

} {1 {# Makefile for the Java classes used to test the Ptolemy parser classes} {
} {java.io.IOException: Stream closed} 0}

######################################################################
####
# 
test FileParameter-8.0 {openForReading on a new FileParameter} {
    set e8 [java::new {ptolemy.kernel.Entity String} entity8]
    set fileParam8 [java::new ptolemy.data.expr.FileParameter $e8 myFileParam8]
    # We return null because the file does not exist
    list [java::isnull [$fileParam8 openForReading]]
} {1}

######################################################################
####
# 
test FileParameter-8.1 {openForReading on something that does not exist} {
    set e8_1 [java::new {ptolemy.kernel.Entity String} entity8_1]
    set fileParam8_1 [java::new ptolemy.data.expr.FileParameter $e8_1 \
			  myFileParam8_1]
    $fileParam8_1 setExpression {doesnotexist}
    catch {[$fileParam8_1 openForReading]} err8_1
    list $err8_1
} {{ptolemy.kernel.util.IllegalActionException: Cannot open file or URL
  in .entity8_1.myFileParam8_1
Because:
no protocol: doesnotexist}}

######################################################################
####
# 
test FileParameter-9.0 {openForWriting on a new FileParameter} {
    set e9 [java::new {ptolemy.kernel.Entity String} entity9]
    set fileParam9 [java::new ptolemy.data.expr.FileParameter $e9 myFileParam9]
    # We return null because no file name has been specified
    list [java::isnull [$fileParam9 openForWriting]]
} {1}

######################################################################
####
# 
test FileParameter-10.0 {openForWriting(true)  on a new FileParameter} {
    set e10 [java::new {ptolemy.kernel.Entity String} entity10]
    set fileParam10 [java::new ptolemy.data.expr.FileParameter $e10 myFileParam10]
    # We return null because no file name has been specified
    list [java::isnull [$fileParam10 openForWriting true]]
} {1}

######################################################################
####
# 
test FileParameter-11.0 {setBaseDirectory  on a new FileParameter} {
    set e11 [java::new {ptolemy.kernel.Entity String} entity11]
    set fileParam11 [java::new ptolemy.data.expr.FileParameter $e11 myFileParam11]
    # We return null because no file name has been specified
    set uri11 [java::new java.net.URI "file:."]
    set r1 [$fileParam11 getBaseDirectory]
    $fileParam11 setBaseDirectory $uri11
    set r2 [$fileParam11 getBaseDirectory]
    list \
	[java::isnull $r1] \
	[$r2 toString]
} {1 file:.}

######################################################################
####
# 
test FileParameter-12.0 {directory with a space} {
    set directoryName "dir with space"
    file mkdir $directoryName
    set dataFileName [file join $directoryName dataFile.txt] 
    set fd [open $dataFileName w]
    puts $fd "# a file"
    puts $fd "1 + 1"
    close $fd
    set e12 [java::new {ptolemy.kernel.Entity String} entity12]
    set fileParam12 [java::new ptolemy.data.expr.FileParameter $e12 myFileParam12]

    $fileParam12 setExpression $dataFileName

    regsub { } [pwd] {%20} pwdURIPath
    $fileParam12 setBaseDirectory [java::new java.net.URI "$pwdURIPath/"]
    set file [$fileParam12 asFile]
    $file exists
} {1}
