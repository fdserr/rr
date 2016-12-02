(ns ^:figwheel-always rr.example
  (:require [clojure.pprint :refer [pprint]]
            [clojure.spec :as spec]
            [rr.core :as rr :include-macros true]
            [rum.core :as rum :include-macros true]))

;; TODO
;; - prettify app
;; - specs

(enable-console-print!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Look Ma, no deref, no swap!

(rr/defaction init [s]
 (-> s
  (dissoc ::editing)
  (assoc-in [::todos] [])))

(rr/defaction edit [s v]
 (assoc-in s [::editing] v))

(rr/defaction add-todo [s]
 ; {:pre [(spec/valid? s ::editing)]}
 (-> s
  (update-in [::todos] conj (::editing s))
  (dissoc ::editing)))

(rr/defaction toggle-todo [s i]
 (update-in s [::todos i ::done] not))

(defn not-empty? [s]
 (not (empty? s)))

(spec/def ::title (spec/and string? not-empty?))
(spec/def ::done boolean?)
(spec/def ::todo (spec/keys :req [::title] :opt [::done]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- handle-change [e]
 (let [v (.-value (.-target e))]
  (rr/disp! edit {::title v})))

(defn- handle-submit [_]
 (rr/disp! add-todo))

(rum/defc form < rum/static [form-value form-disabled]
 [:div#form {}
  [:input.new-todo {:value form-value
                    :placeholder "What needs to be done?"
                    :on-change handle-change}]
  [:button {:on-click handle-submit
            :disabled form-disabled}
    "Add!"]])

;;----

(defn- handle-toggle-todo [i]
 (rr/disp! toggle-todo i))

(rum/defc todo < rum/static [i e done]
 (let [css-class ["todo"]
       css-class (if done
                  (conj css-class "done")
                  css-class)]
  [:li {:class css-class
        :on-click (partial handle-toggle-todo i)}
   (::title e)]))

;;----

(defn- fn-todo [i e]
 (rum/with-key (todo i e (::done e)) i))

(rum/defc todos < rum/static [todo-list]
 [:div#list {}
  [:ul.todo-list
   (map-indexed fn-todo todo-list)]])

;;----

(defn can-add-todo? [s]
 (let [i (::editing s)]
  (and
   (not= "" (::title i))
   (some? (::title i)))))

(rum/defc app < rum/static [s]
 (let [form-value (get-in s [::editing ::title] "")
       form-disabled (not (can-add-todo? s))
       todo-list (get-in s [::todos] [])]
  [:div.todoapp
   (form form-value form-disabled)
   (todos todo-list)]))

;;----

;; Save the file or run this code to initialize the app.
(defonce _ (do
            (add-watch rr/store ::render
             #(rum/mount (app (rr/play)) (.getElementById js/document "app")))
            (rr/disp! init)))
