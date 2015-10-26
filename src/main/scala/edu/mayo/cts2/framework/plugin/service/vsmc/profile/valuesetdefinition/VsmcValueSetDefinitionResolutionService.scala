package edu.mayo.cts2.framework.plugin.service.vsmc.profile.valuesetdefinition

import java.util.Set
import scala.collection.JavaConversions._
import org.apache.commons.lang.StringUtils
import org.apache.commons.collections.CollectionUtils
import org.springframework.stereotype.Component
import edu.mayo.cts2.framework.model.command.Page
import edu.mayo.cts2.framework.model.command.ResolvedReadContext
import edu.mayo.cts2.framework.model.core.CodeSystemReference
import edu.mayo.cts2.framework.model.core.CodeSystemVersionReference
import edu.mayo.cts2.framework.model.core.EntitySynopsis
import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference
import edu.mayo.cts2.framework.model.core.NameAndMeaningReference
import edu.mayo.cts2.framework.model.core.PredicateReference
import edu.mayo.cts2.framework.model.core.PropertyReference
import edu.mayo.cts2.framework.model.core.SortCriteria
import edu.mayo.cts2.framework.model.entity.EntityDirectoryEntry
import edu.mayo.cts2.framework.model.service.core.NameOrURI
import edu.mayo.cts2.framework.model.valuesetdefinition.ResolvedValueSet
import edu.mayo.cts2.framework.model.valuesetdefinition.ResolvedValueSetHeader
import edu.mayo.cts2.framework.plugin.service.vsmc.profile.AbstractService
import edu.mayo.cts2.framework.plugin.service.vsmc.profile.valueset.VsmcValueSetUtils
import edu.mayo.cts2.framework.plugin.service.vsmc.uri.IdType
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ResolvedValueSetResolutionEntityQuery
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ResolvedValueSetResult
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionResolutionService
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.name.ValueSetDefinitionReadId
import javax.annotation.Resource
import edu.mayo.cts2.framework.plugin.service.vsmc.vsac.dao.VsacRestDao
import edu.mayo.cts2.framework.plugin.service.vsmc.vsac.dao.ScalaJSON
import edu.mayo.cts2.framework.plugin.service.vsmc.vsac.dao.JSON._

@Component
class VsmcValueSetDefinitionResolutionService extends AbstractService with ValueSetDefinitionResolutionService {

  @Resource
  var hrefBuilder: HrefBuilder = _

  @Resource
  var vsacRestDao: VsacRestDao = _

  def getSupportedMatchAlgorithms: Set[_ <: MatchAlgorithmReference] = null

  def getSupportedSearchReferences: Set[_ <: PropertyReference] = null

  def getSupportedSortReferences: Set[_ <: PropertyReference] = null

  def getKnownProperties: Set[PredicateReference] = null

  def resolveDefinition(
                         id: ValueSetDefinitionReadId,
                         codeSystemVersions: Set[NameOrURI],
                         codeSystemVersionTag: NameOrURI,
                         query: ResolvedValueSetResolutionEntityQuery,
                         sort: SortCriteria,
                         readContext: ResolvedReadContext,
                         page: Page): ResolvedValueSetResult[EntitySynopsis] = {

    val version = id.getName
    val oid = id.getValueSet.getName

    val labels = vsacRestDao.getValueSetDefinitionLabels(oid)
    var label: String = null
    if (labels != null && !CollectionUtils.isEmpty(labels)) {
      label = labels.get(0)
    }

    val resultJson =
      vsacRestDao.getMembersOfValueSet(oid, version, label, page.getMaxToReturn, page.getPage + 1)

    val total: Int = resultJson.records

    val entrySeq =
      resultJson.rows.foldLeft(Seq[EntitySynopsis]())(_ :+ jsonToEntitySynopsis(_))

    val definitionJson = vsacRestDao.getValueSetDefinition(oid, version, label)

    new ResolvedValueSetResult(buildHeader(definitionJson, resultJson), entrySeq, total <= page.getEnd())
  }

  private def jsonToEntitySynopsis(row: ScalaJSON) = {
    val synopsis = new EntitySynopsis()

    synopsis.setName(row.code)

    val csName = uriResolver.idToName(row.codesystemname, IdType.CODE_SYSTEM)
    synopsis.setNamespace(csName)

    val baseUri = uriResolver.idToBaseUri(csName)

    synopsis.setUri(baseUri + row.code)
    synopsis.setDesignation(row.displayname);

    synopsis.setHref(hrefBuilder.createEntityHref(row))

    synopsis
  }

  /*
   * Since they don't provided versions of used CodeSystems (only
   * CodeSystemNames), we sample the first few entries to pull them.
   */
  private def getCodeSystemVersions(entriesJson: ScalaJSON) = {
    entriesJson.rows.slice(0,25).foldLeft(Map[String,String]())(
      (map, entry) => {
        map ++ Map(entry.codesystemname.toString -> entry.codesystemversion.toString)
      }
    )
  }

  private def buildHeader(json: ScalaJSON, entriesJson: ScalaJSON): ResolvedValueSetHeader = {
    val header = new ResolvedValueSetHeader()

    import VsmcValueSetUtils._

    val valueDefSetRef = buildValueSetDefinitionReference(json, urlConstructor)

    header.setResolutionOf(valueDefSetRef)

    val codeSystems = StringUtils.split(json.codeSystem, ' ')

    val versions = getCodeSystemVersions(entriesJson)

    val versionRefs = codeSystems.foldLeft(Seq[CodeSystemVersionReference]())(
      (seq, entry) => {
        val ref = new CodeSystemVersionReference()

        val csName = entry
        val versionId = versions.getOrElse(csName, "unknown")

        val codeSystemName = uriResolver.idToName(csName, IdType.CODE_SYSTEM)
        val codeSystemUri = uriResolver.idToUri(csName, IdType.CODE_SYSTEM)

        val cs = new CodeSystemReference()
        cs.setContent(codeSystemName)
        cs.setUri(codeSystemUri)

        val csv = new NameAndMeaningReference()
        csv.setContent(csName + "-" + versionId)

        ref.setCodeSystem(cs)
        ref.setVersion(csv)

        seq :+ ref
      })
    header.setResolvedUsingCodeSystem(versionRefs)

    header
  }

  def resolveDefinitionAsEntityDirectory(p1: ValueSetDefinitionReadId, p2: Set[NameOrURI], p3: NameOrURI, p4: ResolvedValueSetResolutionEntityQuery, p5: SortCriteria, p6: ResolvedReadContext, p7: Page): ResolvedValueSetResult[EntityDirectoryEntry] = throw new UnsupportedOperationException()

  def resolveDefinitionAsCompleteSet(p1: ValueSetDefinitionReadId, p2: Set[NameOrURI], p3: NameOrURI, p4: ResolvedReadContext): ResolvedValueSet = throw new UnsupportedOperationException()
}