package ptolemy.actor.gui.test;

import java.net.URL;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;


public class RunMultipleModels {
    public static void main(String[] args) {
        for(int i = 0; i < 25; i++) {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    URL url = RunMultipleModels.class.getResource("RunMultipleModelsModel.xml");
                    try {
                        Workspace workspace = new Workspace("Workspace" + System.currentTimeMillis());
                        MoMLParser parser = new MoMLParser(workspace);
                        NamedObj model = parser.parse(null, url);

                        Manager manager = new Manager(workspace, "Manager" + System.currentTimeMillis());
                        ((CompositeActor)model).setManager(manager);
                        manager.execute();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            System.out.println("Starting Thread:" + i);
            thread.start();
        }
    }
}
