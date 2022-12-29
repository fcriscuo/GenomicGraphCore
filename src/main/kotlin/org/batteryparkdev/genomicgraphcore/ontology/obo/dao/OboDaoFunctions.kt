package org.batteryparkdev.genomicgraphcore.ontology.obo.dao

import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.common.service.XrefUrlPropertyService

fun createPlaceholderOboTerm(newId: String, name: String, namespace: String):String  =
    "CALL apoc.create.node(['OboTerm'], " +
            " {obo_id: ${newId.formatNeo4jPropertyValue()}}, " +
            " { name: ${name.formatNeo4jPropertyValue()}," +
            "created: datetime()};"