&nbsp;
> <a href="https://github.com/Jake-Moore/FactionIntegrations/releases/latest"> <img alt="Latest Release" src="https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/Jake-Moore/176f75278bd544e8657fdcf5562e7693/raw/test.json" /></a>

# FactionIntegrations
Compatibility layer for several factions forks
- AtlasFactions
- VulcanFactions
- Joseph's Factions (MCore Factions)
- SaberFactions
- SaberXFactions
- StellarFactions
- FactionsUUID
- various privatized forks

Forks not supported (and not planned)
- SavageFactions
- SupremeFactions
- UltimateFactionsV2

## Using the Library

Before you can use FactionIntegrations, you have to import it into your project.

### Repository Information
Add the following Repository to your build file.
#### Maven [pom.xml]:
```xml
<repository>
  <id>luxious-public</id>
  <name>Luxious Repository</name>
  <url>https://repo.luxiouslabs.net/repository/maven-public/</url>
</repository>
```
#### Gradle (kotlin) [build.gradle.kts]:
```kotlin
maven {
    name = "luxiousPublic"
    url = uri("https://repo.luxiouslabs.net/repository/maven-public/")
}
```
#### Gradle (groovy) [build.gradle]:
```groovy
maven {
  name "luxiousPublic"
  url "https://repo.luxiouslabs.net/repository/maven-public/"
}
```


### Dependency Information
Add the following dependency to your build file.  
Replace `{VERSION}` with the version listed at the top of this page.  
#### Maven Dependency [pom.xml]
```xml
<dependency>
  <groupId>com.kamikazejam</groupId>
  <artifactId>FactionIntegrations</artifactId>
  <version>{VERSION}</version>
  <scope>compile</scope>
</dependency>
```

#### Gradle Dependency (groovy) [build.gradle]
```groovy
implementation "com.kamikazejam:FactionIntegrations:{VERSION}"
```

#### Gradle Dependency (kotlin) [build.gradle.kts]
```kotlin
implementation("com.kamikazejam:FactionIntegrations:{VERSION}")
```
