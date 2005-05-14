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

package ptolemy.backtrack.plugin.compatibility;

import java.lang.reflect.Method;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

//////////////////////////////////////////////////////////////////////////
//// RulerToggleBreakpointActionDelegate
/**


 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class RulerToggleBreakpointActionDelegate implements IEditorActionDelegate {

    public RulerToggleBreakpointActionDelegate() {
        for (int i = 0; i < DELEGATE_CLASSES.length; i++) {
            try {
                _delegateClass = Class.forName(DELEGATE_CLASSES[i]);
                break;
            } catch (Exception e) {
            }
        }
        
        if (_delegateClass != null)
            try {
                _realDelegate = _delegateClass.newInstance();
                _runMethod =
                    _delegateClass.getMethod("run", new Class[] {
                            IAction.class
                    });
                _selectionChangedMethod =
                    _delegateClass.getMethod("selectionChanged", new Class[] {
                            IAction.class, ISelection.class
                    });
                _setActiveEditorMethod =
                    _delegateClass.getMethod("setActiveEditor", new Class[] {
                            IAction.class, IEditorPart.class
                    });
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
    
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        try {
            _setActiveEditorMethod.invoke(_realDelegate, new Object[] {
                    action, targetEditor
            });
        } catch (Exception e) {
        }
    }
    
    public void run(IAction action) {
        try {
            _runMethod.invoke(_realDelegate, new Object[] {
                    action
            });
        } catch (Exception e) {
        }
    }
    
    public void selectionChanged(IAction action, ISelection selection) {
        try {
            _selectionChangedMethod.invoke(_realDelegate, new Object[] {
                    action, selection
            });
        } catch (Exception e) {
        }
    }

    private Object _realDelegate;
    
    private Class _delegateClass;
    
    private Method _setActiveEditorMethod;
    
    private Method _runMethod;
    
    private Method _selectionChangedMethod;
    
    public static final String[] DELEGATE_CLASSES = new String[] {
        /* Eclipse 3.1 */
        "org.eclipse.debug.ui.actions.RulerToggleBreakpointActionDelegate",
        /* Eclipse 3.0 */
        "org.eclipse.jdt.internal.debug.ui.actions.ManageBreakpointRulerActionDelegate"
    };
}
