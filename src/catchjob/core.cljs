(ns catchjob.core
  (:require-macros [cljs.core.async.macros :refer [go-loop alt!]])
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
          (when-not (empty? (:doer app))
            (menu-item app :doer "my jobs"))
          (menu-item app :wall "catch!")
          (menu-item app :desk "post a job"))))))

(defn apply-job [{:keys [focus apply-job]} entry-id]
  (fn [e]
    (put! apply-job entry-id)
    (put! focus {:name :wall})))

(defn entry-focus-view [entry owner]
  (reify

    om/IRenderState
    (render-state [_ state]
      (div "container"
        (div "content reply"
          (dom/h3 nil "Budget:"
                  (icon "fa-usd fa-fw")
                  (:budget entry))
          (dom/h4 nil "Deadline:"
                  (icon "fa-calendar fa-fw")
                  (str (:deadline entry)))
          (div "description"
            (:description entry))
          (div "actions text-right"
            (dom/button #js {:className "btn btn-link btn-lg"
                             :onClick #(put! (:focus state) {:name :wall})}
                        (icon "fa-chevron-left fa-fw") " Back")
            (dom/button #js {:className "btn btn-success btn-lg"
                             :onClick (apply-job state (:id entry))}
                        "Apply this Job")))))))

(defn entry-item-view [entry owner]
  (reify

    om/IRenderState
    (render-state [_ {:keys [focus]}]
      (dom/div #js {:className (str "entry " (:class entry))
                    :onClick #(do (put! focus {:name :entry :id (:id entry)})
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
               (om/build-all entry-item-view entries
                             {:state {:focus focus}}))))

    om/IWillUnmount
    (will-unmount [_]
      (put! (om/get-state owner :load-entries) :stop))))

(defn find-entry [app id]
  (reduce (fn [found next]
            (or found
                (when (= id (:id next))
                  next)))
          nil
          (:entries app)))

(defn root-view [app owner]
  (reify

    om/IInitState
    (init-state [_]
      {:add-entry (chan)
       :focus (chan)
       :apply-job (chan)
       :view-entry-id nil})

    om/IWillMount
    (will-mount [_]
      (let [{:keys [add-entry focus apply-job]} (om/get-state owner)]
        (go-loop []
          (alt!
           add-entry
           ([v]
              (om/transact! app [:entries] #(cons v %))
              (recur))
           focus
           ([v]
              (when (= (:name v) :entry)
                (om/set-state! owner :view-entry-id (:id v)))
              (om/update! app [:focus] (:name v))
              (recur))
           apply-job
           ([v]
              (om/transact! app [:doer :jobs] #(cons v %))
              (recur))))
        (mock/init-entries! add-entry)))

    om/IRenderState
    (render-state [_ state]
      (div nil
        (om/build topbar-view app)
        (case (:focus app)
          :wall (om/build wall-view
                          (:entries app)
                          {:state (select-keys state [:focus :add-entry])})
          :entry (om/build entry-focus-view
                           (find-entry app (:view-entry-id state))
                           {:state (select-keys state [:focus :apply-job])})
          :desk (om/build desk/desk-view
                          (:desk app)
                          {:state (select-keys state [:focus :add-entry])}))))))

(om/root root-view
         (atom {:focus :wall})
         {:target (. js/document (getElementById "app"))})
