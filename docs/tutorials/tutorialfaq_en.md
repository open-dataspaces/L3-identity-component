# Tutorial FAQ (Reference Implementation)

Q. How long is the validity period of the authorization code obtained through user authentication (authorization code flow)？  
A. The authorization code is valid for 60 seconds. If the validity period expires, please obtain the authorization code again.

Q. How long is the validity period of the access token?  
A. The access token is valid for 300 seconds (Keycloak default setting). If the validity period expires, please obtain the access token again.

