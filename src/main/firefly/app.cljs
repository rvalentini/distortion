(ns firefly.app
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [quil.core :as q]
            [quil.middleware :as m]))

(def width 500)
(def height 500)
(def blackish [50 0 0])

; TODO use width and height instead
; TODO make it move
; TODO idea: color blocks by finding closest square limits surrounding a given point
; TODO idea: combine multiple distortions in sequence

(defn line [x y]
  ;TODO conditional draw when on canvas
  [x y])

(defn barrel-like-dist [undistorted]
  (+ undistorted (* 2 (Math/pow (Math/log undistorted) 2))))

(defn pincushion-like-dist [undistorted]
  (+ undistorted (/ (Math/pow undistorted 2) 300)))

(defn distort [[x y] fn [center-x center-y]]
  (let [[v-x v-y] [(- x center-x) (- y center-y)]
        v-len (Math/sqrt (+ (Math/pow v-x 2) (Math/pow v-y 2)))
        [v-norm-x v-norm-y] [(/ v-x v-len) (/ v-y v-len)]
        distorted (fn v-len)
        [new-v-x new-v-y] [(* distorted v-norm-x) (* distorted v-norm-y)]
        new-p [(+ new-v-x center-x) (+ new-v-y center-y)]]
    new-p))

(defn diamonds []
  (let [steps (range 0 50)                                  ;TODO increase
        points (for [x steps y steps] [x y])
        nodes (filter (fn [p] (not (or (every? odd? p) (every? even? p)))) points)
        scaled (map (fn [p] (mapv #(* 10 %) p)) nodes)
        distort-fn barrel-like-dist]
    (q/stroke blackish)
    (q/stroke-weight 2)
    (q/with-stroke ["green"]
      (q/stroke-weight 5)
      (q/point 250 250))
    (q/stroke-weight 2)
    (doseq [[x y] scaled]
      (q/with-stroke ["blue"]
        (q/line
          (distort [x y] distort-fn [250 250])
          (distort [(+ 10 x) (+ 10 y)] distort-fn [250 250]))
        (q/line
          (distort [x y] distort-fn [250 250])
          (distort [(- x 10) (+ 10 y)] distort-fn [250 250]))))))

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

  (distort [400 400] 0.1 [250 250])

  (def p1 [270 270])
  (distort [1 1] 0.1 [250 250])
  (println {:a 444 :b 88}))
