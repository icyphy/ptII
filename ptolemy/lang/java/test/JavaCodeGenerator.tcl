# Tests for the JavaCodeGenerator
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2000-2001 The Regents of the University of California.
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
test JavaCodeGenerator-1.1 {Generate code for a class} {
    set compileUnitNode \
	    [java::call ptolemy.lang.java.JavaParserManip \
	    parseCanonicalFileName "../nodetypes/IntLitNode.java" 0 ]
    set javaCodeGenerator \
	    [java::new ptolemy.lang.java.JavaCodeGenerator] 
    $compileUnitNode accept $javaCodeGenerator
} {package ptolemy.lang.java.nodetypes;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import ptolemy.lang.IVisitor;
import ptolemy.lang.ITreeNode;
import ptolemy.lang.TreeNode;
import ptolemy.lang.java.JavaVisitor;

public class IntLitNode extends LiteralNode {
public IntLitNode(String literal) {
super(literal);
_childList.trimToSize();
}

public final int classID() {
return NodeClassID.INTLITNODE_ID;
}

protected final Object _acceptHere(IVisitor visitor, LinkedList args) {
return ((JavaVisitor) visitor).visitIntLitNode(this, args);
}

}

}

test JavaCodeGenerator-2.1 {Generate code for a class, then generate again and compare} {
    set compileUnitNode \
	    [java::call ptolemy.lang.java.JavaParserManip \
	    parseCanonicalFileName "../nodetypes/IntLitNode.java" 0 ]
    set javaCodeGenerator \
	    [java::new ptolemy.lang.java.JavaCodeGenerator] 
    set run1 [$compileUnitNode accept $javaCodeGenerator]
    set fd [open "JavaCodeGeneratorTest.java" "w"]
    puts $fd $run1 
    close $fd

    # Now read in what we just generated
    set compileUnitNode2 \
	    [java::call ptolemy.lang.java.JavaParserManip \
	    parseCanonicalFileName "JavaCodeGeneratorTest.java" 0 ]
    set javaCodeGenerator2 \
	    [java::new ptolemy.lang.java.JavaCodeGenerator] 
    set run2 [$compileUnitNode2 accept $javaCodeGenerator2]
    list [string length $run1] \
	    [string length $run2] \
	    [expr {$run1 == $run2}]
} {577 577 1}



test JavaCodeGenerator-3.1 {Generate code for a class using reflection} {
    set compileUnitNode \
	    [java::call ptolemy.lang.java.JavaParserManip \
	    parseCanonicalClassName "ptolemy.lang.java.nodetypes.IntLitNode" 0 ]
    set javaCodeGenerator \
	    [java::new ptolemy.lang.java.JavaCodeGenerator] 
    set run1 [$compileUnitNode accept $javaCodeGenerator]
} {package ptolemy.lang.java.nodetypes;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import ptolemy.lang.IVisitor;
import ptolemy.lang.ITreeNode;
import ptolemy.lang.TreeNode;
import ptolemy.lang.java.JavaVisitor;

public class IntLitNode extends LiteralNode {
public IntLitNode(String literal) {
super(literal);
_childList.trimToSize();
}

public final int classID() {
return NodeClassID.INTLITNODE_ID;
}

protected final Object _acceptHere(IVisitor visitor, LinkedList args) {
return ((JavaVisitor) visitor).visitIntLitNode(this, args);
}

}

}

test JavaCodeGenerator-3.2 {Generate code java.lang.Object using reflection} {
    # Note that this tests the case where the superclass is null
    set compileUnitNode \
	    [java::call ptolemy.lang.java.JavaParserManip \
	    parseCanonicalClassName "java.lang.Object" 0 ]
    set javaCodeGenerator \
	    [java::new ptolemy.lang.java.JavaCodeGenerator] 
    set run1 [$compileUnitNode accept $javaCodeGenerator]
    # Use lsort to handle platform dependencies
    list [lrange $run1 0 4] [lsort [split [lindex $run1 5] "\n"]]
} {{package {java.lang;} public class java.lang.Object} {{} {} {private native static void registerNatives();} {protected native java.lang.Object clone() throws java.lang.CloneNotSupportedException;} {protected void finalize() throws java.lang.Throwable;} public\ Object()\ \{ {public boolean equals(public java.lang.Object );} {public final native java.lang.Class getClass();} {public final native void notify();} {public final native void notifyAll();} {public final native void wait(public abstract final long ) throws java.lang.InterruptedException;} {public final void wait() throws java.lang.InterruptedException;} {public final void wait(public abstract final long , public abstract final int ) throws java.lang.InterruptedException;} {public java.lang.String toString();} {public native int hashCode();} {super();} \}}}
