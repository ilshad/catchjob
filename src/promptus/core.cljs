(ns promptus.core
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [cljs.core.async :refer [put! chan <! >!]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

(def app-state (atom {:text "Hello world!"}))

(defn root [app owner]
  (om/component (dom/h1 nil (:text app))))

(om/root root app-state {:target (. js/document (getElementById "app"))})
