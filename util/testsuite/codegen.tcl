# Ptolemy II test suite definitions for code gen
#
# @Authors: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2001-2003 The Regents of the University of California.
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



if {[string compare removeGraphicalClasses [info procs removeGraphicalClasses]] != 0} \
        then {
    source [file join $PTII util testsuite removeGraphicalClasses.tcl]
} {}

proc speedComparison  {xmlFile \
	{modelName "" } \
	{targetPackage ptolemy.copernicus.shallow.cg} \
	{repeat 3} \
        {modelClass ""} \
	{codeGenType Shallow} \
    } { 

    global relativePathToPTII
    if { $modelName == "" } {
	set parser [java::new ptolemy.moml.MoMLParser]

        # The list of filters is static, so we reset it in case there
        # filters were already added.
        $parser setMoMLFilters [java::null]
        $parser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

	# Filter out graphical classes while inside MoMLParser
	# See ptII/util/testsuite/removeGraphicalClasses.tcl
	removeGraphicalClasses $parser

	set toplevel [$parser parseFile $xmlFile]
	set modelName [string range [$toplevel getFullName] 1 end]
    }

    if { $modelClass == "" } {
	set modelClass CG$modelName
    }

    # The fully qualified classname of the code we generated.
    set targetClass $targetPackage.$modelName.$modelClass


    if {$codeGenType == "Deep"} {
	#puts "Not generation speed comparision stats for Deep yet"
	set args [java::new {String[]} 0]
	puts "Running builtin $codeGenType codegen $repeat times"
	#set codegenElapsed 0
	set codegenElapsed [time {java::call \
		$targetClass \
		main $args} $repeat]
	
	puts "Running exec $codeGenType codegen $repeat times"
	#set codegenExecElapsed 0
	set codegenExecElapsed \
		[time {exec java -classpath $relativePathToPTII $targetClass} $repeat]

    } else {
	if {$codeGenType == "Actor" \
		|| $codeGenType == "Applet"} {
		|| $codeGenType == "Interpreted"} {
	    # Can't run applets without a head
	    # Can't run interpreted code in the nightly build because
	    # we do not find the JavaScope jar files.
	    puts "$codeGenType codegen done"
	} else {
	    set targetClass $modelName.$modelClass

	    set args [java::new {String[]} 2 \
			  [list \
			       "-class" "$targetClass"]]

	    set startDirectory [pwd]
	    cd ../cg/$modelName
	    puts "Running builtin $codeGenType codegen $repeat times"
	    set timeout 1200

	    puts "Setting watchdog for $timeout milliseconds"
	    set watchDog [java::new util.testsuite.WatchDog $timeout]

	    set codegenElapsed \
		    [time {java::call \
		    ptolemy.actor.gui.CompositeActorApplication \
		    main $args} $repeat]
	    $watchDog cancel

	    cd $startDirectory


	    puts "Running exec $codeGenType codegen $repeat times"
	    set codegenExecElapsed \
		[time {exec java -classpath $relativePathToPTII \
		ptolemy.actor.gui.CompositeActorApplication \
		-class $targetClass} $repeat]
	}
    }

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
    set codegenElapsedTime [expr {int([lindex $codegenElapsed 0] /1000.0)}]
    set interpretedElapsedTime [expr {int([lindex $interpretedElapsed 0] /1000.0)}]

    if {[lindex $interpretedElapsed 0] == 0} {
	set elapsedRatio 0
    } else {
	set elapsedRatio [expr { \
		int(
	(([lindex $codegenElapsed 0] + 0.0) \
		/ \
		([lindex $interpretedElapsed 0] + 0.0)) * 100)}]
    }

    set codegenExecElapsedTime \
	    [expr {int([lindex $codegenExecElapsed 0] /1000.0)}]
    set interpretedExecElapsedTime \
	    [expr {int([lindex $interpretedExecElapsed 0] /1000.0)}]

    if {[lindex $interpretedExecElapsed 0] == 0} { 
	set execElapsedRatio 0
    } else {
	set execElapsedRatio [expr { \
		int(
	(([lindex $codegenExecElapsed 0] + 0.0) \
		/ \
		([lindex $interpretedExecElapsed 0] + 0.0)) * 100)}]
    }

    puts "$modelName $repeat builtin runs: Interp/$codeGenType: \
	    $interpretedElapsedTime/$codegenElapsedTime \
	    ($elapsedRatio%)"

    puts "$modelName $repeat exec runs: Interp/$codeGenType: \
	    $interpretedExecElapsedTime/$codegenExecElapsedTime \
	    ($execElapsedRatio%)"

    # The percentage is separated by spaces to make this more machine readable
    return "Times Interp/$codeGenType ms $modelName $repeat \
	    builtin: $interpretedElapsedTime/$codegenElapsedTime \
	    $elapsedRatio % \
	    exec: $interpretedExecElapsedTime/$codegenExecElapsedTime \
	    $execElapsedRatio %"
}

