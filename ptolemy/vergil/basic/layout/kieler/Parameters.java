/*

Copyright (c) 2011-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA LIABLE TO ANY PARTY
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
package ptolemy.vergil.basic.layout.kieler;

import java.util.List;

import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.vergil.actor.ActorGraphModel;
import ptolemy.vergil.basic.layout.LayoutConfiguration;
import ptolemy.vergil.basic.layout.LayoutConfiguration.InteractionMode;
import ptolemy.vergil.modal.FSMGraphModel;
import de.cau.cs.kieler.core.properties.IProperty;
import de.cau.cs.kieler.core.properties.Property;
import de.cau.cs.kieler.kiml.klayoutdata.KShapeLayout;
import de.cau.cs.kieler.kiml.options.Direction;
import de.cau.cs.kieler.kiml.options.EdgeRouting;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.klay.layered.p1cycles.CycleBreakingStrategy;
import de.cau.cs.kieler.klay.layered.p2layers.LayeringStrategy;
import de.cau.cs.kieler.klay.layered.p3order.CrossingMinimizationStrategy;
import de.cau.cs.kieler.klay.layered.p4nodes.NodePlacementStrategy;
import de.cau.cs.kieler.klay.layered.properties.Properties;
import diva.graph.GraphModel;

/**
 * Responsible for translating layout configuration parameters into the KIELER format.
 * Parameters are read from an instance of the {@link LayoutConfiguration} attribute,
 * which is attached to composite entities when the configuration dialog is opened.
 *
 * @see LayoutConfiguration
 * @author Miro Spoenemann, Christoph Daniel Schulze
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (msp)
 * @Pt.AcceptedRating Red (msp)
 */
public class Parameters {

    /**
     * Create a parameters instance.
     *
     * @param compositeEntity the composite entity for which parameters are retrieved.
     */
    public Parameters(CompositeEntity compositeEntity) {
        _compositeEntity = compositeEntity;
    }

    /**
     * Configure the KIELER layout using a property holder.
     *
     * @param parentLayout the layout of the parent node
     * @param graphModel the graph model of the current diagram
     * @exception IllegalActionException if one of the parameters has the wrong type
     */
    public void configureLayout(KShapeLayout parentLayout, GraphModel graphModel)
            throws IllegalActionException {
        // Set general default values.
        parentLayout.setProperty(LayoutOptions.DIRECTION, Direction.RIGHT);
        parentLayout.setProperty(LayoutOptions.BORDER_SPACING, 5.0f);
        parentLayout.setProperty(Properties.EDGE_SPACING_FACTOR, 1.5f);

        // Copy values specified by user.
        List<LayoutConfiguration> configAttributes = _compositeEntity
                .attributeList(LayoutConfiguration.class);
        if (!configAttributes.isEmpty()) {
            LayoutConfiguration configuration = configAttributes.get(0);

            // Whether decorations are to be laid out or left as they are
            BooleanToken decorationsToken = BooleanToken
                    .convert(configuration.includeDecorations.getToken());
            parentLayout.setProperty(DECORATIONS,
                    decorationsToken.booleanValue());

            //            // Whether to optimize relation vertices
            //            BooleanToken optimizeRelationsToken = BooleanToken.convert(
            //                    configuration.optimizeRelations.getToken());
            //            parentLayout.setProperty(OPTIMIZE_RELATIONS,
            //                    optimizeRelationsToken.booleanValue());

            // The node placement algorithm to use
            BooleanToken minimizeBendsToken = BooleanToken
                    .convert(configuration.minimizeBends.getToken());
            if (minimizeBendsToken.booleanValue()) {
                parentLayout.setProperty(Properties.NODEPLACE,
                        NodePlacementStrategy.BRANDES_KOEPF);
            } else {
                parentLayout.setProperty(Properties.NODEPLACE,
                        NodePlacementStrategy.LINEAR_SEGMENTS);
            }

            // Spacing between diagram elements
            DoubleToken spacingToken = DoubleToken
                    .convert(configuration.spacing.getToken());
            parentLayout.setProperty(SPACING,
                    (float) spacingToken.doubleValue());

            // Target aspect ratio for the diagram
            DoubleToken logAspectToken = DoubleToken
                    .convert(configuration.logAspectRatio.getToken());
            parentLayout.setProperty(ASPECT_RATIO,
                    (float) Math.pow(10, logAspectToken.doubleValue()));

            // The interaction mode (constraints the layout according to what the
            // diagram currently looks like)
            InteractionMode interactionMode = (InteractionMode) configuration.interactionMode
                    .getChosenValue();
            if (interactionMode != null) {
                // The switch cases fall through on purpose!
                switch (interactionMode) {
                case Full:
                    parentLayout.setProperty(Properties.CROSSMIN,
                            CrossingMinimizationStrategy.INTERACTIVE);
                case Columns:
                    parentLayout.setProperty(Properties.NODE_LAYERING,
                            LayeringStrategy.INTERACTIVE);
                case Cycles:
                    parentLayout.setProperty(Properties.CYCLE_BREAKING,
                            CycleBreakingStrategy.INTERACTIVE);
                default:
                    // Don't change the configuration in all other cases
                }
            }

        } else {
            parentLayout.setProperty(LayoutOptions.SPACING,
                    SPACING.getDefault());
            parentLayout.setProperty(LayoutOptions.ASPECT_RATIO,
                    ASPECT_RATIO.getDefault());
            parentLayout.setProperty(Properties.NODEPLACE,
                    NodePlacementStrategy.BRANDES_KOEPF);
        }

        if (graphModel instanceof ActorGraphModel) {
            // Set default values for actor models.
            parentLayout.setProperty(LayoutOptions.EDGE_ROUTING,
                    EdgeRouting.ORTHOGONAL);
        } else if (graphModel instanceof FSMGraphModel) {
            // Set default values for modal models.
            parentLayout.setProperty(LayoutOptions.EDGE_ROUTING,
                    EdgeRouting.POLYLINE);
            float spacing = parentLayout.getProperty(SPACING);
            parentLayout.setProperty(SPACING, 2 * spacing);
        }
    }

    /** Layout option that determines whether decoration nodes are included in layout. */
    public static final IProperty<Boolean> DECORATIONS = new Property<Boolean>(
            "ptolemy.vergil.basic.layout.decorations",
            LayoutConfiguration.DEF_DECORATIONS);

    //    /** Layout option for optimizing away superfluous relation vertices. */
    //    public static final IProperty<Boolean> OPTIMIZE_RELATIONS = new Property<Boolean>(
    //            "ptolemy.vergil.basic.layout.optimizeRelations", LayoutConfiguration.DEF_OPTIMIZE_RELATIONS);

    /** Layout option for the overall spacing between elements. */
    public static final IProperty<Float> SPACING = new Property<Float>(
            LayoutOptions.SPACING, (float) LayoutConfiguration.DEF_SPACING);

    /** Layout option for the aspect ratio of connected components. */
    public static final IProperty<Float> ASPECT_RATIO = new Property<Float>(
            LayoutOptions.ASPECT_RATIO,
            (float) LayoutConfiguration.DEF_ASPECT_RATIO);

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** the parent entity from which the layout configuration is read. */
    private CompositeEntity _compositeEntity;

}
