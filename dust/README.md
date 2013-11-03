# Dust Plugin

This plugin provides build time compilation for the [LinkedIn](https://github.com/linkedin/dustjs) fork of [Dust](http://akdubya.github.io/dustjs/) templates.

# How to install

Add the proper sbt plugin to your plugin.sbt file.

Play 2.2.x:

`addSbtPlugin("com.typesafe" % "play-plugins-dust" % "1.5")`

Play 2.0.2:

`addSbtPlugin("com.typesafe" % "play-plugins-dust" % "1.4")`

Play 2.0.1:

`addSbtPlugin("com.typesafe" % "play-plugins-dust" % "1.0-SNAPSHOT")`

# How to Use


Include dust. Note that this is not provided by the sbt plugin. It can be found here: [dust-core-2.1.0.js](https://raw.github.com/typesafehub/play-plugins/master/dust/sample/public/javascripts/dust-core-2.1.0.js)

`<script src="@routes.Assets.at("javascripts/dust-core-2.1.0.js")"></script>`

Put your dust template .tl files under the `app/assets` directory.

Reference the generated .js in a script tag:
`<script src="@routes.Assets.at("example.tl.js")"></script>`

Render the template when you receive the json:

    $(function() {
        $.get('@routes.Application.data', function(data) {
            console.log('data = ' + JSON.stringify(data));
            dust.render('example.tl', data, function(err, out) {
                $('#dust_pan').html(err ? err : out);
            });
        });
    });


# Changelog

1.5 - November 3, 2013

- Made the plugin compatible with Play 2.2.x.
- Upgraded Dust full and core to 2.1.0 from the [LinkedIn](https://github.com/linkedin/dustjs) fork of [Dust](http://akdubya.github.io/dustjs/).
- Added test specs for valid and unvalid template files.
- Bumped up the example to use Play 2.2.x.

# Sample

For an example, see the bundled sample app included built with Play 2.2.x.

## Licence

This software is licensed under the Apache 2 license, quoted below.

Copyright 2012 Typesafe (http://www.typesafe.com).

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this project except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
