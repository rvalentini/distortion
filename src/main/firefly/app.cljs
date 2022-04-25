(ns firefly.app
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]))

(defn simple-component []
  [:div
   [:p "I am a component!"]
   [:p.someclass
    "I have " [:strong "bold"]
    [:span {:style {:color "red"}} " and red "] "text."]])

(defn ^:export run []
  (rdom/render [simple-component] (js/document.getElementById "app")))

(comment
  ;; switch to CLJS REPL
  (shadow/repl :app)

  (println {:a 444 :b 88})
  )
