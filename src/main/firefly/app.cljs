(ns firefly.app)

(defn init []
  (println "Hello World"))

(comment
  ;; switch to CLJS REPL
  (shadow/repl :firefly)

  (println {:a 444 :b 88})
  )
