(ns outpace.schema-transit
  (:require-macros [outpace.schema-transit.macros :refer [record-read-handlers record-write-handlers]])
  (:require [cognitect.transit :refer [read-handler write-handler]]
            [schema.core :as s]))

(defn tag [name]
  (str "outpace.schema-transit." name))

(defn tag-fn [name]
  (fn [_]
    (tag name)))

(defn pred-tag [obj]
  (condp = (:p? obj)
    integer? (tag "Int")
    keyword? (tag "Keyword")
    symbol? (tag "Symbol")
    string? (tag "String")
    nil))

(def js-type-tag
  {s/Bool (tag "Bool")
   s/Num (tag "Num")
   s/Regex (tag "Regex")
   s/Inst (tag "Inst")
   s/Uuid (tag "Uuid")})

(def
  ^{:doc "Transit write handlers for the subset of schemas that can be
  transmitted between Clojure and ClojureScript."}
  cross-platform-write-handlers
  (assoc (record-write-handlers AnythingSchema EqSchema EnumSchema Predicate
                                Maybe NamedSchema Either Both RequiredKey
                                OptionalKey MapEntry One FnSchema Isa)
    schema.core.Predicate (write-handler pred-tag pred-tag)
    js/Function (write-handler js-type-tag js-type-tag)
    (type s/Regex) (write-handler js-type-tag js-type-tag)))

(def
  ^{:doc "Transit write handlers for the set of schemas that can be
  serialized with Transit from a ClojureScript process."}
  write-handlers
  (assoc cross-platform-write-handlers
    js/RegExp (write-handler (tag-fn "RegexInstance")
                             (fn [obj]
                               (let [obj (str obj)
                                     len (count obj)]
                                 (subs obj 1 (dec len)))))))

(def
  ^{:doc "Transit read handlers for the subset of schemas that can be
  transmitted between Clojure and ClojureScript."}
  cross-platform-read-handlers
  (assoc (record-read-handlers AnythingSchema EqSchema EnumSchema Predicate
                               Maybe NamedSchema Either Both RequiredKey
                               OptionalKey MapEntry One FnSchema Isa)
    (tag "Int") (read-handler (constantly s/Int))
    (tag "Keyword") (read-handler (constantly s/Keyword))
    (tag "Symbol") (read-handler (constantly s/Symbol))
    (tag "String") (read-handler (constantly s/Str))
    (tag "Bool") (read-handler (constantly s/Bool))
    (tag "Num") (read-handler (constantly s/Num))
    (tag "Regex") (read-handler (constantly s/Regex))
    (tag "Inst") (read-handler (constantly s/Inst))
    (tag "Uuid") (read-handler (constantly s/Uuid))))

(def
  ^{:doc "Transit read handlers for the set of schemas that can be
  deserialized with Transit by a ClojureScript process."}
  read-handlers
  (assoc cross-platform-read-handlers
    (tag "RegexInstance") (read-handler re-pattern)))
