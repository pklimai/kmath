package space.kscience.kmath.stat

import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class SamplerTest {

    @Test
    fun bufferSamplerTest() {
        val sampler = Sampler { it.chain { nextDouble() } }
        val data = sampler.sampleBuffer(RandomGenerator.default, 100)
        runBlocking { println(data.next()) }
    }
}