(ns promptus.desk
  "Auto-resizable content-editable with rich input control
  and full input feedback."
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [cljs.core.async :refer [put! chan <! >!]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [promptus.util :as util :refer [div ul li span icon]]
            [promptus.re :as re]))

(def desk-init-state
  {:text ""
   :date nil
   :money nil
   :kbd (chan)
   :rows 1
   :mode :ready})

(defn clean-input [owner]
  (doseq [[k v] desk-init-state]
    (om/set-state! owner k v)))

(defn desk-value [state]
  (select-keys state [:text :date]))

(defn save-input [board owner state]
  (when (:new? state)
    (om/update! board [:entries (ids/entry-id board)] (desk-value state))
    (clean-input owner)))

(def parse-input {:text identity
                  :date re/find-date
                  :money re/find-money})

(defn handle-input [e owner _]
  (let [text (.. e -target -value)]
    (doseq [[k finder] parse-input]
      (om/set-state! owner k (finder text)))))

(defn display-props [{:keys [money date]}]
  (div "props"
    (when-not (empty? money)
      (span "item"
            (icon "fa-usd fa-fw") money))
    (when-not (empty? date)
      (span "item"
            (icon "fa-calendar fa-fw")
            (str " "(:day date) "." (:month date) "." (:year date))))))

(defn input-feedback-class [state]
  (str "form-control-feedback"
       (when (= 0 (count (:text state)))
         " hide")))

(defn desk [m owner]
  (reify

    om/IInitState
    (init-state [_]
      desk-init-state)

    om/IWillMount
    (will-mount [_]
      (let [kbd (om/get-state owner :kbd)]
        (go-loop []
          (let [k (<! kbd)
                rows (util/count-rows (om/get-state owner :text))
                resize! #(om/set-state! owner :rows rows)
                save! #(save-input m owner (om/get-state owner))]
            (om/set-state! owner :mode (if (= 1 rows) :ready :edit))
            (case k
              [13 false] (resize!)
              [8 false] (resize!)
              [13 true] (save!)
              nil)
            (recur)))))

    om/IRenderState
    (render-state [_ state]
      (div "container"
        (div "content"
          (div "desk form-group has-feedback"
            (dom/textarea
             #js {:className "form-control input-lg"
                  :rows (:rows state)
                  :value (:text state)
                  :placeholder "Click here and describe new job"
                  :onKeyDown #(put! (:kbd state) [(.-keyCode %) (.-ctrlKey %)])
                  :onChange #(handle-input % owner state)})
            (dom/a #js {:href "#"
                        :className (input-feedback-class state)
                        :onClick #(clean-input owner)}
                   (icon "fa-times fa-2x")))
          ;(display-hints state)
          (div "clearfix")
          (when (= (:mode state) :edit)
            (div "form-group"
              (dom/button #js {:className "btn btn-lg btn-success pull-right"
                               :type "button"
                               :onClick #(save-input m owner state)}
                          "Post new job"))))))))
