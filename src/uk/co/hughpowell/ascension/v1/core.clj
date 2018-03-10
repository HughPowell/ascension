(ns uk.co.hughpowell.ascension.v1.core
  "Copyright (c) 2018.
  This Source Code Form is subject to the terms of the Mozilla Public
  License, v. 2.0. If a copy of the MPL was not distributed with this
  file, You can obtain one at http://mozilla.org/MPL/2.0/."
  (:require [clojure.set :as set]))

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

(defn- internal-vars [for-namespace call-map]
  (let [new-map (as-> call-map $
                      (vals $)
                      (apply merge-with set/union $)
                      (get $ for-namespace)
                      (set/difference $ (-> call-map keys set))
                      (map #(hash-map % (-> % resolve exit-points)) $)
                      (apply merge $))]
    (if (empty? new-map)
      call-map
      (recur for-namespace (merge call-map new-map)))))

(defn create-call-map [for-namespace]
  (let [public-call-map (into {} (map
                                   #(vector (-> % meta :name) (exit-points %))
                                   (entry-points for-namespace)))]
    (internal-vars for-namespace public-call-map)))

(defn namespace-call-map [for-namespace]
  (let [current-namespace *ns*]
    (try
      (change-to-namespace for-namespace)
      (let [result (create-call-map for-namespace)]
        (change-to-namespace current-namespace)
        result)
      (catch Exception e
        (change-to-namespace current-namespace)
        (throw e)))))