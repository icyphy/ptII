/* An actor that read an XSLT file and apply it to its input.

@Copyright (c) 2003-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.xslt;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;

import ptolemy.actor.lib.Transformer;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.XMLToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// XSLTTransformer

/**
   This actor reads an XSLT file and apply it to a dom tree. The file or
   URL is specified using any form acceptable to the FileParameter class.

   <p>Currently, this actor requires the
   <a href="http://saxon.sourceforge.net/">Saxon</a> XSLT processor
   so as to ensure reproducible results.  This restriction may
   be relaxed in later versions of this actor.

   <p>FIXME: what should the type of the input/output ports be???.

   @see ptolemy.actor.lib.javasound.AudioReader
   @author  Yang Zhao, Christopher Hylands Brooks
   @version $Id$
   @since Ptolemy II 4.0
   @Pt.ProposedRating Red (liuj)
   @Pt.AcceptedRating Red (liuj)
 */
public class XSLTransformer extends Transformer {
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

        styleSheetParameters = new PortParameter(this, "styleSheetParameters");
        styleSheetParameters.setTypeAtMost(new RecordType(new String[0],
                new Type[0]));
        styleSheetParameters.setExpression("emptyRecord()");
        styleSheetFile = new FileParameter(this, "styleSheetFile");

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The file name or URL from which to read.  This is a string with
     *  any form accepted by FileParameter.
     *  @see FileParameter
     */
    public FileParameter styleSheetFile;

    /** The parameters to be used in the stylesheet. This is a record
     *  that defaults to "emptyRecord()", an expression language command
     *  that returns an empty record.
     *  For example, if the parameter used in the style sheet is named
     *  <i>a</i> with type <i>int</i>, then the styleSheetParameters has
     *  type <i>{a = int}</i>. If the style sheet has multiple parameters,
     *  then each of them is represented as a field of the record.
     */
    public PortParameter styleSheetParameters;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then set the filename public member.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        XSLTransformer newObject = (XSLTransformer) super.clone(workspace);
        newObject.input.setTypeEquals(BaseType.XMLTOKEN);
        newObject.output.setTypeEquals(BaseType.STRING);
        return newObject;
    }

    /** Consume an XMLToken from the input and apply the XSL transform
     *  specified by the styleSheetFile parameter.  If the styleSheetFile parameter
     *  does not name a file, then the input is copied to the output
     *  without modification.
     *  @exception IllegalActionException If the parent class throws it
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        styleSheetParameters.update();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        javax.xml.transform.Result result = new javax.xml.transform.stream.StreamResult(
                out);

        if (_debugging) {
            _debug("--- open an output stream for the result. \n");
        }

        if (_transformer != null) {
            RecordToken parameters = (RecordToken) styleSheetParameters
                    .getToken();
            if (parameters != null) {
                Iterator labels = parameters.labelSet().iterator();

                while (labels.hasNext()) {
                    String name = (String) labels.next();
                    Token token = parameters.get(name);
                    if (token instanceof StringToken) {
                        StringToken s = (StringToken) token;
                        _transformer.setParameter(name, s.stringValue());
                    } else {
                        _transformer.setParameter(name, token.toString());
                    }
                }
            }
            for (int i = 0; i < input.getWidth(); i++) {
                if (input.hasToken(i)) {
                    XMLToken in = (XMLToken) input.get(i);
                    Document doc = in.getDomTree();

                    try {
                        javax.xml.transform.Source xmlSource = new javax.xml.transform.dom.DOMSource(
                                doc);
                        _transformer.transform(xmlSource, result);

                        if (_debugging) {
                            _debug("--- transform the xmlSource: "
                                    + in.toString() + "\n");
                        }

                        if (out != null) {
                            if (_debugging) {
                                _debug("--- moml change request string: "
                                        + out.toString() + "\n");
                            }

                            StringToken outputToken = new StringToken(
                                    out.toString());
                            output.broadcast(outputToken);

                            if (_debugging) {
                                _debug("--- change request string token "
                                        + "send out. \n");
                            }
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
        } else {
            // If there is no transformer, then output the xml string.
            for (int i = 0; i < input.getWidth(); i++) {
                if (input.hasToken(i)) {
                    XMLToken in = (XMLToken) input.get(i);
                    output.broadcast(new StringToken(in.toString()));
                }
            }
        }
    }

    /** Open the XSL file named by the styleSheetFile parameter and
     *  set up the transformer.
     *  @exception IllegalActionException If the TransformFactory
     *  class name does not start with net.sf.saxon.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _xsltSource = null;
        _transformer = null;

        try {
            BufferedReader reader;

            //          Ignore if the styleSheetFile is blank.
            if (styleSheetFile.getExpression().trim().equals("")) {
                reader = null;
            } else {
                reader = styleSheetFile.openForReading();
            }

            if (reader != null) {
                _xsltSource = new javax.xml.transform.stream.StreamSource(
                        reader);
            } else {
                _xsltSource = null;
            }

            if (_debugging) {
                _debug("processing xsltSource change in " + getFullName());
            }

            if (_xsltSource != null) {
                _transformerFactory = javax.xml.transform.TransformerFactory
                        .newInstance();

                /* if (!_transformerFactory.getClass().getName().startsWith(
                   "net.sf.saxon")) {
                   throw new IllegalActionException(
                   this,
                   "The XSLTransformer actor works best\nwith "
                   + "saxon7.jar.\n"
                   + "The transformerFactory was '"
                   + _transformerFactory.getClass().getName()
                   + "'.\nIf saxon7.jar was in the classpath, then "
                   + "it should have\nstarted with "
                   + "\"net.sf.saxon\".\n"
                   + "If this actor does not use "
                   + "saxon, then the results will be "
                   + "different between\nruns that "
                   + "use saxon and runs that "
                   + "do not.\nDetails:\n"
                   + "This actor uses "
                   + "javax.xml.transform.TransformerFactory.\nThe "
                   + "concrete TransformerFactory class can be "
                   + "adjusted by\nsetting the "
                   + "javax.xml.transform.TransformerFactory "
                   + "property or by\nreading in a jar file that "
                   + "has the appropriate\nService Provider set.\n"
                   + "(For details about Jar Service Providers,\nsee "
                   + "http://download.oracle.com/javase/6/docs/technotes/guides/jar/jar.html\n"
                   + "The saxon7.jar file includes a\n"
                   + "META-INF/services/javax.xml.transform.TransformerFactory "
                   + "\nfile that sets the TransformerFactory "
                   + "class name start with 'net.sf.saxon'.");
                   }*/

                _transformer = _transformerFactory.newTransformer(_xsltSource);

                if (_debugging) {
                    _debug("1 processing xsltSource change in " + getFullName());
                }
            } else {
                _transformer = null;
            }
        } catch (Throwable throwable) {
            throw new IllegalActionException(this, throwable,
                    "Failed to initialize.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    private javax.xml.transform.Source _xsltSource;

    private javax.xml.transform.TransformerFactory _transformerFactory;

    private javax.xml.transform.Transformer _transformer;
}
