# Stockroom

### Requirements

* Install [Leiningen](https://github.com/technomancy/leiningen) - be sure to get version 2.
* Ruby and Bundler
* MySQL >= 5.5


### Setup

To setup your environment:

```bash
$ bundle install
$ bundle exec rake db:setup
$ WAREHOUSE_ENV=test bundle exec rake db:setup
```

To add yourself as a super user locally:

1. Start the server

  ```bash
  $ lein run
  ```


* http://localhost:8080 in the browser

2. Sign in with Google
3. The system will show your Google UID.
4. Copy the UID and run this script to create a super user:

  ```bash
  $ lein run -m stockroom.task.super-user <UID>
  ```

5. Refresh the page

### Development

To run the server in development mode:

```bash
$ lein run # run with development config
```


To run the tests:

```bash
$ lein spec
```

Some helpful scripts:

```bash
$ bundle exec rake -T
```
