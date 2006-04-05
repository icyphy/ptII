# Tests for XMLToken
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

test XMLToken-1.0 {Construct, toString, getDomTree, getType} {
    set xmlToken [java::new ptolemy.data.XMLToken] 
    list [$xmlToken toString] \
	[java::isnull [$xmlToken getDomTree]] \
	[[$xmlToken getType] toString]
} {{} 1 xmltoken}

set xmlText {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="p1" class="ptolemy.actor.TypedCompositeActor">
</entity>}

test XMLToken-2.0 {Construct, toString, getDomTree, getType} {
    set xmlToken [java::new ptolemy.data.XMLToken $xmlText]
    set domTree [$xmlToken getDomTree]
    set doctype	[$domTree getDoctype]	
    list [[$xmlToken -noconvert toString] equals $xmlText] \
	[$doctype getPublicId]
} {1 {-//UC Berkeley//DTD MoML 1//EN}}

test XMLToken-3.0 {convert} {
    set xmlToken0 [java::new ptolemy.data.XMLToken] 
    set xmlToken [java::new ptolemy.data.XMLToken $xmlText]
    set newXMLToken [java::call ptolemy.data.XMLToken convert $xmlToken]
    set r1 [$newXMLToken equals $xmlToken]
    list $r1
} {1}

test XMLToken-3.1 {convert from non-XMLToken} {
    set token [java::new ptolemy.data.Token]
    catch {java::call ptolemy.data.XMLToken convert $token} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.Token 'present' to the type xmltoken.}}
