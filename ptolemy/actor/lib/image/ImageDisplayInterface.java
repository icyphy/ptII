/*
 Interface encapsulating platform dependent code of the ImageDisplay from the
 platform independent parts.

 @Copyright (c) 1998-2014 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */
package ptolemy.actor.lib.image;

import java.awt.Color;
import java.awt.Container;

import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
////ImageDisplayInterface
/**
 * Interface encapsulating platform dependent code of the ImageDisplay from the
 * platform independent parts.
 * @author Jianwu Wang
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating
 * @Pt.AcceptedRating
 */
public interface ImageDisplayInterface {

    /**
     * Free up memory when closing.
     */
    public void cleanUp();

    /** Display the specified token.
     *  @param in The token to display
     */
    public void display(Token in);

    /** Get the background.
     *  @return The background color.
     *  @see #setBackground(Color)
     */
    public Color getBackground();

    /**
     * Get the image's frame.
     * @return the image's frame.
     * @see #setFrame(Object)
     */
    public Object getFrame();

    /**
     * Get the platform dependent picture that contains the image.
     * @return the platform dependent container.
     * @see #setPicture(Object)
     */
    public Object getPicture();

    /**
     * Get the platform dependent container that contains the image.
     * @return the platform dependent container.
     * @see #setPlatformContainer(Object)
     */
    public Object getPlatformContainer();

    /**
     * Get the image tableau.
     * @return the image tableau.
     */
    public Object getTableau();

    /** Initialize an object.  Derived classes should include
     * class-specific initialization here.
     * @param imageDisplay The object to be initialized
     * @exception IllegalActionException If the entity cannot be contained
     * by the proposed container.
     * @exception NameDuplicationException If the container already has an
     * actor with this name.
     */
    public void init(ImageDisplay imageDisplay) throws IllegalActionException,
    NameDuplicationException;

    /**
     * Initialize the effigy of the image.
     * @exception IllegalActionException If there is a problem initializing the effigy
     */
    public void initializeEffigy() throws IllegalActionException;

    /**
     * Initialize window and size attributes.
     * @exception IllegalActionException If there is a problem creating the attributes.
     * @exception NameDuplicationException If there is a problem creating the attributes.
     */
    public void initWindowAndSizeProperties() throws IllegalActionException,
    NameDuplicationException;

    /** Set the container to be placed.
     *  @param container The Container to be placed.
     */
    public void placeContainer(Container container);

    /** Set the background.
     *  @param background The background color.
     *  @see #getBackground()
     */
    public void setBackground(Color background);

    /**
     * Set the frame of the image.
     * @param frame The frame to set.
     * @see #getFrame()
     */
    public void setFrame(Object frame);

    /**
     * Set the platform dependent picture of the image.
     * The container can be AWT container or Android view.
     * @param picture The picture
     * @see #getPicture()
     */
    public void setPicture(Object picture);

    /**
     * Set the platform dependent container of the image.
     * The container can be AWT container or Android view.
     * @param container the platform dependent container.
     * @see #getPlatformContainer()
     */
    public void setPlatformContainer(Object container);

}
