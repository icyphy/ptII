/*

 Copyright (c) 2008 The Regents of the University of California.
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
package ptolemy.actor.gt.controller;

import java.io.File;

import ptolemy.actor.gt.util.RecursiveFileFilter;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// ListDirectory

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ListDirectory extends GTEvent {

    public ListDirectory(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        directory = new FileParameter(this, "directory");
        directory.setExpression(".");
        Parameter allowFiles = new Parameter(directory, "allowFiles");
        allowFiles.setTypeEquals(BaseType.BOOLEAN);
        allowFiles.setToken(BooleanToken.FALSE);
        Parameter allowDirectories = new Parameter(directory,
                "allowDirectories");
        allowDirectories.setTypeEquals(BaseType.BOOLEAN);
        allowDirectories.setToken(BooleanToken.TRUE);

        filter = new StringParameter(this, "filter");
        filter.setExpression("*.xml");

        includeFiles = new Parameter(this, "includeFiles");
        includeFiles.setTypeEquals(BaseType.BOOLEAN);
        includeFiles.setExpression("true");

        includeDirectories = new Parameter(this, "includeDirectories");
        includeDirectories.setTypeEquals(BaseType.BOOLEAN);
        includeDirectories.setExpression("false");

        recursive = new Parameter(this, "recursive");
        recursive.setTypeEquals(BaseType.BOOLEAN);
        recursive.setExpression("false");

        files = new Parameter(this, "files");
        files.setExpression("{ }");
        files.setVisibility(Settable.NOT_EDITABLE);
        files.setPersistent(false);
        Variable variable = new Variable(files, "_textHeightHint");
        variable.setExpression("5");
        variable.setPersistent(false);
    }

    public RefiringData fire(ArrayToken arguments) throws IllegalActionException {
        File[] listedFiles = RecursiveFileFilter.listFiles(directory.asFile(),
                ((BooleanToken) recursive.getToken()).booleanValue(),
                ((BooleanToken) includeFiles.getToken()).booleanValue(),
                ((BooleanToken) includeDirectories.getToken()).booleanValue(),
                filter.stringValue());
        StringBuffer buffer = new StringBuffer("{ ");
        int i = 0;
        for (File file : listedFiles) {
            if (i++ > 0) {
                buffer.append(",\n  ");
            }
            buffer.append('\"');
            buffer.append(StringUtilities.escapeString(file.getPath()));
            buffer.append('\"');
        }
        if (listedFiles.length > 0) {
            buffer.append(' ');
        }
        buffer.append('}');
        files.setExpression(buffer.toString());

        return super.fire(arguments);
    }

    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        super.setContainer(container);

        if (container != null && directory != null) {
            directory.setBaseDirectory(URIAttribute.getModelURI(container));
        }
    }

    public FileParameter directory;

    public Parameter files;

    public StringParameter filter;

    public Parameter includeDirectories;

    public Parameter includeFiles;

    public Parameter recursive;
}
