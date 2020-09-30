# OpenAPI project
This is a generated [Web3j-OpenAPI](https://github.com/web3j/web3j-openapi) project using the [Epirus-CLI](https://github.com/epirus-io/epirus-cli).

The OpenAPI generated code is in `build/generated/source/web3j/main/kotlin`. 

if you want to specify more configuration,
check web3j-openapi-gradle-plugin [configuration](https://github.com/web3j/web3j-openapi-gradle-plugin#code-generation).
## Configuration
The project is ready to be run using `./gradlew run`.
it needs some configuration (link to the web3j-openapi docs).

### Overwrite the current main
To specify another entrypoint to your application, ie Main class, add the following to `build.gradle` :
```shell script
mainClassName = 'com.test.CustomMain'
```

then run `./gradlew run`

**Check [web3j-openapi-gradle-plugin](https://github.com/web3j/web3j-openapi-gradle-plugin#code-generation) for how to generate swagger-ui**
