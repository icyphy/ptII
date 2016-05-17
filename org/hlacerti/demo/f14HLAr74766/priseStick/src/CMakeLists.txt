###################################################################################
###################################################################################
##################### CMakeLists.txt for PRISE FEDERATION #########################
###################################################################################
###################################################################################
#############         PROCEDURE COMPILATION LINUX              ####################
###################################################################################
# depuis le repertoire /CheminSource : ############################################
###################################################################################
#  ** STEP 1 ** : Chargement du fichier de configuration du CERTI               ###
#                                                                               ###
#  >> source /CheminConfigurationCerti/myCERTIenv.sh                            ###
###################################################################################
## Rappel : #######################################################################
#  -------- #######################################################################
#  /CheminConfigurationCerti = Rep ou sont les fichiers de config du CERTI ########
#  Generalement /CERTI/certi_run/share/scripts/ ###################################
#  NomFichierConf.extension = myCERTIenv.sh ou myCERTIenv.csh selon les shells ####
# #################################################################################
###################################################################################
# ** STEP 2 ** : Configuration de la chaine de compilation du repertoire        ###
#                source vers le repertoire de Run en passant par le repertoire  ###
#                de construction                                                ### 
#                                                                               ###
#  >> cd /CheminConstruction                                                    ###
#  >> cmake -DCMAKE_INSTALL_PREFIX=/CheminExecutable /CheminSources             ###
###################################################################################
## Rappel : #######################################################################
#  -------- #######################################################################
#  /CheminConstruction = Rep ou on veut mettre les fichiers de Construction #######
## >> cd CheminConstruction selon ou on on est dans le terminal ###################
#  /CheminExecutable = Rep pour installation Binaires/Fichiers ####################
#  /CheminSources = Rep ou sont les fichiers Sources ##############################
###################################################################################
# ** STEP 3 ** : Compilation et Installation                                    ###
#                                                                               ###
#  >> make                                                                      ###
#  >> make install                                                              ###
###################################################################################
#########           FIN PROCEDURE COMPILATION LINUX          ######################
###################################################################################


###################################################################################
########################### COMMANDES CMAKE #######################################
###################################################################################
# NOM DU PROJET : #################################################################
PROJECT(PRISE_HLA-PROJECT)

###################################################################################
################ Setup reasonable default CMake behavior ##########################
###################################################################################
###################################################################################
#################### CERTI requires CMake 2.4.x version ###########################
CMAKE_MINIMUM_REQUIRED(VERSION 2.4.4)



##################### WARNING POLICY ##############################################
CMAKE_POLICY(SET CMP0014 OLD)
cmake_policy(SET CMP0003 NEW)
###################################################################################

###################################################################################
####################  Use CERTI specific CMake modules first  #####################
#####           You should set CERTI_HOME env var to appropriate value ############
######### or put here the full path to the directory containing FindRTI.cmake #####
#####################  Use CERTI_HOME env var if it is defined ####################
###################################################################################
IF ("$ENV{CERTI_HOME}" STRGREATER "")
    FILE(TO_CMAKE_PATH "$ENV{CERTI_HOME}" CERTI_HOME_TEMP)
    SET(CERTI_HOME ${CERTI_HOME_TEMP} CACHE PATH "The CERTI install directory")
    LIST(APPEND CMAKE_MODULE_PATH "${CERTI_HOME}/share/scripts")

################################################################################
# Or try to use CERTI possible installed at the same INSTALL_PREFIX
#################################################################################
ELSE ("$ENV{CERTI_HOME}" STRGREATER "")
    LIST(APPEND CMAKE_MODULE_PATH "${CMAKE_INSTALL_PREFIX}/share/scripts")    
ENDIF ("$ENV{CERTI_HOME}" STRGREATER "")
#####
#SET(CMAKE_MODULE_PATH $ENV{CERTI_HOME}/share/scripts)

################################################################################
################### For configuration of library for root    ###################
####################                                         ###################
################################################################################
OPTION(USE_FULL_RPATH
             "Use the full RPATH" OFF)
             
