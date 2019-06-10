# Monkey Nut Head Spring Boot Admin Monkey Around

Goal of this bit of messing around is to work out how to best have both 2.1.x and 1.5.x
spring boot admin clients work against a 2.1.x server *without* having to change the
1.5.x applications.

###### Conclusion

Unfortunately, since the 1.5.x registration does not follow re-directs, and the need
to support the new server running at a different location, this has to be
done by proxying the calls to the old endpoint.

***

#### Certificates

Also spent a bit of time trying to get the server to work with self-signed certificates and
SSL on the client.  See the new-client project:

The key store is `keystore.p12` which has a password of `keystore`.  The certificate alias
is `tomcat` (see [this](https://www.drissamri.be/blog/java/enable-https-in-spring-boot/) for
an example of setting this up).


###### Conclusion

After registration, in order for the spring boot admin server to be able to call the 
SSL endpoint, either the certificate will need to be trusted *or* certificate validation
can be turned off (see ``).