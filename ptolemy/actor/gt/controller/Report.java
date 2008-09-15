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

import ptolemy.actor.gt.ChoiceParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.gui.GraphicalMessageHandler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.util.CancelException;

//////////////////////////////////////////////////////////////////////////
//// Report

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Report extends GTEvent {

    /**
     *  @param container
     *  @param name
     *  @throws IllegalActionException
     *  @throws NameDuplicationException
     */
    public Report(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        message = new StringParameter(this, "message");
        message.setExpression("Report from " + getName() + ".");

        mode = new ChoiceParameter(this, "mode", Mode.class);

        response = new Parameter(this, "response");
        response.setExpression("true");
        response.setVisibility(Settable.NOT_EDITABLE);
        response.setPersistent(false);
    }

    public RefiringData fire(ArrayToken arguments) throws IllegalActionException {
        RefiringData data = super.fire(arguments);

        Mode choice = (Mode) mode.getChosenValue();
        switch (choice) {
        case ERROR:
            GraphicalMessageHandler.error(message.stringValue());
            break;
        case MESSAGE:
            GraphicalMessageHandler.message(message.stringValue());
            break;
        case WARNING:
            try {
                GraphicalMessageHandler.warning(message.stringValue());
                response.setToken(BooleanToken.TRUE);
            } catch (CancelException e) {
                response.setToken(BooleanToken.FALSE);
            }
            break;
        case YES_OR_NO:
            response.setToken(BooleanToken.getInstance(GraphicalMessageHandler
                    .yesNoQuestion(message.stringValue())));
            break;
        default:
            throw new IllegalActionException("Unrecognized mode choice \"" +
                    mode.getExpression() + "\".");
        }

        return data;
    }

    public StringParameter message;

    public ChoiceParameter mode;

    public Parameter response;

    public enum Mode {
        ERROR {
            public String toString() {
                return "error";
            }
        },
        MESSAGE {
            public String toString() {
                return "message";
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
}