IF (USE_FULL_RPATH)       
    # see http://www.cmake.org/Wiki/CMake_RPATH_handling#Always_full_RPATH
    # use, i.e. don't skip the full RPATH for the build tree
    SET(CMAKE_SKIP_BUILD_RPATH  FALSE)
    
    # when building, don't use the install RPATH already
    # (but later on when installing)
    SET(CMAKE_BUILD_WITH_INSTALL_RPATH FALSE) 
    
    # the RPATH to be used when installing
    SET(CMAKE_INSTALL_RPATH "${CMAKE_INSTALL_PREFIX}/lib")
    
    # add the automatically determined parts of the RPATH
    # which point to directories outside the build tree to the install RPATH
    SET(CMAKE_INSTALL_RPATH_USE_LINK_PATH TRUE)
ENDIF(USE_FULL_RPATH)

################################################################################
################# You should uncomment the following line ######################
###################### if you want VERBOSE build ###############################
################################################################################
#SET(CMAKE_VERBOSE_MAKEFILE ON)

################################################################################
################### The preferred (default) behaviour is to ###################
#################### build library as shared on all platform ###################
##################################################################################
OPTION(BUILD_SHARED
  "Build libraries as shared library" ON)
IF (BUILD_SHARED)
  SET(BUILD_SHARED_LIBS ON)
ELSE (BUILD_SHARED)
  SET(BUILD_SHARED_LIBS OFF)
ENDIF (BUILD_SHARED)

##################################################################################
##################### Change the build type to release ###########################
####################### when you are ready to release ############################
##################################################################################
SET(CMAKE_BUILD_TYPE "Debug")
###
#SET(CMAKE_BUILD_TYPE "Release")

#########################################################################
############## Define your project packaging basics #####################
#########################################################################
SET(PACKAGE_NAME      ${CMAKE_PROJECT_NAME})
SET(PACKAGE_VERSION_MAJOR "1")
SET(PACKAGE_VERSION_MINOR "0")
SET(PACKAGE_VERSION_PATCH "1")
SET(PACKAGE_VERSION   "${PACKAGE_VERSION_MAJOR}.${PACKAGE_VERSION_MINOR}.${PACKAGE_VERSION_PATCH}")
###
SET(PACKAGE_BUGREPORT "jean-baptiste.chaudron@onera.fr")
SET(PACKAGE_BUGREPORT "david.saussie@polymtl.ca")
MESSAGE(STATUS "Configuring ${PACKAGE_NAME} version ${PACKAGE_VERSION}")

#########################################################################
###################   Call i.e. FIND RTI   ##############################
#########################################################################
FIND_PACKAGE(RTI REQUIRED)
INCLUDE_DIRECTORIES(${RTI_INCLUDE_DIR})
ADD_DEFINITIONS(${RTI_DEFINITIONS})

# libHLA missing from RTI_LIBRARIES, adding it manually
execute_process(COMMAND cat  /etc/fedora-release
                OUTPUT_VARIABLE FEDORA_VERSION)

MESSAGE(STATUS ${FEDORA_VERSION})

#IF (${FEDORA_VERSION} STREQUAL "Fedora release 13 (Goddard)")
	FIND_LIBRARY(HLA_LIBRARY libHLAd.so ${CERTI_HOME}/lib)
	MESSAGE(STATUS "HLA library : ${HLA_LIBRARY}")
	#SET(RTI_LIBRARIES "${RTI_LIBRARIES};${HLA_LIBRARY}")

	MESSAGE(STATUS "RTI_LIBRARIES : ${RTI_LIBRARIES}")
	SET(LIBRT "/lib64/librt.so.1")
#ENDIF (${FEDORA_VERSION} STREQUAL "Fedora release 13 (Goddard)")


MESSAGE(STATUS "* CERTI FOUND *")

#########################################################################
###################   Call i.e. FIND Qt4   ##############################
#########################################################################

FIND_PACKAGE(Qt4)
if (QT4_FOUND)
	SET(QT_USE_QTWEBKIT TRUE)
	SET(QT_USE_QTNETWORK TRUE)
	SET(QT_USE_QTSVG TRUE)
	INCLUDE(${QT_USE_FILE})
	ADD_DEFINITIONS(${QT_DEFINITIONS})
	MESSAGE(STATUS "* Qt4 FOUND *")
endif (QT4_FOUND)

