
# cljs-atom-inspector

### Overview

The <strong>cljs-atom-inspector</strong> is a simple tool for inspecting, browsing
and editing Reagent atoms.

### deps.edn

```
{:deps {bithandshake/pretty-css {:git/url "https://github.com/bithandshake/cljs-atom-inspector"
                                 :sha     "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"}}
```

### Current version

Check out the latest commit on the [release branch](https://github.com/bithandshake/cljs-atom-inspector/tree/release).

### Changelog

You can track the changes of the <strong>cljs-atom-inspector</strong> library [here](CHANGES.md).

### How to use?

The [`atom-inspector.api/view`](documentation/cljs/docs/API.md#view)
Reagent component displays the data stored in a Reagent atom and it provides you
tools to browse and edit the content of the atom.

```
(def my-atom (reagent.core/atom {:my-data {}}))

(defn my-atom-inspector []
      [view ::my-atom-inspector {:ref my-atom}])  
```

![view](https://github.com/bithandshake/cljs-atom-inspector/blob/release/resources/images/view1.png?raw=true)

![view](https://github.com/bithandshake/cljs-atom-inspector/blob/release/resources/images/view2.png?raw=true)

![view](https://github.com/bithandshake/cljs-atom-inspector/blob/release/resources/images/view3.png?raw=true)
