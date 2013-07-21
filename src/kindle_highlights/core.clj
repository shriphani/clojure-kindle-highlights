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

(defn navigate-login
  "Navigates and login"
  [email password]

  ;; use firefox
  (taxi/set-driver! {:browser :firefox})

  ;; navigate
  (taxi/to *amazon-login-url*)

  ;; login
  (taxi/input-text "#ap_email" email)
  (-> "#ap_password"
     (taxi/input-text password)
     (taxi/submit)))

(defn navigate-login-fetch-book-links
  "Args:
    - email : Amazon.com email account
    - passwd : Amazon.com password
    - destination-file : where to store the s-expressions"
  [email password destination-file]

  ;; navigate login
  (navigate-login email password)
  
  ;; fetch readings
  (taxi/click taxi/*driver* "a[href*='your_reading']")

  ;; step through the pagination
  (binding [*out* (java.io.FileWriter. destination-file)]
   (clojure.pprint/pprint
    (reduce
     (fn [acc link]
       (taxi/click taxi/*driver* (format "a[href*='%s']" link))
       (concat acc (get-book-links (taxi/current-url) (taxi/html "table"))))
     []
     (list-pagination-links (taxi/html taxi/*driver* "div.paginationLinks")))))
  (taxi/quit))

(defn highlights-on-page
  [page-html]
  (map
   (fn [a-div]
     (html/text a-div))
   (html/select
    (html/html-resource
     (java.io.StringReader. page-html))
    [:div.highlightRow :span.highlight])))

(defn download-book-highlights
  [book-details email password]
  (let [[title url author] book-details]
    (do
      (taxi/to taxi/*driver* url)
      (taxi/click taxi/*driver*
                  (format
                   "a[href*='/your_highlights_and_notes/%s']"
                   (second (re-find #".*/([0-9|A-Z]*)" url))))
      (highlights-on-page (taxi/html taxi/*driver* "html")))))

(defn fetch-highlights
  "Args
    - book-data-file: File containing book s-expressions
    - filter-routine: Function that accepts a book s-expression and
                      decides the file.
    - email: email
    - password: password
    - destination-file: where to store the quotes"
  [book-data-file filter-routine email password destination-file]
  (navigate-login email password)
  (let [book-data (read-string (slurp book-data-file))]
    (binding [*out* (java.io.FileWriter. destination-file)]
      (clojure.pprint/pprint
       (reduce
        (fn [acc x] (concat (download-book-highlights x email password)
                           acc))
        []
        (filter
         filter-routine
         book-data))))
    (taxi/quit taxi/*driver*)))