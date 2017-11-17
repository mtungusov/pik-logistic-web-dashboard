# pik-logistic-dashboard

A [re-frame](https://github.com/Day8/re-frame) application designed to ... well, that part is up to you.

## Development Mode

### Run application:

```
lein clean
rlwrap lein figwheel dev
```

#### -OR-

```
lein cooper
```


### In Cursive REPL
```
(use 'figwheel-sidecar.repl-api)
(cljs-repl)
```

Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).

## Production Build


To compile clojurescript to javascript:

```
lein clean
lein cljsbuild once min
```

lein do clean, cljsbuild once min