
# Time from TimeAndMoney by Domain Language

This repo contains an extraction of the `Money` type from [the TimeAndMoney library](https://timeandmoney.sourceforge.net/), 
published by [Eric Evans](https://www.linkedin.com/in/ericevansddd/)' company so long ago.

At the time (about 2004), standard Java did not provide good implementation of time and calendar 
related concepts, so it made sense for others to provide them.  Since tne introduction
of LocalDate and related classes in Java 1.8 (2014), there is no need to use external libraries
for dates and time; there is, however, still [a good case for implementing a Money abstraction](https://hilton.org.uk/blog/money-data-type).

Over the course of the years, I've seen various implementations of Money classes in the projects
I've been involved in, and they all more-or-less worked by wrapping Java's `BigDecimal`, which is 
not a very easy-to-use class.  So I thought, why not go back to the work of recognized experts and 
make it available in modern Java, unencumbered by the time and date parts of the original library?

What I did:
* Extracted the Money.java and MoneyTest.java files from the svn archive published on SourceForge
* Replaced uses of `com.domainlanguage.base.Rounding` with the corresponding values in `java.math.RoundingMode`
* Eliminated the default constructor and other methods that were "put in here begrudgingly" for the benefit of ORMs
* Updated tests to Junit 5

The code in this repo is Copyright (c) 2004 [Domain Language, Inc.](http://domainlanguage.com) and
published under the "MIT" license. See file LICENSE. 
