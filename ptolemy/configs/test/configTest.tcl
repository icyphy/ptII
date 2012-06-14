if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}
    set parser [java::new ptolemy.moml.MoMLParser]

    # The list of filters is static, so we reset it in case there
    # filters were already added.
    $parser setMoMLFilters [java::null]

    # Add backward compatibility filters
    $parser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    # Add optional .xml files to be skipped to this list.
    set inputFileNamesToSkip [java::new java.util.LinkedList]
    # Alphabetical please
    $inputFileNamesToSkip add "/apps/apps.xml"
    $inputFileNamesToSkip add "/apps/superb/superb.xml"
    #$inputFileNamesToSkip add "/attributes/decorative.xml"
    $inputFileNamesToSkip add "/chic/chic.xml"
    #$inputFileNamesToSkip add "/codegen.xml"
    $inputFileNamesToSkip add "/configs/ellipse.xml"
    $inputFileNamesToSkip add "/gr.xml"
    $inputFileNamesToSkip add "/io/comm/comm.xml"
    $inputFileNamesToSkip add "/image.xml"
    #$inputFileNamesToSkip add "/experimentalDirectors.xml"
    $inputFileNamesToSkip add "/lib/interactive.xml"
    $inputFileNamesToSkip add "/line.xml"
    $inputFileNamesToSkip add "/jai/jai.xml"
    $inputFileNamesToSkip add "/jmf/jmf.xml"
    $inputFileNamesToSkip add "/joystick/jstick.xml"
    $inputFileNamesToSkip add "/jxta/jxta.xml"
    $inputFileNamesToSkip add "/ptinyos/lib/lib-composite.xml"
    $inputFileNamesToSkip add "/rectangle.xml"
    $inputFileNamesToSkip add "TOSIndex.xml"
    $inputFileNamesToSkip add "/quicktime.xml"
    $inputFileNamesToSkip add "/matlab.xml"
    #$inputFileNamesToSkip add "/x10/x10.xml"
    $inputFileNamesToSkip add "utilityIDAttribute.xml"

    # Tell the parser to skip inputting the above files
    java::field $parser inputFileNamesToSkip $inputFileNamesToSkip 

    # Filter out graphical classes while inside MoMLParser
    # See ptII/util/testsuite/removeGraphicalClasses.tcl
    #removeGraphicalClasses $parser

    set loader [[$parser getClass] getClassLoader]

set URL [$loader getResource ptolemy/configs/ptiny/configuration.xml]
puts "Checking [$URL toString]"
set object [$parser {parse java.net.URL java.net.URL} $URL $URL]
set configuration [java::cast ptolemy.kernel.CompositeEntity $object]
puts "Expanding the configuration"
$configuration description
puts "Done expanding"
set results [[java::cast ptolemy.actor.gui.Configuration $configuration] check]
puts $results

# The list of filters is static, so we reset it
java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]
