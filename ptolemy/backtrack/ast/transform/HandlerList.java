/* List of different handlers.

Copyright (c) 2005 The Regents of the University of California.
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

package ptolemy.backtrack.ast.transform;

import java.util.LinkedList;
import java.util.List;

import ptolemy.backtrack.ast.transform.AssignmentHandler;
import ptolemy.backtrack.ast.transform.ClassHandler;

//////////////////////////////////////////////////////////////////////////
//// HandlerList
/**
    List of different handlers to be called back by {@link TypeAnalyzer}
    during the traversal of the AST.
   
    @author Thomas Feng
    @version $Id$
    @since Ptolemy II 5.1
    @Pt.ProposedRating Red (tfeng)
    @Pt.AcceptedRating Red (tfeng)
*/
public class HandlerList {
    
    /** Add an assignment handler to the list.
     * 
     *  @param handler The assignment handler.
     *  @see AssignmentHandler
     */
    public void addAssignmentHandler(AssignmentHandler handler) {
        _assignmentHandlers.add(handler);
    }
    
    /** Add a class declaration handler to the list.
     * 
     *  @param handler The class declaration handler.
     *  @see ClassHandler
     */
    public void addClassHandler(ClassHandler handler) {
        _classHandlers.add(handler);
    }
    
    /** Get the list of assignment handlers.
     * 
     *  @return The list of assignment handlers.
     */
    public List getAssignmentHandlers() {
        return _assignmentHandlers;
    }
    
    /** Get the list of class declaration handlers.
     * 
     *  @return The list of class declaration handlers.
     */
    public List getClassHandlers() {
        return _classHandlers;
    }
    
    /** Test if there is any assignment handler.
     * 
     *  @return <tt>true</tt> if there are assignment handlers.
     */
    public boolean hasAssignmentHandler() {
        return !_assignmentHandlers.isEmpty();
    }
    
    /** Test if there is any class declaration handler.
     * 
     *  @return <tt>true</tt> if there are class declaration handlers.
     */
    public boolean hasClassHandler() {
        return !_classHandlers.isEmpty();
    }
    
    /** Remove an assignment handler.
     * 
     *  @param handler The assignment handler to be removed.
     */
    public void removeAssignmentHandler(AssignmentHandler handler) {
        _assignmentHandlers.remove(handler);
    }

    /** Remove a class declaration handler.
     * 
     *  @param handler The class declaration handler to be removed.
     */
    public void removeClassHandler(ClassHandler handler) {
        _classHandlers.remove(handler);
    }

    /** The list of assignment handlers.
     */
    private List _assignmentHandlers = new LinkedList();
    
    /** The list of class declaration handlers.
     */
    private List _classHandlers = new LinkedList();
}
