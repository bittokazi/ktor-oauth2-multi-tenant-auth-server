FROM eclipse-temurin:21-jammy
EXPOSE 5035

ARG CONFIG_ENV=""

RUN mkdir /app
COPY --chown=gradle:gradle ./backend/build/libs/*.jar /app/backend.jar
COPY --chown=gradle:gradle ./gateway/build/libs/*.jar /app/gateway.jar
COPY --chown=gradle:gradle ./entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

CMD ./entrypoint.sh
