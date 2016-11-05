(ns rr.core
  (:require [clojure.pprint :refer [pprint]]))

;; TODO:
;; - specs / tests
;; - should commit! take default xf?
;; - debugger
;; - optimize example (require rr, rum?)
;;

;; CHANGES:
;; 0.1.1
;; - add: rf arity 0
;; - add: defaction can take a docstring and metadata (cljs "meta" gotchas still apply).


(enable-console-print!)

;; Polymorphic reducing function (internal use, see defaction macro).

(defmulti rf
 (fn
  ([] ::rf)
  ([_] ::done)
  ([_ [kw & _]] kw)))

(defmethod rf ::rf
 []
 (with-meta {} ::rr-state))

(defmethod rf ::done
 [state]
 state)

(defmethod rf ::no-op
 [state _]
 state)

;; Default transducers.

(defn xf-sfx-result [f!]
 "Transducer to apply side-effecting function f! to the final result
  of the reduction (eg. rendering). The value produced by f! is ignored,
  and the transduction is equivalent to the reduction."
 (fn [rf]
  (fn
   ([] (rf))
   ([result]
    (let [r (rf result)]
     (f! r)
     r))
   ([result input]
    (rf result input)))))

(defn xf-sfx-step [f!]
 "Transducer to apply side-effecting function f! (result, input?) to each step
  of the reduction (eg. logging). The value produced by f! is ignored,
  and the transduction is equivalent to the reduction."
 (fn [rf]
  (fn
   ([] (rf))
   ([result]
    (let [r (rf result)]
     (f! r)
     r))
   ([result input]
    (let [r (rf result input)]
     (f! r input)
     r)))))

(def xf-history
 "Transducer to build up and return a history of the reduction, in the form:
  [{:step <action>
    :result <state map>}
  ...]."
 (fn [rf]
  (let [h (volatile! [])]
   (fn
    ([] (rf))
    ([result]
     (let [_ (rf result)
           r @h]
      (vreset! h [])
      r))
    ([result input]
     (let [r (rf result input)]
      (vswap! h conj {:result r :step input})
      r))))))

;; Default logging.

(defn log!
 ([r] (log! r nil))
 ([r i] (let [pr (if i "STEP RESULT:\n" "FINAL RESULT:\n")
              sr (str pr (with-out-str (pprint r)))
              si (when i (str "STEP INPUT:\n" (with-out-str (pprint i))))]
         (.info js/console (str si sr)))))

;; Default rendering.

(defn render! [r]
 (.info js/console (str "RENDER:\n" (with-out-str (pprint r)))))

;; State management.

(defn validator [{:keys [::initial-state ::actions-history]}]
 (let [r (reduce rf initial-state actions-history)]
  ;TODO (conform r)
  true))

(defonce store
  (atom {::initial-state {}
         ::actions-history []}))
        ; :validator validator))

(declare play)

(defn render-watch [f]
 (let [xf (xf-sfx-result f)]
  (add-watch store :render #(play xf %4))))

;; Dispatch action (internal use, see disp! macro).

(defn -disp!
 [& args]
 (swap! store update-in [::actions-history] conj (vec args))
 nil)

;; Default xform.

(def ^:dynamic *xf*
 (comp
  (filter (fn [[kw & _]] (not= kw ::no-op)))
  (xf-sfx-step log!)
  (xf-sfx-result render!)))

;; Transdux!

(defn play
 ([]
  (play @store))
 ([s]
  (let [{:keys [::initial-state ::actions-history]} s]
   (transduce *xf* rf initial-state actions-history)))
 ([xf s]
  (binding [*xf* xf]
   (play s))))

(defn commit! []
 ""
 (::initial-state
  (swap! store (fn [s]
                (let [v (play s)]
                 (-> s
                  (assoc ::initial-state v)
                  (assoc ::actions-history [])))))))


;; Figwheel: play on code reload

(defn on-js-reload []
 ; (play (comp (take 10) *xf* xf-history) @store)
 (play))

;; Example ;;;;;;;;;;;;;;;;;;;;;;;;;;



(comment
 ;; Hit CMD-S to render once.

 (in-ns 'rr.example)
 (rr/disp! add-todo {:title "Write specs."})
 (rr/disp! add-todo {:title "Develop awesome debug tools."})
 (rr/disp! toggle-todo 1)

 @rr/store

 nil) ;;;;;;;;;;;;;;;;;;;;;;;;;;;

; (->
;  '(defaction
;    ^:foo
;    example
;    "awesome doc"
;    {:foo :bar}
;    [s a b]
;    (prn a)
;    (+ a b))
;  (macroexpand-1)
;  (pprint)
;  (with-out-str)
;  (println))
