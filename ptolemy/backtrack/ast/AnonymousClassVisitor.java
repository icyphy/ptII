/* The visitor used to visit summary and fix anonymous class numbers.

Copyright (c) 1997-2004 The Regents of the University of California.
All rights reserved.
Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

PT_COPYRIGHT_VERSION_2
COPYRIGHTENDKEY

*/

package ptolemy.backtrack.ast;

import java.util.Hashtable;
import java.util.Iterator;

import org.acm.seguin.summary.MethodSummary;
import org.acm.seguin.summary.TraversalVisitor;
import org.acm.seguin.summary.TypeSummary;

//////////////////////////////////////////////////////////////////////////
//// AnonymousClassVisitor
/**
 *  A visitor to visit the AST summary created with the
 *  <a href="http://jrefactory.sourceforge.net/" target="_blank">JRefactory</a>
 *  package. It fixes the anonymous class numbers in the summary to
 *  correctly reflect the numbering scheme used in the Java compiler.
 *  <p>
 *  A summary extracts the information from an AST tree. Classes and their
 *  members found in the AST are recorded as pieces of summaries as children
 *  of the top-level summary, a {@link FileSummary} object. The summary of
 *  each class is annotated with its simple name (not including "." or "$").
 *  The name may not be unique.
 *  <p>
 *  Java supports anonymous inner classes. Those classes do not have names.
 *  Instead, Java compiler assigns a unique number to each such anonymous
 *  class in a single Java source file, starting from 1. JRefactory summary
 *  builder also tries to assign a number to each anonymous class summary,
 *  but its numbering scheme is different from the one taken by the Java
 *  compiler. This class does one single traversal on the summary hierarchy,
 *  and re-numbers the anonymous class summaries.
 * 
 *  @author Thomas Feng
 *  @version $Id$
 *  @since Ptolemy II 4.1
 *  @Pt.ProposedRating Red (tfeng)
 */
class AnonymousClassVisitor extends TraversalVisitor {

    /** Visit a {@link TypeSummary} node. A {@link TypeSummary} represents
     *  a class declaration, whether it is a named class or anonymous class.
     *  If the parent of this node is a {@link MethodSummary}, it is
     *  anonymous; otherwise, it is names.
     *  
     *  @param node The node to be visited.
     *  @param data The data object passed from the visit functions at a
     *   higher level. Not used in this function.
     *  @return The same object as <tt>data</tt>.
     */
    public Object visit(TypeSummary node, Object data) {
        if (node.getParent() instanceof MethodSummary)
            _summaryTable.put(node, new Integer(_anonymousNumber++));
        
        MethodSummary initializer = null;
        Iterator iter = node.getMethods();
        if (iter != null) {
            while (iter.hasNext()) {
                MethodSummary next = (MethodSummary) iter.next();
                if (next.getName().startsWith("***")) {
                    next.accept(this, data);
                    initializer = next;
                }
            }
        }
        
        iter = node.getTypes();
        if (iter != null) {
            while (iter.hasNext()) {
                TypeSummary next = (TypeSummary) iter.next();
                next.accept(this, data);
            }
        }
    
        iter = node.getMethods();
        if (iter != null) {
            while (iter.hasNext()) {
                MethodSummary next = (MethodSummary) iter.next();
                if (next != initializer)
                    next.accept(this, data);
            }
        }
    
        return data;
    }
    
    /** Get the correct summary number for an anonymous class summary.
     * 
     *  @param summary An anonymous class summary, which must have been
     *   visited by this visitor.
     *  @return The number of the summary.
     */
    protected int getSummaryNumber(TypeSummary summary) {
        return ((Integer)_summaryTable.get(summary)).intValue();
    }
    
    /** The current anonymous class number.
     */
    private int _anonymousNumber = 1;
    
    /** The table recording anonymous numbers for each visited {@link
     *  TypeSummary} object. Keys are {@link TypeSummary} objects;
     *  values are {@link Integer} objects.
     */
    private Hashtable _summaryTable = new Hashtable();
}
