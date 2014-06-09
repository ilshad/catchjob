(ns catchjob.desk
  "Auto-resizable content-editable with full input control
  and full input feedback."
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [cljs.core.async :refer [put! chan <! >!]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [catchjob.util :as util :refer [div ul li span icon]]
            [catchjob.re :as re]))

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
  {:id (gensym)
   :datetime (js/Date.)
   :class "type-my"
   :description (:text state)
   :deadline (js/Date. (-> state :date :year)
                       (-> state :date :month)
                       (-> state :date :day))
   :budget (:money state)})

(defn save-input [app owner state]
  (om/transact! app [:entries] #(cons (desk-value state) %))
  (om/update! app [:focus] :wall)
  (om/update! app [:desk] nil))

(def input-parsers {:text identity
                    :date re/find-date
                    :money re/find-money})

(defn handle-input [e owner _]
  (let [text (.. e -target -value)]
    (doseq [[k finder] input-parsers]
      (om/set-state! owner k (finder text)))))

(defn input-feedback-class [state]
  (str "form-control-feedback"
       (when (= (:mode state) :ready)
         " hide")))

(defn text->mode [text]
  (if (= 0 (count text)) :ready :edit))

(defn count-rows [string]
  (reduce #(if (= %2 \newline) (inc %1) %1) 1 string))

(defn props [{:keys [money date]}]
  (ul "props"
    (when-not (empty? money)
      (li nil "Budget:"
        (icon "fa-usd fa-fw") money))
    (when-not (empty? date)
      (li nil "Deadline: "
        (icon "fa-calendar fa-fw")
        (str " "(:day date) "." (:month date) "." (:year date))))))

(defn hint [text]
  (li nil (icon "fa-info-circle fa-fw fa-lg text-info") text))

(def hints
  (ul "hints"
    (hint "Budget: enter $ (dollar sign) and integer (i.e. $ 42).")
    (hint "Add/remove row: press [Enter] or [Backspace]")
    (hint "Deadline: enter date (i.e. 12.09.2014)")
    (hint "Send this job: press [Ctrl+Enter]")))

(defn desk [app owner]
  (reify

    om/IInitState
    (init-state [_]
      desk-init-state)

    om/IWillMount
    (will-mount [_]
      (let [kbd (om/get-state owner :kbd)]
        (go-loop []
          (let [k (<! kbd)
                state (om/get-state owner)
                rows! #(om/set-state! owner :rows (-> state :text count-rows))
                save! #(save-input app owner state)]
            (when (js->clj (.isMounted owner))
              (om/set-state! owner :mode (-> state :text text->mode))
              (case k
                [13 false] (rows!)
                [8 false] (rows!)
                [13 true] (save!)
                nil))
            (recur)))))

    om/IRenderState
    (render-state [_ state]
      (div "container"
        (div "content desk"
          (div "form-group has-feedback"
            (dom/textarea
             #js {:className "form-control input-lg"
                  :rows (:rows state)
                  :value (:text state)
                  :placeholder "Click here and describe new job"
                  :onKeyDown #(put! (:kbd state) [(.-keyCode %) (.-ctrlKey %)])
                  :onChange #(handle-input % owner state)})
            (dom/a #js {:href "#"
                        :className (input-feedback-class state)
                        :onClick #(do (clean-input owner) false)}
                   (icon "fa-times fa-2x")))
          (props state)
          hints)))))
