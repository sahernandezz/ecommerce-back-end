FROM gradle:latest AS BUILD
WORKDIR /usr/app/
COPY . .
RUN gradle build || return 0

FROM amazoncorretto:21
ENV JAR_NAME=ecommerce-back-end-0.0.1-SNAPSHOT.jar
ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME
COPY --from=BUILD $APP_HOME .
EXPOSE 8080
ENTRYPOINT exec java -jar $APP_HOME/build/libs/$JAR_NAME