/* A scope extending attribute that reads multiple values from a file.

 Copyright (c) 2006-2014 The Regents of the University of California.
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

 @ProposedRating Red (liuxj)
 @AcceptedRating Red (liuxj)

 */
package ptolemy.actor.parameters;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Executable;
import ptolemy.actor.Initializable;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ScopeExtendingAttribute;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
//// ParameterSet

/**
 An attribute that reads multiple values from a file and sets
 corresponding parameters in the container.
 The values are in the form:
 <pre>
 <i>attributeName</i> = <i>value</i>
 </pre>
 where <code><i>variableName</i></code> is the name of the attribute
 in a format suitable for {@link ptolemy.kernel.util.NamedObj#setName(String)}
 (i.e., does not contain periods) and  <code><i>value</i></code> is
 the expression in the Ptolemy expression language.
 Comments are lines that begin with the <code>#</code> character.
 Each line in the file is interpreted as a separate assignment.

 <p>The attributes that are created will have the same
 visibility as parameters of the container of the attribute.
 They are shadowed, however, by parameters of the container.
 That is, if the container has a parameter with the same name
 as one in the parameter set, the one in the container provides
 the value to any observer.

 <p>If the file is modified during execution of a model, by default
 this will not be noticed until the next run. If you set the
 <i>checkForFileUpdates</i> parameter to <i>true</i>, then
 on each prefiring of the enclosing opaque composite actor,
 this parameter will check for updates of the file. Otherwise,
 it will only check between runs of the model or when the file
 name or URL gets changed.

 <p>Note that the order the parameters are created is arbitrary,
 this is because we read the file in using java.util.Properties.load(),
 which uses a HashMap to store the properties.  We use a Properties.load()
 because it provides a nice parser for the files and can read and write
 values in both text and XML.

 @author Christopher Brooks, contributor: Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
 @see ptolemy.data.expr.Variable
 */
public class ParameterSet extends ScopeExtendingAttribute implements Executable {
    /** Construct an attribute with the given name contained by the specified
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
    public ParameterSet(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        fileOrURL = new FileParameter(this, "fileOrURL");
        fileOrURL.setExpression("");

        checkForFileUpdates = new Parameter(this, "checkForFileUpdates");
        checkForFileUpdates.setExpression("false");
        checkForFileUpdates.setTypeEquals(BaseType.BOOLEAN);

        StringParameter initialDefaultContents = new StringParameter(this,
                "initialDefaultContents");
        initialDefaultContents
        .setExpression("# This file defines parameters in the current container.\n# Each non-comment line in the file is interpreted as a separate assignment.\n# The lines are of the form:\n# attributeName = value\n# where variableName is the name of the attribute\n# in a format suitable for ptolemy.kernel.util.NamedObj.setName()\n# (i.e., does not contain periods) and value is\n# the expression in the Ptolemy expression language.\n# Comments are lines that begin with the # character.\n# FIXME: After saving, you need to update the fileOrURLParameter by hand.\n# Sample line (remove the leading #):\n# foo = \"bar\"\n");
        initialDefaultContents.setPersistent(false);
        initialDefaultContents.setVisibility(Settable.EXPERT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** If this parameter is set to true, then the specified file or
     *  URL will be checked for updates on every prefiring of the
     *  enclosing opaque composite actor. Otherwise, it will check
     *  for updates only between runs. This is a boolean that
     *  defaults to false.
     */
    public Parameter checkForFileUpdates;

    /** A parameter naming the file or URL to be read that contains
     *  attribute names and values.  The file should be in a format
     *  suitable for java.util.Properties.load(), see the class
     *  comment of this class for details.
     *  This initial default value is the empty string "",
     *  which means that no file will be read and no parameter
     *  values will be defined.
     */
    public FileParameter fileOrURL;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add the specified object to the list of objects whose
     *  preinitialize(), initialize(), and wrapup()
     *  methods should be invoked upon invocation of the corresponding
     *  methods of this object.
     *  @param initializable The object whose methods should be invoked.
     *  @see #removeInitializable(Initializable)
     *  @see ptolemy.actor.CompositeActor#addPiggyback(Executable)
     */
    @Override
    public void addInitializable(Initializable initializable) {
        if (_initializables == null) {
            _initializables = new LinkedList<Initializable>();
        }
        _initializables.add(initializable);
    }

