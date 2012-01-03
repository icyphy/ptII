# Test CodeGenerator
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2006-2009 The Regents of the University of California.
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

if {[info procs sdfModel] == "" } then {
    source [file join $PTII util testsuite models.tcl]
}

# main(String[]) is tested in
# $PTII/ptolemy/codegen/c/actor/lib/test/CodeGenerator2.tcl
# because the test relies on SDFDirector being built and actors being present.
#####
test CodeGenerator-2.1 {Call main} {
    set args [java::new {String[]} 1  auto/Ramp.xml]
    java::call ptolemy.codegen.kernel.CodeGenerator main $args
} {}

#####
test CodeGenerator-3.1 {Call main and generate code in the current directory } {
    file delete -force Ramp.c Ramp.mk 
    set args [java::new {String[]} 3 [list {-codeDirectory} {$CWD} {auto/Ramp.xml}]]
    java::call ptolemy.codegen.kernel.CodeGenerator main $args
    list [file exists Ramp.c] [file exists Ramp.mk]
} {1 1}

#####
test CodeGenerator-4.1 {Call main and copy two files to the codeDirectory because RampNecessaryFiles.c has a fileDependencies block} {
    set codeDirectory [java::call ptolemy.util.StringUtilities getProperty user.home]
    set necessaryFile1 [file join $codeDirectory codegen necessaryFile1]
    set necessaryFile2 [file join $codeDirectory codegen necessaryFile2]
    file delete -force $necessaryFile1
    file delete -force $necessaryFile2

    set args [java::new {String[]} 1 [list {auto/RampNecessaryFiles.xml}]]
    java::call ptolemy.codegen.kernel.CodeGenerator main $args

    list \
	[file exists $necessaryFile1] \
	[file exists $necessaryFile2]
} {1 1}

test CodeGenerator-5.1 {Test problem where generating code for a Pub/Sub with Classes fails on the second run} {
    # r55530 introduced this bug.
    set args [java::new {String[]} 1 [list "auto/PublisherTestSubscriber14.xml"]]

    # The fix is that CodeGenerator.generateCode() now calls purgeModelRecord
    java::call ptolemy.codegen.kernel.CodeGenerator  generateCode $args

    set application [java::new ptolemy.moml.MoMLSimpleApplication auto/PublisherTestSubscriber14.xml]
    [$application getClass] getName
} {ptolemy.moml.MoMLSimpleApplication}


test CodeGenerator-5.2 {Test problem where generating code for a Pub/Sub with Classes fails on the second run} {
    # r55530 introduced this bug.

    # It turns out that after r55530, we need to purge the model records after setting the container to null.
    # Effigy.setContainer() does something similar.

    set parser [java::new ptolemy.moml.MoMLParser]
    set modelURL [[[java::new java.io.File auto/PublisherTestSubscriber14.xml] toURI] toURL]
    set toplevel [java::cast ptolemy.actor.CompositeActor [$parser {parse java.net.URL java.net.URL} [java::null] $modelURL]]
    set manager [java::new ptolemy.actor.Manager [$toplevel workspace] "myManager"]
    $toplevel setManager $manager
    $manager preinitializeAndResolveTypes
    $toplevel setContainer [java::null]

    # Note that if we purge the ModelRecord parser between calls, then this bug goes away.
    #$parser purgeModelRecord $modelURL

    catch [set application [java::new ptolemy.moml.MoMLSimpleApplication auto/PublisherTestSubscriber14.xml]] errMsg
    [$application getClass] getName
} {ptolemy.moml.MoMLSimpleApplication} {Known Failure: after calling setContainer(null), we need to also call purgeModelRecord}

# Purge the model record now for PublisherTestSubscriber14.xml so that when the JUnit tests
# run the model in the same JVM, we don't get this error.
$parser purgeModelRecord $modelURL

java::new ptolemy.moml.MoMLSimpleApplication $PTII/ptolemy/codegen/c/actor/lib/test/auto/PublisherTestSubscriber14.xml
