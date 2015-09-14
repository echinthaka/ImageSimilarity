# add oracle java repo
sudo apt-add-repository ppa:webupd8team/java

# add sbt repo
echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 642AC823

sudo apt-get update
sudo apt-get remove scala-library scala

# install sbt and jdk
sudo apt-get install oracle-java7-installer
sudo apt-get install sbt=0.13.5

# install scala
sudo wget www.scala-lang.org/files/archive/scala-2.10.4.deb
sudo dpkg -i scala-2.10.4.deb
