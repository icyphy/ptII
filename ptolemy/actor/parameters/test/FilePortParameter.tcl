# Tests for the FilePortParameter class
#
# @Author: Christopher Brooks, based on Parameter.tcl by Neil Smyth
#
# @Version: $Id$
#
# @Copyright (c) 2006-2008 The Regents of the University of California.
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

######################################################################
####
#
test FilePortParameter-2.0 {Check constructors} {
    set e [java::new {ptolemy.kernel.Entity String} entity]
    set tok [java::new  {ptolemy.data.DoubleToken double} 4.5]
    set ws [java::new ptolemy.kernel.util.Workspace workspace]

    #set param1 [java::new ptolemy.actor.parameters.FilePortParameter]
    #set param2 [java::new ptolemy.actor.parameters.FilePortParameter $ws]
    set param4 [java::new ptolemy.actor.parameters.FilePortParameter $e id1]
    set param3 [java::new ptolemy.actor.parameters.FilePortParameter $e id2 [java::null]]
    set param5 [java::new ptolemy.actor.parameters.FilePortParameter $e id3 $tok]
    
    #set name1 [$param1 getFullName]
    #set name2 [$param2 getFullName]    
    set name3 [$param3 getFullName]
    set name4 [$param4 getFullName]
    set name5 [$param5 getFullName]
    set value3 [java::isnull [$param3 getToken]] 
    set value5 [[$param5 getToken] toString]
    #list $name1 $name2 $name3 $name4 $value3 
    list $name3 $name4 $name5 $value3 $value5
} {.entity.id2 .entity.id1 .entity.id3 0 {"4.5"}}


######################################################################
####
#
test FilePortParameter-2.1 {Try empty string} {
    set e2_1 [java::new {ptolemy.kernel.Entity String} entity]
    set tok2_1 [java::new  {ptolemy.data.StringToken} ""]
    set param2_1 [java::new ptolemy.actor.parameters.FilePortParameter $e2_1 \
    	param2_1 $tok2_1]
    list [java::isnull [$param2_1 asFile]]
} {1}

######################################################################
####
#
test FilePortParameter-2.2 {Try null token} {
    set e2_2 [java::new {ptolemy.kernel.Entity String} entity]
    set tok2_2 [java::null]
    set param2_2 [java::new ptolemy.actor.parameters.FilePortParameter $e2_2 \
    	param2_2 $tok2_2]
    list [java::isnull [$param2_2 asFile]]
} {1}
