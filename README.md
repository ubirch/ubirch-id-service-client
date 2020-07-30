# Ubirch-id-service-client

This client should be used to communicate with the ubirch-id-service. It needs a redis, 
to cache the public keys until expiration. 

If redis does not exist during first usage, the redis actor will throw an exception and the client
will not work properly. If the connection is lost during a later moment, the exception should be
catched and the request should just become forwarded directly to the id-service.  

# Test

For the test you can just start the redis with the command 'docker-compose up' in the root folder. 
