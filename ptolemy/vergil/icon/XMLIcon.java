/* An icon stored in XML.

 Copyright (c) 1999-2014 The Regents of the University of California.
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
package ptolemy.vergil.icon;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Iterator;

import ptolemy.actor.gui.Configuration;
import ptolemy.kernel.util.ConfigurableAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.ValueListener;
import ptolemy.kernel.util.Workspace;
import diva.canvas.Figure;
import diva.canvas.toolbox.PaintedFigure;
import diva.canvas.toolbox.SVGParser;
import diva.gui.toolbox.FigureIcon;
import diva.util.java2d.PaintedList;
import diva.util.xml.XmlDocument;
import diva.util.xml.XmlElement;
import diva.util.xml.XmlReader;

///////////////////////////////////////////////////////////////////
//// XMLIcon

/**
 An icon is a visual representation of an entity. Three such visual
 representations are supported here.  A background figure is returned
 by the createBackgroundFigure() method.  This figure is specified by
 an attribute named "_iconDescription" of the container, if there is one.
 If there is no such attribute, then a default icon is used.
 The createFigure() method returns this same background figure, but
 decorated with a label giving the name of the container, unless the
 container contains a parameter named "_hideName" with value true.
 The createIcon() method returns a Swing icon given by an attribute named
 "_smallIconDescription", if there is one.  If there is no such
 attribute, then the icon is simply a small representation of the
 background figure.
 <p>
 The XML schema used in the "_iconDescription" and "_smallIconDescription"
 attributes is SVG (scalable vector graphics), although currently Diva
 only supports a small subset of SVG.

 @author Steve Neuendorffer, John Reekie, Contributors: Edward A. Lee, Chad Berkley (Kepler)
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Yellow (neuendor)
 @Pt.AcceptedRating Red (johnr)
 */
public class XMLIcon extends DynamicEditorIcon implements ValueListener {
    /** Construct an icon in the specified workspace and name.
     *  This constructor is typically used in conjunction with
     *  setContainerToBe() and createFigure() to create an icon
     *  and generate a figure without having to have write access
     *  to the workspace.
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the directory of the workspace.
     *  @see #setContainerToBe(NamedObj)
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the attribute.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the specified name contains
     *   a period.
     */
    public XMLIcon(Workspace workspace, String name)
            throws IllegalActionException {
        super(workspace, name);

        try {
            setName(name);
        } catch (NameDuplicationException ex) {
            throw new InternalErrorException(ex);
        }
    }

