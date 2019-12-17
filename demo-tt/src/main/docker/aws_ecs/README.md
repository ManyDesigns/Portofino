## Running Portofino applications on Amazon ECS 

This directory contains an example task definition for the Elastic Container Service (ECS) provided by Amazon Web Services (AWS).

### Deployment using the CLI

#### Setup

We'll perform these operations only the first time.

First, let's install the AWS CLI and the ECS CLI. Refer to Amazon's tutorials.

Then, we can configure the AWS CLI to store our credentials and the default region (e.g., eu-central-1):

```
aws configure
```

Finally, let's create a repository for each Docker image that we'll want to deploy:

```
#for example, registry = 400225363032.dkr.ecr.us-east-2.amazonaws.com
aws ecr create-repository --repository-name demo-tt/database
aws ecr create-repository --repository-name demo-tt/webapp
```

#### Uploading the Software

Then, whenever we have a new image that we want to deploy, we push it to the registry:

```
#The following command prints the `docker login` command that we'll use in the next step
aws ecr get-login --region $region --no-include-email #for example, region = eu-central-1
docker login ...
docker images #to see the images

docker tag demo-tt-database:5.1.1 $registry/demo-tt/database:5.1.0
docker tag demo-tt:5.1.1 $registry/demo-tt/webapp:5.1.1
docker push $registry/demo-tt/database:5.1.0 
docker push $registry/demo-tt/webapp:5.1.1
```

#### Running the Application

To run the application, we need a cluster (a) to run a task (b) which is kept alive by a service (c). So let's create
what we don't have yet. If we already have (a), (b) and (c), then we can proceed to the next section.

Let's start from the cluster (a), by following Amazon's own tutorials using
[the ECS CLI](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/ecs-cli-tutorial-ec2.html) and
[the AWS CLI](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/ECS_AWSCLI_EC2.html):

```
ecs-cli configure --cluster demo-tt --default-launch-type EC2 --config-name demo-tt --region $region
ecs-cli configure profile --access-key AWS_ACCESS_KEY_ID --secret-key AWS_SECRET_ACCESS_KEY --profile-name demo-tt
ecs-cli up --keypair demo-tt --capability-iam --size 1 --instance-type t2.large --cluster-config demo-tt --ecs-profile demo-tt
```

This will create also:
 - one VPC
 - one security group
 - two subnets.
 
Let's take note of their id's and move on.

Now we can create a task (b) using the demo-tt-taskdef.json as a template:

```
aws ecs register-task-definition --cli-input-json demo-tt-taskdef.json
#take note of the task definition ARN
#we can see it again with:
aws ecs list-task-definitions
```

Then, we'll want to create a service (c) to keep that task running. But first, let's make sure we have the necessary
network resources:

```
aws elbv2 create-load-balancer \
     --name demo-tt-app-lb \
     --subnets SubnetId1 SubnetId2
aws elbv2 create-target-group --name demo-tt-webapp --protocol HTTP --port 80 --target-type ip --vpc-id <VpcId>
```

This creates a load balancer to expose our webapp to the outside world.

For the next part, we resort to using the GUI: create a service with the given task definition and load balancer. The
following is work in progress and doesn't work yet:

```
aws ecs create-service \
  --service-name demo-tt --task-definition <arn> --desired-count 1 --launch-type EC2 --cluster demo-tt-lb \
  --network-configuration "awsvpcConfiguration={subnets=[SubnetId1,SubnetId2]}" --load-balancer demo-tt-app-lb
```

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

### Split API and Webapp Over Different Containers

TODO
