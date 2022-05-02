# TLDDoc

Clone of `Tag Library Documentation Generator`

Full [CHANGELOG](CHANGELOG.md)

# Building TLDDoc

## Prerequesits

* Apache Maven 3.5.4\+
* JDK 8\+

## Building-Steps

1. Clean full project
    `mvn clean`
2. Build and test project
    `mvn verify`
3. Generate documentation
    `mvn site`
4. Generate source- and javadoc-artifacts and assemblies
    `mvn package`

## Support runs

* Set version number
    `mvn versions:set -DnewVersion=...`

* Dependency Report
    `mvn versions:display-dependency-updates versions:display-plugin-updates versions:display-property-updates`