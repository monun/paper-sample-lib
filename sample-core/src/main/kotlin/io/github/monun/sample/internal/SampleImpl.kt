package io.github.monun.sample.internal

import io.github.monun.sample.Sample

class SampleImpl: Sample {
    override val message: String
        get() = "This is impl message"
}