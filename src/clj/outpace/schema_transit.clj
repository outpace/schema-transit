(ns outpace.schema-transit
  (:require [cognitect.transit :refer [read-handler
                                       record-read-handlers
                                       record-write-handlers
                                       write-handler]]
            [outpace.schema-transit.macros]
            [schema.core :as s])
  (:import (clojure.lang RT)
           (java.util Date UUID)
           (java.util.regex Pattern)))

(defn tag [name]
  (str "outpace.schema-transit." name))

(def cross-platform-records
  [schema.core.AnythingSchema
   schema.core.EqSchema
   schema.core.EnumSchema
   schema.core.Predicate
   schema.core.Maybe
   schema.core.NamedSchema
   schema.core.Either
   schema.core.Both
   schema.core.RequiredKey
   schema.core.OptionalKey
   schema.core.MapEntry
   schema.core.One
   schema.core.FnSchema
   schema.core.Isa])

(def platform-specific-records
  [schema.core.Record])

(defmacro assoc-primitive-write [m primitive]
  `(assoc ~m
     (class ~(symbol primitive))
     (write-handler (tag ~(str primitive)) (tag ~(str primitive)))))

(defmacro assoc-primitive-read [m primitive]
  `(assoc ~m
     (tag ~(str primitive))
     (read-handler (constantly ~(symbol primitive)))))

(def primitives '[double float long int short char byte boolean
                  doubles floats longs ints shorts chars bytes booleans])

(defmacro assoc-primitives [m op]
  (list* '-> m
         (for [primitive primitives]
           (list op primitive))))

(defn pred-tag [obj]
  (condp = (:p? obj)
    integer? (tag "Int")
    keyword? (tag "Keyword")
    symbol? (tag "Symbol")
    string? (tag "String")
    nil))

(defn java-type-tag [obj]
  (condp = obj
    java.lang.String (tag "String")
    java.lang.Boolean (tag "Bool")
    java.lang.Number (tag "Num")
    java.util.regex.Pattern (tag "Regex")
    java.util.Date (tag "Inst")
    java.util.UUID (tag "Uuid")
    nil))

(def
  ^{:doc "Transit write handlers for the subset of schemas that can be
  transmitted between Clojure and ClojureScript."}
  cross-platform-write-handlers
  (-> (apply record-write-handlers cross-platform-records)
      (assoc schema.core.Predicate (write-handler pred-tag pred-tag)
             Class (write-handler java-type-tag java-type-tag))))

(def
  ^{:doc "Transit write handlers for the set of schemas that can be
  serialized with Transit from a Clojure process."}
  write-handlers
  (-> (merge cross-platform-write-handlers
             (apply record-write-handlers platform-specific-records))
      (assoc-primitives assoc-primitive-write)
      (assoc Pattern (write-handler (tag "Pattern") str)
             Class (write-handler #(or (java-type-tag %) (tag "Class"))
                                  #(or (java-type-tag %) (.getName ^Class %))))))

(def
  ^{:doc "Transit read handlers for the subset of schemas that can be
  transmitted between Clojure and ClojureScript."}
  cross-platform-read-handlers
  (-> (apply record-read-handlers cross-platform-records)
      (assoc (tag "Int") (read-handler (constantly s/Int))
             (tag "Keyword") (read-handler (constantly s/Keyword))
             (tag "Symbol") (read-handler (constantly s/Symbol))
             (tag "String") (read-handler (constantly s/Str))
             (tag "Bool") (read-handler (constantly s/Bool))
             (tag "Num") (read-handler (constantly s/Num))
             (tag "Regex") (read-handler (constantly s/Regex))
             (tag "Inst") (read-handler (constantly s/Inst))
             (tag "Uuid") (read-handler (constantly s/Uuid)))))

(def
  ^{:doc "Transit read handlers for the set of schemas that can be
  deserialized with Transit by a Clojure process."}
  read-handlers
  (-> (merge cross-platform-read-handlers
             (apply record-read-handlers platform-specific-records))
      (assoc-primitives assoc-primitive-read)
      (assoc (tag "Pattern") (read-handler #(Pattern/compile %))
             (tag "Class") (read-handler #(Class/forName % false (RT/baseLoader))))))
