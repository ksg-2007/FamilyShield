#!/bin/sh
set -e
mvn clean package
java -jar target/FamilyShield.jar
