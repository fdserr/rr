(ns ^:figwheel-always rr.example
  (:require [clojure.pprint :refer [pprint]]
            [rr.core :as rr :include-macros true]
            [rum.core :as rum :include-macros true]))

;; TODO
;; - prettify app
;; - specs

(enable-console-print!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


; (-> '(rr/defaction example [s a b] (prn a) (+ a b))
;  (macroexpand-1)
;  (pprint)
;  (with-out-str)
;  (println))


(rr/defaction init [s]
 (-> s
  (dissoc :editing)
  (assoc-in [:todos] [])))

(rr/defaction edit [s v]
 (assoc-in s [:editing] v))

(rr/defaction add-todo [s v]
 (-> s
  (update-in [:todos] conj v)
  (dissoc :editing)))

(rr/defaction toggle-todo [s i]
 (update-in s [:todos i :done] not))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn can-add-todo? [s]
 (let [i (:editing s)]
  (and
   (not= "" (:title i))
   (not (nil? (:title i))))))

(rum/defc form < rum/static [s]
 [:div#form {}
  [:input.new-todo {:value (get-in s [:editing :title] "")
                    :placeholder "What needs to be done?"
                    :on-change #(let [v (.-value (.-target %))]
                                 (rr/disp! edit {:title v}))}]
  [:button {:on-click #(rr/disp! add-todo (:editing s))
            :disabled (not (can-add-todo? s))}
    "Add!"]])

(defn todo [i e]
 (let [css-class ["todo"]
       css-class (if (:done e)
                  (conj css-class "done")
                  css-class)]
  [:li {:class css-class
        :key (str i)
        :on-click #(rr/disp! toggle-todo i)}
   (:title e)]))

(rum/defc todos < rum/static [s]
 [:div#list {}
  [:ul.todo-list
   (map-indexed todo (:todos s))]])

(rum/defc app < rum/static [s]
 (let [])
 [:div.todoapp
  (form s)
  (todos s)])

;; Save the file or run this code to initialize the app.

(defonce _ (do
            (rr/render-watch
             #(rum/mount (app %) (.getElementById js/document "app")))
            (rr/disp! init)))
