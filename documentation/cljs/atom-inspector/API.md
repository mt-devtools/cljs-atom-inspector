
### atom-inspector.api

Functional documentation of the atom-inspector.api ClojureScript namespace

---

##### [README](../../../README.md) > [DOCUMENTATION](../../COVER.md) > atom-inspector.api

### Index

- [view](#view)

---

### view

```
@param (keyword)(opt) inspector-id
@param (map) inspector-props
{:ref (atom)}
```

```
@usage
[atom-inspector {...}]
```

```
@usage
[atom-inspector :my-inspector {...}]
```

```
@usage
(def my-atom (atom {:my-key "My value"}))
[atom-inspector :my-inspector {:ref my-atom}]
```

<details>
<summary>Source code</summary>

```
(defn view
  ([inspector-props]
   [view (random/generate-keyword) inspector-props])

  ([inspector-id inspector-props]
   (reagent/create-class {:reagent-render      (fn [] [atom-inspector inspector-id])
                          :component-did-mount (fn [] (if-not (inspector-id @state/INSPECTORS)
                                                              (swap! state/INSPECTORS assoc inspector-id inspector-props)))})))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [atom-inspector.api :refer [view]]))

(atom-inspector.api/view ...)
(view                    ...)
```

</details>

---

<sub>This documentation is generated with the [clj-docs-generator](https://github.com/bithandshake/clj-docs-generator) engine.</sub>

