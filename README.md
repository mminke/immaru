# Immaru Media Library

## IntelliJ configuration

While running a build from within IntelliJ/Android Studio it is possible a linkage error occurs.
The build.gradle.kts file specifies that the jvm version to use for the jvm target is JVM 21.
However, if IntelliJ/Android Studio is configured to use a lower version jdk for running gradle tasks,
the different files might be compiled using mixed versions resulting in the linkage error.
To solve this issue, configure the proper jdk in the settings:

![Settings](docs/images/settings-build-gradle.png)