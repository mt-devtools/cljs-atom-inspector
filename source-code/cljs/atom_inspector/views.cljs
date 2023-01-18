
(ns atom-inspector.views
    (:require [atom-inspector.helpers      :as helpers]
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
  []
  ; The atom-inspector uses the bithandshake/pretty-css atomic preset and theme kit
  ; and the Material Symbols icon set.
  ; https://github.com/bithandshake/pretty-css
  ; https://fonts.google.com/icons
  ;
  ; Importing the required CSS styles:
  ; - normalize.min.css
  ; - pretty-css.min.css
  ; - material-symbols.min.css
  ; - Material Symbols icon set (hosted by Google Fonts)
  (re-frame.api/dispatch [:x.db/set-item! [:my-data] {}])
  [:<> [:link {:item-prop "url" :rel "stylesheet" :type "text/css"
               :href "https://github.com/bithandshake/pretty-css/blob/release/resources/public/normalize.min.css"}]
       [:link {:item-prop "url" :rel "stylesheet" :type "text/css"
               :href "https://github.com/bithandshake/pretty-css/blob/release/resources/public/pretty-css.min.css"}]
       [:link {:item-prop "url" :rel "stylesheet" :type "text/css"
               :href "https://github.com/bithandshake/pretty-css/blob/release/resources/public/material-symbols.min.css"}]
       [:link {:item-prop "url" :rel "stylesheet" :type "text/css"
               :href "https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200"}]])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- inspector-title
  ; @param (keyword) inspector-id
  ; @param (map) header-props
  [inspector-id _]
  (let [inspected-path (helpers/get-inspected-path inspector-id)
        root-level?    (helpers/root-level?        inspector-id)]
       [:div {:data-font-weight :medium :data-font-size :s :data-line-height :text-block}
             (if root-level? (-> inspector-id        str)
                             (-> inspected-path last str))]))

(defn- label-bar
  ; @param (keyword) inspector-id
  ; @param (map) header-props
  ; {:label (string)(opt)}
  [inspector-id {:keys [label] :as header-props}]
  [:div {:data-font-weight :medium :data-font-size :xs :data-color :muted :data-line-height :text-block}
        (str " > " label)])

(defn- breadcrumbs
  ; @param (keyword) inspector-id
  ; @param (map) header-props
  [inspector-id _]
  (let [inspected-path (helpers/get-inspected-path inspector-id)]
       [:div {:data-font-weight :medium :data-font-size :xs :data-color :muted :data-line-height :text-block
              :data-block-height :xl :data-orientation :horizontal :data-vertical-row-align :center
              :data-fill-color :highlight :data-indent-vertical :xxs}
             (string/join inspected-path " / ")]))

