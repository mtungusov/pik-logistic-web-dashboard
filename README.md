# pik-logistic-dashboard

## Dev

```
rlwrap lein do clean, with-profile dev figwheel
-or-
lein cooper
```

## Prod

```
lein do clean, with-profile prod cljsbuild once
```


### In Cursive REPL
```
(use 'figwheel-sidecar.repl-api)
(cljs-repl)
```
