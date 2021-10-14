# Hibernate and Java threads

## Clone & Run

```
git clone git@github.com:haba713/hibernate_threads.git
cd hibernate_threads/
./gradlew test
    ...
    thread-17: mutex lock, begin, update
    thread-17: 1 + 1 = 2
    thread-17: commit, mutex release
    thread-17: mutex lock, begin, update
    thread-18: mutex lock, begin, update
    thread-19: mutex lock, begin, update
    thread-17: 2 + 1 = 3
    thread-17: commit, mutex release
    thread-18: 2 + 1 = 3
    thread-18: commit, mutex release
    ...
```

(The output varies due to thread scheduling.)

## The test implementation

See the [test implementation](src/test/java/haba713/hibernate_threads/MyEntityTest.java).

## Question #1: Java mutex

```
thread-17: mutex lock, begin, update
thread-18: mutex lock, begin, update
...
thread-17: commit, mutex release
```

How can `thread-18` achieve that line between the two `thread-17` lines in a
synchronized method that uses the boolean variable `mutex` to decide whether
the thread should be yielded or not?

## Question #2: Entity attribute value after commit

```
thread-17: 2 + 1 = 3
thread-17: commit, mutex release
thread-18: 2 + 1 = 3
thread-18: commit, mutex release
```

How can `oldValue` be `2` on `thread-18` even though `thread-17` has committed
the value `3`?  Maybe it's because the Java mutex fails (Question #1) and both
threads can enter parallel the line `int oldValue = myEntity.getMyColumn()`
where the old value is read?
