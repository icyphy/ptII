/* Top-level window for Ptolemy models with a menubar and status bar.

 Copyright (c) 1998-2002 The Regents of the University of California.
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
@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (johnr@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.InternalErrorException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.swing.filechooser.FileFilter;

//////////////////////////////////////////////////////////////////////////
//// PtolemyFrame
/**
This is a top-level window for Ptolemy models with a menubar and status bar.
Derived classes should add components to the content pane using a
line like:
<pre>
    getContentPane().add(component, BorderLayout.CENTER);
</pre>
This extends the base class by associating with it a Ptolemy II model.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
*/
public abstract class PtolemyFrame extends TableauFrame {

    /** Construct a frame associated with the specified Ptolemy II model.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  @see Tableau#show()
     *  @param model The model to put in this frame, or null if none.
     */
    public PtolemyFrame(CompositeEntity model) {
        this(model, null);
    }

    /** Construct a frame associated with the specified Ptolemy II model.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  @see Tableau#show()
     *  @param model The model to put in this frame, or null if none.
     *  @param tableau The tableau responsible for this frame, or null if none.
     */
    public PtolemyFrame(CompositeEntity model, Tableau tableau) {
        super(tableau);
	_fileFilter = new XMLOrMoMLFileFilter();
        setModel(model);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the associated model.
     *  @return The associated model.
     */
    public CompositeEntity getModel() {
        return _model;
    }

    /** Set the associated model.
     *  @param model The associated model.
     */
    public void setModel(CompositeEntity model) {
        _model = model;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Clear the current contents.  First, check to see whether
     *  the contents have been modified, and if so, then prompt the user
     *  to save them.  A return value of false
     *  indicates that the user has canceled the action.
     *  @return False if the user cancels the clear.
     */
    protected boolean _clear() {
        if (super._clear()) {
            setModel(new CompositeEntity());
            return true;
        } else {
            return false;
        }
    }

    /** Display more detailed information than given by _about().
     */
    protected void _help() {
        try {
            URL doc = getClass().getClassLoader().getResource(
                    "ptolemy/configs/doc/basicHelp.htm");
            getConfiguration().openModel(null, doc, doc.toExternalForm());
        } catch (Exception ex) {
            _about();
        }
    }

    /** Query the user for a filename, save the model to that file,
     *  and open a new window to view the model.
     *  If setModel() has been called, then the initial filename
     *  is set to the name of the model.  If setModel() has not yet
     *  been called, then the initial filename to
     *  <code>model.xml</code>.
     *  @return True if the save succeeds.
     */
    protected boolean _saveAs() {
	if (_model == null || _model.getName().length() == 0) {
	    _initialSaveAsFileName = "model.xml";
	} else {
	    // We are not sanitizing the name here . . .
	    _initialSaveAsFileName = _model.getName() + ".xml";
	}
	return super._saveAs();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The model that this window controls, if any.
    private CompositeEntity _model;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Display only .xml and .moml files */
    class XMLOrMoMLFileFilter extends FileFilter {

	/** Accept only .xml or .moml files.
	 *  @param file The file to be checked.
	 *  @return true if the file is a directory, a .xml or a .moml file.
         */
	public boolean accept(File fileOrDirectory) {
	    if (fileOrDirectory.isDirectory()) {
		return true;
	    }

	    String fileOrDirectoryName = fileOrDirectory.getName();
	    int dotIndex = fileOrDirectoryName.lastIndexOf('.');
	    if (dotIndex == -1) {
		return false;
	    }
	    String extension =
		fileOrDirectoryName
		.substring(dotIndex);

	    if (extension != null) {
		if (extension.equalsIgnoreCase(".xml")
		    || extension.equalsIgnoreCase(".moml")) {
                    return true;
		} else {
		    return false;
		}
	    }
	    return false;
	}

	/**  The description of this filter */
	public String getDescription() {
	    return ".xml and .moml files";
	}
    }
}
