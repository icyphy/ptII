/* Top-level window containing a simple text editor or viewer.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.actor.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import ptolemy.actor.injection.PortablePlaceable;
import ptolemy.gui.ExtensionFilenameFilter;
import ptolemy.gui.ImageExportable;
import ptolemy.gui.JFileChooserBugFix;
import ptolemy.gui.UndoListener;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// TextEditor

/**

 A top-level window containing a simple text editor or viewer.
 You can access the public member text to set the text, get the text,
 or set the number of rows or columns.
 After creating this, it is necessary to call show() for it to appear.

 @author Edward A. Lee, contributors: Christopher Brooks, Ben Leinfelder
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
@SuppressWarnings("serial")
public class TextEditor extends TableauFrame implements DocumentListener,
ImageExportable, Printable {
    /** Construct an empty text editor with no name.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     */
    public TextEditor() {
        this("Unnamed");
    }

    /** Construct an empty text editor with the specified title.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  @param title The title to put in the title bar.
     */
    public TextEditor(String title) {
        this(title, null);
    }

    /** Construct an empty text editor with the specified title and
     *  document.  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  @param title The title to put in the title bar.
     *  @param document The document containing text, or null if none.
     */
    public TextEditor(String title, Document document) {
        this(title, document, (Placeable) null);
    }

    /** Construct an empty text editor with the specified title and
     *  document and associated placeable.  After constructing this,
     *  it is necessary to call setVisible(true) to make the frame
     *  appear.
     *  @param title The title to put in the title bar.
     *  @param document The document containing text, or null if none.
     *  @param placeable The associated placeable.
     */
    public TextEditor(String title, Document document, Placeable placeable) {
        // NOTE: Create with no status bar, since we have no use for it now.
        super(null, null, placeable);
        _init(title, document);
    }

    /** Construct an empty text editor with the specified title and
     *  document and associated poratalbeplaceable.  After constructing this,
     *  it is necessary to call setVisible(true) to make the frame
     *  appear.
     *  @param title The title to put in the title bar.
     *  @param document The document containing text, or null if none.
     *  @param portablePlaceable The associated PortablePlaceable.
     */
    public TextEditor(String title, Document document,
            PortablePlaceable portablePlaceable) {
        // NOTE: Create with no status bar, since we have no use for it now.
        super(null, null, portablePlaceable);
        _init(title, document);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The text area. */
    public JTextArea text;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to notification that an attribute or set of attributes
     *  changed.
     */
    @Override
    public void changedUpdate(DocumentEvent e) {
        // Do nothing... We don't care about attributes.
    }

    /** Get the background color.
     *  @return The background color of the scroll pane.
     *  If _scrollPane is null, then null is returned.
     *  @see #setBackground(Color)
     */
    @Override
    public Color getBackground() {
        // Under Java 1.7 on the Mac, the _scrollbar is sometimes null.
        // See http://bugzilla.ecoinformatics.org/show_bug.cgi?id=5574
        if (_scrollPane != null) {
            return _scrollPane.getBackground();
        } else {
            return null;
        }
    }

    /** Return the scroll pane, if there is one, and null if not.
     *  @return The scroll pane.
     */
    public JScrollPane getScrollPane() {
        return _scrollPane;
    }

    // CONTRIBUTED CODE.  The exportImage() methods are from PlotBox,
    // which says:

    // I wanted the ability to use the Plot object in a servlet and to
    // write out the resultant images. The following routines,
    // particularly exportImage(), permit this. I also had to make some
    // minor changes elsewhere. Rob Kroeger, May 2001.

    // NOTE: This code has been modified by EAL to conform with Ptolemy II
    // coding style.

    /** Create a BufferedImage and draw this plot to it.
     *  The size of the returned image matches the current size of the plot.
     *  This method can be used, for
     *  example, by a servlet to produce an image, rather than
     *  requiring an applet to instantiate a PlotBox.
     *  @return An image filled by the plot.
     */
    public synchronized BufferedImage exportImage() {
        Dimension dimension = getSize();
        Rectangle rectangle = new Rectangle(dimension.height, dimension.width);
        return exportImage(new BufferedImage(rectangle.width, rectangle.height,
                BufferedImage.TYPE_INT_ARGB), rectangle,
                _defaultImageRenderingHints(), false);
    }

    /** Draw this plot onto the specified image at the position of the
     *  specified rectangle with the size of the specified rectangle.
     *  The plot is rendered using anti-aliasing.
     *  This can be used to paint a number of different
     *  plots onto a single buffered image.  This method can be used, for
     *  example, by a servlet to produce an image, rather than
     *  requiring an applet to instantiate a PlotBox.
     *  @param bufferedImage Image onto which the plot is drawn.
     *  @param rectangle The size and position of the plot in the image.
     *  @param hints Rendering hints for this plot.
     *  @param transparent Indicator that the background of the plot
     *   should not be painted.
     *  @return The modified bufferedImage.
     */
    public synchronized BufferedImage exportImage(BufferedImage bufferedImage,
            Rectangle rectangle, RenderingHints hints, boolean transparent) {
        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.addRenderingHints(_defaultImageRenderingHints());

        if (!transparent) {
            graphics.setColor(Color.white); // set the background color
            graphics.fill(rectangle);
        }

        print(graphics, rectangle);
        return bufferedImage;
    }

    /** Export an image of the plot in the specified format.
     *  If the specified format is not supported, then pop up a message
     *  window apologizing.
     *  @param out An output stream to which to send the description.
     *  @param formatName A format name, such as "gif" or "png".
     */
    public synchronized void exportImage(OutputStream out, String formatName) {
        try {
            boolean match = false;
            String[] supportedFormats = ImageIO.getWriterFormatNames();
            for (String supportedFormat : supportedFormats) {
                if (formatName.equalsIgnoreCase(supportedFormat)) {
                    match = true;
                    break;
                }
            }
            if (!match) {
                // This exception is caught and reported below.
                throw new Exception("Format " + formatName + " not supported.");
            }
            BufferedImage image = exportImage();
            if (out == null) {
                // FIXME: Write image to the clipboard.
                // final Clipboard clipboard = getToolkit().getSystemClipboard();
                String message = "Copy to the clipboard is not implemented yet.";
                JOptionPane.showMessageDialog(this, message,
                        "Ptolemy Plot Message", JOptionPane.ERROR_MESSAGE);
                return;
            }
            ImageIO.write(image, formatName, out);
        } catch (Exception ex) {
            String message = "Export failed: " + ex.getMessage();
            JOptionPane.showMessageDialog(this, message,
                    "Ptolemy Plot Message", JOptionPane.ERROR_MESSAGE);

            // Rethrow the exception so that we don't report success,
            // and so the stack trace is displayed on standard out.
            throw (RuntimeException) ex.fillInStackTrace();
        }
    }

    /** React to notification that there was an insert into the document.
     */
    @Override
    public void insertUpdate(DocumentEvent e) {
        setModified(true);
    }

    /** Print the text to a printer, which is represented by the
     *  specified graphics object.
     *  @param graphics The context into which the page is drawn.
     *  @param format The size and orientation of the page being drawn.
     *  @param index The zero based index of the page to be drawn.
     *  @return PAGE_EXISTS if the page is rendered successfully, or
     *   NO_SUCH_PAGE if pageIndex specifies a non-existent page.
     *  @exception PrinterException If the print job is terminated.
     */
    @Override
    public int print(Graphics graphics, PageFormat format, int index)
            throws PrinterException {
        if (graphics == null) {
            return Printable.NO_SUCH_PAGE;
        }

        Graphics2D graphics2D = (Graphics2D) graphics;

        double bottomMargin = format.getHeight() - format.getImageableHeight()
                - format.getImageableY();

        double lineHeight = graphics2D.getFontMetrics().getHeight()
                - graphics2D.getFontMetrics().getLeading() / 2;

        int linesPerPage = (int) Math.floor((format.getHeight()
                - format.getImageableY() - bottomMargin)
                / lineHeight);

        int lineYPosition = (int) Math
                .ceil(format.getImageableY() + lineHeight);

        return _print(graphics2D, index, linesPerPage, lineHeight,
                (int) format.getImageableX(), lineYPosition, format.getHeight()
                - bottomMargin);

    }

    /** Print the text to a printer, which is represented by the
     *  specified graphics object.
     *  @param graphics The context into which the page is drawn.
     *  @param drawRect specification of the size.
     *  @return PAGE_EXISTS if the page is rendered successfully, or
     *   NO_SUCH_PAGE if pageIndex specifies a non-existent page.
     */
    public int print(Graphics graphics, Rectangle drawRect) {
        if (graphics == null) {
            return Printable.NO_SUCH_PAGE;
        }

        graphics.setPaintMode();

        Graphics2D graphics2D = (Graphics2D) graphics;

        // Loosely based on
        // http://forum.java.sun.com/thread.jspa?threadID=217823&messageID=2361189
        // Found it unwise to use the TextArea font's size,
        // We area just printing text so use a a font size that will
        // be generally useful.
        graphics2D.setFont(getFont().deriveFont(9.0f));

        // FIXME: we should probably get the color somehow.  Probably exportImage() is being
        // called with transparent set to false?
        graphics2D.setColor(java.awt.Color.BLACK);

        // FIXME: Magic Number 5, similar to what is in PlotBox.
        double bottomMargin = 5;

        double lineHeight = graphics2D.getFontMetrics().getHeight()
                - graphics2D.getFontMetrics().getLeading() / 2;

        int linesPerPage = (int) Math.floor((drawRect.height - bottomMargin)
                / lineHeight);

        int lineYPosition = (int) Math.ceil(lineHeight);

        return _print(graphics2D, 0 /* page */, linesPerPage, lineHeight, 0,
                lineYPosition, drawRect.height - bottomMargin);
    }

    /** React to notification that there was a removal from the document.
     */
    @Override
    public void removeUpdate(DocumentEvent e) {
        setModified(true);
    }

    /** Scroll as necessary so that the last line is visible.
     */
    public void scrollToEnd() {
        // Song and dance to scroll to the new line.
        text.scrollRectToVisible(new Rectangle(new Point(0, text.getHeight())));
    }

    /** Set background color.  This overrides the base class to set the
     *  background of contained scroll pane and text area.
     *  @param background The background color.
     *  @see #getBackground()
     */
    @Override
    public void setBackground(Color background) {
        super.setBackground(background);

        // This seems to be called in a base class constructor, before
        // this variable has been set. Hence the test against null.
        if (_scrollPane != null) {
            _scrollPane.setBackground(background);
        }

        if (text != null) {
            // NOTE: Should the background always be white?
            text.setBackground(background);
        }
    }

    /** Dispose of this frame.
     *     Override this dispose() method to unattach any listeners that may keep
     *  this model from getting garbage collected.  This method invokes the
     *  dispose() method of the superclass,
     *  {@link ptolemy.actor.gui.TableauFrame}.
     */
    @Override
    public void dispose() {
        if (_debugClosing) {
            System.out.println("TextEditor.dispose() : " + this.getName());
        }

        super.dispose();
    }

    /** Write an image to the specified output stream in the specified
     *  format.  Supported formats include at least "gif" and "png",
     *  standard image file formats.  The image is a rendition of the
     *  current view of the model.
     *  @param stream The output stream to write to.
     *  @param format The image format to generate.
     *  @exception IOException If writing to the stream fails.
     *  @exception PrinterException  If the specified format is not supported.
     */
    @Override
    public void writeImage(OutputStream stream, String format)
            throws PrinterException, IOException {
        exportImage(stream, format);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Clear the current contents.  First, check to see whether
     *  the contents have been modified, and if so, then prompt the user
     *  to save them.  A return value of false
     *  indicates that the user has canceled the action.
     *  @return False if the user cancels the clear.
     */
    @Override
    protected boolean _clear() {
        if (super._clear()) {
            text.setText("");
            return true;
        } else {
            return false;
        }
    }

    /** Create the items in the File menu's Export section
     *  This method adds a menu items to export images of the plot
     *  in GIF, PNG, and possibly PDF.
     *  @return The items in the File menu.
     */
    @Override
    protected JMenuItem[] _createFileMenuItems() {
        // This method is similar to ptolemy/actor/gui/PlotTableauFrame.java, but we don't
        // handle pdfs.

        JMenuItem[] fileMenuItems = super._createFileMenuItems();

        JMenu exportMenu = (JMenu) fileMenuItems[_EXPORT_MENU_INDEX];
        exportMenu.setEnabled(true);

        // Next do the export GIF action.
        if (_exportGIFAction == null) {
            _exportGIFAction = new ExportImageAction("GIF");
        }
        JMenuItem exportItem = new JMenuItem(_exportGIFAction);
        exportMenu.add(exportItem);

        // Next do the export PNG action.
        if (_exportPNGAction == null) {
            _exportPNGAction = new ExportImageAction("PNG");
        }
        exportItem = new JMenuItem(_exportPNGAction);
        exportMenu.add(exportItem);

        return fileMenuItems;
    }

    /** Display more detailed information than given by _about().
     */
    @Override
    protected void _help() {
        // FIXME: Give instructions for the editor here.
        _about();
    }

    /** Initializes an empty text editor with the specified title and
     *  document and associated placeable.  After constructing this,
     *  it is necessary to call setVisible(true) to make the frame
     *  appear.
     *
     *  @param title The title to put in the title bar.
     *  @param document The document containing text.
     */
    protected void _init(String title, Document document) {
        setTitle(title);

        text = new JTextArea(document);

        // Since the document may have been null, request it...
        document = text.getDocument();
        document.addDocumentListener(this);
        _scrollPane = new JScrollPane(text);

        getContentPane().add(_scrollPane, BorderLayout.CENTER);
        _initialSaveAsFileName = "data.txt";

        // Set the undo listener, with default key mappings.
        text.getDocument().addUndoableEditListener(new UndoListener(text));
    }

    /** Query the user for a filename, save the model to that file,
     *  and open a new window to view the model.
     *  This overrides the base class to use the ".txt" extension.
     *  @return True if the save succeeds.
     */
    @Override
    protected boolean _saveAs() {
        return _saveAs(".txt");
    }

    /** Print the contents.
     */
    @Override
    protected void _print() {
        // FIXME: What should we print?
        super._print();
    }

    // FIXME: Listen for window closing.

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The export to GIF action. */
    protected Action _exportGIFAction;

    /** The export to PNG action. */
    protected Action _exportPNGAction;

    /** The scroll pane containing the text area. */
    protected JScrollPane _scrollPane;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    ///////////////////////////////////////////////////////////////////
    //// ExportImageAction

    /** Export an image. */
    public class ExportImageAction extends AbstractAction {
        // FIXME: this is very similar to PlotTableaFrame.ExportImageAction.

        /** Create a new action to export an image.
         *  @param formatName The name of the format, currently PNG and
         *  GIF are supported.
         */
        public ExportImageAction(String formatName) {
            super("Export " + formatName);
            _formatName = formatName.toLowerCase(Locale.getDefault());
            putValue("tooltip", "Export " + formatName + " image to a file.");
            // putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_G));
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                   ////

        /** Export an image.
         *  @param e The ActionEvent that invoked this action.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooserBugFix jFileChooserBugFix = new JFileChooserBugFix();
            Color background = null;
            try {
                background = jFileChooserBugFix.saveBackground();

                JFileChooser fileDialog = new JFileChooser();
                fileDialog.setDialogTitle("Specify a file to write to.");
                LinkedList extensions = new LinkedList();
                extensions.add(_formatName);
                fileDialog.addChoosableFileFilter(new ExtensionFilenameFilter(
                        extensions));

                if (_directory != null) {
                    fileDialog.setCurrentDirectory(_directory);
                } else {
                    // The default on Windows is to open at user.home, which is
                    // typically an absurd directory inside the O/S installation.
                    // So we use the current directory instead.
                    // This will throw a security exception in an applet.
                    // FIXME: we should support users under applets opening files
                    // on the server.
                    String currentWorkingDirectory = StringUtilities
                            .getProperty("user.dir");
                    if (currentWorkingDirectory != null) {
                        fileDialog.setCurrentDirectory(new File(
                                currentWorkingDirectory));
                    }
                }

                // Here, we differ from PlotTableauFrame:
                int returnVal = fileDialog.showDialog(
                        TextEditor.this,
                        "Export "
                                + _formatName.toUpperCase(Locale.getDefault()));

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    _directory = fileDialog.getCurrentDirectory();
                    File file = fileDialog.getSelectedFile().getCanonicalFile();

                    if (file.getName().indexOf(".") == -1) {
                        // If the user has not given the file an extension, add it
                        file = new File(file.getAbsolutePath() + "."
                                + _formatName);
                    }
                    if (file.exists()) {
                        if (!MessageHandler.yesNoQuestion("Overwrite "
                                + file.getName() + "?")) {
                            return;
                        }
                    }
                    OutputStream out = null;
                    try {
                        out = new FileOutputStream(file);
                        exportImage(out, _formatName);
                    } finally {
                        if (out != null) {
                            out.close();
                        }
                    }

                    // Open the PNG file.
                    // FIXME: We don't do the right thing with PNG files.
                    // It just opens in a text editor.
                    // _read(file.toURI().toURL());
                    MessageHandler.message("Image file exported to "
                            + file.getName());
                }
            } catch (Exception ex) {
                MessageHandler.error(
                        "Export to "
                                + _formatName.toUpperCase(Locale.getDefault())
                                + " failed", ex);
            } finally {
                jFileChooserBugFix.restoreBackground(background);
            }
        }

        private String _formatName;
    }

    /** Return a default set of rendering hints for image export, which
     *  specifies the use of anti-aliasing.
     */
    private RenderingHints _defaultImageRenderingHints() {
        // From PlotBox
        RenderingHints hints = new RenderingHints(null);
        hints.put(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        return hints;
    }

    /** Print the contents of the editor to a Graphics.
     *  This used both by the print facility and the exportImage facility.
     *  @param graphics2D The context into which the page is drawn.
     *  @return PAGE_EXISTS if the page is rendered successfully, or
     *   NO_SUCH_PAGE if pageIndex specifies a non-existent page.
     */
    private int _print(Graphics2D graphics2D, int index, int linesPerPage,
            double lineHeight, int lineXPosition, int linePosition,
            double bottomLinePosition) {

        int startLine = linesPerPage * index;

        if (startLine > text.getLineCount()) {
            return NO_SUCH_PAGE;
        }

        int endLine = startLine + linesPerPage;
        for (int line = startLine; line < endLine; line++) {
            try {
                String linetext = text.getText(
                        text.getLineStartOffset(line),
                        text.getLineEndOffset(line)
                        - text.getLineStartOffset(line));
                graphics2D.drawString(linetext, lineXPosition, linePosition);
            } catch (BadLocationException e) {
                // Ignore. Never a bad location.
            }

            linePosition += lineHeight;
            if (linePosition > bottomLinePosition) {
                break;
            }
        }
        return PAGE_EXISTS;
    }

}