    /** If the parameter is <i>fileOrURL</i>, and the specified file
     *  name is not null, then open and read the file.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the superclass throws it, or
     *   if the file cannot be read, or if the file parameters cannot
     *   be evaluated.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == fileOrURL) {
            // Do not read the file if the name is the same as
            // what was previously read. EAL 9/8/06
            if (!fileOrURL.getExpression().equals(_fileName)) {
                try {
                    read();
                    validate();
                } catch (Throwable throwable) {
                    throw new IllegalActionException(this, throwable,
                            "Failed to read file: " + fileOrURL.getExpression());
                }
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Expand the scope of the container by creating any required attributes.
     *  This method reads the specified file if it has not already been read
     *  or if has changed since it was last read.
     *  @exception IllegalActionException If any required attribute cannot be
     *   created.
     */
    @Override
    public void expand() throws IllegalActionException {
        _reReadIfNeeded();
        // Do not call validate.
    }

    /** Do nothing.
     */
    @Override
    public void fire() throws IllegalActionException {
    }

    /** Do nothing except invoke the initialize methods
     *  of objects that have been added using addInitializable().
     *  @exception IllegalActionException If one of the added objects
     *   throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        // Invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.initialize();
            }
        }
    }

    /** Return true.
     *  @return True.
     */
    @Override
    public boolean isFireFunctional() {
        return true;
    }

    /** Return false.
     *  @return False.
     */
    @Override
    public boolean isStrict() {
        return false;
    }

    /** Check to see whether the specified file has changed, and if so,
     *  re-read it.
     *  @param count The number of iterations to perform, ignored by this
     *  method.
     *  @exception IllegalActionException If re-reading the file fails.
     *  @return Executable.COMPLETED.
     */
    @Override
    public int iterate(int count) throws IllegalActionException {
        if (((BooleanToken) checkForFileUpdates.getToken()).booleanValue()) {
            if (_reReadIfNeeded()) {
                validate();
            }
        }
        return Executable.COMPLETED;
    }

    /** Do nothing.
     *  @return True.
     */
    @Override
    public boolean postfire() {
        return true;
    }

