# Tests for the Relation class
#
# @Author: Christopher Hylands, Edward A. Lee
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# If a file contains non-graphical tests, then it should be named .tcl
# If a file contains graphical tests, then it should be called .itcl
#
# It would be nice if the tests would work in a vanilla itkwish binary.
# Check for necessary classes and adjust the auto_path accordingly.
#

######################################################################
####
#
test Relation-2.1 {Construct Relations, checks numLinks on empty Relations} {
    set r1 [java::new ptolemy.kernel.Relation]
    set r2 [java::new ptolemy.kernel.Relation "My Relation"]
    list [$r1 numLinks] [$r2 numLinks]
} {0 0}

######################################################################
####
#
test Relation-3.1 {Test linkedPorts on a Relation that has no ports} {
    set r1 [java::new ptolemy.kernel.Relation]
    set enum  [$r1 linkedPorts]
    catch {$enum nextElement} errmsg
    list $errmsg [$enum hasMoreElements]
} {{java.util.NoSuchElementException: exhausted enumeration} 0}

######################################################################
####
#
test Relation-4.1 {Test linkedPorts on a Relation that has no ports} {
    set r1 [java::new ptolemy.kernel.Relation]
    set p1 [java::new ptolemy.kernel.Port]
    set enum  [$r1 linkedPorts $p1]
    catch {$enum nextElement} errmsg
    list $errmsg [$enum hasMoreElements]
} {java.util.NoSuchElementException 0}

######################################################################
####
#
test Relation-6.1 {Test a Relation with three ports} {
    set r1 [java::new ptolemy.kernel.Relation]
    set e1 [java::new ptolemy.kernel.Entity]
    set p1 [java::new ptolemy.kernel.Port $e1 P1]
    set p2 [java::new ptolemy.kernel.Port $e1 P2]
    set p3 [java::new ptolemy.kernel.Port $e1 P3]
    $p3 setContainer $e1
    $p1 link $r1
    $p2 link $r1
    $p3 link $r1
    list [$r1 numLinks] [_testRelationLinkedPorts $r1]
} {3 {{P1 P2 P3}}}

######################################################################
####
#
test Relation-7.1 {Test a Relation with one named port} {
    set r1 [java::new ptolemy.kernel.Relation "my relation"]
    set e1 [java::new ptolemy.kernel.Entity "my entity"]
    set p1 [java::new ptolemy.kernel.Port $e1 "my port"]
    $p1 link $r1
    list [$r1 numLinks] [_testRelationLinkedPorts $r1]
} {1 {{{my port}}}}

######################################################################
####
#
test Relation-8.1 {Test a Relation with two named ports} {
    set r1 [java::new ptolemy.kernel.Relation "my relation"]
    set e1 [java::new ptolemy.kernel.Entity "my entity"]
    set p1 [java::new ptolemy.kernel.Port $e1 "my port"]
    set p2 [java::new ptolemy.kernel.Port $e1 "my other port"]
    $p1 link $r1
    $p2 link $r1
    list [$r1 numLinks] [_testRelationLinkedPorts $r1]
} {2 {{{my port} {my other port}}}}

######################################################################
####
#
test Relation-11.1 {unlink a port} {
    set r1 [java::new ptolemy.kernel.Relation "my relation"]
    set e1 [java::new ptolemy.kernel.Entity "my entity"]
    set e2 [java::new ptolemy.kernel.Entity "other entity"]
    set p1 [java::new ptolemy.kernel.Port $e1 "my port"]
    set p2 [java::new ptolemy.kernel.Port $e2 "my other port"]
    $p1 link $r1
    $p2 link $r1
    $p1 unlink $r1
    list [$r1 numLinks] [_testRelationLinkedPorts $r1]
} {1 {{{my other port}}}}

######################################################################
####
#
test Relation-12.1 {unlinkAll ports} {
    set r1 [java::new ptolemy.kernel.Relation "my relation"]
    set e1 [java::new ptolemy.kernel.Entity "my entity"]
    set e2 [java::new ptolemy.kernel.Entity "other entity"]
    set p1 [java::new ptolemy.kernel.Port $e1 "my port"]
    set p2 [java::new ptolemy.kernel.Port $e2 "my other port"]
    $p1 link $r1
    $p2 link $r1
    $r1 unlinkAll
    list [$r1 numLinks] [_testRelationLinkedPorts $r1]
} {0 {{}}}

######################################################################
####
#
test Relation-13.1 {Test description} {
    set w [java::new ptolemy.kernel.util.Workspace]
    set e1 [java::new ptolemy.kernel.Entity $w E1]
    set p1 [java::new ptolemy.kernel.Port $e1 P1]
    set r1 [java::new ptolemy.kernel.Relation $w R1]
    set r2 [java::new ptolemy.kernel.Relation $w R2]
    $r1 description 7
} {ptolemy.kernel.Relation {.R1} links {
}}

test Relation-13.2 {Test description} {
    # NOTE: Builds on previous example.
    $p1 link $r1
    $p1 link $r2
    $r1 description 7
} {ptolemy.kernel.Relation {.R1} links {
    {ptolemy.kernel.Port {.E1.P1}}
}}

test Relation-13.3 {Test description} {
    # NOTE: Builds on previous example.
    $p1 description 6
} {{.E1.P1} links {
    {{.R1}}
    {{.R2}}
}}

test Relation-13.4 {Test description on workspace} {
    # NOTE: Builds on previous example.
    $w description 15
} {ptolemy.kernel.util.Workspace {} directory {
    {ptolemy.kernel.Entity {.E1} ports {
        {ptolemy.kernel.Port {.E1.P1} links {
            {ptolemy.kernel.Relation {.R1}}
            {ptolemy.kernel.Relation {.R2}}
        }}
    }}
    {ptolemy.kernel.Relation {.R1} links {
        {ptolemy.kernel.Port {.E1.P1}}
    }}
    {ptolemy.kernel.Relation {.R2} links {
        {ptolemy.kernel.Port {.E1.P1}}
    }}
}}

######################################################################
####
#
test Relation-14.1 {Test clone} {
    set w [java::new ptolemy.kernel.util.Workspace]
    set e1 [java::new ptolemy.kernel.Entity $w E1]
    set p1 [java::new ptolemy.kernel.Port $e1 P1]
    set r1 [java::new ptolemy.kernel.Relation $w R1]
    $p1 link $r1
    set r2 [java::cast ptolemy.kernel.Relation [$r1 clone]]
    list [$r1 description 7] [$r2 description 7]
} {{ptolemy.kernel.Relation {.R1} links {
    {ptolemy.kernel.Port {.E1.P1}}
}} {ptolemy.kernel.Relation {.R1} links {
}}}

######################################################################
####
#
test Relation-15.1 {Test a Relation linked twice to the same port} {
    set r1 [java::new ptolemy.kernel.Relation "my relation"]
    set e1 [java::new ptolemy.kernel.Entity "my entity"]
    set p1 [java::new ptolemy.kernel.Port $e1 "my port"]
    $p1 link $r1
    $p1 link $r1
    $r1 description [java::field ptolemy.kernel.util.NamedObj COMPLETE]
} {ptolemy.kernel.Relation {.my relation} attributes {
} links {
    {ptolemy.kernel.Port {.my entity.my port} attributes {
    }}
    {ptolemy.kernel.Port {.my entity.my port} attributes {
    }}
}}

######################################################################
####
#
test Relation-16.0 {Test exportMoML} {
    $r1 exportMoML
} {<relation name="my relation" class="ptolemy.kernel.Relation">
</relation>
}

