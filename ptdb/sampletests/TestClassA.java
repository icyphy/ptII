package ptdb.sampletests;

/*import static org.junit.Assert.*;

import java.io.File; 

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;


import org.powermock.api.easymock.PowerMock; 
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ClassA.class)*/
public class TestClassA {

   
    /*@Test
    public void testCreateDirectoryStructure() throws Exception {
            final String path = "directoryPath";
            File fileMock = PowerMock.createMock(File.class);

            ClassA tested = new ClassA();

            PowerMock.expectNew(File.class, path).andReturn(fileMock);

            EasyMock.expect(fileMock.exists()).andReturn(false);
            EasyMock.expect(fileMock.mkdirs()).andReturn(true);

            PowerMock.replay(fileMock, File.class);

            assertTrue(tested.createDirectoryStructure(path));

            PowerMock.verify(fileMock, File.class);
    }
    
    @Test
    public void testGetSunSign() throws Exception
    {
        ClassB mockClassB = PowerMock.createMock(ClassB.class);
        ClassA classA = new ClassA();
        
        PowerMock.expectNew(ClassB.class).andReturn(mockClassB);
        EasyMock.expect(mockClassB.getMonth("Jan")).andReturn(1);
      
        PowerMock.replayAll();
        
        String output = classA.getSunSign(1, "Jan");
        assertEquals("Capricorn", output);
        PowerMock.verifyAll();
    }*/
}
