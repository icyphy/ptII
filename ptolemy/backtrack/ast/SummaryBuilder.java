/* Build summaries from an AST.

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

import net.sourceforge.jrefactory.ast.ASTAllocationExpression;
import net.sourceforge.jrefactory.ast.ASTClassBody;
import net.sourceforge.jrefactory.ast.Node;

import org.acm.seguin.summary.MethodSummary;
import org.acm.seguin.summary.Summary;
import org.acm.seguin.summary.SummaryLoadVisitor;
import org.acm.seguin.summary.SummaryLoaderState;
import org.acm.seguin.summary.TypeSummary;

//////////////////////////////////////////////////////////////////////////
//// SummaryBuilder
/**
 *  This class builds summaries from an AST. For each anonymous class
 *  summary, it records the corresponding AST node in a {@link Hashtable}.
 *  When those AST nodes are visited in {@link TypeAnalyzer}, their
 *  summaries can be easily retrieved from the table, and the correctly
 *  numbers of those anonymous classes can be retrieved from the table
 *  in {@link SummaryBuilder}.
 * 
 *  @author Thomas Feng
 *  @version $Id$
 *  @since Ptolemy II 4.1
 *  @Pt.ProposedRating Red (tfeng)
 */
public class SummaryBuilder extends SummaryLoadVisitor {
    
    /**
     *  Visit an allocation AST node. If anonymous classes are created
     *  in the allocation, they are recorded as keys in the table. The
     *  summary created by the superclass is recorded as values for
     *  those keys.
     *
     *  @param node The AST node to be visited.
     *  @param data The current state of the visitor, an instanceof {@link
     *   {@link SummaryLoaderState}.
     *  @return The return result from the superclass.
     */
    public Object visit(ASTAllocationExpression node, Object data) {
        SummaryLoaderState state = (SummaryLoaderState) data;
        MethodSummary parent = (MethodSummary) state.getCurrentSummary();
        
        Object result = super.visit(node, data);
        
        Iterator dependIter = parent.getDependencies();
        dependIter.next();  // Ignore a return type.
        int nChildren = node.jjtGetNumChildren();
        for (int i=1; i<nChildren; i++) {
            Node next = node.jjtGetChild(i);
            if (next instanceof ASTClassBody) {
                Summary summary = null;
                int repeat = 1;
                Integer hash = new Integer(parent.hashCode());
                if (_lastChildNumber.containsKey(hash))
                    repeat = ((Integer)_lastChildNumber.get(hash)).intValue();
                _lastChildNumber.put(hash, new Integer(repeat + 1));
                for (int j=0; j<repeat; j++) {
                    summary = (Summary)dependIter.next();
                    while (!(summary instanceof TypeSummary))
                        summary = (Summary)dependIter.next();
                }
                _summaryTable.put(next, summary);
            }
        }
        
        return result;
    }
    
    /** Get the summary for the anonymous class defined at the AST
     *  node.
     *  
     *  @param node The AST node that defines the anonymous class.
     *  @return The summary for the anonymous class.
     */
    protected TypeSummary getSummary(ASTClassBody node) {
        return (TypeSummary)_summaryTable.get(node);
    }
    
    /** The table recording AST nodes and corresponding summaries.
     */
    private Hashtable _summaryTable = new Hashtable();
    
    /** The positions of the last children of {@link MethodSummary}'s.
     *  Keys are {@link MethodSummary} objects; values are {@link
     *  Integer} objects.
     */
    private Hashtable _lastChildNumber = new Hashtable();
}
