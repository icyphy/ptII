/* An attribute representing the size, location, and other window properties.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.gui.Top;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

//////////////////////////////////////////////////////////////////////////
//// WindowPropertiesAttribute
/**
This attribute stores properties of a window, including the width,
height, and location. The token in this attribute is a RecordToken
containing a field "bounds" with a 4-element integer array.
When we fully commit to Java 1.4, there will also be a field
to indicate whether the window is maximized.
By default, this attribute has visibility NONE, so the user will not
see it in parameter editing dialogs.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 2.1
*/
public class WindowPropertiesAttribute extends Parameter
        implements ComponentListener {
    
    /** Construct an attribute with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public WindowPropertiesAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setVisibility(Settable.NONE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Do nothing. This method is
     *  invoked when the component has been made invisible.
     *  @param event The component event.
     */
    public void componentHidden(ComponentEvent event) {}
             
    /** Record the new position. This method is
     *  invoked when the component's position changes.
     *  @param event The component event.
     */
    public void componentMoved(ComponentEvent event) {
        recordProperties(_listeningTo);
    }
             
    /** Record the new size. This method is
     *  invoked when the component's size changes.
     *  @param event The component event.
     */
    public void componentResized(ComponentEvent event) {
        recordProperties(_listeningTo);
    }

    /** Do nothing. This method is
     *  invoked when the component has been made visible.
     *  @param event The component event.
     */
    public void componentShown(ComponentEvent event) {}

    /** Set the value of the attribute to match those of the specified
     *  frame.
     *  @param frame The frame whose properties are to be recorded.
     */
    public void recordProperties(Frame frame) {
        try {
            Rectangle bounds = frame.getBounds();
            Token[] boundsArray = new IntToken[4];
            boundsArray[0] = new IntToken(bounds.x);
            boundsArray[1] = new IntToken(bounds.y);
            boundsArray[2] = new IntToken(bounds.width);
            boundsArray[3] = new IntToken(bounds.height);

            // Determine whether the window is maximized.
            boolean maximized
                    = (frame.getExtendedState() & Frame.MAXIMIZED_BOTH)
                    == Frame.MAXIMIZED_BOTH;

            // Construct values for the record token.
            Token[] values = new Token[2];
            values[0] = new ArrayToken(boundsArray);
            values[1] = new BooleanToken(maximized);

            // Construct field names for the record token.
            String[] names = new String[2];
            names[0] = "bounds";
            names[1] = "maximized";

            setToken(new RecordToken(names, values));
            
            // If we don't do this, then the bounds may not be written.
            setModifiedHeritage(true);
            
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(
                    "Can't set propertes value! " + ex);
        }
    }

    /** Set the properties of the specified frame to match the
     *  current value of the attribute.  If the value of the attribute
     *  has not been set, then do nothing and return true. If the
     *  value of this attribute is malformed in any way, then just
     *  return false.
     *  @param frame The frame whose properties are to be set.
     *  @return True if successful.
     */
    public boolean setProperties(Frame frame) {
        if (_listeningTo != frame) {
            if (_listeningTo != null) {
                _listeningTo.removeComponentListener(this);
            }
            frame.addComponentListener(this);
            _listeningTo = frame;
        }
        try {
            RecordToken value = (RecordToken)getToken();
            if (value == null) return true;
            ArrayToken boundsToken = (ArrayToken)value.get("bounds");
            BooleanToken maximizedToken = (BooleanToken)value.get("maximized");

            int x = ((IntToken)boundsToken.getElement(0)).intValue();
            int y = ((IntToken)boundsToken.getElement(1)).intValue();
            int width = ((IntToken)boundsToken.getElement(2)).intValue();
            int height = ((IntToken)boundsToken.getElement(3)).intValue();
            
            frame.setBounds(x, y, width, height);

            if (maximizedToken != null) {
                boolean maximized = maximizedToken.booleanValue();
                if (maximized) {
                    // FIXME: Regrettably, this doesn't make the window
                    // actually maximized under Windows, at least.
                    frame.setExtendedState(
                            frame.getExtendedState() | Frame.MAXIMIZED_BOTH);
                }
            }

            if (frame instanceof Top) {
                // Disable centering.
                ((Top)frame).setCentering(false);
            }

            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The frame we are listening to. */
    private Frame _listeningTo;
}
