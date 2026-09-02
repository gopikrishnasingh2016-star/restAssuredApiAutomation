# ---------------------------------------------------------------------------
# Test-runner image: everything needed to execute the suite anywhere Docker runs,
# with no local Java or Maven installation.
#
#   docker build -t api-automation:latest .
#   docker run --rm -e ENV=mock -e SUITE=testng -v "$PWD/target:/app/target" api-automation:latest
#   docker run --rm -e ENV=uat -e SUITE=cucumber -e TAGS="@smoke" \
#              -e AUTH_USERNAME=... -e AUTH_PASSWORD=... api-automation:latest
# ---------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21

LABEL org.opencontainers.image.title="REST Assured API Automation" \
      org.opencontainers.image.description="Java 21 + REST Assured + TestNG + Cucumber API test suite" \
      org.opencontainers.image.source="https://github.com/gopikrishnasingh2016-star/restAssuredApiAutomation"

WORKDIR /app

# Dependencies are resolved in their own layer so code changes do not re-download the world.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src

# Defaults; every one of them is overridable with -e at run time.
ENV ENV=mock \
    SUITE=testng \
    TAGS="not @wip" \
    THREADS=4 \
    MAVEN_OPTS="-XX:MaxRAMPercentage=75"

# The suite's exit code is the container's exit code, which is what CI needs.
ENTRYPOINT ["sh", "-c", "mvn -B test -Denv=${ENV} -Dsuite=${SUITE} -Dtags=\"${TAGS}\" -Dthreads=${THREADS} ${MAVEN_EXTRA_ARGS}"]
