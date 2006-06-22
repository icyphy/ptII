/* A scope extending attribute that reads multiple values from a file.

 Copyright (c) 2006 The Regents of the University of California.
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
package ptolemy.data.expr;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Properties;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.util.FileUtilities;
import ptolemy.vergil.toolbox.FileEditorTableauFactory;

//////////////////////////////////////////////////////////////////////////
//// ParameterSet

/**
 An attribute that reads attributes from a file or URL.
 
 The values are in the form:
 <pre>
 <i>attributeName</i> = <i>value<i>
 <pre>
 where <code><i>variableName</i></code> is the name of the attribute
 in a format suitable for {@link ptolemy.kernel.util.NamedObj#setName(String)}
 (i.e., does not contain periods) and  <code><i>value<i></code> is
 the expression in the Ptolemy expression language.
 Comments are lines that begin with the <code>#</code> character.
 Each line in the file is interpreted as a separate assignment.

 <p>The attributes that are created will have the same
 visibility as parameters of the container of the attribute.

 <p>Note that the order the parameters are created is arbitrary,
 this is because we read the file in using java.util.Properties.load(),
 which uses a HashMap to store the properties.  We use a Properties.load()
 because it provides a nice parser for the files and can read and write
 values in both text and XML.

 @author Christopher Brooks, contributor: Edward A. Lee
 @version $Id$
 @see ptolemy.data.expr.Variable
 */
public class ParameterSet extends ScopeExtendingAttribute {
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
        //FileEditorTableauFactory editor = new FileEditorTableauFactory(this, "_editorFactory");
        //((StringAttribute) editor.getAttribute("attributeName")).setExpression("fileOrURL");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

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

    /** If the parameter is <i>fileOrURL</i>, and the specified file
     *  name is not null, then open and read the file.
     *  @param attribute The attribute that changed.
     *  @throws IllegalActionException If the superclass throws it, or
     *   if the file cannot be read, or if the file parameters cannot
     *   be evaluated.
     */
    public void attributeChanged(Attribute attribute) throws IllegalActionException {
        if (attribute == fileOrURL) {
            try {
                read();
            } catch (Exception exception) {
                throw new IllegalActionException(this, exception,
                        "Failed to read file: " + fileOrURL.getExpression());
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Read the contents of the file named by this parameter and create
     *  attributes in the current scope.
     *  @exception IOException If there is a problem reading the file.
     */ 
    public void read() throws IllegalActionException, NameDuplicationException,
            IOException {

        String fileName = fileOrURL.getExpression();
        if (fileName == null || fileName.trim().equals("")) {
            // FIXME: Don't remove _iconDescription!!!! Other parameters?
            // Remove all contained parameters.
            // Copy the list of parameters to avoid concurrent modification exception.
            /*
            List attributeList = new LinkedList(attributeList());
            Iterator attributes = attributeList.iterator();
            while (attributes.hasNext()) {
                Attribute attribute = (Attribute)attributes.next();
                if (attribute != fileOrURL) {
                    attribute.setContainer(null);
                }
            }
            */
            return;
        }
        URL url = FileUtilities.nameToURL(fileName,
                fileOrURL.getBaseDirectory(),
                getClass().getClassLoader());
        if (url == null) {
            throw new IOException("Could not convert \""
                    + fileOrURL.getExpression()
                    + "\" with base \""
                    + fileOrURL.getBaseDirectory() + "\" to a URL.");
        }
        
        // FIXME: Properties are unordered, is this a problem?
        Properties properties = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = url.openStream(); 
            properties.load(url.openStream());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception ex) {
                    // Ignore.
                }
            }
        }

        Iterator attributeNames = properties.keySet().iterator();
        while (attributeNames.hasNext()) {
            String attributeName = (String) attributeNames.next();
            String attributeValue = (String) properties.get(attributeName);
            Parameter variable = null;
            if (getAttribute(attributeName) == null) {
                variable = new Parameter(this, attributeName);
            } else {
                variable = (Parameter)getAttribute(attributeName);
            }
            
            variable.setExpression(attributeValue);
        }
    }
}
