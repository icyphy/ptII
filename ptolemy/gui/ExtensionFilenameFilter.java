/*
 * @(#)ExtensionFileFilter.java        1.7 99/04/23
 *
 * Copyright (c) 1998, 1999 by Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */
package ptolemy.gui;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.swing.filechooser.FileFilter;

/**
 * A convenience implementation of FileFilter that filters out
 * all files except for those type extensions that it knows about.
 *
 * <p>Extensions are of the type ".foo", which is typically found on
 * Windows and Unix boxes, but not on Macintosh. Case is ignored.
 *
 * <p>Extension - create a new filter that filters out all files
 * but gif and jpg image files:
 *
 * <pre>
 *     JFileChooser chooser = new JFileChooser();
 *     ExtensionFilenameFilter filter = new ExtensionFilenameFilter(
 *                   new String{"gif", "jpg"}, "JPEG & GIF Images")
 *     chooser.addChoosableFileFilter(filter);
 *     chooser.showOpenDialog(this);
 * </pre>

 * <p>Note that as of Java 1.6, there is a FileNameExtensionFilter which
 * replaces this class.  See
 * http://download.oracle.com/javase/6/docs/api/javax/swing/filechooser/FileNameExtensionFilter.html
 * However, this class can be used with both java.awt.FileDialog
 * and javax.swing.JFileChooser because it implements java.io.FilenameFilter
 * and extends javax.swing.FilenameFilter.</p>
 *
 * @version $Id$
 * @author Jeff Dinkins
@version $Id$
@since Ptolemy II 9.0
 */
public class ExtensionFilenameFilter extends PtFilenameFilter {
    // This is a duplicate of diva.gui.ExtensionFileFilter because
    // we want to be able to use PtFileChooser with this filter.

    /**
     * Creates a file filter. If no filters are added, then all
     * files are accepted.
     *
     * @see #addExtension(String)
     */
    public ExtensionFilenameFilter() {
        this((String) null, (String) null);
    }

    /**
     * Creates a file filter that accepts files with the given extension.
     * Example: new ExtensionFileFilter("jpg");
     * @param extension The file name extension with no leading period
     * ('.').
     * @see #addExtension(String)
     */
    public ExtensionFilenameFilter(String extension) {
        this(extension, null);
    }

    /**
     * Creates a file filter that accepts the given file type.
     * Example: new ExtensionFileFilter("jpg", "JPEG Image Images");
     *
     * Note that the "." before the extension is not needed. If
     * provided, it will be ignored.
     * @param extension The file name extension with no leading period
     * ('.').
     * @param description A description of this file filter.
     * @see #addExtension(String)
     */
    public ExtensionFilenameFilter(String extension, String description) {
        this(new String[] { extension }, description);
    }

    /**
     * Creates a file filter from the given string array.
     * Example: new ExtensionFileFilter(String {"gif", "jpg"});
     *
     * Note that the "." before the extension is not needed and
     * will be ignored.
     * @param filters An array of file extensions.
     * @see #addExtension(String)
     */
    public ExtensionFilenameFilter(String[] filters) {
        this(filters, null);
    }

    /**
     * Creates a file filter from the given string array and description.
     * Example: new ExtensionFileFilter(String {"gif", "jpg"}, "Gif and JPG Images");
     *
     * Note that the "." before the extension is not needed and will be
     * ignored.
     *
     * @param filters An array of file extensions.
     * @param description A description of this file filter.
     * @see #addExtension(String)
     */
    public ExtensionFilenameFilter(String[] filters, String description) {
        _filters = new Hashtable(filters.length);

        for (String filter : filters) {
            // add filters one by one
            addExtension(filter);
        }

        setDescription(description);
    }

    /** Construct a file filter that filters out all files that do
     *  not have one of the extensions in the given list.
     *  @param extensions A list of extensions, each of which is
     *   a String.
     */
    public ExtensionFilenameFilter(List<String> extensions) {
        Iterator extensionsIterator = extensions.iterator();
        while (extensionsIterator.hasNext()) {
            String matchExtension = (String) extensionsIterator.next();
            addExtension(matchExtension);
        }
    }

    /**
     * Return true if this file should be shown in the directory pane,
     * false if it shouldn't.
     *
     * <p>Files that begin with "." are ignored.</p>
     *
     * <p>This method is used by javax.swing.JFileChoosers.
     *
     * @param f The file to be checked.
     * @return True if the argument is a directory or if the the extension
     * of the file matches one of the extensions.
     * @see #accept(File, String)
     * @see #getExtension(File)
     * @see FileFilter#accept(File)
     */
    public boolean accept(File f) {
        if (f != null) {
            if (f.isDirectory()) {
                return true;
            }

            String extension = getExtension(f);

            if (extension != null && _filters.get(extension) != null) {
                return true;
            }
        }

        return false;
    }

