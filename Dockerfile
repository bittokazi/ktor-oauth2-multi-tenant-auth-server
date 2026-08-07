FROM eclipse-temurin:21-jammy
EXPOSE 5035

ARG RELEASE_TAG=""

RUN mkdir /app
COPY --chown=gradle:gradle ./backend-${RELEASE_TAG}.jar /app/backend.jar
COPY --chown=gradle:gradle ./gateway-${RELEASE_TAG}.jar /app/gateway.jar
COPY --chown=gradle:gradle ./entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

CMD ./app/entrypoint.sh
