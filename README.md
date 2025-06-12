# GitOps React + Java Application

A comprehensive multi-tier application demonstrating GitOps principles with ArgoCD, featuring a React frontend, Java Spring Boot backend, deployed on AWS EKS with complete monitoring and CI/CD pipeline.

## 🏗️ Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   React App     │    │  Java Backend   │    │   Monitoring    │
│   (Frontend)    │◄──►│ (Spring Boot)   │    │  (Prometheus/   │
│                 │    │                 │    │   Grafana)      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
┌─────────────────────────────────▼─────────────────────────────────┐
│                    Kubernetes (AWS EKS)                           │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │   ArgoCD    │  │   Ingress   │  │    Helm     │              │
│  │  (GitOps)   │  │  Controller │  │   Charts    │              │
│  └─────────────┘  └─────────────┘  └─────────────┘              │
└───────────────────────────────────────────────────────────────────┘
                                 │
┌─────────────────────────────────▼─────────────────────────────────┐
│                      AWS Infrastructure                           │
│                        (Managed by Terraform)                    │
└───────────────────────────────────────────────────────────────────┘
```

## 🚀 Technologies Used

- **Frontend**: React 18, Nginx
- **Backend**: Java 11, Spring Boot 2.7
- **Infrastructure**: AWS EKS, Terraform
- **GitOps**: ArgoCD
- **Packaging**: Helm Charts
- **Monitoring**: Prometheus, Grafana
- **Networking**: Kubernetes Ingress (NGINX)
- **CI/CD**: GitHub Actions
- **Container Registry**: Docker Hub (configurable)

## 📋 Prerequisites

Before starting, ensure you have the following tools installed:

- [AWS CLI](https://aws.amazon.com/cli/) (configured with appropriate permissions)
- [kubectl](https://kubernetes.io/docs/tasks/tools/)
- [Terraform](https://terraform.io/downloads) (>= 1.0)
- [Helm](https://helm.sh/docs/intro/install/) (>= 3.0)
- [Docker](https://docs.docker.com/get-docker/)
- [Node.js](https://nodejs.org/) (>= 16)
- [Java](https://openjdk.org/) (>= 11)
- [Maven](https://maven.apache.org/install.html)
- Git

### AWS Permissions Required

Your AWS user/role needs the following permissions:
- EC2 (VPC, Security Groups, Instances)
- EKS (Cluster, Node Groups)
- IAM (Roles, Policies)
- Load Balancer (ALB/NLB)

## 🛠️ Step-by-Step Setup

### Phase 1: Infrastructure Setup with Terraform

1. **Clone and Setup Project**
   ```bash
   git clone <your-repository-url>
   cd gitops-react-java-app
   ```

2. **Configure Terraform Variables**
   ```bash
   cd terraform
   cp terraform.tfvars.example terraform.tfvars
   # Edit terraform.tfvars with your preferred settings
   ```

3. **Deploy AWS Infrastructure**
   ```bash
   terraform init
   terraform plan
   terraform apply
   ```
   
   This will create:
   - VPC with public/private subnets
   - EKS cluster with managed node groups
   - Security groups and IAM roles

4. **Configure kubectl**
   ```bash
   aws eks update-kubeconfig --region us-west-2 --name gitops-cluster
   kubectl get nodes
   ```

### Phase 2: Install ArgoCD

1. **Deploy ArgoCD**
   ```bash
   kubectl create namespace argocd
   kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
   ```

2. **Access ArgoCD UI**
   ```bash
   # Expose ArgoCD server
   kubectl patch svc argocd-server -n argocd -p '{"spec": {"type": "LoadBalancer"}}'
   
   # Get admin password
   kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d
   
   # Get ArgoCD URL
   kubectl get svc argocd-server -n argocd
   ```

### Phase 3: Build and Containerize Applications

1. **Build React Frontend**
   ```bash
   cd apps/frontend
   npm install
   npm run build
   docker build -t your-registry/frontend:latest .
   ```

2. **Build Java Backend**
   ```bash
   cd apps/backend
   mvn clean package
   docker build -t your-registry/backend:latest .
   ```

3. **Push Images to Registry**
   ```bash
   docker push your-registry/frontend:latest
   docker push your-registry/backend:latest
   ```

### Phase 4: Deploy with Helm Charts

1. **Update Helm Values**
   ```bash
   # Update image repositories in helm-charts/frontend/values.yaml
   # Update image repositories in helm-charts/backend/values.yaml
   ```

2. **Install Applications via ArgoCD**
   ```bash
   kubectl apply -f argocd/frontend-app.yaml
   kubectl apply -f argocd/backend-app.yaml
   ```

### Phase 5: Configure Ingress

1. **Install NGINX Ingress Controller**
   ```bash
   helm upgrade --install ingress-nginx ingress-nginx \
     --repo https://kubernetes.github.io/ingress-nginx \
     --namespace ingress-nginx --create-namespace
   ```

2. **Apply Ingress Configuration**
   ```bash
   kubectl apply -f k8s/ingress/app-ingress.yaml
   ```

3. **Get Ingress URL**
   ```bash
   kubectl get ingress -A
   ```

### Phase 6: Setup Monitoring

1. **Install Prometheus and Grafana**
   ```bash
   # Add Helm repositories
   helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
   helm repo add grafana https://grafana.github.io/helm-charts
   helm repo update
   
   # Install monitoring stack
   helm install prometheus prometheus-community/kube-prometheus-stack \
     --namespace monitoring --create-namespace \
     --values monitoring/prometheus/prometheus-values.yaml
   ```

2. **Access Grafana**
   ```bash
   kubectl port-forward service/prometheus-grafana 3000:80 -n monitoring
   ```
   
   Default credentials: `admin` / `prom-operator`

3. **Import Dashboards**
   - Application Dashboard (provided in `monitoring/grafana/dashboards/`)
   - Kubernetes Cluster Dashboard
   - NGINX Ingress Dashboard

### Phase 7: Setup CI/CD Pipeline

1. **Configure GitHub Secrets**
   In your GitHub repository, add these secrets:
   - `DOCKER_USERNAME`: Your Docker registry username
   - `DOCKER_PASSWORD`: Your Docker registry password/token
   - `KUBE_CONFIG`: Your kubectl configuration (base64 encoded)

2. **GitHub Actions Workflow**
   The CI/CD pipeline (`.github/workflows/ci-cd.yaml`) will:
   - Build Docker images on every push
   - Push images to registry
   - Update Helm charts with new image tags
   - ArgoCD automatically deploys changes

## 🌐 Accessing Your Application

1. **Get Ingress URL**
   ```bash
   kubectl get svc ingress-nginx-controller -n ingress-nginx
   ```

2. **Update /etc/hosts (for local testing)**
   ```bash
   echo "<EXTERNAL-IP> gitops-app.local" | sudo tee -a /etc/hosts
   ```

3. **Access Application**
   - Frontend: `http://gitops-app.local`
   - Backend API: `http://gitops-app.local/api/hello`

