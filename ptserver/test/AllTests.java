package ptserver.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ptserver.test.junit.ServletTest;
import ptserver.test.junit.TokenParserTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({ TokenParserTest.class, ServletTest.class })
public class AllTests {

}
