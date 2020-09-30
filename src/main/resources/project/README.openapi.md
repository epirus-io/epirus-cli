# OpenAPI project
This is a generated Web3j-OpenAPI(link) project using the Epirus-CLI(link).

The OPENAPI generated code is in `build/generated/etc`. 

if you want to specify more configuration,
check web3j-openapi-gradle-plugin (link to config)
## Configuration
The project is ready to be run using `./gradlew run`.
it needs some configuration (link to the web3j-openapi docs).

### Overwrite the current main
To specify another entrypoint, ie Main class, of your project.
Add the following to `build.gradle` :
```shell script
mainClassName = 'com.test.CustomMain'
```

then run `./gradlew run`

**Check web3j-openapi-gradle-plugin for how to generate swagger-ui**
