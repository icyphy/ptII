# Common Tcl Procs to Test ModularCodeGen
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2010 The Regents of the University of California.
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

# Load up the test definitions
source PublisherCommon.tcl 
# For a model the size of the Rome Model, use: 3 6
set numberOfSubsPerLevel 3
set levels 6

set composite ptolemy.actor.TypedCompositeActor
puts [modularCodeGenModel $numberOfSubsPerLevel $levels $composite true] 
puts [modularCodeGenModel $numberOfSubsPerLevel $levels $composite false] 

set composite ptolemy.actor.LazyTypedCompositeActor
puts [modularCodeGenModel $numberOfSubsPerLevel $levels $composite true] 
puts [modularCodeGenModel $numberOfSubsPerLevel $levels $composite false] 

set composite ptolemy.cg.lib.ModularCodeGenTypedCompositeActor
puts [modularCodeGenModel $numberOfSubsPerLevel $levels $composite true] 