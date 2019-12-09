This directory contains an example task definition for the Elastic Container Service (ECS) provided by Amazon Web Services (AWS).

### Sample deploy using the CLI

First, we create a repository for each Docker image that we want to deploy (one-time operation).
```
#for example, registry = 400225363032.dkr.ecr.us-east-2.amazonaws.com
aws ecr create-repository --repository-name demo-tt/database --region $region
aws ecr create-repository --repository-name demo-tt/webapp --region $region
```

Then, when we have a new image that we want to deploy, we push it to the registry:

```
aws configure
#The following command prints the `docker login` command that we'll use in the next step
aws ecr get-login --region $region --no-include-email #for example, region = us-east-2
docker login ...
docker images #to see the images

docker tag demo-tt-database:5.1.1 $registry/demo-tt/database:5.1.0
docker tag demo-tt:5.1.1 $registry/demo-tt/webapp:5.1.1
docker push $registry/demo-tt/database:5.1.0 
docker push $registry/demo-tt/webapp:5.1.1
```
