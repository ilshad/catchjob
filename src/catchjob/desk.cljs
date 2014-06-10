(ns catchjob.desk
  (:require [cljs.core.async :refer [put! chan]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [catchjob.util :as util :refer [div ul li icon]]))

(def desk-init-state
  {:text ""
   :date nil
   :money nil
   :rows 1
   :mode :ready
   :done? false
   :kbd (chan)})

(defn clean-input! [owner]
  (doseq [[k v] desk-init-state]
    (om/set-state! owner k v)))

(defn desk-value [state]
  {:id (gensym)
   :datetime (js/Date.)
   :class "type-my"
   :description (:text state)
   :budget (:money state)
   :deadline (js/Date. (-> state :date :year)
                       (-> state :date :month)
                       (-> state :date :day))})

(defn count-rows [text]
  (reduce #(if (= %2 \newline) (inc %1) %1) 1 text))

(def input-parsers
  {:text identity
   :mode #(if (= 0 (count %)) :ready :edit)
   :date util/find-date
   :money util/find-money
   :rows count-rows})

(defn handle-change [e owner state]
  (let [text (.. e -target -value)]
    (doseq [[k finder] input-parsers]
      (om/set-state! owner k (finder text))))
  (when (om/get-state owner :done?)
    (clean-input! owner)
    (put! (:focus state) {:name :wall})))

(defn handle-keydown [e owner state]
  (case [(.-keyCode e) (.-ctrlKey e)]
    [13 true] (do (put! (:add-entry state) (desk-value state))
                  (om/set-state! owner :done? true))
    nil))

(defn input-feedback-class [state]
  (str "form-control-feedback"
       (when (= (:mode state) :ready)
         " hide")))

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
    (hint "Enter \"$\" and integer (i.e. $ 42) - budget.")
    (hint "Enter date (i.e. 12.09.2014) - deadline.")
    (hint "Press [Enter] or [Backspace] - add/remove row.")
    (hint "Press [Ctrl+Enter] - send this job.")))

(defn desk-view [desk owner]
  (reify

    om/IInitState
    (init-state [_]
      desk-init-state)

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
                  :onChange #(handle-change % owner state)
                  :onKeyDown #(handle-keydown % owner state)})
            (dom/a #js {:href "#"
                        :className (input-feedback-class state)
                        :onClick #(do (clean-input! owner) false)}
                   (icon "fa-times fa-2x")))
          (props state)
          hints)))))
