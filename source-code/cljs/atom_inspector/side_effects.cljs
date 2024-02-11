
(ns atom-inspector.side-effects
    (:require [atom-inspector.env   :as env]
              [atom-inspector.state :as state]
              [fruits.map.api       :refer [dissoc-in]]
              [fruits.pretty.api    :as pretty]
              [fruits.reader.api    :as reader]
              [fruits.vector.api    :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn inspect-path!
  ; @important
  ; Only map items are browsable in the inspector.
  ;
  ; @description
  ; Steps into the given path within the atom.
  ;
  ; @usage
  ; (inspect-path! :my-inspector [:my-item :my-subitem])
  ;
  ; @param (keyword) inspector-id
  ; @param (vector) path
  [inspector-id path]
  (swap! state/INSPECTORS assoc-in [inspector-id :meta-items] {:inspected-path path}))

(defn inspect-key!
  ; @important
  ; Only map items are browsable in the inspector.
  ;
  ; @description
  ; Steps into the given key of the currently inspected map item within the atom.
  ;
  ; @usage
  ; (inspect-key! :my-inspector :my-item)
  ;
  ; @param (keyword) inspector-id
  ; @param (*) key
  [inspector-id key]
  (let [inspected-path (env/get-inspected-path inspector-id)]
       (inspect-path! inspector-id (conj inspected-path key))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn go-home!
  ; @ignore
  ;
  ; @description
  ; Resets the inspected path (as an empty vector). Therefore, the inspector steps
  ; back to the root level of the inspected atom.
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (swap! state/INSPECTORS assoc-in [inspector-id :meta-items] {:inspected-path []}))

(defn go-up!
  ; @ignore
  ;
  ; @description
  ; Removes the last item of the inspected path. Therefore, the inspector steps back
  ; to the parent element of the currently inspected item.
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [inspected-path (env/get-inspected-path inspector-id)]
       (swap! state/INSPECTORS assoc-in [inspector-id :meta-items] {:inspected-path (vector/remove-last-item inspected-path)})))

(defn update-inspected-item!
  ; @ignore
  ;
  ; @description
  ; Applies the given function (and passing it the given params) on the currently inspected item.
  ;
  ; @param (keyword) inspector-id
  ; @param (function) f
  ; @param (list of *) params
  [inspector-id f & params]
  (let [atom-ref       (env/get-atom-ref       inspector-id)
        inspected-path (env/get-inspected-path inspector-id)
        inspected-item (env/get-inspected-item inspector-id)
        updated-item   (apply f (cons inspected-item params))]
       (if (env/root-level? inspector-id)
           (reset! atom-ref                         updated-item)
           (swap!  atom-ref assoc-in inspected-path updated-item))))

(defn remove-inspected-item!
  ; @ignore
  ;
  ; @description
  ; Duplicates the inspected item into the bin, then removes the inspected item.
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [atom-ref       (env/get-atom-ref       inspector-id)
        inspected-path (env/get-inspected-path inspector-id)
        inspected-item (env/get-inspected-item inspector-id)]
       (swap! state/INSPECTORS assoc-in [inspector-id :meta-items :bin] inspected-item)
       (if (env/root-level? inspector-id)
           (reset! atom-ref nil)
           (swap!  atom-ref dissoc-in inspected-path))))

(defn restore-inspected-item!
  ; @ignore
  ;
  ; @description
  ; Restores the removed item by using its backup copy stored in the bin.
  ; The inspector empties the bin when the inspected path changes!
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [atom-ref       (env/get-atom-ref       inspector-id)
        inspected-path (env/get-inspected-path inspector-id)
        removed-item   (get-in @state/INSPECTORS [inspector-id :meta-items :bin])]
       (swap! state/INSPECTORS dissoc-in [inspector-id :meta-items :bin])
       (if (env/root-level? inspector-id)
           (reset! atom-ref                         removed-item)
           (swap!  atom-ref assoc-in inspected-path removed-item))))

(defn toggle-raw-view!
  ; @ignore
  ;
  ; @description
  ; ...
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (swap! state/INSPECTORS update-in [inspector-id :meta-items :raw-view?] not))

(defn toggle-edit-mode!
  ; @ignore
  ;
  ; @description
  ; ...
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [atom-ref       (env/get-atom-ref       inspector-id)
        inspected-path (env/get-inspected-path inspector-id)]
       (if-let [edit-mode? (env/edit-mode? inspector-id)]

               ; - When turning off the edit mode, the content of the textarea gets parsed into
               ;   a data structure by using the 'reader/parse-edn' function.
               ;   When it is parsed, this function stores the (parsed) value in the inspected atom.
               ; - The output of the 'reader/parse-edn' function is the original string in case the parse fails
               ;   (e.g., syntax error in the edited copy).
               (let [edit-copy (env/get-edit-copy inspector-id)]
                    (if (env/root-level? inspector-id)
                        (reset! atom-ref                         (reader/parse-edn edit-copy))
                        (swap!  atom-ref assoc-in inspected-path (reader/parse-edn edit-copy))))

               ; - When turning on the edit mode, it makes a copy of the inspected item
               ;   (:edit-copy), and the textarea can change the copy and not the original item.
               ; - The item is displayed in the textarea in a well readable (pretty printed) way,
               ;   by using the 'pretty/mixed->string' function.
               (let [inspected-item (env/get-inspected-item inspector-id)

                     ; The 'pretty/string->mixed' function puts quote marks on strings!
                     ; Therefore, in case of the inspected item is a string, it's better
                     ; to not passing it the 'pretty/string->mixed' function to avoid duplicated quotes.
                     unparsed-item (if (string? inspected-item) inspected-item (pretty/mixed->string inspected-item))]
                    (swap! state/INSPECTORS assoc-in [inspector-id :meta-items :edit-copy] unparsed-item))))

  ; Toggling the edit mode switch
  (swap! state/INSPECTORS update-in [inspector-id :meta-items :edit-mode?] not))
