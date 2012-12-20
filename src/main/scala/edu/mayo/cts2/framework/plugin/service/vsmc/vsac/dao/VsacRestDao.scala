package edu.mayo.cts2.framework.plugin.service.vsmc.vsac.dao

;

import scala.collection.JavaConversions._

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import JSON._
import dispatch._
import net.minidev.json.JSONObject
import edu.mayo.cts2.framework.plugin.service.vsmc.util.ListCache
import org.springframework.beans.factory.InitializingBean

@Component
class VsacRestDao extends InitializingBean {

  @scala.reflect.BeanProperty
  @Value("${utsUsername}")
  var username: String = _

  @scala.reflect.BeanProperty
  @Value("${utsPassword}")
  var password: String = _

  @scala.reflect.BeanProperty
  @Value("${vsacRestUrl}")
  var vsacRestUrl: String = _

  var valueSetCache: ListCache[ScalaJSON] = _

  def afterPropertiesSet() {
    valueSetCache = new ListCache(_getAllValueSets _)
  }

  def getValueSet(oid: String) = {
    val json = getJson(
      vsacRestUrl + "/pc/vs/valueset/" + oid)

    parseJSON(json)
  }

  def getValueSetDefinition(oid: String, version: String) = {
    val json = getJson(
      vsacRestUrl + "/pc/vs/valueset/" + oid + "/def/" + version)

    parseJSON(json)
  }

  def getGroupingInfo(oid: String, version: String) = {
    val params =
      Map(
        "_search" -> "false")

    val json = getJson(
      vsacRestUrl + "/pc/vs/valueset/grouping/" + oid + "/def/" + version, params)

    parseJSON(json)
  }

  def getValueSetDefinitionVersions(oid: String) = {
    val json = getJson(
      vsacRestUrl + "/pc/vs/valueset/" + oid + "/def-versions")

    parseJSON(json).rows.foldLeft(Seq[String]())(_ :+ _.name.toString).sortWith(_ < _)
  }

  private def _getAllValueSets: Seq[ScalaJSON] = {
    val json = postJson(vsacRestUrl + "/pc/vs/search", allValueSetsQueryParams)

    parseJSON(json).rows
  }

  def getAllValueSets: Seq[ScalaJSON] = {
    valueSetCache.get()
  }

  private def allValueSetsQueryParams() = {
    Map(
      "query" -> "***ListAll***",
      "cms" -> null,
      "category" -> null,
      "developer" -> null,
      "mu" -> null)
  }

  def getMembersOfValueSet(oid: String, version: String, rows: Int, page: Int): ScalaJSON = {

    val url = vsacRestUrl + "/pc/code/codes"
    val params =
      Map(
        "oid" -> oid,
        "def" -> version,
        "_search" -> "false",
        "rows" -> rows.toString,
        "page" -> page.toString,
        "sidx" -> "code",
        "sord" -> "asc")

    val json = getJson(url, params)

    parseJSON(json)
  }

  private def getJson(urlString: String, queryParams: Map[String, String] = Map()): String = {
    val result = Http(url(urlString).GET.secure.as_!(username, password) <<? queryParams OK as.String).either

    result() match {
      case Right(content) => content
      case Left(StatusCode(404)) => throw new RuntimeException("Not Found");
      case Left(StatusCode(code)) => throw new RuntimeException("Error: " + code);
    }
  }

  private def postJson(urlString: String, params: Map[String, String]): String = {
    val queryJson = new JSONObject(params).toJSONString

    val result = Http(url(urlString).POST.secure.as_!(username, password) << queryJson OK as.String).either

    result() match {
      case Right(content) => content
      case Left(StatusCode(404)) => throw new RuntimeException("Not Found");
      case Left(StatusCode(code)) => throw new RuntimeException("Error: " + code);
    }
  }
}
