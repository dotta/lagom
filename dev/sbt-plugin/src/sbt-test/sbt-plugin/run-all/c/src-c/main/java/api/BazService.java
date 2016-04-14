package api;

import akka.stream.javadsl.Source;

import akka.NotUsed;
import com.lightbend.lagom.api.transport.Method;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import static com.lightbend.lagom.javadsl.api.Service.*;

public interface BazService extends Service {

  ServiceCall<NotUsed, NotUsed, String> baz();

  @Override
  default Descriptor descriptor() {
    return named("/c").with(restCall(Method.GET,  "/baz",    baz())).withAutoAcl(true);
  }
}
