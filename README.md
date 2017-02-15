# Weighscore

The Weighscore Neural Network Server gives the ability to train neural networks online, simultaneously with their querying. When new data that characterizes the response that was given earlier arrives, the network may be slightly updated using the new data without stopping.
Moreover, there are the following advantages:
- thread safe simultaneous processing - suitable for distributed calling; 
- the initial neural network training may be performed by free command line tool that uses JDBC data source as a training case set;
- it is platform independent, written in java, so it may be run on Solaris, Windows, Linux etc;
- it converts the simple string reference entries to neurons' numeric values, so it requires minimum preliminary data transformation;
- stores the neural network in a simple XML file which is editable with any text editor;
- it is very simple to install and use: it is shipped as one executable JAR file; it doesn't require any additional third-party libraries;
- easily configurable with simple plain-test configuration files;
- is well documented;
- easily called with a simple protocol - there are examples for java and winsock in the reference manual; 
- callable as an Axis webservice - easy to call from almost any client platform.
