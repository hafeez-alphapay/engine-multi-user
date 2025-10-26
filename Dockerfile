# ---- Base image (Tomcat 10 + JDK17) -----------------------------------------
FROM tomcat:10.1-jdk17-temurin AS base

# ---- Build-time args (optional) ---------------------------------------------
ARG WAR_FILE=target/payEngine-0.0.1-SNAPSHOT.war

# ---- Install small tools needed at build/runtime ----------------------------
# unzip   -> to pre-expand the WAR at build time
# curl    -> optional if you later add a container HEALTHCHECK
RUN apt-get update \
 && apt-get install -y --no-install-recommends unzip curl \
 && rm -rf /var/lib/apt/lists/*

# ---- Hardening: remove default Tomcat apps ----------------------------------
RUN rm -rf /usr/local/tomcat/webapps/*

# ---- Pre-expand WAR so Tomcat won't need to write on startup ----------------
COPY ${WAR_FILE} /tmp/ROOT.war
RUN mkdir -p /usr/local/tomcat/webapps/ROOT \
 && unzip -q /tmp/ROOT.war -d /usr/local/tomcat/webapps/ROOT \
 && rm -f /tmp/ROOT.war

# ---- Drop in hardened Tomcat configs (console-only logging, no access logs) -
# Provide these files in your repo under ./conf/
COPY conf/logging.properties /usr/local/tomcat/conf/logging.properties
COPY conf/server.xml         /usr/local/tomcat/conf/server.xml

# ---- JVM/Tomcat runtime options (container friendly) ------------------------
# - Use /tmp for Java temp (writable even with read-only root)
# - Force console logging, fail fast on init failures
ENV JAVA_TOOL_OPTIONS="-Djava.io.tmpdir=/tmp"
ENV CATALINA_OPTS="-Dtomcat.util.logging.console=true -Dorg.apache.catalina.startup.EXIT_ON_INIT_FAILURE=true"
# Pick the profile via ECS task env var if you want; default here is development
ENV JAVA_OPTS="-Dspring.profiles.active=production"

# ---- Security: run as non-root ---------------------------------------------
# Fixed uid:gid so you can set the same in ECS ("user": "1000:1000")
RUN set -eux; \
    groupadd -g 1000 tomcatuser || true; \
    useradd  -u 1000 -g 1000 -M -s /sbin/nologin tomcatuser || true; \
    chown -R 1000:1000 /usr/local/tomcat
USER 1000:1000

# ---- Ports & entrypoint -----------------------------------------------------
EXPOSE 8080
WORKDIR /usr/local/tomcat
CMD ["catalina.sh", "run"]