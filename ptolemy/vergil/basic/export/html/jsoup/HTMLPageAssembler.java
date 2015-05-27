/* An actor that assemble multiple inputs to a HTML page.

 Copyright (c) 2003-2014 The Regents of the University of California.
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

package ptolemy.vergil.basic.export.html.jsoup;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.ParseError;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
////HTMLPageAssembler

/**
<p>
HTMLPageAssembler reads contents from its ports and appends them into the
corresponding parts in the template file that must satisfy the following
requirement: for each port, there must be a corresponding DOM object whose ID is
the same as the port name in the template file, or in the content provided to
another port that is created before this port.
</p>
<p>
HTMLPageAssembler processes the ports in the order that they are added to it.
Each port can consists of a single or multiple channels. In the latter case,
contents from multiple channels are appended in the order that they are
connected to this port.
</p>
<p>
The content for a channel can be a string or an array of strings. HTML scripts,
such as JavaScript, can also be part of the content. For a long content, it is
better to first store the content in a separated file, then read this file using
the FileReader actor to provide the content to the port. A demo is available at
$PTII/ptolemy/vergil/basic/export/html/demo/PageAssembler/PageAssembler.xml. If the content is
provided through a StringConst actor, only the tags defined in the standard Java
library (javax.swing.text.html.HTML.Tag) can be supported. If the content is
read from a file, then all valid HTML tags can be supported. Unknown tags are
ignored without throwing any exceptions.
</p>
<p>
The content of the final HTML page is sent to the output port and saved
to a file if specified.
</p>
<p>
For more information, please refer to "Manual for Creating Web Pages" in Ptolemy.
</p>

@author Baobing (Brian) Wang, Elizabeth Latronico
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Yellow (bwang)
@Pt.AcceptedRating Yellow (bwang)
 */

public class HTMLPageAssembler extends TypedAtomicActor {

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public HTMLPageAssembler(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        template = new FileParameter(this, "template");
        template.setDisplayName("Template HTML file");
        template.setExpression("template.html");

        htmlTitle = new StringParameter(this, "htmlTitle");
        htmlTitle.setDisplayName("HTML page title");
        htmlTitle.setExpression("Page Generated by HTMLPageAssembler");

        saveToFile = new Parameter(this, "saveToFile");
        saveToFile.setDisplayName("Save the new HTML page to a separate file");
        saveToFile.setTypeEquals(BaseType.BOOLEAN);
        saveToFile.setExpression("false");

        outputFile = new FileParameter(this, "outputFile");
        outputFile.setExpression("result.html");
        outputFile.setDisplayName("Output file");

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.STRING);

        newline = new Parameter(this, "newline");
        newline.setExpression("property(\"line.separator\")");
        newline.setVisibility(Settable.NONE);

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The title of the generated HTML page.  The default value
     *  is the string "Page Generated by HTMLPageAssembler".
     */
    public StringParameter htmlTitle;

    /** The output port, which is of type String. */
    public TypedIOPort output;

    /** The file to save the content of the generated page.
     *  The default value is "result.html".
     */
    public FileParameter outputFile;

    /**
     * Specify whether the content of the generated page should be save to a
     * separated file.  The default value is a boolean with the value false.
     */
    public Parameter saveToFile;

