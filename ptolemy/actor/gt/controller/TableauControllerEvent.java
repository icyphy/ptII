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

import ptolemy.actor.gui.Tableau;
import ptolemy.data.ObjectToken;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// TableauControllerEvent

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public abstract class TableauControllerEvent extends InitializableGTEvent {

    /**
     *  @param container
     *  @param name
     *  @throws IllegalActionException
     *  @throws NameDuplicationException
     */
    public TableauControllerEvent(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        referredTableau = new StringParameter(this, "referredTableau");
    }

    public StringParameter referredTableau;

    protected void _closeTableau(final Tableau tableau) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                tableau.close();
            }
        });
    }

    protected abstract TableauParameter _getDefaultTableau();

    protected Tableau _getTableau() throws IllegalActionException {
        TableauParameter parameter = _getTableauParameter();
        if (parameter == null) {
            throw new IllegalActionException("referredTableau has not been " +
                    "specified in " + getName() + ".");
        }
        Tableau tableau = (Tableau) ((ObjectToken) _getTableauParameter()
                .getToken()).getValue();
        return tableau;
    }

    protected TableauParameter _getTableauParameter()
    throws IllegalActionException {
        String tableauName = referredTableau.stringValue().trim();
        if (tableauName.equals("")) {
            return _getDefaultTableau();
        } else {
            Variable variable = ModelScope.getScopedVariable(null, this,
                    tableauName);
            if (variable == null || !(variable instanceof TableauParameter)) {
                throw new IllegalActionException(this, "Unable to find " +
                        "variable with name \"" + tableauName + "\", or the " +
                        "variable is not an instanceof TableauParameter.");
            }
            return (TableauParameter) variable;
        }
    }

    protected void _setTableau(Tableau tableau) throws IllegalActionException {
        ObjectToken token = new ObjectToken(tableau, Tableau.class);
        _getTableauParameter().setToken(token);
    }
}
