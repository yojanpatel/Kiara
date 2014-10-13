Kiara
=====

Kiara, an intelligent music player.

Each User is a wrapper for a Spotify user. They create playlists, by searching songs on Spotify.
A machine learning module, (still to be implemented) will dynamically generate the order to play in.
Each User has multiple playlists, and each playlist has multiple songs.
Each spotify song, that is used by a current user has its own entry since this will allow user-specific data to be stored.

User ---->Playlist
 |         /
 |        /
 |       /
 |      /
 |     /
 v    v
  Song

(-->) := One-to-Many

Uses;
Google App Engine
Google Datastore
JAX-RS (Jersey)
JUnit tests

TODO:
JSON reader/writers for transport
possibly Google's protocol buffers

Client Android app
