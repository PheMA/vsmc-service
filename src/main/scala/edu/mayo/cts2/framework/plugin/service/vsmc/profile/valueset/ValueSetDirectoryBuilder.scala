package edu.mayo.cts2.framework.plugin.service.vsmc.profile.valueset

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import edu.mayo.cts2.framework.plugin.service.vsmc.vsac.dao.JSON._
import edu.mayo.cts2.framework.filter.`match`.ResolvablePropertyReference
import edu.mayo.cts2.framework.plugin.service.vsmc.vsac.dao.ScalaJSON
import edu.mayo.cts2.framework.filter.`match`.ResolvableMatchAlgorithmReference
import edu.mayo.cts2.framework.filter.directory.AbstractRemovingDirectoryBuilder
import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntry
import edu.mayo.cts2.framework.plugin.service.vsmc.uri.UriUtils._
import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntrySummary

case class ValueSetDirectoryBuilder(
      allPossibleResults:Seq[ScalaJSON] , 
      matchAlgorithmReferences:Set[ResolvableMatchAlgorithmReference] , 
      resolvablePropertyReferences:Set[ResolvablePropertyReference[ScalaJSON]]) 
      extends AbstractRemovingDirectoryBuilder[ScalaJSON,ValueSetCatalogEntrySummary](
          allPossibleResults, 
          matchAlgorithmReferences, 
          resolvablePropertyReferences) {
  
  override
  def transformResults(rawResults:java.util.List[ScalaJSON]): java.util.List[ValueSetCatalogEntrySummary] = {
    rawResults.foldLeft(Seq[ValueSetCatalogEntrySummary]()) {
      (seq,row) => seq :+ rowToValueSet(row)
    }
  }
  
  private def rowToValueSet(jsonRow: ScalaJSON) : ValueSetCatalogEntrySummary = {
    val oid = jsonRow.oid
    val codeSystem = new ValueSetCatalogEntrySummary()
    codeSystem.setAbout(oidToUri(oid))
    
    codeSystem  
  }
}