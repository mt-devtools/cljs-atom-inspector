
(ns atom-inspector.views
    (:require [atom-inspector.env          :as env]
              [atom-inspector.side-effects :as side-effects]
              [atom-inspector.state        :as state]
              [pretty.api                  :as pretty]
              [random.api                  :as random]
              [reagent.core                :as reagent]
              [string.api                  :as string]
              [syntax.api                  :as syntax]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- import-styles
  ; @ignore
  []
  [:<> [:style {:rel "stylesheet" :type "text/css"}
               ".atom-inspector button:hover {background-color: #f0f0f0}"
               ".atom-inspector button       {background-color: transparent; border: 0}"
               ".atom-inspector div          {box-sizing: border-box}"
               ".atom-inspector pre          {margin: 0}"
               ".atom-inspector textarea     {border: 0; box-sizing: border-box}"]
       [:link  {:item-prop "url" :rel "stylesheet" :type "text/css"
                :href "https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200"}]])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- inspector-title
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  ; @param (map) header-props
  [inspector-id _]
  (let [inspected-path (env/get-inspected-path inspector-id)
        root-level?    (env/root-level?        inspector-id)]
       [:pre {:style {:font-size   "14px"
                      :font-weight "500"
                      :line-height "20px"}}
             (if root-level? (-> inspector-id        str)
                             (-> inspected-path last str))]))

(defn- label-bar
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  ; @param (map) header-props
  ; {:label (string)(opt)}
  [inspector-id {:keys [label] :as header-props}]
  [:pre {:style {:color       "#888"
                 :font-size   "13px"
                 :font-weight "500"
                 :line-height "20px"}}
        (str label)])

(defn- breadcrumbs
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  ; @param (map) header-props
  [inspector-id _]
  (let [inspected-path (env/get-inspected-path inspector-id)]
       [:div {:style {:align-items      "center"
                      :background-color "#f0f0f0"
                      :color            "#888"
                      :display          "flex"
                      :height           "42px"
                      :margin-top       "6px"
                      :padding          "6px"}}
             [:pre {:style {:font-size   "12px"
                            :font-weight "500"
                            :line-height "20px"}}
                   (string/join inspected-path " || ")]]))

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

