package module;

import com.google.inject.*;
import service.*;

public class Dependencies implements Module {

 public void configure(Binder binder) {
     binder.bind(Service.class).to(SomethingService.class);
  }  
}