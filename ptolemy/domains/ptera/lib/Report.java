/* An event to report a message to the user in various forms.

 Copyright (c) 2008-2014 The Regents of the University of California.
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
package ptolemy.domains.ptera.lib;

import javax.swing.JFrame;
import javax.swing.text.BadLocationException;

import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TextEditor;
import ptolemy.actor.gui.TextEffigy;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ChoiceParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ptera.kernel.Event;
import ptolemy.gui.UndeferredGraphicalMessageHandler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.util.CancelException;
import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
//// Report

/**
 An event to report a message to the user in various forms.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Report extends Event {

    /** Construct an event with the given name contained by the specified
     *  composite entity. The container argument must not be null, or a
     *  NullPointerException will be thrown. This event will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string.
     *  Increment the version of the workspace.
     *  This constructor write-synchronizes on the workspace.
     *
     *  @param container The container.
     *  @param name The name of the state.
     *  @exception IllegalActionException If the state cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   that of an entity already in the container.
     */
    public Report(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        referredTableau = new StringParameter(this, "referredTableau");

        rowsDisplayed = new Parameter(this, "rowsDisplayed");
        rowsDisplayed.setTypeEquals(BaseType.INT);
        rowsDisplayed.setExpression("10");

        columnsDisplayed = new Parameter(this, "columnsDisplayed");
        columnsDisplayed.setTypeEquals(BaseType.INT);
        columnsDisplayed.setExpression("40");

        message = new StringParameter(this, "message");
        message.setExpression("Report from " + getName() + ".");

        mode = new ChoiceParameter(this, "mode", Mode.class);
        mode.setExpression(Mode.MESSAGE.toString());

        response = new Parameter(this, "response");
        response.setExpression("true");
        response.setVisibility(Settable.NOT_EDITABLE);
        response.setPersistent(false);

        tableau = new TableauParameter(this, "tableau");
        tableau.setPersistent(false);
        tableau.setVisibility(Settable.EXPERT);
    }

    /** Process this event. If the mode is {@link Mode#ERROR}, a message is
     *  shown in an error dialog. If the mode is {@link Mode#EXCEPTION}, a
     *  message is shown in the form of an exception. If the mode is {@link
     *  Mode#MESSAGE}, a message is shown in a message dialog. If the mode is
     *  {@link Mode#TABLEAU}, a tableau is opened to show the message. The
     *  default tableau is the one defined in the {@link #tableau} parameter.
     *  However, if {@link #referredTableau} is not an empty string, its value
     *  is interpreted as the name of the tableau parameter in the model, whose
     *  tableau should be used instead of the default one. If the mode is {@link
     *  Mode#WARNING}, a message is shown in a warning dialog. If the mode is
     *  {@link Mode#YES_OR_NO}, a query dialog is shown with the message, which
     *  allows the user to answer with yes or no. The answer is stored in {@link
     *  #response}.
     *
     *  @param arguments The arguments used to process this event, which must be
     *   either an ArrayToken or a RecordToken.
     *  @return A refiring data structure that contains a non-negative double
     *   number if refire() should be called after that amount of model time, or
     *   null if refire() need not be called.
     *  @exception IllegalActionException If the tableau cannot be used, or if
     *   thrown by the superclass.
     */
    @Override
    public RefiringData fire(Token arguments) throws IllegalActionException {
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
            Effigy effigy = EventUtils.findToplevelEffigy(this);
            if (effigy == null) {
                // The effigy may be null if the model is closed.
                return data;
            }

            Tableau tableau = EventUtils.getTableau(this, referredTableau,
                    this.tableau);
            if (tableau != null && !(tableau.getFrame() instanceof TextEditor)) {
                EventUtils
                .setTableau(this, referredTableau, this.tableau, null);
                EventUtils.closeTableau(tableau);
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
                    throw new IllegalActionException(this, e, "Unable to "
                            + "create effigy.");
                }
                try {
                    tableau = new Tableau(textEffigy, "tableau");
                } catch (NameDuplicationException e) {
                    throw new IllegalActionException(this, e, "Unable to "
                            + "create tableau.");
                }
                frame = new TextEditor(tableau.getTitle(),
                        textEffigy.getDocument());
                frame.text.setColumns(((IntToken) columnsDisplayed.getToken())
                        .intValue());
                frame.text.setRows(((IntToken) rowsDisplayed.getToken())
                        .intValue());
                tableau.setFrame(frame);
                frame.setTableau(tableau);
                EventUtils.setTableau(this, referredTableau, this.tableau,
                        tableau);
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
            throw new IllegalActionException("Unrecognized mode choice \""
                    + mode.getExpression() + "\".");
        }

        return data;
    }

    /** The horizontal size of the display, in columns. This contains
     *  an integer, and defaults to 40.
     */
    public Parameter columnsDisplayed;

    /** The message to be displayed.
     */
    public StringParameter message;

    /** The display mode.
     */
    public ChoiceParameter mode;

    /** The tableau parameter referred to, or an empty string.
     */
    public StringParameter referredTableau;

    /** The last received yes-or-no response.
     */
    public Parameter response;

    /** The vertical size of the display, in rows. This contains an integer, and
        defaults to 10. */
    public Parameter rowsDisplayed;

    /** The default tableau.
     */
    public TableauParameter tableau;

    ///////////////////////////////////////////////////////////////////
    //// Mode

    /**
     The display modes.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 8.0
     @Pt.ProposedRating Yellow (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    public enum Mode {
        /** Show in an error dialog.
         */
        ERROR {
            @Override
            public String toString() {
                return "error";
            }
        },
        /** Show in an exception dialog.
         */
        EXCEPTION {
            @Override
            public String toString() {
                return "runtime exception";
            }
        },
        /** Show in a message dialog.
         */
        MESSAGE {
            @Override
            public String toString() {
                return "message";
            }
        },
        /** Show in a tableau.
         */
        TABLEAU {
            @Override
            public String toString() {
                return "tableau";
            }
        },
        /** Show in a warning dialog.
         */
        WARNING {
            @Override
            public String toString() {
                return "warning";
            }
        },
        /** Show in a query.
         */
        YES_OR_NO {
            @Override
            public String toString() {
                return "yes or no";
            }
        }
    }

    /** The message handler for the dialogs.
     */
    private static final MessageHandler _MESSAGE_HANDLER = new UndeferredGraphicalMessageHandler();
}
