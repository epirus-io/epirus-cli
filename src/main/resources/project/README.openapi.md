# OpenAPI project
This is a generated [Web3j-OpenAPI](https://github.com/web3j/web3j-openapi) project using the [Epirus-CLI](https://github.com/epirus-io/epirus-cli).

The OpenAPI generated code is in `build/generated/source/web3j/main/kotlin`. 

if you want to specify more configuration,
check web3j-openapi-gradle-plugin [configuration](https://github.com/web3j/web3j-openapi-gradle-plugin#code-generation).

## Configuration
The project needs some configuration (link to the web3j-openapi docs) to be run.

### Overwrite the current main
To specify another entrypoint to your application, ie Main class, add the following to `build.gradle` :
```shell script
mainClassName = 'com.test.CustomMain'
```

### Run the project

#### Create an executable
To create an executable, run `./gradlew installShadowDist`.
The executable is generated, by default, to: `build/install/AppName-shadow/bin/`

#### Run directly
To run the project using gradle. Make sure to have set some environment variables or configuration
file as specified in the [Web3j-OpenAPI](https://github.com/web3j/web3j-openapi) documentation.

Then, run the following : `./gradlew run`

**Check [web3j-openapi-gradle-plugin](https://github.com/web3j/web3j-openapi-gradle-plugin#code-generation) for how to generate swagger-ui**
