# Tests for the BasePNDirector class
#
# @Author: Mudit Goel
#
# @Version: $Id$
#
# @Copyright (c) 1998 The Regents of the University of California.
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
test RLEndocding-2.1 {Constructor tests} {
    set c1 [java::new ptolemy.actor.CompositeActor]
    set a1 [java::new ptolemy.domains.pn.lib.PNImageSource $c1 "A1"]
    set p1 [$a1 getAttribute "Image_file"]
    $p1 setToken [java::new ptolemy.data.StringToken ptII.pbm]    
    set a2 [java::new ptolemy.domains.pn.lib.MatrixUnpacker $c1 "A2"]
    set a3 [java::new ptolemy.domains.pn.lib.RLEncoder $c1 "A3"]
    set a4 [java::new ptolemy.domains.pn.lib.RLDecoder $c1 "A4"]
    set a5 [java::new ptolemy.domains.pn.lib.MatrixPacker $c1 "A5"]
    set a6 [java::new ptolemy.domains.pn.lib.PNImageSink $c1 "A6"]
    set p1 [$a6 getAttribute "Output_file"]
    $p1 setToken [java::new ptolemy.data.StringToken /tmp/image.pbm]    
    
    set r1 [$a1 getPort "output"]
    set r2 [$a2 getPort "input"]    
    $c1 connect $r1 $r2

    set r1 [$a2 getPort "output"]
    set r2 [$a3 getPort "input"]    
    $c1 connect $r1 $r2

    set r1 [$a2 getPort "dimensions"]
    set r2 [$a3 getPort "dimensionsIn"]    
    $c1 connect $r1 $r2

    set r1 [$a3 getPort "output"]
    set r2 [$a4 getPort "input"]    
    $c1 connect $r1 $r2

    set r1 [$a3 getPort "dimensionsOut"]
    set r2 [$a4 getPort "dimensionsIn"]    
    $c1 connect $r1 $r2

    set r1 [$a4 getPort "output"]
    set r2 [$a5 getPort "input"]    
    $c1 connect $r1 $r2

    set r1 [$a4 getPort "dimensionsOut"]
    set r2 [$a5 getPort "dimensions"]    
    $c1 connect $r1 $r2

    set r1 [$a5 getPort "output"]
    set r2 [$a6 getPort "input"]    
    $c1 connect $r1 $r2

    $c1 setManager $manager
    set d1 [java::new ptolemy.domains.pn.kernel.BasePNDirector D1]
    $c1 setDirector $d1
    $manager run
} {}


