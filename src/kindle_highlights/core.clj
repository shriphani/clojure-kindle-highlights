(ns kindle-highlights.core
  (:require [clj-webdriver.taxi :as taxi]
            [clj-webdriver.element :as element]
            [clojure.string :as string]
            [clojure.tools.cli :as cli]
            [clj-http.client :as http-client]
            [clj-http.cookies :as http-cookies]
            [clj-http.core :as http-core]
            [net.cgrand.enlive-html :as html]
            (org.bovinegenius [exploding-fish :as uri]))
  (:import [java.net URLEncoder]))


(def *amazon-login-url* "https://www.amazon.com/ap/signin?openid.return_to=https%3A%2F%2Fkindle.amazon.com%3A443%2Fauthenticate%2Flogin_callback%3Fwctx%3D%252F&openid.pape.max_auth_age=0&openid.claimed_id=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0%2Fidentifier_select&openid.mode=checkid_setup&pageId=amzn_kindle&openid.assoc_handle=amzn_kindle&openid.identity=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0%2Fidentifier_select&openid.ns=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0")

(defn list-pagination-links
  [a-pagination-div]
  (distinct
   (map
    (fn [an-a-tag] (-> an-a-tag
                     :attrs
                     :href))
    (html/select
     (html/html-resource (java.io.StringReader. a-pagination-div))
     [:a]))))

(defn get-book-links
  [current-url a-table-element]
  (let [parsed-html (html/select
                     (html/html-resource (java.io.StringReader. a-table-element))
                     [:td.titleAndAuthor])]
    (map
     (fn [a-td-tag]
       (map
        (fn [s] (clojure.string/trim s))
        (flatten (list (-> (html/select a-td-tag [:a])
                          first
                          :content)
                       (uri/resolve-path
                        current-url
                        (-> (html/select a-td-tag [:a])
                           first
                           :attrs
                           :href))
                       (-> (html/select a-td-tag [:span])
                          first
                          :content)))))
     parsed-html)))

(defn navigate-login-fetch-book-links
  "Args:
    - email : Amazon.com email account
    - passwd : Amazon.com password
    - destination-file : where to store the s-expressions"
  [email password destination-file]

  ;; use firefox
  (taxi/set-driver! {:browser :firefox})

  ;; navigate
  (taxi/to *amazon-login-url*)

  ;; login
  (taxi/input-text "#ap_email" email)
  (-> "#ap_password"
     (taxi/input-text password)
     (taxi/submit))

  ;; fetch readings
  (taxi/click "a[href*='your_reading']")

  ;; step through the pagination
  (binding [*out* (java.io.FileWriter. destination-file)]
   (clojure.pprint/pprint
    (reduce
     (fn [acc link]
       (taxi/click (format "a[href*='%s']" link))
       (concat acc (get-book-links (taxi/current-url) (taxi/html "table"))))
     []
     (list-pagination-links (taxi/html "div.paginationLinks")))))
  (taxi/quit))