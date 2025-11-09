#!/bin/bash
set -e

# InvoiceMe Backend Deployment Script
# Deploys backend to AWS ECS Fargate

REGION=${AWS_REGION:-us-east-1}
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
ECR_REPO="invoiceme-backend"
CLUSTER_NAME=${CLUSTER_NAME:-invoiceme-cluster}
SERVICE_NAME="invoiceme-backend-service"
IMAGE_TAG=${IMAGE_TAG:-latest}

echo "🚀 Starting InvoiceMe Backend Deployment"
echo "Region: $REGION"
echo "Account ID: $ACCOUNT_ID"
echo "ECR Repo: $ECR_REPO"
echo "Cluster: $CLUSTER_NAME"
echo "Service: $SERVICE_NAME"
echo "Image Tag: $IMAGE_TAG"

# Step 1: Build Docker image
echo ""
echo "📦 Building Docker image..."
cd "$(dirname "$0")/../backend"
docker build -t $ECR_REPO:$IMAGE_TAG -f Dockerfile .

# Step 2: Login to ECR
echo ""
echo "🔐 Logging into ECR..."
aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com

# Step 3: Create ECR repository if it doesn't exist
echo ""
echo "📋 Checking ECR repository..."
if ! aws ecr describe-repositories --repository-names $ECR_REPO --region $REGION &>/dev/null; then
    echo "Creating ECR repository: $ECR_REPO"
    aws ecr create-repository --repository-name $ECR_REPO --region $REGION
fi

# Step 4: Tag and push image
echo ""
echo "🏷️  Tagging and pushing image..."
docker tag $ECR_REPO:$IMAGE_TAG $ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/$ECR_REPO:$IMAGE_TAG
docker push $ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/$ECR_REPO:$IMAGE_TAG

# Step 5: Update ECS task definition
echo ""
echo "📝 Updating ECS task definition..."
TASK_DEF_FILE="ecs-task-definition.json"
# Replace placeholders in task definition
sed -i.bak "s/ACCOUNT_ID/$ACCOUNT_ID/g" $TASK_DEF_FILE
sed -i.bak "s/REGION/$REGION/g" $TASK_DEF_FILE
sed -i.bak "s/:latest/:$IMAGE_TAG/g" $TASK_DEF_FILE

# Register new task definition
TASK_DEF_ARN=$(aws ecs register-task-definition \
    --cli-input-json file://$TASK_DEF_FILE \
    --region $REGION \
    --query 'taskDefinition.taskDefinitionArn' \
    --output text)

echo "Task definition registered: $TASK_DEF_ARN"

# Restore original file
mv $TASK_DEF_FILE.bak $TASK_DEF_FILE

# Step 6: Update ECS service
echo ""
echo "🔄 Updating ECS service..."
SERVICE_FILE="ecs-service.json"
# Replace placeholders
sed -i.bak "s/REGION/$REGION/g" $SERVICE_FILE
sed -i.bak "s/ACCOUNT_ID/$ACCOUNT_ID/g" $SERVICE_FILE

aws ecs update-service \
    --cluster $CLUSTER_NAME \
    --service $SERVICE_NAME \
    --task-definition $TASK_DEF_ARN \
    --region $REGION \
    --force-new-deployment > /dev/null

# Restore original file
mv $SERVICE_FILE.bak $SERVICE_FILE

echo ""
echo "✅ Deployment initiated!"
echo "Service update in progress. Check ECS console for status."
echo ""
echo "To view logs:"
echo "aws logs tail /ecs/invoiceme-backend --follow --region $REGION"

