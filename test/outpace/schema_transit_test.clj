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

(defn transit [obj]
  (let [out (ByteArrayOutputStream.)]
    (transit-write obj out)
    (transit-read (ByteArrayInputStream. (.toByteArray out)))))

(defn transit? [obj]
  (let [t (transit obj)]
    (is (= obj t) t)))

(deftest test-primitive-schemas
  (is (transit? double))
  (is (transit? float))
  (is (transit? long))
  (is (transit? int))
  (is (transit? short))
  (is (transit? char))
  (is (transit? byte))
  (is (transit? boolean)))

(deftest test-primitive-array-schemas
  (is (transit? doubles))
  (is (transit? floats))
  (is (transit? longs))
  (is (transit? ints))
  (is (transit? shorts))
  (is (transit? chars))
  (is (transit? bytes))
  (is (transit? booleans)))

(deftest test-cross-platform-schemas
  (is (transit? s/Any))
  (is (transit? (s/eq "foo")))
  (is (transit? (s/enum 'foo 'bar)))
  (is (transit? (s/pred integer? 'integer?)))
  (is (transit? (s/pred keyword? 'keyword?)))
  (is (transit? (s/pred symbol? 'symbol?)))
  (is (thrown? Exception
               (transit-write (s/pred odd? 'odd?) (ByteArrayOutputStream.))))
  (let [result (transit #"foo")]
    (is (instance? Pattern result))
    (is (= "foo" (str result))))
  (is (transit? s/Str))
  (is (transit? s/Bool))
  (is (transit? s/Num))
  (is (transit? s/Int))
  (is (transit? s/Keyword))
  (is (transit? s/Symbol))
  (is (transit? s/Regex))
  (is (transit? s/Inst))
  (is (transit? s/Uuid)))

(deftest test-class-schema
  (is (transit? Exception)))

(deftest test-composite-schemas
  (is (transit? (s/maybe s/Str)))
  (is (transit? (s/named s/Str "foo")))
  (is (transit? (s/either s/Str s/Int)))
  (is (transit? (s/both s/Str s/Int))))

(deftest test-map-schemas
  (is (transit? (s/required-key "foo")))
  (is (transit? (s/optional-key :foo)))
  (is (transit? (s/map-entry :foo s/Int))))

(deftest test-sequence-schemas
  (is (transit? (s/one s/Str "string")))
  (is (transit? (s/optional s/Str "string")))
  (is (transit? (s/pair s/Int "x" s/Int "y"))))

(deftest test-record-schemas
  (is (transit? (s/record schema.core.Record {:klass Class :schema Class}))))

(deftest test-function-schemas
  (is (transit? (s/=> [s/Str] s/Str))))

(deftest test-isa-schemas
  (derive ::child ::parent)
  (is (transit? (s/isa ::parent))))
