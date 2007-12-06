package doc.tutorial;

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
