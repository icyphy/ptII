Translating ECSL-DP to Ptolemy II MoML

Abstract:
The Embedded Control Systems Language for Distributed Processing
(ECSL-DP) is a graphical modeling language developed at Vanderbilt
University.  Researchers at Vanderbilt created a tool chain from
from Simulink and Stateflow to ECSL-DP using XML intermediate files.
We extended the tool chain to provide support for the Ptolemy II
Modeling Markup Language (MoML).  We ran the toolchain on a 
Matlab file containing an automotive cruise control example and
created a standalone native executable binary using the GNU Java
Compiler.

ECSL-DP
-------
The Embedded Control Systems Language (ECSL) was developed by
Institute for Software Integrated Systems (ISIS) at Vanderbilt
University to import Stateflow and Simulink models and make them
available to other tools.  In 2002, ISIS and DaimlerChrysler AG
extended ECSL to include distributed embedded systems, this
new languge is called ECSL-DP.


HSIF
----
The Hybrid Systems Interchange Format (HSIF)
was similar effort at
model translation.  HSIF was developed at ISIS as part
of the Model-Based Integration of Embedded Software (MoBIES) project.

 The HSIF documentation states:

  "The goal of HSIF is to define an interchange format for hybrid
  system
  models that can be shared between modeling and analysis tools. HSIF
  models represent dynamic systems, whose dynamics includes both
  continuous and discrete behaviors."

FIXME: what is the difference between HSIF and ECLS-DP
  The ECSL-DP paper says:

  "We would also like to integrate analysis tools such as HSIF,
  Checkmate, and SAL, into the tool-chains.

HSIF failed because
HyVisual 2.2, released in 2003 by the UC Berkeley Ptolemy group
includes a HSIF to MoML translator created by Haiyang Zheng.  The HSIF 
to MoML translator is implemented using XSL.


ECSL2MoML
---------
Our plan was to develop a tool that would read in ECSL and generate
MoML:


	Simulink/Stateflow -> Matlab2XML -> XML2ECSL- > ECSL2MoML

Figure 1 

ECSL2MoML is a currently a shell script prototyped in awk.  We chose
awk for the prototype because use of scripting language results in
faster turn around than in using Java or XSL.  However, for long
term use, this script should be converted to a series of XSL files.

Simulink Block to Ptolemy II actors
-----------------------------------
One issue is that Simulink blocks have different names than 
the corresponding Ptolemy II actors.  In addition, the Simulink block
port and parameter names are different than the corresponding Ptolemy
II port and parameter names.

Initially, when developing the translator, we considered using
the Ptolemy II 4.0 actor-oriented class mechansim.
FIXME: more on this.

We also considered using composite actors
FIXME: more on this.

In the end, we decided to construct a set of Ptolemy II wrapper actors
that were named after the corresponding Simulink block but 
extended the corresponding Ptolemy II actor.  The wrapper
actor can rename the ports and parameters as needed.  An additional
benefit is that a Ptolemy II user who is familiar with Simulink will
see actors with Simulink names.


Generating native code
----------------------
To generate native code, we use the Copernicus [FIXME: REF] Shallow
code generator created by Stephen Neuendorffer.  Copernicus uses Soot
[FIXME: REF] to transform Ptolemy models into Java byte code.  The
example automotive cruise control model uses Continuous Time domain
semantics, so we chose to use shallow code generation, which 
reads in a model and generates a Java class file that instantiates
the model, connects the Ptolemy actors together and runs the model.

We then run the model, note what Java classes are loaded 
and create a jar file that contains only the necessary classes.
This method is called "tree shaking," it has been used in
Lisp systems to generate smaller runtime executables. 

Once we have the tree shaken jar file, we pass it to the GNU Java
compiler (GCJ).  GCJ reads .class files, generates object files and
then uses the C linker to create a native executable.  

One issue is that in each object file, all of the references to other
Java classes must be resolved at link time.  This is because GCJ
creates object files (.o files) for each class and these .o files have
references to symbols corresponding with methods in other Java
classes.  Thus, merely having run the model and noted what classes
are loaded is not sufficient, since a class that was loaded might
have methods that were not run but that refer to other classes.
A more fine grained tree shaker that notes method calls might help
here, but instead we used an iterative approach of attempting
to compile the code and then parsing the compiler error messages
and adding missing classes.

FIXME: show some results


Future Work
-----------
Translating Stateflow


References
----------
Sandeep Neema, Gabor Karsai,
"Embedded Control Systems Language for Distributed Processing,"
TR ISIS-04-505, Vanderbilt University, Nashville, TN.
http://www.isis.vanderbilt.edu/publications/archive/Neema_S_5_12_2004_Embedded_C.pdf


C. Brooks, A. Cataldo, E. A. Lee, J. Liu, X. Liu, S. Neuendorffer,
H. Zheng, "HyVisual: A Hybrid System Visual Modeler."
Technical Memorandum UCB/ERL M04/18, University of California,
Berkeley, CA 94720, June 28, 2004. 
http://ptolemy.eecs.berkeley.edu/publications/papers/04/hyvisual

Sprinkle, J., Karsai, G., Lang, A.: Hybrid Systems Interchange Format
(v.4.1.8).
Vanderbilt University, http://www.isis.vanderbilt.edu/projects/mobies/
downloads.asp. (2004)

Sprinkle, J., "Generative Components for Hybrid Systems
Tools,"
6th GPCE Young Researchers Workshop 2004, University of Colorado,
Boulder, CO, USA, October 24, 2004
http://serl.cs.colorado.edu/~rutherfo/gpce_yrw04/program/sprinkle.pdf

Stephen Neuendorffer, "Automatic Specialization of Actor-Oriented
Models in Ptolemy II," Memorandum UCB/ERL M02/41,
EECS, University of California, Berkeley, CA 94720, USA
December 25, 2002 
http://ptolemy.eecs.berkeley.edu/papers/02/actorSpecialization/actorSpecialization.pdf


Raja Vall\'ee-Rai, Laurie Hendren, Vijay Sundaresan, Patrick Lam, Etienne Gagnon and Phong Co","Soot - a Java Optimization Framework,"
"Proceedings of CASCON 1999" pages 125-135, November, 1999
http://www.sable.mcgill.ca/publications/#cascon99


CT domain?

[Tree shaking]
D. Kevin Layer, Chris Richardson, "Lisp systems in the 1990s,"
Communications of the ACM archive Volume 34,  Issue 9 (September 1991)
Pages: 48 - 57,  ACM Press   New York, NY, USA 





