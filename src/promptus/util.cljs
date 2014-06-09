(ns promptus.util
  (:require [om.dom :as dom :include-macros true]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; DOM shorthands
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- tag [tag class-names & contents]
  (apply tag #js {:className class-names} contents))

(def div   (partial tag dom/div))
(def ul    (partial tag dom/ul))
(def li    (partial tag dom/li))
(def span  (partial tag dom/span))

(defn icon [& class-names]
  (dom/i #js {:className (apply str (interpose " " (conj class-names "fa")))}))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Misc utils
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn count-rows [string]
  (reduce #(if (= %2 \newline) (inc %1) %1) 1 string))

(defn random-string [length]
  (let [ascii-codes (concat (range 48 58) (range 66 91) (range 97 123))]
    (apply str (repeatedly length #(char (rand-nth ascii-codes))))))
