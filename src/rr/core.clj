(ns rr.core)
; (:require [clojure.spec :as spec]

; (spec/fdef defaction
;  :args (spec/cat ::label simple-symbol?
;                  ::args vector?
;                  ::body (spec/or :s-expr seq?
;                                  :symbol simple-symbol?))
;  :ret any?)

;; TODO
;; - ensure state is first arg
;; - deal with docstring and metadata

(defmacro defaction [label args & body]
 "An action is a (pure) function (state:map, & args:printable -> state:map)."
  `(do
    (def ~label (memoize (fn ~args ~@body)))
    (defmethod rr.core/rf
               ~(keyword (str *ns*) (name label))
               [~(first args) [~'_ ~@(rest args)]]
               (~label ~@args))))

(defmacro disp! [action & args]
 "Dispatch action & args:printable. Returns nil."
 `(-disp! ~(keyword (str *ns*) (name action)) ~@args))
