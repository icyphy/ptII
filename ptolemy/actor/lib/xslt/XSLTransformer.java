/* An actor that read an XSLT file and apply it to its input.

@Copyright (c) 2003 The Regents of the University of California.
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
@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (liuj@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.xslt;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.FileAttribute;
import ptolemy.kernel.util.*;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;

//////////////////////////////////////////////////////////////////////////
//// XSLTTransformer
/**
This actor reads an XSLT file and apply it to a dom tree. The file or
URL is specified using any form acceptable to the FileAttribute class.

<p>FIXME: what should the type of the input/output ports be???.

@see ptolemy.actor.lib.javasound.AudioReader
@author  Yang Zhao
@version $ $
@since Ptolemy II 1.0
*/
public class XSLTransformer extends Transformer{

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public XSLTransformer(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Set the type of the input port.
        //input.setMultiport(true);
        input.setTypeEquals(BaseType.XMLTOKEN);
        // Set the type of the output port.
        //output.setMultiport(true);
        output.setTypeEquals(BaseType.STRING);

        fileOrURL = new FileAttribute(this, "fileOrURL");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The URL of the file to read from. This parameter contains
     *  a StringToken.
     */
    //Note: it is used to be this parameter for specifying the xslt file.
    //public Parameter xsltURL;

    /** The file name or URL from which to read.  This is a string with
     *  any form accepted by FileAttribute.
     *  @see FileAttribute
     */
    public FileAttribute fileOrURL;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then set the filename public member.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        XSLTransformer newObject = (XSLTransformer)super.clone(workspace);
        newObject.input.setTypeEquals(BaseType.XMLTOKEN);
        newObject.output.setTypeEquals(BaseType.STRING);
        return newObject;
    }


    public void fire() throws IllegalActionException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        javax.xml.transform.Result result = new javax.xml.transform.stream.StreamResult(out);
        System.out.println("--- open an output stream for the result. \n");
        if (_transformer != null ) {
            for (int i = 0; i < input.getWidth(); i++) {
                if (input.hasToken(i)) {
                    XMLToken in = (XMLToken)input.get(i);
                    Document doc = in.getDomTree();
                    try {
                        javax.xml.transform.Source xmlSource
                            = new javax.xml.transform.dom.DOMSource(doc);
                        _transformer.transform(xmlSource, result);
                        //System.out.println("--- transform the xmlSource: " + in.toString() + "\n");
                        if (out != null) {
                            //System.out.println("--- moml change request string: " + out.toString() + "\n");
                            StringToken t = new StringToken(out.toString());
                            output.broadcast(t);
                            //System.out.println("--- change request string token send out. \n");
                        }
                    } catch (TransformerException ex) {
                        throw new IllegalActionException(this, ex,
                                "Failed  to process '" + in + "'");
                    }
                    try {
                        out.flush();
                        out.close();
                    } catch (IOException ex) {
                        throw new IllegalActionException(this, ex,
                                "Failed  to close or flush '" + out + "'");
                    }
                }
            }
        }
        //if there is no transformer, than output the xml string.
        else {
            for (int i = 0; i < input.getWidth(); i++) {
                if (input.hasToken(i)) {
                    XMLToken in = (XMLToken)input.get(i);
                    output.broadcast(new StringToken(in.toString()));
                }
            }
        }
    }

    /** Open the file at the URL.
     *  @exception IllegalActionException Not thrown in this base class
     */
    public void initialize() throws IllegalActionException {
        _xsltSource = null;
        _transformer = null;
        try {
            BufferedReader reader;
            // Ignore if the fileOrUL is blank.
            if (fileOrURL.getExpression().trim().equals("")) {
                reader = null;
            } else {
                reader = fileOrURL.openForReading();
            }
            if (reader != null) {
                _xsltSource = new javax.xml.transform.stream.StreamSource(reader);
            }
            else {
                _xsltSource = null;
            }
            //System.out.println("processing xsltSource change in " + getFullName());
            if (_xsltSource != null) {
                _transformerFactory = javax.xml.transform.TransformerFactory.newInstance();
                _transformer = _transformerFactory.newTransformer(_xsltSource);
				//System.out.println("1 processing xsltSource change in " + getFullName());
            }
            else {
                _transformer = null;
            }
        } catch (java.lang.Exception ex) {
            //System.out.println("exception class is " + ex.getClass());
            throw new IllegalActionException(this, ex.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    private javax.xml.transform.Source _xsltSource;

    private javax.xml.transform.TransformerFactory _transformerFactory;

    private javax.xml.transform.Transformer _transformer;

    // String for the URL.
    //private String _source;

}
