(ns firefly.app
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [quil.core :as q]
            [quil.middleware :as m]))

(defn diamond-background []
  (let [steps (range 0 510 10)
        points-along-x (map #(identity [0 %]) steps)
        points-along-y (map #(identity [% 0]) steps)]
    (q/stroke 50 0 0)
    (q/stroke-weight 2)
    (doseq [[p1 p2] (map vector points-along-x points-along-y)]
      (q/line p1 p2))))

(defn setup []
  (q/smooth)
  (q/background 230 230 230)
  (diamond-background))

(defn canvas []
  (r/create-class
    {:component-did-mount
     (fn [component]
       (let [node (rdom/dom-node component)
             width 500
             height 500]
         (q/sketch
           :title "Red cross"
           :host node
           :setup setup
           :size [width height]
           :middleware [m/fun-mode])))
     :render
     (fn [] [:div])}))


(defn ^:dev/after-load run []
  (rdom/render [canvas] (js/document.getElementById "app")))

(comment
  ;; switch to CLJS REPL
  (shadow/repl :app)
  (js/alert "Bonjour from REPL")

  (println {:a 444 :b 88})
  )
