/* A GUI property that encloses a JComboBox component.

 Copyright (c) 2008-2009 The Regents of the University of California.
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
package ptolemy.actor.gui.properties;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.PtolemyFrame;
import ptolemy.actor.gui.Tableau;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Configurable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Singleton;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.vergil.basic.BasicGraphFrame;

//////////////////////////////////////////////////////////////////////////
//// ComboBox

/**
 A GUI property that encloses a JComboBox component.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ComboBox extends GUIProperty implements ItemListener {

    /** Construct a GUI property with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ComboBox(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Construct a GUI property with the given name contained by the specified
     *  entity with the given Java Swing component. The container argument must
     *  not be null, or a NullPointerException will be thrown.  This attribute
     *  will use the workspace of the container for synchronization and version
     *  counts. If the name argument is null, then the name is set to the empty
     *  string. Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @param component The Java Swing component.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ComboBox(NamedObj container, String name, JComponent component)
            throws IllegalActionException, NameDuplicationException {
        super(container, name, component);
    }

    /** Construct a GUI property with the given name contained by the specified
     *  entity with the given Java Swing component and the given layout
     *  constraint. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute
     *  will use the workspace of the container for synchronization and version
     *  counts. If the name argument is null, then the name is set to the empty
     *  string. Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @param component The Java Swing component.
     *  @param constraint The layout constraint.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ComboBox(NamedObj container, String name, JComponent component,
            Object constraint) throws IllegalActionException,
            NameDuplicationException {
        super(container, name, component, constraint);
    }

    /** Construct a GUI property with the given name contained by the specified
     *  entity with the given layout
     *  constraint. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute
     *  will use the workspace of the container for synchronization and version
     *  counts. If the name argument is null, then the name is set to the empty
     *  string. Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @param constraint The layout constraint.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ComboBox(NamedObj container, String name, Object constraint)
            throws IllegalActionException, NameDuplicationException {
        super(container, name, constraint);
    }

    /** React to an action of changing the selected item in the combo box. The
     *  item must be an instance of {@link Item} and when it is selected, the
     *  {@link Item#select()} method is invoked. After that, if the item
     *  specifies the next item to be selected in its {@link Item#next}
     *  attribute, that next item is selected, which may cause this method to be
     *  invoked again.
     *
     *  @param event The item event representing which item is selected.
     */
    public void itemStateChanged(ItemEvent event) {
        if (event.getStateChange() != ItemEvent.SELECTED) {
            return;
        }

        Item item = (Item) event.getItem();
        item.select();

        if (item.next == null) {
            return;
        }

        try {
            String next = item.next.stringValue();
            if (!next.equals("")) {
                Item nextItem = (Item) getAttribute(next);
                ((JComboBox) getComponent()).setSelectedItem(nextItem);
            }
        } catch (IllegalActionException e) {
            throw new InternalErrorException(this, e,
                    "Unable to find the next item.");
        }
    }

    //////////////////////////////////////////////////////////////////////////
    //// Item

    /**
     The base class for an item that can be added to the combo box as a choice.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 7.1
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    public static class Item extends Attribute implements Configurable {

        /** Construct an item with the given name contained by the specified
         *  entity. The container argument must not be null, or a
         *  NullPointerException will be thrown.  This attribute will use the
         *  workspace of the container for synchronization and version counts.
         *  If the name argument is null, then the name is set to the empty
         *  string. Increment the version of the workspace.
         *  @param container The container.
         *  @param name The name of this attribute.
         *  @exception IllegalActionException If the attribute is not of an
         *   acceptable class for the container, or if the name contains a
         *   period.
         *  @exception NameDuplicationException If the name coincides with
         *   an attribute already in the container.
         */
        public Item(ComboBox container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);

            next = new StringParameter(this, "next");

            parse = new Parameter(this, "parse");
            parse.setTypeEquals(BaseType.BOOLEAN);
            parse.setToken(BooleanToken.FALSE);
        }

        /** Configure the object with data from the specified input source
         *  (a URL) and/or textual data.  The object should interpret the
         *  source first, if it is specified, followed by the literal text,
         *  if that is specified.  The new configuration should usually
         *  override any old configuration wherever possible, in order to
         *  ensure that the current state can be successfully retrieved.
         *  <p>
         *  This method is defined to throw a very general exception to allow
         *  classes that implement the interface to use whatever exceptions
         *  are appropriate.
         *  @param base The base relative to which references within the input
         *   are found, or null if this is not known, or there is none.
         *  @param source The input source, which specifies a URL, or null
         *   if none.
         *  @param text Configuration information given as text, or null if
         *   none.
         *  @exception Exception If something goes wrong.
         */
        public void configure(URL base, String source, String text)
                throws Exception {
            _momlSource = source;
            _momlText = text;
            _parsedObject = null;
        }

        /** Return the input source that was specified the last time the configure
         *  method was called.
         *  @return The string representation of the input URL, or null if the
         *  no source has been used to configure this object, or null if no
         *  external source need be used to configure this object.
         */
        public String getConfigureSource() {
            return _momlSource;
        }

        /** Return the text string that represents the current configuration of
         *  this object.  Note that any configuration that was previously
         *  specified using the source attribute need not be represented here
         *  as well.
         *  @return A configuration string, or null if no configuration
         *  has been used to configure this object, or null if no
         *  configuration string need be used to configure this object.
         */
        public String getConfigureText() {
            return _momlText;
        }

        /** React to this item being selected. In this base class, if a source
         *  file is specified in the configuration of this item, e.g.:
         *  <pre>
         *    &lt;configure source="some_file.xml"&gt;
         *    &lt;/configure&gt;
         *  </pre>
         *  then the source is read and its contents are used as the moml text.
         *  The moml text can also be given directly:
         *  <pre>
         *    &lt;configure&gt;
         *      &lt;entity name="C" class="ptolemy.actor.lib.Const"&gt;
         *      &lt;/entity&gt;
         *    &lt;/configure&gt;
         *  </pre>
         *
         *  Depending on whether the {@link #parse} parameter is true or false,
         *  the moml text may be parsed first or not. If it is parsed, the
         *  returned NamedObj is used to generate a new moml string to be
         *  applied to the model in the current tableau (the nearest tableau
         *  that contains this GUI property). If it is not parsed, then the moml
         *  text is directly applied to the model.
         */
        public void select() {
            if (_momlText != null) {
                NamedObj model = _getModel();

                try {
                    boolean parse = ((BooleanToken) this.parse.getToken())
                            .booleanValue();
                    String moml;
                    if (parse) {
                        _parseSource();
                        moml = getMoml(model, _parsedObject);
                    } else {
                        if (_momlSource != null) {
                            URL url = _parser.fileNameToURL(_momlSource, null);
                            InputStreamReader reader = null;
                            try {
                                reader = new InputStreamReader(
                                        url.openStream());

                                int bufferSize = 1024;
                                char[] buffer = new char[bufferSize];
                                int readSize = 0;
                                StringBuffer string = new StringBuffer();
                                while (readSize >= 0) {
                                    readSize = reader.read(buffer);
                                    if (readSize >= 0) {
                                        string.append(buffer, 0, readSize);
                                    }
                                }
                                _momlText = string.toString();
                                _momlSource = null;
                            } finally {
                                if (reader != null) {
                                    try {
                                    reader.close();
                                    } catch (IOException ex) {
                                    throw new InternalErrorException(
                                            "Failed to close \"" + url +
                                            "\".");
                                    }
                                }
                            }
                        }
                        moml = _momlText;
                    }
                    MoMLChangeRequest request = new MoMLChangeRequest(this,
                            model, moml) {
                        protected void _postParse(MoMLParser parser) {
                            Iterator topObjects =
                                parser.topObjectsCreated().iterator();
                            while (topObjects.hasNext()) {
                                NamedObj topObject =
                                    (NamedObj) topObjects.next();
                                if (topObject.attributeList(Location.class)
                                        .isEmpty()) {
                                    try {
                                        Location location = new Location(
                                                topObject, topObject.uniqueName(
                                                        "_location"));
                                        Point2D center = ((BasicGraphFrame)
                                                _getFrame()).getCenter();
                                        location.setLocation(new double[]{
                                                center.getX(), center.getY()});
                                    } catch (KernelException e) {
                                        throw new InternalErrorException(e);
                                    }
                                }
                            }
                            parser.clearTopObjectsList();
                        }

                        protected void _preParse(MoMLParser parser) {
                            super._preParse(parser);
                            parser.clearTopObjectsList();
                        }
                    };
                    request.setUndoable(true);
                    model.requestChange(request);
                } catch (Exception e) {
                    throw new InternalErrorException(e);
                }
            }
        }

        /** Specify the container NamedObj, adding this attribute to the
         *  list of attributes in the container.  If the container already
         *  contains an attribute with the same name, then throw an exception
         *  and do not make any changes.  Similarly, if the container is
         *  not in the same workspace as this attribute, throw an exception.
         *  If this attribute is already contained by the NamedObj, do nothing.
         *  If the attribute already has a container, remove
         *  this attribute from its attribute list first.  Otherwise, remove
         *  it from the directory of the workspace, if it is there.
         *  If the argument is null, then remove it from its container.
         *  It is not added to the workspace directory, so this could result in
         *  this object being garbage collected.
         *  Note that since an Attribute is a NamedObj, it can itself have
         *  attributes.  However, recursive containment is not allowed, where
         *  an attribute is an attribute of itself, or indirectly of any attribute
         *  it contains.  This method is write-synchronized on the
         *  workspace and increments its version number.
         *  <p>
         *  Subclasses may constrain the type of container by overriding
         *  {@link #setContainer(NamedObj)}.
         *  @param container The container to attach this attribute to..
         *  @exception IllegalActionException If this attribute is not of the
         *   expected class for the container, or it has no name,
         *   or the attribute and container are not in the same workspace, or
         *   the proposed container would result in recursive containment.
         *  @exception NameDuplicationException If the container already has
         *   an attribute with the name of this attribute.
         *  @see #getContainer()
         */
        public void setContainer(NamedObj container)
                throws IllegalActionException, NameDuplicationException {
            ComboBox oldContainer = (ComboBox) getContainer();
            if (oldContainer != null) {
                ((JComboBox) oldContainer.getComponent()).removeItem(this);
            }
            super.setContainer(container);
            ((JComboBox) ((ComboBox) container).getComponent()).addItem(this);
        }

        /** Return the display name of this item, or its name if the display
         *  name is not specified. The returned string is shown in the combo box
         *  for this item.
         *
         *  @return The display name of this item.
         */
        public String toString() {
            return getDisplayName();
        }

        /** The name of the next item in the same combo box to be selected when
         *  this GUI property is selected, or an empty string if there is no
         *  next item.
         */
        public StringParameter next;

        /** A Boolean parameter that determines whether the moml text should be
         *  parsed before applying to the current model in the {@link #select()}
         *  method.
         */
        public Parameter parse;

        /** Get the frame in which this item is selected.
         *
         *  @return The frame.
         */
        protected JFrame _getFrame() {
            NamedObj container = getContainer();
            while (container != null && !(container instanceof Tableau)) {
                container = container.getContainer();
            }
            if (container == null) {
                throw new InternalErrorException("Unable to find tableau.");
            }

            return ((Tableau) container).getFrame();
        }

        /** Get the model contained in the current frame.
         *
         *  @return The model.
         */
        protected NamedObj _getModel() {
            JFrame frame = _getFrame();
            if (!(frame instanceof PtolemyFrame)) {
                throw new InternalErrorException("The current frame has " +
                        "no model.");
            }
            return ((PtolemyFrame) frame).getModel();
        }

        /** Parse the configuration source if it has not been parsed, and store
         *  the result in protected field {@link _parsedObject}.
         *
         *  @exception Exception If it occurs in the parsing.
         */
        protected void _parseSource() throws Exception {
            if (_parsedObject == null) {
                if (_momlSource != null) {
                    URL url = _parser.fileNameToURL(_momlSource, null);
                    _parsedObject = _parser.parse(url, url);
                    _momlSource = null;
                } else {
                    _parsedObject = _parser.parse(_momlText);
                }
                _parser.reset();
            }
        }

        /** The input source that was specified the last time the configure
         *  method was called.
         */
        protected String _momlSource;

        /** The text string that represents the current configuration of this
         *  object.
         */
        protected String _momlText;

        /** The object obtained by parsing the moml text, or null.
         */
        protected NamedObj _parsedObject;

        /** The parser used to parse the moml text.
         */
        protected MoMLParser _parser = new MoMLParser();

        /** Get the moml for the object to be added to the container.
         *  @return The moml string.
         *  @exception Exception If error occurs.
         */
        private static String getMoml(NamedObj container, NamedObj object)
                throws Exception {
            String name;

            if (object instanceof Singleton) {
                name = object.getName();
            } else {
                name = container.uniqueName(object.getName());
            }

            boolean lsidFlag = true;
            try {
                String lsidString = ((StringAttribute) (object.getAttribute(
                        "entityId"))).getExpression();
                if ((lsidString == null) || (lsidString.equals("")))
                    lsidFlag = false;
            } catch (Exception eee) {
                lsidFlag = false;
            }

            StringAttribute alternateGetMomlActionAttribute = null;
            alternateGetMomlActionAttribute = (StringAttribute) object
                    .getAttribute("_alternateGetMomlAction");
            if (alternateGetMomlActionAttribute == null && lsidFlag) {
                Configuration config = null;
                List configsList = Configuration.configurations();
                for (Iterator it = configsList.iterator(); it.hasNext();) {
                    config = (Configuration) it.next();
                    if (config != null) {
                        break;
                    }
                }
                if (config == null) {
                    throw new KernelRuntimeException(object, "Could not find "
                            + "configuration, list of configurations was "
                            + configsList.size() + " elements, all were null.");
                }
                alternateGetMomlActionAttribute = (StringAttribute) config
                        .getAttribute("_alternateGetMomlAction");
            }

            if (alternateGetMomlActionAttribute != null) {
                String alternateGetMomlClassName =
                    alternateGetMomlActionAttribute.getExpression();
                Class getMomlClass = Class.forName(
                        alternateGetMomlClassName);
                Object getMomlAction = getMomlClass.newInstance();
                try {
                    Method getMomlMethod = getMomlClass.getMethod(
                            "getMoml", new Class[] {NamedObj.class,
                                    String.class});
                    return (String) getMomlMethod.invoke(getMomlAction,
                            new Object[] { object, name});
                } catch (NoSuchMethodException e) {
                }
            }

            return "<group name=\"auto\">\n" + object.exportMoML(name) +
                    "</group>\n";
        }
    }

    /** Create a new JComboBox component.
    *
    *  @return A Swing component that can be enclosed in this GUI property.
    *  @exception IllegalActionException Not thrown in this base class.
    */
    protected JComponent _createComponent() throws IllegalActionException {
        JComboBox comboBox = new JComboBox();
        comboBox.addItemListener(this);
        return comboBox;
    }
}
