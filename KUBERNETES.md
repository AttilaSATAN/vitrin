# Kubernetes Deployment Guide

This guide explains how to build and run the **Tusas Vitrin – Flight Tracker** application on a local [minikube](https://minikube.sigs.k8s.io/) cluster.

## Prerequisites

| Tool | Minimum version | Notes |
|------|----------------|-------|
| [Docker Desktop](https://www.docker.com/products/docker-desktop/) | 24+ | Used as the minikube driver |
| [minikube](https://minikube.sigs.k8s.io/docs/start/) | 1.35+ | Local Kubernetes cluster |
| [kubectl](https://kubernetes.io/docs/tasks/tools/) | matching cluster | Comes bundled with Docker Desktop or minikube |

---

## Architecture

```
Browser
  │
  ▼  http://localhost (port 80)
ingress-nginx (minikube tunnel)
  ├── /api/*       → tusas-backend:8080  (Spring Boot REST)
  ├── /ws-native   → tusas-backend:8080  (STOMP WebSocket)
  └── /*           → tusas-frontend:80   (Vue 3 SPA / nginx)

tusas-producer  →  POST /api/flights every 5 s  →  tusas-backend
tusas-backend   →  PostgreSQL (tusas-postgres:5432)
tusas-backend   →  STOMP broadcast → /topic/flights → browser
tusas-backend   →  Kubernetes API  (spawn producer pods on demand)
```

---

## Spawning a producer pod on demand

The backend exposes a dedicated endpoint to create a one-shot producer pod at runtime:

```
POST /api/producer/spawn
Authorization: Basic admin:password
```

**Responses:**

| Condition | HTTP status | Body |
|---|---|---|
| Pod created successfully | 201 Created | `{ "podName": "tusas-producer-xxxxx", "status": "CREATED", "message": "Producer pod created successfully" }` |
| Not running in Kubernetes | 503 Service Unavailable | `{ "podName": null, "status": "UNAVAILABLE", "message": "Application is not running inside a Kubernetes cluster" }` |

The button labelled **▶ Spawn Producer** in the bottom-left corner of the map UI calls this endpoint.

### How it works

1. The adapter checks for the `KUBERNETES_SERVICE_HOST` environment variable — automatically injected by Kubernetes into every pod. If absent the 503 response is returned immediately.
2. If present, it loads the in-cluster kubeconfig from the mounted service account token and calls `POST /api/v1/namespaces/tusas-vitrin/pods`.
3. The new pod inherits the same PostgreSQL environment variables as the backend pod.
4. The pod's `restartPolicy` is `Never` — it runs once and terminates.

---

## RBAC – pod creation permissions

The backend pod needs permission to create pods in the `tusas-vitrin` namespace.
This is handled by `k8s/backend/rbac.yaml` which is applied automatically via `kubectl apply -R -f k8s/`.

The manifest creates:
- **ServiceAccount** `tusas-backend-sa` — the identity the backend pod runs as
- **Role** `pod-creator` — grants `create` and `get` on `pods`
- **RoleBinding** `pod-creator-binding` — attaches the role to the service account

The backend `deployment.yaml` references the service account via `serviceAccountName: tusas-backend-sa`.

To inspect the applied permissions:

```powershell
kubectl get serviceaccount,role,rolebinding -n tusas-vitrin
```

---

## Step 1 – Start minikube

```powershell
minikube start
minikube addons enable ingress
```

Wait until the ingress addon is fully ready:

```powershell
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=120s
```

---

## Step 2 – Build images inside minikube

Point your shell at minikube's Docker daemon so images are immediately available to the cluster without any `image load` step:

**PowerShell:**
```powershell
minikube docker-env --shell powershell | Invoke-Expression
```

**Bash / Git Bash:**
```bash
eval $(minikube docker-env)
```

Then build all three images:

```powershell
docker build --no-cache -t tusas-frontend:latest ./frontend
docker build --no-cache -t tusas-backend:latest  ./backend
docker build --no-cache -t tusas-producer:latest ./producer
```

> **Important:** Always build images with minikube's daemon active. If you open a new terminal session you must run the `minikube docker-env` command again before building.

---

## Step 3 – Deploy to Kubernetes

Apply the namespace first, then all remaining manifests:

```powershell
kubectl apply -f k8s/namespace.yaml
kubectl apply -R -f k8s/
```

Verify all pods reach `Running` status (the backend takes ~40 s on first start due to Hibernate DDL):

```powershell
kubectl get pods -n tusas-vitrin -w
```

Expected output once everything is healthy:

```
NAME                               READY   STATUS    RESTARTS
tusas-backend-xxxx-xxxxx           1/1     Running   0
tusas-frontend-xxxx-xxxxx          1/1     Running   0
tusas-postgres-0                   1/1     Running   0
tusas-producer-xxxx-xxxxx          1/1     Running   0
```

---

## Step 4 – Start the tunnel

The minikube tunnel exposes the Ingress on `localhost:80`. Keep this command running in a dedicated terminal:

```powershell
minikube tunnel
```

> On Windows, the tunnel may request administrator privileges to bind port 80.

### Tunnel troubleshooting

**Symptom:** `http://localhost` is unreachable even though all pods are `Running`.

**Cause:** After a `minikube stop` / `minikube start` cycle the Kubernetes API server binds to a new port. Any tunnel process that was started before the restart still holds the old port and silently fails — it cannot reach the cluster and therefore never binds `localhost:80`.

**Fix:** Kill the stale tunnel and start a fresh one.

```powershell
# 1. Update kubeconfig to the new API server port
minikube update-context

# 2. Find and kill any leftover tunnel process
Get-Process -Name "minikube" -ErrorAction SilentlyContinue | Stop-Process -Force

# 3. Confirm port 80 is no longer in use
netstat -ano | findstr "LISTENING" | findstr ":80 "

# 4. Start a fresh tunnel (keep this terminal open)
minikube tunnel
```

Verify the tunnel is working:

```powershell
# Port 80 should now show LISTENING on 127.0.0.1
netstat -ano | findstr "LISTENING" | findstr ":80 "
```

---

## Step 5 – Open the application

Navigate to **http://localhost** in your browser.

- The sidebar shows **● Live** when the WebSocket connection is established.
- Flight data appears within ~5 seconds as the producer starts posting.

---

## Scaling the backend

**Manual:**
```powershell
kubectl scale deployment tusas-backend --replicas=3 -n tusas-vitrin
```

**Horizontal Pod Autoscaler (HPA):**
```powershell
kubectl autoscale deployment tusas-backend \
  --cpu-percent=50 --min=1 --max=5 -n tusas-vitrin

# Check HPA status
kubectl get hpa -n tusas-vitrin
```


---

## Rebuilding after code changes

Rebuild only the image(s) that changed, then restart the affected deployment:

```powershell
# Re-point to minikube's daemon (if in a new terminal)
minikube docker-env --shell powershell | Invoke-Expression

# Example: rebuild frontend
docker build --no-cache -t tusas-frontend:latest ./frontend
kubectl rollout restart deployment/tusas-frontend -n tusas-vitrin
kubectl rollout status  deployment/tusas-frontend -n tusas-vitrin

# Example: rebuild backend
docker build --no-cache -t tusas-backend:latest ./backend
kubectl rollout restart deployment/tusas-backend -n tusas-vitrin
kubectl rollout status  deployment/tusas-backend -n tusas-vitrin

# Example: rebuild producer
docker build --no-cache -t tusas-producer:latest ./producer
kubectl rollout restart deployment/tusas-producer -n tusas-vitrin
kubectl rollout status  deployment/tusas-producer -n tusas-vitrin
```

---

## Useful commands

```powershell
# Pod logs
kubectl logs -n tusas-vitrin deployment/tusas-backend  -f
kubectl logs -n tusas-vitrin deployment/tusas-frontend -f
kubectl logs -n tusas-vitrin deployment/tusas-producer -f

# Describe a pod (events, image pull errors, etc.)
kubectl describe pod -n tusas-vitrin -l app=tusas-backend

# Check ingress routing
kubectl get ingress -n tusas-vitrin

# Open a shell inside a running container
kubectl exec -it -n tusas-vitrin deployment/tusas-backend -- sh

# Delete all application resources
kubectl delete namespace tusas-vitrin

# Delete all not "Running" pods.
kubectl delete pod -n tusas-vitrin -l app!=tusas-backend,app!=tusas-frontend,app!=tusas-postgres --field-selector=status.phase!=Running

# Stop minikube
minikube stop
```

---

## Manifest structure

```
k8s/
├── namespace.yaml              # Namespace: tusas-vitrin
├── secret.yaml                 # PostgreSQL credentials
├── postgres/
│   ├── statefulset.yaml        # postgres:17 with 1Gi PVC
│   └── service.yaml            # ClusterIP :5432
├── backend/
│   ├── deployment.yaml         # Spring Boot app (runs as tusas-backend-sa)
│   ├── service.yaml            # ClusterIP :8080
│   └── rbac.yaml               # ServiceAccount, Role, RoleBinding for pod creation
├── frontend/
│   ├── deployment.yaml         # nginx serving the Vue SPA
│   ├── service.yaml            # ClusterIP :80
│   └── ingress.yaml            # ingress-nginx routing rules
└── producer/
    ├── configmap.yaml          # API_URL env for the Go producer
    └── deployment.yaml         # Go producer (posts every 5 s)
```