# Generate java code for a model.  The model argument names a .xml
# file which will be interpreted as a relative pathname
# Example: 
# sootCodeGeneration [file join $relativePathToPTII ptolemy actor lib test auto MathFunction3.xml] Deep 3
#
# @param modelPath The relative pathname to the .xml file that defines
# the model. MoMLParser.parse() forces this pathname to be relative
#
# @param codeGenType The type of codegen to do, either "Shallow" or "Deep".
# defaults to Shallow.
#
# @param defaultIterations The default number of iterations.  Defaults
# to the empty string, which means that the model is queried for
# the number of iterations.  If the number of iterations is 0, then a
# warning message is printed and the iterations are set to 200.
# If the iterations cannot be found, then a warning message is printed
#
# If statsOnly is 1, then we do not generate the code, we
# just print the make command to run the stats.
#
# If speedComparison is 1, then a speed comparison of the generated 
# code is performed.
#
# The command to run after generating code is given by the 
# runCommand parameter.
#
proc sootCodeGeneration {{PTII} modelPath {codeGenType Shallow} \
			     {defaultIterations {}} \
			     {statsOnly 0} \
			     {speedComparison 0} \
			     {runCommand "longTest"}} {
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

    # The list of filters is static, so we reset it in case there
    # filters were already added.
    $parser setMoMLFilters [java::null]
    $parser addMoMLFilters \
        [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]


    if { ${codeGenType} == "Applet" } {
          #set args [java::new {String[]} 5 \
  	  #  [list \
  	  #  $modelPath \
	  #	 "-codeGenerator" "applet" \
	  #	 "-run" "false"] ]
	  #java::new ptolemy.copernicus.kernel.Copernicus $args	
	  # -q means do not echo the command being run
   	  set args [list -q $modelPath \
		  "-codeGenerator" "applet" \
		      "-run" "false" \
		      "-compile" "true"]
	  set javaCommand [list sh $PTII/bin/copernicus]
   	  set execCommand [concat $javaCommand $args]
          puts "Running Copernicus: $execCommand"
	  set results [eval exec $execCommand]
	  puts $results
 	  return "Times Interp/Deep ms $modelPath 1 \
	    builtin: 0/0 \
	    0 % \
	    exec: 0/0 \
	    0 %"
    } 

    # Filter out graphical classes while inside MoMLParser
    # See ptII/util/testsuite/removeGraphicalClasses.tcl
    removeGraphicalClasses $parser

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
    set modelName [java::call ptolemy.util.StringUtilities \
	    sanitizeName $modelName]

    #if {[string range $modelPath 0 2] == "../"} {
    #	# Ugh.  Strip off the first ../ because we are cd'ing up one level.
    #	set realModelPath $modelPath
    # 	set modelPath [string range $modelPath 3 end]
    #}


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
	if [java::instanceof \
		$director ptolemy.domains.dt.kernel.DTDirector] {
	    puts "$modelPath:  Deep codegen only works on SDF.\n\
		    The director is not a SDFDirector, it is a $director"
	    return
	}
    
	# check for transparent hierarchy
	set entityList [$compositeActor entityList]
	for {set i 0} {$i < [$entityList size]} {incr i} {
	    set containedActor [$entityList get $i]
	    if [java::instanceof $containedActor \
		    ptolemy.actor.TypedCompositeActor] {
		puts "$modelPath:  Deep codegen does not work on models with transparent hierarchy"
		return
	    }

	}

	set portList [$compositeActor portList]
	if {[$portList size] > 0} {
	    puts "$modelPath:  Deep codegen does not work on models that have external ports"
	    return
	}
	
	puts "We can run Deep codegen on $modelName"

	# speedComparison uses these
	set targetPackage ptolemy.copernicus.java.cg
	set modelClass Main
    }

    # If we this proc was called with the defaultIterations argument
    # set, then we use it.  If it was not set, then we query
    # the model for the iterations parameter and if the iterations
    # parameter was set to 0, we print a message and set it to 200
    #
    set iterationsParameter ""
    if { "$defaultIterations" != "" } {
	puts "Using the defaultIterations parameter of '$defaultIterations'"
	set iterationsParameter $defaultIterations
    } else {
	# Take a stab at guessing the number of iterations
	set compositeActor [java::cast ptolemy.actor.CompositeActor $toplevel]
	set director [$compositeActor getDirector]
	set iterations [$director getAttribute iterations]
	if { $iterations == [java::null] } {
	    puts "WARNING: iterations parameter not found in\n\
		    '$modelName',\n \
		    perhaps this model is not SDF?"
	} else {
	    set iterationsValue [[java::cast ptolemy.data.IntToken \
		    [[java::cast ptolemy.data.expr.Parameter \
		    $iterations]  getToken]] doubleValue]
	    if { [expr { ceil($iterationsValue) \
		    == floor($iterationsValue)} ] } {
		# If the floor and the ceiling are the same this this
		# is an integer value, so round it.
		# If we don't then we are likely to get a NumberFormatException
		set iterationsValue [expr {round ($iterationsValue)}]
	    }
	    puts "iterationsValue = $iterationsValue"
	    if { "$iterationsValue" == "0"} {
		puts "WARNING: iterationsValue was 0, defaulting to 200"
		set iterationsValue 200
	    }
	}
    }

    if { $statsOnly == 1} {
	puts "sootCodeGeneration called with statsOnly == 1, so we are not regenerating"
    } else {
	set command compileDemo
	puts "Now running 'make ... $command', this could take 60 seconds or so"

	set results ""
	# make -C is a GNU make extension that changes to a directory
	#set results [exec make -C .. MODEL=$model SOURCECLASS=$modelPath $command]

	if { ${codeGenType} == "Deep" } {
	    set codeGenerator "java"
	} else {
	    if { ${codeGenType} == "Interpreted" } {
		set codeGenerator "interpreted"
            } else {		
		if { ${codeGenType} == "Actor" } {
		    set codeGenerator "actor"
		} else {
		    set codeGenerator "shallow"
		}
	    }
	}
	# -q means do not echo the command being run
	set args [list -q $modelPath \
		      "-codeGenerator" $codeGenerator \
		      "-compile" "true" \
		      "-output" "codeGenerator.tmp"]
	# If toplevel iterations set, then pass those arguments to copernicus.
	if { ${iterationsParameter} != ""} {
	    lappend args "-iterations" ${iterationsParameter}
	}

	set separator [java::field java.io.File pathSeparator]
	set javaCommand [list sh $PTII/bin/copernicus]
#	set javaCommand [list java -classpath "$PTII$separator$PTII/lib/soot.jar$separator$PTII/lib/jasmin.jar" -Dptolemy.ptII.dir=$PTII ptolemy.copernicus.kernel.Copernicus]

	set execCommand [concat $javaCommand $args]
        puts "Running Copernicus: $execCommand"
	#    	java::new ptolemy.copernicus.kernel.Copernicus $args
	
	set timeout 1300000
	puts "Setting watchdog for $timeout milliseconds"
	set watchDog [java::new util.testsuite.WatchDog $timeout]

	set results [eval exec $execCommand]
	$watchDog cancel

	puts $results

	#    if [catch {set results [exec make -C .. MODEL=$model SOURCECLASS=$modelPath $command]]} errMsg] {
	#	puts $results
	#	puts $errMsg
	#    }
	#    puts $results
    }

    # If the model has a different name than the file name, we
    # handle it here.
    set command $runCommand

    if { $statsOnly == 1 || ${codeGenType} == "Actor" } {
	# Just print the command
	puts "make MODEL=$modelName SOURCECLASS=$modelPath $command"
	return "Times Interp/Deep ms $modelName 1 \
	    builtin: 0/0 \
	    0 % \
	    exec: 0/0 \
	    0 %"
    } else {

	set timeout 1200000
	puts "Setting watchdog for $timeout milliseconds"
	set watchDog [java::new util.testsuite.WatchDog $timeout]

	# We use the makefile generated by MakefileWriter here

	# Note that if running the model generates any output on stderr,
	# then exec will return an error.  To workaround this,
	# try passing -stderrok as the first arg to exec: exec -stderrok make
	# 'make obfuscate' was failing with an error here until we
	# redirected 2>&1
	if [catch {set results [exec make -C ../cg/$modelName \
		MODEL=$modelName \
		SOURCECLASS=$modelPath $command]} errMsg] {
	    $watchDog cancel
	    error $errMsg
	} else {
	    $watchDog cancel
	}

	puts $results
    }

    if {$speedComparison == 1} {
        return [speedComparison $modelPath $modelName $targetPackage \
		    3 $modelClass $codeGenType]
    } else {
	return $results
    }
} 

