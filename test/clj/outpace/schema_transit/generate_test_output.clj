(ns outpace.schema-transit.generate-test-output
  (:require [cognitect.transit :as tr]
            [outpace.schema-transit :as st]
            [schema.core :as s])
  (:import (java.io ByteArrayOutputStream)))

(defn -main []
  (let [stream (ByteArrayOutputStream.)
        writer (tr/writer stream :json-verbose {:handlers st/cross-platform-write-handlers})]
    (tr/write writer {:int s/Int
                      :keyword s/Keyword
                      :symbol s/Symbol
                      (s/required-key "str") s/Str
                      (s/optional-key :maybe) (s/maybe s/Num)
                      :eq (s/eq "foo")
                      :enum (s/enum "first" "second")
                      :pred (s/pred integer?)
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
                      s/Any s/Any})
    (println (.toString stream))))
