# schema-transit

schema-transit is a library that glues together Prismatic's
[Schema](http://github.com/prismatic/schema) library and Cognitect's
[Transit](http://github.com/cognitect/transit-clj) library.

Using schema-transit you can turn a schema into data.  Once it is data you can
send it over the wire, store it in a database, etc.

The latest version of schema-transit is:

[![Clojars Project](http://clojars.org/com.outpace/schema-transit/latest-version.svg)](http://clojars.org/com.outpace/schema-transit)

## Usage

The schema-transit cross-platform read/write handlers are defined as
`outpace.schema-transit/cross-platform-read-handlers` and
`outpace.schema-transit/cross-platform-write-handlers`, respectively.

The platform specific read/write handlers are defined as
`outpace.schema-transit/read-handlers` and
`outpace.schema-transit/write-handlers`, respectively

You can use these like you would any other transit handlers:

```clojure
(require '[cognitect.transit :as t])
(require '[outpace.schema-transit :as st])

(defn transit-write [obj out]
  (t/write (t/writer out
                     :json-verbose
                     {:handlers st/write-handlers})
           obj))

(defn transit-read [in]
  (t/read (t/reader in
                    :json-verbose
                    {:handlers st/read-handlers})))
```

## Approach

Schemas can be divided into two sets: cross-platform and platform specific.

The cross-platform schemas can be serialized, deserialized, and transmitted
between Clojure and ClojureScript.  The following leaf schemas are currently
considered cross-platform: s/Any, s/Int, s/Keyword, s/Symbol, s/String, s/Bool,
s/Num, s/Regex, s/Inst, s/Uuid.  The following composite schemas are
cross-platform if their components are also cross-platform: s/eq, s/maybe,
s/named, s/either, s/both, maps, sets, vectors, s/required-key, s/optional-key,
s/map-entry, s/one, function schema, and s/pair.

The platform specific schemas include the cross-platform schemas, and additional
schemas that can safely be serialized/deserialized but cannot be transmitted
between Clojure and ClojureScript.  Clojure's platform specific schemas are
primitive schemas, a compiled regular expression, and a Java Class.  In the case
of a Java class it is serialized to its fully qualified class name.  When
deserialized a Java class must exist and have already been initialized;
schema-transit finds classes using Class/forName but in a way that will *not*
initialize a class.  On the ClojureScript side, the only platform specific
schema is a regular expression.

## License

    Copyright © Outpace Systems, Inc.
    
    Released under the Apache License, Version 2.0
