/**
 * This test generates a table of all available display modes, enters
 * full-screen mode, if available, and allows you to change the display mode.
 * The application should look fine under each enumerated display mode.
 * On UNIX, only a single display mode should be available, and on Win32,
 * display modes should depend on direct draw availability and the type
 * of graphics card.
 */
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;


/**
 * Table model for showing display mode information in the JTable.
 */
class DisplayModeModel extends DefaultTableModel {
    private DisplayMode[] modes;

    public DisplayModeModel(DisplayMode[] modes) {
        this.modes = modes;
    }

    public DisplayMode getDisplayMode(int r) {
        return modes[r];
    }

    public String getColumnName(int c) {
        return DisplayModeTest.COLUMN_NAMES[c];
    }

    public int getColumnCount() {
        return DisplayModeTest.COLUMN_WIDTHS.length;
    }

    public boolean isCellEditable(int r, int c) {
        return false;
    }

    public int getRowCount() {
        if (modes == null) {
            return 0;
        }

        return modes.length;
    }

    public Object getValueAt(int rowIndex, int colIndex) {
        DisplayMode dm = modes[rowIndex];

        switch (colIndex) {
        case DisplayModeTest.INDEX_WIDTH:
            return Integer.toString(dm.getWidth());

        case DisplayModeTest.INDEX_HEIGHT:
            return Integer.toString(dm.getHeight());

        case DisplayModeTest.INDEX_BITDEPTH: {
            int bitDepth = dm.getBitDepth();
            String ret;

            if (bitDepth == DisplayMode.BIT_DEPTH_MULTI) {
                ret = "Multi";
            } else {
                ret = Integer.toString(bitDepth);
            }

            return ret;
        }

        case DisplayModeTest.INDEX_REFRESHRATE: {
            int refreshRate = dm.getRefreshRate();
            String ret;

            if (refreshRate == DisplayMode.REFRESH_RATE_UNKNOWN) {
                ret = "Unknown";
            } else {
                ret = Integer.toString(refreshRate);
            }

            return ret;
        }
        }

        throw new ArrayIndexOutOfBoundsException("Invalid column value");
    }
}


/**
 * Main frame class
 */