#  # Read in a model, generate code and run it in the current jvm
#  proc sootShallowCodeGenerationBuiltin {model} {
#      global relativePathToPTII

#      # We need to get the classpath so that we can run if we are running
#      # under Javascope, which includes classes in a zip file
#      set builtinClasspath [java::call System getProperty "java.class.path"]
#      set rtjar [java::call System getProperty "sun.boot.class.path"]

#      set sootClasspath $relativePathToPTII/vendors/soot/1.2.2/jasminclasses.jar[java::field java.io.File pathSeparator]$relativePathToPTII/vendors/soot/1.2.2/sootclasses.jar

#      set classpath $relativePathToPTII[java::field java.io.File pathSeparator].[java::field java.io.File pathSeparator]$sootClasspath[java::field java.io.File pathSeparator]$builtinClasspath[java::field java.io.File pathSeparator]$rtjar


#      set args [java::new {String[]} 12 \
#  	    [list \
#  	    $model "-d" $relativePathToPTII \
#  	    "-p" "wjtp.at" "targetPackage:ptolemy.copernicus.java.test.cg" \
#  	    "-p" "wjtp.mt" "targetPackage:ptolemy.copernicus.java.test.cg" \
#  	    "-p" "wjtp.umr" "disabled" \
#  	    ]]
#      set main [java::new ptolemy.copernicus.java.Main $args]
#      set toplevel [$main readInModel $model]
#      $main initialize $toplevel
#      $main addTransforms
#      set modelName ptolemy.copernicus.java.test.cg.CG[$toplevel getName]

