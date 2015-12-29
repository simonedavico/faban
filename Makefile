REPONAME = faban
DOCKERIMAGENAME = benchflow/$(REPONAME)
VERSION = dev
JAVA_VERSION_FOR_COMPILATION = java-7-oracle
JAVA_HOME := `update-java-alternatives -l | cut -d' ' -f3 | grep $(JAVA_VERSION_FOR_COMPILATION)`"/jre"

.PHONY: all build_release 

all: build_release

clean:
	JAVA_HOME=$(JAVA_HOME) ant clean

build:
	JAVA_HOME=$(JAVA_HOME) ant build

build_release:
	JAVA_HOME=$(JAVA_HOME) ant build

test:
	JAVA_HOME=$(JAVA_HOME) ant test