    /** The template file. The default value is "template.html". */
    public FileParameter template;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        HTMLPageAssembler newObject = (HTMLPageAssembler) super
                .clone(workspace);
        // Not sure why this is necessary, but it stops
        // (cd $PTII/ptolemy/configs/test; $PTII/bin/ptjacl allConfigs.tcl) from failing.
        newObject.newline = (Parameter) newObject.getAttribute("newline");
        return newObject;
    }

    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        StringBuffer content = new StringBuffer("");

        try {
            // Parse HTML.  Throw an exception if syntax is invalid
            // Create a parser instead of using static method Jsoup.parse,
            // since static method does not save error list
            Parser htmlParser = Parser.htmlParser();

            // Max of 1 error, since this actor currently only reports error/OK
            htmlParser.setTrackErrors(1);

            // htmlParser does not offer a method to read from a file,
            // so store file data in a string.  Copied from fileReader
            BufferedReader reader = null;
            StringBuffer lineBuffer = new StringBuffer();
            try {
                reader = template.openForReading();

                String newlineValue = ((StringToken) newline.getToken())
                        .stringValue();
                while (true) {
                    String line = reader.readLine();

                    if (line == null) {
                        break;
                    }

                    lineBuffer = lineBuffer.append(line);
                    lineBuffer = lineBuffer.append(newlineValue);
                }
            } catch (Throwable throwable) {
                throw new IllegalActionException(this, throwable,
                        "Failed to read '" + template.getValueAsString() + "'");
            } finally {
                if (template != null) {
                    template.close();
                }
            }

            _document = htmlParser.parseInput(lineBuffer.toString(), "");

            List<ParseError> errors = htmlParser.getErrors();
            if (errors != null && !errors.isEmpty()) {
        	StringBuffer messages = new StringBuffer();
        	for (ParseError error : errors) {
        	    messages.append(error.toString());
        	    // messages.append(error.getErrorMessage() + " at " + error.getPosition() + "\n");
        	}
                throw new IllegalActionException(this, "Template file '"
                        + template.getValueAsString()
                        + "' contains HTML syntax errors:\n"
                        + messages);
            }

            // Set the page title
            _document.title(htmlTitle.stringValue().trim());

            /*
             * Insert the content from each port to its corresponding element.
             * Throw an exception if an element cannot be found whose id
             * attribute matches the port name.
             */

            List<TypedIOPort> portList = inputPortList();
            for (TypedIOPort port : portList) {
                String id = port.getName();

                Elements elements = _document.select("#" + id);

                // Throw exception if an element with this id is not found
                if (elements == null || elements.isEmpty()) {
                    throw new IllegalActionException(this,
                            "Cannot find an element with id = '" + id
                            + "' in the template file.");
                }

                // Throw exception if multiple elements with this id are found
                // (for valid HTML5, each id must be unique within the document)
                // http://dev.w3.org/html5/markup/global-attributes.html
                // The parser does not seem to throw an error, though
                // Note this only checks elements we are inserting content into
                // TODO:  Check all elements?
                if (elements.size() > 1) {
                    throw new IllegalActionException(this, "Id = \"" + id
                            + "\" is not unique in the "
                            + "template file.  Please make sure each"
                            + " element has a unique id (or none).");
                }

                for (int i = port.getWidth() - 1; i >= 0; i--) {
                    Token token = port.get(i);
                    StringBuffer htmlText = new StringBuffer();
                    if (token instanceof ArrayToken) {
                        ArrayToken array = (ArrayToken) token;
                        Token[] lines = array.arrayValue();
                        for (Token line : lines) {
                            htmlText.append(((StringToken) line).stringValue()
                                    + "\n");
                        }
                    } else {
                        htmlText.append(((StringToken) token).stringValue()
                                + "\n");
                    }

                    // Check that each fragment is valid html
                    // Wrap fragment in minimal document and try to parse
                    String testFragment = "<!DOCTYPE html><html><head></head><body>"
                            + htmlText.toString() + " </body></html>";
                    htmlParser.parseInput(testFragment, "");

                    errors = htmlParser.getErrors();
                    if (errors != null && !errors.isEmpty()) {
                	StringBuffer messages = new StringBuffer();
                	for (ParseError error : errors) {
                	    messages.append(error.toString());
                	    // messages.append(error.getErrorMessage() + " at " + error.getPosition() + "\n");
                	}
                        throw new IllegalActionException(this, "Input '"
                                + htmlText.toString()
                                + "' contains HTML syntax errors:\n"
                                + messages);
                    }

                    // We previously checked that there is exactly one element
                    for (Element element : elements) {
                        element.html(htmlText.toString());
                    }
                }
            }

            // Correct <meta> tags, which parser handles improperly
            // HTML5 requires an unclosed meta tag, e.g. <meta >
            // XHTML requires a closed meta tag, e.g. <meta />
            // For HTML5 documents, the parser correctly throws
            // an exception for closed <meta /> tags in input document;
            // however, the parser incorrectly adds a closing /> to the
            // result document

            // TODO:  Check on other self-closing tags like <br>
            content = new StringBuffer(_document.html());

            if (content != null && content.length() > 0) {
                int startTagIndex = content.indexOf("<meta", 0);
                int closeTagIndex = 0;

                while (startTagIndex != -1) {
                    closeTagIndex = content.indexOf("/>", startTagIndex);
                    content.deleteCharAt(closeTagIndex);
                    startTagIndex = content.indexOf("<meta", startTagIndex + 1);
                }
            }

            // TODO:  Check that result file does not contain illegal
            // duplicate items (like ids, body tags, ...)  Parser does not
            // seem to flag all of these situations?

            output.broadcast(new StringToken(content.toString()));

            if (((BooleanToken) saveToFile.getToken()).booleanValue()) {
                outputFile.openForWriting().write(content.toString());
                outputFile.close();
            }
            template.close();

        } catch (IOException e) {
            throw new IllegalActionException(this, e,
                    "Cannot read or write a file");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The parsed HTML document
     */
    private Document _document;

    /** The end of line character(s).  The default value is the value
     *  of the line.separator property
     */
    private Parameter newline;
}
