puts "Simple standalone clone test"

source testDefs.tcl
set workspace [java::new ptolemy.kernel.util.Workspace]
set compositeEntity [java::new ptolemy.kernel.CompositeEntity $workspace]
$compositeEntity setName Top
#set actor [java::new ptolemy.actor.lib.colt.ColtZeta $compositeEntity "Zeta"]
#set actor [java::new ptolemy.actor.lib.Gaussian $compositeEntity "Gaussian"]
#set actor [java::new ptolemy.actor.lib.jjs.JavaScript $compositeEntity "JavaScript"]
#set actor [java::new org.ptolemy.optimization.CompositeOptimizer $compositeEntity "CompositeOptimizer"]
set actor [java::new org.ptolemy.ssm.Map $compositeEntity "Map"]
#set actor [java::new org.ptolemy.ssm.MapTest $compositeEntity "MapTest"]
#set actor [java::new org.ptolemy.ssm.ParameterCloneTest $compositeEntity "ParameterCloneTest"]

set clone [java::cast ptolemy.actor.TypedAtomicActor [$actor clone $workspace]]

set constraints [$actor typeConstraintList]
set c [lsort [jdkPrintArray [$constraints toArray]]]

$actor setContainer [java::null]

$clone setContainer $compositeEntity

set cloneConstraints [$clone typeConstraintList]
set cc [lsort [jdkPrintArray [$cloneConstraints toArray]]]


puts "The constraints of the master:"
set count 0
foreach element $c {
    puts "  [incr count]. $element"
}

puts "The constraints of the clone:"
set count 0
foreach element $cc {
    puts "  [incr count]. $element"
}


if {"$c" == "$cc"} {
    puts "Congrats, the constraints are the same."
} {
    puts "Error! The constraints are not the same?"
}    
