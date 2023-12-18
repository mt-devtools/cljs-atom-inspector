
(ns atom-inspector.env
    (:require [atom-inspector.state :as state]
              [fruits.map.api       :as map]
              [fruits.vector.api    :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-atom-ref
  ; @ignore
  ;
  ; @description
  ; Returns the inspected atom.
  ;
  ; @param (keyword) inspector-id
  ;
  ; @return (atom)
  [inspector-id]
  (get-in @state/INSPECTORS [inspector-id :ref]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-inspected-path
  ; @description
  ; Returns the currently inspected path.
  ;
  ; @param (keyword) inspector-id
  ;
  ; @usage
  ; (get-inspected-path :my-inspector)
  ;
  ; @return (vector)
  [inspector-id]
  (get-in @state/INSPECTORS [inspector-id :meta-items :inspected-path] []))

(defn get-inspected-item
  ; @description
  ; Returns the value that corresponds to the currently inspected path.
  ;
  ; @param (keyword) inspector-id
  ;
  ; @usage
  ; (get-inspected-item :my-inspector)
  ;
  ; @return (*)
  [inspector-id]
  (if-let [atom-ref (get-atom-ref inspector-id)]
          (if-let [inspected-path (get-in @state/INSPECTORS [inspector-id :meta-items :inspected-path])]
                  (get-in @atom-ref inspected-path)
                  (->     @atom-ref))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn root-level?
  ; @ignore
  ;
  ; @description
  ; If the inspected path is an empty vector, the inspector displays the root level of the inspected atom.
  ;
  ; @param (keyword) inspector-id
  ;
  ; @return (boolean)
  [inspector-id]
  (= [] (get-inspected-path inspector-id)))

(defn edit-mode?
  ; @ignore
  ;
  ; @description
  ; - Returns TRUE if the inspector is in edit mode.
  ; - When the inspected path changes, the inspector turns off the edit mode and discards
  ;   the unsaved changes (by deleting the meta items).
  ;
  ; @param (keyword) inspector-id
  ;
  ; @return (boolean)
  [inspector-id]
  (get-in @state/INSPECTORS [inspector-id :meta-items :edit-mode?]))

(defn raw-view?
  ; @ignore
  ;
  ; @description
  ; - Returns TRUE if the inspected item is displayed in raw mode also.
  ; - When the inspected path changes, the inspector turns off the raw view (by deleting the meta items).
  ;
  ; @param (keyword) inspector-id
  ;
  ; @return (boolean)
  [inspector-id]
  (get-in @state/INSPECTORS [inspector-id :meta-items :raw-view?]))

(defn inspected-item-removed?
  ; @ignore
  ;
  ; @description
  ; - Returns TRUE if the inspected item has been moved to the bin.
  ; - When the inspected path changes, the inspector empties the bin (by deleting the meta items).
  ;
  ; @param (keyword) inspector-id
  ;
  ; @return (boolean)
  [inspector-id]
  ; The deleted item might be a FALSE!
  (some? (get-in @state/INSPECTORS [inspector-id :meta-items :bin])))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-edit-copy
  ; @ignore
  ;
  ; @description
  ; When editing the inspected item, the textarea doesn't change the item directly.
  ; It makes a copy of the item before start editing.
  ;
  ; @param (keyword) inspector-id
  ;
  ; @return (string)
  [inspector-id]
  (get-in @state/INSPECTORS [inspector-id :meta-items :edit-copy]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-inspected-map-keys
  ; @ignore
  ;
  ; @description
  ; When inspecting a map it can be browsed by its keys.
  ; This function returns the inspected map's keys (ordered in alphabetically).
  ;
  ; @param (keyword) inspector-id
  ;
  ; @return (vector)
  [inspector-id]
  (if-let [inspected-item (get-inspected-item inspector-id)]
          (-> inspected-item map/keys vector/abc-items)))
