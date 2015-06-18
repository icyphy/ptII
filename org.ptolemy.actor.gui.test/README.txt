To run the unit tests, especially org.ptolemy.core.test.ModelDefinitionTest.testModelDefinitionWithCustomActorFromMOMLWithVersion,
you need to include org.ptolemy.core.test.actor on the classpath. In a plain JUnit test run, 
the MoMLParser uses the default Java class loading via a SimpleClassLoadingStrategy instance.

Similarly, when running the tests on an OSGi runtime, the bundle org.ptolemy.core.test.actor must be included.
In such a configuration this actor bundle will register a ModelElementClassProvider for its MyConst actor 
on the OSGiClassLoadingStrategy used by the MoMLParser.