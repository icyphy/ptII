# Tests for the BasePNDirector class
#
# @Author: Mudit Goel
#
# @Version: $Id$
#
# @Copyright (c) 1998-1999 The Regents of the University of California.
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
set manager [java::new ptolemy.actor.Manager]


######################################################################
####
#
test PNImageSource-2.1 {Constructor tests} {
    set c1 [java::new ptolemy.actor.CompositeActor]
    set a1 [java::new ptolemy.domains.pn.lib.PNImageSource $c1 "A1"]
    list [$a1 getFullName] 
} {..A1}

######################################################################
####
#
test PNImageSource-3.1 {Test fire} {
    # NOTE: Uses the setup above
    set p1 [$a1 getAttribute "Image_file"]
    $p1 setToken [java::new ptolemy.data.StringToken ptII.pbm]
    $a1 initialize
    $a1 fire
} {}

######################################################################
####
#
test PNImageSource-4.1 {Test application} {
    set a2 [java::new ptolemy.domains.pn.lib.PNImageSink $c1 "A2"]
    set r1 [$a1 getPort "output"]
    set r2 [$a2 getPort "input"]
    $c1 connect $r1 $r2
    $c1 setManager $manager
    set d1 [java::new ptolemy.domains.pn.kernel.BasePNDirector D1]
    $c1 setDirector $d1
    $manager run
} {}


