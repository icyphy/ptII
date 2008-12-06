/*

 Copyright (c) 2008 The Regents of the University of California.
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
package ptolemy.actor.gt.controller;

import javax.swing.SwingUtilities;

import ptolemy.actor.Initializable;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.data.ObjectToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ObjectType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// TableauParameter

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class TableauParameter extends Parameter implements Initializable {

    public TableauParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        setTypeEquals(new ObjectType(Tableau.class));
        setToken(new ObjectToken(null, Tableau.class));
    }

    public void addInitializable(Initializable initializable) {
        throw new InternalErrorException("The addInitializable() method is " +
                "not implemented in TableauParameter, and should not be " +
                "invoked.");
    }

    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        TableauParameter newObject = (TableauParameter) super.clone(workspace);
        try {
            newObject.setToken(new ObjectToken(null, Tableau.class));
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }
        return newObject;
    }

    public String getExpression() {
        return "";
    }

    public void initialize() throws IllegalActionException {
        final Tableau tableau = (Tableau) ((ObjectToken) getToken()).getValue();
        if (tableau != null) {
            setToken(new ObjectToken(null, Tableau.class));
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    tableau.close();
                    Effigy effigy = (Effigy) tableau.getContainer();
                    if (effigy != null) {
                        try {
                            effigy.setContainer(null);
                        } catch (KernelException e) {
                            // Ignore if we can't remove the effigy from its
                            // container.
                        }
                    }
                }
            });
        }
    }

    public void preinitialize() throws IllegalActionException {
    }

    public void removeInitializable(Initializable initializable) {
    }

    public void setContainer(NamedObj container)
    throws IllegalActionException, NameDuplicationException {
        NamedObj oldContainer = getContainer();
        if (oldContainer instanceof Initializable) {
            ((Initializable) oldContainer).removeInitializable(this);
        }

        super.setContainer(container);

        if (container instanceof Initializable) {
            ((Initializable) container).addInitializable(this);
        }
    }

    public void setExpression(String expression) {
    }

    public void wrapup() throws IllegalActionException {
    }
}
