An OO/Functional Crit-bit tree in Java, inspired by
[djb](http://cr.yp.to/critbit.html), [Adam Langley](https://github.com/agl/critbit), and [Okasaki](http://www.eecs.usma.edu/webs/people/okasaki/pubs.html).

A primary goal for the project is space-efficiency.  It accomplishes this by
defining the five different kinds of nodes in a critbit tree - no
nodes have unused or unnecessary references, and leaf nodes are collapsed into
their parents when possible.

An immutable/functional implementation is provided for use where such traits
are desirable.  A mutable implementation with much higher insertion throughput
and lower garbage collection tolls is also available.

Special thanks to [rkapsi's](https://github.com/rkapsi)
[patricia-trie](https://github.com/rkapsi/patricia-trie) project for a nice
set of key analyzers and tests.  If you're more concerned with Java Map
interface compatibility and/or performance, you should probably use that
project instead.




