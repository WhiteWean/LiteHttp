# LiteHttp

A simple and easy-to-use package for OkHttp.

Litehttp is a network request framework based on OkHttp that implements standard RESTful style network requests. The core advantage of Litehttp is that it provides a concise and easy-to-use interface, allowing developers to no longer focus on the details of network requests and thus focus more on their business logic. Litehttp supports custom callbacks, logging, caching of GET and POST, cookie saving, and provides a custom SSL authentication setting interface.


# The use of litehttp

Add Dependency in build.gradle
```
implementation 'io.github.WhiteWean:LiteHttp:1.0.8'
implementation 'com.jakewharton:disklrucache:2.0.2'
implementation 'com.squareup.okhttp3:okhttp:4.11.0'
```

Permission settings in AndroidManifest.xml
```
<uses-permission android:name="android.permission.INTERNET"/>
```



# Encapsulated the following function

- Standard restfyl style request method includes:get, post, put, head, delete, options, patch, trace.
- Support custom callback, provide defined callback for file, string and Bitemap.
- Support logging function.
- Support caching for both GET and POST.
- Support cookie persistence
- Provide a custom SSL authentication setting interface



# Demo

The demonstration code can be found in app directory



