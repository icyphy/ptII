# Ptolemy II test suite definitions for code gen
#
# @Authors: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2001 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
#######################################################################




proc speedComparison  {xmlFile \
	{modelName "" } \
	{targetPackage ptolemy.copernicus.shallow.cg} \
	{repeat 3} \
        {modelClass ""} } { 

    global relativePathToPTII
    if { $modelName == "" } {
	set parser [java::new ptolemy.moml.MoMLParser]
	set toplevel [$parser parseFile $xmlFile]
	set modelName [string range [$toplevel getFullName] 1 end]
    }

    if { $modelClass == "" } {
	set modelClass CG$modelName
    }

    # The fully qualified classname of the code we generated.
    set targetClass $targetPackage.$modelName.$modelClass

    set args [java::new {String[]} 2 \
	    [list \
	    "-class" "$targetClass"]]

    puts "Running builtin shallow $repeat times"
    set shallowElapsed [time {java::call \
    	    ptolemy.actor.gui.CompositeActorApplication \
    	    main $args} $repeat]

    puts "Running exec shallow $repeat times"
    set shallowExecElapsed \
	    [time {exec java -classpath $relativePathToPTII \
	    ptolemy.actor.gui.CompositeActorApplication \
	    -class $targetClass} $repeat]

    set args [java::new {String[]} 1 \
	    [list $xmlFile]]

    puts "Running builtin interpreted $repeat times"
    set interpretedElapsed [time {java::call \
    	    ptolemy.actor.gui.MoMLSimpleApplication \
    	    main $args} $repeat]

    puts "Running exec interpreted $repeat times"
    set interpretedExecElapsed \
	    [time {exec java -classpath $relativePathToPTII \
	    ptolemy.actor.gui.MoMLSimpleApplication \
	    $xmlFile} $repeat]

    # Convert from Tcl time's microseconds to milliseconds
    set shallowElapsedTime [expr {int([lindex $shallowElapsed 0] /1000.0)}]
    set interpretedElapsedTime [expr {int([lindex $interpretedElapsed 0] /1000.0)}]

    if {[lindex $interpretedElapsed 0] == 0} {
	set elapsedRatio 0
    } else {
	set elapsedRatio [expr { \
		int(
	(([lindex $shallowElapsed 0] + 0.0) \
		/ \
		([lindex $interpretedElapsed 0] + 0.0)) * 100)}]
    }

    set shallowExecElapsedTime \
	    [expr {int([lindex $shallowExecElapsed 0] /1000.0)}]
    set interpretedExecElapsedTime \
	    [expr {int([lindex $interpretedExecElapsed 0] /1000.0)}]

    if {[lindex $interpretedExecElapsed 0] == 0} { 
	set execElapsedRatio 0
    } else {
	set execElapsedRatio [expr { \
		int(
	(([lindex $shallowExecElapsed 0] + 0.0) \
		/ \
		([lindex $interpretedExecElapsed 0] + 0.0)) * 100)}]
    }

    puts "$modelName $repeat builtin runs: Interp/Shallow: \
	    $interpretedElapsedTime/$shallowElapsedTime \
	    ($elapsedRatio%)"

    puts "$modelName $repeat exec runs: Interp/Shallow: \
	    $interpretedExecElapsedTime/$shallowExecElapsedTime \
	    ($execElapsedRatio%)"

    # The percentage is separated by spaces to make this more machine readable
    return "Times Interp/Shallow ms $modelName $repeat \
	    builtin: $interpretedElapsedTime/$shallowElapsedTime \
	    $elapsedRatio % \
	    exec: $interpretedExecElapsedTime/$shallowExecElapsedTime \
	    $execElapsedRatio %"
}

# Generate java code for a model.  The model argument names a .xml
# file which will be interpreted as a relative pathname
# (MoMLParser.parse() forces this)
proc sootCodeGeneration {modelPath {codeGenType Shallow}} {
    global relativePathToPTII

    if {[file extension $modelPath] == ""} {
	set model [file tail $modelPath]
    } else {
	set modelWithExtension [file tail $modelPath]
	set model [string range $modelWithExtension \
		0 [expr {[string length $modelWithExtension] - \
			[string length [file extension $modelWithExtension]] \
			- 1}]]
    }

    if { ! [file exists $modelPath ] } {
	error "'$modelPath' does not exist"
    }

    # Some models do not have names that match their file extensions
    # For example ptolemy/domains/dt/kernel/test/auto/Chain3.xml defines
    # a system called TestChain3
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parseFile $modelPath]
    # Strip off the leading .
    set modelName [string range [$toplevel getFullName] 1 end]

    if {"$model" != "$modelName"} {
	puts stderr "WARNING: model name and file name do not match\n\
		'$modelPath'\n defines a model named\n\
		'$modelName', yet the file is called '$model'"
    }

    if {"$modelName" == ""} {
	puts stderr "WARNING: model name was empty, defaulting to file name"
	set modelName $model
    }
    # Get rid of any spaces in the name
    set modelName [java::call ptolemy.copernicus.kernel.SootUtilities \
	    sanitizeName $modelName]

    if {[string range $modelPath 0 2] == "../"} {
	# Ugh.  Strip off the first ../ because we are cd'ing up one level.
	set realModelPath $modelPath
	set modelPath [string range $modelPath 3 end]
    }


    puts "adjusted modelPath: $modelPath"
    puts "modelName: $modelName"

    # speedComparison uses this
    set targetPackage ptolemy.copernicus.shallow.cg

    # speedComparison uses this
    set modelClass ""

    # If this is deep code gen, then check that the model is a flat sdf model
    if { ${codeGenType} == "Deep" } {
	set compositeActor [java::cast ptolemy.actor.CompositeActor $toplevel]
	set director [$compositeActor getDirector]
	if ![java::instanceof \
		$director ptolemy.domains.sdf.kernel.SDFDirector] {
	    puts "$modelPath:  Deep codegen only works on SDF.\n\
		    The director is not a SDFDirector, it is a $director"
	    return
	}
    
	set deepEntityList [$compositeActor deepEntityList]
	for {set i 0} {$i < [$deepEntityList size]} {incr i} {
	    set containedActor [$deepEntityList get $i]
	    if [java::instanceof $containedActor \
		    ptolemy.actor.TypedCompositeActor] {
		puts "$modelPath:  Deep codegen only works on flat models"
		return
	    }

	}
	puts "We can run Deep codegen on $modelName"

	# speedComparison uses these
	set targetPackage ptolemy.copernicus.java.cg
	set modelClass Main
    }

    set command compileDemo
    puts "Now running 'make ... $command', this could take 60 seconds or so"

    set results ""
    # make -C is a GNU make extension that changes to a directory
    set results [exec make -C .. MODEL=$model SOURCECLASS=$modelPath ITERATIONS_PARAMETER= $command]
    puts $results
