#!/bin/bash
set -e

# InvoiceMe IAM Roles Creation Script
# Creates necessary IAM roles for ECS tasks

REGION=${AWS_REGION:-us-east-1}
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)

echo "🔐 Creating IAM Roles for InvoiceMe"
echo "Region: $REGION"
echo "Account ID: $ACCOUNT_ID"

# ECS Task Execution Role (for pulling images, writing logs)
echo ""
echo "📝 Creating ECS Task Execution Role..."
aws iam create-role \
    --role-name ecsTaskExecutionRole \
    --assume-role-policy-document '{
        "Version": "2012-10-17",
        "Statement": [{
            "Effect": "Allow",
            "Principal": {
                "Service": "ecs-tasks.amazonaws.com"
            },
            "Action": "sts:AssumeRole"
        }]
    }' 2>/dev/null || echo "Role already exists, updating policy..."

# Attach managed policy for ECS task execution
aws iam attach-role-policy \
    --role-name ecsTaskExecutionRole \
    --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy

# Allow access to Secrets Manager
aws iam put-role-policy \
    --role-name ecsTaskExecutionRole \
    --policy-name SecretsManagerAccess \
    --policy-document "{
        \"Version\": \"2012-10-17\",
        \"Statement\": [{
            \"Effect\": \"Allow\",
            \"Action\": [
                \"secretsmanager:GetSecretValue\",
                \"secretsmanager:DescribeSecret\"
            ],
            \"Resource\": [
                \"arn:aws:secretsmanager:$REGION:$ACCOUNT_ID:secret:invoiceme/*\"
            ]
        }]
    }"

# ECS Task Role (for application-level AWS access if needed)
echo ""
echo "📝 Creating ECS Task Role..."
aws iam create-role \
    --role-name ecsTaskRole \
    --assume-role-policy-document '{
        "Version": "2012-10-17",
        "Statement": [{
            "Effect": "Allow",
            "Principal": {
                "Service": "ecs-tasks.amazonaws.com"
            },
            "Action": "sts:AssumeRole"
        }]
    }' 2>/dev/null || echo "Role already exists..."

# Add any application-specific permissions here if needed
# For now, we'll just create the role

echo ""
echo "✅ IAM roles created/updated successfully!"
echo ""
echo "Role ARNs:"
echo "  Execution Role: arn:aws:iam::$ACCOUNT_ID:role/ecsTaskExecutionRole"
echo "  Task Role: arn:aws:iam::$ACCOUNT_ID:role/ecsTaskRole"
echo ""
echo "Update these ARNs in your ECS task definition JSON files."



