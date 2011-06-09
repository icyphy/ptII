# Test the EmbeddedCActor
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2007 The Regents of the University of California.
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

# Run the codegenerator on file
proc testCG {file} {
    catch {set application [createAndExecute $file]} errMsg
    set args [java::new {String[]} 1 \
	  [list $file]]

    set returnValue [java::call ptolemy.codegen.kernel.CodeGenerator \
   	generateCode $args]
    list $returnValue
}

######################################################################
####
#
test EmbeddedCActor-1.1 {Don't generate code each time the model } {
    java::new ptolemy.actor.gui.MoMLSimpleApplication auto/Scale.xml
    set userHome [java::call ptolemy.util.StringUtilities getProperty user.home]
    set codegenHome "$userHome/codegen"
    set javaFileMtime [file mtime $codegenHome/ScaleEmbeddedCActor20.java]
    java::new ptolemy.actor.gui.MoMLSimpleApplication auto/Scale.xml
    if {$javaFileMtime != [file mtime $codegenHome/ScaleEmbeddedCActor20.java]} {
	error "The file $codegenHome/ScaleEmbeddedCActor0.java was updated even though the model did not change"
}
    list 1
} {1}

######################################################################
####
#
test EmbeddedCActor-2.1 {Copy a file to the codeDirectory} {

    set codeDirectory [java::call ptolemy.util.StringUtilities getProperty user.home]
    set necessaryFile1 [file join $codeDirectory codegen EmbeddedCActorFileDependency.c]
    file delete -force $necessaryFile1

    set args [java::new {String[]} 1 [list {auto/EmbeddedCActorFileDependency.xml}]]
    java::call ptolemy.codegen.kernel.CodeGenerator main $args

    list \
	[file exists $necessaryFile1] \
} {1}

