/*
 A clean reimplementation of Ptolemy II's original ExtensionFilenameFilter, that was based on Sun-licensed code.

 Copyright (c) 2016 The Regents of the University of California.
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
package ptolemy.gui;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * An implementation of both javax.swing.filechooser.FileFilter and
 * java.io.FilenameFilter that only accepts files that have one of the
 * registered extensions.
 * <p>
 * This class is provided in Ptolemy II to support usage with both
 * java.awt.FileDialog and javax.swing.JFileChooser. As it appears that on
 * MacOSX the FileDialog implementation is better than the JFileChooser, Ptolemy
 * II has some logic in ptolemy.gui.PtGUIUtilities.useFileDialog() to decide
 * which "file-picker"-component to use. Several Ptolemy II classes use this
 * utility while still passing a common filter instance to the selected
 * component. (e.g. ptolemy.gui.Query, ptolemy.gui.Top ...)
 * </p>
 * <p>
 * The javax.swing.filechooser.FileNameExtensionFilter in the JDK can only be
 * used with JFileChooser.
 * </p>
 *
 * @version $Id$
 * @author Erwin De Ley
 * @since Ptolemy II 11.0
 */
public class ExtensionFilenameFilter extends PtFilenameFilter {

	/**
	 * Construct a file filter that filters out all files that do not have one
	 * of the extensions in the given list.
	 *
	 * @param extensions
	 *            the file extensions to use
	 */
	public ExtensionFilenameFilter(List<String> extensions) {
		for (String ext : extensions) {
			registerExtension(ext);
		}
	}

	/**
	 * Creates a filter that accepts the given file type, specified by a number
	 * of extensions and a meaningful description of the file types involved.
	 *
	 * @param description
	 *            a description of the types of files with one of the given
	 *            extensions
	 * @param extensions
	 *            the file extensions to use
	 */
	public ExtensionFilenameFilter(String description, List<String> extensions) {
		for (String ext : extensions) {
			registerExtension(ext);
		}
		setDescription(description);
	}

	/**
	 * Creates a filter that accepts the given file type, specified by a number
	 * of extensions and a meaningful description of the file types involved.
	 *
	 * @param description
	 *            a description of the types of files with one of the given
	 *            extensions
	 * @param extensions
	 *            the file extensions to use
	 */
	public ExtensionFilenameFilter(String description, String... extensions) {
		for (String ext : extensions) {
			registerExtension(ext);
		}
		setDescription(description);
	}

	/**
	 * Return true if the given file has one of the registered extensions, or is
	 * a directory. Otherwise, or if the file is null, return false.
	 * <p>
	 * Files whose name begins with "." are not accepted.
	 * </p>
	 *
	 * @param file
	 *            The file to be checked.
	 * @return true if the given file has one of the registered extensions, or
	 *         is a directory.
	 */
	@Override
	public boolean accept(File file) {
		if (file == null) {
			return false;
		} else if (file.isDirectory()) {
			return true;
		} else {
			String ext = getExtension(file);
			return (ext != null && _registeredExtensions.contains(ext));
		}
	}

	/**
	 * Return true if the given file name has one of the registered extensions,
	 * or is a directory. Otherwise, or if the directory or name is null, return
	 * false.
	 * <p>
	 * Files whose name begins with "." are not accepted.
	 * </p>
	 *
	 * @param directory
	 *            the parent directory of the file
	 * @param name
	 *            the name of the file.
	 * @return true if the given file has one of the registered extensions, or
	 *         is a directory.
	 */
	@Override
	public boolean accept(File directory, String name) {
		if (name == null || directory == null) {
			return false;
		} else {
			return accept(new File(directory, name));
		}
	}

	/**
	 * @return The human readable description of the types of files accepted by
	 *         this filter.
	 */
	@Override
	public String getDescription() {
		return _description;
	}

	/**
	 * Set the human readable description of the types of files accepted by this
	 * filter.
	 *
	 * @param description
	 *            the human readable description of the types of files accepted
	 *            by this filter.
	 */
	public void setDescription(String description) {
		_description = description;
	}

	/**
	 * @return description + registered extensions
	 */
	@Override
	public String toString() {
		return getDescription() + " : " + _registeredExtensions;
	}

	///////////////////////////////////////////////////////////////////
	////              protected methods                           ////
	/**
	 * Register an additional extension to accept.
	 *
	 * @param extension
	 *            The extension to be added.
	 */
	protected void registerExtension(String extension) {
		if (extension == null || "".equals(extension.trim())) {
			return;
		} else {
			_registeredExtensions.add(extension.trim().toLowerCase(Locale.getDefault()));
		}
	}

	///////////////////////////////////////////////////////////////////
	////                  private methods                          ////
	/**
	 * Return the extension of the given file.
	 * <p>
	 * If the file is null, if it's name does not contain a '.', or if it ends
	 * in a '.', this method returns null.
	 * </p>
	 * 
	 * @param file
	 * @return the extension of the file or null if the file is null, or if it
	 *         has no extension.
	 */
	private String getExtension(File file) {
		if (file == null) {
			return null;
		} else {
			String fileName = file.getName();
			int i = fileName.lastIndexOf('.');
			if (i > 0 && i < fileName.length() - 1) {
				String ext = fileName.substring(i + 1);
				return ext.toLowerCase(Locale.getDefault());
			} else {
				return null;
			}
		}
	}

	///////////////////////////////////////////////////////////////////
	////               private fields                              ////
	private Set<String> _registeredExtensions = new HashSet<>();

	private String _description = null;
}
