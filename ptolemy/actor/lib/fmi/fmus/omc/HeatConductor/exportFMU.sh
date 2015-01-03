export OPENMODELICALIBRARY=`pwd`:/usr/lib/omlibrary:$MODELICAPATH

rm -f HeatConductor_*
rm -f *.fmu
rm -f modelDescription.xml
#omc +d=nogen,initialization,backenddaeinfo,discreteinfo,stateselection exportFMU.mos
# Disable tearing (see James' email from Nov. 26, 2014)
omc +tearingMethod=noTearing exportFMU.mos
#omc exportFMU.mos
rm -f HeatConductor_*
rm -f *.libs *.mat *.makefile *.c
#rm -f modelDescription.xml

# 1/2/15: OpenModelica puts the binaries to binaries/makefileParams.platform
# The section below fixes this bug in the FMU.
FMU=HeatConductor.fmu
CURDIR=`pwd`
TEMP=`mktemp --directory`
echo "Temporary directory is $TEMP"
mv $FMU $TEMP/
mkdir $TEMP/fmu
unzip $TEMP/$FMU -d $TEMP/fmu
mv $TEMP/fmu/binaries/makefileParams.platform $TEMP/fmu/binaries/linux64
cd $TEMP/fmu
rm $TEMP/$FMU
zip -r $TEMP/$FMU .
cd $CURDIR
mv $TEMP/$FMU .
rm -rf $TEMP




