"Copyright (c) 2018.
This Source Code Form is subject to the terms of the Mozilla Public
License, v. 2.0. If a copy of the MPL was not distributed with this
file, You can obtain one at http://mozilla.org/MPL/2.0/."

(ns uk.co.hughpowell.ascension.v1.app
  (:require [mount.core :as mount]
            [uk.co.hughpowell.ascension.v1.repl])
  (:gen-class))

(defn -main [& [project-path]]
  (try
    (mount/start-with-args project-path)
    (finally
      (mount/stop))))
