# RAR: исходники в ./bitrix24-ra (JCA outbound-адаптер к Bitrix REST; не бинарь с портала Bitrix)
# WAR: eis-gateway — HTTP -> JNDI java:/eis/Bitrix24CF -> CCI
FROM eclipse-temurin:17-jdk AS rar
WORKDIR /opt/bitrix24-ra
COPY bitrix24-ra/ .
RUN chmod +x gradlew && ./gradlew --no-daemon clean assemble

FROM gradle:8.10.2-jdk17 AS gateway
WORKDIR /w
COPY . /w
RUN ./gradlew :bitrix-eis-gateway:war --no-daemon -q

FROM quay.io/wildfly/wildfly:31.0.1.Final-jdk17
ENV WILDFLY_HOME=/opt/jboss/wildfly
ENV BITRIX_WEBHOOK_BASE_URL="" \
    BITRIX_CONNECT_TIMEOUT_MS=10000 \
    BITRIX_READ_TIMEOUT_MS=30000
COPY --from=rar /opt/bitrix24-ra/build/distributions/bitrix24-ra.rar ${WILDFLY_HOME}/standalone/deployments/bitrix24-ra.rar
COPY --from=gateway /w/bitrix-eis-gateway/build/libs/eis-gateway.war ${WILDFLY_HOME}/standalone/deployments/eis-gateway.war
EXPOSE 8080 9990
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
