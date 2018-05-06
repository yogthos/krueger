# krueger

Federated news

## Prerequisites

Krueger uses PostgreSQL as the persistence layer

## Running

Create a local configuration file called `dev-config.edn`, and set the database connection there:

```clojure
{:database-url "postgresql://localhost/krueger?user=<user>&password=<pass>"}
```

Next run the migrations:

```
lein run migrate
```

Start the server

```
lein run 
```

## License

Copyright © 2018
