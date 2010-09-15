/**
 * 
 */
package doc.tutorial.graph.junit;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import doc.tutorial.graph.ShortestPathFinder;

/**
 * @author eal
 *
 */
public class ShortestPathFinderTest {
    
    public CompositeActor testModel;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        // Build test model.
        testModel = new CompositeActor();
        
        // Actor a
        AtomicActor a = new AtomicActor(testModel, "a");
        IOPort aOut = new IOPort(a, "aOut", false, true);
        
        // Actor b
        AtomicActor b = new AtomicActor(testModel, "b");
        IOPort bIn = new IOPort(b, "bIn", true, false);
        IOPort bOut = new IOPort(b, "bOut", false, true);
        
        // Actor c
        AtomicActor c = new AtomicActor(testModel, "c");
        IOPort cIn1 = new IOPort(c, "cIn1", true, false);
        IOPort cIn2 = new IOPort(c, "cIn2", true, false);

        // Connections
        ComponentRelation relation = testModel.connect(aOut, bIn);
        testModel.connect(bOut, cIn1);
        cIn2.link(relation);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link doc.tutorial.graph.ShortestPathFinder#calculateDistance(java.lang.Object[], int, int)}.
     */
    @Test
    public void testCalculateDistance() {
        List<Entity> entitiesList = testModel.entityList();
        Object[] entities = entitiesList.toArray();
        int result = ShortestPathFinder.calculateDistance(entities, 0, 2);
        Assert.assertEquals(1, result);
    }
}
