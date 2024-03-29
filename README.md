# DistLedger

Distributed Systems Project 2022/2023

## Authors

**Group A58**

### Team Members

| Name           | User                               | Email                                      |
| -------------- | ---------------------------------- | ------------------------------------------ |
| Beatriz Matias | <https://github.com/JS-GHub>       | <mailto:joaohsereno@tecnico.ulisboa.pt>    |
| João Sereno    | <https://github.com/bea-w>         | <mailto:beatriz.matias@tecnico.ulisboa.pt> | 
| Matilde Heitor | <https://github.com/matildeheitor> | <mailto:matildenheitor@tecnico.ulisboa.pt> |

## Getting Started

The overall system is made up of several modules. The main server is the _DistLedgerServer_. The clients are the _User_ 
and the _Admin_. The definition of messages and services is in the _Contract_. The future naming server
is the _NamingServer_. The definition of the VectorClock class is in the _Utils_.

See the [Project Statement](https://github.com/tecnico-distsys/DistLedger) for a complete domain and system description.

### Prerequisites

The Project is configured with Java 17 (which is only compatible with Maven >= 3.8), but if you want to use Java 11 you
can too -- just downgrade the version in the POMs.

To confirm that you have them installed and which versions they are, run in the terminal:

```s
javac -version
mvn -version
```

### Installation

To compile and install all modules:

```s
mvn clean install
```

### Instructions

#### `NamingServer`
To run:
```s
mvn compile exec:java 
```

To run with debug annotations:
```s
mvn compile exec:java -Ddebug
```

#### `DistLedgerServer`
To run:
```s
mvn compile exec:java -Dexec.args="<port> <qualifier>"
```
where `<qualifier>` can be either `A`, `B` or `C`. 

To run with debug annotations:
```s
mvn compile exec:java -Dexec.args="<port> <qualifier>" -Ddebug
```
where `<qualifier>` can be either `A`, `B` or `C`.

#### `User`
To run:
```s
mvn compile exec:java
```

To run with debug annotations:
```s
mvn compile exec:java -Ddebug
```

#### `Admin`
To run:
```s
mvn compile exec:java
```

To run with debug annotations:
```s
mvn compile exec:java -Ddebug
```

## Built With

* [Maven](https://maven.apache.org/) - Build and dependency management tool;
* [gRPC](https://grpc.io/) - RPC framework.
