(ns rr.core
  (:require [clojure.pprint :refer [pprint]]))

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

(defn log!
 "Default logging (JS console)."
 ([r] (log! r nil))
 ([r i] (let [pr (if i "STEP RESULT:\n" "FINAL RESULT:\n")
              sr (str pr (with-out-str (pprint r)))
              si (when i (str "STEP INPUT:\n" (with-out-str (pprint i))))]
         (.info js/console (str si sr)))))

(defn render! [r]
 "Default rendering (JS console)."
 (.info js/console (str "RENDER:\n" (with-out-str (pprint r)))))

;; State management.

(defn- validator [{:keys [::initial-state ::actions-history]}]
 (let [r (reduce rf initial-state actions-history)]
  ;TODO (conform r)
  true))

(defonce store
  (atom {::initial-state {}
         ::actions-history []}))
        ; :validator validator))

(declare play)

;; Dispatch action (internal use, see disp! macro).

(defn -disp!
 [& args]
 (swap! store update-in [::actions-history] conj (vec args))
 nil)

;; Default xform.

(def ^:dynamic *xf*
 (map identity))
 ; (comp
 ;  (filter (fn [[kw & _]] (not= kw ::no-op)))
 ;  (xf-sfx-step log!)
 ;  (xf-sfx-result render!)))

;; Transdux!

(defn play
 "Run the reduction, with optional xf transform."
 ([]
  (play *xf* @store))
 ([xf]
  (binding [*xf* xf]
   (play)))
 ([xf s]
  (let [{:keys [::initial-state ::actions-history]} s]
   (binding [*xf* xf]
    (transduce *xf* rf initial-state actions-history)))))

(defn commit!
 "Set initial state to be the result of the reduction, with optional
  xf transducer. Empty the history of actions, such that initial state and
  reduction are equal. Idempotent."
 ([]
  (commit! *xf* store))
 ([xf]
  (binding [*xf* xf]
   (commit!)))
 ([xf s]
  (::initial-state
   (swap! s (fn [m]
             (let [v (play *xf* m)]
              (-> m
               (assoc ::initial-state v)
               (assoc ::actions-history []))))))))


;;;;;;;;;

(defn- fifo-assoc [m k v i]
 (let [m (assoc m k v)
       ks (keys m)
       c (count ks)]
  (if (< c i)
   m
   (select-keys m (rest ks)))))

(defn- memoize-fifo
  [f]
  (let [mem (atom {})]
    (fn [& args]
      (if-let [e (find @mem args)]
        (val e)
        (let [ret (apply f args)]
          (swap! mem fifo-assoc args ret 64)
          ret)))))

;; Figwheel: play on code reload
(defn on-js-reload []
 ; (play (comp (take 10) *xf* xf-history) @store)
 (play))
