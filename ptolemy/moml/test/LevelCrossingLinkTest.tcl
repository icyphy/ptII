# Tests for the Level Crossing Links
#
# @Author: Edward A. Lee, Contributor: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2012 The Regents of the University of California.
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

# Tests for Level Crossing Links, see https://chess.eecs.berkeley.edu/bugzilla/show_bug.cgi?id=217

if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[string compare sdfModel [info procs sdfModel]] != 0} \
        then {
    source [file join $PTII util testsuite models.tcl]
} {}

test LevelXing-1.1 {Create a model with level crossing links} {
    #  Create a model with level crossing links
    set e0 [sdfModel 5]
    $e0 allowLevelCrossingConnect true
    set e1 [java::new ptolemy.actor.TypedCompositeActor $e0 "e1"]
    $e1 allowLevelCrossingConnect true
    set ramp [java::new ptolemy.actor.lib.Ramp $e1 "ramp"]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 "rec"]

    # Create a level crossing link where the relation is in the top level
    $e0 connect \
	    [java::field [java::cast ptolemy.actor.lib.Source $ramp] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]

    [$e0 getManager] execute
    list [enumToTokenValues [$rec getRecord 0]]
} {{0 1 2 3 4}}

test LevelXing-1.2 {export the model and try to load it} {
    # Uses 1.1 above
    set moml [$e0 exportMoML]

    # See https://chess.eecs.berkeley.edu/bugzilla/show_bug.cgi?id=217

    set parser [java::new ptolemy.moml.MoMLParser]
    set top [java::cast ptolemy.actor.TypedCompositeActor [$parser parse $moml]]
    set manager [java::new ptolemy.actor.Manager [$top workspace] MyManager]
    $top setManager $manager
    $manager execute
    list [enumToTokenValues [$rec getRecord 0]]
} {{0 1 2 3 4}}


test LevelXing-2.1 {Create a model with a level crossing link where the relation is inside the inner composite} {
    #  Create a model with level crossing links
    set e2 [sdfModel 5]
    $e2 allowLevelCrossingConnect true
    set e3 [java::new ptolemy.actor.TypedCompositeActor $e2 "e3"]
    $e3 allowLevelCrossingConnect true
    set ramp [java::new ptolemy.actor.lib.Ramp $e3 "ramp"]
    set rec [java::new ptolemy.actor.lib.Recorder $e2 "rec"]

    # Instead of putting the relation in e2, we put it in e3
    $e3 connect \
	    [java::field [java::cast ptolemy.actor.lib.Source $ramp] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]

    [$e2 getManager] execute
    list [enumToTokenValues [$rec getRecord 0]]
} {{0 1 2 3 4}}

test LevelXing-2.2 {export the model and try to load it} {
    # Uses 2.1 above
    set moml [$e2 exportMoML]

    set top [java::cast ptolemy.actor.TypedCompositeActor [$parser parse $moml]]
    set manager [java::new ptolemy.actor.Manager [$top workspace] MyManager]
    $top setManager $manager
    $manager execute
    list [enumToTokenValues [$rec getRecord 0]]
} {{0 1 2 3 4}}

