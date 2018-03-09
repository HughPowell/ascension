(ns uk.co.hughpowell.ascension.v1.core
  "Copyright (c) 2018.
  This Source Code Form is subject to the terms of the Mozilla Public
  License, v. 2.0. If a copy of the MPL was not distributed with this
  file, You can obtain one at http://mozilla.org/MPL/2.0/.")

(defn- entry-points [for-namespace]
  (vals (ns-publics for-namespace)))

(def change-to-namespace (comp in-ns ns-name))

(defn- exit-points [v]
  (let [var-meta (meta v)
        var-name (str (:name var-meta))
        var-namespace (ns-name (:ns var-meta))
        fully-qualified-name (str var-namespace "/" var-name)]
    (->> fully-qualified-name
         symbol
         clojure.repl/source-fn
         read-string
         flatten
         (filter symbol?)
         (map resolve)
         (filter some?)
         set
         (map #(select-keys (meta %) [:ns :name]))
         (group-by :ns)
         (map (fn [[ns symbols]] (vector ns
                                         (disj (set (map :name symbols))
                                               (symbol var-name)))))
         (into {}))))

(defn namespace-call-map [for-namespace]
  (let [current-namespace *ns*]
    (try
      (change-to-namespace for-namespace)
      (let [call-map (into {} (map
                                #(vector (-> % meta :name) (exit-points %))
                                (entry-points for-namespace)))]
        (change-to-namespace current-namespace)
        call-map)
      (catch Exception _
        (change-to-namespace current-namespace)))))