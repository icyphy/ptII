/* An actor that reads a URL naming a directory and outputs each 
element of the directory one at a time.

@Copyright (c) 1998-2001 The Regents of the University of California.
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

						PT_COPYRIGHT_VERSION 2
						COPYRIGHTENDKEY
@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (liuj@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.Token;
import ptolemy.data.StringToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;

import java.io.File;
import java.io.IOException;
import java.net.URL;

//////////////////////////////////////////////////////////////////////////
//// URLDirectoryReader
/**
This actor reads a URL and if the URL names a directory, it outputs
the name of each file or subdirectory contained in the directory. 
If the URL names a file, then it outputs the name of that file.

<p>If the <i>repeat</i> flag is true, then the sequence of file
names is repeated indefinitely.
If the <i>refresh</i> flag is true, and the <i>repeat</i> flag is
true, then the directory is re-read before repeating the sequence of
files and subdirectories.


<p>One alternative implementation would be that if the URL named a file,
then the actor would output the names of the files and subdirectories
in the adjacent file.
<br>Another alternative implementation would output the names of the
files and subdirectories in an array.
<br>An extension would be to include a filter parameter that could be
a regular expression that would allow us to filter the file names.
<br> Another extension would be to include an actor that would filter
file names.
<br> Should this actor extend URLReader or SequenceActor?

@author  Christopher Hylands
@version $Id$ */
public class URLDirectoryReader extends URLReader {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public URLDirectoryReader(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Set the type of the output port.
        output.setTypeEquals(BaseType.STRING);

        // Set the repeat Flag.
        repeat = new Parameter(this, "repeat", new BooleanToken(false));
	repeat.setTypeEquals(BaseType.BOOLEAN);
        attributeChanged(repeat);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Repeat after outputting all elements of the directory.
     *	Default is false.
     */
    public Parameter repeat;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the specified attribute is <i>URL</i>, then close
     *  the current file (if there is one) and open the new one.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>URL</i> and the file cannot be opened.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == repeat) {
            _repeatFlag = ((BooleanToken)repeat.getToken()).booleanValue();
        }
        super.attributeChanged(attribute);
    }

    /** Output the data read in the prefire.
     *  @exception IllegalActionException If there's no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
	output.broadcast(new StringToken(_data[_iterationCount]));
    }

    /** Open the file at the URL, and set the width of the output.
     *  @exception IllegalActionException Not thrown in this base class
     */
    public void initialize() throws IllegalActionException {
	super.initialize();
        _iterationCount = 0;
    }

    /** Update the iteration counter until it exceeds the number of
     *  elements in the directory.  If the <i>repeat</i> parameter
     *  is true, then repeat the same sequence of directory elements
     *  again.  If the <i>repeat</i> and <i>refresh</i> parameters
     *  are both true, then reread the directory before repeating
     *  the sequence of directory elements
     *
     *  @exception IllegalActionException If the sourceURL is not valid.
     */
    public boolean postfire() throws IllegalActionException {
	_iterationCount++;
	if (_iterationCount >= _data.length) {
	    if (!_repeatFlag) {
		return false;
	    } else {
		_iterationCount = 0;
		if (_refreshFlag) {
		    _data = _list(_source);
		}
	    }
	}
	return super.postfire();
    }

    /** Read one row from the input and prepare for output them.
     *  @exception IllegalActionException If the <i>sourceURL</i> is invalid.
     */
    public boolean prefire() throws IllegalActionException {
        try {
	    _data = _list(_source);
            return super.prefire();
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** If the URL names a directory return an array containing
     *  the names of the files and subdirectories contained in the
     *  directory.  If the URL names a file, then return an array
     *  of size 1 containing the name of the file.  If the URL
     *  names neither a file or directory, return null.
     *
     *  @param source The filename or URL to open
     *  @return An array of Strings where each element of the array
     *  names a file or subdirectory.
     *  @exception IllegalActionException If the source is a malformed
     *  URL
     */
    private String [] _list(String source) throws IllegalActionException{
	try {
	    URL sourceURL = new URL(source);

	    if (sourceURL.getProtocol().equals("file")) {
		// First, try opening the source as a file.
		File file = new File(sourceURL.getFile());
		if (file.isDirectory()) { 
		    return file.list();
		} else if (file.isFile()) {
		    return new String[] {file.toString()};
		} else {
		    throw new IllegalActionException("'" + source
						     + "' is neither a file "
						     + "or a directory?");
		}
	    } else {
		// FIXME: handle urls here.
		throw new IllegalActionException("'" + source + "' does not "
						 + "have the file: protocol");
	    }
	} catch (Exception ex) {
	    throw new IllegalActionException("Could not open '" + source
					     + "' :" + ex);
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // Count of the iterations.
    private int _iterationCount = 0;

    // An array containing the files and subdirectories in the directory
    // named by sourceURL.
    // FIXME: Should we clone this?
    private String[] _data;

    // Flag to indicate whether or not to repeat the sequence.
    private boolean _repeatFlag;
}
