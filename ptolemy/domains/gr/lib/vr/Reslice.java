/* An actor that reads an array of images.

 @Copyright (c) 2005 The Regents of the University of California.
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

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.domains.gr.lib.vr;

import ij.ImagePlus;
import ij.plugin.Slicer;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
////Slicer

/**
 An actor that reads an array of images.

 @see ptolemy.actor.lib.medicalimaging

 @author T. Crawford
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red
 @Pt.AcceptedRating Red

 */
public class Reslice extends TypedAtomicActor {
    /**Construct an actor with the given container and name.
     * @param container The container
     * @param name The name of this actor
     * @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     * @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Reslice(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.OBJECT);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.OBJECT);

        xResolution = new Parameter(this, "xResolution");
        xResolution.setExpression("256");
        xResolution.setTypeEquals(BaseType.INT);

        yResolution = new Parameter(this, "yResolution");
        yResolution.setExpression("256");
        yResolution.setTypeEquals(BaseType.INT);

        stackSize = new Parameter(this, "stackSize");
        stackSize.setExpression("50");
        stackSize.setTypeEquals(BaseType.INT);
    }

    ////////////////////////////////////////////////////////////////////
    ////////               ports and parameters                  ////////
    //public FilePortParameter input;
    public TypedIOPort input;

    public TypedIOPort output;

    public Parameter xResolution;

    public Parameter yResolution;

    public Parameter stackSize;

    ////////////////////////////////////////////////////////////////////
    ////////                public methods                     ////////

    /** Output the data read in the prefire.
     *  @exception IllegalActionException If there's no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        ObjectToken objectToken = (ObjectToken) input.get(0);
        ImagePlus imagePlus = (ImagePlus) objectToken.getValue();
        Slicer slicer = new Slicer();
        _imagePlus = slicer.reslice(imagePlus);
        output.broadcast(new ObjectToken(_imagePlus));
    }

    public void initialize() throws IllegalActionException {
        /*_xResolution = */((IntToken) xResolution.getToken()).intValue();
        /*_yResolution = */((IntToken) yResolution.getToken()).intValue();
        /*_stackSize = */((IntToken) stackSize.getToken()).intValue();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    //Image that is readin
    private ImagePlus _imagePlus;

    //private int _stackSize;

    //private int _xResolution;

    //private int _yResolution;
}
