package io.github.monun.sample

interface Sample {
    val message: String

    companion object: Sample by LibraryLoader.loadImplement(Sample::class.java)
}