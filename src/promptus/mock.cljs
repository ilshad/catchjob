(ns promptus.mock
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [cljs.core.async :refer [put! chan <! >! timeout]]))

(def formidos
  ["Quisque quis dui diam."
   "Nam sit amet mauris lacus."
   "Morbi non pellentesque justo."
   "Phasellus cursus tempus erat."
   "Donec sodales mi eget sem eleifend pulvinar."
   "Donec a diam lectus. Sed sit amet ipsum mauris."
   "Lorem ipsum dolor sit amet, consectetur adipiscing elit."])

(defn mock-entry []
  {:id (gensym)
   :datetime (js/Date.)
   :class (rand-nth ["type-a" "type-b" "type-c" "type-d"])
   :description (apply str (interpose " " (repeatedly
                                           (rand-nth [3 6 9 12 15 18])
                                           (partial rand-nth formidos))))})

(defn init-entries! [app]
  (swap! app assoc :entries (mapv #(mock-entry) (range 10))))

(defn load-entries! [app]
  (go-loop []
    (<! (timeout (rand-nth (range 500 2000 100))))
    (swap! app update-in [:entries] #(cons (mock-entry) %))
    (recur)))
