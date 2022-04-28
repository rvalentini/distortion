(ns firefly.app
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]                          ;TODO understand rdom vs. core ns.
            [quil.core :as q]
            [quil.middleware :as m]))

(defn setup []
  (q/smooth)
  (q/background 230 230 230)
  (q/stroke 130, 0 0)
  (q/stroke-weight 4)
  (let [cross-size      70
        circ-size       50
        canvas-x-center (/ (q/width) 2)
        canvas-y-center (/ (q/height) 2)
        left            (- canvas-x-center cross-size)
        right           (+ canvas-x-center cross-size)
        top             (+ canvas-y-center cross-size)
        bottom          (- canvas-y-center cross-size)]
    (q/line left bottom right top)
    (q/line right bottom left top)

    (q/fill 255 150)
    (q/ellipse canvas-x-center canvas-y-center circ-size circ-size)))

(defn canvas []
  (r/create-class
    {:component-did-mount
     (fn [component]
       (let [node (rdom/dom-node component)                 ;TODO understand what is dom-node doing?
             width (/ (.-innerWidth js/window) 2)
             height (/ (.-innerHeight js/window) 2)]
         (q/sketch
           :title "Red cross"
           :host node
           :setup setup
           :size [width height]
           :middleware [m/fun-mode])))
     :render
     (fn [] [:div])})) ;TODO why :div here?


(defn ^:dev/after-load run []
  (rdom/render [canvas] (js/document.getElementById "app")))

(comment
  ;; switch to CLJS REPL
  (shadow/repl :app)
  (js/alert "Bonjour from REPL")

  (println {:a 444 :b 88})
  )
