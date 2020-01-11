FROM adoptopenjdk/openjdk11:jre-11.0.5_10-alpine

ARG APP_NAME=demoTodoService
ARG APP_CONFIG=app-config.properties
ARG DEPLOY_DIR=/usr/local/opt/todo-service
ARG HTTP_PORT=7079
ARG HTTP_HOST=0.0.0.0
ARG REPO_HOST=repo
ARG REPO_PORT=7082

ENV APP_NAME=$APP_NAME
ENV APP_CONFIG=$APP_CONFIG
ENV DEPLOY_DIR=$DEPLOY_DIR
ENV HTTP_PORT=$HTTP_PORT
ENV HTTP_HOST=$HTTP_HOST
ENV REPO_HOST=$REPO_HOST
ENV REPO_PORT=$REPO_PORT

EXPOSE "$HTTP_PORT"

RUN mkdir -p $DEPLOY_DIR
COPY ./deploy/demo-todo-service-0.0.1-dist.jar $DEPLOY_DIR
COPY ./target/classes/app-config.properties $DEPLOY_DIR
WORKDIR $DEPLOY_DIR
CMD java -jar ./demo-todo-service-0.0.1-dist.jar  -p "$HTTP_PORT" -h "$HTTP_HOST" -n "$APP_NAME" -c "$APP_CONFIG" -repoHost "$REPO_HOST" -repoPort "$REPO_PORT"