(defn- icon-button
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  ; @param (map) button-props
  ; {:disabled? (boolean)(opt)
  ;  :icon (keyword)
  ;  :label (string)
  ;  :on-click (function)}
  [inspector-id {:keys [disabled? icon label on-click]}]
  [:button {:on-click (if-not disabled? on-click)
            :style {:opacity (if disabled? ".5")
                    :cursor  (if disabled? "default")
                    :align-items     "center"
                    :display         "flex"
                    :flex-direction  "column"
                    :height          "60px"
                    :justify-content "center"
                    :user-select     "none"
                    :width           "60px"}}
           [:i {:class :material-symbols-outlined
                :style {:font-size   "24px"
                        :font-weight "200"}}
               icon]
           [:pre {:style {:font-size "12px"}} label]])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- decrease-integer-button
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  [icon-button inspector-id {:icon :remove :label "Dec"
                             :on-click #(side-effects/update-inspected-item! inspector-id dec)}])

(defn- increase-integer-button
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  [icon-button inspector-id {:icon :add :label "Inc"
                             :on-click #(side-effects/update-inspected-item! inspector-id inc)}])

(defn- swap-boolean-button
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  [icon-button inspector-id {:icon :change_circle :label "Swap"
                             :on-click #(side-effects/update-inspected-item! inspector-id not)}])


(defn- toggle-raw-view-button
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [raw-view? (env/raw-view? inspector-id)]
       [icon-button inspector-id {:icon  (if raw-view? :code_off :code)
                                  :label (if raw-view? "Raw" "Raw")
                                  :on-click #(side-effects/toggle-raw-view! inspector-id)}]))

(defn- go-home-button
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [root-level? (env/root-level? inspector-id)]
       [icon-button inspector-id {:icon :home :label "Root" :disabled? root-level?
                                  :on-click #(side-effects/go-home! inspector-id)}]))

(defn- go-up-button
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [root-level? (env/root-level? inspector-id)]
       [icon-button inspector-id {:icon :chevron_left :label "Go up" :disabled? root-level?
                                  :on-click #(side-effects/go-up! inspector-id)}]))

(defn- remove-item-button
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  [icon-button inspector-id {:icon :delete :label "Remove"
                             :on-click #(side-effects/remove-inspected-item! inspector-id)}])

(defn- edit-item-button
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [edit-mode? (env/edit-mode? inspector-id)]
       [icon-button inspector-id {:icon     (if edit-mode? :edit_off :edit)
                                  :label    (if edit-mode? "Save" "Edit")
                                  :on-click #(side-effects/toggle-edit-mode! inspector-id)}]))

(defn- recycle-item-button
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (if (env/inspected-item-removed? inspector-id)
      [icon-button inspector-id {:icon :recycling :label "Restore"
                                 :on-click #(side-effects/restore-inspected-item! inspector-id)}]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- toolbar
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  ; @param (list of Reagent component symbols) buttons
  [inspector-id & buttons]
  [:div {:style {:padding-bottom "12px"}}
        [:div {:style {:display "flex"}}
              (letfn [(f [%1 %2] (conj %1 [%2 inspector-id]))]
                     (reduce f [:<>] buttons))]
        [:div {:style {:width "100%" :height "1px" :border "1px dashed #ddd"}}]])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- empty-item-label
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [inspected-item (env/get-inspected-item inspector-id)]
       (if (empty? inspected-item)
           [:pre {:style {:font-size "13px" :line-height "20px"}}
                 "Empty"])))

(defn- raw-item
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (if-let [raw-view? (env/raw-view? inspector-id)]
          (let [inspected-item (env/get-inspected-item inspector-id)]
               [:div {:style {:padding-top "48px"}}
                     [:pre {:style {:font-size "13px" :line-height "20px"
                                    :padding "6px" :background-color "#f0f0f0"}}
                           (pretty/mixed->string inspected-item)]])))

(defn- item-editor
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (letfn [(f [v] (swap! state/INSPECTORS assoc-in [inspector-id :meta-items :edit-copy] v))]
         (if-let [edit-mode? (env/edit-mode? inspector-id)]
                 (let [edit-copy (env/get-edit-copy inspector-id)]
                      [:pre {:style {:padding-top "48px"}}
                            [:textarea {:value edit-copy
                                        :style {:font-size "13px" :line-height "20px"
                                                :min-height "420px" :width "100%"
                                                :padding "6px" :background-color "#f0f0f0"}
                                        :on-change (fn [e] (let [v (-> e .-target .-value)]
                                                                (f v)))}]]))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- map-key
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  ; @param (*) key
  [inspector-id key]
  [:button {:style {:cursor "pointer" :display "block"}
            :on-click #(side-effects/inspect-key! inspector-id key)}
           [:pre {:style {:font-size "13px" :line-height "20px"}}
                 (cond (string? key) (syntax/quotes key)
                       (nil?    key) (str           "nil")
                       :return       (str           key))]])

(defn- map-item
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [inspected-item (env/get-inspected-item     inspector-id)
        map-keys       (env/get-inspected-map-keys inspector-id)]
       [:<> [header  inspector-id {:label (str "map, "(count map-keys)" item(s)")}]
            [toolbar inspector-id go-home-button go-up-button remove-item-button toggle-raw-view-button edit-item-button]
            [empty-item-label inspector-id]
            [:div {:style {:overflow "scroll"}}
                  (letfn [(f [%1 %2] (conj %1 [map-key inspector-id %2]))]
                         (reduce f [:<>] map-keys))
                  [raw-item    inspector-id]
                  [item-editor inspector-id]]]))

(defn- vector-key
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  ; @param (*) key
  [_ key]
  [:pre {:style {:font-size "13px" :line-height "20px"}}
        (cond (nil?    key) (str "nil")
              (string? key) (syntax/quotes key)
              :return       (str           key))])

(defn- vector-item
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [inspected-item (env/get-inspected-item inspector-id)]
       [:<> [header  inspector-id {:label (str "vector, " (count inspected-item) " item(s)")}]
            [toolbar inspector-id go-home-button go-up-button remove-item-button toggle-raw-view-button edit-item-button]
            [empty-item-label inspector-id]
            [:div {:style {:overflow "scroll"}}
                  (letfn [(f [%1 %2] (conj %1 [vector-key inspector-id %2]))]
                         (reduce f [:<>] inspected-item))
                  [raw-item    inspector-id]
                  [item-editor inspector-id]]]))

(defn- boolean-item
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [inspected-item (env/get-inspected-item inspector-id)]
       [:<> [header  inspector-id {:label "boolean"}]
            [toolbar inspector-id go-home-button go-up-button remove-item-button swap-boolean-button]
            [:pre {:style {:font-size "13px" :line-height "20px"}}
                  (str inspected-item)]]))

(defn- integer-item
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [inspected-item (env/get-inspected-item inspector-id)]
       [:<> [header  inspector-id {:label "integer"}]
            [toolbar inspector-id go-home-button go-up-button remove-item-button decrease-integer-button increase-integer-button edit-item-button]
            [:pre {:style {:font-size "13px" :line-height "20px"}}
                  (str inspected-item)]
            [item-editor inspector-id]]))

(defn- string-item
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [inspected-item (env/get-inspected-item inspector-id)]
       [:<> [header  inspector-id {:label (str "string, "(count inspected-item) " char")}]
            [toolbar inspector-id go-home-button go-up-button remove-item-button edit-item-button]
            [:div {:style {:overflow "scroll"}}
                  [:pre {:style {:font-size "13px" :line-height "20px" :white-space "normal"}}
                        (syntax/quotes inspected-item)]
                  [item-editor inspector-id]]]))

(defn- keyword-item
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [inspected-item (env/get-inspected-item inspector-id)]
       [:<> [header  inspector-id {:label "keyword"}]
            [toolbar inspector-id go-home-button go-up-button remove-item-button edit-item-button]
            [:pre {:style {:font-size "13px" :line-height "20px" :white-space "normal"}}
                  (str inspected-item)]
            [item-editor inspector-id]]))

(defn- symbol-item
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [inspected-item (env/get-inspected-item inspector-id)]
       [:<> [header  inspector-id {:label "symbol"}]
            [toolbar inspector-id go-home-button go-up-button remove-item-button]
            [:pre {:style {:font-size "13px" :line-height "20px" :white-space "normal"}}
                  (str inspected-item)]]))

(defn- nil-item
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  [:<> [header  inspector-id {:label "nil"}]
       [toolbar inspector-id go-home-button go-up-button recycle-item-button edit-item-button]
       [:pre {:style {:font-size "13px" :line-height "20px"}} "nil"]
       [item-editor inspector-id]])

(defn- unknown-item
  ; @ignore
  ;
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [inspected-item (env/get-inspected-item inspector-id)]
       [:<> [header  inspector-id {:label "unknown"}]
            [toolbar inspector-id go-home-button go-up-button remove-item-button]
            [:pre {:style {:font-size "13px" :line-height "20px" :white-space "normal"}}
                  (str inspected-item)]]))

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
  [:div {:class :atom-inspector :style {:display "flex" :flex-direction "column" :height "100%"}}
        [import-styles]
        [inspected-item inspector-id]])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn view
  ; @param (keyword)(opt) inspector-id
  ; @param (map) inspector-props
  ; {:ref (atom)}
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
   (reagent/create-class {:reagent-render      (fn [] [atom-inspector inspector-id])
                          :component-did-mount (fn [] (if-not (inspector-id @state/INSPECTORS)
                                                              (swap! state/INSPECTORS assoc inspector-id inspector-props)))})))
