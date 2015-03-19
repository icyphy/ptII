ptolemy/actor/lib/fmi/fmus/omc/README.txt
Version: $Id$

The fmus here use the work of James Nutaro.

The fmus include files from https://github.com/smiz/sparse_fmi


For information about how to install OpenModelica and export FMUs, see

http://chess.eecs.berkeley.edu/ptexternal/wiki/Main/OpenModelica


There are several ways to create a .fmu

The simplest way is to create a .mos file like exportFMU.mos:

--start--
loadModel(Modelica, {"3.2.1"}); 
  getErrorString();
  loadModel(HeatConductor); getErrorString();
  OpenModelica.Scripting.translateModelFMU(
    className=HeatConductor,
    version="2.0"); 
  getErrorString();
--end--

and then invoke

omc exportFMU.mos

To use the tearingMethod, use:

omc +tearingMethod=noTearing exportFMU.mos

Using omc creates the modelDescription.xml file and binaries for one
platform.

Using omc is the most reliable method, though the fmu that is produced
will not work on other platforms.

Ptolemy II has an extension that builds a fmu at runtime.  This
extension runs "make platformName", where platformName is the fmu
platform such as linux64 or darwin64.

Unfortunately, the location of the OpenModelica platform varies between
installations.  Under Linux, OpenModelica tends to be located in
/usr/local/openmodelica.  Under Mac OS X, when installed using
MacPorts, /opt/local.

Thus, building OpenModelica fmus from within Ptolemy is fragile.



