# Portofino Amazon AWS S3 Module

This module allow to save **Blobs** files in a S3 bucket.

## Configuration

Add this module as maven dependency.

Add in your `portofino.properties` or `portofino-local.properties` the following properties:

```
blobmanager.type=s3
aws.region=eu-west-1
aws.s3.bucket=test-portofino-s3-noencrypt-bucket
#aws.s3.location=optional
```

Add your credentials following this guide https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/setup-credentials.html
