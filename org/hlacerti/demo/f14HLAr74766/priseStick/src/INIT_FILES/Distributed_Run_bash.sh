# CHANGE HERE ACCORDING TO YOUR ENVIRONNMENT

export CERTI_RUN_DIR=$HOME/CERTI/certi_run
export QT_ENV_SCRIPT_DIR=$HOME
export SDSE_BIN_DIR=$HOME/projet/s-3a-in326-hla-prise/run/bin
export CERTI_HOST=ihawk

# Export CERTI paths
source $CERTI_RUN_DIR/share/scripts/myCERTI_env.sh

# Configuration environnement QT
source $QT_ENV_SCRIPT_DIR/my_QT_env.sh

# On laisse au systeme le temps d'ouvrir des shells
SLEEP_TIME=0.1

sleep $SLEEP_TIME
gnome-terminal --working-directory=$SDSE_BIN_DIR -t "FED 0 : MAIN_CONTROLLER" -e ./Main_Controller_Fed &
sleep $SLEEP_TIME
gnome-terminal --working-directory=$SDSE_BIN_DIR -t "FED 1 : JOYSTICK_FED" -e ./Joystick_Fed &
sleep $SLEEP_TIME
gnome-terminal --working-directory=$SDSE_BIN_DIR -t "FED 9 : JAVA_PFD_FED" -e 'java -jar PFD.jar' &
sleep $SLEEP_TIME
gnome-terminal --working-directory=$SDSE_BIN_DIR -t "FED 2 : COCKPIT_FED" -e ./Cockpit_Fed &
