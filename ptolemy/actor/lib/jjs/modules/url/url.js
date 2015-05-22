/**
 * This module has utilities for URL resolution and parsing. Call require('url') to use it..
 * @module url
 * @author Marten Lohstroh
 * @copyright http://terraswarm.org/accessors/copyright.txt
 *
 * This module provides a Nashorn implementation the Node.js url module.
 */

// Java types used.
var URL = Java.type('java.net.URL');

exports.parse = urlParse;
exports.resolve = urlResolve;
exports.resolveObject = urlResolveObject;
exports.format = urlFormat;

exports.Url = Url;

/** 
 *  This class provides the following functions:
 *  <ul>
 *  <li> '''format'''(''urlObj''): Take a parsed URL object, and return a 
        formatted URL string. </li>
 *  <li> '''parse'''(''urlStr''): Take a URL string, and return an object. </li>
 *  <li> '''resolve'''(''from'', ''to''): Take a base URL, and a href URL, and 
         resolve them as a browser would for an anchor tag. </li>
 *  </ul>
 */
function Url() {
  this.protocol = null;
  this.slashes = null;
  this.auth = null;
  this.host = null;
  this.port = null;
  this.hostname = null;
  this.hash = null;
  this.search = null;
  this.query = null;
  this.pathname = null;
  this.path = null;
  this.href = null;
}

