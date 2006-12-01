package ptolemy.actor.gui.test;

import java.net.URL;
import java.util.Vector;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;


/** This test (from Rome AFRL) was causing a stack trace.
 */
public class RunMultipleModels {
    public static void main(String[] args) throws Exception {
        for(int i = 0; i < 25; i++) {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    try {
                        String threadName = Thread.currentThread().getName();
                        URL url = RunMultipleModels.class.getResource("RunMultipleModelsModel.xml");

                        Workspace workspace = new Workspace("Workspace"
                                + threadName);
                        MoMLParser parser = new MoMLParser(workspace);
                        NamedObj model = parser.parse(null, url);
                        
                        Manager manager = new Manager(workspace, "Manager"
                                + threadName);
                        ((CompositeActor)model).setManager(manager);
                        manager.execute();
                    } catch (Exception ex) {
                        //System.err.println("Thread "
                        //        + Thread.currentThread().getName());
                        ex.printStackTrace();
                    }
                }
            });

            System.out.println("Starting " + thread.getName());
            thread.start();
        }
    }
}
