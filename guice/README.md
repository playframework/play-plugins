# Play 2.1
this plugin now is obsolate. Use ```play.GlobalSetting#getControllerInstance``` instead.


# For older verions of Play

# Guice Plugin

This plugin provides support for [Guice](http://code.google.com/p/google-guice/)

# Features

* Allow constructor injection via delegate 

this design pattern can be achieved the same as with manual Injection. 
[This] (https://github.com/typesafehub/play-plugins/tree/master/inject/sample_without_static_field) sample app demonstrates it.

* Allows static field injection to a preconfigured package (```controllers``` by default) ie

```java
//requires default constructor
public class Application extends Controller {
  
  @Inject static Service s;

  public static Result index() {
    return ok(index.render(s.demonstrate()));
  }
  
}
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

```"com.typesafe" % "play-plugins-guice" % "2.0.3"``` to your dependencies

play 2.0.1:

```"com.typesafe" % "play-plugins-guice" % "2.0.2"``` to your dependencies


* create a file called ```play.plugins``` in your ```app/conf``` directory

* add ```1500:com.typesafe.plugin.inject.GuicePlugin```

* that's it

# Testing

* testing can be achieved two ways in case of using field injection:

  * making the injected dependencies anything but private

  * injected dependencies can be private in which case a new constructor (alongside with an empty default one) could be added to controllers which could be used to inject the mocked dependencies
  (I would recommend the former solution)

* testing is trivial in case of using constructor injection.

# Sample

for an example, see the bundled sample app


## Licence

This software is licensed under the Apache 2 license, quoted below.

Copyright 2012 Typesafe (http://www.typesafe.com).

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this project except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
