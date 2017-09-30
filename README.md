# rr

rr (pronounced "rr") is a tiny ClojureScript library for state management. Without mutable state, as the name implies.

## Usage

Add the following entry to your project.clj :dependencies:

   `[rr "0.1.1"]`

## Example

```
cljs.user=>(require '[rr.core :as rr])
nil
cljs.user=> (rr/defaction add [state x] (update-in state [:val] + x))
#object[cljs.core.MultiFn]
cljs.user=> (rr/defaction mult [state x] (update-in state [:val] * x))
#object[cljs.core.MultiFn]
cljs.user=> (rr/disp! add 2)
nil
cljs.user=> (rr/disp! mult 5)
nil
cljs.user=> (rr/disp! add 5)
nil
cljs.user=> (rr/play)
{:val 15}
cljs.user=> (rr/defaction mult [state x] (update-in state [:val] * 10 x))
#object[cljs.core.MultiFn]
cljs.user=> (rr/play)
{:val 105}
cljs.user=> (rr/commit!)
{:val 105}
```

## Overview

- Simple API: `defaction` and `disp!`, `play` and `commit!`.
- Immmutable state, no atom.
- Code change of previously executed actions is reloadable, current state is never stale.
- Open: actions are printable data (serializable).
- Fast reduction: actions yield memoized functions.
- Transducer friendly, xforms provided for logging, rendering and debugging.

## Rationale

_[...] every primitive recursive function on lists can be redefined in terms of
fold._
Graham Hutton, (1999) A Tutorial on the Universality
and Expressiveness of Fold. Journal of Functional
Programming, 9 (4). pp. 355-372.

In Land of LISP, `fold`'s name is `reduce`. Consider this:
```clj
(reduce + init-val [10 20 30])
=> 60
```
Then mentally map it to that:
```clj
(reduce apply initial-state [action1 action2 action3])
=> current-state
```
Read: apply `apply` to `action1` and the `initial-state`; then apply `apply` to `action2` and the previous resulting state; then apply `apply`... when the list is exhausted (reduced), the returned value is the `current-state`.

Reduce/fold as "universal and expressive" as it gets. This concept, popularized by [Redux JS](http://redux.js.org/), and inspired by the [Elm Architecture](https://guide.elm-lang.org/architecture/), will maybe help ClojureScript users to get over with the relatively complected usage of atoms to manage app state.

## Status

Wet paint. More paint coming.

## CHANGES:
### 0.1.1
- add: commit! arity 2 (pass your own store, Mr Hauman ^)
- add: fifo memoization
- add: rf arity 0
- add: defaction can take a docstring and metadata (cljs "meta" gotchas still apply).
- change: (BREAKING) play arity 1 and 2 params. no change to arity 0.
- change: (BREAKING) removed render-watch, set watches directly on the store atom.
- change: (BREAKING) removed default transform (log and render), set xf manually where needed.


## Development

To get an interactive development environment run:

    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

    lein clean

To create a production build run:

    lein do clean, cljsbuild once min

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL.

## License

Copyright © 2016 François De Serres (@fdserr)

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
