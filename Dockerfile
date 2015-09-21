FROM ubuntu:latest
RUN echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
# RUN sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 642AC823
RUN sudo apt-get install -yy apt-transport-https
RUN sudo apt-get install -yy software-properties-common

# Install JAVA
RUN sudo apt-add-repository ppa:webupd8team/java
RUN sudo apt-get update
RUN echo debconf shared/accepted-oracle-license-v1-1 select true | sudo debconf-set-selections
RUN echo debconf shared/accepted-oracle-license-v1-1 seen true | sudo debconf-set-selections
RUN sudo apt-get install -y wget libjansi-java oracle-java7-installer

#Install Scala and sbt
RUN sudo apt-get remove scala-library scala
RUN sudo apt-get install -y --force-yes sbt=0.13.6

# Install scala
RUN sudo wget www.scala-lang.org/files/archive/scala-2.10.4.deb
RUN sudo dpkg -i scala-2.10.4.deb

# javacv depends on latest libstdc++.
RUN sudo add-apt-repository ppa:ubuntu-toolchain-r/test
RUN sudo apt-get update
RUN sudo apt-get install -y gcc libstdc++6
ADD . /src
WORKDIR src/
RUN sbt clean compile
RUN sbt assembly
CMD scala -classpath target/scala-2.10/imageservice.jar com.chinthaka.imagesimilarity.server.JettyServer localhost
