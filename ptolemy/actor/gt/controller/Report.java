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

import javax.swing.JFrame;
import javax.swing.text.BadLocationException;

import ptolemy.actor.gt.GTEntityUtils;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TextEditor;
import ptolemy.actor.gui.TextEffigy;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.ChoiceParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.gui.UndeferredGraphicalMessageHandler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.util.CancelException;
import ptolemy.util.MessageHandler;

//////////////////////////////////////////////////////////////////////////
//// Report

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Report extends TableauControllerEvent {

    /**
     *  @param container
     *  @param name
     *  @throws IllegalActionException
     *  @throws NameDuplicationException
     */
    public Report(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        rowsDisplayed = new Parameter(this, "rowsDisplayed");
        rowsDisplayed.setTypeEquals(BaseType.INT);
        rowsDisplayed.setExpression("10");

        columnsDisplayed = new Parameter(this, "columnsDisplayed");
        columnsDisplayed.setTypeEquals(BaseType.INT);
        columnsDisplayed.setExpression("40");

        message = new StringParameter(this, "message");
        message.setExpression("Report from " + getName() + ".");

        mode = new ChoiceParameter(this, "mode", Mode.class);
        mode.setExpression(Mode.TABLEAU.toString());

        response = new Parameter(this, "response");
        response.setExpression("true");
        response.setVisibility(Settable.NOT_EDITABLE);
        response.setPersistent(false);

        tableau = new TableauParameter(this, "tableau");
        tableau.setPersistent(false);
        tableau.setVisibility(Settable.EXPERT);
    }

    public RefiringData fire(ArrayToken arguments)
        throws IllegalActionException {
        RefiringData data = super.fire(arguments);

        Mode choice = (Mode) mode.getChosenValue();
        String text = message.stringValue();
        MessageHandler oldHandler;
        switch (choice) {
        case ERROR:
            oldHandler = MessageHandler.getMessageHandler();
            try {
                MessageHandler.setMessageHandler(_MESSAGE_HANDLER);
                MessageHandler.error(text);
            } finally {
                MessageHandler.setMessageHandler(oldHandler);
            }
            break;
        case MESSAGE:
            oldHandler = MessageHandler.getMessageHandler();
            try {
                MessageHandler.setMessageHandler(_MESSAGE_HANDLER);
                MessageHandler.message(text);
            } finally {
                MessageHandler.setMessageHandler(oldHandler);
            }
            break;
        case EXCEPTION:
            throw new RuntimeException(text);
        case TABLEAU:
            Effigy effigy = GTEntityUtils.findToplevelEffigy(this);
            if (effigy == null) {
                // The effigy may be null if the model is closed.
                return data;
            }

            Tableau tableau = _getTableau();
            if (tableau != null &&
                    !(tableau.getFrame() instanceof TextEditor)) {
                _setTableau(null);
                _closeTableau(tableau);
                tableau = null;
            }

            boolean openNewWindow = true;
            String previousText = null;
            if (tableau != null) {
                JFrame frame = tableau.getFrame();
                if (frame instanceof TextEditor) {
                    TextEditor editor = (TextEditor) frame;
                    if (editor.getEffigy() == null) {
                        previousText = editor.text.getText();
                    } else {
                        openNewWindow = false;
                    }
                }
            }

            TextEditor frame;
            if (openNewWindow) {
                TextEffigy textEffigy;
                try {
                    textEffigy = TextEffigy.newTextEffigy(effigy, "");
                } catch (Exception e) {
                    throw new IllegalActionException(this, e, "Unable to " +
                            "create effigy.");
                }
                try {
                    tableau = new Tableau(textEffigy, "tableau");
                } catch (NameDuplicationException e) {
                    throw new IllegalActionException(this, e, "Unable to " +
                            "create tableau.");
                }
                frame = new TextEditor(tableau.getTitle(),
                        textEffigy.getDocument());
                frame.text.setColumns(((IntToken) columnsDisplayed.getToken())
                        .intValue());
                frame.text.setRows(((IntToken) rowsDisplayed.getToken())
                        .intValue());
                tableau.setFrame(frame);
                frame.setTableau(tableau);
                _setTableau(tableau);
                frame.pack();
                frame.setVisible(true);
                if (previousText != null) {
                    frame.text.setText(previousText);
                }
            } else {
                frame = (TextEditor) tableau.getFrame();
            }
            frame.text.append(text + "\n");
            try {
                int lineOffset = frame.text.getLineStartOffset(frame.text
                        .getLineCount() - 1);
                frame.text.setCaretPosition(lineOffset);
            } catch (BadLocationException ex) {
                // Ignore ... worst case is that the scrollbar
                // doesn't move.
            }
            break;
        case WARNING:
            try {
                oldHandler = MessageHandler.getMessageHandler();
                try {
                    MessageHandler.setMessageHandler(_MESSAGE_HANDLER);
                    MessageHandler.warning(text);
                } finally {
                    MessageHandler.setMessageHandler(oldHandler);
                }
                response.setToken(BooleanToken.TRUE);
            } catch (CancelException e) {
                response.setToken(BooleanToken.FALSE);
            }
            break;
        case YES_OR_NO:
            oldHandler = MessageHandler.getMessageHandler();
            boolean success = false;
            boolean answer;
            try {
                MessageHandler.setMessageHandler(_MESSAGE_HANDLER);
                answer = MessageHandler.yesNoQuestion(text);
                success = true;
            } finally {
                MessageHandler.setMessageHandler(oldHandler);
            }
            if (success) {
                response.setToken(BooleanToken.getInstance(answer));
            }
            break;
        default:
            throw new IllegalActionException("Unrecognized mode choice \"" +
                    mode.getExpression() + "\".");
        }

        return data;
    }

    /** The horizontal size of the display, in columns. This contains
     *  an integer, and defaults to 40.
     */
    public Parameter columnsDisplayed;

    public StringParameter message;

    public ChoiceParameter mode;

    public Parameter response;

    /** The vertical size of the display, in rows. This contains an integer, and
        defaults to 10. */
    public Parameter rowsDisplayed;

    public TableauParameter tableau;

    public enum Mode {
        ERROR {
            public String toString() {
                return "error";
            }
        },
        EXCEPTION {
            public String toString() {
                return "runtime exception";
            }
        },
        MESSAGE {
            public String toString() {
                return "message";
            }
        },
        TABLEAU {
            public String toString() {
                return "tableau";
            }
        },
        WARNING {
            public String toString() {
                return "warning";
            }
        },
        YES_OR_NO {
            public String toString() {
                return "yes or no";
            }
        }
    }

    protected TableauParameter _getDefaultTableau() {
        return tableau;
    }

    private static final MessageHandler _MESSAGE_HANDLER =
        new UndeferredGraphicalMessageHandler();
}
