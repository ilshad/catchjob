(ns promptus.core
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [cljs.core.async :refer [put! chan <! >!]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [promptus.util :as util :refer [div ul li span icon]]
            [promptus.desk :as desk]
            [promptus.mock :as mock]))

(enable-console-print!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Topbar
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn menu-item [app focus & contents]
  (li (when (= (:focus app) focus) "active")
    (apply dom/a #js {:href "#"
                      :onClick #(om/update! app [:focus] focus)}
           contents)))

(defn topbar [app _]
  (div "header"
    (div "container"
      (div "row"
        (div "site-title" "catch job!")
        (ul "nav nav-pills pull-right"
          (menu-item app :help (icon "fa-question-circle fa-lg"))
          (menu-item app :wall "catch!")
          (menu-item app :desk "post a job")
          (menu-item app :message false "messages"
                     (let [c (-> app :messages count)]
                       (when (> c 0)
                         (span "badge" (str c))))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Wall
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn display-datetime [v]
  (div "text-muted" (str v)))

(defn entry-view [entry _]
  (dom/div #js {:className (str "entry " (:class entry))
                :onClick #(js/alert (:description entry))}
    (->> entry :datetime display-datetime)
    (->> entry :description (div "description"))))

(defn wall [app _]
  (om/component
   (div "container"
     (apply div "content"
            (om/build-all entry-view (:entries app))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Layout
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn not-implemented [])

(defn root [app _]
  (om/component
   (div nil
     (om/build topbar app)
     (case (:focus app)
       :desk (om/build desk/desk (:desk app))
       :wall (om/build wall app)
       (div "container"
         (div "content text-muted"
           (dom/h1 nil "Not implemented yet")))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Main
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def app (atom {:focus :wall}))

(om/root root app {:target (. js/document (getElementById "app"))})

(mock/init-entries! app)
(mock/load-entries! app)
