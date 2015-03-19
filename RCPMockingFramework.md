# Motivation #

Currently existing mocking frameworks like [EasyMock](http://www.easymock.org/), [JMock](http://www.jmock.org/), [JMockit](https://jmockit.dev.java.net/), [Mockito](http://mockito.org/) and [PowerMock](http://powermock.org/) have some short comings when testing Eclipse Plug-ins, RCP applications and OSGi applications (e.g. concerning class loading and threading issues). The framework here to be developed should address these things.

# The issue with the cglib classloader #

The problem with the cglib classloader has already been solved.

## JMock ##
  * [Can't mock classes in Eclipse JUnit Plugin-Tests](http://jira.codehaus.org/browse/JMOCK-124)
  * [jMock - Mocking Classes in Eclipse Plug-in Tests](http://www.jmock.org/eclipse.html)

## Mockito ##
  * [Mockito does not work in eclipse plugins](http://code.google.com/p/mockito/issues/detail?id=11)

## EasyMock ##
  * [CodeGenerationException mocking a class in Eclipse plugin](http://sourceforge.net/tracker/?func=detail&aid=2994002&group_id=82958&atid=567837)