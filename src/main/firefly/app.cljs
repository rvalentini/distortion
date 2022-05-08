(ns firefly.app
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [quil.core :as q]
            [quil.middleware :as m]))

(def width 500)
(def height 500)
(def blackish [50 0 0])

; TODO use width and height instead
; TODO idea: color blocks by finding closest square limits surrounding a given point

(defn diamond-background []
  (let [steps (range 0 510 20)
        points-along-x (mapcat (fn [s] [[0 s] [width s]]) steps)
        points-along-y (mapcat (fn [s] [[s 0] [s height]]) steps)]
    (q/stroke blackish)
    (q/stroke-weight 2)
    (doseq [[p1 p2] (map vector points-along-x points-along-y)]
      (q/line p1 p2)
      (q/with-rotation [(- q/HALF-PI)]
        (q/with-translation [(- height) 0]
          (q/line p1 p2))))))

(defn setup []
  (q/smooth)
  (q/background 230 230 230)
  (q/with-translation [250 250]
    (diamond-background)))

(defn canvas []
  (r/create-class
    {:component-did-mount
     (fn [component]
       (let [node (rdom/dom-node component)
             c-width (* 2 width)
             c-height (* 2 height)]
         (q/sketch
           :title "Red cross"
           :host node
           :setup setup
           :size [c-width c-height]
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
