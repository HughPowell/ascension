"Copyright (c) 2018.
This Source Code Form is subject to the terms of the Mozilla Public
License, v. 2.0. If a copy of the MPL was not distributed with this
file, You can obtain one at http://mozilla.org/MPL/2.0/."

(ns uk.co.hughpowell.ascension.v1.repl
  (:require [mount.core :as mount]
            [uk.co.hughpowell.ascension.v1.config :as config]
            [clojure.tools.nrepl :as nrepl]
            [clojure.java.shell :as shell])
  (:import (java.net ConnectException)))

(declare client)

(mount/defstate nrepl-server
  :start (shell/with-sh-dir (mount/args)
                            "lein with-profiles \"dev,ascension\" repl")
  :stop (nrepl/message client {:op   :eval
                               :code "(exit)"}))

(mount/defstate conn
  :start (loop [n 30]
           (when-not (zero? n)
             (try
               (nrepl/connect
                 :port
                 (get-in config/config [:server :port]))
               (println "connected")
               (catch ConnectException e e))))
  :stop (.close conn))

(mount/defstate client
  :start (nrepl/client conn (get-in config/config [:client :timeout])))
