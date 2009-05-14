/*
Below is the copyright agreement for the Ptolemy II system.
Version: $Id$

Copyright (c) 2009 The Regents of the University of California.
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

public class LatticeElement extends State {

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

        isAcceptableSolution = new Parameter(this, "isAcceptableSolution", BooleanToken.TRUE);
        isAcceptableSolution.setTypeEquals(BaseType.BOOLEAN);

        solutionColor = new ColorAttribute(this, "solutionColor");
        solutionColor.setToken("{1.0, 1.0, 1.0, 1.0}");

        /*_icon = */ new LatticeElementIcon(this, "LatticeElementIcon");

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    //private LatticeElementIcon _icon;

    public ColorAttribute solutionColor;

    public Parameter isAcceptableSolution;

}
