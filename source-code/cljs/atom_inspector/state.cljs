
(ns atom-inspector.state
    (:require [reagent.core :refer [atom]]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @atom (map)
; The inspector stores the reference of the inspected atom and the meta items
; in the INSPECTORS atom. Its keyed by the inspector IDs to provides the ability
; of multiple using the inspector.
(defonce INSPECTORS (atom {}))
