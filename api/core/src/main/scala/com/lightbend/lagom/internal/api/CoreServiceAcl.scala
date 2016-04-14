/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api

import com.lightbend.lagom.api.transport.Method

case class CoreServiceAcl(method: Option[Method], pathRegex: Option[String])
