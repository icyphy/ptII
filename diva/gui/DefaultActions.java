/*
  Copyright (c) 1998-2005 The Regents of the University of California
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
  PROVIDED HEREUNDER IS ON AN  BASIS, AND THE UNIVERSITY OF
  CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
  ENHANCEMENTS, OR MODIFICATIONS.

  PT_COPYRIGHT_VERSION_2
  COPYRIGHTENDKEY
*/
package diva.gui;

import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;


/**
 * A collection of static methods that
 * create useful default actions.
 *
 * @author John Reekie
 * @version $Id$
 */
public class DefaultActions {
    public final static String COPY = "Copy";
    public final static String CLOSE = "Close";
    public final static String CUT = "Cut";
    public final static String EXIT = "Exit";
    public final static String NEW = "New";
    public final static String OPEN = "Open";
    public final static String PASTE = "Paste";
    public final static String PRINT = "Print";
    public final static String QUIT = "Quit";
    public final static String SAVE = "Save";
    public final static String SAVE_AS = "Save As";

    /** Create an action named "Copy" that copies the current selection from
     * the current document and places it into the application's clipboard.
     * If there is no current document, or the application does not have a
     * clipboard, do nothing.
     */
    public static Action copyAction(final Application app) {
        return new AbstractAction(COPY) {
                public void actionPerformed(ActionEvent e) {
                    View view = app.getCurrentView();
                    Clipboard c = app.getClipboard();

                    if ((view != null) && (c != null)) {
                        view.copy(c);
                    }
                }
            };
    }

    /** Create an action named "Close" that closes the current
     * document. If there is no current document, do nothing.
     */
    public static Action closeAction(final Application app) {
        return new AbstractAction(CLOSE) {
                public void actionPerformed(ActionEvent e) {
                    app.closeDocument(app.getCurrentView().getDocument());
                }
            };
    }

    /** Create an action named "Cut" that cuts the current selection from
     * the current document and places it into the application's clipboard.
     * If there is no current document, or the application does not have a
     * clipboard, do nothing.
     */
    public static Action cutAction(final Application app) {
        return new AbstractAction(CUT) {
                public void actionPerformed(ActionEvent e) {
                    View view = app.getCurrentView();
                    Clipboard c = app.getClipboard();

                    if ((view != null) && (c != null)) {
                        view.cut(c);
                    }
                }
            };
    }

    /** Create an action named "Exit" that tries to close all the
     * open documents, and if all of them are closed successfully,
     * then exits Java.  As soon as one document is not successfully closed,
     * the action is cancelled.
     */
    public static Action exitAction(final Application app) {
        return new AbstractAction(EXIT) {
                public void actionPerformed(ActionEvent e) {
                    Iterator docs = app.documentList().iterator();
                    boolean succeeded = true;

                    while (docs.hasNext() && succeeded) {
                        Document d = (Document) docs.next();
                        succeeded &= app.closeDocument(d);
                    }

                    if (succeeded) {
                        System.exit(0);
                    }
                }
            };
    }

    /** Create an action named "New" that creates a new
     * document.
     */
    public static Action newAction(final Application app) {
        return new AbstractAction(NEW) {
                public void actionPerformed(ActionEvent e) {
                    Document doc = app.getDocumentFactory().createDocument(app);
                    app.addDocument(doc);

                    View v = app.createView(doc);
                    app.addView(v);
                    app.setCurrentView(v);
                }
            };
    }

    /** Create an action named "Open" that opens a new
     * document.
     */
    public static Action openAction(final Application app) {
        return new AbstractAction(OPEN) {
                public void actionPerformed(ActionEvent e) {
                    Document doc = app.getStoragePolicy().open(app);

                    //FIXME: should this check be done in the app instead?
                    if (doc != null) {
                        app.addDocument(doc);

                        View v = app.createView(doc);
                        app.addView(v);
                        app.setCurrentView(v);
                    }
                }
            };
    }

    /** Create an action named "Print" that prints the current
     * document to the printer, if it implements the Printable or
     * Pageable interface.  If there is no current document, or the
     * Document is not printable/pageable do nothing.
     */
    public static Action printAction(final Application app) {
        return new AbstractAction(PRINT) {
                public void actionPerformed(ActionEvent e) {
                    View view = app.getCurrentView();

                    if ((view != null) && view instanceof Printable) {
                        PrinterJob job = PrinterJob.getPrinterJob();
                        PageFormat format = job.pageDialog(job.defaultPage());
                        job.setPrintable((Printable) view, format);

                        if (job.printDialog()) {
                            try {
                                job.print();
                            } catch (Exception ex) {
                                app.showError("PrintingFailed", ex);
                            }
                        }
                    } else if ((view != null) && view instanceof Pageable) {
                        PrinterJob job = PrinterJob.getPrinterJob();
                        PageFormat format = job.pageDialog(job.defaultPage());
                        job.setPageable((Pageable) view);

                        if (job.printDialog()) {
                            try {
                                job.print();
                            } catch (Exception ex) {
                                app.showError("PrintingFailed", ex);
                            }
                        }
                    }
                }
            };
    }

    /** Create an action named "Paste" that pastes the current selection from
     * the current document and places it into the application's clipboard.
     * If there is no current document, or the application does not have a
     * clipboard, do nothing.
     */
    public static Action pasteAction(final Application app) {
        return new AbstractAction(PASTE) {
                public void actionPerformed(ActionEvent e) {
                    View view = app.getCurrentView();
                    Clipboard c = app.getClipboard();

                    if ((view != null) && (c != null)) {
                        view.paste(c);
                    }
                }
            };
    }

    /** Create an action named "Quit" that exits Java.
     *
     * @deprecated The standard windows term is "Exit," so use exitAction()
     */
    public static Action quitAction(final Application app) {
        return exitAction(app);
    }

    /** Create an action named "Save" that saves the current
     * document. If there is no current document, do nothing.
     */
    public static Action saveAction(final Application app) {
        return new AbstractAction(SAVE) {
                public void actionPerformed(ActionEvent e) {
                    app.getStoragePolicy().save(app.getCurrentView()
                                                               .getDocument()); //FIXME???
                }
            };
    }

    /** Create an action named "Save As" that saves the current
     * document to a different location. If there is no current
     * document, do nothing.
     */
    public static Action saveAsAction(final Application app) {
        return new AbstractAction(SAVE_AS) {
                public void actionPerformed(ActionEvent e) {
                    app.getStoragePolicy().saveAs(app.getCurrentView()
                                                                 .getDocument());
                }
            };
    }
}
