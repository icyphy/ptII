# Utilities for creating models.
#
# @Author: Edward A. Lee
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

# Create a DE model with no actors in it and return it.
# The optional argument sets the stop time for the execution.
# It defaults to 1.0.
#
proc deModel {{stopTime 1.0}} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set manager [java::new ptolemy.actor.Manager]
    $e0 setName top
    $e0 setManager $manager
    set director \
            [java::new ptolemy.domains.de.kernel.DEDirector $e0 DEDirector]
    $director setStopTime $stopTime
    return $e0
}

# Get a parameter by name, properly cast to Parameter.
#
proc getParameter {namedobj paramname} {
    set p [$namedobj getAttribute $paramname]
    return [java::cast ptolemy.data.expr.Parameter $p]
}

# Create an SDF model with no actors in it and return it.
# The optional argument sets the number of iterations to be executed.
# It defaults to one.
#
proc sdfModel {{iters 1}} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set manager [java::new ptolemy.actor.Manager]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector]
    $e0 setDirector $director
    $e0 setName top
    $e0 setManager $manager

    set iterparam [getParameter $director iterations]
    $iterparam setToken [java::new ptolemy.data.IntToken $iters];

    return $e0
}
