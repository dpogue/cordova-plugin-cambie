---
license: Copyright 2013 Darryl Pogue

         Licensed under the Apache License, Version 2.0 (the "License");
         you may not use this file except in compliance with the License.
         You may obtain a copy of the License at

           http://www.apache.org/licenses/LICENSE-2.0

         Unless required by applicable law or agreed to in writing, software
         distributed under the License is distributed on an "AS IS" BASIS,
         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
         See the License for the specific language governing permissions and
         limitations under the License.
---

setApplicationMenus
====================

Parses the menu structure out of the scope element and load it into the native UI.

    navigator.setApplicationMenus([scopeElement], [success], [error]);

- __scopeElement:__ Root element for menu parsing. (`HTMLElement`) (Optional, Default: `document.body`)
- __success:__ Success callback. (`Function`)
- __error:__ Error callback. (`Function`)

Description
-----------

The `setApplicationMenus` method parses the menu structure out of the scope
element and load it into the native UI. If no scope element is specified, it
defaults to the document body.

The success and error parameters are callbacks that will be called either when
parsing the menu structure is complete or when there is an error in parsing the
menu structure respectively.

Supported Platforms
-------------------

- Android

