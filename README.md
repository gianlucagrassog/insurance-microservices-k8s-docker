## Kubernetes

Start minikube 
```
    minikube start
    eval $(minikube docker-env)
    minikube addons enable ingress
```   
Build docker images
```
    docker build -t purchase-service:v1 ./purchase-service 
    docker build -t user-service:v1 ./user-service
    docker build -t policy-service:v1 ./policy-service
    docker build -t receipt-service:v1 ./receipt-service
```  
### Receipt Service
Make sure to substitute your email in the code. <br />
In receiptApp.py substitute user@gmail.com with your email.

If you want to use also different sender address, substitute email and password in "3-service-db-secret-file.yaml" in k8s folder.
### /etc/hosts

```sh
echo "$(minikube ip) insurance.app.loc" | sudo tee -a /etc/hosts
```

### Demo

```
# Create a user
curl insurance.app.loc/users/ -X POST -H "Content-Type: application/json" -d '{"name": "Steve Jobs", "age": 18, "bmclass": 10}'

# Create a policy
curl insurance.app.loc/policies/ -X POST -H "Content-Type: application/json" -d '{"name": "Policy1", "type": "bonus malus", "description": "I am a insurance policy"}'

#Create a optional
curl insurance.app.loc/optionals/ -X POST -H "Content-Type: application/json" -d '{"name": "Optional1", "price": 70.5, "description": "I am a optional"}'

# Create a purchase
curl insurance.app.loc/purchases -X POST -H "Content-Type: application/json" -d '{"description":"my purchase", "user": "...", "policy": "...", "optionals_list": ["...","..."]}'

# Get the users
curl insurance.app.loc/users/

# Get the policies
curl insurance.app.loc/policies/

# Get the optionals
curl insurance.app.loc/optionals/

# Get the purchases
curl insurance.app.loc/purchases/

```
###Other useful commands
```
minikube stop
minikube delete
kubectl rollout restart deploy
```
## Docker (docker-compose) 

```
docker-compose up --build
```   

### /etc/hosts

```sh
echo "127.0.0.1 insurance.local" | sudo tee -a /etc/hosts
```
### Receipt Service
Make sure to substitute your email in the code.<br />
In receiptApp.py substitute user@gmail.com with your email.

If you want to use also different sender address, substitute email and password in "docker-compose.yaml".
### Demo

```
# Create a user
curl insurance.local/users/ -X POST -H "Content-Type: application/json" -d '{"name": "Bill Gates", "age": 18, "bmclass": 10}'

# Delete a user
curl insurance.local/users/{_id} -X DELETE

# Create a policy
curl insurance.local/policies/ -X POST -H "Content-Type: application/json" -d '{"name": "Policy1", "type": "bonus malus", "description": "I am a insurance policy"}'

# Delete a policy
curl insurance.local/policies/{_id} -X DELETE

#Create a optional
curl insurance.local/optionals/ -X POST -H "Content-Type: application/json" -d '{"name": "Optional1", "price": 70.5, "description": "I am a optional"}'

# Delete a optional
curl insurance.local/optionals/{_id} -X DELETE

# Create a purchase
curl insurance.local/purchases -X POST -H "Content-Type: application/json" -d '{"description":"my purchase", "user": "...", "policy": "...", "optionals_list": ["...","..."]}'

# Delete a purchase
curl insurance.local/purchases/{_id} -X DELETE

# Get the users
curl insurance.local/users/

# Get the policies
curl insurance.local/policies/

# Get the optionals
curl insurance.local/optionals/

# Get the purchases
curl insurance.local/purchases/

```
### Useful commands
Stop the containers using the following command:
```
docker-compose down
```
Delete all containers using the following command:
```
docker rm -vf $(docker ps -aq)
```
Delete all volumes using the following command:
```
docker volume rm $(docker volume ls -q)
```

Delete all images using the following command:
```
docker rmi -f $(docker images -aq)
```
curl insurance.local/users/ -X PUT -H "Content-Type: application/json" -d '{"_id":"61f56d70daa4d40009dc4224", name": "Alfio Gates", "age": 18, "bmclass": 10}'
