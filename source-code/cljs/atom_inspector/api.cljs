
(ns atom-inspector.api
    (:require [atom-inspector.env          :as env]
              [atom-inspector.side-effects :as side-effects]
              [atom-inspector.views        :as views]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; atom-inspector.env
(def get-inspected-path env/get-inspected-path)
(def get-inspected-item env/get-inspected-item)

; atom-inspector.side-effects
(def inspect-path! side-effects/inspect-path!)
(def inspect-key!  side-effects/inspect-key!)

; atom-inspector.views
(def view views/view)
