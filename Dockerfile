FROM oracle/graalvm-ce:19.2.1
COPY . /tmp
RUN gu install native-image
WORKDIR /tmp/spring-graal-native-samples/commandlinerunner
COPY spring-graal-native-samples/commandlinerunner/compile.sh /compile.sh
RUN ["chmod", "+x", "/compile.sh"]
RUN /compile.sh
