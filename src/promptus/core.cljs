(ns promptus.core
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [cljs.core.async :refer [put! chan <! >!]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [promptus.util :as util :refer [div ul li span icon]]))

(enable-console-print!)

(def app-state (atom {:entries [{:description "foo bar"}
                                {:description "aua"}]
                      :messages ["foo" "bar"]}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Topbar
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn menu-item [on-click active? & contents]
  (li (when active? "active")
    (apply dom/a
           #js {:href "#"
                :onClick on-click}
           contents)))

(defn topbar [app _]
  (div "header"
    (ul "nav nav-pills pull-right"
      (menu-item (constantly nil) false "Post a job")
      (menu-item (constantly nil) true "Messages"
                 (let [c (-> app :messages count)]
                   (when (> c 0)
                     (span "badge" (str c))))))
    (dom/h2
     #js {:className "text-muted"}
     "prompt/us")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Wall
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn entry-view [entry _]
  (div "well well-sm" (:description entry)))

(defn wall [app _]
  (om/component
   (apply div "wall"
          (om/build-all entry-view (:entries app)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Layout
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn root [app _]
  (om/component
   (div "container"
     (om/build topbar app)
     (om/build wall app))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Main
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(om/root root app-state
         {:target (. js/document (getElementById "app"))})
