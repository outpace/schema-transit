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

(defn pred-tag [obj]
  (condp = (:p? obj)
    integer? (tag "Int")
    keyword? (tag "Keyword")
    symbol? (tag "Symbol")
    nil))

(def write-handlers
  (-> (apply record-write-handlers records)
      (assoc-primitive-write double)
      (assoc-primitive-write float)
      (assoc-primitive-write long)
      (assoc-primitive-write int)
      (assoc-primitive-write short)
      (assoc-primitive-write char)
      (assoc-primitive-write byte)
      (assoc-primitive-write boolean)
      (assoc-primitive-write doubles)
      (assoc-primitive-write floats)
      (assoc-primitive-write longs)
      (assoc-primitive-write ints)
      (assoc-primitive-write shorts)
      (assoc-primitive-write chars)
      (assoc-primitive-write bytes)
      (assoc-primitive-write booleans)
      (assoc schema.core.Predicate
        (write-handler pred-tag pred-tag))
      (assoc Pattern (write-handler (tag "Pattern") str))
      (assoc Class
        (write-handler (tag "Class")
                       #(.getName ^Class %)))))

(def read-handlers
  (-> (apply record-read-handlers records)
      (assoc-primitive-read double)
      (assoc-primitive-read float)
      (assoc-primitive-read long)
      (assoc-primitive-read int)
      (assoc-primitive-read short)
      (assoc-primitive-read char)
      (assoc-primitive-read byte)
      (assoc-primitive-read boolean)
      (assoc-primitive-read doubles)
      (assoc-primitive-read floats)
      (assoc-primitive-read longs)
      (assoc-primitive-read ints)
      (assoc-primitive-read shorts)
      (assoc-primitive-read chars)
      (assoc-primitive-read bytes)
      (assoc-primitive-read booleans)
      (assoc (tag "Int") (read-handler (constantly s/Int)))
      (assoc (tag "Keyword") (read-handler (constantly s/Keyword)))
      (assoc (tag "Symbol") (read-handler (constantly s/Symbol)))
      (assoc (tag "Pattern") (read-handler #(Pattern/compile %)))
      (assoc (tag "Class")
        (read-handler #(Class/forName % false (RT/baseLoader))))))
