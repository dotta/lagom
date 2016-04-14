/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api.tools;

import static com.lightbend.lagom.javadsl.api.Service.*;

import com.lightbend.lagom.api.transport.Method;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;

import akka.NotUsed;

public interface AclService extends Service {

    ServiceCall<String, NotUsed, NotUsed> getMock();

    ServiceCall<NotUsed, NotUsed, NotUsed> addMock();

    default Descriptor descriptor() {
        return named("/aclservice").with(
            restCall(Method.GET,  "/mocks/:id", getMock()),
            restCall(Method.POST, "/mocks", addMock())
        ).withAutoAcl(true);
    }
}
