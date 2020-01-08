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

docker tag demo-tt-database:5.1.1 $registry/demo-tt/database:5.1.1
docker tag demo-tt:5.1.1 $registry/demo-tt/webapp:5.1.1
docker push $registry/demo-tt/database:5.1.1
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
#take note of the load balancer and target group ARN's
```

This creates a load balancer to expose our webapp to the outside world. However, the load balancer does not listen to
any connection yet; we have to create a listener. First, we'll get a listener JSON template to fill:

```
aws elbv2  create-listener --generate-cli-skeleton > listener.json
```

Then, let's edit `listener.json` with the appropriate data. In this directory we can find an example. We'll then feed
our JSON file to the AWS CLI:

```
aws elbv2 create-listener --cli-input-json file:///home/alessio/projects/portofino5/demo-tt/src/main/docker/aws_ecs/listener.json
```

We now need another component before we can create the service: a security group allowing inbound traffic to port 8080
and to the database port (5432 for PostgreSQL, 3306 for MySQL/MariaDB). We'll use the GUI for that as it's simpler that
way: https://$region.console.aws.amazon.com/ec2/v2/home#SecurityGroups

Finally, we can create the service with the given task definition and load balancer:

```
aws ecs create-service --service-name demo-tt --task-definition  "<task-definition-arn>" --desired-count 1 \
  --launch-type EC2 --cluster demo-tt \
  --network-configuration "awsvpcConfiguration={subnets=[SubnetId1,SubnetId2]},securityGroups=[sg-...]" \
  --load-balancers targetGroupArn=<target-group-arg>,containerName=demo-tt-webapp,containerPort=8080
```

We'll also need a security group for the load balancer, that allows traffic on port 80. We can use the GUI for that as
well.

### Updating the Service

At some point after we've got the application up and running, we'll want to update it. Let's see how we can do that. 

We'll start by creating, tagging and pushing the Docker image(s) again. We'll reuse the same repositories, unless we've
added new images; in that case, refer to the appropriate section.

We can give each image a new version number; in that case, we'll need to update our task to reference the new versions,
and then update our service to deploy the new task.

Or, we can leave the same version numbers (e.g. while doing integration tests); then, we'll update the service by just
checking the box "force new deployment"
([docs](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/update-service.html)).

Of course, we can also mix the two, and update some version numbers while leaving others as they are.

### Connecting to our instance via SSH

Prerequisites:
 - we've created the cluster with a keypair
 - we've allowed SSH access to the EC2 instance through its security group.  

Note the instance public IP on https://$region.console.aws.amazon.com/ec2/home#Instances

Then, run a command like the following:
```
ssh -i keypair.pem ec2-user@x.y.z.k
```

### Split API and Webapp Over Different Containers

This can be done in several ways, here we propose one: a single task with all the containers and a single load balancer.

We'll want to create 2 targets groups, one for the frontend and one for the backend. The frontend will be the default
one, as in the previous guide. We'll then add another rule to the same listener with the path `/api/*` forwarding to
the target group for the backend. We can do that with the CLI or with the GUI.

Make sure that the API target group has an appropriate health check set, or the task will be eventually restarted again
and again. By default the health check targets `/`, it might be necessary to change it to, for example, `/api`. 
