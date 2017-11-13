
![Kotys](https://i.imgur.com/TY0wjhh.png)

A minimalistic yet highly functional JSON library

# Features

* Lightweight - small filesize and no dependencies and quick parsing
* No boilerplate - as simplistic as possible, yet very flexible
* Easy mapping - Easily convert JSON to objects, and back

# Examples

Presuming you have the following JSON stored as a string called "jsonString":

```json
{
  "name":"Bob",
  "age":25,
  "siblings":[
    "Jeff",
    "Sarah"
  ],
  "address":{
    "road":"Roadway Drive",
    "house":123,
    "city":"Townington",
    "country":"Germany"
  }
}
```
### Basic usage

You can easily access fields using the JSON and JSONArray objects:

```java
JSON person = new JSON(jsonString);
System.out.println(person.string("name")); // prints "Bob"
System.out.println(person.array("siblings").string(0)); // prints "Jeff"
System.out.println(person.json("address").string("city")); // prints "Townington"
```

### Object mapping

If you have the following classes, you are able to map the JSON object to a Person object. Notice the annotations, these will be elaborated upon later.

```java
public @JSONContainer class Person {
    private String name;
    private int age;
    private String[] siblings;
    private Address address;

    public String getName() { return name; }
    public int getAge() { return age; }
    public String[] getSiblings() { return siblings; }
    public Address getAddress() { return address; }
}
```
```java
public class Address {
    @JSONField(jsonKey = "road") private String road;
    @JSONField(jsonKey =  "house") private int house;
    @JSONField(jsonKey = "city") private String city;
    @JSONField(jsonKey = "country") private String country;
    private String something;

    public String getRoad() { return road; }
    public int getHouse() { return house; }
    public String getCity() { return city; }
    public String getCountry() { return country; }
}
```

After creating the JSON object, you can then specify to marshal it to a Person object.

```java
JSON person = new JSON(jsonString);
Person personObject = person.marshal(Person.class);
System.out.println(personObject.getAge()); // prints "25"
```

The annotation `@JSONContainer` specifies that all JSON keys (unless specifically ignored) will be mapped to a field with the same name. 
`@JSONField` specifies that only the provided field is mapped to the key specified in the annotation.

# Usage

Add Kotys as a dependency to your project. *Replace {version} with the version you would like, a list can be found [here](https://github.com/Arraying/Kotys/releases).*


### Maven

```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
<dependencies>
  <dependency>
    <groupId>com.github.Arraying</groupId>
    <artifactId>Kotys</artifactId>
    <version>{version}</version>
  </dependency>
</dependencies>
```

### Gradle

```gradle
repositories {
  maven { 
    url 'https://jitpack.io' 
  }
}
dependencies {
  compile 'com.github.Arraying:Kotys:{version}'
}
```

# Development & Contribution

I will be providing bugfixes and changes. If you'd like to contribute, great! Currently, the main focus on the library should be opimization. Just make any changes and PR.

