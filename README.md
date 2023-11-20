&nbsp;
> <a href="https://github.com/Jake-Moore/FactionIntegrations/releases/latest"> <img alt="Latest Release" src="https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/Jake-Moore/176f75278bd544e8657fdcf5562e7693/raw/test.json" /></a>

# FactionIntegrations
Compatibility for Several factions Plugins (WIP)
- Should probably rework this repo using Maven modules, to avoid using reflection

## Using the Library
Before you can use FactionIntegrations, you have to import it into your project.  

### Import with Maven
Add the following Repository to your pom.xml
```xml
<repository>
  <id>luxious-public</id>
  <name>Luxious Repository</name>
  <url>https://nexus.luxiouslabs.net/public</url>
</repository>
```
Then add the following dependency  
Replace `{VERSION}` with the version listed at the top of this page.
```xml
<dependency>
  <groupId>com.kamikazejam</groupId>
  <artifactId>factionintegrations</artifactId>
  <version>{VERSION}</version>
  <scope>provided</scope>
</dependency>
```
&nbsp;
### Import with Gradle
```kotlin
maven {
    name = "luxiousPublic"
    url = uri("https://nexus.luxiouslabs.net/public")
}
```
Then add the following dependency  
Replace `{VERSION}` with the version listed at the top of this page.
```kotlin
compileOnly 'com.kamikazejam:factionintegrations:{VERSION}'
```


## Supported Factions Forks:
- AtlasFactions
- SupremeFactions
- StellarFactions
- SavageFactions
- SaberFactionsX
- SaberFactions
- LockedThread
- VulcanFactions
- FactionsUUID
- MassiveCore Factions
