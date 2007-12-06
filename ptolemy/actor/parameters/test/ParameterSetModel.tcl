# Tests for the ParameterSet class
#
# @Author: Christopher Brooks
#
# @Version $Id$
#
# @Copyright (c) 2006-2007 The Regents of the University of California.
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
test ParameterSetModel-1.0 {Change the contents of the file read by ParameterSet.  The new value should be re-read when the model is re-run} {
    set e0 [sdfModel 5]

    set parameterSet [java::new ptolemy.actor.parameters.ParameterSet $e0 "parameterSet"]

    set const [java::new ptolemy.actor.lib.Const $e0 const]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $const] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]

    set fd [open TestParameterSetModel.txt w]
    puts $fd "a=11"
    close $fd
    set fileOrURL [java::cast ptolemy.data.expr.FileParameter \
 		       [$parameterSet getAttribute fileOrURL]]
     set URL [[java::new java.io.File TestParameterSetModel.txt] \
 	toURL]
     $fileOrURL setExpression [$URL toString]

    $parameterSet read

    set p [getParameter $const value]
    $p setExpression "a"

    [$e0 getManager] execute
    set r1 [enumToTokenValues [$rec getRecord 0]]

    set fd [open TestParameterSetModel.txt w]
    puts $fd "a=42"
    close $fd

    # Should not have to call read here
    #$parameterSet read

    [$e0 getManager] execute
    set r2 [enumToTokenValues [$rec getRecord 0]]

    file delete -force TestParameterSetMode.txt
    list $r1 $r2	
} {{11 11 11 11 11} {42 42 42 42 42}}
