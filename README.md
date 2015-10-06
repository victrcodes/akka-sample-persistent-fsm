# akka-sample-persistent-fsm

A very simple example demonstrating ```PersistentFSM``` functionality. The app generates 5 random numbers at single run and puts them into sequence.

## Usage

```sbt run```

generates 5 new random numbers and appends them to an existing persisted sequence

```sbt "run reset"```

does the same as above, except "reseting" - deleting the existing persisted data before