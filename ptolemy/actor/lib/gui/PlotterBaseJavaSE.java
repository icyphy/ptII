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

//////////////////////////////////////////////////////////////////////////
//// PlotterBaseJavaSE

/**
 * Java SE implementation of PlotterBaseInterface.  The code is largely based on
 * the original platform dependent version of the PlotterBase but was moved here
 * in order to support portability of the actor.
 * @author Edward A. Lee Contributors: Anar Huseynov
 * @version $Id$ 
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class PlotterBaseJavaSE implements PlotterBaseInterface {

    /** 
     * {@inheritDoc}
     */
    public void init(PlotterBase plotterBase) {
        _plotterBase = plotterBase;
    }

    /** 
     * {@inheritDoc}
     */
    public void initWindowAndSizeProperties() throws IllegalActionException,
            NameDuplicationException {
        _windowProperties = new WindowPropertiesAttribute(_plotterBase,
                "_windowProperties");
        // Note that we have to force this to be persistent because
        // there is no real mechanism for the value of the properties
        // to be updated when the window is moved or resized. By
        // making it persistent, when the model is saved, the
        // attribute will determine the current size and position
        // of the window and save it.
        _windowProperties.setPersistent(true);
        _plotSize = new SizeAttribute(_plotterBase, "_plotSize");
        _plotSize.setPersistent(true);
    }

    /** 
     * {@inheritDoc}
     */
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
     * {@inheritDoc}
     */
    public void updateWindowAndSizeAttributes() {
        if (_frame != null) {
            _windowProperties.recordProperties(_frame);
        }

        if (_plotterBase.plot != null) {
            _plotSize.recordSize((Component) _plotterBase.plot);
        }
    }

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

    /** 
     * {@inheritDoc}
     */
    public void setTableauTitle(String title) {
        if (_tableau != null) {
            _tableau.setTitle(title);
        }
    }

    /** 
     * {@inheritDoc}
     */
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
     * {@inheritDoc}
     */
    public void cleanUp() {
        _tableau = null;
    }

    /** 
     * {@inheritDoc}
     */
    public void remove() {
        SwingUtilities.invokeLater(new Runnable() {
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
     * {@inheritDoc}
     */
    public Object getTableau() {
        return _tableau;
    }

    /** 
     * {@inheritDoc}
     */
    public Object getFrame() {
        return _frame;
    }

    /** 
     * {@inheritDoc}
     */
    public Object getPlatformContainer() {
        return _container;
    }

    /** 
     * {@inheritDoc}
     */
    public void updateSize() {
        if (_plotSize != null) {
            _plotSize.setSize((Component) _plotterBase.plot);
        }

        _frame.pack();
    }

    /** 
     * {@inheritDoc}
     */
    public void bringToFront() {
        if (_frame != null) {
            // show() used to call pack, which would override any manual
            // changes in placement. No more.
            _frame.show();
            _frame.toFront();
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void initializeEffigy() throws IllegalActionException {
        // Need an effigy and a tableau so that menu ops work properly.
        Effigy containerEffigy = Configuration.findEffigy(_plotterBase
                .toplevel());

        if (containerEffigy == null) {
            throw new IllegalActionException(_plotterBase,
                    "Cannot find effigy for top level: "
                            + _plotterBase.toplevel().getFullName());
        }

        try {
            PlotEffigy plotEffigy = new PlotEffigy(containerEffigy,
                    containerEffigy.uniqueName("plot"));
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
     * {@inheritDoc}
     */
    public PlotBoxInterface newPlot() {
        return new Plot();
    }

    /** 
     * {@inheritDoc}
     */
    public void setPlatformContainer(Object container) {
        _container = (Container) container;
    }

    ///////////////////////////////////////////////////////////////////
    ////                      protected variables                 ////
    protected SizeAttribute _plotSize;
    protected PlotterBase _plotterBase;
    protected WindowPropertiesAttribute _windowProperties;
    /** Container into which this plot should be placed. */
    protected Container _container;

    /** Frame into which plot is placed, if any. */
    protected transient PlotTableauFrame _frame;
    protected PlotWindowTableau _tableau;

    /** A reference to the listener for removal purposes. */
    protected WindowClosingAdapter _windowClosingAdapter;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Listener for windowClosing action. */
    class WindowClosingAdapter extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
            _plotterBase.cleanUp();
        }
    }
}
