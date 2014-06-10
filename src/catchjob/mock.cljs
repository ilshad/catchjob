(ns catchjob.mock
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [chan <! >! timeout]]
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

(defn load-entries! [out]
  (let [in (chan)
        st (atom nil)]
    (go-loop []
      (reset! st (<! in))
      (recur))
    (go-loop []
      (<! (timeout (rand-nth (range 500 3000 300))))
      (case @st
        :start (>! out (mock-entry))
        nil)
      (recur))
    in))
