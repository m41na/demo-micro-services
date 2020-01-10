FROM adoptopenjdk/openjdk11:jre-11.0.5_10-alpine

ARG APP_NAME=demoRepoService
ARG DEPLOY_DIR=/usr/local/opt/repo-service
ARG HTTP_PORT=7082
ARG HTTP_HOST=localhost
ARG HTTPS_PORT=8445

ENV APP_NAME=$APP_NAME
ENV DEPLOY_DIR=$DEPLOY_DIR
ENV ASSETS_DIR=$DEPLOY_DIR/www
ENV HTTP_PORT=$HTTP_PORT
ENV HTTP_HOST=$HTTP_HOST
ENV HTTPS_PORT=$HTTPS_PORT

RUN mkdir -p $DEPLOY_DIR
COPY ./deploy/demo-repo-service-0.0.1-dist.jar $DEPLOY_DIR
WORKDIR $DEPLOY_DIR

EXPOSE "$HTTP_PORT"
EXPOSE "$HTTPS_PORT"

RUN echo "demo-repo will run in host $HTTP_HOST on port $HTTP_PORT/$HTTPS_PORT"
CMD java -jar ./demo-repo-service-0.0.1-dist.jar works.hop.repo.RepoApp -p "$HTTP_PORT" -h "$HTTP_HOST" -n "$APP_NAME" -httpsPort "$HTTPS_PORT" -assets "$ASSETS_DIR"