    /**
     * Return true if this file should be shown in the directory pane,
     * false if it shouldn't.
     *
     * <p>Files that begin with "." are ignored.</p>
     *
     * <p>This method is used by java.awt.FileDialog.
     *
     * @param directory The directory in which the file was found.
     * @param name The name of the file.
     * @return Return true if this file should be shown in the directory pane,
     * false if it shouldn't.
     * @see #accept(File)
     * @see #getExtension(File)
     * @see FileFilter#accept(File)
     */
    public boolean accept(File directory, String name) {
        int i = name.lastIndexOf('.');

        String extension = "";
        if (i > 0 && i < name.length() - 1) {
            extension = name.substring(i + 1).toLowerCase();
        }

        if (extension != null && _filters.get(extension) != null) {
            return true;
        }
        return false;
    }

    /**
     * Add a filetype "dot" extension to filter against.
     *
     * For example: the following code will create a filter that filters
     * out all files except those that end in ".jpg" and ".tif":
     * <pre>
     *   ExtensionFileFilter filter = new ExtensionFileFilter();
     *   filter.addExtension("jpg");
     *   filter.addExtension("tif");
     * </pre>
     * Note that the "." before the extension is not needed and will be ignored.
     * @param extension The extension to be added.
     */
    public void addExtension(String extension) {
        if (extension == null) {
            return;
        }

        if (_filters == null) {
            _filters = new Hashtable(5);
        }

        _filters.put(extension.toLowerCase(), this);
        _fullDescription = null;
    }

    /**
     * If the filter contains only one extension, return the extension
     * name.  Otherwise, return null.
     * @return The default extension.
     */
    public String getDefaultExtension() {
        // Added by Heloise Hse
        if (_filters.size() == 1) {
            return (String) _filters.keys().nextElement();
        } else {
            return null;
        }
    }

    /**
     * Return the human readable description of this filter. For
     * example: "JPEG and GIF Image Files (*.jpg, *.gif)"
     *
     * @return The human readable description of this filter.
     * @see #setDescription(String)
     * @see #setExtensionListInDescription(boolean)
     * @see #isExtensionListInDescription()
     * @see FileFilter#getDescription()
     */
    public String getDescription() {
        if (_fullDescription == null) {
            if (_description == null || isExtensionListInDescription()) {
                if (_description != null) {
                    _fullDescription = _description;
                } else {
                    _fullDescription = "";
                }

                //_fullDescription += " (";

                StringBuffer result = new StringBuffer();
                //Iterator extensions = _filters.values().iterator();
                Enumeration extensions = _filters.keys();
                int extensionNumber = 1;
                int size = _filters.size();

                while (extensions.hasMoreElements()) {
                    String extension = (String) extensions.nextElement();
                    result.append(".");
                    result.append(extension);

                    if (extensionNumber < size - 1) {
                        result.append(", ");
                    } else if (extensionNumber < size) {
                        result.append(" and ");
                    }

                    extensionNumber++;
                }

                result.append(" files");
                _fullDescription += result;

                // // build the description from the extension list
                // Enumeration extensions = _filters.keys();

                // if (extensions != null) {
                //     _fullDescription += ("." + (String) extensions.nextElement());

                //     while (extensions.hasMoreElements()) {
                //         _fullDescription += (", " + (String) extensions
                //                 .nextElement());
                //     }
                // }

                //_fullDescription += ")";
            } else {
                _fullDescription = _description;
            }
        }

        return _fullDescription;
    }

    /**
     * Return the extension portion of the file's name.
     * @param f The file.
     * @return the extension portion of the name of the file or null
     * if the argument is null.
     */
    public String getExtension(File f) {
        if (f != null) {
            String filename = f.getName();
            int i = filename.lastIndexOf('.');

            if (i > 0 && i < filename.length() - 1) {
                return filename.substring(i + 1).toLowerCase();
            }
        }
        return null;
    }

    /**
     * Return true if the extension list (.jpg, .gif, etc) should
     * show up in the human readable description.
     *
     * Only relevant if a description was provided in the constructor
     * or using setDescription();
     *
     * @return True if the extension list (.jpg, .gif, etc) should
     * show up in the human readable description.
     * @see #getDescription()
     */
    public boolean isExtensionListInDescription() {
        return _useExtensionsInDescription;
    }

    /**
     * Set the human readable description of this filter. For
     * example: filter.setDescription("Gif and JPG Images");
     *
     * @param description The human readable description of this
     * filter.
     * @see #getDescription()
     * @see #setExtensionListInDescription(boolean)
     * @see #isExtensionListInDescription()
     */
    public void setDescription(String description) {
        _description = description;
        _fullDescription = null;
    }

    /**
     * Determine whether the extension list (.jpg, .gif, etc) should
     * show up in the human readable description.
     *
     * This method is only relevant if a description was provided in
     * the constructor or using setDescription();
     *
     * @param useExtensionListInDescription True if the list of extensions
     * should appear in the human readable description.
     * @see #getDescription()
     * @see #isExtensionListInDescription()
     */
    public void setExtensionListInDescription(
            boolean useExtensionListInDescription) {
        _useExtensionsInDescription = useExtensionListInDescription;
        _fullDescription = null;
    }

    /**
     * Return a string description of this filter.
     *
     * @see #getDescription()
     */
    public String toString() {
        return getDescription();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////
    private Hashtable _filters = null;

    private String _description = null;

    private String _fullDescription = null;

    private boolean _useExtensionsInDescription = true;
}
