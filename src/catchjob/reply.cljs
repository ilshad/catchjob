(ns catchjob.reply
  (:require [cljs.core.async :refer [put!]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [catchjob.util :as util :refer [div]]))

(defn reply-view [reply owner]
  (reify

    om/IRender
    (render [_]
      (div "container"
        (div "content reply"
          (div "form-group"
            "Reply..."))))))
