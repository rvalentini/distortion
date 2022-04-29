# Firefly

This is an animation project for exploring optical distortion using shadow-cljs and Quil.

## Usage
This command will start the dev process, which will start an nREPL, build the cljs sources and recompile on every file change:
```bash
clj -M:shadow-cljs watch app
```
Note: The command `npx shadow-cljs watch app` is used when using shadow-cljs for dependency management.\

Application is available on port `8080`

## IntelliJ spcific setup

### dependencies via pom.xml

1. Prepare run configuration to connect to REPL that was started by the watch command.
2. When connected to the nREPL upgrade from CLJ REPL to CLJS REPL by sending
```clojure
(shadow/repl :app)
```
## Noob troubleshooting

* Error message `No available JS runtime.`\
-> Open `localhost:8080` - The page needs to be open in the browser so the compiled JS code can be loaded by the browsers JS runtime.
* The required JS dependency "react" is not available, it was required by ...\
-> Install required react JS dependencies by running `npm install react react-dom create-react-class`

