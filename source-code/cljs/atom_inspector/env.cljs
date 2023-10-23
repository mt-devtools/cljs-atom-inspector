
(ns atom-inspector.env
    (:require [atom-inspector.state :as state]
              [map.api              :as map]
              [vector.api           :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-atom-ref
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  ;
  ; @return (atom)
  [inspector-id]
  ; Returns the inspected atom.
  (get-in @state/INSPECTORS [inspector-id :ref]))

(defn get-inspected-path
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  ;
  ; @return (vector)
  [inspector-id]
  ; Returns the inspected path. Always returns it as a vector even if it has no value yet.
  (get-in @state/INSPECTORS [inspector-id :meta-items :inspected-path] []))

(defn get-inspected-item
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  ;
  ; @return (*)
  [inspector-id]
  ; Reads the inspected atom on the currently inspected path.
  (if-let [atom-ref (get-atom-ref inspector-id)]
          (if-let [inspected-path (get-in @state/INSPECTORS [inspector-id :meta-items :inspected-path])]
                  (get-in @atom-ref inspected-path)
                  (->     @atom-ref))))

(defn root-level?
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  ;
  ; @return (boolean)
  [inspector-id]
  ; If the inspected path is an empty vector, the inspector displays the root
  ; of the inspected atom.
  (= [] (get-inspected-path inspector-id)))

(defn edit-mode?
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  ;
  ; @return (boolean)
  [inspector-id]
  ; Returns true if the inspected item editor turned on.
  ; When the inspected path changes, the inspector always turns off the edit mode
  ; and discards the unsaved changes (by deleting the meta items).
  (get-in @state/INSPECTORS [inspector-id :meta-items :edit-mode?]))

(defn raw-view?
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  ;
  ; @return (boolean)
  [inspector-id]
  ; Returns true if the inspected item displayed in raw mode too.
  ; When the inspected path changes, the inspector always turns off the raw view
  ; (by deleting the meta items).
  (get-in @state/INSPECTORS [inspector-id :meta-items :raw-view?]))

(defn inspected-item-removed?
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  ;
  ; @return (boolean)
  [inspector-id]
  ; Returns true if the inspected item moved to the bin.
  ; When the inspected path changes, the inspector always empties the bin
  ; (by deleting the meta items).
  (get-in @state/INSPECTORS [inspector-id :meta-items :bin]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-edit-copy
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  ;
  ; @return (string)
  [inspector-id]
  ; When editing an inspected item, the textarea doesn't changes the item directly.
  ; It makes a copy of the item before start editing.
  (get-in @state/INSPECTORS [inspector-id :meta-items :edit-copy]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-inspected-map-keys
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  ;
  ; @return (vector)
  [inspector-id]
  ; When inspecting a map it can be browsed by its keys.
  ; This function returns with the inspected map's keys (ordered in alphabetically).
  (if-let [inspected-item (get-inspected-item inspector-id)]
          (-> inspected-item map/get-keys vector/abc-items)))
