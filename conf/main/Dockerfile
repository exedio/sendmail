FROM debian:stretch-20210511

ARG JDK
RUN \
	apt-get update && \
	apt-get install --no-install-recommends --assume-yes \
		${JDK}-jdk-headless && \
	rm -rf /var/lib/apt/lists/*

ARG JENKINS_OWNER
RUN \
	mkdir /var/jenkins-build-survivor && \
	chown ${JENKINS_OWNER} /var/jenkins-build-survivor

LABEL purpose=jenkins
LABEL jdk=${JDK}
