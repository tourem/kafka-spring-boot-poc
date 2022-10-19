# Kafka Spring Boot Poc


*	This is simple application to demonstrate the Spring boot application to kakfa connectivity.
*	Spring boot application will act as producer and consumer. 


![Demo App](images/demo-spring-boot-kafka-flow.png) 

# Build docker image and run

## Docker Compose

Compose is a tool for defining and running multi-container Docker applications. With Compose, you use a YAML file to configure your application’s services. 
Then, with a single command, you create and start all the services from your configuration. To learn more about all the features of Compose, see the list of features.
Compose works in all environments: production, staging, development, testing, as well as CI workflows. You can learn more about each case in Common Use Cases.
Using Compose is basically a three-step process:


1.  Define your app’s environment with a Dockerfile so it can be reproduced anywhere.
2.	Define the services that make up your app in docker-compose.yml so they can be run together in an isolated environment.
3.	Run docker-compose up and Compose starts and runs your entire app


Compose has commands for managing the whole lifecycle of your application:

*	Start, stop, and rebuild services
*	View the status of running services
*	Stream the log output of running services
*	Run a one-off command on a service

Make sure you are at folder /ocp-demo-app/demo-spring-boot-kafka and run below command

*   **build images**, this command will use the **DockerFile** to first create spring boot jar then build the image 
* 	This command will use the default file name **docker-compose.yml** 
* 	Just for demo purpose we are creating only 1-1 instance of zookeeper and kafka. 

![Demo App](images/single-node-zoo-kafka.jpeg)

* 	Here is the content of docker-compose.yml


	
	version: '3.3'
		
		services:
		  zookeeper:
		    image: confluentinc/cp-zookeeper
		    ports:
		      - 2181:2181
		    environment:
		      - ALLOW_ANONYMOUS_LOGIN=yes
		      - ZOOKEEPER_CLIENT_PORT=2181
		
		
		  kafka:
		    image: confluentinc/cp-kafka
		    depends_on:
		      - zookeeper
		    ports:
		      - 9092:9092
		    environment:
		      - KAFKA_BROKER_ID=1
		      - KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181
		      - KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092
		      - KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
		      - KAFKA_INTER_BROKER_LISTENER_NAME=PLAINTEXT
		      - KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1
		      - KAFKA_AUTO_CREATE_TOPICS_ENABLE=true
		      
		      
		  demo-spring-boot-kafka:
		    build: .
		    container_name: demo-spring-boot-kafka
		    depends_on:
		      - zookeeper
		      - kafka
		
		    ports:
		      - 8080:8080
		    environment:
		      - KAFKA_ENDPOINT=localhost:9092



* 	Before running this , you would like to replace the **localhost** with your ip address. 

*	Now lets **create/build** image for demo-spring-boot-kafka

	$ docker-compose build
	
	
* 	this command will create image for demo-spring-boot-kafka and you can verify this with docker images comamnd

	$ docker images
	
	
![Docker-compose build](images/compose-build.JPG) 
	
*   **run the application**, use below command to run the application


	$ docker-compose up -d

* 	This will also create a default network

![Docker-compose build](images/compose-up.JPG) 	


* 	here are the process,containers & images in use by docker-compose

![Docker-compose build](images/compose-all.JPG) 	


# Kubernetes


*	build image and push to docker hub	

*	use below commands to create image and tag and push to docker hub

*	go to folder /ocp-demo-app/ocp-demo-app-db

*	build image with required tag

	$ docker build . -t sumitgupta28/demo-spring-boot-kafka
	
*	this will create docker image with tag	**sumitgupta28/demo-spring-boot-kafka:latest**

*	docker login
	
	$ docker login

*	docker push

	$ docker push sumitgupta28/demo-spring-boot-kafka
	
	
![Docker Push](images/docker-push.JPG) 

![Docker Push](images/docker-push-1.JPG) 


Since now we have pushed the producer/consumer app to docker hub. so let try to deploy app on kubernates. 

Just for understanding to start with we will do the **kafka cluster creation with plain-yaml approach** for now and later we will see the **helm charts and Operators**. 


#### Kafka

> Apache Kafka is a scalable and fault-tolerant distributed message broker, and it can handle large volumes of real-time data efficiently. 

> Compared to other message brokers such as RabbitMQ or ActiveMQ, it has a much higher throughput.

> Kafka's pub/sub messaging system is probably the most widely used feature, 

> however we can also use it for log aggregation since it provides persistent storage for published messages.

![Docker Push](images/Kafka_Zookeeper.png) 

> Kafka has 5 components:


> 1.    **Topic**: A topic is a category or feed name to which messages are published by the message producers. Topics are partitioned and each partition is represented by the ordered immutable sequence of messages. Each message in the partition is assigned a unique sequential ID (offset).
> 2. 	**Broker**: A Kafka cluster consists of servers where each one may have server processes (brokers). Topics are created within the context of broker processes.
> 3. 	**Zookeeper**: Zookeeper serves as the coordinator between the Kafka broker and consumers.
> 4. 	**Producers**: Producers publish data to the topics by choosing the appropriate partition within the topic.
> 5. 	**Consumers**: Consumers are the applications or processes that subscribe to topics and process the feed of published messages.
 
