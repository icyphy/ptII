/* Base class for plotters, except histograms.

 @Copyright (c) 1998-2005 The Regents of the University of California.
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

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PlotEffigy;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.plot.Plot;

//////////////////////////////////////////////////////////////////////////
//// Plotter

/**
 Base class for plotters.  This class contains an instance of the
 Plot class from the Ptolemy plot package as a public member.
 It provides a parameter that determines whether to fill the plot
 when wrapup is invoked. It also has a <i>legend</i> parameter,
 which gives a comma-separated list of labels to attach to
 each dataset.  Normally, the number of elements in this list
 should equal the number of input channels, although this
 is not enforced.
 <p>
 This actor also provides a parameter
 <i>startingDataset</i>, which specifies the starting point
 for the number of the dataset to use to create the plots.
 This defaults to zero, but will typically be set to a positive
 number when more than one instance of a plotter actor shares
 the same plot object.

 @see ptolemy.plot.Plot

 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (cxh)
 */
public class Plotter extends PlotterBase {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Plotter(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        startingDataset = new Parameter(this, "startingDataset",
                new IntToken(0));
        startingDataset.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The starting dataset number to which data is plotted.
     *  This parameter has type IntToken, with default value 0.
     *  Its value must be non-negative.
     */
    public Parameter startingDataset;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is <i>startingDataset</i>, then check its validity.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>startingDataset</i> and its value is negative, or if the
     *   superclass throws it.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        // NOTE: Do not react to changes in _windowProperties.
        // Those properties are only used when originally opening a window.
        if (attribute == startingDataset) {
            if (((IntToken) startingDataset.getToken()).intValue() < 0) {
                throw new IllegalActionException(this,
                        "startingDataset: negative value is not allowed.");
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** If the plot has not already been created, create it.
     *  If configurations specified by a call to configure() have not yet
     *  been processed, process them. Clear the data sets that this
     *  actor is responsible for (starting with the one indexed
     *  by <i>startingDataset</i>, up to <i>startingDataset</i> +
     *  <i>width</i> - 1, where <i>width</i> is the width of the
     *  input port.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        if (plot == null) {
            // Create a new plot.
            plot = _newPlot();
            plot.setTitle(getName());
            plot.setButtons(true);
        }

        if ((_frame == null) && (_container == null)) {
            // Need an effigy and a tableau so that menu ops work properly.
            Effigy containerEffigy = Configuration.findEffigy(toplevel());

            if (containerEffigy == null) {
                throw new IllegalActionException(this,
                        "Cannot find effigy for top level: "
                                + toplevel().getFullName());
            }

            try {
                PlotEffigy plotEffigy = new PlotEffigy(containerEffigy,
                        containerEffigy.uniqueName("plot"));

                // The default identifier is "Unnamed", which is no good for
                // two reasons: Wrong title bar label, and it causes a save-as
                // to destroy the original window.
                plotEffigy.identifier.setExpression(getFullName());

                PlotWindowTableau tableau = new PlotWindowTableau(plotEffigy,
                        "tableau");
                _frame = tableau.frame;
            } catch (Exception ex) {
                throw new IllegalActionException(this, null, ex,
                        "Error creating effigy and tableau");
            }

            _windowProperties.setProperties(_frame);
            _implementDeferredConfigurations();

            // The SizeAttribute property is used to specify the size
            // of the Plot component. Unfortunately, with Swing's
            // mysterious and undocumented handling of component sizes,
            // there appears to be no way to control the size of the
            // Plot from the size of the Frame, which is specified
            // by the WindowPropertiesAttribute.
            if (_plotSize != null) {
                _plotSize.setSize(plot);
            }

            _frame.pack();
        } else {
            if (plot instanceof Plot) {
                int width = ((Plot) plot).getNumDataSets();
                int offset = ((IntToken) startingDataset.getToken()).intValue();

                for (int i = width - 1; i >= 0; i--) {
                    ((Plot) plot).clear(i + offset);
                }

                plot.repaint();
            } else {
                plot.clear(false);
                plot.repaint();
            }
        }

        if (_frame != null) {
            // show() used to call pack, which would override any manual
            // changes in placement. No more.
            _frame.show();
            _frame.toFront();
        }
    }
}
