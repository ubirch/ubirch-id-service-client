# Ubirch-id-service-client

This client should be used to communicate with the ubirch-id-service. It needs a redis, 
to cache the public keys until expiration. 

If redis does not exist during first usage, the redis actor will throw an exception and the client
will not work properly. If the connection is lost during a later moment, the exception should be
catched and the request should just become forwarded directly to the id-service.  


# Config

The URL of the ubirch-id-service must be set as well as the redis cache max time to live: 

ubirchIdService.client {
  rest.host = "http://localhost:8081"
  redis.cache.maxTTL = 86400 // 24 hours in seconds
}

The redis host and port need to be set by:

ubirch.redisUtil {
  host = "localhost"
  port = 6379
}




# Test

For the test you can just start the redis with the command 'docker-compose up' in the root folder. 