![Docker Push](images/Pub_Consumer.png) 
 
 
### Kubernetes YAML 

Refer to **\kubernetes\demo-spring-boot-kafka.yml**


> 1.    **Create Zookeeper Deployment and Service**:

This will create Zookeeper Deployment with 1 replica and zookeeper service

	
	apiVersion: apps/v1
	kind: Deployment
	metadata:
	  labels:
	    app: zookeeper
	  name: zookeeper
	
	spec:
	  replicas: 1
	  selector:
	    matchLabels:
	      app: zookeeper
	  template:
	    metadata:
	      labels:
	        app: zookeeper
	    spec:
	      containers:
	      - image: library/zookeeper:3.4.13
	        imagePullPolicy: IfNotPresent
	        name: zookeeper
	        ports:
	        - containerPort: 2181
	        env:
	        - name: ZOO_MY_ID
	          value: "1"
		
	---

	apiVersion: v1
	kind: Service
	metadata:
	  labels:
	    app: zookeeper-service
	  name: zookeeper-service
	
	spec:
	  type: NodePort
	  ports:
	  - name: zookeeper-port
	    port: 2181
	    targetPort: 2181
	  selector:
	    app: zookeeper
    

	

> 2.    **Create Kafka StatefulSet and Service**

This will create kafka Server and StatefulSet of kafka with 3 replicas

	
	apiVersion: v1
	kind: Service
	metadata:
	  name: kafka
	  labels:
	    app: kafka
	spec:
	  ports:
	  - port: 9092
	    name: plaintext
	  clusterIP: None
	  selector:
	    app: kafka
	    
	---
	apiVersion: apps/v1
	kind: StatefulSet
	metadata:
	  name: kafka
	spec:
	  selector:
	    matchLabels:
	      app: kafka
	  serviceName: "kafka"
	  replicas: 3
	  podManagementPolicy: OrderedReady
	  template:
	    metadata:
	      labels:
	        app: kafka # has to match .spec.selector.matchLabels
	    spec:
	      containers:
	      - name: kafka
	        image: wurstmeister/kafka:2.11-2.0.0
	        imagePullPolicy: IfNotPresent
	        ports:
	        - containerPort: 9092
	          name: plaintext
	        env:
	          - name: KAFKA_ADVERTISED_PORT
	            value: "9092"
	          - name: KAFKA_ZOOKEEPER_CONNECT
	            value: "zookeeper-service:2181"
	          - name: KAFKA_LISTENERS
	            value: "PLAINTEXT://:9092"
	
	
> 2.    **Deploy Producer/Consumer Application**

this will create Deployemnt and Service for **demo-spring-boot-kafka** app and 

	
	---

	apiVersion: v1
	kind: Service
	metadata:
	  name: demo-spring-boot-kafka
	  labels:
	    app: demo-spring-boot-kafka
	spec:
	  type: NodePort
	  selector:
	    app: demo-spring-boot-kafka
	  ports:
	  - protocol: TCP
	    port: 8080
	    nodePort: 30080
	    name: http
	    
	---
	apiVersion: apps/v1
	kind: Deployment
	metadata:
	  name: demo-spring-boot-kafka
	spec:
	  selector:
	    matchLabels:
	      app: demo-spring-boot-kafka
	  replicas: 1
	  template:
	    metadata:
	      labels:
	        app: demo-spring-boot-kafka
	    spec:
	      containers:
	      - name: demo-spring-boot-kafka
	        image: sumitgupta28/demo-spring-boot-kafka:latest
	        ports:
	        - containerPort: 8080
	        env:
	          - name: KAFKA_ENDPOINT
	            value: kafka:9092
	
	
	
![Docker Push](images/demo-spring-boot-kafka-k8s.png) 



### Kubernetes lets apply..

	
	$ kubectl apply -f demo-spring-boot-kafka.yml
	service/zookeeper-service created
	deployment.apps/zookeeper created
	service/kafka created
	statefulset.apps/kafka created
	service/demo-spring-boot-kafka created
	deployment.apps/demo-spring-boot-kafka created
		

**Validate All the objects**

	
	$ kubectl get all
	NAME                                      READY   STATUS    RESTARTS   AGE
	pod/kafka-0                               1/1     Running   0          71s
	pod/kafka-1                               1/1     Running   0          25s
	pod/kafka-2                               1/1     Running   0          23s
	pod/demo-spring-boot-kafka-5c88dc58f4-vts2l   1/1     Running   1          71s
	pod/zookeeper-cf4546599-8qpdm             1/1     Running   0          71s
	
	NAME                         TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)          AGE
	service/kafka                ClusterIP   None           <none>        9092/TCP         71s
	service/demo-spring-boot-kafka   NodePort    10.96.123.55   <none>        8080:30080/TCP   71s
	service/zookeeper-service    NodePort    10.108.26.85   <none>        2181:31027/TCP   71s
	
	NAME                                 READY   UP-TO-DATE   AVAILABLE   AGE
	deployment.apps/demo-spring-boot-kafka   1/1     1            1           71s
	deployment.apps/zookeeper            1/1     1            1           71s
	
	NAME                                            DESIRED   CURRENT   READY   AGE
	replicaset.apps/demo-spring-boot-kafka-5c88dc58f4   1         1         1       71s
	replicaset.apps/zookeeper-cf4546599             1         1         1       71s
	
	NAME                     READY   AGE
	statefulset.apps/kafka   3/3     71s
	
	

