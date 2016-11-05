# rr

rr (pronounced "rr") is a tiny ClojureScript library for state management. Without mutable state, as the name implies.

## Usage

Not yet pushed to clojars, build and install locally:
```
git clone https://github.com/fdserr/rr.git
cd rr
lein do cljsbuild once min, install
```
Add the following entry to your project.clj :dependencies:

   `[rr "0.1.1-SNAPSHOT"]`

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

TODO:
- Spec rr
- Leverage spec / exercise actions.
- Server side replay, sync up.
- Kewl debugger.

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
