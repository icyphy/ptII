// JavaScript definition of accessor-specific JSDoc tags.
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

// Portions of this file are based on
// jsdoc/lib/jsdoc/tag/dictionary/definitions.js, which is Apache
// License-2.0

/** JavaScript definition of accessor-specific JSDoc tags.
 *
 * See <a href="http://usejsdoc.org/about-plugins.html">http://usejsdoc.org/about-plugins.html</a>.
 *
 * For about how to use this, see <a href="https://chess.eecs.berkeley.edu/ptexternal/wiki/Main/JSDocSystems#JSDocCustomTagPlugin">https://chess.eecs.berkeley.edu/ptexternal/wiki/Main/JSDocSystems#JSDocCustomTagPlugin</a>
 *
 * This plugin uses a fork of JSDoc, see https://github.com/terraswarm/jsdoc
 *
 * Running "cd accessors/web; ant jsdoc" will clone the above repo for you.
 *
 * @author Christopher Brooks
 * @version $$Id$$
 * @since Ptolemy II 11.0
 */

/*global console, exports, getSourcePaths, require, writePtDoc, xmlEscape */
/*jshint globalstrict: true, multistr: true */
'use strict';

var path = require('path');

//Start of text from jsdoc/lib/jsdoc/tag/dictionary/definitions.js
function filepathMinusPrefix(filepath) {
    var sourcePaths = getSourcePaths();
    var commonPrefix = path.commonPrefix(sourcePaths);
    var result = '';

    if (filepath) {
        filepath = path.normalize(filepath);
        // always use forward slashes in the result
        result = (filepath + path.sep).replace(commonPrefix, '')
            .replace(/\\/g, '/');
    }

    if (result.length > 0 && result[result.length - 1] !== '/') {
        result += '/';
    }

    return result;
}
//end of text from jsdoc/lib/jsdoc/tag/dictionary/definitions.js

exports.defineTags = function (dictionary) {
    dictionary.defineTag("accessor", {
        mustHaveValue: true,
        isNamespace: true, // Needed to get "accessor-" into the file name.
        onTagged: function (doclet, tag) {
            //console.log("accessorJSDocTags.js: accessor: " + doclet + " " + tag);

            // Start of text from jsdoc/lib/jsdoc/tag/dictionary/definitions.js

            // setDocletKindToTitle is not declared here, so we do what it says:
            //setDocletKindToTitle(doclet, tag);
            doclet.addTag('kind', tag.title);

            // setDocletNameToValue(doclet, tag);
            if (tag.value && tag.value.description) { // as in a long tag
                doclet.addTag('name', tag.value.description);
            } else if (tag.text) { // or a short tag
                doclet.addTag('name', tag.text);
            }

            if (!doclet.name) {
                // setDocletNameToFilename(doclet, tag);
                var name = '';

                if (doclet.meta.path) {
                    name = filepathMinusPrefix(doclet.meta.path);
                }
                name += doclet.meta.filename.replace(/\.js$/i, '');

                doclet.name = name;
            }
            // Not sure if we need this:
            // in case the user wrote something like `/** @accessor  accessor:foo */`:
            //doclet.name = stripModuleNamespace(doclet.name);

            // setDocletTypeToValueType(doclet, tag);
            if (tag.value && tag.value.type) {
                // Add the type names and other type properties (such as `optional`).
                // Don't overwrite existing properties.
                Object.keys(tag.value).forEach(function (prop) {
                    if (!hasOwnProperty.call(doclet, prop)) {
                        doclet[prop] = tag.value[prop];
                    }
                });
            }

            // End of text from jsdoc/lib/jsdoc/tag/dictionary/definitions.js

            doclet.accessor = tag.name;
        }
    });
    dictionary.defineTag("input", {
        mustHaveValue: true,
        canHaveType: true,
        canHaveName: true,
        onTagged: function (doclet, tag) {
            if (!doclet.inputs) {
                doclet.inputs = [];
            }
            doclet.inputs.push(tag.value);
        }
    });
    dictionary.defineTag("output", {
        mustHaveValue: true,
        canHaveType: true,
        canHaveName: true,
        onTagged: function (doclet, tag) {
            if (!doclet.outputs) {
                doclet.outputs = [];
            }
            doclet.outputs.push(tag.value);
        }
    });
    dictionary.defineTag("parameter", {
        mustHaveValue: true,
        canHaveType: true,
        canHaveName: true,
        onTagged: function (doclet, tag) {
            if (!doclet.parameters) {
                doclet.parameters = [];
            }
            doclet.parameters.push(tag.value);
        }
    });
};
