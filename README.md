
# cljs-atom-inspector

### Overview

The <strong>cljs-atom-inspector</strong> is a simple ClojureScript tool for inspecting,
browsing or editing ClojureScript atoms or Reagent atoms.

> UI components in this library are Reagent components. For more information, check out
  the [Reagent Project](https://github.com/reagent-project/reagent).

### deps.edn

```
{:deps {monotech-tools/cljs-atom-inspector {:git/url "https://github.com/monotech-tools/cljs-atom-inspector"
                                            :sha     "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"}}
```

### Current version

Check out the latest commit on the [release branch](https://github.com/monotech-tools/cljs-atom-inspector/tree/release).

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

### Browsing an atom:

![preview](https://github.com/monotech-tools/cljs-atom-inspector/blob/release/resources/public/images/preview1.png?raw=true)

### Displaying the inspected path in raw mode:

![preview](https://github.com/monotech-tools/cljs-atom-inspector/blob/release/resources/public/images/preview2.png?raw=true)

### Editing the content of the inspected path:

![preview](https://github.com/monotech-tools/cljs-atom-inspector/blob/release/resources/public/images/preview3.png?raw=true)
