/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import sbt._
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning
import play.PlayImport._
import play.core.PlayVersion

object FrontendBuild extends Build with MicroService {

  val appName = "address-capture-frontend"

  override lazy val plugins: Seq[Plugins] = Seq(
    SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin
  )

  override lazy val appDependencies: Seq[ModuleID] = compile ++ testDependencies ++ itDependencies

  private val jacksonVersion = "2.7.4"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "frontend-bootstrap" % "6.5.0",
    "uk.gov.hmrc" %% "play-partials" % "4.2.0",
    "uk.gov.hmrc" %% "play-authorised-frontend" % "5.0.0",
    "uk.gov.hmrc" %% "play-config" % "2.0.1",
    "uk.gov.hmrc" %% "play-json-logger" % "2.1.1",
    "uk.gov.hmrc" %% "govuk-template" % "4.0.0",
    "uk.gov.hmrc" %% "play-health" % "1.1.0",
    "uk.gov.hmrc" %% "play-ui" % "4.14.0",
    "uk.gov.hmrc" %% "http-caching-client" % "5.3.0",
    "com.univocity" % "univocity-parsers" % "1.5.6" withSources(),
    "uk.gov.hmrc" %% "play-async" % "0.2.0",
    "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion,
    "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion,
    "com.fasterxml.jackson.core" % "jackson-annotations" % jacksonVersion,
    "com.fasterxml.jackson.dataformat" % "jackson-dataformat-xml" % jacksonVersion,
    "com.fasterxml.jackson.dataformat" % "jackson-dataformat-properties" % jacksonVersion,
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion
  )

  val testDependencies = baseTestDependencies("test")

  val itDependencies = baseTestDependencies("it")

  private def baseTestDependencies(scope: String) = Seq(
    "org.scalatest" %% "scalatest" % "2.2.4" % scope,
    "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % scope,
    "com.github.simplyscala" %% "scalatest-embedmongo" % "0.2.2" % scope,
    "org.pegdown" % "pegdown" % "1.4.2" % scope,
    "org.scalatestplus" %% "play" % "1.2.0" % scope,
    "org.mockito" % "mockito-all" % "1.10.19" % scope,
    "com.pyruby" % "java-stub-server" % "0.14" % scope,
    "com.github.tomakehurst" % "wiremock" % "1.58" % scope,
    "uk.gov.hmrc" %% "hmrctest" % "1.6.0" % scope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope
  )

}