#      # Make a stab at getting the iterations
#      set director [$toplevel getDirector]
#      set iterations [$director getAttribute iterations]
#      if { $iterations == [java::null] } {
#  	puts "WARNING: iterations parameter not found in\n '$modelName',\n \
#  		perhaps this model is not SDF?"
#      } else {
#  	set iterationsValue [[java::cast ptolemy.data.IntToken \
#  	    [[java::cast ptolemy.data.expr.Parameter \
#  	    $iterations]  getToken]] doubleValue]
#  	puts "iterationsValue = $iterationsValue"
#      }


#      # See KernelMain.generateCode for a description of why this is necessary
#      $args set 0 "java.lang.Object"
#      java::call soot.Main setReservedNames
#      java::call soot.Main setCmdLineArgs $args
#      set main [java::new soot.Main]
#      set ccl [java::new soot.ConsoleCompilationListener]
#      java::call soot.Main addCompilationListener $ccl
#      $main run
#      #set thread [java::new Thread main]
#      #$thread start

#      # Find the new classes in the new Scene.
#      # Soot tries to be smart and refresh the scene in between comilations, so so must we.
#      java::call ptolemy.copernicus.java.PtolemyUtilities loadSootReferences

#  #    exec java -Xmx132m -classpath $classpath \
#  #	    ptolemy.copernicus.java.Main 
#  #           $model -d $relativePathToPTII \
#  #	    -p wjtp.at targetPackage:ptolemy.copernicus.java.test.cg \
#  #	    -p wjtp.mt targetPackage:ptolemy.copernicus.java.test.cg




#  #    set applicationArguments [java::new {java.lang.String[]} 4 [list \
#  #	    "-class" $modelName \
#  #	    "-iterations" "10" \
#  #	    ]]
#  #
#  #    set application [java::new ptolemy.actor.gui.CompositeActorApplication]
#  #    $application processArgs $applicationArguments
#  #    set models [listToObjects [$application models]]
#  #    $application waitForFinish
#  #    set result {}
#  #    foreach model $models {
#  #        set modelc [java::cast ptolemy.actor.gui.test.TestModel $model]
#  #        lappend result [listToStrings [$modelc getResults]]
#  #    }
#  #    list $result

#  #    return [exec java -Xfuture -classpath $classpath ptolemy.actor.gui.CompositeActorApplication -iterations 10 -class $modelName]
#      puts "sootShallowCodeGeneration {$model}: running $modelName"

#      return [exec java -Xfuture -classpath $classpath ptolemy.actor.gui.CompositeActorApplication -class $modelName]
#  }


