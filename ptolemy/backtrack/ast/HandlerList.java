/* 

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

package ptolemy.backtrack.ast;

import java.util.LinkedList;
import java.util.List;

import ptolemy.backtrack.ast.transform.AssignmentHandler;
import ptolemy.backtrack.ast.transform.ClassHandler;

//////////////////////////////////////////////////////////////////////////
//// HandlerList
/**
 *  
 * 
 *  @author Thomas Feng
 *  @version $Id$
 *  @since Ptolemy II 4.1
 *  @Pt.ProposedRating Red (tfeng)
 */
public class HandlerList {
    
    public void addAssignmentHandler(AssignmentHandler handler) {
        _assignmentHandlers.add(handler);
    }
    
    public void addClassHandler(ClassHandler handler) {
        _classHandlers.add(handler);
    }
    
    public List getAssignmentHandlers() {
        return _assignmentHandlers;
    }
    
    public List getClassHandlers() {
        return _classHandlers;
    }
    
    public boolean hasAssignmentHandler() {
        return !_assignmentHandlers.isEmpty();
    }
    
    public boolean hasClassHandler() {
        return !_classHandlers.isEmpty();
    }
    
    public void removeAssignmentHandler(AssignmentHandler handler) {
        _assignmentHandlers.remove(handler);
    }

    public void removeClassHandler(ClassHandler handler) {
        _classHandlers.remove(handler);
    }

    private List _assignmentHandlers = new LinkedList();
    
    private List _classHandlers = new LinkedList();
}
