FROM adoptopenjdk/openjdk15
ARG VERSION
COPY "build/libs/finch-api-${VERSION}.jar" "/finch.jar"

EXPOSE 8080
CMD java -Dlog4j2.formatMsgNoLookups=true -jar finch.jar