#    if [catch {set results [exec make -C .. MODEL=$model SOURCECLASS=$modelPath $command]]} errMsg] {
#	puts $results
#	puts $errMsg
#    }
#    puts $results
    # If the model has a different name than the file name, we
    # handle it here.
    set command runDemo
    set results [exec make -C .. MODEL=$modelName \
	    SOURCECLASS=$modelPath $command]
    puts $results

    return [speedComparison $realModelPath $modelName $targetPackage \
	    3 $modelClass]
} 


# Read in a model, generate code and run it in the current jvm
proc sootShallowCodeGenerationBuiltin {model} {
    global relativePathToPTII

    # We need to get the classpath so that we can run if we are running
    # under Javascope, which includes classes in a zip file
    set builtinClasspath [java::call System getProperty "java.class.path"]
    set rtjar [java::call System getProperty "sun.boot.class.path"]

    set sootClasspath $relativePathToPTII/vendors/soot/1.2.2/jasminclasses.jar[java::field java.io.File pathSeparator]$relativePathToPTII/vendors/soot/1.2.2/sootclasses.jar

    set classpath $relativePathToPTII[java::field java.io.File pathSeparator].[java::field java.io.File pathSeparator]$sootClasspath[java::field java.io.File pathSeparator]$builtinClasspath[java::field java.io.File pathSeparator]$rtjar


    set args [java::new {String[]} 12 \
	    [list \
	    $model "-d" $relativePathToPTII \
	    "-p" "wjtp.at" "targetPackage:ptolemy.copernicus.java.test.cg" \
	    "-p" "wjtp.mt" "targetPackage:ptolemy.copernicus.java.test.cg" \
	    "-p" "wjtp.umr" "disabled" \
	    ]]
    set main [java::new ptolemy.copernicus.java.Main $args]
    set toplevel [$main readInModel $model]
    $main initialize $toplevel
    $main addTransforms
    set modelName ptolemy.copernicus.java.test.cg.CG[$toplevel getName]

    # Make a stab at getting the iterations
    set director [$toplevel getDirector]
    set iterations [$director getAttribute iterations]
    if { $iterations == [java::null] } {
	puts "WARNING: iterations parameter not found in\n '$modelName',\n \
		perhaps this model is not SDF?"
    } else {
	set iterationsValue [[java::cast ptolemy.data.IntToken \
	    [[java::cast ptolemy.data.expr.Parameter \
	    $iterations]  getToken]] doubleValue]
	puts "iterationsValue = $iterationsValue"
    }


    # See KernelMain.generateCode for a description of why this is necessary
    $args set 0 "java.lang.Object"
    java::call soot.Main setReservedNames
    java::call soot.Main setCmdLineArgs $args
    set main [java::new soot.Main]
    set ccl [java::new soot.ConsoleCompilationListener]
    java::call soot.Main addCompilationListener $ccl
    $main run
    #set thread [java::new Thread main]
    #$thread start

    # Find the new classes in the new Scene.
    # Soot tries to be smart and refresh the scene in between comilations, so so must we.
    java::call ptolemy.copernicus.java.PtolemyUtilities loadSootReferences

#    exec java -Xmx132m -classpath $classpath \
#	    ptolemy.copernicus.java.Main 
#           $model -d $relativePathToPTII \
#	    -p wjtp.at targetPackage:ptolemy.copernicus.java.test.cg \
#	    -p wjtp.mt targetPackage:ptolemy.copernicus.java.test.cg




#    set applicationArguments [java::new {java.lang.String[]} 4 [list \
#	    "-class" $modelName \
#	    "-iterations" "10" \
#	    ]]
#
#    set application [java::new ptolemy.actor.gui.CompositeActorApplication]
#    $application processArgs $applicationArguments
#    set models [listToObjects [$application models]]
#    $application waitForFinish
#    set result {}
#    foreach model $models {
#        set modelc [java::cast ptolemy.actor.gui.test.TestModel $model]
#        lappend result [listToStrings [$modelc getResults]]
#    }
#    list $result

#    return [exec java -Xfuture -classpath $classpath ptolemy.actor.gui.CompositeActorApplication -iterations 10 -class $modelName]
    puts "sootShallowCodeGeneration {$model}: running $modelName"

    return [exec java -Xfuture -classpath $classpath ptolemy.actor.gui.CompositeActorApplication -class $modelName]
}


