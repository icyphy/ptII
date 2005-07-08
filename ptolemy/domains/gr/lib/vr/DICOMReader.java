/* An actor that reads DICOM files.

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

import ij.IJ;
import ij.ImagePlus;

import java.awt.Image;
import java.net.URL;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.FilePortParameter;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.data.AWTImageToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
////DICOMReader
/**
 An actor that reads DICOM files.

 @see ptolemy.actor.lib.medicalimaging

 @author T. Crawford
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red
 @Pt.AcceptedRating Red

 */
public class DICOMReader extends TypedAtomicActor {
    /**Construct an actor with the given container and name.
     * @param container The container
     * @param name The name of this actor
     * @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     * @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */

    public DICOMReader(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        fileOrURL = new FilePortParameter(this, "fileOrURL");

        output = new TypedIOPort(this, "output");
        output.setOutput(true);
        output.setTypeEquals(BaseType.OBJECT);

    }

    ////////////////////////////////////////////////////////////////////
    ////////               ports and parameters                  ////////

    public FilePortParameter fileOrURL;

    public TypedIOPort output;

    ////////////////////////////////////////////////////////////////////
    ////////                public methods                     ////////

    /** Output the data read in the prefire.
     *  @exception IllegalActionException If there's no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        output.broadcast(new AWTImageToken(_image));
    }

    public void initialize() throws IllegalActionException {
        _parameterPort = fileOrURL.getPort();
    }

    public boolean prefire() throws IllegalActionException {
        super.prefire();
        if (_parameterPort.hasToken(0)) {
            fileOrURL.update();
            _readImage();
            return true;
        } else {
            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private void _readImage() throws IllegalActionException {
        _url = fileOrURL.asURL();

        if (_url == null) {
            throw new IllegalActionException("sourceURL was null");
        }
        _fileRoot = _url.getFile();
        if (_imagePlus == null) {
            _image = ((ImagePlus) IJ.runPlugIn("ij.plugin.DICOM", _fileRoot))
                    .getImage();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** Image that is read in. */
    private ImagePlus _imagePlus;

    //  The URL as a string.
    private String _fileRoot;

    // Image that is read in.
    private Image _image;

    // The URL of the file.
    private URL _url;

    private ParameterPort _parameterPort;

}
