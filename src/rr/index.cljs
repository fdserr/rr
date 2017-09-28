(ns rr.index
  (:require
   [rr.core :refer []]
   [cljs.test :refer [is testing async]]
   [sablono.core :as sab :include-macros true :refer [html]])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest defcard-doc mkdn-pprint-source]]))

(defcard
  "# Transitions Store (_aka._ trans-store)")

(defcard
  "## Transitions")

(defmulti apply-transition
  (fn [_ d] (:transition d)))

(defn transition
  [t & args]
  (let [d {:transition t
           :args args}]
    (when (apply-transition nil d)
      d)))

;; example

(defmethod apply-transition :add
  [s {[n] :args}]
  (println "add")
  (update-in s [:val] #(+ % n)))

(defmethod apply-transition :rem
  [s {[n] :args}]
  (update-in s [:val] #(- % n)))

;; end example

(deftest test-apply-transition
  (is (= {:val 1}
         (apply-transition {:val 0} (transition :add 1)))))

;;;

(defcard "## Store Constructor")

(deftype TransStore
  [transitions ^:mutable __cached]
  IEquiv
  (-equiv [o other] (and (= TransStore (type other))
                         (= (.-transitions o) (.-transitions other))))
  ICloneable
  (-clone [o] (TransStore. (.-transitions o) nil)))

(defn trans-store
  ([] (trans-store []))
  ([v] (TransStore. v nil)))

;; example

(trans-store)

(trans-store [{:transition :add :args [1]}])

;; end example

(deftest test-trans-store
  (is (= [] (.-transitions (trans-store)))))

;;;

(defcard "## Storing Transitions")

(extend-type TransStore
  ICollection
  (-conj [o t] (TransStore. (conj (.-transitions o) t) nil))
  IEmptyableCollection
  (-empty [o] (TransStore. [] nil)))

;; example

(-> (trans-store)
    (conj {:transition :add :args [40]})
    (conj {:transition :add :args [4]})
    (conj {:transition :rem :args [2]}))

;; end example

(deftest test-conj
  (is (= [{:transition :add :args [40]}
          {:transition :add :args [4]}
          {:transition :rem :args [2]}]
         (-> (trans-store)
             (conj (transition :add 40))
             (conj (transition :add 4))
             (conj (transition :rem 2))
             (.-transitions)))))

;;;

(defcard "## Obtaining Current State")

(defn state
  ([ts]
   (state ts identity))
  ([ts xf]
   (transduce xf
              (completing apply-transition)
              nil
              (.-transitions ts))))

(extend-type TransStore
  IDeref
  (-deref [o] (let [c (.-__cached o)
                    s (or c (state o))]
                (when-not c
                  (set! (.-__cached o) s))
                s)))

;; example

(-> (trans-store)
    (conj (transition :add 40))
    (conj (transition :add 4))
    (conj (transition :rem 2))
    (deref))

;; end example

(deftest test-deref
  (is (= {:val 42}
         (-> (trans-store)
             (conj (transition :add 40))
             (conj (transition :add 4))
             (conj (transition :rem 2))
             (deref)))))

;;;

(defcard "## State History")

(extend-type TransStore
  ICounted
  (-count [o] (count (.-transitions o)))
  IIndexed
  (-nth ([o i]
         (-nth o i nil))
        ([o i nf]
         (if (> i (count (.-transitions o)))
           (or nf (throw (js/Error. "Index out of bounds.")))
           (state o (take i))))))

(deftest test-nth
  (is (= {:val 44}
         (-> (trans-store [(transition :add 40)
                           (transition :add 4)
                           (transition :rem 2)])
             (nth 2)))))

;;;

(defcard "## Side effects")

(defcard "## Hooks")

(defcard "### Log")

(defcard "### Trace")

(defcard "### Watch")

(defcard "### Render UI")

(defcard "## Example: A Visual Debugger")

(defcard "## Guards")

(defcard "## State Machine")

(defcard "## Example: App Development Workflow")

(defcard "## JavaScript Interface")

;;; REPL
(comment

 (require '[rr.core :as rr])

 nil)