## 📊 Monitoring and Observability

### Grafana Dashboards
- **Application Dashboard**: Application-specific metrics
- **Kubernetes Dashboard**: Cluster health and resource usage
- **NGINX Ingress Dashboard**: Traffic and performance metrics

### Key Metrics to Monitor
- Application response times
- Error rates
- Resource utilization (CPU/Memory)
- Pod health and availability
- Ingress traffic patterns

## 🔄 GitOps Workflow

1. **Developer pushes code** to the main branch
2. **GitHub Actions** builds and pushes new Docker images
3. **ArgoCD** detects changes in the Git repository
4. **ArgoCD** automatically syncs and deploys updates to Kubernetes
5. **Monitoring** tracks deployment success and application health

## �� Testing the Setup

1. **Test Frontend-Backend Communication**
   ```bash
   curl http://gitops-app.local/api/hello
   ```

2. **Test ArgoCD Sync**
   - Make a change to the application code
   - Push to Git
   - Watch ArgoCD automatically deploy the change

3. **Test Monitoring**
   - Generate some traffic to your application
   - Check Grafana dashboards for metrics

## 🔧 Useful Commands

### Kubernetes
```bash
# Check pod status
kubectl get pods -A

# Check ArgoCD applications
kubectl get applications -n argocd

# Port forward to services
kubectl port-forward service/prometheus-grafana 3000:80 -n monitoring
```

### ArgoCD CLI
```bash
# Login to ArgoCD
argocd login <ARGOCD_SERVER>

# List applications
argocd app list

# Sync application
argocd app sync frontend-app
```

### Terraform
```bash
# Plan infrastructure changes
terraform plan

# Apply changes
terraform apply

# Destroy infrastructure
terraform destroy
```

## 🐛 Troubleshooting

### Common Issues

1. **Pods stuck in Pending state**
   ```bash
   kubectl describe pod <pod-name>
   # Check for resource constraints or node issues
   ```

2. **ArgoCD sync issues**
   ```bash
   kubectl logs -n argocd deployment/argocd-application-controller
   ```

3. **Ingress not working**
   ```bash
   kubectl get ingress -A
   kubectl describe ingress <ingress-name>
   ```

4. **Images not pulling**
   ```bash
   kubectl describe pod <pod-name>
   # Check imagePullSecrets and registry access
   ```

### Logs
```bash
# Application logs
kubectl logs -f deployment/frontend
kubectl logs -f deployment/backend

# ArgoCD logs
kubectl logs -f -n argocd deployment/argocd-server

# Ingress controller logs
kubectl logs -f -n ingress-nginx deployment/ingress-nginx-controller
```

## �� Cleanup

To destroy all resources:

1. **Delete Kubernetes resources**
   ```bash
   kubectl delete -f argocd/
   helm uninstall prometheus -n monitoring
   helm uninstall ingress-nginx -n ingress-nginx
   ```

2. **Destroy AWS infrastructure**
   ```bash
   cd terraform
   terraform destroy
   ```

## 📚 Additional Resources

- [ArgoCD Documentation](https://argo-cd.readthedocs.io/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Helm Documentation](https://helm.sh/docs/)
- [Terraform AWS Provider](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [Prometheus Operator](https://prometheus-operator.dev/)

## �� Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For questions and support:
- Open an issue in this repository
- Check the troubleshooting section above
- Review the official documentation for each tool

---

**Note**: This is a demonstration project. For production use, consider additional security measures, backup strategies, and monitoring enhancements.
