# Cambie
A unified native UI plugin for Apache Cordova 3.x.  
This is *highly* experimental right now, and should probably not be used in
production projects. The API is guaranteed to change as it is refined and
expanded to other platforms.

The target platforms for Cambie are:

* Android 4.x+

It is a goal to also target these platforms wherever possible:

* Android 2.3
* BlackBerry OS 10
* Windows Phone 8
* iOS 5.x+
* Firefox OS 1.x+


## Installing
Using the cordova CLI tools, run this in your project directory:  
`$ cordova plugin install https://github.com/dpogue/cordova-plugin-cambie.git`

Using plugman directly, run this in your project directory:  
`$ plugman --platform android --project . --plugin https://github.com/dpogue/cordova-plugin-cambie.git`


## Cambie API

### hideApplicationMenus
`navigator.hideApplicationMenus();`

Hides the native application chrome for title bars, tabs, menus, and drawers.

### showApplicationMenus
`navigator.showApplicationMenus();`

Shows the native application chrome for title bars, tabs, menus, and drawers.
This only makes the native UI visible, it does not affect the contents of it.

### setApplicationMenus
`navigator.setApplicationMenus([scope element], [success], [error]);`

Parses the menu structure out of the scope element and load it into the native
UI. If no scope element is specified, it defaults to the document body.

The success and error parameters are callbacks that will be called either when
parsing the menu structure is complete or when there is an error in parsing the
menu structure respectively.


## Defining Menus
We try to be as close as possible to Firefox OS in terms of HTML structure, so
that you can build your application with plain HTML and CSS and then use Cambie
to provide native integration with minimal changes.

A good starting point is to read
http://buildingfirefoxos.com/building-blocks/headers.html

As an example, we have a page with this HTML markup:

```html
<body>
    <header data-nav="back"> <!-- data-nav will change in future versions! -->

        <!-- Becomes the title in the menu bar -->
        <h1>Page title</h1>

        <!-- Becomes the action buttons in the header -->
        <menu type="toolbar">
            <button id="button-add">Action Button</button>
        </menu>

        <!-- Becomes the application popup menu -->
        <menu type="popup">
            <menuitem label="Menu Item 1" icon="img/menu1.png">
            <menuitem label="Menu Item 2" icon="img/menu2.png" disabled="disabled">
            <menuitem label="Menu Item 3" icon="img/menu3.png">
        </menu>
    </header>

    <p>Page content goes here</p>
</body>
```

When the page is shown in our application, we can call Cambie to update the
native menus:  
`navigator.setApplicationMenus();`

If you're building a single-page application with multiple internal pages, as
managed by a UI framework, you can pass the page element into Cambie and it
will only load menus appearing within that page element:  
`window.navigator.setApplicationMenus(document.getElementById('my-app-page'));`
