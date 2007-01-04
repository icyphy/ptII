# Tests for the ParameterSet class
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
test ParameterSet-1.0 {Read in fileReadingAttribute.txt} {
    set e0 [sdfModel 5]

#    set parameterSet [java::new ptolemy.data.expr.ParameterSet $e0 "parameterSet"]

    set const [java::new ptolemy.actor.lib.Const $e0 const]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $const] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]

#     set fd [open TestParameterSet.txt w]
#     puts $fd "a=10"
#     close $fd
#     set fileOrURL [java::cast ptolemy.data.expr.FileParameter \
# 		       [$parameterSet getAttribute fileOrURL]]
#     set URL [[java::new java.io.File TestParameterSet.txt] \
# 	toURL]
#     $fileOrURL setExpression [$URL toString]

    set p [getParameter $const value]
    $p setExpression "a"


    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {}
