FROM adoptopenjdk/openjdk11:jre-11.0.5_10-alpine

ARG DEPLOY_NAME=demoRestService
ARG DEPLOY_DIR=/usr/local/etc/zesty
ARG ASSETS_DIR=$DEPLOY_DIR/www
ARG DEPLOY_HTTP_PORT=7080
ARG DEPLOY_HTTPS_PORT=8443
ARG DEPLOY_HTTP_HOST=localhost

ENV APP_NAME=$DEPLOY_NAME
ENV APP_JAR=$DEPLOY_JAR
ARG HTTP_PORT=$DEPLOY_HTTP_PORT
ARG HTTPS_PORT=$DEPLOY_HTTPS_PORT
ARG HTTP_HOST=$DEPLOY_HTTP_HOST

RUN mkdir -p $DEPLOY_DIR
COPY ./deploy/demo-rest-service-0.0.1-dist.jar $DEPLOY_DIR
WORKDIR $DEPLOY_DIR
#RUN java -jar ./demo-rest-service-0.0.1-dist.jar com.practicaldime.zesty.app.AppLoader -p $HTTP_PORT -h $HTTP_HOST -n $APP_NAME -httpsPort $HTTPS_PORT -assets $ASSETS_DIR
CMD ["java", "-jar", "./demo-rest-service-0.0.1-dist.jar", "com.practicaldime.zesty.app.AppLoader", "$HTTP_HOST", "-n", "$APP_NAME"]
