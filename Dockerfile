FROM openjdk:21-jdk-slim

# Install SQLite
RUN apt-get update && \
    apt-get install -y sqlite3 && \
    rm -rf /var/lib/apt/lists/*

# Install Litestream
RUN apt-get update && \
    apt-get install -y wget && \
    wget https://github.com/benbjohnson/litestream/releases/download/v0.3.5/litestream-v0.3.5-linux-amd64.deb && \
    dpkg -i litestream-v0.3.5-linux-amd64.deb && \
    rm -f litestream-v0.3.5-linux-amd64.deb && \
    apt-get clean

# Set the working directory inside the container
WORKDIR /app

# Copy the entire project directory into the container
COPY /target/*-standalone.jar /app/home-dashboard.jar

# Specify the command to run your application
CMD litestream restore -o sensor.db s3://$SENSOR_BUCKET/db && litestream replicate -exec "java -jar /app/home-dashboard.jar" sensor.db s3://$SENSOR_BUCKET/db
