package ptolemy.uidesigner;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class UIDesigner extends JFrame {

    private JPanel contentPane;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIDesigner frame = new UIDesigner();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the frame.
     */
    public UIDesigner() {
        setTitle("UI Designer");
        setPreferredSize(new Dimension(800, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        
        JPanel pnlWest = new JPanel();
        pnlWest.setPreferredSize(new Dimension(150, 10));
        contentPane.add(pnlWest, BorderLayout.WEST);
        pnlWest.setLayout(new BorderLayout(0, 0));
        
        JPanel pnlNamedObjectTree = new JPanel();
        pnlNamedObjectTree.setBorder(new TitledBorder(null, "Named Object Tree", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        pnlWest.add(pnlNamedObjectTree, BorderLayout.CENTER);
        
        JPanel pnlEast = new JPanel();
        pnlEast.setPreferredSize(new Dimension(150, 10));
        contentPane.add(pnlEast, BorderLayout.EAST);
        pnlEast.setLayout(new BorderLayout(0, 0));
        
        JPanel pnlModelImage = new JPanel();
        pnlModelImage.setBorder(new TitledBorder(null, "Graph Preview", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        pnlModelImage.setPreferredSize(new Dimension(10, 150));
        pnlEast.add(pnlModelImage, BorderLayout.NORTH);
        
        JPanel pnlRemoteObjects = new JPanel();
        pnlRemoteObjects.setBorder(new TitledBorder(null, "Remote Named Objects", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        pnlEast.add(pnlRemoteObjects, BorderLayout.CENTER);
        
        JScrollPane spnScreen = new JScrollPane();
        contentPane.add(spnScreen, BorderLayout.CENTER);
        
        JTabbedPane tblScreen = new JTabbedPane(JTabbedPane.TOP);
        tblScreen.setBorder(new TitledBorder(null, "User Interface Layout", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        spnScreen.setViewportView(tblScreen);
        
        JPanel pnlTab = new JPanel();
        tblScreen.addTab("New tab", null, pnlTab, null);
    }

}
