/*

Copyright (c) 2011-2016 The Regents of the University of California.
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

import de.cau.cs.kieler.core.properties.IProperty;
import de.cau.cs.kieler.core.properties.Property;
import de.cau.cs.kieler.kiml.klayoutdata.KLayoutData;
import de.cau.cs.kieler.kiml.klayoutdata.KShapeLayout;
import de.cau.cs.kieler.kiml.options.Direction;
import de.cau.cs.kieler.kiml.options.EdgeRouting;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.klay.layered.p1cycles.CycleBreakingStrategy;
import de.cau.cs.kieler.klay.layered.p2layers.LayeringStrategy;
import de.cau.cs.kieler.klay.layered.p3order.CrossingMinimizationStrategy;
import de.cau.cs.kieler.klay.layered.p4nodes.NodePlacementStrategy;
import de.cau.cs.kieler.klay.layered.properties.FixedAlignment;
import de.cau.cs.kieler.klay.layered.properties.Properties;
import diva.graph.GraphModel;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.vergil.actor.ActorGraphModel;
import ptolemy.vergil.basic.layout.AbstractLayoutConfiguration;
import ptolemy.vergil.basic.layout.AbstractLayoutConfiguration.InteractionMode;
import ptolemy.vergil.basic.layout.ActorLayoutConfiguration;
import ptolemy.vergil.basic.layout.ModalLayoutConfiguration;
import ptolemy.vergil.modal.FSMGraphModel;

/**
 * Responsible for translating layout configuration parameters into the KIELER format.
 * Parameters are read from an instance of the {@link AbstractLayoutConfiguration} attribute,
 * which is attached to composite entities when the configuration dialog is opened.
 *
 * @see AbstractLayoutConfiguration
 * @author Miro Spoenemann, Christoph Daniel Schulze, Ulf Rueegg
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
     * @param parentLayout
     *          the layout of the parent node
     * @param graphModel
     *          the graph model of the current diagram
     * @exception IllegalActionException
     *          if one of the parameters has the wrong type
     */
    public void configureLayout(KShapeLayout parentLayout,
            GraphModel graphModel) throws IllegalActionException {

        // Configuration values specified by user.
        List<AbstractLayoutConfiguration> configAttributes = _compositeEntity
                .attributeList(AbstractLayoutConfiguration.class);

        AbstractLayoutConfiguration layoutConfiguration = null;
        if (!configAttributes.isEmpty()) {
            layoutConfiguration = configAttributes.get(0);
        }

        // Note that when the layout configuration dialog of a model has never been opened,
        // there is no configuration element
        // Otherwise subsequently applied layout options override each other
        // in the following calls, with the user-specified, diagram-specific
        // options being the strongest

        // 1. default values
        _applyDefault(parentLayout);

        // 2. common configuration values
        _applyAbstractConfiguration(parentLayout, layoutConfiguration);

        // 3. model specific configuration values
        if (graphModel instanceof ActorGraphModel) {
            _applyActorConfiguration(parentLayout,
                    (ActorLayoutConfiguration) layoutConfiguration);
        } else if (graphModel instanceof FSMGraphModel) {
            _applyModalConfiguration(parentLayout,
                    (ModalLayoutConfiguration) layoutConfiguration);
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * An initial, default layout configuration applied to every graph.
     *
     * @param parentLayout
     *          the layout of the parent node
     */
    private void _applyDefault(KLayoutData parentLayout)
            throws IllegalActionException {

        // Set general default values.
        parentLayout.setProperty(LayoutOptions.DIRECTION, Direction.RIGHT);
        parentLayout.setProperty(LayoutOptions.BORDER_SPACING, 5.0f);
        parentLayout.setProperty(Properties.EDGE_SPACING_FACTOR, 1.5f);

        parentLayout.setProperty(LayoutOptions.SPACING, SPACING.getDefault());
        parentLayout.setProperty(LayoutOptions.ASPECT_RATIO,
                ASPECT_RATIO.getDefault());

    }

    /**
     * This applies user-specified layout options the graph,
     * if they exist.
     *
     * @param parentLayout
     *          the layout of the parent node
     * @param abstractConfiguration
     *          the container with user-specified options, may be null
     * @exception IllegalActionException
     *          thrown if one of the parameters has the wrong type
     */
    private void _applyAbstractConfiguration(KLayoutData parentLayout,
            AbstractLayoutConfiguration abstractConfiguration)
            throws IllegalActionException {

        if (abstractConfiguration != null) {
            // Whether decorations are to be laid out or left as they are
            BooleanToken decorationsToken = BooleanToken.convert(
                    abstractConfiguration.includeDecorations.getToken());
            parentLayout.setProperty(DECORATIONS,
                    decorationsToken.booleanValue());

            // Spacing between diagram elements
            DoubleToken spacingToken = DoubleToken
                    .convert(abstractConfiguration.spacing.getToken());
            parentLayout.setProperty(SPACING,
                    (float) spacingToken.doubleValue());

            // Target aspect ratio for the diagram
            DoubleToken logAspectToken = DoubleToken
                    .convert(abstractConfiguration.logAspectRatio.getToken());
            parentLayout.setProperty(ASPECT_RATIO,
                    (float) Math.pow(10, logAspectToken.doubleValue()));

            // The interaction mode (constraints the layout according to what the
            // diagram currently looks like)
            InteractionMode interactionMode = (InteractionMode) abstractConfiguration.interactionMode
                    .getChosenValue();
            if (interactionMode != null) {
                // The switch cases fall through on purpose!
                switch (interactionMode) {
                case Full:
                    parentLayout.setProperty(Properties.CROSS_MIN,
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
        }

    }

    /**
     * This applies default layout options for actor models
     * as well as user-specified layout options specific to actor models,
     * if they exist.
     *
     * @param parentLayout
     *          the layout of the parent node
     * @param actorConfiguration
     *          the container with user-specified options, may be null
     * @exception IllegalActionException
     *          thrown if one of the parameters has the wrong type
     */
    private void _applyActorConfiguration(KLayoutData parentLayout,
            ActorLayoutConfiguration actorConfiguration)
            throws IllegalActionException {

        // Set default values for actor models.
        parentLayout.setProperty(LayoutOptions.EDGE_ROUTING,
                EdgeRouting.ORTHOGONAL);

        // User-specified properties
        if (actorConfiguration != null) {
            // The node placement algorithm to use
            BooleanToken minimizeBendsToken = BooleanToken
                    .convert(actorConfiguration.minimizeBends.getToken());
            if (minimizeBendsToken.booleanValue()) {
                parentLayout.setProperty(Properties.NODE_PLACER,
                        NodePlacementStrategy.BRANDES_KOEPF);
            } else {
                parentLayout.setProperty(Properties.NODE_PLACER,
                        NodePlacementStrategy.LINEAR_SEGMENTS);
            }
        }
    }

    /**
     * This applies default layout options for FSM models
     * as well as user-specified layout options specific to FSM models,
     * if they exist.
     *
     * @param parentLayout
     *          the layout of the parent node
     * @param modalConfiguration
     *          the container with user-specified options, may be null
     * @exception IllegalActionException
     *          thrown if one of the parameters has the wrong type
     */
    private void _applyModalConfiguration(KLayoutData parentLayout,
            ModalLayoutConfiguration modalConfiguration)
            throws IllegalActionException {

        // Set default values for modal models.
        parentLayout.setProperty(LayoutOptions.EDGE_ROUTING,
                EdgeRouting.SPLINES);
        parentLayout.setProperty(LayoutOptions.DIRECTION,
                ModalLayoutConfiguration.DEF_DIRECTION);

        float spacing = parentLayout.getProperty(SPACING);
        parentLayout.setProperty(SPACING, spacing / 2f);
        parentLayout.setProperty(Properties.OBJ_SPACING_IN_LAYER_FACTOR, 8f);

        // The node placement algorithm to use
        parentLayout.setProperty(Properties.NODE_PLACER,
                NodePlacementStrategy.BRANDES_KOEPF);
        parentLayout.setProperty(Properties.FIXED_ALIGNMENT,
                FixedAlignment.BALANCED);

        // User-specified
        if (modalConfiguration != null) {

            // For FSMs the user can choose whether he wants to use splines
            // or not. Depending on the choice we have to adapt the layout options
            BooleanToken useSplines = BooleanToken
                    .convert(modalConfiguration.drawSplines.getToken());
            parentLayout.setProperty(SPLINES, useSplines.booleanValue());

            if (useSplines.booleanValue()) {
                // spline routing
                parentLayout.setProperty(LayoutOptions.EDGE_ROUTING,
                        EdgeRouting.SPLINES);
            } else {
                // arc-style routing
                parentLayout.setProperty(SPACING, spacing / 4f);
                parentLayout.setProperty(Properties.OBJ_SPACING_IN_LAYER_FACTOR,
                        20f);
            }

            // direction
            Direction dir = (Direction) modalConfiguration.direction
                    .getChosenValue();
            parentLayout.setProperty(LayoutOptions.DIRECTION, dir);
        }
    }

    /** Layout option that determines whether decoration nodes are included in layout. */
    public static final IProperty<Boolean> DECORATIONS = new Property<Boolean>(
            "ptolemy.vergil.basic.layout.decorations",
            AbstractLayoutConfiguration.DEF_DECORATIONS);

    /** Layout option for the overall spacing between elements. */
    public static final IProperty<Float> SPACING = new Property<Float>(
            LayoutOptions.SPACING,
            (float) AbstractLayoutConfiguration.DEF_SPACING);

    /** Layout option for the aspect ratio of connected components. */
    public static final IProperty<Float> ASPECT_RATIO = new Property<Float>(
            LayoutOptions.ASPECT_RATIO,
            (float) AbstractLayoutConfiguration.DEF_ASPECT_RATIO);

    /** Layout option that determines whether splines should be used for FSMs. */
    public static final IProperty<Boolean> SPLINES = new Property<Boolean>(
            "ptolemy.vergil.basic.layout.splines",
            ModalLayoutConfiguration.DEF_USE_SPLINES);

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** the parent entity from which the layout configuration is read. */
    private CompositeEntity _compositeEntity;

}
