# Emailer  

This plugin provides a simple emailer.

## installation

play 2.0.2:

* add ```"com.typesafe" %% "play-plugins-mailer" % "2.0.4"``` to your dependencies (```project/Build.scala```)

play 2.0.1:
* add ```"com.typesafe" %% "play-plugins-mailer" % "2.0.2"``` to your dependencies (```project/Build.scala```)

and then
* add ```1500:com.typesafe.plugin.CommonsMailerPlugin``` to your ```conf/play.plugins```

furthermore, the following parameters can be configured in ```conf/application.conf```

```
smtp.host (mandatory)
smtp.port (defaults to 25)
smtp.ssl (defaults to no)
smtp.user (optional)
smtp.password (optional)
```


## using it from java 

```java
import com.typesafe.plugin.*;
MailerAPI mail = play.Play.application().plugin(MailerPlugin.class).email();
mail.setSubject("mailer");
mail.addRecipient("Peter Hausel Junior <noreply@email.com>","example@foo.com");
mail.addFrom("Peter Hausel <noreply@email.com>");
//sends html
mail.sendHtml("<html>html</html>" );
//sends text/text
mail.send( "text" );
//sends both text and html
mail.send( "text", "<html>html</html>");

```

## using it from scala

```scala
import com.typesafe.plugin._
val mail = use[MailerPlugin].email
mail.setSubject("mailer")
mail.addRecipient("Peter Hausel Junior <noreply@email.com>","example@foo.com")
mail.addFrom("Peter Hausel <noreply@email.com>")
//sends html
mail.sendHtml("<html>html</html>" )
//sends text/text
mail.send( "text" )
//sends both text and html
mail.send( "text", "<html>html</html>")

```


## Licence

This software is licensed under the Apache 2 license, quoted below.

Copyright 2012 Typesafe (http://www.typesafe.com).

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this project except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.