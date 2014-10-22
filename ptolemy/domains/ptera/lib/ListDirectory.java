/* An event to list all the files matching a filter in a directory.

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
package ptolemy.domains.ptera.lib;

import java.io.File;

import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ptera.kernel.Event;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.util.RecursiveFileFilter;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// ListDirectory

/**
 An event to list all the files matching a filter in a directory. Each time it
 is fired, it updates its {@link #files} parameter to contain names of all the
 files (and/or directories) in the specified directory. A filter can be defined
 to filter the included file names.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ListDirectory extends Event {

    /** Construct an event with the given name contained by the specified
     *  composite entity. The container argument must not be null, or a
     *  NullPointerException will be thrown. This event will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string.
     *  Increment the version of the workspace.
     *  This constructor write-synchronizes on the workspace.
     *
     *  @param container The container.
     *  @param name The name of the state.
     *  @exception IllegalActionException If the state cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   that of an entity already in the container.
     */
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

    /** Process this event with the given arguments. The number of arguments
     *  provided must be equal to the number of formal parameters defined for
     *  this event, and their types must match. The actions of this event are
     *  executed.
     *
     *  @param arguments The arguments used to process this event, which must be
     *   either an ArrayToken or a RecordToken.
     *  @return A refiring data structure that contains a non-negative double
     *   number if refire() should be called after that amount of model time, or
     *   null if refire() need not be called.
     *  @exception IllegalActionException If the number of the arguments or
     *   their types do not match, the actions cannot be executed, or any
     *   expression (such as guards and arguments to the next events) cannot be
     *   evaluated.
     *  @see #refire(Token, RefiringData)
     */
    @Override
    public RefiringData fire(Token arguments) throws IllegalActionException {
        File[] listedFiles = RecursiveFileFilter.listFiles(directory.asFile(),
                ((BooleanToken) recursive.getToken()).booleanValue(),
                ((BooleanToken) includeFiles.getToken()).booleanValue(),
                ((BooleanToken) includeDirectories.getToken()).booleanValue(),
                false /*filesOnly*/, false /*directoriesOnly*/,
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

    /** Specify the container, adding the entity to the list
     *  of entities in the container.  If the container already contains
     *  an entity with the same name, then throw an exception and do not make
     *  any changes.  Similarly, if the container is not in the same
     *  workspace as this entity, throw an exception.  If this entity is
     *  a class element and the proposed container does not match
     *  the current container, then also throw an exception.
     *  If the entity is already contained by the container, do nothing.
     *  If this entity already has a container, remove it
     *  from that container first.  Otherwise, remove it from
     *  the directory of the workspace, if it is present.
     *  If the argument is null, then unlink the ports of the entity
     *  from any relations and remove it from its container.
     *  It is not added to the workspace directory, so this could result in
     *  this entity being garbage collected.
     *  Derived classes may further constrain the container
     *  to subclasses of CompositeEntity by overriding the protected
     *  method _checkContainer(). This method validates all
     *  deeply contained instances of Settable, since they may no longer
     *  be valid in the new context.  This method is write-synchronized
     *  to the workspace and increments its version number.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace, or
     *   if the protected method _checkContainer() throws it, or if
     *   a contained Settable becomes invalid and the error handler
     *   throws it.
     *  @exception NameDuplicationException If the name of this entity
     *   collides with a name already in the container.
     *  @see #getContainer()
     */
    @Override
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        super.setContainer(container);

        if (container != null && directory != null) {
            directory.setBaseDirectory(URIAttribute.getModelURI(container));
        }
    }

    /** The director in which files are to be listed.
     */
    public FileParameter directory;

    /** A parameter with an ArrayToken containing strings that are names of the
     *  files matching the given filter in the specified directory. It is
     *  updated every time this event is fired.
     */
    public Parameter files;

    /** The filter that all the included file names must match.
     */
    public StringParameter filter;

    /** Whether directory names should be included.
     */
    public Parameter includeDirectories;

    /** Whether file names should be included.
     */
    public Parameter includeFiles;

    /** Whether files in the subdirectories should be searched as well.
     */
    public Parameter recursive;

}
