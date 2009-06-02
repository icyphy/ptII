# Tests for the DefaultModelAttribute class
#
# @Author: Christopher Brooks
#
# @Version $Id$
#
# @Copyright (c) 2009 The Regents of the University of California.
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
test DefaultModelAttribute-2.0 {Check constructor} {
    set toplevel [java::new ptolemy.kernel.CompositeEntity]
    $toplevel setName toplevel
    set pattern1 [java::new ptolemy.actor.gt.Pattern $toplevel pattern1]
    set param4 [java::new ptolemy.actor.gt.DefaultModelAttribute $pattern1 id1]
    
    set name4 [$param4 getFullName]
    list $name4 [$param4 getExpression] [$param4 getValueAsString]
} {.toplevel.pattern1.id1 {} {""}}

######################################################################
####
# 
test DefaultModelAttribute-3.0 {asFile} {
    # uses param4 from 2.0 above
    set fileParam3 [java::cast ptolemy.data.expr.FileParameter [java::field [java::cast ptolemy.actor.gt.ParameterAttribute $param4] parameter]]
    list [java::isnull [$fileParam3 asFile]]
} {1}

######################################################################
####
# 
test DefaultModelAttribute-3.1 {asFile on $PTII} {
    set e3_1 [java::new ptolemy.kernel.CompositeEntity]
    $e3_1 setName e3_1
    set pattern3_1 [java::new ptolemy.actor.gt.Pattern $e3_1 pattern3_1]
    set defaultModelAttribute3_1 [java::new ptolemy.actor.gt.DefaultModelAttribute $pattern3_1 \
			  myDefaultModelAttribute3_1]
    $defaultModelAttribute3_1 setExpression {$PTII}
    set fileParam3_1 [java::cast ptolemy.data.expr.FileParameter [java::field [java::cast ptolemy.actor.gt.ParameterAttribute $defaultModelAttribute3_1] parameter]]
    set file3_1 [$fileParam3_1 asFile]
    set uri3_1 [[$file3_1 getCanonicalFile] toURI]

    # Compare against $PTII
    set ptIIFile [java::new java.io.File $PTII]
    set ptIIURI [[$ptIIFile getCanonicalFile] toURI]

    list [$uri3_1 compareTo $ptIIURI] \
	[java::isnull [$fileParam3_1 getBaseDirectory]]
} {0 1}

######################################################################
####
# 
test DefaultModelAttribute-3.2 {asFile on $CLASSPATH} {
    set e3_2 [java::new ptolemy.kernel.CompositeEntity]
    $e3_2 setName e3_2
    set pattern3_2 [java::new ptolemy.actor.gt.Pattern $e3_2 pattern3_2]
    set defaultModelAttribute3_2 [java::new ptolemy.actor.gt.DefaultModelAttribute $pattern3_2 \
			  myDefaultModelAttribute3_2]

    $defaultModelAttribute3_2 setExpression {$CLASSPATH/ptolemy/actor/gt/test/DefaultModelAttribute.tcl}
    set fileParam3_2 [java::cast ptolemy.data.expr.FileParameter [java::field [java::cast ptolemy.actor.gt.ParameterAttribute $defaultModelAttribute3_2] parameter]]
    set url3_2a [$fileParam3_2 asURL]
    set uri3_2a [$url3_2a toURI]

    set e3_2 [java::new {ptolemy.kernel.Entity String} entity3_2]
    set fileParam3_2b [java::new ptolemy.data.expr.FileParameter $e3_2 myFileParam3_2]
    $fileParam3_2b setExpression {$CLASSPATH/ptolemy/actor/gt/test/DefaultModelAttribute.tcl}
    set url3_2b [$fileParam3_2b asURL]
    set uri3_2b [$url3_2b toURI]

    list [$uri3_2a compareTo $uri3_2b]

} {0}


######################################################################
####
# 
test DefaultModelAttribute-4.0 {asURL} {
    # uses param4 from 2.0 above
    set fileParam3 [java::cast ptolemy.data.expr.FileParameter [java::field [java::cast ptolemy.actor.gt.ParameterAttribute $param4] parameter]]
    list [java::isnull [$fileParam3 asURL]]
} {1}

######################################################################
####
# 
test DefaultModelAttribute-4.1 {asURL on $PTII} {
    set e4_1 [java::new ptolemy.kernel.CompositeEntity]
    $e4_1 setName e4_1
    set pattern4_1 [java::new ptolemy.actor.gt.Pattern $e4_1 pattern4_1]
    set defaultModelAttribute4_1 [java::new ptolemy.actor.gt.DefaultModelAttribute $pattern4_1 \
			  myDefaultModelAttribute4_1]
    $defaultModelAttribute4_1 setExpression {$PTII}
    set fileParam4_1 [java::cast ptolemy.data.expr.FileParameter [java::field [java::cast ptolemy.actor.gt.ParameterAttribute $defaultModelAttribute4_1] parameter]]
    set url4_1 [$fileParam4_1 asURL]

    set file4_1 [[java::new java.io.File [$url4_1 getFile]] getCanonicalFile]
    set ptIIFile [[java::new java.io.File $PTII] getCanonicalFile]
    
    list [$file4_1 equals $ptIIFile] \
	[java::isnull [$fileParam4_1 getBaseDirectory]]
} {1 1}

######################################################################
####
# 
test DefaultModelAttribute-5.0 {clone} {
    set e5_1 [java::new ptolemy.kernel.CompositeEntity]
    $e5_1 setName e5_1
    set pattern5_1 [java::new ptolemy.actor.gt.Pattern $e5_1 pattern5_1]
    set defaultModelAttribute5_1 [java::new ptolemy.actor.gt.DefaultModelAttribute $pattern5_1 \
			  myDefaultModelAttribute5_1]

    set fileParam5 [java::cast ptolemy.data.expr.FileParameter [java::field [java::cast ptolemy.actor.gt.ParameterAttribute $defaultModelAttribute5_1] parameter]]
    set e5_0 [java::new {ptolemy.kernel.Entity String} entity5_0]
    set cloneFileParam5 [$fileParam5 clone [$e5_0 workspace]]
    # FIXME: test that the baseDirectory, reader and writer are closed
    list [$fileParam5 toString] [$cloneFileParam5 toString]
} {{ptolemy.data.expr.FileParameter {.e5_1.pattern5_1.myDefaultModelAttribute5_1.model} ""} {ptolemy.data.expr.FileParameter {.model} ""}}


