@echo off 

rem ------------------------------------------------------------
rem This batch simulates all FMUs of the FmuSDK and stores
rem simulation results in CSV files, one per simulation run.
rem (c) 2011 QTronic GmbH
rem ------------------------------------------------------------

echo -----------------------------------------------------------
echo Runnig all FMUs of the FmuSDK ...

echo -----------------------------------------------------------
call fmusim me fmu\me\bouncingBall.fmu 4 0.01 0 c
move /Y result.csv result_me_bouncingBall.csv 

echo -----------------------------------------------------------
call fmusim cs fmu\cs\bouncingBall.fmu 4 0.01 0 c
move /Y result.csv result_cs_bouncingBall.csv 

echo -----------------------------------------------------------
call fmusim me fmu\me\vanDerPol.fmu 5 0.1 0 c
move /Y result.csv result_me_vanDerPol.csv 

echo -----------------------------------------------------------
call fmusim cs fmu\cs\vanDerPol.fmu 5 0.1 0 c
move /Y result.csv result_cs_vanDerPol.csv 

echo -----------------------------------------------------------
call fmusim me fmu\me\dq.fmu 1 0.1 0 c
move /Y result.csv result_me_dq.csv 

echo -----------------------------------------------------------
call fmusim cs fmu\cs\dq.fmu 1 0.1 0 c
move /Y result.csv result_cs_dq.csv 

echo -----------------------------------------------------------
call fmusim me fmu\me\inc.fmu 15 15 0 c
move /Y result.csv result_me_inc.csv 

echo -----------------------------------------------------------
call fmusim cs fmu\cs\inc.fmu 15 0.5 0 c
move /Y result.csv result_cs_inc.csv 

echo -----------------------------------------------------------
call fmusim me fmu\me\values.fmu 12 12 0 c
move /Y result.csv result_me_values.csv 

echo -----------------------------------------------------------
call fmusim cs fmu\cs\values.fmu 12 0.3 0 c
move /Y result.csv result_cs_values.csv 
