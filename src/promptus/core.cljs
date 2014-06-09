(ns promptus.core
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [cljs.core.async :refer [put! chan <! >!]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [promptus.util :as util :refer [div ul li span icon]]
            [promptus.mock :as mock]))

(enable-console-print!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Topbar
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn menu-item [on-click active? & contents]
  (li (when active? "active")
    (apply dom/a #js {:href "#" :onClick on-click} contents)))

(defn topbar [app _]
  (div "header"
    (div "container"
      (div "row"
        (div "site-title" "catch job")
        (ul "nav nav-pills pull-right"
          (menu-item (constantly nil) false
                     (icon "fa-question-circle fa-lg"))
          (menu-item (constantly nil) false "post a job")
          (menu-item (constantly nil) false "messages"
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
  (div (str "entry " (:class entry))
    (->> entry :datetime display-datetime)
    (->> entry :description (div "description"))))

(defn wall [app _]
  (om/component
   (div "container"
     (apply div "wall"
            (om/build-all entry-view (:entries app))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Layout
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn root [app _]
  (om/component
   (div nil
     (om/build topbar app)
     (om/build wall app))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Main
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def app (atom {}))

(om/root root app {:target (. js/document (getElementById "app"))})

(mock/init-entries! app)
(mock/load-entries! app)
