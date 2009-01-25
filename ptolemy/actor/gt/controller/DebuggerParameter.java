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

import java.util.LinkedList;

import javax.swing.text.BadLocationException;

import ptolemy.actor.TypedActor;
import ptolemy.actor.gt.GTEntityUtils;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TextEditor;
import ptolemy.actor.gui.TextEffigy;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.erg.kernel.ERGController;
import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.DebugListener;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// DebuggerParameter

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class DebuggerParameter extends TableauParameter
        implements DebugListener {

    public DebuggerParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        rowsDisplayed = new Parameter(this, "rowsDisplayed");
        rowsDisplayed.setTypeEquals(BaseType.INT);
        rowsDisplayed.setExpression("10");

        columnsDisplayed = new Parameter(this, "columnsDisplayed");
        columnsDisplayed.setTypeEquals(BaseType.INT);
        columnsDisplayed.setExpression("70");

        hierarchical = new Parameter(this, "hierarchical");
        hierarchical.setTypeEquals(BaseType.BOOLEAN);
        hierarchical.setExpression("true");
    }

    /** React to the given event.
     *  @param event The event.
     */
    public void event(DebugEvent event) {
        NamedObj container = getContainer();
        message(((GTDebugEvent) event).toString(container));
    }

    public void initialize() throws IllegalActionException {
        super.initialize();
        _registerDebugListener(true);
    }

    /** React to a debug message.
     *  @param message The debug message.
     */
    public void message(String message) {
        try {
            Tableau tableau = (Tableau) ((ObjectToken) getToken()).getValue();

            if (tableau == null) {
                tableau = _createTableau();
            }

            if (tableau != null) {
                if (!tableau.getFrame().isVisible()) {
                    tableau.getFrame().setVisible(true);
                }
                TextEditor frame = (TextEditor) tableau.getFrame();
                frame.text.append(message + "\n");
                try {
                    int lineOffset = frame.text.getLineStartOffset(frame.text
                            .getLineCount() - 1);
                    frame.text.setCaretPosition(lineOffset);
                } catch (BadLocationException ex) {
                    // Ignore ... worst case is that the scrollbar doesn't move.
                }
            }
        } catch (Throwable e) {
            throw new InternalErrorException(this, e, "Unable to report " +
                    "message \"" + message + "\".");
        }
    }

    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _registerDebugListener(false);
    }

    /** The horizontal size of the display, in columns. This contains
     *  an integer, and defaults to 40.
     */
    public Parameter columnsDisplayed;

    public Parameter hierarchical;

    /** The vertical size of the display, in rows. This contains an integer, and
     *  defaults to 10.
     */
    public Parameter rowsDisplayed;

    private Tableau _createTableau() throws IllegalActionException {
        Effigy effigy = GTEntityUtils.findToplevelEffigy(this);
        TextEffigy textEffigy;
        try {
            textEffigy = TextEffigy.newTextEffigy(effigy, "");
        } catch (Exception e) {
            throw new IllegalActionException(this, e, "Unable to create " +
                    "effigy.");
        }
        Tableau tableau;
        try {
            tableau = new Tableau(textEffigy, "tableau");
        } catch (NameDuplicationException e) {
            throw new IllegalActionException(this, e, "Unable to create " +
                    "tableau.");
        }
        TextEditor frame = new TextEditor(tableau.getTitle(),
                textEffigy.getDocument());
        frame.text.setColumns(((IntToken) columnsDisplayed.getToken())
                .intValue());
        frame.text.setRows(((IntToken) rowsDisplayed.getToken()).intValue());
        tableau.setFrame(frame);
        frame.setTableau(tableau);
        setToken(new ObjectToken(tableau, Tableau.class));
        frame.pack();
        frame.setVisible(true);
        return tableau;
    }

    private void _registerDebugListener(boolean register)
    throws IllegalActionException {
        NamedObj container = getContainer();
        boolean hierarchical = ((BooleanToken) this.hierarchical.getToken())
                .booleanValue();
        if (container instanceof ERGController) {
            LinkedList<ERGController> controllers =
                new LinkedList<ERGController>();
            controllers.add((ERGController) container);
            while (!controllers.isEmpty()) {
                ERGController controller = controllers.removeFirst();
                for (Object entity : controller.deepEntityList()) {
                    if (entity instanceof GTEvent) {
                        GTEvent event = (GTEvent) entity;
                        if (register) {
                            event.addDebugListener(this);
                        } else {
                            event.removeDebugListener(this);
                        }
                        if (!hierarchical) {
                            continue;
                        }
                        TypedActor[] refinements = event.getRefinement();
                        if (refinements != null) {
                            for (TypedActor refinement : refinements) {
                                if (refinement instanceof ERGController) {
                                    controllers.add((ERGController) refinement);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