### Validate Application..
	
	http://<<<host>>:30080/kafka/publish/TestMessage
	Message sent! check logs!
	
**Application logs**
	
	
	
	2021-01-13 05:04:18.672  INFO 1 --- [nio-8080-exec-6] c.ocp.demo.kafka.producer.KafkaProducer  : Sending message -> TestMessage
	2021-01-13 05:04:18.687  INFO 1 --- [nio-8080-exec-6] o.a.k.clients.producer.ProducerConfig    : ProducerConfig values:
	        acks = 1
	        batch.size = 16384
	        bootstrap.servers = [kafka:9092]
	        buffer.memory = 33554432
	        client.dns.lookup = use_all_dns_ips
	        client.id = producer-1
	        compression.type = none
	        connections.max.idle.ms = 540000
	        delivery.timeout.ms = 120000
	        enable.idempotence = false
	        interceptor.classes = []
	        internal.auto.downgrade.txn.commit = true
	        key.serializer = class org.apache.kafka.common.serialization.StringSerializer
	        linger.ms = 0
	        max.block.ms = 60000
	        max.in.flight.requests.per.connection = 5
	        max.request.size = 1048576
	        metadata.max.age.ms = 300000
	        metadata.max.idle.ms = 300000
	        metric.reporters = []
	        metrics.num.samples = 2
	        metrics.recording.level = INFO
	        metrics.sample.window.ms = 30000
	        partitioner.class = class org.apache.kafka.clients.producer.internals.DefaultPartitioner
	        receive.buffer.bytes = 32768
	        reconnect.backoff.max.ms = 1000
	        reconnect.backoff.ms = 50
	        request.timeout.ms = 30000
	        retries = 2147483647
	        retry.backoff.ms = 100
	        sasl.client.callback.handler.class = null
	        sasl.jaas.config = null
	        sasl.kerberos.kinit.cmd = /usr/bin/kinit
	        sasl.kerberos.min.time.before.relogin = 60000
	        sasl.kerberos.service.name = null
	        sasl.kerberos.ticket.renew.jitter = 0.05
	        sasl.kerberos.ticket.renew.window.factor = 0.8
	        sasl.login.callback.handler.class = null
	        sasl.login.class = null
	        sasl.login.refresh.buffer.seconds = 300
	        sasl.login.refresh.min.period.seconds = 60
	        sasl.login.refresh.window.factor = 0.8
	        sasl.login.refresh.window.jitter = 0.05
	        sasl.mechanism = GSSAPI
	        security.protocol = PLAINTEXT
	        security.providers = null
	        send.buffer.bytes = 131072
	        ssl.cipher.suites = null
	        ssl.enabled.protocols = [TLSv1.2]
	        ssl.endpoint.identification.algorithm = https
	        ssl.engine.factory.class = null
	        ssl.key.password = null
	        ssl.keymanager.algorithm = SunX509
	        ssl.keystore.location = null
	        ssl.keystore.password = null
	        ssl.keystore.type = JKS
	        ssl.protocol = TLSv1.2
	        ssl.provider = null
	        ssl.secure.random.implementation = null
	        ssl.trustmanager.algorithm = PKIX
	        ssl.truststore.location = null
	        ssl.truststore.password = null
	        ssl.truststore.type = JKS
	        transaction.timeout.ms = 60000
	        transactional.id = null
	        value.serializer = class org.apache.kafka.common.serialization.StringSerializer
	
	2021-01-13 05:04:18.745  INFO 1 --- [nio-8080-exec-6] o.a.kafka.common.utils.AppInfoParser     : Kafka version: 2.6.0
	2021-01-13 05:04:18.746  INFO 1 --- [nio-8080-exec-6] o.a.kafka.common.utils.AppInfoParser     : Kafka commitId: 62abe01bee039651
	2021-01-13 05:04:18.746  INFO 1 --- [nio-8080-exec-6] o.a.kafka.common.utils.AppInfoParser     : Kafka startTimeMs: 1610514258745
	2021-01-13 05:04:18.788  INFO 1 --- [ad | producer-1] org.apache.kafka.clients.Metadata        : [Producer clientId=producer-1] Cluster ID: RXYyHa1HRM6ViJ3QevY1pg
	2021-01-13 05:04:19.105  INFO 1 --- [ntainer#0-0-C-1] c.ocp.demo.kafka.consumer.KafkaConsumer  : Consumed message -> TestMessage	
	
	
	