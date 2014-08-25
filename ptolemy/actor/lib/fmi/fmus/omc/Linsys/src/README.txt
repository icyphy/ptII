I have committed a first version of the sparse FMI generator to the
OpenModelica code repository. To use it, you will also need the
attached files for the sparse FMI runtime system. I'd like to get some
feedback on this, fix any problems, and then prioritize new features
(e.g., support for state events, matrices, algebraic sub-systems,
external function calls, etc.) I've include two examples of models
that work with the current version of the code generator to give you a
sense of what (very limited) language constructs it supports.

Regarding the files for the runtime system, do we have a repository we
want to put them in? I could add them to the OpenModelica repository,
but I'd be happy to put them someplace else too (like the Ptolemy
repository or is there a repository for SOEP?)

Below are the steps for generating an FMI:

1) Get the OpenModelica source code from the repository and build
yourself the compiler. You only need the omc compiler, and you won't
be using any of the standard Modelica runtime system, so go ahead and
disable any packages that make it easy to build.

2) Run the OpenModelica compiler on your .mo file with the command

omc +s +simCodeTarget=sfmi <your file>.mo Modelica

This will create two files: <your model>_FMI.cpp and
modelDescription.xml

The .cpp file is the source code for the FMI and the XML file is the
standard FMI description file.

3) Compile the <your model>_FMI.cpp file and the sfmi_runtime.cpp to
create an FMI. You can either link these directly into your
application, or create a .so/.dll file with them and then load that.

Some important caveats:

The FMI generator does not generate any of the co-simulation functions
(but I didn't think we needed those).

The code generator will only work for simple models like the ones I've
attached as examples. No arrays, no matrices, no algebraic loops, no
events. Just plain old systems of ODEs.