    /** Create a new icon with the given name in the given container.
     *  By default, the icon contains no graphic objects.
     *  @param container The container for this attribute.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If thrown by the parent
     *  class or while setting an attribute.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public XMLIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _paintedList = null;
        _description = null;
    }

    /**
     * Instantiate an XMLIcon in a NamedObj.
     *
     * <p>This method looks for the _alternateXMLIcon attribute in the
     * configuration. If it is found, it returns an XMLIcon of the
     * class found there, if not, it returns an instance of this class.
     *
     *  @param container The container for this attribute.
     *  @param name The name of this attribute.
     *  @return an instance of the XMLIcon class.
     *  @exception NameDuplicationException  If an object with
     *  that name already exists in the container.
     *  @exception IllegalActionException If the specified name contains
     *   a period.
     */
    public static XMLIcon getXMLIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        try {
            Class XMLIconClass = _getAlternateXMLIcon();
            if (XMLIconClass == null) {
                return new XMLIcon(container, name);
            }

            Class[] argsClass = new Class[] { NamedObj.class, String.class };
            Constructor alternateXMLIconConstructor = XMLIconClass
                    .getConstructor(argsClass);
            XMLIcon xmlIcon = (XMLIcon) alternateXMLIconConstructor
                    .newInstance(new Object[] { container, name });
            return xmlIcon;
        } catch (Exception ex) {
            System.out
                    .println("Warning: could not instantiate alternate XMLIcon class. "
                            + "Using default XMLIcon.  : " + ex.getMessage());
            ex.printStackTrace();
            return new XMLIcon(container, name);
        }
    }

    /**
     * Instantiate an XMLIcon in a Workspace.
     *
     * <p>This method looks for the _alternateXMLIcon attribute in the
     * configuration. If it is found, it returns an XMLIcon of the
     * class found there, if not, it returns an instance of this class.
     *
     *  @param workspace The workspace that will list the attribute.
     *  @param name The name of this attribute.
     *  @return an instance of the XMLIcon class.
     *  @exception NameDuplicationException  If an object with
     *  that name already exists in the container.
     *  @exception IllegalActionException If the specified name contains
     *   a period.
     */
    public static XMLIcon getXMLIcon(Workspace workspace, String name)
            throws NameDuplicationException, IllegalActionException {
        try {
            Class XMLIconClass = _getAlternateXMLIcon();
            if (XMLIconClass == null) {
                return new XMLIcon(workspace, name);
            }
            // Get the new object and return it
            Class[] argsClass = new Class[] {
                    ptolemy.kernel.util.Workspace.class, String.class };
            Constructor alternateXMLIconConstructor = XMLIconClass
                    .getConstructor(argsClass);
            XMLIcon xmlIcon = (XMLIcon) alternateXMLIconConstructor
                    .newInstance(new Object[] { workspace, name });
            return xmlIcon;
        } catch (Exception ex) {
            System.out
                    .println("Warning: could not instantiate alternate XMLIcon class. "
                            + "Using default XMLIcon.  : " + ex.getMessage());
            ex.printStackTrace();
            return new XMLIcon(workspace, name);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an object with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        XMLIcon newObject = (XMLIcon) super.clone(workspace);
        newObject._paintedList = null;
        newObject._description = null;
        newObject._smallIconDescription = null;
        return newObject;
    }

    /** Create a background figure based on this icon.  The background figure
     *  will be painted with each graphic element that this icon contains.
     *  @return A figure for this icon.
     */
    @Override
    public Figure createBackgroundFigure() {
        // Get the description.
        NamedObj container = (NamedObj) getContainerOrContainerToBe();
        ConfigurableAttribute description = (ConfigurableAttribute) container
                .getAttribute("_iconDescription");

        // If the description has changed...
        if (_description != description) {
            if (_description != null) {
                // Remove this as a listener if there
                // was a previous description.
                _description.removeValueListener(this);
            }

            // update the description.
            _description = description;

            if (_description != null) {
                // Listen for changes in value to the icon description.
                _description.addValueListener(this);
            }

            // clear the caches
            _recreateFigure();
        }

        // Update the painted list.
        _updatePaintedList();

        // If the paintedList is still null, then return the default figure.
        if (_paintedList == null) {
            return _createDefaultBackgroundFigure();
        }

        return new PaintedFigure(_paintedList);
    }

    /** Create a new Swing icon.  This class looks for an attribute
     *  called "_smallIconDescription", and if it exists, uses it to
     *  create the icon.  If it does not exist, then it simply creates
     *  a small version of the background figure returned by
     *  createBackgroundFigure().
     *  @return A new Swing Icon.
     */
    @Override
    public javax.swing.Icon createIcon() {
        // In this class, we cache the rendered icon, since creating icons from
        // figures is expensive.
        if (_iconCache != null) {
            return _iconCache;
        }

        // No cached object, so rerender the icon.
        // Get the description.
        NamedObj container = (NamedObj) getContainerOrContainerToBe();
        ConfigurableAttribute description = (ConfigurableAttribute) container
                .getAttribute("_smallIconDescription");

        // If there is no separate small icon description, return
        // a scaled version of the background figure, as done by the base
        // class.
        if (description == null) {
            return super.createIcon();
        }

        // If the description has changed...
        if (_smallIconDescription != description) {
            if (_smallIconDescription != null) {
                // Remove this as a listener if there
                // was a previous description.
                _smallIconDescription.removeValueListener(this);
            }

            _smallIconDescription = description;

            // Listen for changes in value to the icon description.
            _smallIconDescription.addValueListener(this);
        }

        // clear the caches
        _recreateFigure();

        // Update the painted list, if necessary
        Figure figure;

        try {
            String text = _smallIconDescription.value();
            Reader in = new StringReader(text);

            // NOTE: Do we need a base here?
            XmlDocument document = new XmlDocument((URL) null);
            XmlReader reader = new XmlReader();
            reader.parse(document, in);

            XmlElement root = document.getRoot();
            PaintedList paintedList = SVGParser.createPaintedList(root);
            figure = new PaintedFigure(paintedList);
        } catch (Exception ex) {
            return super.createIcon();
        }

        // NOTE: The size is hardwired here.  Should it be?
        // The second to last argument specifies the border.
        // The last says to turn anti-aliasing on.
        _iconCache = new FigureIcon(figure, 20, 15, 0, true);
        return _iconCache;
    }

    /** Return the painted list contained by this icon.
     *  This is used by the icon editor.
     *  @return The painted list contained by this icon.
     */
    public PaintedList paintedList() {
        if (_paintedList == null) {
            _updatePaintedList();
        }

        return _paintedList;
    }

    /** Return a string representing this Icon.
     */
    @Override
    public String toString() {
        String str = super.toString() + "(";

        // FIXME: Something is missing here.
        return str + ")";
    }

    /** React to the fact that the value of an attribute named
     *  "_iconDescription" contained by the same container has changed
     *  value by redrawing the figure.
     *  @param settable The object that has changed value.
     */
    @Override
    public void valueChanged(Settable settable) {
        String name = ((Nameable) settable).getName();

        if (name.equals("_iconDescription")
                || name.equals("_smallIconDescription")) {
            _recreateFigure();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a description of the object.  Lines are indented according to
     *  to the level argument using the protected method _getIndentPrefix().
     *  Zero, one or two brackets can be specified to surround the returned
     *  description.  If one is specified it is the the leading bracket.
     *  This is used by derived classes that will append to the description.
     *  Those derived classes are responsible for the closing bracket.
     *  An argument other than 0, 1, or 2 is taken to be equivalent to 0.
     *  This method is read-synchronized on the workspace.
     *  @param detail The level of detail.
     *  @param indent The amount of indenting.
     *  @param bracket The number of surrounding brackets (0, 1, or 2).
     *  @return A description of the object.
     * @exception IllegalActionException
     */
    @Override
    protected String _description(int detail, int indent, int bracket)
            throws IllegalActionException {
        String result = "";

        if (bracket == 0) {
            result += super._description(detail, indent, 0);
        } else {
            result += super._description(detail, indent, 1);
        }

        result += " graphics {\n";
        result += "FIXME";
        result += _getIndentPrefix(indent) + "}";

        if (bracket == 2) {
            result += "}";
        }

        return result;
    }

    /** Recreate the figure.  Call to cause createIcon() to call
     *  createBackgroundFigure() to obtain a new figure.
     */
    @Override
    protected void _recreateFigure() {
        super._recreateFigure();
        _paintedList = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Check to see if there is an _alternateXMLIcon attribute in the
     * configuration.  This attribute should name a class that
     * is an alternative to XMLIcon.
     * If the attribute is present, return the class, if not, return null.
     */
    private static Class _getAlternateXMLIcon() throws Exception {
        // Applets might not have a configuration, so we check to see
        // if there is a next configuration.
        Iterator configurations = Configuration.configurations().iterator();
        // FIXME: why does this always skip the first configuration?
        // Instead, we should check each configuration as we go.
        if (configurations.hasNext()) {
            Configuration config = (Configuration) configurations.next();
            String alternateXMLIconClassName = null;
            if (config != null) {
                // If _alternateXMLIcon is set in the config, use that
                // class as the XMLIcon instead of the default

                StringAttribute alternateXMLIconAttribute = (StringAttribute) config
                        .getAttribute("_alternateXMLIcon");
                if (alternateXMLIconAttribute != null) {
                    alternateXMLIconClassName = alternateXMLIconAttribute
                            .getExpression();
                }

                if (alternateXMLIconClassName == null) { //attribute was not found
                    return null;
                } else {
                    Class alternateXMLIconClass = Class
                            .forName(alternateXMLIconClassName);
                    return alternateXMLIconClass;
                }
            }
        }
        return null;
    }

    /** Update the painted list of the icon based on the SVG data
     *  in the associated "_iconDescription" parameter, if there is one.
     */
    private void _updatePaintedList() {
        // create a new list because the PaintedList we had before
        // was used to create some PaintedFigures already.
        // FIXME: test for 'svg' processing instruction
        if (_description == null) {
            _paintedList = null;
            return;
        }

        try {
            String text = _description.value();
            Reader in = new StringReader(text);

            // FIXME: Do we need a base here?
            XmlDocument document = new XmlDocument((URL) null);
            XmlReader reader = new XmlReader();
            reader.parse(document, in);

            XmlElement root = document.getRoot();

            _paintedList = SVGParser.createPaintedList(root);
        } catch (Exception ex) {
            // If we fail, then we'll just get a default figure.
            _paintedList = null;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    // The list of painted objects contained in this icon.
    private PaintedList _paintedList;

    // The description of this icon in XML.
    private ConfigurableAttribute _description;

    // The description of the small version of the icon in XML.
    private ConfigurableAttribute _smallIconDescription;
}
