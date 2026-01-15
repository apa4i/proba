

# Tradu-Aeron Developer Onboarding Guide

Welcome to the Tradu-Aeron trading system! This guide will help you get up to speed with our institutional-grade, ultra-low-latency trading platform.

## Table of Contents

1. [Welcome](#welcome)
2. [What We're Building](#what-were-building)
3. [System Architecture Overview](#system-architecture-overview)
4. [Setting Up Your Development Environment](#setting-up-your-development-environment)
5. [Core Concepts You Must Understand](#core-concepts-you-must-understand)
6. [Common Pitfalls to Avoid](#common-pitfalls-to-avoid)
7. [Learning Resources](#learning-resources)
8. [Welcome Aboard!](#welcome-aboard)
9. [Appendix: Quick Reference](#appendix-quick-reference)

---

## Welcome

Welcome to the team! You're joining a high-performance trading system where **bugs cost real money**. This means we have exceptionally high standards for code quality, testing, and production readiness.

### Mission Statement
Build an institutional-grade trading platform that delivers:
- **Sub-millisecond latency** (10-20μs for co-located clients)
- **Zero data loss** with Raft consensus
- **100% business continuity** (<10 second recovery)
- **Regulatory compliance** (MiFID II, complete audit trail)

## What We're Building

### System Overview
Tradu-Aeron is a distributed trading platform supporting multiple asset classes (FOREX, Crypto, Equities, Indices, Commodities) with:

- **Throughput**: 300-500K orders/second (scalable to 1M+)
- **Latency**: 35-150μs WebSocket, 10-20μs FIX co-located
- **Availability**: 3-node minimum, 7-node production cluster
- **Recovery**: <10 seconds for complete state restoration
---

## System Architecture Overview

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│  CLIENTS (Web UI, Mobile, FIX Terminals)                │
└───────────────────────────┬─────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────┐
│  GATEWAYS (Command/Query/WebSocket/FIX/Charts)          │
│  • Stateless REST APIs (Command/Query)                  │
│  • Stateful WebSocket (Real-time streaming)             │
│  • FIX 4.4 Gateway (Institutional clients)              │
│  • Auth0 JWT Authentication                             │
└───────────────────────────┬─────────────────────────────┘
                            │ Direct Business Messages
┌───────────────────────────▼─────────────────────────────┐
│  RELAY SERVICE (Gateway ↔ Cluster Bridge)               │
│  • Bidirectional relay without envelope wrappers        │
│  • Simplified sequencer (50% perf improvement)          │
└───────────────────────────┬─────────────────────────────┘
                            │ cluster.offer()
┌───────────────────────────▼─────────────────────────────┐
│  AERON CLUSTER (7-node Raft consensus)                  │
│  ┌─────────────────────────────────────────────────┐   │
│  │  1. OMS - Order validation, routing, positions  │   │
│  │  2. Matching Engine - ART order books (4 shards)│   │
│  │  3. Risk Manager - Pre/post-trade validation    │   │
│  │  4. MTM - Mark-to-market, P&L calculation       │   │
│  │  5. Market Data - DEPRECATED (external now)     │   │
│  │  6-7. Identity - User/Session management        │   │
│  │  8. Instrument - Symbol definitions             │   │
│  └─────────────────────────────────────────────────┘   │
│  • Single-threaded execution (no concurrency)           │
│  • Deterministic state machine (all nodes identical)    │
│  • Snapshot every 100K messages                         │
└───────────────────────────┬─────────────────────────────┘
                            │ Domain Events
┌───────────────────────────▼─────────────────────────────┐
│  EXTERNAL SERVICES                                       │
│  • LP Connectors (1 per LP: Integral, Hotspot, etc.)   │
│  • Price Aggregator (3 Active-Active replicas)         │
│  • Persistence Service (async DB writes)               │
└───────────────────────────┬─────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────┐
│  DATA LAYER (Materialized Views - NOT in critical path) │
│  • PostgreSQL (operational queries)                     │
│  • TimescaleDB (time-series historical data)            │
└─────────────────────────────────────────────────────────┘
```


## Setting Up Your Development Environment

### Prerequisites

#### Required Software
- **Java 21** (OpenJDK or Oracle)
- **Maven 3.8+**
- **Docker Desktop** (for PostgreSQL, TimescaleDB)
- **Git**
- **IDE**: IntelliJ IDEA (recommended) or Eclipse

#### Recommended Tools
- **Postman** or **cURL** for API testing
- **WebSocket client** (e.g., websocat, Postman)
- **FIX client** (e.g., QuickFIX Banzai) for FIX testing
- **VisualVM** or **JProfiler** for performance profiling
- **kubectl** for Kubernetes cluster management
- **k9s** for interactive Kubernetes cluster management

### AWS Credentials Configuration

#### Installing AWS CLI

**MacOS:**
```bash
# Using Homebrew
brew install awscli

# Verify installation
aws --version
```

**Linux:**
```bash
# Using package manager (Ubuntu/Debian)
sudo apt-get update
sudo apt-get install awscli -y

# Or using pip
pip3 install awscli --upgrade --user

# Verify installation
aws --version
```

**Windows:**
```powershell
# Download and run the AWS CLI MSI installer from:
# https://awscli.amazonaws.com/AWSCLIV2.msi

# Or using Chocolatey
choco install awscli

# Verify installation (in new terminal)
aws --version
```

#### Configuring AWS Credentials

##### Option 1: Using AWS CLI Configure (Recommended for Development)
```bash
# Configure default profile
aws configure

# You'll be prompted for:
# - AWS Access Key ID: [Your access key]
# - AWS Secret Access Key: [Your secret key]
# - Default region name: us-east-1
# - Default output format: json
```

##### Option 2: Manual Configuration
**MacOS/Linux:**
```bash
# Create AWS credentials directory
mkdir -p ~/.aws

# Create credentials file
cat > ~/.aws/credentials << EOF
[default]
aws_access_key_id = YOUR_ACCESS_KEY_ID
aws_secret_access_key = YOUR_SECRET_ACCESS_KEY

[profile-1]
aws_access_key_id = YOUR_PROFILE_1_ACCESS_KEY_ID
aws_secret_access_key = YOUR_PROFILE_1_SECRET_ACCESS_KEY

[profile-2]
aws_access_key_id = YOUR_PROFILE_2_ACCESS_KEY_ID
aws_secret_access_key = YOUR_PROFILE_2_SECRET_ACCESS_KEY
EOF

# Create config file
cat > ~/.aws/config << EOF
[default]
region = <your-default-region>
output = json

[profile profile-1]
region = <profile-1-region>
output = json

[profile profile-2]
region = <profile-2-region>
output = json
EOF

# Set correct permissions
chmod 600 ~/.aws/credentials
chmod 600 ~/.aws/config
```

**Windows (PowerShell):**
```powershell
# Create AWS credentials directory
New-Item -ItemType Directory -Force -Path $env:USERPROFILE\.aws

# Create credentials file
@"
[default]
aws_access_key_id = YOUR_ACCESS_KEY_ID
aws_secret_access_key = YOUR_SECRET_ACCESS_KEY

[profile-1]
aws_access_key_id = YOUR_PROFILE_1_ACCESS_KEY_ID
aws_secret_access_key = YOUR_PROFILE_1_SECRET_ACCESS_KEY

[profile-2]
aws_access_key_id = YOUR_PROFILE_2_ACCESS_KEY_ID
aws_secret_access_key = YOUR_PROFILE_2_SECRET_ACCESS_KEY
"@ | Out-File -FilePath $env:USERPROFILE\.aws\credentials -Encoding ASCII

# Create config file
@"
[default]
region = <your-default-region>
output = json

[profile profile-1]
region = <profile-1-region>
output = json

[profile profile-2]
region = <profile-2-region>
output = json
"@ | Out-File -FilePath $env:USERPROFILE\.aws\config -Encoding ASCII
```

##### Option 3: Using Environment Variables
**MacOS/Linux:**
```bash
# Add to ~/.bashrc, ~/.zshrc, or ~/.profile
export AWS_ACCESS_KEY_ID="YOUR_ACCESS_KEY_ID"
export AWS_SECRET_ACCESS_KEY="YOUR_SECRET_ACCESS_KEY"
export AWS_DEFAULT_REGION="<your-region>"

# For specific profile
export AWS_PROFILE="<profile-name>"

# Reload shell configuration
source ~/.bashrc  # or ~/.zshrc
```

**Windows (PowerShell):**
```powershell
# Set for current session
$env:AWS_ACCESS_KEY_ID="YOUR_ACCESS_KEY_ID"
$env:AWS_SECRET_ACCESS_KEY="YOUR_SECRET_ACCESS_KEY"
$env:AWS_DEFAULT_REGION="<your-region>"

# Set permanently (requires admin)
[System.Environment]::SetEnvironmentVariable("AWS_ACCESS_KEY_ID", "YOUR_ACCESS_KEY_ID", "User")
[System.Environment]::SetEnvironmentVariable("AWS_SECRET_ACCESS_KEY", "YOUR_SECRET_ACCESS_KEY", "User")
[System.Environment]::SetEnvironmentVariable("AWS_DEFAULT_REGION", "<your-region>", "User")
```

#### Verify AWS Configuration
```bash
# Test AWS credentials
aws sts get-caller-identity

# Expected output:
# {
#     "UserId": "AIDACKCEVSQ6C2EXAMPLE",
#     "Account": "<your-account-id>",
#     "Arn": "arn:aws:iam::<your-account-id>:user/<your-username>"
# }

# List available profiles
aws configure list-profiles

# Use specific profile
aws s3 ls --profile <profile-name>
```

#### AWS Profile Best Practices
- **default**: Default profile for general use
- **profile-1**, **profile-2**: Additional profiles for different AWS accounts/roles
- Use descriptive profile names that indicate their purpose
- Configure appropriate access permissions for each profile

**⚠️ SECURITY WARNING:**
- NEVER commit AWS credentials to Git
- NEVER share credentials via Slack/email
- Use temporary credentials (STS assume-role) when appropriate
- Rotate credentials regularly (every 90 days recommended)
- Enable MFA for sensitive accounts
- Follow the principle of least privilege for all profiles

### kubectl Installation & Configuration

#### Installing kubectl

**MacOS:**
```bash
# Using Homebrew
brew install kubectl

# Or download specific version
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/darwin/amd64/kubectl"
chmod +x kubectl
sudo mv kubectl /usr/local/bin/

# Verify installation
kubectl version --client
```

**Linux:**
```bash
# Download latest stable version
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"

# Validate binary (optional but recommended)
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl.sha256"
echo "$(cat kubectl.sha256)  kubectl" | sha256sum --check

# Install kubectl
chmod +x kubectl
sudo mv kubectl /usr/local/bin/

# Verify installation
kubectl version --client
```

**Windows (PowerShell):**
```powershell
# Using Chocolatey
choco install kubernetes-cli

# Or download manually
curl.exe -LO "https://dl.k8s.io/release/v1.29.0/bin/windows/amd64/kubectl.exe"

# Add to PATH
$env:Path += ";C:\path\to\kubectl"

# Verify installation
kubectl version --client
```

#### Configuring kubectl for EKS Clusters

##### List Available EKS Clusters
```bash
# List all EKS clusters in your AWS account
aws eks list-clusters --region <aws-region>

# Example output:
# {
#     "clusters": [
#         "cluster-1",
#         "cluster-2",
#         "cluster-3"
#     ]
# }

# If using a specific profile
aws eks list-clusters --region <aws-region> --profile <profile-name>
```

##### Update kubeconfig for EKS
```bash
# Configure kubectl for a specific cluster from the list above
aws eks update-kubeconfig --name <cluster-name> --region <aws-region>

# If using a specific AWS profile
aws eks update-kubeconfig --name <cluster-name> --region <aws-region> --profile <profile-name>

# Example:
# aws eks update-kubeconfig --name cluster-1 --region us-east-1
```

##### Verify Cluster Access
```bash
# List available kubectl contexts
kubectl config get-contexts

# Switch to the desired context
kubectl config use-context <context-name>

# Verify connection
kubectl cluster-info
kubectl get nodes
kubectl get namespaces
```

#### kubectl Configuration

##### Set Default Namespace
```bash
# Set default namespace for current context
kubectl config set-context --current --namespace=tradu-trading

# Verify
kubectl config view --minify | grep namespace
```

##### Create kubectl Aliases (Optional but Recommended)
**MacOS/Linux (add to ~/.bashrc or ~/.zshrc):**
```bash
# kubectl aliases
alias k='kubectl'
alias kgp='kubectl get pods'
alias kgs='kubectl get services'
alias kgd='kubectl get deployments'
alias kl='kubectl logs'
alias kd='kubectl describe'
alias ke='kubectl exec -it'

# Context switching aliases (replace with your actual context names from 'kubectl config get-contexts')
alias kctx1='kubectl config use-context <context-name-1>'
alias kctx2='kubectl config use-context <context-name-2>'
```

**Windows (PowerShell profile):**
```powershell
# Edit PowerShell profile
notepad $PROFILE

# Add aliases
Set-Alias -Name k -Value kubectl
function kgp { kubectl get pods $args }
function kgs { kubectl get services $args }
function kgd { kubectl get deployments $args }
function kl { kubectl logs $args }
function kd { kubectl describe $args }
```

##### Enable kubectl Autocompletion
**MacOS/Linux (Bash):**
```bash
# Add to ~/.bashrc
source <(kubectl completion bash)

# If using alias 'k'
complete -F __start_kubectl k
```

**MacOS/Linux (Zsh):**
```bash
# Add to ~/.zshrc
source <(kubectl completion zsh)

# If using alias 'k'
complete -F __start_kubectl k
```

**Windows (PowerShell):**
```powershell
# PowerShell doesn't support kubectl autocompletion natively
# Consider using k9s for better interactive experience
```

#### Common kubectl Commands
```bash
# View cluster info
kubectl cluster-info
kubectl get nodes
kubectl top nodes

# View resources
kubectl get pods -n tradu-trading
kubectl get services -n tradu-trading
kubectl get deployments -n tradu-trading

# Describe resources (detailed info)
kubectl describe pod tradu-oms-5d8f7b9c8-x7k2m -n tradu-trading
kubectl describe service tradu-command-gateway -n tradu-trading

# View logs
kubectl logs tradu-oms-5d8f7b9c8-x7k2m -n tradu-trading
kubectl logs -f tradu-oms-5d8f7b9c8-x7k2m -n tradu-trading  # Follow logs
kubectl logs tradu-oms-5d8f7b9c8-x7k2m -n tradu-trading --tail=100  # Last 100 lines

# Execute commands in pod
kubectl exec -it tradu-oms-5d8f7b9c8-x7k2m -n tradu-trading -- /bin/bash

# Port forwarding (access pod locally)
kubectl port-forward tradu-command-gateway-7b8c9d-x5k3m 8080:8080 -n tradu-trading

# Scale deployment
kubectl scale deployment tradu-matching-engine --replicas=5 -n tradu-trading

# Restart deployment (rolling restart)
kubectl rollout restart deployment tradu-oms -n tradu-trading

# View rollout status
kubectl rollout status deployment tradu-oms -n tradu-trading

# View rollout history
kubectl rollout history deployment tradu-oms -n tradu-trading
```

### k9s Installation & Configuration

**k9s** is an interactive terminal UI for Kubernetes that makes cluster management significantly easier.

#### Installing k9s

**MacOS:**
```bash
# Using Homebrew
brew install derailed/k9s/k9s

# Verify installation
k9s version
```

**Linux:**
```bash
# Download latest release
curl -sS https://webinstall.dev/k9s | bash

# Or download specific version manually
wget https://github.com/derailed/k9s/releases/download/v0.32.0/k9s_Linux_amd64.tar.gz
tar -xzf k9s_Linux_amd64.tar.gz
sudo mv k9s /usr/local/bin/

# Verify installation
k9s version
```

**Windows:**
```powershell
# Using Chocolatey
choco install k9s

# Using Scoop
scoop install k9s

# Verify installation
k9s version
```

#### Using k9s

##### Basic Usage
```bash
# Launch k9s (uses current kubectl context)
k9s

# Launch with specific namespace
k9s -n tradu-trading

# Launch with read-only mode (safe for production)
k9s --readonly

# Launch with specific context
k9s --context <context-name>
```

##### k9s Keyboard Shortcuts
| Key | Action |
|-----|--------|
| `:pod` | View pods |
| `:svc` | View services |
| `:deploy` | View deployments |
| `:ns` | View/switch namespaces |
| `:ctx` | Switch context |
| `/` | Filter resources |
| `l` | View logs |
| `d` | Describe resource |
| `e` | Edit resource |
| `s` | Shell into pod |
| `y` | View YAML |
| `ctrl-d` | Delete resource |
| `?` | Help |
| `:q` or `ctrl-c` | Quit |

##### k9s Navigation
1. **Start k9s**: `k9s`
2. **Switch to pods**: Type `:pod` and press Enter
3. **Filter pods**: Press `/` and type filter (e.g., `oms`)
4. **View logs**: Navigate to pod with arrow keys, press `l`
5. **Shell into pod**: Navigate to pod, press `s`
6. **Switch namespace**: Type `:ns` and select namespace


##### k9s Best Practices
- **Use read-only mode for sensitive environments**: `k9s --readonly --context <context-name>`
- **Filter aggressively**: Use `/` to filter resources (e.g., `/oms` to see only OMS pods)
- **Tail logs with context**: Press `l` on a pod, then use `0`-`9` to adjust log lines
- **Use pulse view**: Press `:pulse` to see cluster health overview
- **Bookmark contexts**: Press `:ctx` to quickly switch between different clusters

### Terraform Installation & Configuration

**Terraform** is an Infrastructure as Code (IaC) tool used for provisioning and managing cloud infrastructure for the Tradu-Aeron platform.

#### Installing Terraform

**MacOS:**
```bash
# Using Homebrew (Recommended)
brew tap hashicorp/tap
brew install hashicorp/tap/terraform

# Verify installation
terraform version
```

**Linux (Ubuntu/Debian):**
```bash
# Add HashiCorp GPG key
wget -O- https://apt.releases.hashicorp.com/gpg | sudo gpg --dearmor -o /usr/share/keyrings/hashicorp-archive-keyring.gpg

# Add HashiCorp repository
echo "deb [signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] https://apt.releases.hashicorp.com $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/hashicorp.list

# Update and install
sudo apt update
sudo apt install terraform

# Verify installation
terraform version
```

**Linux (RHEL/CentOS/Fedora):**
```bash
# Add HashiCorp repository
sudo yum install -y yum-utils
sudo yum-config-manager --add-repo https://rpm.releases.hashicorp.com/RHEL/hashicorp.repo

# Install Terraform
sudo yum install terraform

# Verify installation
terraform version
```

**Linux (Manual Installation):**
```bash
# Download latest version (check https://www.terraform.io/downloads for latest)
wget https://releases.hashicorp.com/terraform/1.7.0/terraform_1.7.0_linux_amd64.zip

# Unzip
unzip terraform_1.7.0_linux_amd64.zip

# Move to /usr/local/bin
sudo mv terraform /usr/local/bin/

# Verify installation
terraform version
```

**Windows (PowerShell):**
```powershell
# Using Chocolatey (Recommended)
choco install terraform

# Or using Scoop
scoop install terraform

# Verify installation
terraform version
```

**Windows (Manual Installation):**
1. Download Terraform from https://www.terraform.io/downloads
2. Extract the `.zip` file
3. Move `terraform.exe` to a directory in your PATH (e.g., `C:\Program Files\Terraform\`)
4. Open a new PowerShell window and verify:
   ```powershell
   terraform version
   ```

#### Terraform Configuration

##### Enable Tab Completion (Optional but Recommended)

**MacOS/Linux (Bash):**
```bash
# Add to ~/.bashrc
terraform -install-autocomplete

# Reload shell configuration
source ~/.bashrc
```

**MacOS/Linux (Zsh):**
```bash
# Add to ~/.zshrc
terraform -install-autocomplete

# Reload shell configuration
source ~/.zshrc
```

**Windows (PowerShell):**
```powershell
# PowerShell doesn't support native Terraform autocomplete
# Consider using Terraform Language Server with VSCode instead
```

##### Basic Terraform Commands

```bash
# Initialize Terraform working directory (downloads providers)
terraform init

# Validate configuration files
terraform validate

# Format configuration files to canonical style
terraform fmt

# Preview infrastructure changes
terraform plan

# Apply infrastructure changes
terraform apply

# Destroy infrastructure
terraform destroy

# Show current state
terraform show

# List resources in state
terraform state list

# Workspace management
terraform workspace list
terraform workspace new <workspace-name>
terraform workspace select <workspace-name>
```

#### Terraform Best Practices for Tradu-Aeron

##### Project Structure
```
terraform/
├── environments/
│   ├── dev/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── terraform.tfvars
│   ├── staging/
│   │   └── ...
│   └── production/
│       └── ...
├── modules/
│   ├── eks-cluster/
│   ├── rds/
│   └── vpc/
└── backend.tf          # Remote state configuration
```

##### Remote State Configuration
Always use remote state for team collaboration:

```hcl
# backend.tf
terraform {
  backend "s3" {
    bucket         = "tradu-terraform-state"
    key            = "env/<environment>/terraform.tfstate"
    region         = "us-east-1"
    encrypt        = true
    dynamodb_table = "terraform-state-lock"
  }
}
```

##### AWS Credentials for Terraform
Terraform uses AWS credentials configured via AWS CLI (see [AWS Credentials Configuration](#aws-credentials-configuration)):

```bash
# Terraform will automatically use AWS credentials from:
# 1. Environment variables (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)
# 2. ~/.aws/credentials file
# 3. IAM role (if running on EC2)

# Use specific AWS profile
export AWS_PROFILE=<profile-name>
terraform plan

# Or specify in provider configuration
provider "aws" {
  region  = "us-east-1"
  profile = "<profile-name>"
}
```

##### Workspace Management
Use workspaces to manage multiple environments:

```bash
# Create workspace for each environment
terraform workspace new dev
terraform workspace new staging
terraform workspace new production

# Switch between environments
terraform workspace select dev
terraform plan

terraform workspace select production
terraform plan
```

##### Security Best Practices
- **Never commit secrets to Git** - Use AWS Secrets Manager or Parameter Store
- **Use variable files** - Keep sensitive values in `.tfvars` files (add to `.gitignore`)
- **Enable state encryption** - Always use `encrypt = true` in backend configuration
- **Use state locking** - Prevent concurrent modifications with DynamoDB
- **Review plans carefully** - Always run `terraform plan` before `apply`
- **Use least privilege IAM** - Grant minimal permissions needed for Terraform operations

#### Verify Terraform Installation

```bash
# Check Terraform version
terraform version

# Should output something like:
# Terraform v1.7.0
# on darwin_amd64

# Initialize a test directory
mkdir terraform-test
cd terraform-test

# Create a simple test configuration
cat > main.tf << 'EOF'
terraform {
  required_version = ">= 1.0"
}

output "hello_world" {
  value = "Terraform is working!"
}
EOF

# Initialize and apply
terraform init
terraform apply -auto-approve

# Expected output:
# Apply complete! Resources: 0 added, 0 changed, 0 destroyed.
# Outputs:
# hello_world = "Terraform is working!"

# Clean up
cd ..
rm -rf terraform-test
```

#### Troubleshooting

| Issue | Solution |
|-------|----------|
| **"command not found: terraform"** | Ensure Terraform is in your PATH. Reinstall or manually add to PATH. |
| **AWS credentials not found** | Configure AWS CLI: `aws configure` |
| **State locking errors** | Check DynamoDB table exists and IAM permissions allow access |
| **Provider download fails** | Run `terraform init -upgrade` to force provider re-download |
| **Version conflicts** | Check `required_version` in Terraform configuration matches installed version |
| **Backend initialization fails** | Verify S3 bucket exists and you have access permissions |

#### Additional Resources

- [Official Terraform Documentation](https://www.terraform.io/docs)
- [Terraform AWS Provider](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [Terraform Best Practices](https://www.terraform-best-practices.com/)
- [HashiCorp Learn - Terraform Tutorials](https://learn.hashicorp.com/terraform)

### Claude Code Setup

**Claude Code** is an AI-powered development assistant that helps with code generation, debugging, and architectural guidance for the Tradu-Aeron trading system.

#### Software Requirements

| Requirement | Description |
|-------------|-------------|
| **AWS Account** | Active account with Bedrock access enabled |
| **Claude Model Access** | Access to Claude Opus 4 and Claude Sonnet 4.5 enabled in Bedrock |
| **Node.js** | Required to install Claude Code via npm |
| **AWS CLI** | For credential configuration (see [AWS Credentials Configuration](#aws-credentials-configuration)) |

#### Step-by-Step Setup Guide

##### Step 1: Install Claude Code

**MacOS/Linux:**
```bash
# Install globally via npm
npm install -g @anthropic-ai/claude-code

# Verify installation
claude --version
```

**Windows (PowerShell):**
```powershell
# Install globally via npm
npm install -g @anthropic-ai/claude-code

# Verify installation
claude --version
```

##### Step 2: Enable Claude Models in AWS Bedrock

1. **Log into the AWS Bedrock Console**
   ```
   https://console.aws.amazon.com/bedrock/home
   ```

2. **Navigate to Model access** in the left sidebar

3. **Request access to Claude models:**
   - Claude Opus 4
   - Claude Sonnet 4.5

4. **First-time users:**
   - Select **Chat/Text playground**
   - Choose an Anthropic model
   - Complete the use case form

5. **Wait for approval** (usually instant, may take a few minutes)

##### Step 3: Configure AWS Credentials

If you haven't already configured AWS CLI (see [AWS Credentials Configuration](#aws-credentials-configuration)), run:

```bash
aws configure

# You'll be prompted for:
# - AWS Access Key ID
# - AWS Secret Access Key
# - Default region name (e.g., us-east-1)
# - Default output format (e.g., json)
```

##### Step 4: Configure Claude Code for Bedrock

Set the required environment variables to persist across all terminals and system restarts:

**MacOS/Linux:**

First, determine your shell:
```bash
echo $SHELL
# Output: /bin/bash or /bin/zsh
```

For **Bash** (if output is `/bin/bash`):
```bash
# Add to ~/.bashrc (for all new terminal sessions)
cat >> ~/.bashrc << 'EOF'

# Claude Code Bedrock Configuration
export CLAUDE_CODE_USE_BEDROCK=1
export AWS_REGION=us-east-1
EOF

# Apply to current session
source ~/.bashrc
```

For **Zsh** (if output is `/bin/zsh` - default on modern macOS):
```bash
# Add to ~/.zshrc (for all new terminal sessions)
cat >> ~/.zshrc << 'EOF'

# Claude Code Bedrock Configuration
export CLAUDE_CODE_USE_BEDROCK=1
export AWS_REGION=us-east-1
EOF

# Apply to current session
source ~/.zshrc
```

**Verify persistence:**
```bash
# Close terminal and open a new one, then check:
echo $CLAUDE_CODE_USE_BEDROCK
# Should output: 1

echo $AWS_REGION
# Should output: us-east-1
```

**Windows (PowerShell):**

Set as **User Environment Variables** (persists across all sessions and reboots):

```powershell
# Set permanently for all PowerShell sessions and system restarts
[System.Environment]::SetEnvironmentVariable("CLAUDE_CODE_USE_BEDROCK", "1", "User")
[System.Environment]::SetEnvironmentVariable("AWS_REGION", "us-east-1", "User")

# Reload environment variables in current session
$env:CLAUDE_CODE_USE_BEDROCK = [System.Environment]::GetEnvironmentVariable("CLAUDE_CODE_USE_BEDROCK", "User")
$env:AWS_REGION = [System.Environment]::GetEnvironmentVariable("AWS_REGION", "User")
```

**Verify persistence:**
```powershell
# Close PowerShell and open a new one, then check:
$env:CLAUDE_CODE_USE_BEDROCK
# Should output: 1

$env:AWS_REGION
# Should output: us-east-1

# Or check system environment variables
[System.Environment]::GetEnvironmentVariable("CLAUDE_CODE_USE_BEDROCK", "User")
[System.Environment]::GetEnvironmentVariable("AWS_REGION", "User")
```

##### Step 5: Configure Models (Opus 4 + Sonnet 4.5)

**MacOS/Linux:**

For **Bash**:
```bash
# Add to ~/.bashrc
cat >> ~/.bashrc << 'EOF'

# Claude Models Configuration
export ANTHROPIC_MODEL='us.anthropic.claude-opus-4-20250514-v1:0'
export ANTHROPIC_SMALL_FAST_MODEL='global.anthropic.claude-sonnet-4-5-20250929-v1:0'
EOF

# Apply to current session
source ~/.bashrc
```

For **Zsh**:
```bash
# Add to ~/.zshrc
cat >> ~/.zshrc << 'EOF'

# Claude Models Configuration
export ANTHROPIC_MODEL='us.anthropic.claude-opus-4-20250514-v1:0'
export ANTHROPIC_SMALL_FAST_MODEL='global.anthropic.claude-sonnet-4-5-20250929-v1:0'
EOF

# Apply to current session
source ~/.zshrc
```

**Windows (PowerShell):**
```powershell
# Set permanently for all sessions and system restarts
[System.Environment]::SetEnvironmentVariable("ANTHROPIC_MODEL", "us.anthropic.claude-opus-4-20250514-v1:0", "User")
[System.Environment]::SetEnvironmentVariable("ANTHROPIC_SMALL_FAST_MODEL", "global.anthropic.claude-sonnet-4-5-20250929-v1:0", "User")

# Reload in current session
$env:ANTHROPIC_MODEL = [System.Environment]::GetEnvironmentVariable("ANTHROPIC_MODEL", "User")
$env:ANTHROPIC_SMALL_FAST_MODEL = [System.Environment]::GetEnvironmentVariable("ANTHROPIC_SMALL_FAST_MODEL", "User")
```

##### Step 6: Set Output Token Limits (Recommended)

**MacOS/Linux:**

For **Bash**:
```bash
# Add to ~/.bashrc
cat >> ~/.bashrc << 'EOF'

# Claude Token Limits
export CLAUDE_CODE_MAX_OUTPUT_TOKENS=4096
export MAX_THINKING_TOKENS=1024
EOF

# Apply to current session
source ~/.bashrc
```

For **Zsh**:
```bash
# Add to ~/.zshrc
cat >> ~/.zshrc << 'EOF'

# Claude Token Limits
export CLAUDE_CODE_MAX_OUTPUT_TOKENS=4096
export MAX_THINKING_TOKENS=1024
EOF

# Apply to current session
source ~/.zshrc
```

**Windows (PowerShell):**
```powershell
# Set permanently for all sessions and system restarts
[System.Environment]::SetEnvironmentVariable("CLAUDE_CODE_MAX_OUTPUT_TOKENS", "4096", "User")
[System.Environment]::SetEnvironmentVariable("MAX_THINKING_TOKENS", "1024", "User")

# Reload in current session
$env:CLAUDE_CODE_MAX_OUTPUT_TOKENS = [System.Environment]::GetEnvironmentVariable("CLAUDE_CODE_MAX_OUTPUT_TOKENS", "User")
$env:MAX_THINKING_TOKENS = [System.Environment]::GetEnvironmentVariable("MAX_THINKING_TOKENS", "User")
```

##### Step 7: Launch Claude Code

```bash
# Start Claude Code
claude

# You should see the Claude Code prompt
# Start interacting with Claude in the terminal
```

#### Verify Installation

**MacOS/Linux:**
```bash
# Open a NEW terminal window to test persistence, then check:

# Check environment variables
echo $CLAUDE_CODE_USE_BEDROCK  # Should output: 1
echo $AWS_REGION               # Should output: us-east-1
echo $ANTHROPIC_MODEL          # Should output: us.anthropic.claude-opus-4-20250514-v1:0
echo $ANTHROPIC_SMALL_FAST_MODEL  # Should output: global.anthropic.claude-sonnet-4-5-20250929-v1:0
echo $CLAUDE_CODE_MAX_OUTPUT_TOKENS  # Should output: 4096
echo $MAX_THINKING_TOKENS      # Should output: 1024

# Test Bedrock access
aws bedrock list-inference-profiles --region us-east-1

# Test Claude Code
claude
```

**Windows (PowerShell):**
```powershell
# Open a NEW PowerShell window to test persistence, then check:

# Check environment variables
$env:CLAUDE_CODE_USE_BEDROCK  # Should output: 1
$env:AWS_REGION               # Should output: us-east-1
$env:ANTHROPIC_MODEL          # Should output: us.anthropic.claude-opus-4-20250514-v1:0
$env:ANTHROPIC_SMALL_FAST_MODEL  # Should output: global.anthropic.claude-sonnet-4-5-20250929-v1:0
$env:CLAUDE_CODE_MAX_OUTPUT_TOKENS  # Should output: 4096
$env:MAX_THINKING_TOKENS      # Should output: 1024

# Test Bedrock access
aws bedrock list-inference-profiles --region us-east-1

# Test Claude Code
claude
```

**After System Restart:**

All environment variables should still be available after rebooting your system. Open a terminal and verify all variables are still set using the commands above.

#### Troubleshooting

| Issue | Solution |
|-------|----------|
| **Environment variables not persisting** | Verify you added them to the correct shell config file (`~/.bashrc` for Bash, `~/.zshrc` for Zsh). Check with `echo $SHELL`. For Windows, ensure you used `[System.Environment]::SetEnvironmentVariable` with `"User"` scope. |
| **Variables lost after system restart** | On MacOS/Linux, verify your shell profile file (`~/.bashrc` or `~/.zshrc`) is being loaded on startup. On Windows, confirm variables are set at User level, not Process level. |
| **New terminal doesn't have variables** | Run `source ~/.bashrc` (or `~/.zshrc`) to reload the profile. For Windows, close and reopen PowerShell completely. |
| **Region errors** | Run: `aws bedrock list-inference-profiles --region <your-region>` to verify available models |
| **"On-demand throughput isn't supported"** | Use inference profile IDs (as shown above), not direct model IDs |
| **Throttling (HTTP 429)** | AWS token quotas exceeded; wait a moment and retry |
| **Authentication errors** | Verify AWS credentials: `aws sts get-caller-identity` |
| **Model access denied** | Check Bedrock model access is granted in AWS Console |
| **Command not found: claude** | Reinstall: `npm install -g @anthropic-ai/claude-code` |

#### Additional Resources

- [Official Claude Code Bedrock Documentation](https://docs.anthropic.com/claude/docs/claude-code)
- [AWS Bedrock Documentation](https://docs.aws.amazon.com/bedrock/)
- [AWS Bedrock Pricing](https://aws.amazon.com/bedrock/pricing/)

### Initial Setup

#### 1. Clone the Repository
```bash
git clone https://github.com/tradu/omnius.git
cd omnius
```

#### 2. Build the Project
```bash
# Full build with tests
mvn clean install

# Skip tests for faster build
mvn clean install -DskipTests

# Build specific module
mvn clean install -pl tradu-oms -am
```

### Local Environment Setup

#### Overview

The Tradu-Aeron trading system runs as an Aeron cluster managed by the **Tradu-launcher** module. The launcher starts all cluster components (OMS, Matching Engine, Risk, MTM, Identity, Instrument) as a single unified cluster node.

#### Cluster Architecture

The `tradu-launcher` module contains the cluster configuration and dependencies for all components:
- **OMS** (Order Management Service)
- **Matching Engine** (Order execution and matching)
- **Risk Manager** (Pre/post-trade validation)
- **MTM** (Mark-to-Market, P&L calculation)
- **Identity** (User/Session management)
- **Instrument** (Symbol definitions and metadata)

#### Starting the Cluster

##### Main Class
`com.tradu.launcher.UnifiedClusterNode`

##### Working Directory
Set your working directory to:
```
<path-to-omnius>/config/development/udp
```

Replace `<path-to-omnius>` with the full path to your omnius project directory.

**Example:**
```
/Users/nntomov/tradu/workspace1/omnius/config/development/udp
```

##### Program Arguments
Add the cluster configuration file as a program argument:
```
cluster.yaml
```

**About cluster.yaml:**
This configuration file describes the concrete modules of the cluster (OMS, Matching Engine, Risk, MTM, Identity, Instrument) and their specific configurations within the cluster. It defines:
- Which services are enabled/disabled
- Service-specific configuration parameters
- Cluster topology and communication settings
- Resource allocation per service

##### JVM Parameters

Add the following JVM parameters to enable proper module access for Aeron:

```bash
--add-opens=java.base/sun.nio.ch=ALL-UNNAMED
--add-opens=java.base/java.lang.reflect=ALL-UNNAMED
--add-opens=java.base/java.nio=ALL-UNNAMED
--add-opens=java.base/java.util.concurrent=ALL-UNNAMED
--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED
--add-opens=java.base/jdk.internal.util=ALL-UNNAMED
--add-opens=java.base/java.lang=ALL-UNNAMED
--add-opens=java.base/java.util=ALL-UNNAMED
--add-opens=java.base/java.io=ALL-UNNAMED
```

#### IntelliJ IDEA Configuration

To configure the cluster startup in IntelliJ IDEA:

1. **Create a new Run Configuration:**
   - Go to `Run > Edit Configurations...`
   - Click `+` and select `Application`
   - Name: `UnifiedClusterNode`

2. **Configure the settings:**
   - **Module**: `tradu-launcher`
   - **JDK**: Java 21
   - **Main class**: `com.tradu.launcher.UnifiedClusterNode`
   - **Program arguments**: `cluster.yaml`
   - **Working directory**: `<path-to-omnius>/config/development/udp`
   - **VM options**: (paste all the `--add-opens` parameters from above)

3. **Save the configuration** and click `OK`

**Example Configuration Screenshot:**

![IntelliJ IDEA Run Configuration Example](docs/images/intellij-run-config-example.png)

The screenshot above shows a complete configuration example with:
- **Name**: UnifiedClusterNode
- **Run on**: Local machine
- **Build and run**: Java 21 SDK of 'tradu-launcher' with `-cp tradu-launcher`
- **VM options**: All required `--add-opens` parameters
- **Main class**: com.tradu.launcher.UnifiedClusterNode
- **Program arguments**: cluster.yaml
- **Working directory**: /Users/nntomov/tradu/workspace1/omnius/config/development/udp

#### Non-Cluster Services Configuration

**Important:** The `cluster.yaml` configuration is **only required for cluster services** (UnifiedClusterNode).

For **non-cluster services** like gateways, you do NOT need to add `cluster.yaml` as a program argument.

**Example: Query Gateway Configuration**

To run the Query Gateway:

1. **Create a new Run Configuration:**
   - Go to `Run > Edit Configurations...`
   - Click `+` and select `Application`
   - Name: `QueryGateway`

2. **Configure the settings:**
   - **Module**: `tradu-query-gateway`
   - **JDK**: Java 21
   - **Main class**: `com.tradu.gateway.query.QueryGatewayApplication`
   - **Program arguments**: *(leave empty or add Spring Boot arguments like `--server.port=8081`)*
   - **Working directory**: `<path-to-omnius>` (project root)
   - **VM options**: *(typically not needed for gateways)*

**Other Non-Cluster Services:**
- `tradu-command-gateway` - Command Gateway
- `tradu-websocket-gateway` - WebSocket Gateway
- `tradu-fix-gateway` - FIX Gateway
- `tradu-charts-gateway` - Charts Gateway
- `tradu-price-aggregator` - Price Aggregator
- `tradu-lp-connector` - LP Connector

All of these services run as standard Spring Boot applications and do NOT require the `cluster.yaml` configuration.

## Core Concepts You Must Understand

### 1. Aeron Cluster Fundamentals

#### What is Aeron?
Aeron is an ultra-low-latency messaging system with:
- **IPC**: 1-2μs shared memory (co-located services)
- **UDP**: 25-50μs network communication
- **Raft consensus**: Leader election, log replication, fault tolerance

#### Single-Threaded Execution Model
**CRITICAL**: Aeron Cluster operates **SINGLE-THREADED**. This means:

- ✅ **NO concurrency constructs needed** - No `synchronized`, `volatile`, `AtomicLong`, `ConcurrentHashMap`
- ✅ **NO race conditions possible** - All callbacks execute sequentially on one thread
- ✅ **Use Agrona primitive collections** - Avoid boxing overhead

```java
// ✅ CORRECT: Simple primitives and Agrona collections
private boolean enabled = true;
private long counter = 0;
private final Long2ObjectHashMap<Order> orders = new Long2ObjectHashMap<>();

// ❌ WRONG: Unnecessary concurrency primitives
private volatile boolean enabled = true;
private final AtomicLong counter = new AtomicLong(0);
private final ConcurrentHashMap<Long, Order> orders = new ConcurrentHashMap<>();
```

#### Deterministic State Machine
**ALL nodes (leader AND followers) MUST execute identical code paths.**

```java
// ✅ CORRECT: No guards before cluster.offer() - ALL nodes execute this
public class ClusteredOMSService {
    private void sendOrderBatch(OrderBatch batch) {
        cluster.offer(encodeOrderBatch(batch));  // Aeron handles routing
    }
}

// ❌ WRONG: Guards break determinism - leader and followers diverge
public class ClusteredOMSService {
    private void sendOrderBatch(OrderBatch batch) {
        if (!canSendExternal()) return;  // FORBIDDEN for cluster.offer()!
        cluster.offer(encodeOrderBatch(batch));
    }
}
```

**When guards ARE needed**: ONLY for external systems (LPs, databases, HTTP)
```java
// ✅ CORRECT: Guard for external LP communication
if (isLeader() && !isReplaying()) {
    lpConnectionManager.sendOrder(order);  // External system
}
```

### 2. Fixed-Point Arithmetic

**CRITICAL**: All monetary values use `long` primitives (NO floating-point).

#### Why?
- **Determinism**: Floating-point math is non-deterministic across CPUs
- **Performance**: 100x faster than `BigDecimal`
- **Precision**: No rounding errors (0.1 + 0.2 = 0.3 exactly)

#### Domain Objects
```java
// All monetary values use fixed-point with scale factors
public final class Price {
    private final long scaledValue;     // 1.08500 stored as 108500L
    private final AssetType assetType;  // Determines scale factor

    public static Price forex(double value) {
        return new Price((long)(value * 100_000), AssetType.FOREX);
    }
}

public final class Quantity {
    private final long scaledValue;

    public static Quantity forexLots(int lots) {
        return new Quantity(lots * 100_000L, AssetType.FOREX);
    }
}

public final class Money {
    private final long scaledValue;
    private final String currency;

    public static Money usd(double value) {
        return new Money((long)(value * 100), "USD");
    }
}
```

#### Scale Factors by Asset Type
| Asset Type | Decimals | Scale Factor | Example |
|------------|----------|--------------|---------|
| FOREX | 5 | 100,000 | 1.08500 = 108500L |
| CRYPTO | 8 | 100,000,000 | 0.12345678 = 12345678L |
| EQUITIES | 2 | 100 | 123.45 = 12345L |
| INDICES | 2 | 100 | 5432.10 = 543210L |

#### Arithmetic Operations
```java
// ✅ CORRECT: Domain objects handle scale factors
Price eurusd = Price.forex(1.08500);
Quantity lot = Quantity.forexLots(1);
Money pnl = eurusd.multiply(lot);  // Scales handled internally

// ❌ FORBIDDEN: Manual scale factor math
long pnlScaled = (priceScaled * qtyScaled) / (100_000 * 100_000);
```

### 3. SBE (Simple Binary Encoding)

#### What is SBE?
Zero-copy binary serialization for ultra-low latency messaging.

#### Key Concepts
- **Flyweight Pattern**: Decoders point directly to buffer (DO NOT copy data)
- **Fixed-size messages**: Predictable memory layout
- **No heap allocation**: All operations are stack-based

#### CRITICAL: Decoder Flyweight Trap
```java
// ❌ WRONG: Storing decoder reference - buffer will be reused!
private final AtomicReference<OrderDecoder> lastOrder = new AtomicReference<>();

void onFragment(DirectBuffer buffer, int offset, int length, Header header) {
    OrderDecoder decoder = new OrderDecoder();
    decoder.wrap(buffer, offset, blockLength, version);
    lastOrder.set(decoder);  // DANGER: buffer reused by Aeron!
}

void verifyOrder() {
    OrderDecoder order = lastOrder.get();
    long orderId = order.orderId();  // Returns garbage!
}

// ✅ CORRECT: Copy ALL data immediately
private record OrderData(long orderId, String symbol, long quantity, ...) {}
private final AtomicReference<OrderData> lastOrder = new AtomicReference<>();

void onFragment(DirectBuffer buffer, int offset, int length, Header header) {
    OrderDecoder decoder = new OrderDecoder();
    decoder.wrap(buffer, offset, blockLength, version);

    // Copy NOW before buffer is reused
    long orderId = decoder.orderId();
    String symbol = decoder.symbol();
    long quantity = decoder.quantity();

    lastOrder.set(new OrderData(orderId, symbol, quantity, ...));
}
```

#### Message Schema Example
```xml
<!-- resources/sbe/order-schema.xml -->
<sbe:message name="OrderRequest" id="1">
    <field name="orderId" id="1" type="uint64"/>
    <field name="accountId" id="2" type="uint64"/>
    <field name="symbol" id="3" type="symbol12"/>
    <field name="side" id="4" type="Side"/>
    <field name="quantity" id="5" type="uint64"/>
    <field name="price" id="6" type="uint64"/>
</sbe:message>
```

### 4. Snapshot & Recovery

#### Accumulate-Persist-Clear Pattern
```java
@Override
protected void takeSnapshot(ExclusivePublication snapshotPublication) {
    final ExpandableArrayBuffer buffer = new ExpandableArrayBuffer();
    int offset = 0;

    // 1. Version and metadata
    buffer.putInt(offset, SNAPSHOT_VERSION); offset += Integer.BYTES;
    buffer.putLong(offset, cluster.time()); offset += Long.BYTES;

    // 2. Serialize ALL state (business + deduplication + counters)
    buffer.putInt(offset, processedSequences.size()); offset += Integer.BYTES;
    for (long seq : processedSequences) {
        buffer.putLong(offset, seq); offset += Long.BYTES;
    }

    buffer.putInt(offset, orders.size()); offset += Integer.BYTES;
    orders.forEachLong((orderId, order) -> {
        // Serialize order fields...
        return true;
    });

    // 3. Offer with retry logic
    while (true) {
        long result = snapshotPublication.offer(buffer, 0, offset);
        if (result >= 0) break;
        else if (result == Publication.BACK_PRESSURED ||
                 result == Publication.ADMIN_ACTION ||
                 result == Publication.NOT_CONNECTED) {
            cluster.idleStrategy().idle();
        } else {
            throw new RuntimeException("Fatal snapshot error: " + result);
        }
    }

    // 4. Clear AFTER successful write
    int clearedSequences = processedSequences.size();
    processedSequences.clear();

    log.info("Snapshot completed - {} bytes, cleared {} sequences",
             offset, clearedSequences);
}
```

#### Memory Management
- **ACCUMULATE** state during operation (deduplication sets grow - THIS IS CORRECT)
- **PERSIST** all state to snapshot when triggered (every 100K messages)
- **CLEAR** tracking structures after successful persistence

**Memory growth between snapshots is expected and correct, not a memory leak.**

### 5. Direct Cluster Communication

#### Message Flow Pattern
```
Gateway → Relay Service → cluster.offer(ORDER_REQUEST) → OMS
OMS → cluster.offer(ORDER_BATCH) → Matching Engine
Matching Engine → cluster.offer(EXECUTION_REPORT) → Relay → Gateway
```

#### NO Envelope Wrappers
```java
// ✅ CORRECT: Direct business messages
public void onOrderRequest(DirectBuffer buffer, int offset, int length) {
    OrderRequestDecoder decoder = new OrderRequestDecoder();
    decoder.wrap(buffer, offset, blockLength, version);

    // Process order...

    // Send directly to cluster
    OrderBatchEncoder encoder = new OrderBatchEncoder();
    // ... encode batch ...
    cluster.offer(outputBuffer, 0, encodedLength);
}

// ❌ WRONG: Envelope wrappers (removed in Issue #219)
public void onOrderRequest(DirectBuffer buffer, int offset, int length) {
    GatewayCommand command = new GatewayCommand(buffer, offset, length);
    cluster.offer(command.encode());  // Unnecessary wrapper
}
```

---

## Common Pitfalls to Avoid

### 1. Guarding cluster.offer() [CRITICAL]
```java
// ❌ FORBIDDEN: Breaks deterministic state machine
if (!canSendExternal()) return;
cluster.offer(buffer, offset, length);

// ✅ CORRECT: ALL nodes execute cluster.offer()
cluster.offer(buffer, offset, length);

// ✅ CORRECT: Guards ONLY for external systems
if (isLeader() && !isReplaying()) {
    externalLP.sendOrder(order);
}
```

### 2. Storing SBE Decoder References [CRITICAL]
```java
// ❌ FORBIDDEN: Buffer will be reused!
private OrderDecoder lastOrder;

void onFragment(DirectBuffer buffer, int offset, int length, Header header) {
    lastOrder = new OrderDecoder();
    lastOrder.wrap(buffer, offset, blockLength, version);
}

// ✅ CORRECT: Copy data immediately
private OrderData lastOrder;

void onFragment(DirectBuffer buffer, int offset, int length, Header header) {
    OrderDecoder decoder = new OrderDecoder();
    decoder.wrap(buffer, offset, blockLength, version);
    lastOrder = new OrderData(decoder.orderId(), decoder.symbol(), ...);
}
```

### 3. Using Concurrent Primitives [HIGH]
```java
// ❌ FORBIDDEN: Unnecessary in single-threaded model
private volatile boolean enabled;
private final AtomicLong counter = new AtomicLong(0);
private final ConcurrentHashMap<Long, Order> orders;

// ✅ CORRECT: Simple primitives
private boolean enabled;
private long counter = 0;
private final Long2ObjectHashMap<Order> orders = new Long2ObjectHashMap<>();
```

### 4. Non-Deterministic Time [CRITICAL]
```java
// ❌ FORBIDDEN: Different during replay
long timestamp = System.currentTimeMillis();
Instant now = Instant.now();

// ✅ CORRECT: Deterministic cluster time
ClusterClock clock = new ClusterClock(cluster);
long timestamp = clock.currentTimeMillis();
Instant now = clock.instant();
```

### 5. Floating-Point Arithmetic [CRITICAL]
```java
// ❌ FORBIDDEN: Non-deterministic, rounding errors
double price = 1.0850;
double pnl = price * quantity;

// ✅ CORRECT: Fixed-point with domain objects
Price price = Price.forex(1.0850);
Quantity quantity = Quantity.forexLots(1);
Money pnl = price.multiply(quantity);
```

### 6. Blocking Operations [CRITICAL]
```java
// ❌ FORBIDDEN: Blocks cluster thread for seconds!
Future<Result> future = asyncOperation();
Result result = future.get(5, TimeUnit.SECONDS);

// ✅ CORRECT: Synchronous execution
Result result = syncOperation();
```

### 7. Manual Scale Factor Math [HIGH]
```java
// ❌ FORBIDDEN: Error-prone manual scaling
long pnlScaled = (priceScaled * qtyScaled) / (FOREX_SCALE * QUANTITY_SCALE);

// ✅ CORRECT: Domain objects handle scales
Money pnl = price.multiply(quantity);
```

### 8. Insufficient Test Coverage [HIGH]
```java
// ❌ INSUFFICIENT: Only happy path tested
@Test
void shouldCreateOrder() {
    Order order = createValidOrder();
    orderService.createOrder(order);
    verify(repository).save(order);
}

// ✅ COMPLETE: Happy path + edge cases + error cases
@Test void shouldCreateOrderWhenValidRequest() { ... }
@Test void shouldRejectOrderWhenInvalidQuantity() { ... }
@Test void shouldRejectOrderWhenInsufficientBalance() { ... }
@Test void shouldRejectOrderWhenSymbolNotFound() { ... }
@Test void shouldRejectOrderWhenAccountSuspended() { ... }
```

---

## Learning Resources

### Internal Documentation
- [CLAUDE.md](CLAUDE.md) - Comprehensive coding standards and architectural patterns
- [docs/architecture/](docs/architecture/) - Architecture Decision Records (ADRs)
- [docs/patterns/SNAPSHOT_PATTERNS.md](docs/patterns/SNAPSHOT_PATTERNS.md) - Snapshot implementation guide
- [docs/deployment/](docs/deployment/) - Deployment guides
- [docs/runbooks/](docs/runbooks/) - Operational procedures
- [Useful Links (Confluence)](https://confluence.cyolo.fxcorporate.com/display/EH/Useful+Links) - Company-wide useful links and resources

### External Resources

#### Aeron & Distributed Systems
- [Aeron Cookbook](https://aeroncookbook.com/) - Comprehensive Aeron guide
- [Aeron GitHub](https://github.com/real-logic/aeron) - Official repository with examples
- [Aeron Cluster Tutorial](https://github.com/real-logic/aeron/wiki/Cluster-Tutorial) - Cluster setup guide
- [Mechanical Sympathy Blog](https://mechanical-sympathy.blogspot.com/) - Martin Thompson (Aeron creator)

#### SBE (Simple Binary Encoding)
- [SBE GitHub](https://github.com/real-logic/simple-binary-encoding) - Official repository
- [SBE Wiki](https://github.com/real-logic/simple-binary-encoding/wiki) - Schema design guide

#### Domain-Driven Design
- "Domain-Driven Design" by Eric Evans (Blue Book)
- "Implementing Domain-Driven Design" by Vaughn Vernon (Red Book)
- [DDD Reference](https://www.domainlanguage.com/ddd/reference/) - Quick reference

#### Financial Markets
- "Trading and Exchanges" by Larry Harris - Market microstructure
- "Inside the Black Box" by Rishi K. Narang - Quantitative trading
- [Investopedia](https://www.investopedia.com/) - Financial terms and concepts

#### Performance & Low Latency
- "Java Performance" by Scott Oaks - JVM tuning
- "Systems Performance" by Brendan Gregg - System-level optimization
- [JMH Samples](https://github.com/openjdk/jmh/tree/master/jmh-samples) - Micro-benchmarking

---

## Welcome Aboard!

You're now equipped with everything you need to start contributing to Tradu-Aeron. Remember:

1. **Quality over speed** - This is a trading system where bugs cost money
2. **Ask questions** - No question is too simple when you're learning
3. **Read the code** - The codebase is your best teacher
4. **Test thoroughly** - >90% coverage is not optional
5. **Never break the build** - Run tests before pushing

We're excited to have you on the team. Let's build something amazing together!

---

## Appendix: Quick Reference

### Agrona Collections Cheat Sheet
| Instead of | Use |
|------------|-----|
| `Map<Long, V>` | `Long2ObjectHashMap<V>` |
| `Map<Integer, V>` | `Int2ObjectHashMap<V>` |
| `Map<Long, Long>` | `Long2LongHashMap` |
| `Map<K, Long>` | `Object2LongHashMap<K>` |
| `Set<Long>` | `LongHashSet` |
| `Set<Integer>` | `IntHashSet` |

### Scale Factor Quick Reference
| Asset Type | Decimals | Scale Factor | Example |
|------------|----------|--------------|---------|
| FOREX | 5 | 100,000 | 1.08500 = 108500L |
| CRYPTO | 8 | 100,000,000 | 0.12345678 = 12345678L |
| EQUITIES | 2 | 100 | 123.45 = 12345L |
| INDICES | 2 | 100 | 5432.10 = 543210L |
| COMMODITIES | 2 | 100 | 1850.25 = 185025L |