public class DisplayModeTest extends JFrame implements ActionListener,
                                                       ListSelectionListener {
    private boolean waiting = false;
    private Object exitLock = new Object();
    private GraphicsDevice device;
    private DisplayMode originalDM;
    private JButton exit = new JButton("Exit");
    private JButton changeDM = new JButton("Set Display");
    private JLabel currentDM = new JLabel();
    private JTable dmList = new JTable();
    private JScrollPane dmPane = JTable.createScrollPaneForTable(dmList);
    private boolean isFullScreen = false;
    public static final int INDEX_WIDTH = 0;
    public static final int INDEX_HEIGHT = 1;
    public static final int INDEX_BITDEPTH = 2;
    public static final int INDEX_REFRESHRATE = 3;
    public static final int[] COLUMN_WIDTHS = new int[] {
        100,
        100,
        100,
        100
    };
    public static final String[] COLUMN_NAMES = new String[] {
        "Width",
        "Height",
        "Bit Depth",
        "Refresh Rate"
    };

    public DisplayModeTest(GraphicsDevice device) {
        super(device.getDefaultConfiguration());
        this.device = device;
        setTitle("Display Mode Test");
        originalDM = device.getDisplayMode();
        setDMLabel(originalDM);
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent ev) {
                    restoreDisplayMode();
                }
            });

        // Make sure a DM is always selected in the list
        exit.addActionListener(this);
        changeDM.addActionListener(this);
        changeDM.setEnabled(device.isDisplayChangeSupported());
    }

    public void restoreDisplayMode() {
        if (waiting) {
            synchronized (exitLock) {
                exitLock.notifyAll();
            }

            waiting = false;
        } else {
            if (device.isDisplayChangeSupported()
                    && !originalDM.equals(device.getDisplayMode())) {
                device.setDisplayMode(originalDM);
            }
        }

        System.exit(0);
    }

    public void actionPerformed(ActionEvent ev) {
        Object source = ev.getSource();

        if (source == exit) {
            restoreDisplayMode();
        } else { // if (source == changeDM)

            int index = dmList.getSelectionModel().getAnchorSelectionIndex();

            if (index >= 0) {
                DisplayModeModel model = (DisplayModeModel) dmList.getModel();
                DisplayMode dm = model.getDisplayMode(index);
                device.setDisplayMode(dm);
                setDMLabel(dm);
                repaint();
            }
        }
    }

    public void valueChanged(ListSelectionEvent ev) {
        changeDM.setEnabled(device.isDisplayChangeSupported());
    }

    private void initComponents(Container c) {
        setContentPane(c);
        c.setLayout(new BorderLayout());

        // Current DM
        JPanel currentPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        c.add(currentPanel, BorderLayout.NORTH);

        JLabel current = new JLabel("Current Display Mode : ");
        currentPanel.add(current);
        currentPanel.add(currentDM);

        // Display Modes
        JPanel modesPanel = new JPanel(new GridLayout(1, 2));
        c.add(modesPanel, BorderLayout.CENTER);

        // List of display modes
        for (int i = 0; i < COLUMN_WIDTHS.length; i++) {
            TableColumn col = new TableColumn(i, COLUMN_WIDTHS[i]);
            col.setIdentifier(COLUMN_NAMES[i]);
            col.setHeaderValue(COLUMN_NAMES[i]);
            dmList.addColumn(col);
        }

        dmList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dmList.getSelectionModel().addListSelectionListener(this);
        modesPanel.add(dmPane);

        // Controls
        JPanel controlsPanelA = new JPanel(new BorderLayout());
        modesPanel.add(controlsPanelA);

        JPanel controlsPanelB = new JPanel(new GridLayout(2, 1));
        controlsPanelA.add(controlsPanelB, BorderLayout.NORTH);

        // Exit
        JPanel exitPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        controlsPanelB.add(exitPanel);
        exitPanel.add(exit);

        // Change DM
        JPanel changeDMPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        controlsPanelB.add(changeDMPanel);
        changeDMPanel.add(changeDM);
        controlsPanelA.add(new JPanel(), BorderLayout.CENTER);
    }

    public void waitForExit() {
        waiting = true;
        repaint();

        synchronized (exitLock) {
            try {
                exitLock.wait();
            } catch (InterruptedException e) {
            }
        }
    }

    public void show() {
        super.show();
        dmList.setModel(new DisplayModeModel(device.getDisplayModes()));
    }

    public void setDMLabel(DisplayMode newMode) {
        int bitDepth = newMode.getBitDepth();
        int refreshRate = newMode.getRefreshRate();
        String bd;
        String rr;

        if (bitDepth == DisplayMode.BIT_DEPTH_MULTI) {
            bd = "Multi";
        } else {
            bd = Integer.toString(bitDepth);
        }

        if (refreshRate == DisplayMode.REFRESH_RATE_UNKNOWN) {
            rr = "Unknown";
        } else {
            rr = Integer.toString(refreshRate);
        }

        currentDM.setText(COLUMN_NAMES[INDEX_WIDTH] + ": " + newMode.getWidth()
                + " " + COLUMN_NAMES[INDEX_HEIGHT] + ": " + newMode.getHeight()
                + " " + COLUMN_NAMES[INDEX_BITDEPTH] + ": " + bd + " "
                + COLUMN_NAMES[INDEX_REFRESHRATE] + ": " + rr);
    }

    public void begin() {
        isFullScreen = device.isFullScreenSupported();
        setUndecorated(isFullScreen);
        setResizable(!isFullScreen);

        if (isFullScreen) {
            // Full-screen mode
            try {
                device.setFullScreenWindow(this);
                waitForExit();
            } finally {
                device.setFullScreenWindow(null);
            }
        } else {
            // Windowed mode
            pack();
            setVisible(true);
        }
    }

    public static void main(String[] args) {
        GraphicsEnvironment env = GraphicsEnvironment
            .getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = env.getScreenDevices();

        for (int i = 0; i < devices.length; i++) {
            DisplayModeTest test = new DisplayModeTest(devices[i]);
            test.initComponents(test.getContentPane());
            test.begin();
        }
    }
}
