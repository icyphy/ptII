/* Top-level window for Ptolemy models with a menubar and status bar.

 Copyright (c) 1998-2000 The Regents of the University of California.
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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.actor.CompositeActor;
import ptolemy.data.expr.Parameter;
import ptolemy.data.StringToken;
import ptolemy.kernel.util.KernelException;
import ptolemy.gui.CancelException;
import ptolemy.gui.MessageHandler;
import ptolemy.gui.Top;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JFileChooser;

//////////////////////////////////////////////////////////////////////////
//// PtolemyTop
/**
This is a top-level window for Ptolemy models with a menubar and status bar.
Derived classes should add components to the content pane using a
line like:
<pre>
    getContentPane().add(component, BorderLayout.CENTER);
</pre>

@author Edward A. Lee
@version $Id$
*/
public abstract class PtolemyTop extends Top {

    /** Construct an empty top-level frame. The window is centered on the
     *  screen, and is separately iconified and deiconified by the window
     *  manager. After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     */
    public PtolemyTop() {
        super();
    }

    /** Get the view that created this frame.
     *  @return The view.
     */
    public View getView() {
        return _view;
    }

    /** Set the key used to identify this window, and display a string
     *  representation of this key in the titlebar.
     *  @param key The key identifying the model.
     */
    public void setView(View view) {
	_view = view;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The view that created this frame.
    private View _view = null;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Read the specified URL.  This delegates to the ModelDirectory
     *  to ensure that the preferred view of the model is opened, and
     *  that a model is not opened more than once.
     *  @param url The URL to read.
     *  @exception Exception If the URL cannot be read.
     */
    protected void _read(URL url) throws Exception {
        // NOTE: Used to use for the first argument the following, but
        // it seems to not work for relative file references:
        // new URL("file", null, _directory.getAbsolutePath()
        Application application = (Application)_view.toplevel();
        application.openModel(url, url, url.toExternalForm());
    }

    /** Query the user for a filename and save the model to that file.
     *  This overrides the base class to update the entry in the
     *  ModelDirectory.
     */
    protected void _saveAs() {
        JFileChooser fileDialog = new JFileChooser();
        fileDialog.setDialogTitle("Save as...");
        if (_directory != null) {
            fileDialog.setCurrentDirectory(_directory);
        } else {
            // The default on Windows is to open at user.home, which is
            // typically an absurd directory inside the O/S installation.
            // So we use the current directory instead.
            // FIXME: This will probably fail with a security exception in
            // applets.
            String cwd = System.getProperty("user.dir");
            if (cwd != null) {
                fileDialog.setCurrentDirectory(new File(cwd));
            }
        }
        int returnVal = fileDialog.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileDialog.getSelectedFile();

	    // update the name of the model proxy.
            try {
		String newKey = file.toURL().toExternalForm();
		ModelProxy proxy = (ModelProxy)getView().getContainer();
	        Parameter id = (Parameter)proxy.getAttribute("identifier");
		id.setToken(new StringToken(newKey));
	    } catch (MalformedURLException ex) {
                try {
                    MessageHandler.warning(
                            "Unable to associate file with a URL: " + ex);
                } catch (CancelException exception) {}
            } catch (KernelException ex) {
                try {
                    MessageHandler.warning(
                            "Unable to associate file with a URL: " + ex);
                } catch (CancelException exception) {}
            }

            _file = file;
	    // FIXME: better title.
	    setTitle(_file.getName());
	    _directory = fileDialog.getCurrentDirectory();
            _save();
        }
    }
}
