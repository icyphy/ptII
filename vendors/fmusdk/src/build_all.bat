@echo off 

rem ------------------------------------------------------------
rem This batch builds both simulators and all FMUs of the FmuSDK
rem (c) 2011 QTronic GmbH
rem ------------------------------------------------------------

call build_fmusim_me
call build_fmusim_cs
echo -----------------------------------------------------------
echo Making the FMUs of the FmuSDK ...
pushd models

call build_fmu me dq 
call build_fmu me inc
call build_fmu me values
call build_fmu me vanDerPol
call build_fmu me bouncingBall

call build_fmu cs dq
call build_fmu cs inc
call build_fmu cs values
call build_fmu cs vanDerPol
call build_fmu cs bouncingBall

popd


