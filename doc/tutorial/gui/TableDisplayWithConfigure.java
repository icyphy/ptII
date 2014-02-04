/*
Below is the copyright agreement for the Ptolemy II system.
Version: $Id$

Copyright (c) 2007-2009 The Regents of the University of California.
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
*/
package doc.tutorial.gui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import ptolemy.actor.gui.EditorPaneFactory;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

public class TableDisplayWithConfigure extends TableDisplay {
    public TableDisplayWithConfigure(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        new Factory(this, "_factory");
    }

    public class Factory extends EditorPaneFactory {
        public Factory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        public Component createEditorPane() {
            if (_table == null) {
                @SuppressWarnings("serial")
				TableModel dataModel = new AbstractTableModel() {
                    public int getColumnCount() {
                        return 10;
                    }

                    public int getRowCount() {
                        return 10;
                    }

                    public Object getValueAt(int row, int col) {
                        return Integer.valueOf(row * col);
                    }
                };
                _table = new JTable(dataModel);
            }
            return _table;
        }
    }
}
