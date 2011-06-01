package ptserver.test.junit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.sun.corba.se.spi.activation.ServerManager;

@RunWith(Suite.class)
@Suite.SuiteClasses({ TokenParserTest.class, ServletTest.class,
        ServerManager.class })
public class AllTests {

}
