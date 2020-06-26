# Car Listings Scraper 🚗

### Created by Simon and Jordan

This is a REST API created using Springboot that scrapes car sales listing sites to help find the best deal without manually searching.

## Requirements

* Java version "11.0.7" 2020-04-14 LTS
* Apache Maven 3.6.3
* Jsoup

## Running Locally

1. Run `./mvnw spring-boot:run`
2. Visit `http://localhost:8080/search?make=Volvo&model=MaxS&minPrice=5000&maxPrice=10000`

## Deploying/redeploying .jar

1. Login to EC2 instance by running the following `ssh -i "autogo-ec2-instance.pem" ec2-user@ec2-3-10-106-69.eu-west-2.compute.amazonaws.com`
2. To remove current .jar, run `rm -rf name-of-target.jar`
3. In another GitBash terminal, navigate to desktop directory, run the following to upload a .jar `scp -i autogo-ec2-instance.pem scraper/target/scraper-0.0.1-SNAPSHOT.jar ec2-user@ec2-3-10-106-69.eu-west-2.compute.amazonaws.com:~`
4. Run `java -jar name-of-target.jar`



cp -r usr/bin/amazon-corretto-11.0.7.10.1-linux-x64/bin usr/bin

cp -a /usr/bin/amazon-corretto-11.0.7.10.1-linux-x64/bin/. /usr/bin/
