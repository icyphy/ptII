# Test GenericCodeGenerator
#
# @Author: Christopher Brooks
#
# @Version: $Id$
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

if {[info procs sdfModel] == "" } then {
    source [file join $PTII util testsuite models.tcl]
}

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}

#####
test HTMLCodeGenerator-1.1 {Instantiate a HTMLCodeGenerator, call a few methods} {
    set model [sdfModel]
    set codeGenerator \
	    [java::new ptolemy.cg.kernel.generic.html.HTMLCodeGenerator \
	    $model "myCodeGenerator"]

    list \
	[$codeGenerator toString] \
	[$codeGenerator comment {This is a comment}] \
} {{ptolemy.cg.kernel.generic.html.HTMLCodeGenerator {.top.myCodeGenerator}} {<!-- This is a comment -->
}}

#####
test HTMLCodeGenerator-5.1 {generateCode(StringBuffer)} {
    set stringBuffer [java::new StringBuffer]
    set result [$codeGenerator {generateCode StringBuffer} $stringBuffer]
    list $result [$stringBuffer toString]
} {0 {<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>top</title>
</head>
<body>
top contains: <ul>
<li></li>
</ul>
</body>
</html>
}}


