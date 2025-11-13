# InvoiceMe Deployment Scripts

This directory contains scripts for deploying InvoiceMe to AWS.

## Scripts Overview

### `setup-aws-infrastructure.sh`
Creates all AWS infrastructure using CloudFormation:
- VPC, subnets, internet gateway
- RDS PostgreSQL database
- ECS cluster
- Application Load Balancer
- Security groups
- S3 bucket for frontend
- Secrets Manager entries

**Usage:**
```bash
export DB_PASSWORD='your-secure-password'
export DB_USERNAME='invoiceme_admin'
export GOOGLE_CLIENT_ID='your-client-id'
export GOOGLE_CLIENT_SECRET='your-client-secret'
export ENVIRONMENT=prod
./setup-aws-infrastructure.sh
```

### `create-iam-roles.sh`
Creates IAM roles required for ECS tasks:
- `ecsTaskExecutionRole` - For pulling images and writing logs
- `ecsTaskRole` - For application-level AWS access

**Usage:**
```bash
./create-iam-roles.sh
```

### `deploy-backend.sh`
Builds and deploys backend to ECS:
1. Builds Docker image
2. Pushes to ECR
3. Updates ECS task definition
4. Deploys to ECS service

**Usage:**
```bash
export AWS_REGION=us-east-1
export ENVIRONMENT=prod
export IMAGE_TAG=latest  # or specific version
./deploy-backend.sh
```

### `deploy-frontend.sh`
Builds and deploys frontend to S3 + CloudFront:
1. Builds frontend with production API URL
2. Uploads to S3
3. Invalidates CloudFront cache

**Usage:**
```bash
export AWS_REGION=us-east-1
export ENVIRONMENT=prod
export BACKEND_API_URL=http://your-alb-dns/api
export CLOUDFRONT_DIST_ID=your-distribution-id
./deploy-frontend.sh
```

## Quick Start

1. **Set up infrastructure:**
   ```bash
   ./setup-aws-infrastructure.sh
   ```

2. **Create IAM roles:**
   ```bash
   ./create-iam-roles.sh
   ```

3. **Update ECS config files** with values from CloudFormation outputs

4. **Deploy backend:**
   ```bash
   ./deploy-backend.sh
   ```

5. **Deploy frontend:**
   ```bash
   ./deploy-frontend.sh
   ```

## Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `AWS_REGION` | AWS region for deployment | No (default: us-east-1) |
| `ENVIRONMENT` | Environment name (dev/staging/prod) | No (default: prod) |
| `DB_PASSWORD` | PostgreSQL database password | Yes (for infrastructure) |
| `DB_USERNAME` | PostgreSQL database username | Yes (for infrastructure) |
| `GOOGLE_CLIENT_ID` | Google OAuth Client ID | Yes (for infrastructure) |
| `GOOGLE_CLIENT_SECRET` | Google OAuth Client Secret | Yes (for infrastructure) |
| `BACKEND_API_URL` | Backend API URL for frontend | Yes (for frontend) |
| `CLOUDFRONT_DIST_ID` | CloudFront distribution ID | No (for cache invalidation) |
| `IMAGE_TAG` | Docker image tag | No (default: latest) |

## Prerequisites

- AWS CLI configured with appropriate credentials
- Docker installed and running
- Node.js and npm installed
- Appropriate AWS permissions

See `../DEPLOYMENT.md` for detailed deployment instructions.



