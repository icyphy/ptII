# Ptolemy II test suite definitions
#
# @Authors: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2001 The Regents of the University of California.
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


# Generate java code for a model.  The model argument names a .xml
# file which will be interpreted as a relative pathname
# (MoMLParser.parse() forces this)
proc saveAsJava {model} {
    global relativePathToPTII
    set MoMLToJava [java::new ptolemy.codegen.saveasjava.MoMLToJava]
    set javaFile [$MoMLToJava convert $model]

    # We need to get the classpath so that we can run if we are running
    # under Javascope, which includes classes in a zip file
    set builtinClasspath [java::call System getProperty "java.class.path"]
    set classpath $relativePathToPTII[java::field java.io.File pathSeparator].[java::field java.io.File pathSeparator]$builtinClasspath
    set modelName [string range $javaFile \
	    0 [expr {[string length $javaFile]-6}]]
    exec javac -classpath $relativePathToPTII $javaFile
    return [exec java -classpath $classpath ptolemy.actor.gui.CompositeActorApplication -class $modelName ]
}


# Generate code for all the xml files in a directory.
proc autoSaveAsJava {autoDirectory} {
    foreach file [glob $autoDirectory/*.xml] {
	puts "------------------ testing $file"
	test "Auto" "Automatic test in file $file" {
	    saveAsJava $file
	    list {}
	} {{}}
    }
}

