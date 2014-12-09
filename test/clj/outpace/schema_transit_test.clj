(ns outpace.schema-transit-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [cognitect.transit :as t]
            [outpace.schema-transit :as st]
            [schema.core :as s])
  (:import (java.io ByteArrayInputStream ByteArrayOutputStream)
           (java.util.regex Pattern)))

(defn transit-write [obj]
  (let [out (ByteArrayOutputStream.)]
    (t/write (t/writer out
                       :json-verbose
                       {:handlers st/write-handlers})
             obj)
    (.toByteArray out)))

(defn transit-read
  ([in]
     (transit-read in st/read-handlers))
  ([in read-handlers]
     (t/read (t/reader in
                       :json-verbose
                       {:handlers read-handlers}))))

(defn roundtrip [obj]
  (transit-read (ByteArrayInputStream. (transit-write obj))))

(defn roundtrip? [obj]
  (let [t (roundtrip obj)]
    (is (= obj t) t)))

(deftest test-cross-platform-schemas
  (testing "leaf schemas"
    (is (roundtrip? s/Any))
    (is (roundtrip? (s/eq "foo")))
    (derive ::child ::parent)
    (is (roundtrip? (s/isa ::parent)))
    (is (roundtrip? (s/enum 'foo 'bar)))
    (is (roundtrip? (s/pred integer? 'integer?)))
    (is (roundtrip? (s/pred keyword? 'keyword?)))
    (is (roundtrip? (s/pred symbol? 'symbol?)))
    (is (= (roundtrip (s/pred string?)) s/Str))
    (is (thrown? Exception (transit-write (s/pred odd? 'odd?)
                                          (ByteArrayOutputStream.))))
    (is (roundtrip? s/Int))
    (is (roundtrip? s/Keyword))
    (is (roundtrip? s/Symbol))
    (is (roundtrip? s/Str))
    (is (roundtrip? s/Bool))
    (is (roundtrip? s/Num))
    (is (roundtrip? s/Regex))
    (is (roundtrip? s/Inst))
    (is (roundtrip? s/Uuid)))
  (testing "composite schemas"
    (is (roundtrip? (s/maybe s/Str)))
    (is (roundtrip? (s/named s/Str "foo")))
    (is (roundtrip? (s/either s/Str s/Int)))
    (is (roundtrip? (s/both s/Str s/Int))))
  (testing "map schemas"
    (is (roundtrip? (s/required-key "foo")))
    (is (roundtrip? (s/optional-key :foo)))
    (is (roundtrip? (s/map-entry :foo s/Int))))
  (testing "sequence schemas"
    (is (roundtrip? (s/one s/Str "string")))
    (is (roundtrip? (s/optional s/Str "string")))
    (is (roundtrip? (s/pair s/Int "x" s/Int "y"))))
  (testing "function schema"
    (is (roundtrip? (s/make-fn-schema s/Str [[s/Str]]))))
  (testing "actual output"
    (is (= {:int s/Int
            :keyword s/Keyword
            :symbol s/Symbol
            (s/required-key "str") s/Str
            (s/optional-key :maybe) (s/maybe s/Num)
            :eq (s/eq "foo")
            :enum (s/enum "first" "second")
            :pred (s/pred integer? 'integer?)
            :named (s/named s/Str "string")
            :either (s/either s/Str s/Int)
            :both (s/both s/Str s/Int)
            :map-entry (s/map-entry s/Str s/Int)
            :seq [(s/one s/Str "string") (s/optional s/Int "int")]
            :pair (s/pair s/Str "string" s/Int "int")
            :isa (s/isa :foo/bar)
            :bool s/Bool
            :num s/Num
            :regex s/Regex
            :inst s/Inst
            :uuid s/Uuid
            :fn (s/make-fn-schema s/Str [[s/Str]])
            s/Any s/Any}
           (transit-read (io/input-stream (io/resource "clojurescript.transit"))
                         st/cross-platform-read-handlers)))))

(deftest test-record-schemas
  (is (roundtrip? (s/record schema.core.Record {:klass Class :schema Class}))))

(deftest test-platform-specific-schemas
  (testing "primitive schemas"
    (is (roundtrip? double))
    (is (roundtrip? float))
    (is (roundtrip? long))
    (is (roundtrip? int))
    (is (roundtrip? short))
    (is (roundtrip? char))
    (is (roundtrip? byte))
    (is (roundtrip? boolean)))
  (testing "primitive array schemas"
    (is (roundtrip? doubles))
    (is (roundtrip? floats))
    (is (roundtrip? longs))
    (is (roundtrip? ints))
    (is (roundtrip? shorts))
    (is (roundtrip? chars))
    (is (roundtrip? bytes))
    (is (roundtrip? booleans)))
  (testing "class schema"
    (is (roundtrip? Exception)))
  (testing "pattern instance"
    (let [result (roundtrip #"foo")]
      (is (instance? Pattern result))
      (is (= "foo" (str result))))))
