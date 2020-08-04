# Ubirch-id-service-client

This client should be used to communicate with the ubirch-id-service. It needs a redis, 
to cache the public keys until expiration. 

If redis does not exist during first usage, the redis actor will throw an exception and the client
will not work properly. If the connection is lost during a later moment, the exception should be
catched and the request should just become forwarded directly to the id-service.  


# Config

The URL of the ubirch-id-service must be set by the environment variable ID_SERVICE_BASE_URL. The 
redis cache max time to live can be set by the environmental variable ID_SERVICE_CLIENT_MAX_TTL. 
The default value is 86400 (24 hours).

The redis host and port are set by the env vars: REDIS_HOST and REDIS_PORT.


# Test

For the test you can just start the redis with the command 'docker-compose up' in the root folder. 
