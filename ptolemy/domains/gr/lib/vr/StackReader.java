/* An actor that reads an array of images.

 Copyright (c) 1998-2005 The Regents of the University of California.
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

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.domains.gr.lib.vr;

import ij.ImagePlus;
import ij.ImageStack;

import ij.process.ColorProcessor;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.FilePortParameter;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.data.AWTImageToken;
import ptolemy.data.IntToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.lib.SDFTransformer;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.awt.Image;
import java.net.URL;

//////////////////////////////////////////////////////////////////////////
////StackReader

/**
 An actor that reads an array of images.

 <<<<<<< StackReader.java
 @author T. Crawford
 @version
 @since
 @Pt.ProposedRating Red
 @Pt.AcceptedRating Red

 */
public class StackReader extends SDFTransformer {
    /**Construct an actor with the given container and name.
     * @param container The container
     * @param name The name of this actor
     * @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     * @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public StackReader(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input_tokenConsumptionRate.setExpression("stackSize");

        output.setTypeEquals(BaseType.OBJECT);

        stackSize = new Parameter(this, "stackSize");
        stackSize.setExpression("50");
        stackSize.setTypeEquals(BaseType.INT);

        xResolution = new Parameter(this, "xResolution");
        xResolution.setExpression("256");
        xResolution.setTypeEquals(BaseType.INT);

        yResolution = new Parameter(this, "yResolution");
        yResolution.setExpression("256");
        yResolution.setTypeEquals(BaseType.INT);
    }

    ////////////////////////////////////////////////////////////////////
    ////////               ports and parameters                  ////////
    //public FilePortParameter input;

    /* Parameter allowing user to define resolution of image in stack */
    public Parameter xResolution;

    /* Parameter allowing user to define resolution of images in stack */
    public Parameter yResolution;

    /*Parameter allowing user to define number of images in stack */
    public Parameter stackSize;

    ////////////////////////////////////////////////////////////////////
    ////////                public methods                     ////////

    /** Send the stack to the output as an ObjectToken.
     * This has the side effect of reading in the stack.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        _readStack();
        _imagePlus = new ImagePlus("Image Stack", _imageStack);
        System.out.println("stackSize = " + _imageStack.getSize());

        // output.broadcast(new AWTImageToken(_image));
        output.broadcast(new ObjectToken(_imagePlus));
    }

    /** Initialize variables to parameter values.
     * @exception IllegalActionException If there's no director.
     */
    public void initialize() throws IllegalActionException {
        // _parameterPort =  input.getPort();
        _xResolution = ((IntToken) xResolution.getToken()).intValue();
        _yResolution = ((IntToken) yResolution.getToken()).intValue();
        _stackSize = ((IntToken) stackSize.getToken()).intValue();
    }

    /*  public boolean prefire() throws IllegalActionException {
     public boolean prefire() throws IllegalActionException {
     Token rateToken = input_tokenConsumptionRate.getToken();
     int required = ((IntToken) rateToken).intValue();

     // Derived classes may convert the input port to a multiport.
     for (int i = 0; i < input.getWidth(); i++) {
     if (!input.hasToken(i, required)) {
     if (_debugging) {
     _debug("Called prefire(), which returns false");
     }

     return false;
     }
     }

     return super.prefire();
     }
     }*/

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    /** Read images into stack.
     * @exception IllegalActionException If URL is null.
     */
    private void _readStack() throws IllegalActionException {
        _imageStack = new ImageStack(_xResolution, _yResolution);

        Token[] token = input.get(0, _stackSize);

        for (int i = 0; i < _stackSize; i++) {
            _fileRoot = new String[_stackSize];
            _fileRoot[i] = ((StringToken) token[i]).stringValue();
            System.out.println("_fileRoot = " + _fileRoot[i]);

            if (_fileRoot[i] == null) {
                throw new IllegalActionException("sourceURL was null");
            }

            //_fileRoot = _url.getFile();
            //if (imagePlus == null) {
            //FIXME Should check each image to see if valid
            ImagePlus imagePlus = new ImagePlus(_fileRoot[i]);

            if (imagePlus != null) {
                _image = imagePlus.getImage();
                _colorProcessor = new ColorProcessor(_image);
                _imageStack.addSlice(_fileRoot[i], _colorProcessor);
                imagePlus = null;
                System.out.println("stackSize = " + _imageStack.getSize());
            } else {
                throw new IllegalActionException("_image is null");
            }
        }
    }

    //FIXME Is this necessary?

    /** Return the type constraint that the type of the elements of the
     *  output array is no less than the type of the input port.
     *  @return A list of inequalities.
     */

    /*public List typeConstraintList() {
     BaseType outType = (BaseType) output.getType();
     InequalityTerm elementTerm = outType.getTypeTerm();
     Inequality ineq = new Inequality(input.getTypeTerm(), elementTerm);

     List result = new LinkedList();
     result.add(ineq);
     return result;
     }*/

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    /** Image that is being read in ImageJ format. */
    private ImagePlus _imagePlus;

    /** ColorProcessor of image being read. */
    private ColorProcessor _colorProcessor;

    /** Stack being created. */
    private ImageStack _imageStack;

    /** The URL represented as a string. */
    private String[] _fileRoot;

    /**  Image that is being read */
    private Image _image;

    /**  The URL of the file. */
    private URL _url;

    /** Number of images in stack as defined by user */
    private int _stackSize;

    /** X resolution of the images */
    private int _xResolution;

    /** Y resolution of the images */
    private int _yResolution;
}
