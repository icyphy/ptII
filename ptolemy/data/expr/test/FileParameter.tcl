# Tests for the FileParameter class
#
# @Author: Christopher Brooks
#
# @Version $Id$
#
# @Copyright (c) 1997-2006 The Regents of the University of California.
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

# 
#


######################################################################
####
# 
test FileParameter-2.0 {Check constructor} {
    set e [java::new {ptolemy.kernel.Entity String} entity]
    set param4 [java::new ptolemy.data.expr.FileParameter $e id1]
    
    set name4 [$param4 getFullName]
    list $name4
} {.entity.id1}

test FileParameter-3.0 {asFile} {
    set e [java::new {ptolemy.kernel.Entity String} entity]
    set fileParam3 [java::new ptolemy.data.expr.FileParameter $e myFileParam3]
    list [java::isnull [$fileParam3 asFile]]
} {1}

test FileParameter-4.0 {asURL} {
    set e [java::new {ptolemy.kernel.Entity String} entity]
    set fileParam4 [java::new ptolemy.data.expr.FileParameter $e myFileParam4]
    list [java::isnull [$fileParam4 asURL]]
} {1}

test FileParameter-5.0 {clone} {
    set e5 [java::new {ptolemy.kernel.Entity String} entity5]
    set fileParam5 [java::new ptolemy.data.expr.FileParameter $e5 myFileParam5]
    set e5_1 [java::new {ptolemy.kernel.Entity String} entity5_1]
    set cloneFileParam5 [$fileParam5 clone [$e5_1 workspace]]
    # FIXME: test that the baseDirectory, reader and writer are closed
    list [$fileParam5 toString] [$cloneFileParam5 toString]
} {{ptolemy.data.expr.FileParameter {.entity5.myFileParam5} ""} {ptolemy.data.expr.FileParameter {.myFileParam5} ""}}

test FileParameter-6.0 {close on a new FileParameter} {
    set e6 [java::new {ptolemy.kernel.Entity String} entity6]
    set fileParam6 [java::new ptolemy.data.expr.FileParameter $e6 myFileParam6]
    # Sucess is not throwing an exception
    $fileParam6 close
} {}

test FileParameter-7.0 {getBaseDirectory on a new FileParameter} {
    set e7 [java::new {ptolemy.kernel.Entity String} entity7]
    set fileParam7 [java::new ptolemy.data.expr.FileParameter $e7 myFileParam7]
    list [java::isnull [$fileParam7 getBaseDirectory]]
} {1}

test FileParameter-8.0 {openForReading on a new FileParameter} {
    set e8 [java::new {ptolemy.kernel.Entity String} entity8]
    set fileParam8 [java::new ptolemy.data.expr.FileParameter $e8 myFileParam8]
    # We return null because the file does not exist
    list [java::isnull [$fileParam8 openForReading]]
} {1}

test FileParameter-9.0 {openForWriting on a new FileParameter} {
    set e9 [java::new {ptolemy.kernel.Entity String} entity9]
    set fileParam9 [java::new ptolemy.data.expr.FileParameter $e9 myFileParam9]
    # We return null because no file name has been specified
    list [java::isnull [$fileParam9 openForWriting]]
} {1}

test FileParameter-10.0 {openForWriting(true)  on a new FileParameter} {
    set e10 [java::new {ptolemy.kernel.Entity String} entity10]
    set fileParam10 [java::new ptolemy.data.expr.FileParameter $e10 myFileParam10]
    # We return null because no file name has been specified
    list [java::isnull [$fileParam10 openForWriting true]]
} {1}

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