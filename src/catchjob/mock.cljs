(ns catchjob.mock
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [put! chan <! >! timeout]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(def formidos
  ["Quisque quis dui diam."
   "Nam sit amet mauris lacus."
   "\n"
   "Morbi non pellentesque justo."
   "Phasellus cursus tempus erat."
   "Donec sodales mi eget sem eleifend pulvinar."
   "\n"
   "Donec a diam lectus. Sed sit amet ipsum mauris."
   "Lorem ipsum dolor sit amet, consectetur adipiscing elit."])

(defn mock-entry []
  {:id (gensym)
   :datetime (js/Date.)
   :class (rand-nth ["type-a" "type-b" "type-c" "type-d"])
   :deadline nil
   :budget (rand-nth (range 100 10000 10))
   :description (apply str (interpose " " (repeatedly
                                           (rand-nth [3 6 9 12 15 18])
                                           (partial rand-nth formidos))))})

(defn init-entries! [add-entry]
  (go
   (dotimes [i 10]
     (>! add-entry (mock-entry)))))

(defn load-entries! [add-entry app]
  (go-loop []
    (<! (timeout (rand-nth (range 500 5000 300))))
    (when (= (:focus @app) :wall)
      (>! add-entry (mock-entry)))
    (recur)))
