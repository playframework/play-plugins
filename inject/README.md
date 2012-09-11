# Play 2.1
this plugin now is obsolate. Use ```play.GlobalSetting#getControllerInstance``` instead.


# For older verions of Play

# Manual Injection Plugin

This plugin provides support for manual dependency injection. Injection points are defined using ```@Provides``` (and ```@Singleton```) annotations in a class called ```module.Dependencies```. 
These would be injected into a preconfigured package (```controllers``` by default)

# Features

* Allows static field injection to a preconfigured package (```controllers``` by default) ie

(see ```sample``` for a full example)

```java
// define your dependencies in module/Dependencies.java
public class Dependencies {
  
  @Provides 
  @Singleton
  public Something makeSomething() { 
    return new SpecialSomething();
  }

  @Provides 
  @Singleton
  public MyService makeService(Something s) {
    return new SpecialService(s);
  }


}
```

```java
//any controller
public class Application extends Controller {
  
  @Inject static MyService s;

  public static Result index() {
    return ok(index.render(s.demonstrate()));
  }
  
}

```

* or you can use constructor injection with a delege
(see ```sample_without_static_field``` for a full example)

in app/controllers/Application.java:

```java
public class Application extends Controller {

  private MyService s;

  @Inject public Application( MyService s) {
    this.s=s;
  }

  public  Result index() {
    return ok(index.render(s.demonstrate()));
  }

}
```

in app/module/Dependencies.java:

```java
public class Dependencies {

  public static InjectPlugin inject() {
    return Play.application().plugin(InjectPlugin.class);
  }
  
  //this is needed for each controller
  public static controllers.Application application() {
    return inject().getInstance(controllers.Application.class);
  }

  @Provides
  @Singleton
  public Something makeSomething() {
    return new Something() {
      public String noWhat() {
        return "yay";
      }
    };
  }

  @Provides
  @Singleton
  public MyService makeService(Something s) {
    return new MyService(s) {
      public String demonstrate() { return s.noWhat();}
    };
  }

}
```


in ```conf/routes```:

```
GET     /                           module.Dependencies.application.index
```

* Dependency modules are configurable (by default it's ```module.Dependencies```)

* Allows direct access to the factory method ie 

```java 
//in Global.java
play.Play.application().plugin(InjectPlugin.class).getInstance(MyServiceInterface.class)
```

# How to install

* add 

play 2.0.2:

```"com.typesafe" % "play-plugins-inject" % "2.0.3"``` to your dependencies

play 2.0.1:

```"com.typesafe" % "play-plugins-inject" % "2.0.2"``` to your dependencies


* create a file called ```play.plugins``` in your ```app/conf``` directory

* add ```1500:com.typesafe.plugin.inject.ManualInjectionPlugin```

* that's it

# Testing

* testing can be achieved two ways while using the static field injection:

  * making the injected dependencies anything but private

  * injected dependencies can be private in which case a new constructor could be added to controllers which could be used to inject the mocked dependencies
  (I would recommend the former solution)
* if the static delegate approach was used, one can directly test the controller



# Sample

for an example, see the bundled sample app


## Licence

This software is licensed under the Apache 2 license, quoted below.

Copyright 2012 Typesafe (http://www.typesafe.com).

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this project except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
