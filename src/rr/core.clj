(ns rr.core)
 ; (:require [cljs.analyzer :as cljs]
 ;           [clojure.spec :as spec]))

;; TODO
;; - ensure state is first arg and is being returned (ouchy!)
;; - fix to access cljs metadata?

;; -- metadata
;; this breaks cljs analyzer on second call at the REPL:
; (defmacro var-data
;   [sym]
;   (cljs/resolve-var &env sym))

;; -- specs tbc...
; (spec/fdef defaction
;  :args (spec/cat ::label simple-symbol?
;                  ::args vector?
;                  ::body (spec/or :s-expr seq?
;                                  :symbol simple-symbol?))
;  :ret any?)

(defmacro defaction [& args]
 "An action is a (pure) function (state:map, & args:printable -> state:map)."
 {:arglists '([name docstring? attr-map? args-vector & body])}
 (let [m (if (symbol? (first args))
          (meta (first args))
          {})
       [label args] (if (symbol? (first args))
                     (vector (first args) (rest args))
                     (throw (IllegalArgumentException. "First argument must be a symbol.")))
       [doc args] (if (string? (first args))
                   (vector (first args) (rest args))
                   (vector nil args))
       [m args] (if (map? (first args))
                  (vector (merge m (first args)) (rest args))
                  (vector m args))
       [args body] (if (vector? (first args))
                    (vector (first args) (rest args))
                    (throw (IllegalArgumentException. "Args must be a vector.")))
       m (if doc (assoc m :doc doc) m)
       label (with-meta label m)]
  `(do
    (def ~label (memoize (fn ~args ~@body)))
    (defmethod rr.core/rf
               ~(keyword (str *ns*) (name label))
               [~(first args) [~'_ ~@(rest args)]]
               (~label ~@args)))))

(defmacro disp! [action & args]
 "Dispatch action & args:printable. Returns nil."
 {:arglists '([name & args?])}
 `(-disp! ~(keyword (str *ns*) (name action)) ~@args))
