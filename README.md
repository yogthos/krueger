# krueger

Federated news

## Prerequisites

* [JDK](https://www.azul.com/downloads/zulu/)
* [Leiningen](https://leiningen.org/)
* PostgreSQL

## Running

Create a local configuration file called `dev-config.edn`, and set the database connection there:

```clojure
{:database-url "postgresql://localhost/krueger?user=<user>&password=<pass>"
 :dictionary "dictionary.edn"}
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

Krueger may be freely used under the [AGPL license](https://www.gnu.org/licenses/agpl-3.0.html). As per the AGPL, you can use Krueger for personal or commercial purposes for free, but, you must publically release any modifications to the software (under the same license).

Copyright Dmitri Sotnikov © 2018
