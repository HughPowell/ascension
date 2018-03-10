(require '[clojure.tools.namespace.repl :as nr])
(nr/refresh)
(in-ns 'user)
(def my-ns (first (filter #(= 'riemann.blueflood (ns-name %)) (all-ns))))
