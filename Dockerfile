FROM docker:27-cli

RUN apk add --no-cache bash make git

WORKDIR /workspace

ENTRYPOINT ["bash", "-lc"]
CMD ["make help"]
