# Tests for the SDFCodeGenerator class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2000-2001 The Regents of the University of California.
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

# Ptolemy II bed, see /users/cxh/ptII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs enumToObjects] == "" } then {
     source enums.tcl
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

proc cg_generate {systemClass} {
    global PTII
    if {[expr { ![file exists "$systemClass.class"] || \
	    [file mtime "$systemClass.class"] < \
	    [file mtime "$systemClass.java"] } ] } {
	puts "Running make $systemClass.class"
	exec make $systemClass.class
    }
    puts "Removing [file join $PTII cg $systemClass]"
    file delete -force [file join $PTII cg $systemClass]

    set args [java::new {String[]} {8} [list \
	    "-class" "ptolemy.domains.sdf.codegen.test.$systemClass" \
	    "-iterations" "10" \
	    "-outdir" $PTII \
	    "-outpkg" "cg.$systemClass" \
	    ]]
    puts [$args getrange]
    set sdfCodeGenerator \
	    [java::new ptolemy.domains.sdf.codegen.SDFCodeGenerator]
    $sdfCodeGenerator processArgs $args
    $sdfCodeGenerator generateCode

    set results {}
    set currentDirectory [pwd]
    cd $PTII/cg/$systemClass
    exec javac -classpath ../.. CG_Main.java
    set result [exec java -classpath ../.. cg.$systemClass.CG_Main]
    cd $currentDirectory
    return $result
}

######################################################################
####
#
test SDFCodeGenerator-2.1 {Compile and run the RampSystem test} {
    set result [cg_generate RampSystem]
    lrange $result 0 9
} {2 4 6 8 10 12 14 16 18 20}

test SDFCodeGenerator-3.1 {Compile and run the RampArraySystem test} {
    set result [cg_generate RampArraySystem]
    lrange $result 0 9
} {{0.0, 0.1} {1.0, 2.0} {2.0, 4.0} {3.0, 6.0} {4.0, 8.0} {5.0, 10.0} {6.0, 12.0} {7.0, 14.0} {8.0, 16.0} {9.0, 18.0}}

test SDFCodeGenerator-4.1 {Compile and run the IntDoubleSystem test} {
    set result [cg_generate IntDoubleSystem]
    lrange $result 0 9
} {1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0 10.0}

test SDFCodeGenerator-5.1 {Compile and run the DotProductSystem test} {
    set result [cg_generate DotProductSystem]
    lrange $result 0 9
} {0 1 2 3 4 5 6 7 8 9}  {Known Failure: Dot Product does not work yet} 