(defn- header
  ; @param (keyword) inspector-id
  ; @param (map) header-props
  ; {:label (string)(opt)}
  [inspector-id header-props]
  [:div [inspector-title inspector-id header-props]
        [label-bar       inspector-id header-props]
        [breadcrumbs     inspector-id header-props]])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- icon-button
  ; @param (keyword) inspector-id
  ; @param (map) button-props
  ; {:disabled? (boolean)(opt)
  ;  :icon (keyword)
  ;  :label (string)
  ;  :on-click (function)}
  [inspector-id {:keys [disabled? icon label on-click]}]
  [:div {:data-disabled     disabled?
         :data-hover-color  (if-not disabled? :highlight)
         :on-click          (if-not disabled? on-click)
         :data-block-height            :5xl
         :data-block-width             :5xl
         :data-orientation             :vertical
         :data-click-effect            :opacity
         :data-horizontal-column-align :center
         :data-vertical-column-align   :bottom
         :data-selectable              false}
        [:i   {:data-icon-family :material-symbols-outlined :data-icon-size :m} icon]
        [:pre {:data-font-size :xxs} label]])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- decrease-integer-button
  ; @param (keyword) inspector-id
  [inspector-id]
  [icon-button inspector-id {:icon :remove :label "Dec"
                             :on-click #(side-effects/update-inspected-item! inspector-id dec)}])

(defn- increase-integer-button
  ; @param (keyword) inspector-id
  [inspector-id]
  [icon-button inspector-id {:icon :add :label "Inc"
                             :on-click #(side-effects/update-inspected-item! inspector-id inc)}])

(defn- swap-boolean-button
  ; @param (keyword) inspector-id
  [inspector-id]
  [icon-button inspector-id {:icon :change_circle :label "Swap"
                             :on-click #(side-effects/update-inspected-item! inspector-id not)}])


(defn- toggle-raw-view-button
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [raw-view? (helpers/raw-view? inspector-id)]
       [icon-button inspector-id {:icon  (if raw-view? :code_off :code)
                                  :label (if raw-view? "Raw" "Raw")
                                  :on-click #(side-effects/toggle-raw-view! inspector-id)}]))

(defn- go-home-button
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [root-level? (helpers/root-level? inspector-id)]
       [icon-button inspector-id {:icon :home :label "Root" :disabled? root-level?
                                  :on-click #(side-effects/go-home! inspector-id)}]))

(defn- go-up-button
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [root-level? (helpers/root-level? inspector-id)]
       [icon-button inspector-id {:icon :chevron_left :label "Go up" :disabled? root-level?
                                  :on-click #(side-effects/go-up! inspector-id)}]))

(defn- remove-item-button
  ; @param (keyword) inspector-id
  [inspector-id]
  [icon-button inspector-id {:icon :delete :label "Remove"
                             :on-click #(side-effects/remove-inspected-item! inspector-id)}])

(defn- edit-item-button
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [edit-mode? (helpers/edit-mode? inspector-id)]
       [icon-button inspector-id {:icon     (if edit-mode? :edit_off :edit)
                                  :label    (if edit-mode? "Save" "Edit")
                                  :on-click #(side-effects/toggle-edit-mode! inspector-id)}]))

(defn- recycle-item-button
  ; @param (keyword) inspector-id
  [inspector-id]
  (if (helpers/inspected-item-removed? inspector-id)
      [icon-button inspector-id {:icon :recycling :label "Restore"
                                 :on-click #(side-effects/restore-inspected-item! inspector-id)}]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- toolbar
  ; @param (keyword) inspector-id
  ; @param (list of symbols) buttons
  [inspector-id & buttons]
  [:div {:data-outdent-bottom :s}
        [:div {:data-orientation :horizontal :data-indent-horizontal :xs}
              (letfn [(f [%1 %2] (conj %1 [%2 inspector-id]))]
                     (reduce f [:<>] buttons))]
        [:div {:style {:width "100%" :height "1px" :border "1px dashed var( --border-color-highlight)"}}]])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- raw-item
  ; @param (keyword) inspector-id
  [inspector-id]
  (if-let [raw-view? (helpers/raw-view? inspector-id)]
          (let [inspected-item (helpers/get-inspected-item inspector-id)]
               [:div {:data-indent-top :xxl}
                     [:pre {:data-font-size :xs :data-fill-color :highlight :data-indent-all :xxs}
                           (pretty/mixed->string inspected-item)]])))

(defn- item-editor
  ; @param (keyword) inspector-id
  [inspector-id]
  (letfn [(f [v] (swap! state/INSPECTORS assoc-in [inspector-id :meta-items :edit-copy] v))]
         (if-let [edit-mode? (helpers/edit-mode? inspector-id)]
                 (let [edit-copy (helpers/get-edit-copy inspector-id)]
                      [:pre {:data-indent-top :xxl}
                            [:textarea {:data-fill-color :highlight :value edit-copy
                                        :data-indent-all :xxs
                                        :style {:min-height "420px" :width "100%"}
                                        :on-change (fn [e] (let [v (-> e .-target .-value)]
                                                                (f v)))}]]))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- map-key
  ; @param (keyword) inspector-id
  ; @param (*) key
  [inspector-id key]
  [:button {:style {:display :block} :data-hover-effect :opacity
            :on-click #(side-effects/inspect-key! inspector-id key)}
           (cond (string? key) (syntax/quotes key)
                 (nil?    key) (str           "nil")
                 :return       (str           key))])

(defn- map-item
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [inspected-item (helpers/get-inspected-item     inspector-id)
        map-keys       (helpers/get-inspected-map-keys inspector-id)]
       [:pre {:data-font-size :xs}
             [header  inspector-id {:label (str "map, "(count map-keys)" item(s)")}]
             [toolbar inspector-id go-home-button go-up-button remove-item-button toggle-raw-view-button edit-item-button]
             (if (empty? inspected-item) "Empty")
             (letfn [(f [%1 %2] (conj %1 [map-key inspector-id %2]))]
                    (reduce f [:<>] map-keys))
             [raw-item    inspector-id]
             [item-editor inspector-id]]))

(defn- vector-item
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [inspected-item (helpers/get-inspected-item inspector-id)]
       [:div [header  inspector-id {:label (str "vector, " (count inspected-item) " item(s)")}]
             [toolbar inspector-id go-home-button go-up-button remove-item-button toggle-raw-view-button edit-item-button]
             (if (empty? inspected-item) "Empty")
             (letfn [(f [%1 %2] (conj %1 [:pre {:data-font-size :xs}
                                               (cond (nil?    %2) (str "nil")
                                                     (string? %2) (syntax/quotes %2)
                                                     :return      (str           %2))]))]
                    (reduce f [:pre] inspected-item))
             [raw-item    inspector-id]
             [item-editor inspector-id]]))

(defn- boolean-item
  ; @param (keyword) inspector-id
  [inspector-id]
  [:div [header  inspector-id {:label "boolean"}]
        [toolbar inspector-id go-home-button go-up-button remove-item-button swap-boolean-button]
        (let [inspected-item (helpers/get-inspected-item inspector-id)]
             [:pre (str inspected-item)])])

(defn- integer-item
  ; @param (keyword) inspector-id
  [inspector-id]
  [:div [header  inspector-id {:label "integer"}]
        [toolbar inspector-id go-home-button go-up-button remove-item-button decrease-integer-button increase-integer-button edit-item-button]
        (let [inspected-item (helpers/get-inspected-item inspector-id)]
             [:pre (str inspected-item)]
             [item-editor inspector-id])])

(defn- string-item
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [inspected-item (helpers/get-inspected-item inspector-id)]
       [:div [header  inspector-id {:label (str "string, "(count inspected-item) " char.")}]
             [toolbar inspector-id go-home-button go-up-button remove-item-button edit-item-button]
             [:pre {:style {:white-space :normal} :data-font-size :xs}
                   (syntax/quotes inspected-item)]
             [item-editor inspector-id]]))

(defn- keyword-item
  ; @param (keyword) inspector-id
  [inspector-id]
  [:div [header  inspector-id {:label "keyword"}]
        [toolbar inspector-id go-home-button go-up-button remove-item-button edit-item-button]
        (let [inspected-item (helpers/get-inspected-item inspector-id)]
             [:pre {:style {:white-space :normal} :data-font-size :xs}
                   (str inspected-item)])
        [item-editor inspector-id]])

(defn- symbol-item
  ; @param (keyword) inspector-id
  [inspector-id]
  [:div [header  inspector-id {:label "symbol"}]
        [toolbar inspector-id go-home-button go-up-button remove-item-button]
        (let [inspected-item (helpers/get-inspected-item inspector-id)]
             [:pre {:style {:white-space :normal} :data-font-size :xs}
                   (str inspected-item)])])

(defn- nil-item
  ; @param (keyword) inspector-id
  [inspector-id]
  [:div [header  inspector-id {:label "nil"}]
        [toolbar inspector-id go-home-button go-up-button recycle-item-button edit-item-button]
        [:pre {:data-font-size :xs} "nil"]
        [item-editor inspector-id]])

(defn- unknown-item
  ; @param (keyword) inspector-id
  [inspector-id]
  [:div [header  inspector-id {:label "unknown"}]
        [toolbar inspector-id go-home-button go-up-button remove-item-button]
        (let [inspected-item (helpers/get-inspected-item inspector-id)]
             [:pre {:style {:white-space :normal} :data-font-size :xs}
                   (str inspected-item)])])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn inspected-item
  ; @param (keyword) inspector-id
  [inspector-id]
  (let [inspected-item (helpers/get-inspected-item inspector-id)]
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
  ; @param (keyword) inspector-id
  [inspector-id]
  [:div {:data-scroll-axis :x :data-indent-all :xs :data-fill-color :default}
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
                          :component-did-mount (fn [] (swap! state/INSPECTORS assoc inspector-id inspector-props))})))
