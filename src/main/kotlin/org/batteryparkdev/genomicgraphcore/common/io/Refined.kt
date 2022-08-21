package org.batteryparkdev.genomicgraphcore.common.io

interface Refined<in T> {
    abstract fun isValid(value: T) : Boolean
}