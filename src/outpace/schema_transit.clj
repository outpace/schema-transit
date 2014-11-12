(ns outpace.schema-transit
  (:require [cognitect.transit :refer [read-handler
                                       record-read-handlers
                                       record-write-handlers
                                       write-handler]]
            [schema.core :as s])
  (:import (clojure.lang RT)
           (java.util Date UUID)
           (java.util.regex Pattern)))

(def records [schema.core.AnythingSchema
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
              schema.core.Record
              schema.core.FnSchema
              schema.core.Isa])

(defn tag [name]
  (str "outpace.schema-transit." name))

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
    nil))

(def write-handlers
  (-> (apply record-write-handlers records)
      (assoc-primitives assoc-primitive-write)
      (assoc schema.core.Predicate
        (write-handler pred-tag pred-tag))
      (assoc Pattern (write-handler (tag "Pattern") str))
      (assoc Class
        (write-handler (tag "Class")
                       #(.getName ^Class %)))))

(def read-handlers
  (-> (apply record-read-handlers records)
      (assoc-primitives assoc-primitive-read)
      (assoc (tag "Int") (read-handler (constantly s/Int)))
      (assoc (tag "Keyword") (read-handler (constantly s/Keyword)))
      (assoc (tag "Symbol") (read-handler (constantly s/Symbol)))
      (assoc (tag "Pattern") (read-handler #(Pattern/compile %)))
      (assoc (tag "Class")
        (read-handler #(Class/forName % false (RT/baseLoader))))))
