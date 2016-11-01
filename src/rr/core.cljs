(ns rr.core
  (:require [clojure.pprint :refer [pprint]]))

;; TODO:
;; - cleanup, rename to Transdux
;; - robust, easy example (ns require, rum?, debug, repl vs run)
;; - doc, readme
;; ---
;; - action doc & metadata
;; - cljs/spec
;; - debugger
;;

(enable-console-print!)

;; Useful transducers in the context of t-dux.

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

;; Default xform.

(def ^:dynamic *xf*
 (comp
  (filter (fn [[kw & _]] (not= kw ::no-op)))
  (xf-sfx-step log!)
  (xf-sfx-result render!)))

;; Polymorphic reducing function (internal use, see defaction macro).

(defmulti rf
 (fn
  ([_] ::done)
  ([_ [kw & _]] kw)))

(defmethod rf ::done
 [state]
 state)

(defmethod rf ::no-op
 [state _]
 state)

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

;; Figwheel: t-dux on code reload

(defn on-js-reload []
 ; (t-dux (comp (take 10) *xf* xf-history) @store)
 (play @store))

;; Example ;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment

 (in-ns 'rr.example)

 (rr/disp! init)
 (rr/disp! add-todo {:title "Write specs."})
 (rr/disp! add-todo {:title "Develop awesome debug tools."})
 (rr/disp! toggle-todo 1)

 @store

 nil) ;;;;;;;;;;;;;;;;;;;;;;;;;;;
