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
                      :onClick #(do (om/update! app [:focus] focus)
                                    false)}
           contents)))

(defn topbar-view [app _]
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

(defn wall-view [entries owner]
  (reify

    om/IWillMount
    (will-mount [_]
      (let [ch (mock/load-entries! (om/get-state owner :add-entry))]
        (put! ch :start)
        (om/set-state! owner :load-entries ch)))

    om/IRender
    (render [_]
      (div "container"
        (apply div "content" (om/build-all entry-view entries))))

    om/IWillUnmount
    (will-unmount [_]
      (put! (om/get-state owner :load-entries) :stop))))

(defn root-view [app owner]
  (reify

    om/IInitState
    (init-state [_]
      {:add-entry (chan)
       :focus (chan)})

    om/IWillMount
    (will-mount [_]
      (let [{:keys [add-entry focus]} (om/get-state owner)]
        (go-loop []
          (let [entry (<! add-entry)]
            (om/transact! app [:entries] #(cons entry %))
            (recur)))
        (go-loop []
          (om/update! app [:focus] (<! focus))
          (recur))
        (mock/init-entries! add-entry)))

    om/IRenderState
    (render-state [_ {:keys [add-entry focus]}]
      (div nil
        (om/build topbar-view app)
        (case (:focus app)
          :desk (om/build desk/desk-view
                          (:desk app)
                          {:state {:add-entry add-entry
                                   :focus focus}})
          :wall (om/build wall-view
                          (:entries app)
                          {:state {:add-entry add-entry}}))))))

(om/root root-view
         (atom {:focus :wall})
         {:target (. js/document (getElementById "app"))})
