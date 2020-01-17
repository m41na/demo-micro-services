FROM adoptopenjdk/openjdk11:jre-11.0.5_10-alpine

ARG APP_NAME=demoWebAcceptor
ARG DEPLOY_DIR=/usr/local/opt/web-acceptor
ARG ASSETS_DIR=www
ARG HTTP_PORT=7081
ARG HTTP_HOST=0.0.0.0
ARG HTTPS_PORT=8444
ARG NATS_HOST=dnats
ARG NATS_PORT=4222
ARG NATS_TOPIC=nats-events
ARG MAX_RECONNECT=-1
ARG NATS_CONFIG=/app-config.properties

ENV APP_NAME=$APP_NAME
ENV DEPLOY_DIR=$DEPLOY_DIR
ENV ASSETS_DIR=$ASSETS_DIR
ENV HTTP_PORT=$HTTP_PORT
ENV HTTP_HOST=$HTTP_HOST
ENV HTTPS_PORT=$HTTPS_PORT
ENV NATS_HOST=$NATS_HOST
ENV NATS_PORT=$NATS_PORT
ENV NATS_TOPIC=$NATS_TOPIC
ENV MAX_RECONNECT=$MAX_RECONNECT
ENV NATS_CONFIG=$NATS_CONFIG

RUN mkdir -p "$DEPLOY_DIR"/"$ASSETS_DIR"
COPY ./www/index.html "$DEPLOY_DIR"/"$ASSETS_DIR"
COPY ./www/index.css "$DEPLOY_DIR"/"$ASSETS_DIR"
COPY ./deploy/demo-web-acceptor-0.0.1-dist.jar $DEPLOY_DIR
WORKDIR $DEPLOY_DIR

EXPOSE "$HTTP_PORT"
EXPOSE "$HTTPS_PORT"

RUN echo "demo message acceptor will run in host $HTTP_HOST on port $HTTP_PORT/$HTTPS_PORT and listen for nats messages from $NATS_TOPIC topic through host $NATS_HOST on port $NATS_PORT"
CMD java -jar ./demo-web-acceptor-0.0.1-dist.jar works.hop.acceptor.AcceptorApp -p "$HTTP_PORT" -h "$HTTP_HOST" -n "$APP_NAME" -httpsPort "$HTTPS_PORT" -assets "$ASSETS_DIR" -natsHost "$NATS_HOST" -natsPort "$NATS_PORT" -natsTopic "$NATS_TOPIC" -c "$NATS_CONFIG"
