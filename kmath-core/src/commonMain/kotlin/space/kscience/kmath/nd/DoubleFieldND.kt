package space.kscience.kmath.nd

import space.kscience.kmath.misc.UnstableKMathAPI
import space.kscience.kmath.operations.DoubleField
import space.kscience.kmath.operations.ExtendedField
import space.kscience.kmath.operations.NumbersAddOperations
import space.kscience.kmath.operations.ScaleOperations
import space.kscience.kmath.structures.DoubleBuffer
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(UnstableKMathAPI::class)
public class DoubleFieldND(
    shape: IntArray,
) : BufferedFieldND<Double, DoubleField>(shape, DoubleField, ::DoubleBuffer),
    NumbersAddOperations<StructureND<Double>>,
    ScaleOperations<StructureND<Double>>,
    ExtendedField<StructureND<Double>> {

    override val zero: NDBuffer<Double> by lazy { produce { zero } }
    override val one: NDBuffer<Double> by lazy { produce { one } }

    override fun number(value: Number): NDBuffer<Double> {
        val d = value.toDouble() // minimize conversions
        return produce { d }
    }

    override val StructureND<Double>.buffer: DoubleBuffer
        get() = when {
            !shape.contentEquals(this@DoubleFieldND.shape) -> throw ShapeMismatchException(
                this@DoubleFieldND.shape,
                shape
            )
            this is NDBuffer && this.strides == this@DoubleFieldND.strides -> this.buffer as DoubleBuffer
            else -> DoubleBuffer(strides.linearSize) { offset -> get(strides.index(offset)) }
        }

    @Suppress("OVERRIDE_BY_INLINE")
    override inline fun StructureND<Double>.map(
        transform: DoubleField.(Double) -> Double,
    ): NDBuffer<Double> {
        val buffer = DoubleBuffer(strides.linearSize) { offset -> DoubleField.transform(buffer.array[offset]) }
        return NDBuffer(strides, buffer)
    }

    @Suppress("OVERRIDE_BY_INLINE")
    override inline fun produce(initializer: DoubleField.(IntArray) -> Double): NDBuffer<Double> {
        val array = DoubleArray(strides.linearSize) { offset ->
            val index = strides.index(offset)
            DoubleField.initializer(index)
        }
        return NDBuffer(strides, DoubleBuffer(array))
    }

    @Suppress("OVERRIDE_BY_INLINE")
    override inline fun StructureND<Double>.mapIndexed(
        transform: DoubleField.(index: IntArray, Double) -> Double,
    ): NDBuffer<Double> = NDBuffer(
        strides,
        buffer = DoubleBuffer(strides.linearSize) { offset ->
            DoubleField.transform(
                strides.index(offset),
                buffer.array[offset]
            )
        })

    @Suppress("OVERRIDE_BY_INLINE")
    override inline fun combine(
        a: StructureND<Double>,
        b: StructureND<Double>,
        transform: DoubleField.(Double, Double) -> Double,
    ): NDBuffer<Double> {
        val buffer = DoubleBuffer(strides.linearSize) { offset ->
            DoubleField.transform(a.buffer.array[offset], b.buffer.array[offset])
        }
        return NDBuffer(strides, buffer)
    }

    override fun scale(a: StructureND<Double>, value: Double): StructureND<Double> = a.map { it * value }

    override fun power(arg: StructureND<Double>, pow: Number): NDBuffer<Double> = arg.map { power(it, pow) }

    override fun exp(arg: StructureND<Double>): NDBuffer<Double> = arg.map { exp(it) }

    override fun ln(arg: StructureND<Double>): NDBuffer<Double> = arg.map { ln(it) }

    override fun sin(arg: StructureND<Double>): NDBuffer<Double> = arg.map { sin(it) }
    override fun cos(arg: StructureND<Double>): NDBuffer<Double> = arg.map { cos(it) }
    override fun tan(arg: StructureND<Double>): NDBuffer<Double> = arg.map { tan(it) }
    override fun asin(arg: StructureND<Double>): NDBuffer<Double> = arg.map { asin(it) }
    override fun acos(arg: StructureND<Double>): NDBuffer<Double> = arg.map { acos(it) }
    override fun atan(arg: StructureND<Double>): NDBuffer<Double> = arg.map { atan(it) }

    override fun sinh(arg: StructureND<Double>): NDBuffer<Double> = arg.map { sinh(it) }
    override fun cosh(arg: StructureND<Double>): NDBuffer<Double> = arg.map { cosh(it) }
    override fun tanh(arg: StructureND<Double>): NDBuffer<Double> = arg.map { tanh(it) }
    override fun asinh(arg: StructureND<Double>): NDBuffer<Double> = arg.map { asinh(it) }
    override fun acosh(arg: StructureND<Double>): NDBuffer<Double> = arg.map { acosh(it) }
    override fun atanh(arg: StructureND<Double>): NDBuffer<Double> = arg.map { atanh(it) }
}

public fun AlgebraND.Companion.real(vararg shape: Int): DoubleFieldND = DoubleFieldND(shape)

/**
 * Produce a context for n-dimensional operations inside this real field
 */
public inline fun <R> DoubleField.nd(vararg shape: Int, action: DoubleFieldND.() -> R): R {
    contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
    return DoubleFieldND(shape).run(action)
}