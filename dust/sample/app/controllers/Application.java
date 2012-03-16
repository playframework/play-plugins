package controllers;

import org.codehaus.jackson.node.ObjectNode;

import play.*;
import play.mvc.*;
import play.libs.*;
import views.html.*;

public class Application extends Controller {

	public static Result frame() {
		return ok(index.render());
	}

	public static Result data() {
		ObjectNode json = Json.newObject();

		json.put("name", "Json");
		json.put("count", 1);

		return ok(json);
	}

}