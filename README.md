# schema-transit

schema-transit is a library that glues together Prismatic's
[http://github.com/prismatic/schema](Schema) library and Cognitect's
[http://github.com/cognitect/transit-clj](Transit) library.

Using schema-transit you can turn a schema into data.  Once it is data you can
send it over the wire, store it in a database, etc.

The latest version of schema-transit is:

    [outpace/schema-transit "0.1.0"]

## Approach

Most of schemas are instances of defrecord types, and transit-clj has support
for easily creating read/write handlers for a defrecord type.  However, there
are other schemas that need some more work to be serializable.  For example, a
Java class is a valid schema that matches instances of itself.

### Cross-platform Serialization

Many of the non-record schemas (including the cross-platform schemas: s/Str,
s/Int, etc.) are serialized as namespaced symbols.  The intention is that they
can be transmitted across the wire between Clojure and ClojureScript processes.

### Platform Specific Serialization

A Java class is serialized using its name, and when it is deserialized the class
is found using its class name (in a way that will *not* automatically initialize
the class).  This would allow sending a class schema between Clojure processes
(with the appropriate classes available on each classpath), but it would not be
possible (obviously) to send a class schema between Clojure and ClojureScript
processes.

## Usage

The schema-transit write handlers are defined as
`outpace.schema-transit/write-handlers`, and the read handlers as
`outpace.schema-transit/read-handlers`.

You can use these like you would any other transit handlers:

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

## Future

As mentioned several times already, a future goal is to enable transmission of
schemas between Clojure and ClojureScript processes, but this is not yet
implemented.

Also, this library is still early stage, and the exact cross-platform
serialization strategy is still being solidified, so any long term storage of
schemas (such as in a database) is not recommended unless you are willing to
deal with migrating the schemas between versions of schema-transit.

Once schema-transit reaches 1.0, you will be guaranteed that the format will not
change except possibly between major releases (and even then we will attempt to
maintain some kind of optional backwards compatibility).

## License

    Copyright © Outpace Systems, Inc.
    
    Released under the Apache License, Version 2.0
