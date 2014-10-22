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
package ptolemy.vergil.basic.layout;

import ptolemy.actor.parameters.DoubleRangeParameter;
import ptolemy.data.expr.ChoiceParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

/**
 * An attribute for parameters of automatic layout. This is read by the KIELER
 * layout action to generate a configuration for the layout algorithm.
 *
 * @see ptolemy.vergil.basic.layout.kieler.KielerLayoutAction
 * @author Miro Spoenemann, Christoph Daniel Schulze
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (msp)
 * @Pt.AcceptedRating Red (msp)
 */
public class LayoutConfiguration extends Attribute {

    /** Available modes of user interaction. */
    public enum InteractionMode {
        /** No user interaction: full automatic layout. */
        None,
        /** User positioning affects cycle detection. */
        Cycles,
        /** User positioning affects cycle detection and node layering. */
        Columns,
        /** User positioning affects cycle detection, node layering, and node order. */
        Full;
    }

    /**
     * Create and initialize a layout configuration.
     *
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public LayoutConfiguration(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        useOldAlgorithm = new Parameter(this, "useOldAlgorithm");
        useOldAlgorithm.setDisplayName("Use old algorithm");
        useOldAlgorithm.setTypeEquals(BaseType.BOOLEAN);
        useOldAlgorithm.setExpression(Boolean.toString(DEF_OLD_ALGORITHM));
        useOldAlgorithm.setVisibility(Settable.EXPERT);

        includeDecorations = new Parameter(this, "includeDecorations");
        includeDecorations.setDisplayName("Include decorations");
        includeDecorations.setTypeEquals(BaseType.BOOLEAN);
        includeDecorations.setExpression(Boolean.toString(DEF_DECORATIONS));

        minimizeBends = new Parameter(this, "minimizeBends");
        minimizeBends.setDisplayName("Minimize edge bends");
        minimizeBends.setTypeEquals(BaseType.BOOLEAN);
        minimizeBends.setExpression(Boolean.toString(DEF_MINIMIZE_BENDS));
        minimizeBends.setVisibility(Settable.EXPERT);

        //        optimizeRelations = new Parameter(this, "optimizeRelations");
        //        optimizeRelations.setDisplayName("Optimize relations");
        //        optimizeRelations.setTypeEquals(BaseType.BOOLEAN);
        //        optimizeRelations.setExpression(Boolean.toString(DEF_OPTIMIZE_RELATIONS));
        // DEBUG Start
        // This should be commented back in once the feature is finished.
        //        optimizeRelations.setVisibility(Settable.EXPERT);
        // DEBUG End

        spacing = new DoubleRangeParameter(this, "spacing");
        spacing.setDisplayName("Object spacing");
        spacing.min.setExpression("2.0");
        spacing.max.setExpression("50.0");
        spacing.setExpression(Double.toString(DEF_SPACING));
        spacing.min.setVisibility(Settable.NONE);
        spacing.max.setVisibility(Settable.NONE);
        spacing.minLabel.setVisibility(Settable.NONE);
        spacing.maxLabel.setVisibility(Settable.NONE);
        spacing.precision.setVisibility(Settable.NONE);

        logAspectRatio = new DoubleRangeParameter(this, "logAspectRatio");
        logAspectRatio.setDisplayName("Aspect ratio");
        logAspectRatio.min.setExpression("-1.0");
        logAspectRatio.max.setExpression("1.0");
        logAspectRatio.setExpression(Double.toString(Math
                .log10(DEF_ASPECT_RATIO)));
        logAspectRatio.minLabel.setExpression("Narrow");
        logAspectRatio.maxLabel.setExpression("Wide");
        logAspectRatio.min.setVisibility(Settable.NONE);
        logAspectRatio.max.setVisibility(Settable.NONE);
        logAspectRatio.minLabel.setVisibility(Settable.NONE);
        logAspectRatio.maxLabel.setVisibility(Settable.NONE);
        logAspectRatio.precision.setVisibility(Settable.NONE);

        interactionMode = new ChoiceParameter(this, "interactionMode",
                InteractionMode.class);
        interactionMode.setDisplayName("Interaction mode");
        interactionMode.setExpression(DEF_INTERACTION_MODE.toString());

        helpURL = new StringParameter(this, "_helpURL");
        helpURL.setExpression("ptolemy/vergil/basic/layout/layout.htm");
        helpURL.setVisibility(Settable.NONE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       public parameters                   ////

    /** Whether to use Ptolemy's original layout algorithm. */
    public Parameter useOldAlgorithm;

    /** Whether to include unconnected nodes such as comments. */
    public Parameter includeDecorations;

    /** Whether bends are minimized.  The default value is true. */
    public Parameter minimizeBends;

    //    /** Whether to try to optimize relation vertices or not. */
    //    public Parameter optimizeRelations;

    /** The overall spacing between graph elements. */
    public DoubleRangeParameter spacing;

    /** The aspect ratio for placement of connected components (logarithmic). */
    public DoubleRangeParameter logAspectRatio;

    /** Mode of user interaction: whether user positioning is allowed to affect the layout. */
    public ChoiceParameter interactionMode;

    /** Customized help file to be displayed by the layout configuration dialog. */
    public StringParameter helpURL;

    /** Default value for useOldAlgorithm. */
    public static final boolean DEF_OLD_ALGORITHM = false;

    /** Default value for includeDecorations. */
    public static final boolean DEF_DECORATIONS = true;

    /** Default value for minimizeBends. */
    public static final boolean DEF_MINIMIZE_BENDS = true;

    //    /** Default value for optimizeRelations. */
    //    public static final boolean DEF_OPTIMIZE_RELATIONS = true;

    /** Default value for spacing. */
    public static final double DEF_SPACING = 10.0;

    /** Default value for aspectRatio (non-logarithmic). */
    public static final double DEF_ASPECT_RATIO = 1.6;

    /** Default value for interaction mode. */
    public static final InteractionMode DEF_INTERACTION_MODE = InteractionMode.None;

}
