(ns catchjob.core
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [cljs.core.async :refer [put! chan <! >!]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [catchjob.util :as util :refer [div ul li span icon]]
            [catchjob.desk :as desk]
            [catchjob.mock :as mock]))

(enable-console-print!)

(defn menu-item [app focus & contents]
  (li (when (= (:focus app) focus) "active")
    (apply dom/a #js {:href "#"
                      :onClick #(do (om/update! app [:focus] focus) false)}
           contents)))

(defn topbar [app _]
  (div "header"
    (div "container"
      (div "row"
        (div "site-title" "catch job!")
        (ul "nav nav-pills pull-right"
          (menu-item app :wall "catch!")
          (menu-item app :desk "post a job"))))))

(defn entry-view [entry _]
  (dom/div #js {:className (str "entry " (:class entry))
                :onClick #(js/alert (:description entry))}
    (->> entry :datetime str (div "created text-muted"))
    (->> entry :description (div "description"))))

(defn wall [app _]
  (om/component
   (div "container"
     (apply div "content"
            (om/build-all entry-view (:entries app))))))

(defn root [app _]
  (reify

    om/IWillMount
    (will-mount [_]
      (mock/init-entries! app)
      (mock/load-entries! app))

    om/IRender
    (render [_]
      (div nil
        (om/build topbar app)
        (case (:focus app)
          :desk (om/build desk/desk app)
          :wall (om/build wall app))))))

(om/root root
         (atom {:focus :wall})
         {:target (. js/document (getElementById "app"))})
