(defproject kindle-highlights "0.1.0-SNAPSHOT"
  :description "Code to parse the kindle page and obtain a list of highlights"
  :url "http://blog.shriphani.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[clj-http "0.7.5"]
                 [clj-webdriver "0.6.0"]
                 [org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.cli "0.2.2"]
                 [enlive "1.1.1"]
                 [org.bovinegenius/exploding-fish "0.3.3"]]
  :main kindle-highlights.main)