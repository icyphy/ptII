/* An attribute that creates an editor pane to edit the icon description.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.toolbox;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import ptolemy.actor.gui.EditorFactory;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.kernel.util.ConfigurableAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.SingletonConfigurableAttribute;
import ptolemy.moml.MoMLChangeRequest;
import diva.canvas.toolbox.SVGParser;
import diva.util.xml.XmlDocument;
import diva.util.xml.XmlElement;
import diva.util.xml.XmlReader;

//////////////////////////////////////////////////////////////////////////
//// AnnotationEditorFactory
/**
If this class is contained by a visible attribute (one that has
an attribute called "_iconDescription"), then double clicking on that
attribute will invoke an editor for a textual annotation.
This class is contained by visible attribute in the Vergil
utilities library, which provides a facility for adding visual
annotations to diagrams.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
@deprecated Use ptolemy.vergil.kernel.attributes.TextAttribute.
*/

public class AnnotationEditorFactory extends EditorFactory {

    /** Construct a factory with the specified container and name.
     *  @param container The container.
     *  @param name The name of the factory.
     *  @exception IllegalActionException If the factory is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public AnnotationEditorFactory(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _container = container;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create an editor for configuring the specified object.
     */
    public void createEditor(NamedObj object, Frame parent) {
        ComponentDialog dialog = new ComponentDialog(
                parent, "Edit Annotation", createEditorPane());

        String button = dialog.buttonPressed();
        if (!button.equals("OK")) return;

        String newText = _textArea.getText();
        if (newText == null || newText.trim().equals("")) {
            // NOTE: Should we delete the attribute... no visible text.
            newText = "Double click to edit text.";
        }
        String moml = "<configure><svg><text x=\"20\" y=\"20\" "
            + "style=\"font-size:"
            + _fontProperties.getStringValue("fontSize")
            + "; font-family:"
            + _fontProperties.getStringValue("fontFamily")
            + "; fill:"
            + _fontProperties.getStringValue("fontColor")
            + "\">"
            + newText
            + "</text></svg></configure>";
        _iconDescription.requestChange(new MoMLChangeRequest(
                this, _iconDescription, moml));
    }

    /** Return a new widget for configuring the container.
     *  @return A JPanel that is a text editor for editing the annotation text.
     */
    public Component createEditorPane() {
        _textArea = new JTextArea();
        _iconDescription =
            (ConfigurableAttribute)_container
            .getAttribute("_iconDescription");
        if (_iconDescription == null) {
            try {
                _iconDescription = new SingletonConfigurableAttribute(
                        _container, "_iconDescription");
            } catch (KernelException ex) {
                // Cannot occur.
                throw new InternalErrorException(ex.toString());
            }
        }
        // Parse the SVG to find the text.
        String text = _iconDescription.getExpression();

        // Default font characteristics.
        _fontSize = "14";
        _fontFamily = "SansSerif";
        _fontColor = "blue";

        try {
            Reader in = new StringReader(text);
            // NOTE: Do we need a base here?
            XmlDocument document = new XmlDocument((URL)null);
            XmlReader reader = new XmlReader();
            reader.parse(document, in);
            XmlElement root = document.getRoot();
            String name = root.getType();
            if (name.equals("svg")) {
                Iterator children = root.elements();
                while (children.hasNext()) {
                    XmlElement child = (XmlElement)children.next();
                    name = child.getType();
                    if (name.equals("text")) {
                        text = child.getPCData();
                        String style = (String)
                            child.getAttributeMap().get("style");
                        if (style != null) {
                            StringTokenizer tokenizer = new StringTokenizer(
                                    style, ";");
                            while (tokenizer.hasMoreTokens()) {
                                String token = tokenizer.nextToken();
                                int colon = token.indexOf(":");
                                if (colon > 0) {
                                    String property =
                                        token.substring(0, colon).trim();
                                    if (property.equals("fill")) {
                                        _fontColor = token.substring(colon+1);
                                    } else if (property.equals("font-size")) {
                                        _fontSize = token.substring(colon+1);
                                    } else if (property.equals("font-family")) {
                                        _fontFamily = token.substring(colon+1);
                                    }
                                }
                            }
                        }
                        // We are done once we find a text element.
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            // If we fail, then we use the text as is.
        }

        _textArea.setText(text);

        AnnotationTextEditor editor = new AnnotationTextEditor(_textArea);
        return editor;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** The container. */
    private NamedObj _container;

    /** Query box for font properties. */
    Query _fontProperties;

    /** Font characteristic. */
    private String _fontSize = "14";
    private String _fontFamily = "sanserif";
    private String _fontColor = "blue";

    /** The attribute containing the icon description. */
    private ConfigurableAttribute _iconDescription;

    /** The text area of the editor. */
    private JTextArea _textArea;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A text widget for editing textual annotations (which are
     *  visible attributes).
     */
    public class AnnotationTextEditor extends JPanel {

        /** Create an annotation text editor.
         */
        public AnnotationTextEditor(JTextArea textArea) {
            super();
            JScrollPane pane = new JScrollPane(textArea);
            // NOTE: Should the size be hardwired here?
            pane.setPreferredSize(new Dimension(600, 300));
            add(pane);

            // Add a query with font properties.
            _fontProperties = new Query();
            String[] sizes = {"9", "10", "11", "12", "14", "18", "24", "32"};
            _fontProperties.addChoice("fontSize", "font size", sizes,
                    _fontSize, true);

            // FIXME: Need a way to specify Italic, Bold (style).
            // Check SVG standard and SVGParser.

            // Get font family names from the Font class in Java.
            // This includes logical font names, per Font class in Java:
            // Dialog, DialogInput, Monospaced, Serif, SansSerif, or Symbol.
            String[] families = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();
            _fontProperties.addChoice("fontFamily", "font family", families,
                    _fontFamily, false);

            // FIXME: Add a facility to invoke a color chooser using
            // JColorChooser.
            // public static Color showDialog(Component component (parent),
            //                  String title,
            //                  Color initialColor)
            String[] colors = SVGParser.colorNames();
            // The last argument makes this editable.
            // Colors can be given in hex #rrggbb.
            _fontProperties.addChoice("fontColor", "font color", colors,
                    _fontColor, true);

            add(_fontProperties);
        }
    }
}
