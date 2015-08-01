puts "Simple standalone clone test"

source testDefs.tcl
set workspace [java::new ptolemy.kernel.util.Workspace]
set compositeEntity [java::new ptolemy.kernel.CompositeEntity $workspace]
$compositeEntity setName Top
#set actor [java::new ptolemy.actor.lib.colt.ColtZeta $compositeEntity "Zeta"]
#set actor [java::new ptolemy.actor.lib.Gaussian $compositeEntity "Gaussian"]
set actor [java::new ptolemy.actor.lib.jjs.JavaScript $compositeEntity "JavaScript"]
set clone [java::cast ptolemy.actor.TypedAtomicActor [$actor clone $workspace]]

#set actor [java::new org.ptolemy.optimization.CompositeOptimizer $compositeEntity "CompositeOptimizer"]
#set clone [java::cast ptolemy.actor.TypedCompositeActor [$actor clone $workspace]]




set constraints [$actor typeConstraintList]
set c [jdkPrintArray [$constraints toArray] "\n" ]

set cloneConstraints [$clone typeConstraintList]
set cc [jdkPrintArray [$cloneConstraints toArray] "\n" ]

puts "The constraints of the master:"
puts "$c"
puts "The constraints of the clone:"
puts "$cc"

if {"$c" == "$cc"} {
    puts "Congrats, the constraints are the same."
} {
    puts "Error! The constraints are not the same?"
}    
