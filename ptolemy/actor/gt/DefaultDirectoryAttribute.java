/*

@Copyright (c) 1997-2008 The Regents of the University of California.
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

						PT_COPYRIGHT_VERSION_2
						COPYRIGHTENDKEY



 */

package ptolemy.actor.gt;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.kernel.util.Workspace;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class DefaultDirectoryAttribute extends ParameterAttribute implements
        ValueListener {

    public DefaultDirectoryAttribute(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    public DefaultDirectoryAttribute(Workspace workspace) {
        super(workspace);
    }

    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        super.setContainer(container);
        if (container != null) {
            _checkContainerClass(container, Pattern.class, false);
            _checkUniqueness(container);
        }
    }

    public void valueChanged(Settable settable) {
        String display = directory.getExpression() + "/";
        String filter = fileFilter.getExpression();
        if (filter.equals("")) {
            display += "*.xml";
        } else {
            display += filter;
        }
        try {
            if (subdirs.getToken().equals(BooleanToken.TRUE)) {
                display += " [...]";
            }
        } catch (IllegalActionException e) {
        }
        parameter.setExpression(display);
    }

    public FileParameter directory;

    public StringParameter fileFilter;

    public Parameter subdirs;

    protected void _initParameter() throws IllegalActionException,
            NameDuplicationException {
        parameter = new StringParameter(this, "display");
        parameter.setDisplayName("Display (./)");
        parameter.setPersistent(false);
        parameter.setVisibility(NONE);

        directory = new FileParameter(this, "directory");
        directory.setDisplayName("Directory");
        directory.setExpression(".");
        directory.addValueListener(this);
        Parameter allowFiles = new Parameter(directory, "allowFiles");
        allowFiles.setTypeEquals(BaseType.BOOLEAN);
        allowFiles.setToken(BooleanToken.FALSE);
        Parameter allowDirectories = new Parameter(directory,
                "allowDirectories");
        allowDirectories.setTypeEquals(BaseType.BOOLEAN);
        allowDirectories.setToken(BooleanToken.TRUE);

        fileFilter = new StringParameter(this, "filter");
        fileFilter.setDisplayName("File filter (*.xml)");
        fileFilter.setExpression("");
        fileFilter.addValueListener(this);

        subdirs = new Parameter(this, "subdirs");
        subdirs.setDisplayName("Include subdirs");
        subdirs.setTypeEquals(BaseType.BOOLEAN);
        subdirs.setExpression("true");
        subdirs.addValueListener(this);
    }

}
