# Emailer  

This plugin provides a simple emailer.

## installation

play 2.3.x:

* add ```"com.typesafe.play.plugins" %% "play-plugins-mailer" % "2.3.1"``` to your dependencies (```project/Build.scala```)

and then
* add ```1500:com.typesafe.plugin.CommonsMailerPlugin``` to your ```conf/play.plugins```

furthermore, the following parameters can be configured in ```conf/application.conf```

```
smtp.host (mandatory)
smtp.port (defaults to 25)
smtp.ssl (defaults to no)
smtp.tls (defaults to no)
smtp.user (optional)
smtp.password (optional)
smtp.debug (defaults to no, to take effect you also need to set the log level to "DEBUG" for the application logger)
smtp.mock (defaults to no, will only log all the email properties instead of sending an email)
```


## using it from java 

```java
import com.typesafe.plugin.*;
MailerAPI mail = play.Play.application().plugin(MailerPlugin.class).email();
mail.setSubject("mailer");
mail.setRecipient("Peter Hausel Junior <noreply@email.com>","example@foo.com");
mail.setFrom("Peter Hausel <noreply@email.com>");
//adds attachment
mail.addAttachment("attachment.pdf", new File("/some/path/attachment.pdf"));
//adds inline attachment from byte array
byte[] data = "data".getBytes();
mail.addAttachment("data.txt", data, "text/plain", "A simple file", EmailAttachment.INLINE);
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
mail.setRecipient("Peter Hausel Junior <noreply@email.com>","example@foo.com")
//or use a list
mail.setBcc(List("Dummy <example@example.org>", "Dummy2 <example@example.org>"):_*)
mail.setFrom("Peter Hausel <noreply@email.com>")
//adds attachment
mail.addAttachment("attachment.pdf", new File("/some/path/attachment.pdf"))
//adds inline attachment from byte array
val data: Array[Byte] = "data".getBytes
mail.addAttachment("data.txt", data, "text/plain", "A simple file", EmailAttachment.INLINE)
//sends html
mail.sendHtml("<html>html</html>" )
//sends text/text
mail.send( "text" )
//sends both text and html
mail.send( "text", "<html>html</html>")
```

use[MailerPlugin] needs an implicit play.api.Application available to it.  If you do not have one available already from where you are trying to create the mailer you may want to add this line to get the current Application.

```scala
import play.api.Play.current
```

## Licence

This software is licensed under the Apache 2 license, quoted below.

Copyright 2012 Typesafe (http://www.typesafe.com).

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this project except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
