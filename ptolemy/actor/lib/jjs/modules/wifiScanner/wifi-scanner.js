// wifi-scanner module for cordova accessors host.
// 
// Below is the copyright agreement for the Ptolemy II system.
//
// Copyright (c) 2017 The Regents of the University of California.
// All rights reserved.
//
// Permission is hereby granted, without written agreement and without
// license or royalty fees, to use, copy, modify, and distribute this
// software and its documentation for any purpose, provided that the above
// copyright notice and the following two paragraphs appear in all copies
// of this software.
//
// IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
// FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.
//
// THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
// INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
// ENHANCEMENTS, OR MODIFICATIONS.
//

/**
 *  Module for retrieving wifiScan information from an android phone. This module throws
 *  an error if startScan is called on any other platform because
 *  iOS is not supported by the Cordova plugin used by this module. According to the
 *  wifiWizard github project at https://github.com/hoerresb/WifiWizard:
 *
 *  "iOS has limited functionality, as Apple's WifiManager equivalent is only available
 *  as a private API. Any app that used these features would not be allowed on the app 
 *  store. The only function availabe for iOS is getCurrentSSID"
 *
 *  @module wifiScanner
 *  @author Matt Weber
 *  @version $$Id$$
 */

// Stop extra messages from jslint and jshint.  Note that there should
// be no space between the / and the * and global. See
// https://chess.eecs.berkeley.edu/ptexternal/wiki/Main/JSHint */
/*globals addInputHandler, console, get, getParameter, getResource, error, exports, extend, get, input, output, parameter, require, send */
/*jshint globalstrict: true*/

'use strict';

// exports.requiredPlugins = ['com.pylonproducts.wifiwizard', 'cordova-plugin-device', 'cordova-plugin-android-permissions'];

//The WifiWizard module (https://github.com/hoerresb/WifiWizard) has two steps in obtaining scan data:
//first call "startScan" and then after a timeout, call getScanResults.
//I can think of no scenario where an accessor would want to directly control this timeout
//to something other than the least value that yields successful results
//or start a scan without getting the scan results so I have elected to
//combine both functions and specify the timeout inside this module as the following
//magic number. Units are ms.
var scanTimeout = 10;


/** This function will first check that the necessary location
 *   permissions have been acquired.  If permissions are successfully
 *   acquired or have already been acquired, this function will call
 *   the onSuccess callback (with no arguments).  If permssions are not
 *   successfully acquired this function will call the onFailure
 *   callback (with no arguments).  If either of the android permission
 *   requests produce an error (which is not the same as the user
 *   rejecting permssions) this function throws an error.
 *
 *   @param onSuccess The callback function to be executed upon
 *   successful acquisition of location permissions
 *   @param onFailure The callback function to be executed if location
 *                    permissions are not already given and the user
 *                    rejects the location permission request
 */
function checkAndGetLocationPermissions(onSuccess, onFailure) {
    var permissions = cordova.plugins.permissions;
    var locPermission = permissions.ACCESS_COARSE_LOCATION;
    permissions.checkPermission(locPermission,
                                function(checkStatus) {
                                    if(checkStatus.hasPermission){
                                        onSuccess();
                                    } else {
                                        permissions.requestPermission(locPermission,
                                                                      function(requestStatus){
                                                                          if(requestStatus.hasPermission){
                                                                              onSuccess();
                                                                          } else {
                                                                              onFailure();
                                                                          }
                                                                      },
                                                                      function(){
                                                                          throw "Unable to check permissions in wifiScanner Module";
                                                                      }
                                                                     );
                                    }
                                },
                                function() {
                                    throw "Unable to check permissions in wifiScanner Module";
                                }
                               );
}


/** Retrieves a list of the available networks as an array of objects and passes them 
 *   to the callback function listHandler(networks). The format of the array is:
 *  
 *   networks = [
 *      {   "level": signal_level, // raw RSSI value
 *          "SSID": ssid, // SSID as string, with escaped double quotes: "\"ssid name\""
 *          "BSSID": bssid // MAC address of WiFi router as string
 *          "frequency": frequency of the access point channel in MHz
 *          "capabilities": capabilities // Describes the authentication, key management, and encryption schemes supported by the access point.
 *      }
 *  ]
 *  
 *  @param listHandler The callback function that is passed the aforementioned networks array as it's first argument
 */

exports.scan = function(listHandler) {
    if( device.platform!== 'Android'){
        console.log("device is not an android!!");
        console.log("device is a: " + device.model );
        throw "'wifiScanner module's scan function is only supported on Android. Your platform is " + device.model ;
    }

    checkAndGetLocationPermissions(
        function(){
            WifiWizard.startScan( 
                function(){
                    setTimeout(
                        WifiWizard.getScanResults(
                            {numLevels: false},
                            listHandler,
                            function(){
                                throw "Cannot complete scan in wifiScanner: Error calling WifiWizard.getScanResults.";
                            }),
                        scanTimeout);
                },
                function(){
                    throw "Cannot complete scan in wifiScanner: Error calling WifiWizard.startScan.";
                }
            );
        },

        //FIXME, replace this function with a more intelligent mechanism for dealing with
        //the user rejecting a necessary permission. Maybe this shouldn't be a show stopper.
        function(){
            throw 'Unable to continue startScan in wifiScanner because the user has rejected required location permissions';
        }
    );
};
