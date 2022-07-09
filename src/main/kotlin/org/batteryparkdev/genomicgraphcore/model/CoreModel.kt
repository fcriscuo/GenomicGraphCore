package org.batteryparkdev.genomicgraphcore.model

import org.batteryparkdev.nodeidentifier.model.NodeIdentifier

interface CoreModel {
    abstract fun getNodeIdentifier(): NodeIdentifier

    abstract fun generateLoadModelCypher(): String

    abstract fun isValid(): Boolean

    abstract fun getPubMedId(): Int


}