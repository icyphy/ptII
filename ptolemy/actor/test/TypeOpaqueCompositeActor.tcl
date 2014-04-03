# Tests for the TypedOpaqueCompositeActor
#
# @Author: Christopher Brooks
#
# $Id$
#
# @Copyright (c) 2005-2008 The Regents of the University of California.
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

#
#

# NOTE:  All of the following tests use this director,
# pretty much as a dummy.
set director [java::new ptolemy.actor.Director]
set manager [java::new ptolemy.actor.Manager]

######################################################################
####
#
test TypeOpaqueCompositeActor-2.1 {Constructor tests} {
    set e0 [java::new ptolemy.actor.TypeOpaqueCompositeActor]
    $e0 setManager $manager
    $e0 setDirector $director
    $e0 setName E0
    set w [java::new ptolemy.kernel.util.Workspace W]
    set e1 [java::new ptolemy.actor.TypeOpaqueCompositeActor]
    set e2 [java::new ptolemy.actor.TypeOpaqueCompositeActor $w]
    set e3 [java::new ptolemy.actor.TypeOpaqueCompositeActor $e0 E3]
    list [$e1 getFullName] [$e2 getFullName] [$e3 getFullName]
} {. . .E0.E3}

######################################################################
####
#
test TypeOpaqueCompositeActor-3.1 {newPort} {
    # Uses 2.1 above
    set port [$e3 newPort P1]
    list [[$port getContainer] getFullName] [$e3 description]
} {.E0.E3 {ptolemy.actor.TypeOpaqueCompositeActor {.E0.E3} attributes {
    {ptolemy.kernel.util.SingletonConfigurableAttribute {.E0.E3._iconDescription} attributes {
    }}
} ports {
    {ptolemy.actor.TypedIOPort {.E0.E3.P1} attributes {
        {ptolemy.data.expr.Parameter {.E0.E3.P1.defaultValue} value undefined}
    } links {
    } insidelinks {
    } configuration {{width 0}} receivers {
    } remotereceivers {
    } type {declared unknown resolved unknown}}
} classes {
} entities {
} relations {
}}}

######################################################################
####
#
test TypeOpaqueCompositeActor-3.2 {newPort with same name} {
    # Uses 2.1 above
    catch {$e3 newPort P1} msg
    list $msg
} {{ptolemy.kernel.util.NameDuplicationException: Attempt to insert object named "P1" into container named ".E0.E3", which already contains an object with that name.}}

######################################################################
####
#
test TypeOpaqueCompositeActor-3.2 {newPort with bogus name} {
    # Uses 2.1 above
    catch {$e3 newPort .} msg
    list $msg
} {{ptolemy.kernel.util.InternalErrorException:   in .E0.E3
Because:
Cannot set a name with a period: .
  in .<Unnamed Object>}}

######################################################################
####
#
test TypeOpaqueCompositeActor-3.2 {typeConstraints} {
    set typeConstraints [$e3 typeConstraints]
    $typeConstraints size
} {0}
