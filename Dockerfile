FROM openjdk:11
COPY build/libs/gw-delivery-0.0.1-SNAPSHOT.jar parceldeliverygw.jar
EXPOSE 8098
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75.0", "-jar", "./parceldeliverygw.jar", "-Duser.timezone=Asia/Baku", "--spring.profiles.active=local"]