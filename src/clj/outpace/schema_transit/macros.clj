(ns outpace.schema-transit.macros)

(defmacro schema-tag [name]
  `(str "schema.core." ~name))

(defmacro record-write-handlers [& records]
  `(-> {}
       ~@(for [record records]
           `(assoc ~(symbol (str "schema.core/" record))
              (cognitect.transit/write-handler
               (fn [x#]
                 ~(schema-tag (str record)))
               (fn [x#]
                 (into {} x#)))))))

(defmacro record-read-handlers [& records]
  `(-> {}
       ~@(for [record records]
           `(assoc ~(schema-tag (str record))
              (cognitect.transit/read-handler
               (fn [x#]
                 (~(symbol (str "schema.core/map->" record ".")) x#)))))))
