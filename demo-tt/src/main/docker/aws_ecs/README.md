## Running Portofino applications on Amazon ECS 

This directory contains an example task definition for the Elastic Container Service (ECS) provided by Amazon Web Services (AWS).

### Deployment using the CLI: first time

First, we create a repository for each Docker image that we want to deploy (this is a one-time operation):
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

We now need a cluster (a) to run a task (b) which is kept alive by a service (c). So let's create what we don't have yet.
If we already have (a), (b) and (c), then we can proceed to the next section.

Let's start from the cluster (a):

TODO

Now we can create a task (b) using the demo-tt-taskdef.json as a template:

TODO

Then, we'll want to create a service (c) to keep that task running:

TODO

### Deployment using the CLI: updating a service

We'll start by creating, tagging and pushing the Docker image(s) again. We'll reuse the same repositories, unless we've
added new images; in that case, refer to the previous section.

We can give each image a new version number; in that case, we'll need to update our task to reference the new versions,
and then update our service to deploy the new task.

Or, we can leave the same version numbers (e.g. while doing integration tests); then, we'll update the service by just
checking the box "force new deployment"
([docs](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/update-service.html)).

Of course, we can also mix the two, and update some version numbers while leaving others as they are.

### Connecting to our instance via SSH

Note the instance public IP on https://us-east-2.console.aws.amazon.com/ec2/home#Instances

Then, run a command like the following:
```
ssh -i keypair.pem ec2-user@x.y.z.k
```
