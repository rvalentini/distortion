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
; TODO idea: generify distortion by passing dist-fn

(defn distort [[x y] factor [x-center y-center]]
  (q/with-stroke ["green"]
    (q/stroke-weight 5)
    (q/point x-center y-center))
  (q/stroke-weight 2)
  (let [dist-u-x (- x x-center)
        dist-u-y (- y y-center)
        dist-u-x-norm (/ (- dist-u-x width) width)
        dist-u-y-norm (/ (- dist-u-y height) height)
        dist-d-x (* dist-u-x-norm (- 1 (* factor (Math/pow dist-u-x-norm 2))))
        dist-d-y (* dist-u-y-norm (- 1 (* factor (Math/pow dist-u-y-norm 2))))
        x-d (* dist-d-x x)
        y-d (* dist-d-y y)]
    [x-d y-d]))

(defn diamonds []
  (let [steps (range 0 50)
        points (for [x steps y steps] [x y])
        nodes (filter (fn [p] (not (or (every? odd? p) (every? even? p)))) points)
        scaled (map (fn [p] (mapv #(* 10 %) p)) nodes)]
    (q/stroke blackish)
    (q/stroke-weight 2)
    (doseq [[x y] scaled]
      (q/with-stroke ["blue"]
        (q/line [x y] [(+ 10 x) (+ 10 y)])
        (q/line [x y] [(- x 10) (+ 10 y)])))
    (doseq [[x y] scaled]
      (q/line (distort [x y] 0.1 [250 250]) (distort [(+ 10 x) (+ 10 y)] 0.1 [250 250]))
      (q/line (distort [x y] 0.1 [250 250]) (distort [(- x 10) (+ 10 y)] 0.1 [250 250])))))


(defn debug-mark-origin []
  (q/stroke "red")
  (q/stroke-weight 5)
  (q/point 0 0))

(defn setup []
  (q/smooth)
  (q/background 230 230 230)
  (q/with-translation [250 250]
    (debug-mark-origin)
    (diamonds)))

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

  (def p1 [270 270])
  (distort [1 1] 0.1 [250 250])
  (println {:a 444 :b 88}))
