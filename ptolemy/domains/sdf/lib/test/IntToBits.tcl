# Test IntToBits.
#
# @Author: Michael Leung
#
# @Version: $Id$
#
# @Copyright (c) 1997-2000 The Regents of the University of California.
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

# Tycho test bed, see $TYCHO/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

######################################################################
#### Test IntToBits in an SDF model
#

test IntToBits-1.1 {test 1} {
    set e0 [sdfModel 1]
    set const [java::new ptolemy.actor.lib.Const $e0 const]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    set conver [java::new ptolemy.domains.sdf.lib.IntToBits $e0 conver]

    set value [getParameter $const value]
    $value setToken [java::new ptolemy.data.IntToken 5]

    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $const] output] \
            [java::field $conver input]
    $e0 connect \
            [java::field $conver output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]

    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]

} {false false false false false false false false false false false false false false false false false false false false false false false false false false false false false true false true}



######################################################################
#### Test IntToBits in an SDF model
#

test IntToBits-1.2 {test 2} {
    set e0 [sdfModel 1]
    set const [java::new ptolemy.actor.lib.Const $e0 const]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    set conver [java::new ptolemy.domains.sdf.lib.IntToBits \
                    $e0 conver]    

    set value [getParameter $const value]
    $value setToken [java::new ptolemy.data.IntToken 225]    
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $const] output] \
	    [java::field $conver input]
    $e0 connect \
            [java::field $conver output] \
	    [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]

    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]

} {false false false false false false false false false false false false false false false false false false false false false false false false true true true false false false false true}
