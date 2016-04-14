/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api.security

import com.lightbend.lagom.internal.api.transport.CoreHeaderTransformer
import com.lightbend.lagom.api.security.ServicePrincipal
import com.lightbend.lagom.internal.api.transport.CoreRequestHeader
import com.lightbend.lagom.internal.api.transport.CoreResponseHeader

class CoreUserAgentServiceIdentificationStrategy extends CoreHeaderTransformer {

  override def transformClientRequest(request: CoreRequestHeader): CoreRequestHeader = request.principal match {
    case Some(principal: ServicePrincipal) =>
      val serviceName = principal.serviceName()
      request.withHeader(CoreUserAgentServiceIdentificationStrategy.UserAgent, serviceName)
    case _ => request
  }

  override def transformServerRequest(request: CoreRequestHeader): CoreRequestHeader = {
    val userAgent = request.header(CoreUserAgentServiceIdentificationStrategy.UserAgent)
    userAgent match {
      case Some(value) => request.withPrincipal(ServicePrincipal.forServiceNamed(value))
      case None        => request
    }
  }

  override def transformServerResponse(response: CoreResponseHeader, request: CoreRequestHeader): CoreResponseHeader = response

  override def transformClientResponse(response: CoreResponseHeader, request: CoreRequestHeader): CoreResponseHeader = response
}

object CoreUserAgentServiceIdentificationStrategy {
  private final val UserAgent = "User-Agent"
  def apply(): CoreUserAgentServiceIdentificationStrategy = new CoreUserAgentServiceIdentificationStrategy()
}
