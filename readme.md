(Readme not finished)

Visually follow real-time build status
- Is my application working?
- Can I publish my application?
- Which builds are running? When will they finish?

Gain insight with details
- How long ago did events happen?
- Latest change in build

Quickly react to common build errors
- Why are my builds failing?

Stay informed about recent project events
- What has happened when I was at lunch, or sick?
- Make better decisions with better data




A build is a set of actions the build server performs.
Actions can be:
- code on a branch, or many branches
- compilation or minification or similar
- initialization of test database
- running tests
- email/other notifications


How to get started developing
======

There are three builds:
- `min`: production, minified build. Accessible at `index.html`
- `dev`: not minified. Located at `index-dev.html`
- `devcards`: Tests and isolated test environments. Located at `devcards.html`

You can start the development environment like so:

- start emacs
- open `project.clj`
- M-x cider-jack-in-clojurescript
- navigate to the cljs repl buffer

    ;; When connected, write

    (cljs "devcards")
    to connect to the devcards build

    or to connect to the dev build, write
    (cljs "dev")

Figwheel builds and development time commands are defined in dev/user.clj

Once the previous commands have been executed, you can navigate to one of these
pages:
- http://localhost:3449/
- http://localhost:3449/index-dev.html
- http://localhost:3449/cards.html
