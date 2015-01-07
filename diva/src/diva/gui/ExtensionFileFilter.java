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
package diva.gui;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

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
 *     ExtensionFileFilter filter = new ExtensionFileFilter(
 *                   new String{"gif", "jpg"}, "JPEG &amp; GIF Images")
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
 * @deprecated Use ptolemy.gui.ExtensionFilenameFilter because it can be used with PtFileChooser
 * @author Jeff Dinkins
 */
@Deprecated
public class ExtensionFileFilter extends FileFilter implements FilenameFilter {
    //private static String TYPE_UNKNOWN = "Type Unknown";

    //private static String HIDDEN_FILE = "Hidden File";

    private Hashtable filters = null;

    private String description = null;

    private String fullDescription = null;

    private boolean useExtensionsInDescription = true;

    /**
     * Creates a file filter. If no filters are added, then all
     * files are accepted.
     *
     * @see #addExtension(String)
     */
    public ExtensionFileFilter() {
        this((String) null, (String) null);
    }

    /**
     * Creates a file filter that accepts files with the given extension.
     * Example: new ExtensionFileFilter("jpg");
     *
     * @see #addExtension(String)
     */
    public ExtensionFileFilter(String extension) {
        this(extension, null);
    }

    /**
     * Creates a file filter that accepts the given file type.
     * Example: new ExtensionFileFilter("jpg", "JPEG Image Images");
     *
     * Note that the "." before the extension is not needed. If
     * provided, it will be ignored.
     *
     * @see #addExtension(String)
     */
    public ExtensionFileFilter(String extension, String description) {
        this(new String[] { extension }, description);
    }

    /**
     * Creates a file filter from the given string array.
     * Example: new ExtensionFileFilter(String {"gif", "jpg"});
     *
     * Note that the "." before the extension is not needed and
     * will be ignored.
     *
     * @see #addExtension(String)
     */
    public ExtensionFileFilter(String[] filters) {
        this(filters, null);
    }

    /**
     * Creates a file filter from the given string array and description.
     * Example: new ExtensionFileFilter(String {"gif", "jpg"}, "Gif and JPG Images");
     *
     * Note that the "." before the extension is not needed and will be
     * ignored.
     *
     * @see #addExtension(String)
     */
    public ExtensionFileFilter(String[] filters, String description) {
        this.filters = new Hashtable(filters.length);

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
    public ExtensionFileFilter(List extensions) {
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
     * <p>This method is used by javax.swing.JFileChoosers,
     *
     * @see #accept(File, String)
     * @see #getExtension(File)
     * @see FileFilter#accept(File)
     */
    @Override
    public boolean accept(File f) {
        if (f != null) {
            if (f.isDirectory()) {
                return true;
            }

            String extension = getExtension(f);

            if (extension != null && filters.get(extension) != null) {
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
     * @see #accept(File)
     * @see #getExtension(File)
     * @see FileFilter#accept(File)
     */
    @Override
    public boolean accept(File directory, String name) {
        int i = name.lastIndexOf('.');

        String extension = "";
        if (i > 0 && i < name.length() - 1) {
            extension = name.substring(i + 1).toLowerCase(Locale.getDefault());
        }

        if (extension != null && filters.get(extension) != null) {
            return true;
        }
        return false;
    }

    /**
     * If the filter contains only one extension, return the extension
     * name.  Otherwise, return null.
     *
     * Added by Heloise Hse
     */
    public String getDefaultExtension() {
        if (filters.size() == 1) {
            return (String) filters.keys().nextElement();
        } else {
            return null;
        }
    }

    /**
     * Return the extension portion of the file's name .
     */
    public String getExtension(File f) {
        if (f != null) {
            String filename = f.getName();
            int i = filename.lastIndexOf('.');

            if (i > 0 && i < filename.length() - 1) {
                return filename.substring(i + 1).toLowerCase(
                        Locale.getDefault());
            }
        }
        return null;
    }

    /**
     * Adds a filetype "dot" extension to filter against.
     *
     * For example: the following code will create a filter that filters
     * out all files except those that end in ".jpg" and ".tif":
     *
     *   ExtensionFileFilter filter = new ExtensionFileFilter();
     *   filter.addExtension("jpg");
     *   filter.addExtension("tif");
     *
     * Note that the "." before the extension is not needed and will be ignored.
     */
    public void addExtension(String extension) {
        if (extension == null) {
            return;
        }

        if (filters == null) {
            filters = new Hashtable(5);
        }

        filters.put(extension.toLowerCase(Locale.getDefault()), this);
        fullDescription = null;
    }

    /**
     * Returns the human readable description of this filter. For
     * example: "JPEG and GIF Image Files (*.jpg, *.gif)"
     *
     * @see #setDescription(String)
     * @see #setExtensionListInDescription(boolean)
     * @see #isExtensionListInDescription()
     * @see FileFilter#getDescription()
     */
    @Override
    public String getDescription() {
        if (fullDescription == null) {
            if (description == null || isExtensionListInDescription()) {
                if (description != null) {
                    fullDescription = description;
                } else {
                    fullDescription = "";
                }

                //fullDescription += " (";

                StringBuffer result = new StringBuffer();
                //Iterator extensions = filters.values().iterator();
                Enumeration extensions = filters.keys();
                int extensionNumber = 1;
                int size = filters.size();

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
                fullDescription += result;

                // // build the description from the extension list
                // Enumeration extensions = filters.keys();

                // if (extensions != null) {
                //     fullDescription += ("." + (String) extensions.nextElement());

                //     while (extensions.hasMoreElements()) {
                //         fullDescription += (", " + (String) extensions
                //                 .nextElement());
                //     }
                // }

                //fullDescription += ")";
            } else {
                fullDescription = description;
            }
        }

        return fullDescription;
    }

    /**
     * Sets the human readable description of this filter. For
     * example: filter.setDescription("Gif and JPG Images");
     *
     * @see #setDescription(String)
     * @see #setExtensionListInDescription(boolean)
     * @see #isExtensionListInDescription()
     */
    public void setDescription(String description) {
        this.description = description;
        fullDescription = null;
    }

    /**
     * Determines whether the extension list (.jpg, .gif, etc) should
     * show up in the human readable description.
     *
     * Only relevant if a description was provided in the constructor
     * or using setDescription();
     *
     * @see #getDescription()
     * @see #isExtensionListInDescription()
     */
    public void setExtensionListInDescription(boolean b) {
        useExtensionsInDescription = b;
        fullDescription = null;
    }

    /**
     * Returns whether the extension list (.jpg, .gif, etc) should
     * show up in the human readable description.
     *
     * Only relevant if a description was provided in the constructor
     * or using setDescription();
     *
     * @see #getDescription()
     */
    public boolean isExtensionListInDescription() {
        return useExtensionsInDescription;
    }

    /**
     * Return a string description of this filter.
     *
     * @see #getDescription()
     */
    @Override
    public String toString() {
        return getDescription();
    }
}
