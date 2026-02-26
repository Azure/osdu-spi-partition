FROM alpine
WORKDIR /opt
COPY ./devops/core-plus/bootstrap/ /opt/

RUN apk update && apk add \
    bash \
    curl \
    jq \
    && chmod 775 bootstrap_partition.sh
RUN addgroup -g 10001 -S nonroot \
  && adduser -h /opt -G nonroot -S -u 10001 nonroot
RUN chown -R 10001:10001 /opt
USER 10001:10001
CMD ["/bin/bash", "-c", "./bootstrap_partition.sh && sleep 365d"]
