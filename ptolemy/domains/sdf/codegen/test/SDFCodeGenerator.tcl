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

######################################################################
####
#
test SDFCodeGenerator-2.1 {Constructor tests} {
    if {[expr { ![file exists "RampSystem.class"] || \
	    [file mtime "RampSystem.class"] < \
	    [file mtime "RampSystem.java"] } ] } {
	puts "Running make RampSystem.class"
	exec make RampSystem.class
    }
    puts "Removing [file join $PTII cg RampSystem]"
    file delete -force [file join $PTII cg RampSystem]

    set args [java::new {String[]} {8} [list \
	    "-class" "ptolemy.domains.sdf.codegen.test.RampSystem" \
	    "-iterations" "50" \
	    "-outdir" $PTII \
	    "-outpkg" "cg.RampSystem" \
	    ]]
    puts [$args getrange]
    set sdfCodeGenerator \
	    [java::new ptolemy.domains.sdf.codegen.SDFCodeGenerator]
    $sdfCodeGenerator processArgs $args
    $sdfCodeGenerator generateCode
} {}

######################################################################
####
#
test SDFCodeGenerator-2.2 {Compile and run the ramp test} {
    # Note uses setup from SDFCodeGenerator-1.1
    set results {}
    set currentDirectory [pwd]
    cd $PTII/cg/RampSystem
    exec javac -classpath ../.. CG_Main.java
    set result [exec java -classpath ../.. cg.RampSystem.CG_Main]
    cd $currentDirectory
    lrange $result 0 49
} {0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49}
