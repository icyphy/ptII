package ptserver.test.junit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({ TokenParserTest.class, ServletTest.class })
public class AllTests {

}
