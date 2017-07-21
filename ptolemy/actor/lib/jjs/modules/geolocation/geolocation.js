// geolocation module for CapeCode accessors host.
// 
// Below is the copyright agreement for the Ptolemy II system.
//
// Copyright (c) 2015-2016 The Regents of the University of California.
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

/** Module for retrieving geolocation information, based on the device's
 *  IP address. An http get request returns the position.
 *  The exported function is 'getPosition', that has as parameters:
 *  ** the function to execute in case of success,
 *  ** the function to execute in case an error occured
 *  ** and the options, not applicable in this case.
 *  
 *  If the status code of the response is OK (200) then a location object, 
 *  with longitude, latitude, accuracy and timestamp attributes, is passed 
 *  to the caller onSuccess function. In case of error, the caller onError
 *  function is called on the error.
 *
 *  @module geolocation
 *  @author Chadlia Jerad
 *  @version $$Id: geolocation.js 75980 2017-07-18 00:19:25Z chadlia.jerad $$
 */

var httpClient = require('@accessors-modules/httpClient');

exports.getPosition = function(onSuccess, onError, options) {

	httpClient.get('https://ipinfo.io/json', function(response) {
		if (response.statusCode == 200) {
			var location = {};
			var loc = JSON.parse(response.body).loc;
			loc = loc.split(',');
			location.latitude = Number(loc[0]);
			location.longitude = Number(loc[1]);
		   	location.accuracy = 'low';  
		   	location.timestamp = response.headers.date;
			onSuccess.call(this, location);
		} else {
			onError.call(this, response.statusCode);
		};
	});
}