if (NOT QT4_FOUND)
	MESSAGE(STATUS "* Qt4 NOT FOUND, GUI NOT ACTIVATED *")
endif (NOT QT4_FOUND)

#########################################################################
#################### CPack specific Configuration #######################
#########################################################################
SET(CPACK_PACKAGE_DESCRIPTION_SUMMARY "Federation PRISE HLA using CERTI")
###
#SET(CPACK_PACKAGE_DESCRIPTION_FILE "${CMAKE_CURRENT_SOURCE_DIR}/HLA_Tutorial_DESCRIPTION.txt")
###
#SET(CPACK_RESOURCE_FILE_LICENSE "${CMAKE_CURRENT_SOURCE_DIR}/COPYING")
SET(CPACK_PACKAGE_NAME ${CMAKE_PROJECT_NAME})
SET(CPACK_SYSTEM_NAME ${CMAKE_SYSTEM_NAME})
#########################################################################
# Avoid putting BUILDIR inside source package (simple case)
#########################################################################
IF ("${CMAKE_BINARY_DIR}" STREQUAL "${CMAKE_SOURCE_DIR}")
     SET(CPACK_SOURCE_IGNORE_FILES "/CVS/;/\\\\.svn/;\\\\.swp$;\\\\.#;/#;.*~;cscope.*;\\\\.cproject;\\\\.project;autom4te.cache/*")
ELSE ("${CMAKE_BINARY_DIR}" STREQUAL "${CMAKE_SOURCE_DIR}")
     SET(CPACK_SOURCE_IGNORE_FILES "/CVS/;/\\\\.svn/;\\\\.swp$;\\\\.#;/#;.*~;cscope.*;\\\\.cproject;\\\\.project;autom4te.cache/*;${CMAKE_BINARY_DIR}/*")
ENDIF ("${CMAKE_BINARY_DIR}" STREQUAL "${CMAKE_SOURCE_DIR}")

SET(CPACK_SOURCE_GENERATOR "ZIP;TGZ")
IF (WIN32)
  SET(CPACK_GENERATOR "ZIP;NSIS")
  #########################################################################
  ## Add a custom target callable from IDE (Mostly for Visual Studio)
  #########################################################################
  GET_FILENAME_COMPONENT(CPACK_COMMAND ${CMAKE_COMMAND} PATH)
  SET(CPACK_COMMAND ${CPACK_COMMAND}/cpack)
  ADD_CUSTOM_TARGET(PACKAGE_SOURCE_ZIP
              COMMAND ${CPACK_COMMAND} -G ZIP --config CPackSourceConfig.cmake
              COMMENTS "Build a ZIP file containing the source")  
  ##############################################################            
  # NSIS installer specific part
  ##############################################################
  INCLUDE(InstallRequiredSystemLibraries)
ELSE (WIN32)
  SET(CPACK_GENERATOR "ZIP;TGZ")
ENDIF(WIN32)
SET(CPACK_PACKAGE_VERSION ${PACKAGE_VERSION})
SET(CPACK_PACKAGE_VERSION_MAJOR ${PACKAGE_VERSION_MAJOR})
SET(CPACK_PACKAGE_VERSION_MINOR ${PACKAGE_VERSION_MINOR})
SET(CPACK_PACKAGE_VERSION_PATCH ${PACKAGE_VERSION_PATCH})

INCLUDE(CPack)

############################################################################################
################### CONFIGURATION DES SOUS-REPERTOIRES SOURCES ET INCLUDE ##################
############################################################################################



# FOR 1_JOYSTICK FEDERATE ##################################################
SET(1_JOYSTICK_FED_SOURCE_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/1_JOYSTICK/Sources)
SET(1_JOYSTICK_FED_INCLUDE_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/1_JOYSTICK/Includes)
ADD_SUBDIRECTORY(${1_JOYSTICK_FED_SOURCE_DIRECTORY})
ADD_SUBDIRECTORY(${1_JOYSTICK_FED_INCLUDE_DIRECTORY})
INCLUDE_DIRECTORIES(${1_JOYSTICK_FED_SOURCE_DIRECTORY}
                    ${1_JOYSTICK_FED_INCLUDE_DIRECTORY}
		    ${CMAKE_CURRENT_SOURCE_DIR}/XML/Includes
		    ${CMAKE_CURRENT_SOURCE_DIR}/XML/Sources)
									  