    /** Check to see whether the specified file has changed, and if so,
     *  re-read it.
     *  @return True.
     *  @exception IllegalActionException If re-reading the file fails.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (((BooleanToken) checkForFileUpdates.getToken()).booleanValue()) {
            if (_reReadIfNeeded()) {
                validate();
            }
        }
        return true;
    }

    /** Check to see whether the specified file has changed, and if so,
     *  re-read it, and invoke the preinitialize() methods
     *  of objects that have been added using addInitializable().
     *  @exception IllegalActionException If one of the added objects
     *   throws it, or if re-reading the file fails.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        // Invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.preinitialize();
            }
        }
        if (_reReadIfNeeded()) {
            validate();
        }
    }

    /** Read the contents of the file named by this parameter and create
     *  attributes in the current scope.
     *  @exception IOException If there is a problem reading the file.
     *  @exception IllegalActionException If there is a problem
     *  reading the previous attribute or  validating the settables
     *  @exception NameDuplicationException If there is a problem removing
     *  a previous attribute or creating a new variable.
     */
    public void read() throws IllegalActionException, NameDuplicationException,
    IOException {

        _fileName = fileOrURL.getExpression();

        if (_fileName == null || _fileName.trim().equals("")) {
            // Delete all previously defined attributes.
            if (_properties != null) {
                Iterator attributeNames = _properties.keySet().iterator();
                while (attributeNames.hasNext()) {
                    String attributeName = (String) attributeNames.next();
                    getAttribute(attributeName).setContainer(null);
                }
                _properties = null;
            }
            return;
        }

        URL url = fileOrURL.asURL();

        if (url == null) {
            throw new IOException("Could not convert \""
                    + fileOrURL.getExpression() + "\" with base \""
                    + fileOrURL.getBaseDirectory() + "\" to a URL.");
        }
        // NOTE: Properties are unordered, which is not
        // strictly right in Ptolemy II semantics.  However,
        // we wait until all are loaded before validating them,
        // so it should be OK.
        Properties properties = new Properties();
        InputStream inputStream = null;
        try {
            URLConnection connection = url.openConnection();
            inputStream = connection.getInputStream();
            properties.load(url.openStream());
            _date = connection.getDate();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable throwable) {
                    // Ignore.
                }
            }
        }

        if (_properties != null) {
            // Remove previous parameters that are not defined
            // in the new set.
            Iterator attributeNames = _properties.keySet().iterator();
            while (attributeNames.hasNext()) {
                String attributeName = (String) attributeNames.next();
                if (!properties.containsKey(attributeName)) {
                    getAttribute(attributeName).setContainer(null);
                }
            }
        }

        _properties = properties;

        // Iterate through all the properties and either create new parameters
        // or set current parameters.
        // Use entrySet for performance reasons.
        Iterator attributeMapEntries = properties.entrySet().iterator();
        while (attributeMapEntries.hasNext()) {
            Map.Entry attributeNames = (Map.Entry) attributeMapEntries.next();
            String attributeName = (String) attributeNames.getKey();
            String attributeValue = (String) attributeNames.getValue();
            Variable variable = (Variable) getAttribute(attributeName);
            if (variable == null) {
                variable = new Variable(this, attributeName);
            }
            variable.setExpression(attributeValue);
        }
    }

    /** Remove the specified object from the list of objects whose
     *  preinitialize(), initialize(), and wrapup()
     *  methods should be invoked upon invocation of the corresponding
     *  methods of this object. If the specified object is not
     *  on the list, do nothing.
     *  @param initializable The object whose methods should no longer be invoked.
     *  @see #addInitializable(Initializable)
     *  @see ptolemy.actor.CompositeActor#removePiggyback(Executable)
     */
    @Override
    public void removeInitializable(Initializable initializable) {
        if (_initializables != null) {
            _initializables.remove(initializable);
            if (_initializables.size() == 0) {
                _initializables = null;
            }
        }
    }

    /** Override the base class to register as a piggyback with the nearest opaque
     *  composite actor above in the hierarchy.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace.
     *  @exception NameDuplicationException If the container already has
     *   an entity with the name of this entity.
     */
    @Override
    public void setContainer(NamedObj container) throws IllegalActionException,
    NameDuplicationException {
        if (container != getContainer()) {
            // May need to unregister as a piggyback with the previous container.
            NamedObj previousContainer = getContainer();
            if (previousContainer instanceof CompositeActor) {
                ((CompositeActor) previousContainer).removePiggyback(this);
            }
        }
        super.setContainer(container);
        if (container instanceof CompositeActor) {
            ((CompositeActor) container).addPiggyback(this);
        }
    }

    /** Do nothing.
     */
    @Override
    public void stop() {
    }

    /** Do nothing.
     */
    @Override
    public void stopFire() {
    }

    /** Do nothing.
     */
    @Override
    public void terminate() {
    }

    /** Check to see whether the specified file has changed, and if so,
     *  re-read it, and invoke the wrapup() methods
     *  of objects that have been added using addInitializable().
     *  @exception IllegalActionException If one of the added objects
     *   throws it, or if re-reading the file fails.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        // Invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.wrapup();
            }
        }
        if (_reReadIfNeeded()) {
            validate();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** If either the file name or the date on the file have changed
     *  since the last reading, then re-read the file.
     *  @return True if re-reading was done.
     *  @exception IllegalActionException If re-reading the file fails.
     */
    private boolean _reReadIfNeeded() throws IllegalActionException {
        try {
            String currentFileName = fileOrURL.getExpression();
            if (!currentFileName.equals(_fileName)) {
                // File name has changed. Must re-read.
                read();
                return true;
            }
            URL url = fileOrURL.asURL();
            if (url == null) {
                throw new IOException("Could not convert \""
                        + fileOrURL.getExpression() + "\" with base \""
                        + fileOrURL.getBaseDirectory() + "\" to a URL.");
            }
            long date = url.openConnection().getDate();
            if (date == 0L || date != _date) {
                read();
                return true;
            }
            return false;
        } catch (NameDuplicationException ex) {
            // Two separate exceptions to get FindBugs to shut up.
            throw new IllegalActionException(this, ex,
                    "Failed to re-read parameter set, problem with dupliate names.");
        } catch (IOException ex2) {
            throw new IllegalActionException(this, ex2,
                    "Failed to re-read parameter set.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** Date of the file when last read. */
    private long _date = 0L;

    /** The previously read file name. */
    private String _fileName;

    /** List of objects whose (pre)initialize() and wrapup() methods
     *  should be slaved to these.
     */
    private transient List<Initializable> _initializables;

    /** Cached copy of the last hashset of properties, used to remove old
     *  properties.
     */
    private Properties _properties;
}
