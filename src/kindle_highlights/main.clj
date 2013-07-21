(ns kindle-highlights.main
  (:require [clojure.tools.cli :as cli]
            [kindle-highlights.core :as core]))



(defn -main
  [& args]
  (let [[optional [email password destination-file] banner] (cli/cli args
                                                    ["--author" "Restrict to this author (please pass just 1 word)"]
                                                    ["--book-list" :flag true]
                                                    ["--highlights" :flag true]
                                                    ["--book-details"])]
    (when (:book-list optional)
      (core/navigate-login-fetch-book-links email password destination-file))
    (when (:highlights optional)
      (core/fetch-highlights (:book-details optional)
                             (if (:author optional)
                               (fn [book-detail]
                                 (re-find (re-pattern (:author optional)) (nth book-detail 2)))
                               identity)
                             email
                             password
                             destination-file))))