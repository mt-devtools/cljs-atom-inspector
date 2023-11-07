
### atom-inspector.api

Functional documentation of the atom-inspector.api ClojureScript namespace

---

##### [README](../../../README.md) > [DOCUMENTATION](../../COVER.md) > atom-inspector.api

### Index

- [get-inspected-item](#get-inspected-item)

- [get-inspected-path](#get-inspected-path)

- [inspect-key!](#inspect-key)

- [inspect-path!](#inspect-path)

- [view](#view)

---

### get-inspected-item

```
@description
Reads the inspected atom on the currently inspected path.
```

```
@param (keyword) inspector-id
```

```
@usage
(get-inspected-item :my-inspector)
```

```
@return (*)
```

<details>
<summary>Source code</summary>

```
(defn get-inspected-item
  [inspector-id]
  (if-let [atom-ref (get-atom-ref inspector-id)]
          (if-let [inspected-path (get-in @state/INSPECTORS [inspector-id :meta-items :inspected-path])]
                  (get-in @atom-ref inspected-path)
                  (->     @atom-ref))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [atom-inspector.api :refer [get-inspected-item]]))

(atom-inspector.api/get-inspected-item ...)
(get-inspected-item                    ...)
```

</details>

---

### get-inspected-path

```
@description
Returns the inspected path. Always returns it as a vector even if it has no value yet.
```

```
@param (keyword) inspector-id
```

```
@usage
(get-inspected-path :my-inspector)
```

```
@return (vector)
```

<details>
<summary>Source code</summary>

```
(defn get-inspected-path
  [inspector-id]
  (get-in @state/INSPECTORS [inspector-id :meta-items :inspected-path] []))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [atom-inspector.api :refer [get-inspected-path]]))

(atom-inspector.api/get-inspected-path ...)
(get-inspected-path                    ...)
```

</details>

---

### inspect-key!

```
@description
- Steps into the given key of the currently inspected map item.
- Only map items are browsable in the inspector.
```

```
@param (keyword) inspector-id
@param (*) key
```

<details>
<summary>Source code</summary>

```
(defn inspect-key!
  [inspector-id key]
  (let [inspected-path (env/get-inspected-path inspector-id)]
       (inspect-path! inspector-id (conj inspected-path key))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [atom-inspector.api :refer [inspect-key!]]))

(atom-inspector.api/inspect-key! ...)
(inspect-key!                    ...)
```

</details>

---

### inspect-path!

```
@description
...
```

```
@param (keyword) inspector-id
@param (vector) path
```

<details>
<summary>Source code</summary>

```
(defn inspect-path!
  [inspector-id path]
  (swap! state/INSPECTORS assoc-in [inspector-id :meta-items] {:inspected-path path}))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [atom-inspector.api :refer [inspect-path!]]))

(atom-inspector.api/inspect-path! ...)
(inspect-path!                    ...)
```

</details>

---

### view

```
@param (keyword)(opt) inspector-id
@param (map) inspector-props
{:controls (keywords in vector)(opt)
  Default: [:edit-value :go-root :raw-view :remove-value :step-integer :swap-boolean :toggle-boolean]
 :edit-types (keywords in vector)(opt)
  Default: [:boolean :integer :keyword :map :nil :string :vector]
 :header (keywords in vector)(opt)
  Default: [:breadcrumbs :key-label :type-label]
 :initial-path (vector)(opt)
  Default: []
 :ref (atom)}
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
   (reagent/create-class {:reagent-render       (fn [] [atom-inspector inspector-id])
                          :component-did-mount  (fn [] (if-not (inspector-id @state/INSPECTORS)
                                                               (let [inspector-props (prototypes/inspector-props-prototype inspector-props)]
                                                                    (side-effects/inspect-path! inspector-id (:initial-path inspector-props))
                                                                    (swap! state/INSPECTORS assoc inspector-id inspector-props))))})))
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

