(ns outpace.schema-transit-test
  (:require-macros [cemerick.cljs.test :refer [deftest is testing]])
  (:require [cemerick.cljs.test]
            [cljs.nodejs :as nodejs]
            [cognitect.transit :as t]
            [outpace.schema-transit :as st]
            [schema.core :as s]))

(defn transit-write [obj]
  (t/write (t/writer :json-verbose
                     {:handlers st/write-handlers})
           obj))

(defn transit-read
  ([in]
     (transit-read in st/read-handlers))
  ([in read-handlers]
     (t/read (t/reader :json-verbose
                       {:handlers read-handlers})
             in)))

(defn roundtrip [obj]
  (transit-read (transit-write obj)))

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
    (is (thrown? js/Error (transit-write (s/pred odd? 'odd?))))
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
    (let [fs (nodejs/require "fs")]
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
             (transit-read (.readFileSync fs "resources/clojure.transit" "utf-8")
                           st/cross-platform-read-handlers))))))

(deftest test-platform-specific-schemas
  (let [result (roundtrip #"foo")]
    (is (instance? js/RegExp result))
    (is (s/validate result "foo" ))))
