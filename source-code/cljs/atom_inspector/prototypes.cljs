
(ns atom-inspector.prototypes)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn inspector-props-prototype
  ; @ignore
  ;
  ; @param (map) inspector-props
  ;
  ; @return (map)
  ; {:controls (keywords in vector)
  ;  :edit-types (keywords in vector)
  ;  :header (keywords in vector)
  ;  :initial-path (vector)
  ;  :root-path (vector)}
  [inspector-props]
  (merge {:controls     [:edit-value :go-root :raw-view :remove-value :step-integer :swap-boolean :toggle-boolean]
          :edit-types   [:boolean :integer :keyword :map :nil :string :vector]
          :header       [:breadcrumbs :key-label :type-label]
          :initial-path []
          :root-path    []}
         (-> inspector-props)))
