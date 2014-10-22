/* A helper class to handle actions in GUI properties.

 Copyright (c) 2008-2014 The Regents of the University of California.
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

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.PtolemyFrame;
import ptolemy.actor.gui.Tableau;
import ptolemy.kernel.util.Attribute;
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
//// GUIAction

/**
 A helper class to handle actions in GUI properties.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class GUIAction extends Attribute {

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
    public GUIAction(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
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

    /** Get the frame in which this item is selected.
     *
     *  @return The frame.
     */
    public JFrame getFrame() {
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
    public NamedObj getModel() {
        JFrame frame = getFrame();
        if (!(frame instanceof PtolemyFrame)) {
            throw new InternalErrorException("The current frame has "
                    + "no model.");
        }
        return ((PtolemyFrame) frame).getModel();
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
     *  Depending on whether the parse parameter is true or false,
     *  the moml text may be parsed first or not. If it is parsed, the
     *  returned NamedObj is used to generate a new moml string to be
     *  applied to the model in the current tableau (the nearest tableau
     *  that contains this GUI property). If it is not parsed, then the moml
     *  text is directly applied to the model.
     *
     *  @param parse Whether the configure text should be parsed before applying
     *   to the current model.
     *  @exception Exception If error occurs in performing the action.
     */
    public void perform(boolean parse) throws Exception {
        if (_momlText != null) {
            NamedObj model = getModel();

            String moml;
            if (parse) {
                _parseSource();
                moml = getMoml(model, _parsedObject);
            } else {
                if (_momlSource != null) {
                    URL url = _parser.fileNameToURL(_momlSource, null);
                    InputStreamReader reader = null;
                    try {
                        reader = new InputStreamReader(url.openStream());

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
                                        "Failed to close \"" + url + "\".");
                            }
                        }
                    }
                }
                moml = _momlText;
            }
            MoMLChangeRequest request = new MoMLChangeRequest(this, model, moml) {
                @Override
                protected void _postParse(MoMLParser parser) {
                    Iterator topObjects = parser.topObjectsCreated().iterator();
                    while (topObjects.hasNext()) {
                        NamedObj topObject = (NamedObj) topObjects.next();
                        if (topObject.attributeList(Location.class).isEmpty()) {
                            try {
                                Location location = new Location(topObject,
                                        topObject.uniqueName("_location"));
                                Point2D center = ((BasicGraphFrame) getFrame())
                                        .getCenter();
                                location.setLocation(new double[] {
                                        center.getX(), center.getY() });
                            } catch (KernelException e) {
                                throw new InternalErrorException(e);
                            }
                        }
                    }
                    parser.clearTopObjectsList();
                }

                @Override
                protected void _preParse(MoMLParser parser) {
                    super._preParse(parser);
                    parser.clearTopObjectsList();
                }
            };
            request.setUndoable(true);
            model.requestChange(request);
        }
    }

    /** Parse the configuration source if it has not been parsed, and store
     *  the result in protected field {@link #_parsedObject}.
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
     *  @param container The container.
     *  @param object The object whose moml is to be got.
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
            String lsidString = ((StringAttribute) object
                    .getAttribute("entityId")).getExpression();
            if (lsidString == null || lsidString.equals("")) {
                lsidFlag = false;
            }
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
            String alternateGetMomlClassName = alternateGetMomlActionAttribute
                    .getExpression();
            Class getMomlClass = Class.forName(alternateGetMomlClassName);
            Object getMomlAction = getMomlClass.newInstance();
            try {
                Method getMomlMethod = getMomlClass.getMethod("getMoml",
                        new Class[] { NamedObj.class, String.class });
                return (String) getMomlMethod.invoke(getMomlAction,
                        new Object[] { object, name });
            } catch (NoSuchMethodException e) {
            }
        }

        return "<group name=\"auto\">\n" + object.exportMoML(name)
                + "</group>\n";
    }
}
