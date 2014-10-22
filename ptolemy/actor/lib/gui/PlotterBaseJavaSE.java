/*
 Interface encapsulating platform dependent code of the PlotterBase from the
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

package ptolemy.actor.lib.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.SwingUtilities;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PlotEffigy;
import ptolemy.actor.gui.PlotTableau;
import ptolemy.actor.gui.PlotTableauFrame;
import ptolemy.actor.gui.SizeAttribute;
import ptolemy.actor.gui.WindowPropertiesAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.plot.Plot;
import ptolemy.plot.PlotBox;
import ptolemy.plot.PlotBoxInterface;

///////////////////////////////////////////////////////////////////
//// PlotterBaseJavaSE

/**
 * Java SE implementation of PlotterBaseInterface.  The code is largely based on
 * the original platform dependent version of the PlotterBase but was moved here
 * in order to support portability of the actor.
 * @author Edward A. Lee Contributors: Anar Huseynov
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class PlotterBaseJavaSE implements PlotterBaseInterface {

    /** Show and then bing the frame to the front. */
    @Override
    public void bringToFront() {
        if (_frame != null) {
            // show() used to call pack, which would override any manual
            // changes in placement. No more.
            _frame.show();
            _frame.toFront();
        }
    }

    /**
     * Free up memory when closing.
     */
    @Override
    public void cleanUp() {
        _tableau = null;
    }

    /**
     * Get the plotter's frame.
     * @return the plotter's frame.
     * @see #setFrame(Object)
     */
    @Override
    public Object getFrame() {
        return _frame;
    }

    /**
     * Get the platform dependent container that contains the plotter.
     * @return the platform dependent container.
     * @see #setPlatformContainer(Object)
     */
    @Override
    public Object getPlatformContainer() {
        return _container;
    }

    /**
     * Get the plotter tableau.
     * @return the plotter tableau.
     */
    @Override
    public Object getTableau() {
        return _tableau;
    }

    /**
     * Initialize the implementation.
     * @param plotterBase the instance that created the implementation.
     */
    @Override
    public void init(PlotterBase plotterBase) {
        _plotterBase = plotterBase;
    }

    /**
     * Initialize the effigy of the plotter.
     * @exception IllegalActionException If there is a problem initializing the effigy
     */
    @Override
    public void initializeEffigy() throws IllegalActionException {
        // Need an effigy and a tableau so that menu ops work properly.
        Effigy containerEffigy = Configuration.findEffigy(_plotterBase
                .toplevel());

        if (containerEffigy == null) {
            try {
                containerEffigy = new PlotEffigy(_plotterBase.workspace());
                //containerEffigy.setModel(_plotterBase.toplevel());
            } catch (Exception ex) {
                throw new IllegalActionException(_plotterBase, ex,
                        "Cannot find effigy for top level: "
                                + _plotterBase.toplevel().getFullName());
            }
        }

        try {
            PlotEffigy plotEffigy;
            // In PN models, there could be multiple of these running
            // at the same time, in which case we may get a name collision.
            // To prevent this, synchronize on the effigy.
            synchronized (containerEffigy) {
                plotEffigy = new PlotEffigy(containerEffigy,
                        containerEffigy.uniqueName("plot"));
            }
            // Specify that the associated plot is the one created here.
            plotEffigy.setPlot(_plotterBase.plot);
            // Specify that the associated Ptolemy model is this actor.
            plotEffigy.setModel(_plotterBase);

            // The default identifier is "Unnamed", which is no good for
            // two reasons: Wrong title bar label, and it causes a save-as
            // to destroy the original window.
            plotEffigy.identifier.setExpression(_plotterBase.getFullName());

            _tableau = new PlotWindowTableau(plotEffigy, "tableau");

            setFrame(_tableau.frame);
        } catch (Exception ex) {
            throw new IllegalActionException(_plotterBase, null, ex,
                    "Error creating effigy and tableau");
        }
    }

    /**
     * Initialize the effigy of the plotter.
     * @exception IllegalActionException If there is a problem initializing the effigy
     */
    @Override
    public void initWindowAndSizeProperties() throws IllegalActionException,
            NameDuplicationException {
        _windowProperties = (WindowPropertiesAttribute) _plotterBase
                .getAttribute("_windowProperties",
                        WindowPropertiesAttribute.class);
        if (_windowProperties == null) {
            _windowProperties = new WindowPropertiesAttribute(_plotterBase,
                    "_windowProperties");
            // Note that we have to force this to be persistent because
            // there is no real mechanism for the value of the properties
            // to be updated when the window is moved or resized. By
            // making it persistent, when the model is saved, the
            // attribute will determine the current size and position
            // of the window and save it.
            _windowProperties.setPersistent(true);
        }
        _plotSize = (SizeAttribute) _plotterBase.getAttribute("_plotSize",
                SizeAttribute.class);
        if (_plotSize == null) {
            _plotSize = new SizeAttribute(_plotterBase, "_plotSize");
            _plotSize.setPersistent(true);
        }
    }

    /**
     * Create a new instance of the PlotBoxInterface implementation.
     * @return a new instance of the PlotBoxInterface implementation.
     */
    @Override
    public PlotBoxInterface newPlot() {
        return new Plot();
    }

    /**
     * Remove the plot from the current container, if there is one.
     */
    @Override
    public void remove() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (_plotterBase.plot != null) {
                    if (_container != null) {
                        _container.remove((Component) _plotterBase.plot);
                        _container.invalidate();
                        _container.repaint();
                    } else if (_frame != null) {
                        _frame.dispose();
                    }
                }
            }
        });
    }

    /**
     * Remove the plot from the frame if the container is null.
     */
    @Override
    public void removeNullContainer() {
        // NOTE: This actor always shows the plot buttons, even if
        // the plot is in a separate frame.  They are very useful.
        if (_container == null) {
            // Dissociate with any container.
            // NOTE: _remove() doesn't work here.  Why?
            if (_frame != null) {
                _frame.dispose();
            }

            _frame = null;

            // If we forget the plot, then its properties get lost.
            // Also, if the window is deleted during a run, the data
            // will be lost. So do not forget the plot.
            // plot = null;
            return;
        }
    }

    /**
     * Set the frame of the plotter.
     * @param frame The frame to set.
     * @see #getFrame()
     */
    @Override
    public void setFrame(Object frame) {
        if (_frame != null) {
            _frame.removeWindowListener(_windowClosingAdapter);
        }

        if (frame == null) {
            _frame = null;
            return;
        }

        _frame = (PlotTableauFrame) frame;

        _windowClosingAdapter = new WindowClosingAdapter();
        _frame.addWindowListener(_windowClosingAdapter);

        _windowProperties.setProperties(_frame);
    }

    /**
     * Set the platform dependent container of the plotter.
     * The container can be AWT container or Android view.
     * @param container the platform dependent container.
     * @see #getPlatformContainer()
     */
    @Override
    public void setPlatformContainer(Object container) {
        _container = (Container) container;
    }

    /**
     * Set the title of the tableau.
     * @param title the title to set.
     */
    @Override
    public void setTableauTitle(String title) {
        if (_tableau != null) {
            _tableau.setTitle(title);
        }
    }

    /**
     * Update size attribute of the plotter.
     */
    @Override
    public void updateSize() {
        if (_plotSize != null) {
            _plotSize.setSize((Component) _plotterBase.plot);
        }

        if (_frame != null) {
            _frame.pack();
        }
    }

    /**
     * Update values of the attributes.
     */
    @Override
    public void updateWindowAndSizeAttributes() {
        if (_frame != null) {
            _windowProperties.recordProperties(_frame);
        }

        if (_plotterBase.plot != null) {
            _plotSize.recordSize((Component) _plotterBase.plot);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    /** Container into which this plot should be placed. */
    protected Container _container;

    /** Frame into which plot is placed, if any. */
    protected transient PlotTableauFrame _frame;

    /** An attribute that contains the size of the plot. */
    protected SizeAttribute _plotSize;

    /** The base instance that created the implementation. */
    protected PlotterBase _plotterBase;

    /** The Plotter tableau. */
    protected PlotWindowTableau _tableau;

    /** A reference to the listener for removal purposes. */
    protected WindowClosingAdapter _windowClosingAdapter;

    /** An attribute tha contains the size and position of the window. */
    protected WindowPropertiesAttribute _windowProperties;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Tableau that creates a PlotterPlotFrame.
     */
    protected class PlotWindowTableau extends PlotTableau {
        /** Construct a new tableau for the model represented by the
         *  given effigy.
         *  @param container The container.
         *  @param name The name.
         *  @exception IllegalActionException If the container does not accept
         *   this entity (this should not occur).
         *  @exception NameDuplicationException If the name coincides with an
         *   attribute already in the container.
         */
        public PlotWindowTableau(PlotEffigy container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
            frame = new PlotTableauFrame(this, (PlotBox) _plotterBase.plot,
                    _plotterBase);
            setFrame(frame);
        }

        /** The frame. */
        public PlotTableauFrame frame;
    }

    /** Listener for windowClosing action. */
    class WindowClosingAdapter extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            _plotterBase.cleanUp();
        }
    }
}
