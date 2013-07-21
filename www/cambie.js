/**
 * Copyright 2013 Darryl Pogue
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var exec = require('cordova/exec');


/**
 * Polyfill for the HTMLElement click() method.
 */
if (typeof HTMLElement !== 'undefined' && !HTMLElement.prototype.click) {
    HTMLElement.prototype.click = function() {
        var evt = this.ownerDocument.createEvent('MouseEvents');
        evt.initMouseEvent('click', true, true, this.ownerDocument.defaultView, 1, 0, 0, 0, 0, false, false, false, false, 0, null);
        this.dispatchEvent(evt);
    }
}


/**
 * Our global-ish parsing function that takes an element representing a page
 * and generates the JSON structure that we need.
 */
var parsePageNavigation = function(page_el, success, error) {
    if (typeof page_el === 'undefined') {
        page_el = document.body;
    }

    if (typeof success === 'undefined') {
        success = function() { }
    }

    if (typeof error === 'undefined') {
        error = function(err) { console.error('Cambie error! ' + err); }
    }

    var header = page_el.querySelector('header');
    var footer = page_el.querySelector('footer');
    var jsobj = {};


    jsobj['title'] = (function(el) {
        // Default to the <title> tag
        var title = document.title;

        if (el.getAttribute('data-title')) {
            title = el.getAttribute('data-title');
        }

        if (el.getAttribute('title')) {
            title = el.getAttribute('title');
        }

        for (var i = 6; i > 0; --i) {
            var heading = el.querySelector('h'+i);

            if (heading) {
                title = heading.textContent;
            }
        }

        return title;
    })(header);


    jsobj['nav'] = (function(el) {
        var nav = (el.getAttribute('data-nav') || 'none').toLowerCase();

        if (nav != 'none' && nav != 'back' && nav != 'menu' && nav != 'cancel') {
            console.error('Invalid navigation type: ' + nav);
            return 'none';
        }

        return nav;
    })(header);


    if (header) {
        var menu = header.querySelector('menu[type="popup"]');
    } else {
        var menu = null;
    }
    jsobj['menu'] = (function(el) {
        var menuitems = [];

        var global_menu = document.querySelector('body > menu[type=popup]')

        if (!el && !global_menu) { return menuitems; }

        var children = el.querySelectorAll('menuitem');
        for (var i = 0, ii = children.length; i < ii; ++i) {
            var mi = {};

            var child = children[i];

            mi['label']     = child.getAttribute('label');
            mi['icon']      = child.getAttribute('icon');
            mi['disabled']  = !!child.getAttribute('disabled');

            var callbackId = 'MenuClick' + cordova.callbackId++;
            cordova.callbacks[callbackId] = {
                success:    (function(target) {
                                return function() { target.click(); }
                            })(child),
                fail:       error
            };
            mi['callback'] = callbackId;

            menuitems.push(mi);
        }

        if (global_menu) {
            var children = global_menu.querySelectorAll('menuitem');
            for (var i = 0, ii = children.length; i < ii; ++i) {
                var mi = {};

                var child = children[i];

                mi['label']     = child.getAttribute('label');
                mi['icon']      = child.getAttribute('icon');
                mi['disabled']  = !!child.getAttribute('disabled');

                var callbackId = 'MenuClick' + cordova.callbackId++;
                cordova.callbacks[callbackId] = {
                    success:    (function(target) {
                                    return function() { target.click(); }
                                })(child),
                    fail:       error
                };
                mi['callback'] = callbackId;

                menuitems.push(mi);
            }
        }

        return menuitems;
    })(menu);


    if (header) {
        var actions = header.querySelector('menu[type="toolbar"]');
    } else {
        var actions = null;
    }
    jsobj['actions'] = (function(el) {
        var items = [];

        if (!el) { return items; }

        var children = el.querySelectorAll('a, button');
        for (var i = 0, ii = children.length; i < ii; ++i) {
            var mi = {};

            var child = children[i];

            mi['label']     = child.textContent;
            mi['icon']      = child.getAttribute('icon');
            mi['disabled']  = !!child.getAttribute('disabled');

            var callbackId = 'ActionClick' + cordova.callbackId++;
            cordova.callbacks[callbackId] = {
                success:    (function(target) {
                                return function() { target.click(); }
                            })(child),
                fail:       error
            };
            mi['callback'] = callbackId;

            items.push(mi);
        }

        return items;
    })(actions);



    if (footer) {
        var actions = footer.querySelector('menu[type="toolbar"]');
    } else {
        var actions = null;
    }
    jsobj['tabs'] = (function(el) {
        var items = [];

        if (!el) { return items; }

        var children = el.querySelectorAll('a, button');
        for (var i = 0, ii = children.length; i < ii; ++i) {
            var mi = {};

            var child = children[i];

            mi['label']     = child.textContent;
            mi['icon']      = child.getAttribute('icon');
            mi['disabled']  = !!child.getAttribute('disabled');

            var callbackId = 'TabClick' + cordova.callbackId++;
            cordova.callbacks[callbackId] = {
                success:    (function(target) {
                                return function() { target.click(); }
                            })(child),
                fail:       error
            };
            mi['callback'] = callbackId;

            items.push(mi);
        }

        return items;
    })(actions);

    // Hide the header and footer since they're native now
    if (header) {
        header.style.display = 'none';
    }
    if (footer) {
        footer.style.display = 'none';
    }

    exec(success, error, 'Cambie', 'update', [jsobj]);
};


var Cambie = function() {
    exec(function() { }, function() { }, 'Cambie', 'init', []);

    return {
        setApplicationMenus:    parsePageNavigation,

        hideApplicationMenus:   function() {
            exec(function() { }, function() { }, 'Cambie', 'hide', []);
        },

        showApplicationMenus:   function() {
            exec(function() { }, function() { }, 'Cambie', 'show', []);
        }
    };
}

module.exports = new Cambie();
