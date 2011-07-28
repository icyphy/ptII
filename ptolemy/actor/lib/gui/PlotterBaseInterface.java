/* 
 Interface encapsulating platform dependent code of the PlotterBase from the 
 platform independent parts.

 @Copyright (c) 1998-2010 The Regents of the University of California.
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
package ptolemy.actor.lib.gui;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.plot.PlotBoxInterface;

//////////////////////////////////////////////////////////////////////////
//// PlotterBaseInterface
/**
 * Interface encapsulating platform dependent code of the PlotterBase from the 
 * platform independent parts.
 * @author Anar Huseynov
 * @version $Id$ 
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public interface PlotterBaseInterface {

    /**
     * Initialize the implementation.
     * @param plotterBase the instance that created the implementation.
     */
    public void init(PlotterBase plotterBase);

    /**
     * Initialize window and size attributes.
     * @throws IllegalActionException if there is a problem creating the attributes.
     * @throws NameDuplicationException if there is a problem creating the attributes.
     */
    public void initWindowAndSizeProperties() throws IllegalActionException,
            NameDuplicationException;

    /**
     * Update values of the attributes.
     */
    public void updateWindowAndSizeAttributes();

    /**
     * Set the title of the tableau.
     * @param title the title to set.
     */
    public void setTableauTitle(String title);

    /**
     * Set the frame of the plotter.
     * @param frame The frame to set.
     */
    public void setFrame(Object frame);

    /**
     * Free up memory when closing. 
     */
    public void cleanUp();

    /**
     * Remove the plot from the current container, if there is one.
     */
    public void remove();

    /**
     * Remove the plot from the frame if the container is null.
     */
    public void removeNullContainer();

    /**
     * Get the plotter tableau.
     * @return the plotter tableau.
     */
    public Object getTableau();

    /**
     * Get the plotter's frame.
     * @return the plotter's frame.
     */
    public Object getFrame();

    /**
     * Set the platform dependent container of the plotter.
     * The container can be AWT container or Android view.
     * @param container the platform dependent container.
     * @see #getPlatformContainer()
     */
    public void setPlatformContainer(Object container);

    /**
     * Get the platform dependent container that contains the plotter.
     * @return the platform dependent container.
     * @see #setPlatformContainer(Object)
     */
    public Object getPlatformContainer();

    /**
     * Update size attribute of the plotter.
     */
    public void updateSize();

    /**
     * Bring the plotter frame to the front.
     */
    public void bringToFront();

    /**
     * Initialize the effigy of the plotter.
     * @throws IllegalActionException
     */
    public void initializeEffigy() throws IllegalActionException;

    /**
     * Create a new instance of the PlotBoxInterface implementation.
     * @return a new instance of the PlotBoxInterface implementation.
     */
    public PlotBoxInterface newPlot();
}
