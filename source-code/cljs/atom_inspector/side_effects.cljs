
(ns atom-inspector.side-effects
    (:require [atom-inspector.env   :as env]
              [atom-inspector.state :as state]
              [map.api              :refer [dissoc-in]]
              [pretty.print         :as pretty]
              [reader.api           :as reader]
              [vector.api           :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn inspect-key!
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  ; @param (*) key
  [inspector-id key]
  ; Steps into the given key of the currently inspected map item.
  ; Only map items are browsable in the inspector.
  (let [inspected-path (env/get-inspected-path inspector-id)]
       (swap! state/INSPECTORS assoc-in [inspector-id :meta-items] {:inspected-path (conj inspected-path key)})))

(defn go-home!
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  ; Resets the inspected path to an empty vector and the inspector steps back to
  ; the root level of the inspected atom.
  (swap! state/INSPECTORS assoc-in [inspector-id :meta-items] {:inspected-path []}))

(defn go-up!
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  ; Removes the last item of the inspected path, and the inspector steps back
  ; to the parent element of the currently inspected item.
  (let [inspected-path (env/get-inspected-path inspector-id)]
       (swap! state/INSPECTORS assoc-in [inspector-id :meta-items] {:inspected-path (vector/remove-last-item inspected-path)})))

(defn update-inspected-item!
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  ; @param (function) f
  ; @param (list of *) params
  [inspector-id f & params]
  ; Applies the given function and passing it the given params on the currently
  ; inspected item.
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
  ; @param (keyword) inspector-id
  [inspector-id]
  ; Duplicates the inspected item into the :bin, then removes the inspected item.
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
  ; @param (keyword) inspector-id
  [inspector-id]
  ; Restores the removed item by using its backup copy stored in the :bin.
  ; The inspector empties the bin when the inspected path changes!
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
  ; @param (keyword) inspector-id
  [inspector-id]
  (swap! state/INSPECTORS update-in [inspector-id :meta-items :raw-view?] not))

(defn toggle-edit-mode!
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [atom-ref       (env/get-atom-ref       inspector-id)
        inspected-path (env/get-inspected-path inspector-id)]
       (if-let [edit-mode? (env/edit-mode? inspector-id)]

               ; When turning off the edit mode, the content of the textarea
               ; parsed into a data structure by using the reader/string->mixed
               ; function. After it parsed, this function stores the (parsed) value
               ; in the inspected atom.
               ; In case of the parse fails (e.g. syntax error in the edited copy)
               ; the output of the string->mixed function is a string.
               (let [edit-copy (env/get-edit-copy inspector-id)]
                    (if (env/root-level? inspector-id)
                        (reset! atom-ref                         (reader/string->mixed edit-copy))
                        (swap!  atom-ref assoc-in inspected-path (reader/string->mixed edit-copy))))

               ; When turning on the edit mode, it makes a copy of the inspected item
               ; (:edit-copy) and the textarea can changes the copy, not the original item.
               ; By using the pretty/mixed->string function the item displayed in the textarea
               ; in a well readable (pretty printed) way.
               (let [inspected-item (env/get-inspected-item inspector-id)

                     ; The mixed->string function puts quote marks on strings!
                     ; Therefore in case of the inspected item is a string, it's better
                     ; to not passing it the mixed->string function.
                     unparsed-item (if (string? inspected-item) inspected-item (pretty/mixed->string inspected-item))]
                    (swap! state/INSPECTORS assoc-in [inspector-id :meta-items :edit-copy] unparsed-item))))

  ; Toggling the edit-mode switch
  (swap! state/INSPECTORS update-in [inspector-id :meta-items :edit-mode?] not))
