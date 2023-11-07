
# cljs-atom-inspector

### Overview

The <strong>cljs-atom-inspector</strong> is a simple ClojureScript tool for inspecting,
browsing or editing ClojureScript atoms or Reagent atoms.

### deps.edn

```
{:deps {bithandshake/cljs-atom-inspector {:git/url "https://github.com/bithandshake/cljs-atom-inspector"
                                          :sha     "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"}}
```

### Current version

Check out the latest commit on the [release branch](https://github.com/bithandshake/cljs-atom-inspector/tree/release).

### Changelog

You can track the changes of the <strong>cljs-atom-inspector</strong> library [here](CHANGES.md).

# Usage

> Some parameters of the following functions and some further functions are not discussed in this file.
  To learn more about the available functionality, check out the [functional documentation](documentation/COVER.md)!

The [`atom-inspector.api/view`](documentation/cljs/atom-inspector/API.md#view)
Reagent component displays the data stored in a Reagent atom and provides you
tools to browse and edit the content of the atom.

```
(def my-atom (reagent.core/atom {:my-data {}}))

(defn my-atom-inspector []
      [view ::my-atom-inspector {:ref my-atom}])  
```

### Browsing the atom:

![view](https://github.com/bithandshake/cljs-atom-inspector/blob/release/resources/images/view1.png?raw=true)

### Displaying the inspected path in raw mode:

![view](https://github.com/bithandshake/cljs-atom-inspector/blob/release/resources/images/view2.png?raw=true)

### Editing the content of the inspected path:

![view](https://github.com/bithandshake/cljs-atom-inspector/blob/release/resources/images/view3.png?raw=true)
