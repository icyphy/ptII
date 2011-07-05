package ptolemy.homer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.TreeModel;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.injection.PtolemyInjector;
import ptolemy.actor.injection.PtolemyModule;
import ptolemy.homer.tree.AttributeTreeModel;
import ptolemy.homer.tree.NamedObjectTree;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;

public class UIDesigner extends JFrame {

    private final JPanel contentPane;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    PtolemyModule module = new PtolemyModule(ResourceBundle
                            .getBundle("ptolemy.actor.JavaSEActorModule"));
                    PtolemyInjector.createInjector(module);
                    MoMLParser parser = new MoMLParser(new Workspace());
                    parser.resetAll();

                    MoMLParser.setMoMLFilters(BackwardCompatibility
                            .allFilters());

                    //enter the address of any model xml file
                    CompositeActor topLevelActor = (CompositeActor) parser
                            .parseFile("C:/Users/Ishwinder/Desktop/Studio/Release 1/addermodel.xml");

                    //if FullTreeModel is used it gives the complete model tree
                    // but due to some reason scroll bars does not appear  
                    //EntityTreeModel
                    UIDesigner frame = new UIDesigner(new AttributeTreeModel(
                            topLevelActor));
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the frame.
     * @param fullTreeModel 
     */
    public UIDesigner(TreeModel fullTreeModel) {
        setTitle("UI Designer");
        setPreferredSize(new Dimension(800, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 950, 600);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JPanel pnlWest = new JPanel();
        pnlWest.setPreferredSize(new Dimension(150, 60));
        contentPane.add(new JScrollPane(pnlWest), BorderLayout.WEST);
        pnlWest.setLayout(new BorderLayout(0, 0));

        NamedObjectTree pnlNamedObjectTree = new NamedObjectTree(fullTreeModel);
        pnlNamedObjectTree.setBorder(new TitledBorder(null,
                "Named Object Tree", TitledBorder.LEADING, TitledBorder.TOP,
                null, null));
        pnlWest.add(pnlNamedObjectTree, BorderLayout.CENTER);

        JPanel pnlEast = new JPanel();
        pnlEast.setPreferredSize(new Dimension(150, 10));
        contentPane.add(pnlEast, BorderLayout.EAST);
        pnlEast.setLayout(new BorderLayout(0, 0));

        JPanel pnlModelImage = new JPanel();
        pnlModelImage.setBorder(new TitledBorder(null, "Graph Preview",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        pnlModelImage.setPreferredSize(new Dimension(10, 150));
        pnlEast.add(pnlModelImage, BorderLayout.NORTH);

        JPanel pnlRemoteObjects = new JPanel();
        pnlRemoteObjects.setBorder(new TitledBorder(null,
                "Remote Named Objects", TitledBorder.LEADING, TitledBorder.TOP,
                null, null));
        pnlEast.add(pnlRemoteObjects, BorderLayout.CENTER);

        JScrollPane spnScreen = new JScrollPane();
        contentPane.add(spnScreen, BorderLayout.CENTER);

        TabbedLayoutScene tblScreen = new TabbedLayoutScene(JTabbedPane.TOP);

        spnScreen.setViewportView(tblScreen);

    }

}
