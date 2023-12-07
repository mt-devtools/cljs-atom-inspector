
(ns atom-inspector.views
    (:require [atom-inspector.env          :as env]
              [atom-inspector.prototypes   :as prototypes]
              [atom-inspector.side-effects :as side-effects]
              [atom-inspector.state        :as state]
              [fruits.pretty.api           :as pretty]
              [fruits.random.api           :as random]
              [fruits.string.api           :as string]
              [fruits.syntax.api           :as syntax]
              [fruits.vector.api           :as vector]
              [reagent.core                :as reagent]
              [ugly-elements.api           :as ugly-elements]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- inspector-title
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  ; @param (map) header-props
  [inspector-id _]
  (if (-> @state/INSPECTORS inspector-id :header (vector/contains-item? :key-label))
      (let [inspected-path (env/get-inspected-path inspector-id)
            root-level?    (env/root-level?        inspector-id)]
           [ugly-elements/label ::inspector-title
                                {:font-size :xs
                                 :content (if root-level? (-> inspector-id        str)
                                                          (-> inspected-path last str))}])))

(defn- label-bar
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  ; @param (map) header-props
  ; {:label (string)(opt)}
  [inspector-id {:keys [label] :as header-props}]
  (if (-> @state/INSPECTORS inspector-id :header (vector/contains-item? :type-label))
      [:<> [ugly-elements/label ::label-bar
                                {:content label
                                 :color :muted
                                 :font-size :xs}]
           [ugly-elements/horizontal-separator {}]]))

(defn- breadcrumbs
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  ; @param (map) header-props
  [inspector-id _]
  (if (-> @state/INSPECTORS inspector-id :header (vector/contains-item? :breadcrumbs))
      (let [crumbs (-> inspector-id env/get-inspected-path (vector/->items #(-> {:label %})))]
           [ugly-elements/breadcrumbs ::breadcrumbs
                                      {:crumbs crumbs}])))

(defn- toolbar
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  ; @param (list of Reagent component symbols) buttons
  [inspector-id & buttons]
  [:<> [ugly-elements/row ::toolbar
                          {:content (letfn [(f0 [%1 %2] (conj %1 [%2 inspector-id]))]
                                           (reduce f0 [:<>] buttons))}]
       [ugly-elements/horizontal-line      {}]
       [ugly-elements/horizontal-separator {}]])

(defn- header
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  ; @param (map) header-props
  ; {:label (string)(opt)}
  [inspector-id header-props]
  [:<> [inspector-title inspector-id header-props]
       [label-bar       inspector-id header-props]
       [breadcrumbs     inspector-id header-props]])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- decrease-integer-button
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (if (-> @state/INSPECTORS inspector-id :controls (vector/contains-item? :step-integer))
      [ugly-elements/icon-button ::decrease-integer-button
                                 {:icon     :remove
                                  :label    "Dec"
                                  :on-click #(side-effects/update-inspected-item! inspector-id dec)}]))

(defn- increase-integer-button
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (if (-> @state/INSPECTORS inspector-id :controls (vector/contains-item? :step-integer))
      [ugly-elements/icon-button ::increase-integer-button
                                 {:icon     :add
                                  :label    "Inc"
                                  :on-click #(side-effects/update-inspected-item! inspector-id inc)}]))

(defn- swap-boolean-button
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (if (-> @state/INSPECTORS inspector-id :controls (vector/contains-item? :swap-boolean))
      [ugly-elements/icon-button ::swap-boolean-button
                                 {:icon     :change_circle
                                  :label    "Swap"
                                  :on-click #(side-effects/update-inspected-item! inspector-id not)}]))

(defn- toggle-raw-view-button
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (if (-> @state/INSPECTORS inspector-id :controls (vector/contains-item? :raw-view))
      (let [raw-view? (env/raw-view? inspector-id)]
           [ugly-elements/icon-button ::toggle-raw-view-button
                                      {:icon     (if raw-view? :code_off :code)
                                       :label    (if raw-view? "Raw" "Raw")
                                       :on-click #(side-effects/toggle-raw-view! inspector-id)}])))

(defn- go-home-button
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (if (-> @state/INSPECTORS inspector-id :controls (vector/contains-item? :go-root))
      (let [root-level? (env/root-level? inspector-id)]
           [ugly-elements/icon-button ::go-home-button
                                      {:disabled? root-level?
                                       :icon      :home
                                       :label     "Root"
                                       :on-click  #(side-effects/go-home! inspector-id)}])))

(defn- go-up-button
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [root-level? (env/root-level? inspector-id)]
       [ugly-elements/icon-button ::go-up-button
                                  {:disabled? root-level?
                                   :icon      :chevron_left
                                   :label     "Go up"
                                   :on-click  #(side-effects/go-up! inspector-id)}]))

(defn- remove-item-button
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (if (-> @state/INSPECTORS inspector-id :controls (vector/contains-item? :remove-value))
      [ugly-elements/icon-button ::remove-item-button
                                 {:icon     :delete
                                  :label    "Remove"
                                  :on-click #(side-effects/remove-inspected-item! inspector-id)}]))

(defn- edit-item-button
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (if (-> @state/INSPECTORS inspector-id :controls (vector/contains-item? :edit-value))
      (let [edit-mode? (env/edit-mode? inspector-id)]
           [ugly-elements/icon-button ::edit-item-button
                                      {:icon     (if edit-mode? :edit_off :edit)
                                       :label    (if edit-mode? "Save" "Edit")
                                       :on-click #(side-effects/toggle-edit-mode! inspector-id)}])))

(defn- restore-item-button
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (if (env/inspected-item-removed? inspector-id)
      [ugly-elements/icon-button ::restore-item-button
                                 {:icon     :recycling
                                  :label    "Restore"
                                  :on-click #(side-effects/restore-inspected-item! inspector-id)}]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- empty-item-label
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [inspected-item (env/get-inspected-item inspector-id)]
       (if (empty? inspected-item)
           [ugly-elements/label ::empty-item-label
                                {:color   :muted
                                 :content "Empty"}])))

(defn- raw-item
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (if-let [raw-view? (env/raw-view? inspector-id)]
          (let [inspected-item (env/get-inspected-item inspector-id)]
               [:<> [ugly-elements/horizontal-separator {:height :m}]
                    [ugly-elements/box ::raw-item
                                       {:content (pretty/mixed->string inspected-item)}]])))

(defn- item-editor
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (letfn [(f0 [v] (swap! state/INSPECTORS assoc-in [inspector-id :meta-items :edit-copy] v))]
         (if-let [edit-mode? (env/edit-mode? inspector-id)]
                 (let [edit-copy (env/get-edit-copy inspector-id)]
                      [:<> [ugly-elements/horizontal-separator {:height :m}]
                           [ugly-elements/textarea ::item-editor
                                                   {:on-change f0
                                                    :style     {:min-height "420px"}
                                                    :value     edit-copy}]]))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- map-key
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  ; @param (*) key
  [inspector-id key]
  [ugly-elements/button {:on-click #(side-effects/inspect-key! inspector-id key)
                         :content  (cond (string? key) (syntax/quotes key)
                                         (nil?    key) (str           "nil")
                                         :return       (str           key))}])

(defn- map-item
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [editable?      (-> @state/INSPECTORS inspector-id :edit-types (vector/contains-item? :map))
        inspected-item (env/get-inspected-item     inspector-id)
        map-keys       (env/get-inspected-map-keys inspector-id)]
       [:<> [header inspector-id {:label (str "map, "(count map-keys)" item(s)")}]
            (if editable? [toolbar inspector-id go-home-button go-up-button remove-item-button toggle-raw-view-button edit-item-button]
                          [toolbar inspector-id go-home-button go-up-button remove-item-button toggle-raw-view-button])
            [empty-item-label inspector-id]
            [:div {:style {:display "flex" :flex-direction "column" :overflow "scroll"}}
                  (letfn [(f0 [%1 %2] (conj %1 [map-key inspector-id %2]))]
                         (reduce f0 [:<>] map-keys))
                  [raw-item    inspector-id]
                  [item-editor inspector-id]]]))

(defn- vector-key
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  ; @param (*) key
  [_ key]
  [ugly-elements/label {:color   :muted
                        :content (cond (nil?    key) (str "nil")
                                       (string? key) (syntax/quotes key)
                                       :return       (str           key))}])

(defn- vector-item
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [editable?      (-> @state/INSPECTORS inspector-id :edit-types (vector/contains-item? :vector))
        inspected-item (env/get-inspected-item inspector-id)]
       [:<> [header inspector-id {:label (str "vector, " (count inspected-item) " item(s)")}]
            (if editable? [toolbar inspector-id go-home-button go-up-button remove-item-button toggle-raw-view-button edit-item-button]
                          [toolbar inspector-id go-home-button go-up-button remove-item-button toggle-raw-view-button])
            [empty-item-label inspector-id]
            [:div {:style {:overflow "scroll"}}
                  (letfn [(f0 [%1 %2] (conj %1 [vector-key inspector-id %2]))]
                         (reduce f0 [:<>] inspected-item))
                  [raw-item    inspector-id]
                  [item-editor inspector-id]]]))

(defn- boolean-item
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [editable?      (-> @state/INSPECTORS inspector-id :edit-types (vector/contains-item? :boolean))
        inspected-item (env/get-inspected-item inspector-id)]
       [:<> [header  inspector-id {:label "boolean"}]
            (if editable? [toolbar inspector-id go-home-button go-up-button remove-item-button swap-boolean-button edit-item-button]
                          [toolbar inspector-id go-home-button go-up-button remove-item-button swap-boolean-button])
            [ugly-elements/label ::boolean-item
                                 {:color   :muted
                                  :content (str inspected-item)}]
            [item-editor inspector-id]]))

(defn- integer-item
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [editable?      (-> @state/INSPECTORS inspector-id :edit-types (vector/contains-item? :integer))
        inspected-item (env/get-inspected-item inspector-id)]
       [:<> [header inspector-id {:label "integer"}]
            (if editable? [toolbar inspector-id go-home-button go-up-button remove-item-button decrease-integer-button increase-integer-button edit-item-button]
                          [toolbar inspector-id go-home-button go-up-button remove-item-button decrease-integer-button increase-integer-button])
            [ugly-elements/label ::integer-item
                                 {:color   :muted
                                  :content (str inspected-item)}]
            [item-editor inspector-id]]))

(defn- string-item
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [editable?      (-> @state/INSPECTORS inspector-id :edit-types (vector/contains-item? :string))
        inspected-item (env/get-inspected-item inspector-id)]
       [:<> [header inspector-id {:label (str "string, "(count inspected-item) " char")}]
            (if editable? [toolbar inspector-id go-home-button go-up-button remove-item-button edit-item-button]
                          [toolbar inspector-id go-home-button go-up-button remove-item-button])
            [:div {:style {:overflow "scroll"}}
                  [ugly-elements/label ::string-item
                                       {:color   :muted
                                        :content (syntax/quotes inspected-item)
                                        :style   {:white-space "normal"}}]
                  [item-editor inspector-id]]]))

(defn- keyword-item
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [editable?      (-> @state/INSPECTORS inspector-id :edit-types (vector/contains-item? :keyword))
        inspected-item (env/get-inspected-item inspector-id)]
       [:<> [header inspector-id {:label "keyword"}]
            (if editable? [toolbar inspector-id go-home-button go-up-button remove-item-button edit-item-button]
                          [toolbar inspector-id go-home-button go-up-button remove-item-button edit-item-button])
            [ugly-elements/label ::keyword-item
                                 {:color   :muted
                                  :content (str inspected-item)
                                  :style   {:white-space "normal"}}]
            [item-editor inspector-id]]))

(defn- symbol-item
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [inspected-item (env/get-inspected-item inspector-id)]
       [:<> [header  inspector-id {:label "symbol"}]
            [toolbar inspector-id go-home-button go-up-button remove-item-button]
            [ugly-elements/label ::symbol-item
                                 {:color   :muted
                                  :content (str inspected-item)
                                  :style   {:white-space "normal"}}]]))

(defn- nil-item
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [editable? (-> @state/INSPECTORS inspector-id :edit-types (vector/contains-item? :keyword))]
       [:<> [header inspector-id {:label "nil"}]
            (if editable? [toolbar inspector-id go-home-button go-up-button restore-item-button edit-item-button]
                          [toolbar inspector-id go-home-button go-up-button restore-item-button])
            [ugly-elements/label ::nil-item
                                 {:color   :muted
                                  :content "nil"}]
            [item-editor inspector-id]]))

(defn- unknown-item
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [inspected-item (env/get-inspected-item inspector-id)]
       [:<> [header  inspector-id {:label "unknown"}]
            [toolbar inspector-id go-home-button go-up-button remove-item-button]
            [ugly-elements/label ::nil-item
                                 {:color   :muted
                                  :content (str inspected-item)
                                  :style   {:white-space "normal"}}]]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn inspected-item
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [inspected-item (env/get-inspected-item inspector-id)]
       (cond (map?     inspected-item) [map-item      inspector-id]
             (vector?  inspected-item) [vector-item   inspector-id]
             (boolean? inspected-item) [boolean-item  inspector-id]
             (integer? inspected-item) [integer-item  inspector-id]
             (string?  inspected-item) [string-item   inspector-id]
             (keyword? inspected-item) [keyword-item  inspector-id]
             (var?     inspected-item) [symbol-item   inspector-id]
             (nil?     inspected-item) [nil-item      inspector-id]
             :return                   [unknown-item  inspector-id])))

(defn- atom-inspector
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  ; By using the '{height: 100%}' and the '{display: flex}' settings on this element,
  ; the inspector's height is limited to its parent element's height
  ; and the '{overflow: scroll}' setting can make the inspector's content scrollable.
  [:<> [ugly-elements/import-styles]
       [ugly-elements/column ::atom-inspector
                             {:style   {:height "100%"}
                              :content [inspected-item inspector-id]}]])

        ; DEBUG
        ; [:div (-> @state/INSPECTORS inspector-id (dissoc :ref) str)]

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn view
  ; @param (keyword)(opt) inspector-id
  ; @param (map) inspector-props
  ; {:controls (keywords in vector)(opt)
  ;   Default: [:edit-value :go-root :raw-view :remove-value :step-integer :swap-boolean :toggle-boolean]
  ;  :edit-types (keywords in vector)(opt)
  ;   Default: [:boolean :integer :keyword :map :nil :string :vector]
  ;  :header (keywords in vector)(opt)
  ;   Default: [:breadcrumbs :key-label :type-label]
  ;  :initial-path (vector)(opt)
  ;   Default: []
  ;  :ref (atom)}
  ;
  ; @usage
  ; [atom-inspector {...}]
  ;
  ; @usage
  ; [atom-inspector :my-inspector {...}]
  ;
  ;
  ; @usage
  ; (def my-atom (atom {:my-key "My value"}))
  ; [atom-inspector :my-inspector {:ref my-atom}]
  ([inspector-props]
   [view (random/generate-keyword) inspector-props])

  ([inspector-id inspector-props]
   (reagent/create-class {:reagent-render       (fn [] [atom-inspector inspector-id])
                          :component-did-mount  (fn [] (if-not (inspector-id @state/INSPECTORS)
                                                               (let [inspector-props (prototypes/inspector-props-prototype inspector-props)]
                                                                    (side-effects/inspect-path! inspector-id (:initial-path inspector-props))
                                                                    (swap! state/INSPECTORS assoc inspector-id inspector-props))))})))
