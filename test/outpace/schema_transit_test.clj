(ns outpace.schema-transit-test
  (:require [clojure.test :refer :all]
            [cognitect.transit :as t]
            [outpace.schema-transit :refer :all]
            [schema.core :as s])
  (:import (java.io ByteArrayInputStream ByteArrayOutputStream)
           (java.util.regex Pattern)))

(defn transit-write [obj out]
  (t/write (t/writer out
                     :json-verbose
                     {:handlers write-handlers})
           obj))

(defn transit-read [in]
  (t/read (t/reader in
                    :json-verbose
                    {:handlers read-handlers})))

(defn roundtrip [obj]
  (let [out (ByteArrayOutputStream.)]
    (transit-write obj out)
    (transit-read (ByteArrayInputStream. (.toByteArray out)))))

(defn roundtrip? [obj]
  (let [t (roundtrip obj)]
    (is (= obj t) t)))

(deftest test-primitive-schemas
  (is (roundtrip? double))
  (is (roundtrip? float))
  (is (roundtrip? long))
  (is (roundtrip? int))
  (is (roundtrip? short))
  (is (roundtrip? char))
  (is (roundtrip? byte))
  (is (roundtrip? boolean)))

(deftest test-primitive-array-schemas
  (is (roundtrip? doubles))
  (is (roundtrip? floats))
  (is (roundtrip? longs))
  (is (roundtrip? ints))
  (is (roundtrip? shorts))
  (is (roundtrip? chars))
  (is (roundtrip? bytes))
  (is (roundtrip? booleans)))

(deftest test-cross-platform-schemas
  (is (roundtrip? s/Any))
  (is (roundtrip? (s/eq "foo")))
  (is (roundtrip? (s/enum 'foo 'bar)))
  (is (roundtrip? (s/pred integer? 'integer?)))
  (is (roundtrip? (s/pred keyword? 'keyword?)))
  (is (roundtrip? (s/pred symbol? 'symbol?)))
  (is (thrown? Exception
               (transit-write (s/pred odd? 'odd?) (ByteArrayOutputStream.))))
  (let [result (roundtrip #"foo")]
    (is (instance? Pattern result))
    (is (= "foo" (str result))))
  (is (roundtrip? s/Str))
  (is (roundtrip? s/Bool))
  (is (roundtrip? s/Num))
  (is (roundtrip? s/Int))
  (is (roundtrip? s/Keyword))
  (is (roundtrip? s/Symbol))
  (is (roundtrip? s/Regex))
  (is (roundtrip? s/Inst))
  (is (roundtrip? s/Uuid)))

(deftest test-class-schema
  (is (roundtrip? Exception)))

(deftest test-composite-schemas
  (is (roundtrip? (s/maybe s/Str)))
  (is (roundtrip? (s/named s/Str "foo")))
  (is (roundtrip? (s/either s/Str s/Int)))
  (is (roundtrip? (s/both s/Str s/Int))))

(deftest test-map-schemas
  (is (roundtrip? (s/required-key "foo")))
  (is (roundtrip? (s/optional-key :foo)))
  (is (roundtrip? (s/map-entry :foo s/Int))))

(deftest test-sequence-schemas
  (is (roundtrip? (s/one s/Str "string")))
  (is (roundtrip? (s/optional s/Str "string")))
  (is (roundtrip? (s/pair s/Int "x" s/Int "y"))))

(deftest test-record-schemas
  (is (roundtrip? (s/record schema.core.Record {:klass Class :schema Class}))))

(deftest test-function-schemas
  (is (roundtrip? (s/=> [s/Str] s/Str))))

(deftest test-isa-schemas
  (derive ::child ::parent)
  (is (roundtrip? (s/isa ::parent))))
