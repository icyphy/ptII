/*

 Copyright (c) 2008-2009 The Regents of the University of California.
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

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;

import javax.swing.JFrame;

import ptolemy.actor.gui.Tableau;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ChoiceParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ptera.kernel.Event;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// SetTableau

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class SetTableau extends Event {

    /**
     *  @param container
     *  @param name
     *  @exception IllegalActionException
     *  @exception NameDuplicationException
     */
    public SetTableau(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        referredTableau = new StringParameter(this, "referredTableau");

        alwaysOnTop = new Parameter(this, "alwaysOnTop");
        alwaysOnTop.setTypeAtMost(BaseType.BOOLEAN);
        alwaysOnTop.setToken(BooleanToken.FALSE);

        enabled = new Parameter(this, "enabled");
        enabled.setTypeAtMost(BaseType.BOOLEAN);
        enabled.setToken(BooleanToken.TRUE);

        focused = new Parameter(this, "focused");
        focused.setTypeAtMost(BaseType.BOOLEAN);
        focused.setToken(BooleanToken.FALSE);

        resizable = new Parameter(this, "resizable");
        resizable.setTypeAtMost(BaseType.BOOLEAN);
        resizable.setToken(BooleanToken.TRUE);

        screenLocation = new Parameter(this, "screenLocation");
        screenLocation.setTypeEquals(BaseType.INT_MATRIX);
        screenLocation.setToken("[-1, -1]");

        screenSize = new Parameter(this, "screenSize");
        screenSize.setTypeEquals(BaseType.INT_MATRIX);
        screenSize.setToken("[-1, -1]");

        state = new ChoiceParameter(this, "state", TableauState.class);
        state.setExpression(TableauState.NORMAL.toString());

        title = new StringParameter(this, "title");

        visible = new Parameter(this, "visible");
        visible.setTypeAtMost(BaseType.BOOLEAN);
        visible.setToken(BooleanToken.TRUE);
    }

    public RefiringData fire(Token arguments) throws IllegalActionException {
        RefiringData data = super.fire(arguments);

        Tableau tableau = EventUtils.getTableau(this, referredTableau, null);
        JFrame frame = tableau.getFrame();

        boolean isAlwaysOnTop = ((BooleanToken) alwaysOnTop.getToken())
                .booleanValue();
        if (frame.isAlwaysOnTop() != isAlwaysOnTop) {
            frame.setAlwaysOnTop(isAlwaysOnTop);
        }

        boolean isEnabled = ((BooleanToken) enabled.getToken()).booleanValue();
        if (frame.isEnabled() != isEnabled) {
            frame.setEnabled(isEnabled);
        }

        boolean isFocused = ((BooleanToken) focused.getToken()).booleanValue();
        if (isFocused) {
            frame.requestFocus();
        }

        boolean isResizable = ((BooleanToken) resizable.getToken())
                .booleanValue();
        if (frame.isResizable() != isResizable) {
            frame.setResizable(isResizable);
        }

        IntMatrixToken newLocation = (IntMatrixToken) screenLocation.getToken();
        Point location = frame.getLocation();
        int x = newLocation.getElementAt(0, 0);
        if (x >= 0) {
            location.x = x;
        }
        int y = newLocation.getElementAt(0, 1);
        if (y >= 0) {
            location.y = y;
        }
        frame.setLocation(location);

        IntMatrixToken newSize = (IntMatrixToken) screenSize.getToken();
        Dimension size = frame.getSize();
        int width = newSize.getElementAt(0, 0);
        if (width >= 0) {
            size.width = width;
        }
        int height = newSize.getElementAt(0, 1);
        if (height >= 0) {
            size.height = height;
        }
        frame.setSize(size);

        TableauState newState = (TableauState) state.getChosenValue();
        switch (newState) {
        case ICONIFIED:
            if ((frame.getExtendedState() & Frame.ICONIFIED) != Frame.ICONIFIED) {
                frame.setExtendedState(Frame.ICONIFIED);
            }
            break;
        case MAXIMIZED:
            if ((frame.getExtendedState() & Frame.MAXIMIZED_BOTH) != Frame.MAXIMIZED_BOTH) {
                frame.setExtendedState(Frame.MAXIMIZED_BOTH);
            }
            break;
        case NORMAL:
            // FIXME: Findbugs BIT says that this will always be equal:
            // "This method compares an expression of the form (e
            // &amp; 0) to 0, which will always compare equal.  This
            // may indicate a logic error or typo."

            if ((frame.getExtendedState() & Frame.NORMAL) != Frame.NORMAL) {
                frame.setExtendedState(Frame.NORMAL);
            }
            break;
        }

        String newTitle = title.stringValue().trim();
        if (!newTitle.equals("") && frame.getTitle().equals(newTitle)) {
            frame.setTitle(newTitle);
        }

        boolean isVisible = ((BooleanToken) visible.getToken()).booleanValue();
        if (frame.isVisible() != isVisible) {
            frame.setVisible(isVisible);
        }

        return data;
    }

    public Parameter alwaysOnTop;

    public Parameter enabled;

    public Parameter focused;

    public StringParameter referredTableau;

    public Parameter resizable;

    public Parameter screenLocation;

    public Parameter screenSize;

    public ChoiceParameter state;

    public StringParameter title;

    public Parameter visible;

    public enum TableauState {
        ICONIFIED {
            public String toString() {
                return "iconified";
            }
        },
        MAXIMIZED {
            public String toString() {
                return "mazimized";
            }
        },
        NORMAL {
            public String toString() {
                return "normal";
            }
        }
    }
}
