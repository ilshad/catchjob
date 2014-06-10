(ns catchjob.wall
  (:require [cljs.core.async :refer [put!]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [catchjob.util :as util :refer [div]]
            [catchjob.mock :as mock]))

(defn entry-view [entry owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [focus]}]
      (dom/div #js {:className (str "entry " (:class entry))
                    :onClick #(do (put! focus {:name :reply :entry entry})
                                  false)}
        (->> entry :datetime str (div "created text-muted"))
        (->> entry :description (div "description"))))))

(defn wall-view [entries owner]
  (reify

    om/IWillMount
    (will-mount [_]
      (let [ch (mock/load-entries! (om/get-state owner :add-entry))]
        (put! ch :start)
        (om/set-state! owner :load-entries ch)))

    om/IRenderState
    (render-state [_ {:keys [focus]}]
      (div "container"
        (apply div "content"
               (om/build-all entry-view entries {:state {:focus focus}}))))

    om/IWillUnmount
    (will-unmount [_]
      (put! (om/get-state owner :load-entries) :stop))))
