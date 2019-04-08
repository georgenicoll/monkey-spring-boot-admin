# Monkey Nut Head Spring Boot Admin Monkey Around

Goal of this bit of messing around is to work out how to best have both 2.1.x and 1.5.x
spring boot admin clients work against a 2.1.x server *without* having to change the
1.5.x applications.

#### Conclusion

Unfortunately, since the 1.5.x registration does not follow re-directs, and the need
to support the new server running at a different location, this has to be
done by proxying the calls to the old endpoint.