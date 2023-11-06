
(ns atom-inspector.state
    (:require [reagent.core :refer [atom]]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @atom (map)
; - The inspector stores the reference of the inspected atom and the meta items in the 'INSPECTORS' atom.
; - Its keyed by the inspector IDs to provide the ability of multiple use of the inspector.
(defonce INSPECTORS (atom {}))