# FOR COMMON SOURCES FILES ##################################################
SET(COMMON_SOURCE_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/COMMON/Sources)
SET(COMMON_INCLUDE_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/COMMON/Includes)
ADD_SUBDIRECTORY(${COMMON_SOURCE_DIRECTORY})
ADD_SUBDIRECTORY(${COMMON_INCLUDE_DIRECTORY})
INCLUDE_DIRECTORIES(${COMMON_SOURCE_DIRECTORY} ${COMMON_INCLUDE_DIRECTORY})


# FOR XML DEPENDENCES ##################################################
SET(XML_SOURCE_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/XML/Sources)
SET(XML_INCLUDE_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/XML/Includes)
ADD_SUBDIRECTORY(${XML_SOURCE_DIRECTORY})
ADD_SUBDIRECTORY(${XML_INCLUDE_DIRECTORY})
INCLUDE_DIRECTORIES(${XML_SOURCE_DIRECTORY}
                    ${XML_INCLUDE_DIRECTORY})

##########################################################################
####### CONFIGURATION DES CHEMINS POUR LES EXE & FICHIERS_CONF ###########
##########################################################################
SET(INIT_FILES_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/INIT_FILES)

####################################################################################
####################################################################################
###***************** COMPILATION ET CREATION DES FEDERES *********************######
####################################################################################
####################################################################################




####################################################################################
##################  SETUP 1_JOYSTICK FEDERATE (FED 1) #############################
####################################################################################

###########   the executable name & the source list  ############################
SET(EXE_NAME Joystick_Fed)
SET(SRC_LIST ${1_JOYSTICK_FED_SOURCE_DIRECTORY}/Joystick_Fed_Main.cc
             ${1_JOYSTICK_FED_SOURCE_DIRECTORY}/Joystick_Fed.cc
             ${1_JOYSTICK_FED_SOURCE_DIRECTORY}/YokePedalSystem.cc
             ${COMMON_SOURCE_DIRECTORY}/Timer.cc
             ${XML_SOURCE_DIRECTORY}/tinyxml2.cpp
             ${XML_SOURCE_DIRECTORY}/prise_parameters_parser.cpp)

################## Generic CMake executable target ###############################
ADD_EXECUTABLE(${EXE_NAME} ${SRC_LIST})
TARGET_LINK_LIBRARIES (${EXE_NAME} ${RTI_LIBRARIES} ${HLA_LIBRARY} ${LIBRT})
INSTALL(TARGETS ${EXE_NAME} 
        RUNTIME DESTINATION bin)
        

##################################################################################
##################################################################################
####################### Executable & Files Installation ##########################
##################################################################################

INSTALL(FILES 
	${XML_SOURCE_DIRECTORY}/prise_parameters.xml
        DESTINATION bin)

INSTALL(FILES 
	${INIT_FILES_DIRECTORY}/PRISE_V2.fed
        DESTINATION bin)

INSTALL(FILES 
	${INIT_FILES_DIRECTORY}/Distributed_Run_bash.sh
        DESTINATION bin)

INSTALL(FILES 
	${INIT_FILES_DIRECTORY}/Local_Run_bash.sh
        DESTINATION bin)

# Ne pas oublier de faire un >> chmod 700 Run_bash.sh

##################################################################################
# Pour installer le fichier fed dans le repertoire approprié du CERTI
##################################################################################
INSTALL(FILES 
	${INIT_FILES_DIRECTORY}/PRISE_V2.fed
        DESTINATION ${CERTI_HOME}/share/federations)


######################  MESSAGE DE FIN DE CONFIGURATION   ########################
MESSAGE(STATUS "**********************************************************************************")
MESSAGE(STATUS "**********************************************************************************")
MESSAGE(STATUS "**********                                                        ****************")
MESSAGE(STATUS "********** ${CMAKE_PROJECT_NAME} has been successfully configured ****************")
MESSAGE(STATUS "**********                                                        ****************")
MESSAGE(STATUS "**********************************************************************************")
MESSAGE(STATUS "**********************************************************************************")

###################################################################################
########################### FIN COMMANDES CMAKE ###################################
###################################################################################
