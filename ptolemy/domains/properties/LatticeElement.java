/*
 * A lattice element that represents a property value.
 * 
 * Below is the copyright agreement for the Ptolemy II system.
 * 
 * Copyright (c) 2009 The Regents of the University of California. All rights
 * reserved.
 * 
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */
package ptolemy.domains.properties;

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.modal.kernel.State;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

/**
 * A lattice element that represents a property value used in property
 * resolution. It has a color attribute that the solver uses to color model
 * objects. It also has an acceptability parameter the solver uses to give the
 * user warnings.
 * 
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class LatticeElement extends State {

    /**
     * Construct a lattice element with the given name contained by the
     * specified composite entity. The container argument must not be null, or a
     * NullPointerException will be thrown. This lattice element will use the
     * workspace of the container for synchronization and version counts. If the
     * name argument is null, then the name is set to the empty string. The
     * lattice element is an acceptable solution by default, and its color is
     * set to be solid white.
     * @param container The specified container.
     * @param name The name for the lattice element.
     * @exception IllegalActionException If the lattice element cannot be
     * contained by the proposed container, or if there is a problem setting the
     * default parameters.
     * @exception NameDuplicationException If the name coincides with that of an
     * entity already in the container.
     */
    public LatticeElement(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        refinementName.setVisibility(Settable.NONE);

        //isInitialState.setDisplayName("isInitialEvent");
        //isFinalState.setDisplayName("isFinalEvent");
        isInitialState.setVisibility(Settable.NONE);
        isInitialState.setPersistent(false);
        isInitialState.setToken(BooleanToken.FALSE);
        isFinalState.setVisibility(Settable.NONE);
        isFinalState.setPersistent(false);

        isAcceptableSolution = new Parameter(this, "isAcceptableSolution",
                BooleanToken.TRUE);
        isAcceptableSolution.setTypeEquals(BaseType.BOOLEAN);

        solutionColor = new ColorAttribute(this, "solutionColor");
        solutionColor.setToken("{1.0, 1.0, 1.0, 1.0}");

        /* _icon = */new LatticeElementIcon(this, "LatticeElementIcon");

    }

    ///////////////////////////////////////////////////////////////////
    ////                   ports and parameters                    ////

    //private LatticeElementIcon _icon;

    /**
     * The color of the lattice element. The property solver uses this to color
     * model objects that is tagged with this lattice element value.
     */
    public ColorAttribute solutionColor;

    /**
     * Indicate whether this lattice element is an acceptable solution. If this
     * parameter is false and model object is tagged with this lattice element
     * value, then the property solver gives an exception to warn the user.
     */
    public Parameter isAcceptableSolution;

}
